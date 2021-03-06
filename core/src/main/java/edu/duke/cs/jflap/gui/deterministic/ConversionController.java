/*
*  JFLAP - Formal Languages and Automata Package
*
*
*  Susan H. Rodger
*  Computer Science Department
*  Duke University
*  August 27, 2009

*  Copyright (c) 2002-2009
*  All rights reserved.

*  JFLAP is open source software. Please see the LICENSE for terms.
*
*/

package edu.duke.cs.jflap.gui.deterministic;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.event.AutomataStateListener;
import edu.duke.cs.jflap.automata.fsa.FSATransition;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.automata.fsa.NFAToDFA;
import edu.duke.cs.jflap.automata.graph.Graph;
import edu.duke.cs.jflap.automata.graph.LayoutAlgorithm;
import edu.duke.cs.jflap.automata.graph.layout.GEMLayoutAlgorithm;
import edu.duke.cs.jflap.debug.EDebug;
import edu.duke.cs.jflap.gui.environment.FrameFactory;

/**
 * This is the class that controls the conversion of an NFA to a DFA.
 *
 * @author Thomas Finley
 */
public class ConversionController {
	/** The NFA we're converting to a DFA. */
	private final FiniteStateAutomaton nfa;

	/** The DFA being built. */
	private final FiniteStateAutomaton dfa;

	/** The final answer DFA, used for reference. */
	private final FiniteStateAutomaton answer;

	/** The view that's being repainted. */
	private final ConversionPane view;

	/** The NFA to DFA converter helper object. */
	private final NFAToDFA converter = new NFAToDFA();

	/**
	 * The graph object that we use to help lay out those states that are
	 * automatically placed. Here, vertex objects are the sets of states from
	 * the original NFA.
	 */
	private final Graph<Set<State>> graph = new Graph<>();

	/** The layout algorithm. */
	private final LayoutAlgorithm<Set<State>> layout = new GEMLayoutAlgorithm<>();

	/**
	 * Maps a set of NFA states to a DFA state. This structure is maintained in
	 * part through the <CODE>registerState</CODE> method.
	 */
	private final Map<Set<State>, State> setToState = new HashMap<>();

	/** Maps a state to a set of NFA states. */
	private final Map<State, Set<State>> stateToSet = new HashMap<>();

	/**
	 * Instantiates a <CODE>ConversionController</CODE>.
	 *
	 * @param nfa
	 *            the NFA we're converting to a DFA
	 * @param dfa
	 *            the DFA being built
	 * @param view
	 *            the view that needs to be repainted
	 */
	public ConversionController(final FiniteStateAutomaton nfa, final FiniteStateAutomaton dfa,
			final ConversionPane view) {
		this.nfa = nfa;
		this.dfa = dfa;
		this.view = view;
		// Set up an initial state in the DFA.
		converter.createInitialState(nfa, dfa).setPoint(new Point(50, 50));
		registerState(dfa.getInitialState());
		answer = converter.convertToDFA(nfa);

		// Create the graph.
		initializeGraph();
	}

	/**
	 * This method will expand all states in an automaton.
	 */
	public void complete() {
		final LinkedList<State> stateQueue = new LinkedList<>();
		// Add all states to the state queue.
		stateQueue.addAll(dfa.getStates());
		// When a state is added to the DFA, make sure we know about it.
		final AutomataStateListener listener = e -> {
			if (!e.isAdd()) {
				return;
			}
			// When the DFA gets a new state, add the state to
			// the end of the queue.
			stateQueue.addLast(e.getState());
		};
		dfa.addStateListener(listener);

		while (stateQueue.size() != 0) {
			expandState(stateQueue.removeFirst());
		}

		// Remove the state listener.
		dfa.removeStateListener(listener);
	}

	/**
	 * This method checks to make sure that the finite state automaton is done.
	 * If it is, then the finished automaton is put in a new window.
	 */
	public void done() {
		final int statesRemaining = answer.getStates().size() - dfa.getStates().size(),
				transitionsRemaining = answer.getTransitions().size() - dfa.getTransitions().size();
		if (statesRemaining + transitionsRemaining != 0) {
			final String states = statesRemaining == 0 ? "All the states are there.\n"
					: statesRemaining + " more state" + (statesRemaining == 1 ? "" : "s") + " must be placed.\n";
			final String trans = transitionsRemaining == 0 ? "All the transitions are there.\n"
					: transitionsRemaining + " more transition" + (transitionsRemaining == 1 ? "" : "s")
							+ " must be placed.\n";
			final String message = "The DFA has not been completed.\n" + states + trans;
			JOptionPane.showMessageDialog(view, message);
			return;
		}
		JOptionPane.showMessageDialog(view, "The DFA is fully built!\n" + "It will now be placed in a new window.");
		FrameFactory.createFrame(dfa.clone());
	}

	/**
	 * Expands all the transitions for a state. This will add all of the
	 * transitions of a state with no user interaction.
	 *
	 * @param state
	 *            the state to expand
	 */
	public void expandState(final State state) {
		final List<?> createdStates = converter.expandState(state, nfa, dfa);
		// We want to lay out those states.
		// First, get the sets of states the new states represent.
		final Set<Set<State>> iso = new HashSet<>(setToState.keySet());
		Iterator<?> it = createdStates.iterator();
		while (it.hasNext()) {
			final State dfaState = (State) it.next();
			registerState(dfaState);
			iso.remove(stateToSet.get(dfaState));
		}
		// Run the layout algorithm.
		layout.layout(graph, iso);
		it = createdStates.iterator();
		while (it.hasNext()) {
			final State dfaState = (State) it.next();
			final Object o = stateToSet.get(dfaState);
			dfaState.getPoint().setLocation(graph.pointForVertex(o));
			dfaState.setPoint(dfaState.getPoint());
		}
	}

	/**
	 * Asks the user for input regarding a new state.
	 *
	 * @see edu.duke.cs.jflap.gui.deterministic.TransitionExpanderTool
	 * @param start
	 *            the initial state we're expanding on a terminal
	 * @param point
	 *            the point that the mouse was released on
	 * @param end
	 *            optionally the state the mouse was released on, or
	 *            <CODE>null</CODE>
	 */
	public void expandState(final State start, final Point point, State end) {
		// Ask the user for a terminal.
		final String terminal = JOptionPane.showInputDialog(view, "Expand on what terminal?");
		if (terminal == null) {
			return;
		}

		if (terminal.equals("")) {
			JOptionPane.showMessageDialog(view, "One can't have lambda in the DFA!", "Improper terminal",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		final List<State> states = getStatesForString(start.getLabel(), nfa);
		final List<State> endStates = converter.getStatesOnTerminal(terminal, states, nfa);

		if (endStates.size() == 0) {
			JOptionPane.showMessageDialog(view,
					"The group {" + start.getLabel() + "} does not expand on the terminal " + terminal + "!",
					"Improper expansion", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Ask the user for the states it should expand to.
		String userEnd = "";
		if (end == null) {
			userEnd = JOptionPane.showInputDialog(view,
					"Which group of NFA states will that go to on " + terminal + "?");
		}
		if (userEnd == null) {
			return;
		}
		List<State> userEndStates = endStates;
		try {
			if (end == null) {
				userEndStates = getStatesForString(userEnd, nfa);
			}
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(view, "The list of states is not formatted correctly!", "Format error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// Was the user right?
		if (!converter.containSameStates(userEndStates, endStates)) {
			JOptionPane.showMessageDialog(view, "That list of states is incorrect!", "Wrong set",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		final State end2 = converter.getStateForStates(userEndStates, dfa, nfa);
		if (end == null) {
			end = end2;
		}
		if (end != end2) {
			// Group mismatch.
			JOptionPane
					.showMessageDialog(view,
							"The group {" + start.getLabel() + "} does not go to\n" + "group {" + end.getLabel()
									+ "} on terminal " + terminal + "!",
							"Improper transition", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (end == null) {
			// We need to create the state.
			end = converter.createStateWithStates(dfa, userEndStates, nfa);
			registerState(end);
			end.setPoint(point);
		}

		// Set up the transition.
		final Transition t = new FSATransition(start, end, terminal);
		dfa.addTransition(t);
	}

	private List<State> getStatesForString(final String label, final Automaton automaton) {
		final StringTokenizer tokenizer = new StringTokenizer(label, " \t\n\r\f,q");
		final ArrayList<State> states = new ArrayList<>();
		while (tokenizer.hasMoreTokens()) {
			states.add(automaton.getStateWithID(Integer.parseInt(tokenizer.nextToken())));
		}
		states.remove(null);
		return states;
	}

	private void initializeGraph() {
		final Map<State, Set<State>> stateToSet = new HashMap<>(); // Different...
		final List<State> s = answer.getStates();
		final List<Transition> t = answer.getTransitions();
		for (int i = 0; i < s.size(); i++) {
			final Set<State> fromNfa = new HashSet<>(getStatesForString(s.get(i).getLabel(), nfa));
			stateToSet.put(s.get(i), fromNfa);
			// setToState.put(s[i], fromNfa);
			graph.addVertex(fromNfa, s.get(i).getPoint());
		}
		for (int i = 0; i < t.size(); i++) {
			graph.addEdge(stateToSet.get(t.get(i).getFromState()), stateToSet.get(t.get(i).getToState()));
		}
	}

	public void performFirstLayout() {
		view.validate();
		final Set<Set<State>> isonodes = new HashSet<>();
		final Set<State> initialSet = stateToSet.get(dfa.getInitialState());
		isonodes.add(initialSet);
		graph.addVertex(initialSet, new Point(0, 0));
		layout.layout(graph, isonodes);
		final Rectangle r = view.editor.getBounds(null);
		r.grow(-50, -50);
		graph.moveWithinFrame(r);
		// Set the position of the initial state.
		final Point p = new Point();
		p.setLocation(graph.pointForVertex(initialSet));
		dfa.getInitialState().setPoint(p);
	}

	/**
	 * Registers that a state has been added to the DFA.
	 *
	 * @param state
	 *            the state that was added
	 * @throws IllegalArgumentException
	 *             if the state registered conflicts with any existing
	 */
	private void registerState(final State state) {
		final Set<State> set = new HashSet<>(getStatesForString(state.getLabel(), nfa));
		final State inMap = setToState.get(set);
		EDebug.print(set);
		EDebug.print(inMap);
		EDebug.print(state);

		// EDebug.print(setToState.size());
		// EDebug.print(state.getLabel());
		for (final Object o : setToState.keySet()) {
			EDebug.print(o.toString());
		}

		if (inMap != null && inMap != state) {
			throw new IllegalArgumentException("This set is in the DFA!");
		}
		setToState.put(set, state);
		stateToSet.put(state, set);
	}
}
