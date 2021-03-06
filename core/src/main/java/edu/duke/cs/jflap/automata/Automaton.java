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

package edu.duke.cs.jflap.automata;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.automata.event.AutomataNoteEvent;
import edu.duke.cs.jflap.automata.event.AutomataNoteListener;
import edu.duke.cs.jflap.automata.event.AutomataStateEvent;
import edu.duke.cs.jflap.automata.event.AutomataStateListener;
import edu.duke.cs.jflap.automata.event.AutomataTransitionEvent;
import edu.duke.cs.jflap.automata.event.AutomataTransitionListener;
import edu.duke.cs.jflap.automata.mealy.MooreMachine;
import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * The automata object is the root class for the representation of all forms of
 * automata, including FSA, PDA, and Turing machines. This object does NOT
 * simulate the behavior of any of those machines; it simply maintains a
 * structure that holds and maintains the data necessary to represent such a
 * machine.
 *
 * @see edu.duke.cs.jflap.automata.State
 * @see edu.duke.cs.jflap.automata.Transition
 *
 * @author Thomas Finley
 */
public class Automaton implements Serializable, Cloneable {
	private static final long serialVersionUID = 0L;

	/**
	 * Turn a into b. This code is copied from the clone method and tweaked. If
	 * I am daring, I will remove it from clone and call this.
	 *
	 * @param dest
	 * @param src
	 */
	public static void become(final Automaton dest, final Automaton src) {

		dest.clear();
		// Copy over the states.
		final HashMap<State, State> map = new HashMap<>(); // Old states to new
		// states.
		src.states.forEach(state -> {
			final State nstate = new State(state.getID(), new Point(state.getPoint()), dest);
			nstate.setLabel(state.getLabel());
			nstate.setName(state.getName());
			map.put(state, nstate);
			dest.addState(nstate);
			/*
			 * If it is a Moore machine, set the state output.
			 */
			if (src instanceof MooreMachine) {
				((MooreMachine) dest).setOutput(nstate, ((MooreMachine) src).getOutput(state));
			}
		});

		// Set special states.
		src.finalStates.forEach(state -> {
			dest.addFinalState(map.get(state));
		});

		dest.setInitialState(map.get(src.getInitialState()));

		// Copy over the transitions.
		src.states.forEach(state -> {
			final State from = map.get(state);
			src.getTransitionsFromState(state).stream().forEach(transition -> {
				final State to = map.get(transition.getToState());
				// call clone instead of copy so that GUI can get appropriately
				// updated
				final Transition toBeAdded = transition.clone();
				toBeAdded.setFromState(from);
				toBeAdded.setToState(to);
				dest.addTransition(toBeAdded);
			});
		});

		final List<Note> srcNotes = src.getNotes();
		final List<Note> destNotes = dest.getNotes();

		checkArgument(srcNotes.size() == destNotes.size());

		IntStream.range(0, srcNotes.size()).forEach(i -> {
			dest.addNote(new Note(srcNotes.get(i).getAutoPoint(), srcNotes.get(i).getText()));
			destNotes.get(i).initializeForView(srcNotes.get(i).getView());
		});

		dest.setEnvironmentFrame(src.getEnvironmentFrame());
	}

	private final transient Logger logger = LoggerFactory.getLogger(Automaton.class);

	// AUTOMATA SPECIFIC CRAP
	// This includes lots of stuff not strictly necessary for the
	// defintion of automata, but stuff that makes it at least
	// somewhat efficient in the process.
	private String fileName = ""; // Jinghui bug fixing.

	private EnvironmentFrame myEnvFrame = null;

	/** The collection of states in this automaton. */
	protected Set<State> states;

	/** The cached array of states. */
	private List<State> cachedStates = null;

	/** The cached array of transitions. */
	private List<Transition> cachedTransitions = null;

	/** The cached array of final states. */
	private List<State> cachedFinalStates = null;

	/**
	 * The collection of final states in this automaton. This is a subset of the
	 * "states" collection.
	 */
	protected Set<State> finalStates;

	/** The initial state. */
	protected State initialState = null;

	/** The list of transitions in this automaton. */
	protected Set<Transition> transitions;

	/**
	 * A mapping from states to a list holding transitions from those states.
	 */
	private HashMap<State, List<Transition>> transitionFromStateMap = new HashMap<>();

	/**
	 * A mapping from state to a list holding transitions to those states.
	 */
	private HashMap<State, List<Transition>> transitionToStateMap = new HashMap<>();

	/**
	 * A mapping from states to an array holding transitions from a state. This
	 * is a sort of cashing.
	 */
	private HashMap<State, List<Transition>> transitionArrayFromStateMap = new HashMap<>();

	/**
	 * A mapping from states to an array holding transitions from a state. This
	 * is a sort of cashing.
	 */
	private HashMap<State, List<Transition>> transitionArrayToStateMap = new HashMap<>();

	private final ArrayList<Note> myNotes = new ArrayList<>();

	public Color myColor = new Color(255, 255, 150);

	// LISTENER STUFF
	// Structures related to this object as something that generates
	// events, in particular as it pertains to the removal and
	// addition of states and transtions.
	private transient HashSet<AutomataTransitionListener> transitionListeners = new HashSet<>();

	private transient HashSet<AutomataStateListener> stateListeners = new HashSet<>();

	private transient HashSet<AutomataNoteListener> noteListeners = new HashSet<>();

	/**
	 * Creates an instance of <CODE>Automaton</CODE>. The created instance has
	 * no states and no transitions.
	 */
	public Automaton() {
		states = new HashSet<>();
		transitions = new HashSet<>();
		finalStates = new HashSet<>();
		initialState = null;
	}

	/**
	 * Adds a single final state to the set of final states. Note that the
	 * general automaton can have an unlimited number of final states, and
	 * should have at least one. The state that is added should already be one
	 * of the existing states.
	 *
	 * @param finalState
	 *            a new final state to add to the collection of final states
	 */
	public void addFinalState(final State finalState) {
		cachedFinalStates = null;
		finalStates.add(finalState);
		distributeStateEvent(new AutomataStateEvent(this, finalState, false, false, true));
	}

	public void addNote(final Note note) {
		myNotes.add(note);
		distributeNoteEvent(new AutomataNoteEvent(this, note, true, false));
	}

	/**
	 * Adds a <CODE>AutomataNoteListener</CODE> to this automata.
	 *
	 * @param listener
	 *            the listener to add
	 */
	public void addNoteListener(final AutomataNoteListener listener) {
		noteListeners.add(listener);
	}

	/**
	 * Adds a new state to this automata. Clients should use the
	 * <CODE>createState</CODE> method instead.
	 *
	 * @param state
	 *            the state to add
	 */
	protected final void addState(final State state) {
		states.add(state);
		transitionFromStateMap.put(state, new LinkedList<Transition>());
		transitionToStateMap.put(state, new LinkedList<Transition>());
		cachedStates = null;
		distributeStateEvent(new AutomataStateEvent(this, state, true, false, false));
	}

	/**
	 * Adds a <CODE>AutomataStateListener</CODE> to this automata.
	 *
	 * @param listener
	 *            the listener to add
	 */
	public void addStateListener(final AutomataStateListener listener) {
		stateListeners.add(listener);
	}

	/**
	 * Adds a <CODE>Transition</CODE> to this automaton. This method may do
	 * nothing if the transition is already in the automaton.
	 *
	 * @param trans
	 *            the transition object to add to the automaton
	 */
	public void addTransition(final Transition trans) {
		if (!getTransitionClass().isInstance(trans) || trans == null) {
			throw (new IncompatibleTransitionException());
		}
		if (transitions.contains(trans)) {
			return;
		}
		if (trans.getToState() == null || trans.getFromState() == null) {
			return;
		}
		transitions.add(trans);
		if (transitionFromStateMap == null) {
			transitionFromStateMap = new HashMap<>();
		}
		List<Transition> list = transitionFromStateMap.get(trans.getFromState());
		list.add(trans);
		if (transitionToStateMap == null) {
			transitionToStateMap = new HashMap<>();
		}
		list = transitionToStateMap.get(trans.getToState());
		list.add(trans);
		transitionArrayFromStateMap.remove(trans.getFromState());
		transitionArrayToStateMap.remove(trans.getToState());
		cachedTransitions = null;

		distributeTransitionEvent(new AutomataTransitionEvent(this, trans, true, false));
	}

	/**
	 * Adds a <CODE>AutomataTransitionListener</CODE> to this automata.
	 *
	 * @param listener
	 *            the listener to add
	 */
	public void addTransitionListener(final AutomataTransitionListener listener) {
		transitionListeners.add(listener);
	}

	/**
	 * Reset all non-transient data structures.
	 */
	protected void clear() {

		final HashSet<Transition> t = new HashSet<>(transitions);
		for (final Transition o : t) {
			removeTransition(o);
		}
		transitions = new HashSet<>();

		final HashSet<State> s = new HashSet<>(states);
		for (final State o : s) {
			removeState(o);
		}
		states = new HashSet<>();

		finalStates = new HashSet<>();

		initialState = null;

		cachedStates = null;

		cachedTransitions = null;

		cachedFinalStates = null;

		transitionFromStateMap = new HashMap<>();
		transitionToStateMap = new HashMap<>();

		transitionArrayFromStateMap = new HashMap<>();

		transitionArrayToStateMap = new HashMap<>();

		while (myNotes.size() != 0) {
			final AutomatonPane ap = myNotes.get(0).getView();
			ap.remove(myNotes.get(0));
			ap.repaint();
			deleteNote(myNotes.get(0));
		}
	}

	/**
	 * Creates a clone of this automaton.
	 *
	 * @return a clone of this automaton, or <CODE>null</CODE> if the clone
	 *         failed
	 */
	@Override
	public Automaton clone() {
		Automaton a;
		// Try to create a new object.
		try {
			a = getClass().newInstance();
		} catch (final Throwable e) {
			// Well golly, we're sure screwed now!
			logger.error("Warning: clone of automaton failed: {}", e.getMessage());
			return null;
		}
		a.setEnvironmentFrame(getEnvironmentFrame());

		// Copy over the states.
		final HashMap<State, State> map = new HashMap<>(); // Old states to new
		// states.
		states.forEach(state -> {
			final State newState = new State(state.getID(), new Point(state.getPoint()), a);
			newState.setLabel(state.getLabel());
			newState.setName(state.getName());
			map.put(state, newState);
			a.addState(newState);
			if (this instanceof MooreMachine) {
				((MooreMachine) a).setOutput(newState, ((MooreMachine) this).getOutput(state));
			}
		});

		finalStates.forEach(state -> {
			a.addFinalState(map.get(state));
		});

		a.setInitialState(map.get(getInitialState()));

		// Copy over the transitions.

		states.forEach(state -> {
			final State from = map.get(state);
			getTransitionsFromState(state).forEach(transition -> {
				final State to = map.get(transition.getToState());
				System.err.println("Transition name: " + transition.toString());
				final Transition toBeAdded = transition.clone();
				toBeAdded.setFromState(from);
				toBeAdded.setToState(to);
				System.err.println("toBeAdded is null: " + (toBeAdded == null));
				System.err.println("toBeAdded.from is null: " + (toBeAdded.getFromState() == null));
				System.err.println("toBeAdded.to is null: " + (toBeAdded.getToState() == null));
				a.addTransition(toBeAdded);
			});
		});

		final List<Note> notes = getNotes();
		final List<Note> copyNotes = a.getNotes();
		checkArgument(notes.size() == copyNotes.size());

		IntStream.range(0, notes.size()).forEach(i -> {
			a.addNote(new Note(notes.get(i).getAutoPoint(), notes.get(i).getText()));
			copyNotes.get(i).setView(notes.get(i).getView());
		});
		// Should be done now!
		return a;
	}

	/**
	 * Creates a state, inserts it in this automaton, and returns that state.
	 * The ID for the state is set appropriately.
	 *
	 * @param point
	 *            the point to put the state at
	 */
	public State createState(final Point point) {
		int i = 0;
		while (getStateWithID(i) != null) {
			i++;
		}
		final State state = new State(i, point, this);
		addState(state);
		return state;
	}

	/**
	 * Creates a state, inserts it in this automaton, and returns that state.
	 * The ID for the state is set appropriately.
	 *
	 * @param point
	 *            the point to put the state at
	 */
	public final State createStateWithId(final Point point, final int i) {
		final State state = new State(i, point, this);
		addState(state);
		return state;
	}

	public void deleteNote(final Note note) {
		for (int k = 0; k < myNotes.size(); k++) {
			if (note == myNotes.get(k)) {
				myNotes.remove(k);
			}
		}
		distributeNoteEvent(new AutomataNoteEvent(this, note, true, false));
	}

	/**
	 * Gives an automata note change event to all state listeners.
	 *
	 * @param event
	 *            the event to distribute
	 */
	void distributeNoteEvent(final AutomataNoteEvent event) {
		final Iterator<AutomataNoteListener> it = noteListeners.iterator();
		while (it.hasNext()) {
			final AutomataNoteListener listener = it.next();
			listener.automataNoteChange(event);
		}
	}

	/**
	 * Gives an automata state change event to all state listeners.
	 *
	 * @param event
	 *            the event to distribute
	 */
	void distributeStateEvent(final AutomataStateEvent event) {
		final Iterator<AutomataStateListener> it = stateListeners.iterator();
		while (it.hasNext()) {
			final AutomataStateListener listener = it.next();
			listener.automataStateChange(event);
		}
	}

	/**
	 * Gives an automata transition change event to all transition listeners.
	 *
	 * @param event
	 *            the event to distribute
	 */
	void distributeTransitionEvent(final AutomataTransitionEvent event) {
		final Iterator<AutomataTransitionListener> it = transitionListeners.iterator();
		while (it.hasNext()) {
			final AutomataTransitionListener listener = it.next();
			listener.automataTransitionChange(event);
		}
	}

	/**
	 * Gets the Environment Frame the automaton is in.
	 *
	 * @return the environment frame.
	 */
	public EnvironmentFrame getEnvironmentFrame() {
		return myEnvFrame;
	}

	public String getFileName() {
		int last = fileName.lastIndexOf("\\");
		if (last == -1) {
			last = fileName.lastIndexOf("/");
		}

		return fileName.substring(last + 1);
	}

	public String getFilePath() {
		int last = fileName.lastIndexOf("\\");
		if (last == -1) {
			last = fileName.lastIndexOf("/");
		}

		return fileName.substring(0, last + 1);
	}

	/**
	 * Returns an array that contains every state in this automaton that is a
	 * final state. The array is not necessarily gauranteed to be in any
	 * particular order.
	 *
	 * @return an array containing all final states of this automaton
	 */
	public List<State> getFinalStates() {
		if (cachedFinalStates == null) {
			cachedFinalStates = new LinkedList<>(finalStates);
		}
		return cachedFinalStates;
	}

	/**
	 * Returns the start state for this automaton.
	 *
	 * @return the start state for this automaton
	 */
	public State getInitialState() {
		return initialState;
	}

	public List<Note> getNotes() {
		return myNotes;
	}

	// /**
	// * Gets the map of blocks for this automaton.
	// *
	// * @return the map of blocks
	// */
	// public Map getBlockMap() {
	// return blockMap;
	// }

	/**
	 * Returns an array that contains every state in this automaton. The array
	 * is gauranteed to be in order of ascending state IDs.
	 *
	 * @return an array containing all the states in this automaton
	 */
	public List<State> getStates() {
		if (cachedStates == null) {
			cachedStates = new ArrayList<>(states);
			cachedStates.sort(new Comparator<State>() {
				@Override
				public int compare(final State o1, final State o2) {
					return o1.getID() - o2.getID();
				}

				@Override
				public boolean equals(final Object o) {
					return this == o;
				}
			});
		}
		return cachedStates;
	}

	/**
	 * Returns the <CODE>State</CODE> in this automaton with this ID.
	 *
	 * @param id
	 *            the ID to look for
	 * @return the instance of <CODE>State</CODE> in this automaton with this
	 *         ID, or <CODE>null</CODE> if no such state exists
	 */
	public State getStateWithID(final int id) {
		final Iterator<State> it = states.iterator();
		while (it.hasNext()) {
			final State state = it.next();
			if (state.getID() == id) {
				return state;
			}
		}
		return null;
	}

	/**
	 * Returns the particular class that added transition objects should be a
	 * part of. Subclasses may wish to override in case they want to restrict
	 * the type of transitions their automaton will respect. By default this
	 * method simply returns the class object for the abstract class
	 * <CODE>automata.Transition</CODE>.
	 *
	 * @see #addTransition
	 * @see edu.duke.cs.jflap.automata.Transition
	 * @return the <CODE>Class</CODE> object that all added transitions should
	 *         derive from
	 */
	protected Class<? extends edu.duke.cs.jflap.automata.Transition> getTransitionClass() {
		return edu.duke.cs.jflap.automata.Transition.class;
	}

	/**
	 * Retrieves all transitions.
	 *
	 * @return an array containing all transitions for this automaton
	 */
	public List<Transition> getTransitions() {
		if (cachedTransitions == null) {
			cachedTransitions = Lists.newArrayList(transitions);
		}
		return cachedTransitions;
	}

	/**
	 * Retrieves all transitions that eminate from a state.
	 *
	 * @param from
	 *            the <CODE>State</CODE> from which returned transitions should
	 *            come from
	 * @return an array of the <CODE>Transition</CODE> objects emanating from
	 *         this state
	 */
	public List<Transition> getTransitionsFromState(final State from) {
		List<Transition> toReturn = transitionArrayFromStateMap.get(from);
		if (toReturn == null) {
			final List<Transition> list = transitionFromStateMap.get(from);
			toReturn = list;
			transitionArrayFromStateMap.put(from, toReturn);
		}
		return toReturn;
	}

	/**
	 * Retrieves all transitions going from one given state to another given
	 * state.
	 *
	 * @param from
	 *            the state all returned transitions should come from
	 * @param to
	 *            the state all returned transitions should go to
	 * @return an array of all transitions coming from <CODE>from</CODE> and
	 *         going to <CODE>to</CODE>
	 */
	public List<Transition> getTransitionsFromStateToState(final State from, final State to) {
		final List<Transition> list = new ArrayList<>();
		getTransitionsFromState(from).stream().forEach(transition -> {
			if (transition.getToState() == to) {
				list.add(transition);
			}
		});
		return list;
	}

	/**
	 * Retrieves all transitions that travel from a state.
	 *
	 * @param to
	 *            the <CODE>State</CODE> to which all returned transitions
	 *            should go to
	 * @return an array of all <CODE>Transition</CODE> objects going to the
	 *         State
	 */
	public List<Transition> getTransitionsToState(final State to) {
		List<Transition> toReturn = transitionArrayToStateMap.get(to);
		if (toReturn == null) {
			final List<Transition> list = transitionToStateMap.get(to);
			toReturn = list;
			transitionArrayToStateMap.put(to, toReturn);
		}
		return toReturn;
	}

	@Override
	public int hashCode() {
		// EDebug.print("The Hash is that is hashed, is truly hashed");
		int ret = 0;
		for (final Object o : states) {
			ret += ((State) o).specialHash();
		}
		for (final Object o : transitions) {
			ret += ((Transition) o).specialHash();
		}
		for (final Object o : myNotes) {
			ret += ((Note) o).specialHash();
		}
		ret += finalStates.hashCode();
		ret += initialState == null ? 0 : (int) (initialState.specialHash() * Math.PI);

		// EDebug.print(ret);
		return ret;
	}

	/**
	 * Determines if the state passed in is in the set of final states.
	 *
	 * @param state
	 *            the state to determine if is final
	 * @return <CODE>true</CODE> if the state is a final state in this
	 *         automaton, <CODE>false</CODE> if it is not
	 */
	public boolean isFinalState(final State state) {
		return finalStates.contains(state);
	}

	/**
	 * Determines if the state passed in is the initial states. Added for JFLAP
	 * 6.3
	 *
	 * @param state
	 *            the state to determine if is final
	 * @return <CODE>true</CODE> if the state is a final state in this
	 *         automaton, <CODE>false</CODE> if it is not
	 */
	public boolean isInitialState(final State state) {
		return (state.equals(initialState));
	}

	/**
	 * Tells if the passed in object is indeed a state in this automaton.
	 *
	 * @param state
	 *            the state to check for membership in the automaton
	 * @return <CODE>true</CODE> if this state is in the automaton,
	 *         <CODE>false</CODE>otherwise
	 */
	public boolean isState(final State state) {
		return states.contains(state);
	}

	/**
	 * This handles deserialization so that the listener sets are reset to avoid
	 * null pointer exceptions when one tries to add listeners to the object.
	 *
	 * @deprecated
	 * @param in
	 *            the input stream for the object @
	 */
	@Deprecated
	private void readObject(final java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		// Reset all nonread objects.
		/*
		 * resetForLoad();
		 *
		 * // Do the reading in of objects. int version = in.readInt(); if
		 * (version >= 0) { // Adjust by version. // The reading for version 0
		 * of this object. Set s = (Set) in.readObject(); Iterator it =
		 * s.iterator(); while (it.hasNext()) addState((State) it.next());
		 *
		 * initialState = (State) in.readObject(); finalStates = (Set)
		 * in.readObject(); // Let the class take care of the transition stuff.
		 * Set trans = (Set) in.readObject(); it = trans.iterator(); while
		 * (it.hasNext()) addTransition((Transition) it.next()); if (this
		 * instanceof TuringMachine) { ((TuringMachine) this).tapes =
		 * in.readInt(); } } while (!in.readObject().equals("SENT")) ; // Read
		 * until sentinel.
		 */
	}

	/**
	 * Removes a state from the set of final states. This will not remove a
	 * state from the list of states; it shall merely make it nonfinal.
	 *
	 * @param state
	 *            the state to make not a final state
	 */
	public void removeFinalState(final State state) {
		cachedFinalStates = null;
		finalStates.remove(state);
		distributeStateEvent(new AutomataStateEvent(this, state, false, false, true));
	}

	/**
	 * Removes a <CODE>AutomataNoteListener</CODE> from this automata.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeNoteListener(final AutomataNoteListener listener) {
		noteListeners.remove(listener);
	}

	/**
	 * Removes a state from the automaton. This will also remove all transitions
	 * associated with this state.
	 *
	 * @param state
	 *            the state to remove
	 */
	public void removeState(final State state) {
		getTransitionsFromState(state).forEach(x -> removeTransition(x));
		getTransitionsToState(state).forEach(x -> removeTransition(x));
		
		distributeStateEvent(new AutomataStateEvent(this, state, false, false, false));
		states.remove(state);
		finalStates.remove(state);
		if (state == initialState) {
			initialState = null;
		}

		transitionFromStateMap.remove(state);
		transitionToStateMap.remove(state);

		transitionArrayFromStateMap.remove(state);
		transitionArrayToStateMap.remove(state);

		cachedStates = null;
	}

	/**
	 * Removes a <CODE>AutomataStateListener</CODE> from this automata.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeStateListener(final AutomataStateListener listener) {
		stateListeners.remove(listener);
	}

	/**
	 * Removes a <CODE>Transition</CODE> from this automaton.
	 *
	 * @param trans
	 *            the transition object to remove from this automaton.
	 */
	public void removeTransition(final Transition trans) {
		transitions.remove(trans);
		List<Transition> l = transitionFromStateMap.get(trans.getFromState());
		l.remove(trans);
		l = transitionToStateMap.get(trans.getToState());
		l.remove(trans);
		// Remove cached arrays.
		transitionArrayFromStateMap.remove(trans.getFromState());
		transitionArrayToStateMap.remove(trans.getToState());
		cachedTransitions = null;

		distributeTransitionEvent(new AutomataTransitionEvent(this, trans, false, false));
	}

	/**
	 * Removes a <CODE>AutomataTransitionListener</CODE> from this automata.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeTransitionListener(final AutomataTransitionListener listener) {
		transitionListeners.remove(listener);
	}

	/**
	 * Replaces a <CODE>Transition</CODE> in this automaton with another
	 * transition with the same from and to states. This method will delete the
	 * old if the transition is already in the automaton.
	 *
	 * @param oldTrans
	 *            the transition object to add to the automaton
	 * @param newTrans
	 *            the transition object to add to the automaton
	 */
	public void replaceTransition(final Transition oldTrans, final Transition newTrans) {
		if (!getTransitionClass().isInstance(newTrans)) {
			throw new IncompatibleTransitionException();
		}
		if (oldTrans.equals(newTrans)) {
			return;
		}
		if (transitions.contains(newTrans)) {
			removeTransition(oldTrans);
			return;
		}
		if (!transitions.remove(oldTrans)) {
			throw new IllegalArgumentException("Replacing transition that not already in the automaton!");
		}
		transitions.add(newTrans);
		List<Transition> list = transitionFromStateMap.get(oldTrans.getFromState());
		list.set(list.indexOf(oldTrans), newTrans);
		list = transitionToStateMap.get(oldTrans.getToState());
		list.set(list.indexOf(oldTrans), newTrans);
		transitionArrayFromStateMap.remove(oldTrans.getFromState());
		transitionArrayToStateMap.remove(oldTrans.getToState());
		cachedTransitions = null;
		distributeTransitionEvent(new AutomataTransitionEvent(this, newTrans, true, false));
	}

	// /**
	// * A mapping from the name of an automaton to the automaton. Used for
	// * referencing the same automaton from multiple buliding blocks
	// */
	// private HashMap blockMap = new HashMap();

	public void selectStatesWithinBounds(final Rectangle bounds) {
		for (final State state : getStates()) {
			state.setSelect(false);
			if (bounds.contains(state.getPoint())) {
				state.setSelect(true);
			}
		}
	}

	/**
	 * Changes the environment frame this automaton is in.
	 *
	 * @param frame
	 *            the environment frame
	 */
	public void setEnvironmentFrame(final EnvironmentFrame frame) {
		myEnvFrame = frame;
	}

	public void setFilePath(final String name) {
		fileName = name;
	}

	/**
	 * Sets the new initial state to <CODE>initialState</CODE> and returns what
	 * used to be the initial state, or <CODE>null</CODE> if there was no
	 * initial state. The state specified should already exist in the automata.
	 *
	 * @param initialState
	 *            the new initial state
	 * @return the old initial state, or <CODE>null</CODE> if there was no
	 *         initial state
	 */
	public State setInitialState(final State initialState) {
		final State oldInitialState = this.initialState;
		this.initialState = initialState;
		distributeStateEvent(new AutomataStateEvent(this, initialState, false, false, true));
		return oldInitialState;
	}

	/**
	 * Returns a string representation of this <CODE>Automaton</CODE>.
	 */
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append(super.toString());
		buffer.append('\n');
		for (final State state : getStates()) {
			if (initialState == state) {
				buffer.append("--> ");
			}
			buffer.append(state);
			if (isFinalState(state)) {
				buffer.append(" **FINAL**");
			}
			buffer.append('\n');
			for (final Transition transition : getTransitionsFromState(state)) {
				buffer.append('\t');
				buffer.append(transition);
				buffer.append('\n');
			}
		}

		return buffer.toString();
	}

	/**
	 * This handles serialization. No longer used.
	 */
	private void writeObject(final java.io.ObjectOutputStream out) throws java.io.IOException {
		/*
		 * out.writeInt(0); // Version of the stream. // Version 0 outstuff...
		 * out.writeObject(states); out.writeObject(initialState);
		 * out.writeObject(finalStates); out.writeObject(transitions); if (this
		 * instanceof TuringMachine) { out.writeInt(((TuringMachine)
		 * this).tapes); } out.writeObject("SENT"); // The sentinel object.
		 */
	}
}
