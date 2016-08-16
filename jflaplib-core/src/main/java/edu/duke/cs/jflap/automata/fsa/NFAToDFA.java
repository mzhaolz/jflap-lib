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

package edu.duke.cs.jflap.automata.fsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.automata.AlphabetRetriever;
import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.AutomatonChecker;
import edu.duke.cs.jflap.automata.ClosureTaker;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.StatePlacer;
import edu.duke.cs.jflap.automata.Transition;

/**
 * The NFA to DFA converter object can be used to convert a nondeterministic
 * finite state automaton to a deterministic finite state automaton. To use the
 * converter, you first need to get the initial state for the dfa you are
 * building by calling createInitialState. Then you simply continue to expand
 * the states in the dfa by calling expandState until you have no more states in
 * your dfa that you haven't expanded. See convertToDFA for help on how to
 * perform the conversion. (WARNING: You will want to clone the NFA before you
 * call convertToDFA because it will change the automaton.) This nfa to dfa
 * conversion requires that the labels on the states in the dfa list the states
 * from the nfa that they represent because it is from the label that the
 * converter determines which states from the nfa a state in the dfa represents.
 * There is no map used here to allow for the user to create and label states
 * himself, without having to worry about mapping the state to the states it
 * represents.
 *
 * @author Ryan Cavalcante
 */
public class NFAToDFA {
	/**
	 * Creates an instance of <CODE>NFAToDFA</CODE>
	 */
	public NFAToDFA() {
	}

	/**
	 * Returns true if <CODE>states1</CODE> and <CODE>states2</CODE> are
	 * identical (i.e. they contain exactly the same states, and no extras).
	 *
	 * @param nfaStates
	 *            a set of states
	 * @param states2
	 *            a set of states
	 * @return true if <CODE>states1</CODE> and <CODE>states2</CODE> are
	 *         identical (i.e. they contain exactly the same states, and no
	 *         extras).
	 */
	public boolean containSameStates(final List<State> nfaStates, final List<State> states2) {
		final int len1 = nfaStates.size();
		final int len2 = states2.size();
		if (len1 != len2) {
			return false;
		}

		nfaStates.sort((s, t) -> s.hashCode() - t.hashCode());
		states2.sort((s, t) -> s.hashCode() - t.hashCode());

		for (int k = 0; k < nfaStates.size(); k++) {
			// if (!containsState(states1[k], states2))
			// return false;
			if (nfaStates.get(k) != states2.get(k)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns a deterministic finite state automaton equivalent to
	 * <CODE>automaton</CODE>. <CODE>automaton</CODE> is not at all affected by
	 * this conversion.
	 *
	 * @param automaton
	 *            the automaton to convert to a dfa.
	 * @return a deterministic finite state automaton equivalent to
	 *         <CODE>automaton</CODE>.
	 */
	public FiniteStateAutomaton convertToDFA(final Automaton automaton) {
		/** check if actually nfa. */
		final AutomatonChecker ac = new AutomatonChecker();
		if (!ac.isNFA(automaton)) {
			return (FiniteStateAutomaton) automaton.clone();
		}
		/** remove multiple character labels. */
		if (FSALabelHandler.hasMultipleCharacterLabels(automaton)) {
			FSALabelHandler.removeMultipleCharacterLabelsFromAutomaton(automaton);
		}
		/** create new finite state automaton. */
		final FiniteStateAutomaton dfa = new FiniteStateAutomaton();
		final State initialState = createInitialState(automaton, dfa);
		/**
		 * get initial state and add to list of states that need to be expanded.
		 */
		final List<State> list = new ArrayList<>();
		list.add(initialState);
		/** while still more states to be expanded. */
		while (!list.isEmpty()) {
			final List<State> statesToExpand = new ArrayList<>();
			final Iterator<State> it = list.iterator();
			while (it.hasNext()) {
				final State state = it.next();
				/** expand state. */
				statesToExpand.addAll(expandState(state, automaton, dfa));
				it.remove();
			}
			list.addAll(statesToExpand);
		}

		return dfa;
	}

	/**
	 * Returns the initial state for <CODE>dfa</CODE>. A state is created to
	 * represent the initial state from <CODE>nfa</CODE> (and its closure), and
	 * added to <CODE>dfa</CODE>. The initial state of <CODE>dfa</CODE> is set
	 * to the returned state.
	 *
	 * @param nfa
	 *            the nfa being converted to a dfa
	 * @param dfa
	 *            the dfa being built during the conversion.
	 */
	public State createInitialState(final Automaton nfa, final Automaton dfa) {
		/** get closure of initial state from nfa. */
		final State initialState = nfa.getInitialState();
		final List<State> initialClosure = ClosureTaker.getClosure(initialState, nfa);
		/**
		 * create state in dfa to represent closure of initial state in nfa.
		 */
		final State state = createStateWithStates(dfa, initialClosure, nfa);
		// StatePlacer sp = new StatePlacer();
		// Point point = sp.getPointForState(dfa);
		// State state = dfa.createState(point);
		/** set to initial state in dfa. */
		dfa.setInitialState(state);
		// state.setLabel(getStringForStates(initialClosure));
		// if(hasFinalState(initialClosure, nfa)) {
		// dfa.addFinalState(state);
		// }
		return state;
	}

	/**
	 * Creates a state in <CODE>dfa</CODE>, labelled with the set of states in
	 * <CODE>states</CODE>, which are all states from <CODE>nfa</CODE>.
	 *
	 * @param dfa
	 *            the dfa. the automaton the state is added to
	 * @param initialClosure
	 *            the set of states from the nfa that this created state in the
	 *            dfa will represent
	 * @param nfa
	 *            the nfa
	 * @return the created state
	 */
	public State createStateWithStates(final Automaton dfa, final List<State> initialClosure, final Automaton nfa) {
		final StatePlacer sp = new StatePlacer();
		final State state = dfa.createState(sp.getPointForState(dfa));
		state.setLabel(getStringForStates(initialClosure));
		if (hasFinalState(initialClosure, nfa)) {
			dfa.addFinalState(state);
		}
		return state;
	}

	/**
	 * Returns a list of States created by expanding <CODE>state</CODE> in
	 * <CODE>dfa</CODE>. <CODE>state<CODE> is a state in <CODE>dfa</CODE> that
	 * represents a set of states in <CODE>nfa</CODE>. This method adds
	 * transitions to <CODE>dfa</CODE> from <CODE>state</CODE> on all terminals
	 * in the alphabet of <CODE>nfa</CODE> for which it is relevant. For each
	 * letter in the alphabet, you determine the reachable states (from
	 * <CODE>nfa</CODE>) from the set of states represented by
	 * <CODE>state</CODE>. You then create a state in <CODE>dfa</CODE> that
	 * represents all these reachable states and add a transition to DFA
	 * connecting <CODE>state</CODE> and this newly created state.
	 *
	 * @param state
	 *            the state from dfa
	 * @param nfa
	 *            the nfa being converted to a dfa
	 * @param dfa
	 *            the dfa being built from the conversion
	 * @return a list of States created by expanding <CODE>state</CODE>.
	 */
	public List<State> expandState(final State state, final Automaton nfa, final Automaton dfa) {
		final List<State> list = new ArrayList<>();
		final AlphabetRetriever far = new FSAAlphabetRetriever();
		/** for each letter in the alphabet. */
		for (final String letter : far.getAlphabet(nfa)) {
			/**
			 * get states reachable on terminal from all states represented by
			 * state.
			 */
			final List<State> states = getStatesOnTerminal(letter, getStatesForState(state, nfa), nfa);
			/** if any reachable states on terminal. */
			if (states.size() > 0) {
				/**
				 * get state from dfa that represents list of reachable states
				 * in nfa.
				 */
				State toState = getStateForStates(states, dfa, nfa);
				/** if no such state. */
				if (toState == null) {
					/** create state, add to list */
					toState = createStateWithStates(dfa, states, nfa);
					// StatePlacer sp = new StatePlacer();
					// Point point = sp.getPointForState(dfa);
					// toState = dfa.createState(point);
					// toState.setLabel(getStringForStates(states));
					// if(hasFinalState(states, nfa)) {
					// dfa.addFinalState(toState);
					// }
					list.add(toState);
				}
				/**
				 * add transition to dfa from state to state that represents
				 * reachables states on terminal from nfa.
				 */
				final Transition transition = new FSATransition(state, toState, letter);
				dfa.addTransition(transition);
			}
		}
		return list;
	}

	/**
	 * Returns the State mapped to <CODE>states</CODE>.
	 *
	 * @param states
	 *            the states
	 * @param dfa
	 *            the automaton that contains a state that is mapped to
	 *            <CODE>states</CODE>
	 * @return the State mapped to <CODE>states</CODE>.
	 */
	public State getStateForStates(final List<State> states, final Automaton dfa, final Automaton nfa) {
		final List<State> dfaStates = dfa.getStates();
		for (int k = 0; k < dfaStates.size(); k++) {
			final List<State> nfaStates = getStatesForState(dfaStates.get(k), nfa);
			if (containSameStates(nfaStates, states)) {
				return dfaStates.get(k);
			}
		}
		return null;
	}

	/**
	 * Returns the State array mapped to <CODE>state</CODE>.
	 *
	 * @param state
	 *            the state.
	 * @param automaton
	 *            the nfa whose states are represented by <CODE>state</CODE>.
	 * @return the State array mapped to <CODE>state</CODE>.
	 */
	public List<State> getStatesForState(final State state, final Automaton automaton) {
		if (state.getLabel() == null) {
			return Collections.emptyList();
		}
		final StringTokenizer tokenizer = new StringTokenizer(state.getLabel(), " \t\n\r\f,q");
		final List<State> states = new ArrayList<>();
		while (tokenizer.hasMoreTokens()) {
			states.add(automaton.getStateWithID(Integer.parseInt(tokenizer.nextToken())));
		}
		return states;
	}

	/**
	 * Returns all states reachable on <CODE>terminal</CODE> from
	 * <CODE>states</CODE>, including the closure of all reachable states.
	 *
	 * @param terminal
	 *            the terminal (alphabet character)
	 * @param list2
	 *            the set of states that we are checking to see if they have
	 *            transitions on <CODE>terminal</CODE>.
	 * @param automaton
	 *            the automaton.
	 * @return all states reachable on <CODE>terminal</CODE> from
	 *         <CODE>states</CODE>, including the closure of all reachable
	 *         states.
	 */
	public List<State> getStatesOnTerminal(final String terminal, final List<State> list2, final Automaton automaton) {
		final Set<State> list = new HashSet<>();
		for (int k = 0; k < list2.size(); k++) {
			final State state = list2.get(k);
			final List<Transition> transitions = automaton.getTransitionsFromState(state);
			for (int i = 0; i < transitions.size(); i++) {
				final FSATransition transition = (FSATransition) transitions.get(i);
				if (transition.getLabel().equals(terminal)) {
					final State toState = transition.getToState();
					final List<State> closure = ClosureTaker.getClosure(toState, automaton);
					for (int j = 0; j < closure.size(); j++) {
						list.add(closure.get(j));
					}
				}
			}
		}
		return Lists.newArrayList(list);
	}

	/**
	 * Returns a string representation of <CODE>states</CODE>.
	 *
	 * @param initialClosure
	 *            the set of states.
	 * @return a string representation of <CODE>states</CODE>.
	 */
	public String getStringForStates(final List<State> initialClosure) {
		final StringBuffer buffer = new StringBuffer();
		for (int k = 0; k < initialClosure.size() - 1; k++) {
			buffer.append(Integer.toString(initialClosure.get(k).getID()));
			buffer.append(",");
		}
		buffer.append(Integer.toString(initialClosure.get(initialClosure.size() - 1).getID()));
		return buffer.toString();
	}

	/**
	 * Returns true if one or more of the states in <CODE>states</CODE> are
	 * final.
	 *
	 * @param initialClosure
	 *            the set of states
	 * @param automaton
	 *            the automaton that contains <CODE>states</CODE>
	 * @return true if one or more of the states in <CODE>states</CODE> are
	 *         final
	 */
	public boolean hasFinalState(final List<State> initialClosure, final Automaton automaton) {
		for (int k = 0; k < initialClosure.size(); k++) {
			if (automaton.isFinalState(initialClosure.get(k))) {
				return true;
			}
		}
		return false;
	}
}
