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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The useless states detector object can be used to find all states in an
 * automaton that either are not reachable from the initial state, or that
 * cannot reach a final state. This does only naive checking for the existence
 * of edges. It cannot be used, for example, to solve the halting problem for a
 * Turing machine.
 *
 * @author Thomas Finley
 */
public class UselessStatesDetector {
	/**
	 * Returns a copy of an automaton that has all useless states removed.
	 *
	 * @param a
	 *            the automaton
	 * @return a copy of the automaton with useless states removed
	 */
	public static Automaton cleanAutomaton(final Automaton a) {
		final Automaton ac = a.clone();
		final List<State> s = ac.getStates();
		final Set<State> useless = getUselessStates(ac);
		for (int i = 0; i < s.size(); i++) {
			if (useless.contains(s.get(i)) && s.get(i) != ac.getInitialState()) {
				ac.removeState(s.get(i));
			}
		}
		if (useless.contains(ac.getInitialState())) {
			final List<Transition> t = ac.getTransitions();
			for (int i = 0; i < t.size(); i++) {
				ac.removeTransition(t.get(i));
			}
		}
		return ac;
	}

	/**
	 * Find all states that can lead to a final state.
	 *
	 * @param a
	 *            the automaton
	 * @return the set of state that can lead to a final state
	 */
	private static Set<State> findFinal(final Automaton a) {
		final Set<State> finalized = new HashSet<>();
		finalized.addAll(a.getFinalStates());
		boolean added = finalized.size() != 0;
		final List<Transition> t = a.getTransitions();
		while (added) {
			added = false;
			for (int i = 0; i < t.size(); i++) {
				if (finalized.contains(t.get(i).getToState())) {
					added = added || finalized.add(t.get(i).getFromState());
				}
			}
		}
		return finalized;
	}

	/**
	 * Find all states reachable from an initial state.
	 *
	 * @param a
	 *            the automaton
	 * @return the set of states reachable from an initial state
	 */
	private static Set<State> findInitial(final Automaton a) {
		final Set<State> initialized = new HashSet<>();
		initialized.add(a.getInitialState());
		boolean added = true;

		while (added) {
			added = false;
			for (final Transition transition : a.getTransitions()) {
				if (initialized.contains(transition.getFromState())) {
					added = added || initialized.add(transition.getToState());
				}
			}
		}
		return initialized;
	}

	/**
	 * Returns all states in automaton that are useless.
	 *
	 * @param a
	 *            the automaton to find useless states
	 * @return a set containing all states in the automaton that are unreachable
	 *         from the initial state or cannot lead to a final state
	 * @throws IllegalArgumentException
	 *             if the automata does not have an initial state
	 */
	public static Set<State> getUselessStates(final Automaton a) {
		if (a.getInitialState() == null) {
			throw new IllegalArgumentException("Automata does not have an initial state!");
		}
		final Set<State> finalized = findFinal(a);
		final Set<State> initialized = findInitial(a);
		final Set<State> useless = new HashSet<>(a.getStates());
		finalized.retainAll(initialized);
		useless.removeAll(finalized);
		return useless;
	}

	/**
	 * One can't create an instance of this.
	 */
	private UselessStatesDetector() {
	}
}
