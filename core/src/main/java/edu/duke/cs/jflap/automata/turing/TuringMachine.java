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

package edu.duke.cs.jflap.automata.turing;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.Point;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.Note;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.gui.action.OpenAction;

/**
 * This subclass of <CODE>Automaton</CODE> is specifically for a definition of a
 * Turing machine, possibly with multiple tapes.
 *
 * We also redefine equals(), and clone(), as well as become() to deal with
 * Building Blocks, so that we don't have to deal with them in Automaton.
 *
 * @author Thomas Finley, Henry Qin
 */
public class TuringMachine extends Automaton {
	/**
	 *
	 */
	private static final long serialVersionUID = -297306935840189114L;

	private static final Logger logger = LoggerFactory.getLogger(TuringMachine.class);

	public static void become(final TuringMachine dest, final TuringMachine src) {
		logger.debug("Calling the real become");
		System.out.println("Calling the real become");

		dest.clear();
		// Copy over the states.
		final HashMap<TMState, TMState> map = new HashMap<>(); // Old
		// states
		// to
		// new
		// states.
		// Iterator it = src.states.iterator();
		for (final Object o : src.states) {
			logger.debug("become method, processing {}", o.getClass().getName());
			final TMState state = (TMState) o;
			final TMState nstate = new TMState(state.getID(), new Point(state.getPoint()), dest); // this
			// time
			// we're
			// not
			// using
			// copy
			// constructor
			// copyStaticRelevantDataForBlocks(nstate, state, dest, src);
			nstate.setLabel(state.getLabel());
			nstate.setName(state.getName());
			map.put(state, nstate);
			dest.addState(nstate);
		}
		// Set special states.
		for (final Object o : src.finalStates) {
			final TMState tms = (TMState) o;
			dest.addFinalState(map.get(tms));
		}
		dest.setInitialState(map.get(src.getInitialState()));

		// Copy over the transitions.
		for (final Object o : src.states) {
			final TMState tms = (TMState) o;
			final List<Transition> ts = src.getTransitionsFromState(tms);
			final TMState from = map.get(tms);
			for (int i = 0; i < ts.size(); i++) {
				final TMState to = map.get(ts.get(i).getToState());
				final Transition toBeAdded = ts.get(i).clone();
				toBeAdded.setFromState(from);
				toBeAdded.setToState(to);

				// dest.addTransition(ts[i].copy(from, to));
				dest.addTransition(toBeAdded);
			}
		}
		for (int k = 0; k < src.getNotes().size(); k++) {
			final Note curNote = src.getNotes().get(k);
			dest.addNote(new Note(curNote.getAutoPoint(), curNote.getText()));
			dest.getNotes().get(k).initializeForView(curNote.getView());
		}
		dest.setEnvironmentFrame(src.getEnvironmentFrame());
		// EDebug.print("finished");
	}

	/**
	 * The number of tapes. It's public for some hacky reasons related to
	 * serialization.
	 */
	public int tapes;

	public boolean isOuterMost;

	// MERLIN MERLIN MERLIN MERLIN MERLIN//
	private TMState parent = null; // not going to force it with compiler, just
	// make sure you set it WHERE it MATTERS

	/**
	 * Creates a 1-tape Turing machine with no states and no transitions.
	 */
	public TuringMachine() {
		this(1);
	}

	/**
	 * Creates a Turing machine with a variable number of tapes, no states, and
	 * no transitions.
	 *
	 * @param tapes
	 *            the number of tapes for the Turing machine
	 */
	public TuringMachine(final int tapes) {
		super();
		this.tapes = tapes;
	}

	/**
	 * Adds a transition to this Turing machine.
	 *
	 * @param t
	 *            the transition to add
	 * @throws IllegalArgumentException
	 *             if this transition requires a different number of tapes than
	 *             required by other Turing machines
	 */
	@Override
	public void addTransition(final Transition t) {
		try {
			final int ttapes = ((TMTransition) t).tapes();
			if (tapes == 0) {
				tapes = ttapes;
			}
			if (ttapes != tapes) {
				throw new IllegalArgumentException("Transition has " + ttapes + " tapes while TM has " + tapes);
			}
			super.addTransition(t);
		} catch (final ClassCastException e) {

		}
	}

	/**
	 * We need to implement our own clone() method, rather than use that of
	 * Automaton, because we use TMStates instead of ordinary states, and we
	 * handle the building block cloning in a more elegant way.
	 */
	@Override
	public TuringMachine clone() {
		// MERLIN MERLIN MERLIN MERLIN MERLIN//

		final TuringMachine a = new TuringMachine(tapes());
		a.setEnvironmentFrame(getEnvironmentFrame());

		final HashMap<TMState, TMState> map = new HashMap<>(); // Old
		// states
		// to
		// new
		// states.
		for (final Object o : states) {

			// System.out.println(o.getClass().getName());

			final TMState tms = (TMState) o;
			final TMState ntms = new TMState(tms); // I could write a clone for
													// that
			// TM too, I suppose, but there's
			// nothing wrong with a nice C++
			// style copy constructor
			ntms.setAutomaton(a); // recognize thine new master, after the
			// convenience of the copy constructor, lest
			// there be great many bugs.

			ntms.setLabel(tms.getLabel());
			ntms.setName(tms.getName());

			map.put(tms, ntms);

			a.addState(ntms);

			// using OBJECT equality, and OBJECT hashcode, which is fine here
			// because we want to know if the objects are literally the same
			// (which they should be)
		}
		for (final Object o : finalStates) {
			final TMState tms = (TMState) o;
			a.addFinalState(map.get(tms));
		}
		a.setInitialState(map.get(getInitialState()));

		for (final Object o : states) {
			final TMState tms = (TMState) o;
			final List<Transition> ts = getTransitionsFromState(tms);
			final TMState from = map.get(tms);
			for (int i = 0; i < ts.size(); i++) {
				final TMState to = map.get(ts.get(i).getToState());
				final Transition toBeAdded = ts.get(i).clone();
				toBeAdded.setFromState(from);
				toBeAdded.setToState(to);
				// a.addTransition(ts[i].copy(from, to));
				a.addTransition(toBeAdded);
			}
		}

		return a;
	}

	/**
	 * Creates a state, inserts it in this automaton, and returns that state.
	 * The ID for the state is set appropriately.
	 *
	 * @param point
	 *            the point to put the state at
	 */
	public final TMState createBlock(final Point point) {
		int i = 0;
		while (getStateWithID(i) != null) {
			i++;
		}
		final OpenAction read = new OpenAction();
		OpenAction.setOpenOrRead(true);
		final JButton button = new JButton(read);
		button.doClick();
		OpenAction.setOpenOrRead(false);
		return getAutomatonFromFile(i, point);
	}

	public TMState createInnerTM(final Point point, final Serializable auto, final String name, final int i) {
		final TMState ntms = new TMState(i, point, this);
		final TuringMachine innerTM = (TuringMachine) auto;
		addState(ntms);
		ntms.setInnerTM(innerTM);
		ntms.setInternalName(name);
		return ntms;
	}

	/**
	 * Creates a TMState, inserts it in this automaton, and returns that state.
	 * The ID for the state is set appropriately. This method was once Final in
	 * Automaton, but it must be overriden here, because TMStates are not like
	 * standard states.
	 *
	 * @param point
	 *            the point to put the state at
	 */
	@Override
	public State createState(final Point point) {
		return createTMState(point);
	}

	/**
	 * For the sake of separation, some methods must unfortunately be
	 * duplicated.
	 *
	 *
	 */
	public final TMState createTMState(final Point point) {
		int i = 0;
		while (getStateWithID(i) != null) {
			i++;
		}
		final TMState state = new TMState(i, point, this);
		addState(state);
		return state;
	}

	/**
	 * For the sake of separation, some methods must unfortunately be
	 * duplicated.
	 *
	 *
	 */
	public final TMState createTMStateWithID(final Point p, final int i) {
		final TMState state = new TMState(i, p, this);
		addState(state);
		return state;
	}

	/**
	 * Reads the automaton in from a file.
	 */
	private TMState getAutomatonFromFile(final int i, final Point point) {
		final TMState block = new TMState(i, point, this);
		final Serializable serial = OpenAction.getLastObjectOpened();
		final File lastFile = OpenAction.getLastFileOpened();
		if (lastFile == null || OpenAction.isOpened() == false) {
			return null;
		}

		// block = putBlockContentsInAutomaton(block, serial,
		// lastFile.getName(),
		// this);
		checkArgument(serial instanceof TuringMachine);
		final TuringMachine tm = (TuringMachine) serial;

		tm.setEnvironmentFrame(getEnvironmentFrame());
		block.setInternalName(lastFile.getName());

		// MERLIN MERLIN MERLIN MERLIN MERLIN//

		block.setInnerTM(tm);

		block.setName(lastFile.getName().substring(0, lastFile.getName().length() - 4));
		addState(block);
		return block;
	}

	public Map<String, TuringMachine> getBlockMap() {
		final Map<String, TuringMachine> ret = new HashMap<>();
		// that's right, EVERY state in TM has an inner Auto, even if that inner
		// auto might be empty.
		for (final State s : states) {
			final TMState state = (TMState) s;
			ret.put(state.getInternalName(), state.getInnerTM());
		}

		return ret;
	}

	public TMState getParent() {
		return parent;
	}

	/**
	 * Returns the class of <CODE>Transition</CODE> this automaton must accept.
	 *
	 * @return the <CODE>Class</CODE> object for
	 *         <CODE>automata.tm.TMTransition</CODE>
	 */
	@Override
	protected Class<? extends Transition> getTransitionClass() {
		return edu.duke.cs.jflap.automata.turing.TMTransition.class;
	}

	public void setParent(final TMState tms) {
		parent = tms;
	}

	/**
	 * Returns the number of tapes this Turing machine uses.
	 *
	 * @return the number of tapes this Turing machine uses
	 */
	public int tapes() {
		return tapes;
	}
}
