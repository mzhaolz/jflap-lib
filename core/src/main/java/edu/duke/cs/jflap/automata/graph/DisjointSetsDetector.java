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

package edu.duke.cs.jflap.automata.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;

/**
 * The disjoint sets detector can be used to determine the disjoint sets of
 * states in a given automaton. Two sets of states are determined to be disjoint
 * if there are no transitions from one set to the other. Also, given a state,
 * the disjoint sets detector can return the set of states that are connected to
 * that state.
 *
 * @author Ryan Cavalcante
 */
public class DisjointSetsDetector {
	/** the states accounted for in the determination of disjoint sets. */
	protected ArrayList<State> STATES_IN_A_SET;

	/**
	 * Instantiates a <CODE>DisjointSetsDetector</CODE>.
	 */
	public DisjointSetsDetector() {
		STATES_IN_A_SET = new ArrayList<>();
	}

	/**
	 * Returns true if all states in <CODE>automaton</CODE> have been accounted
	 * for in the determination of disjoint sets.
	 *
	 * @param automaton
	 *            the automaton
	 * @return true if all states in <CODE>automaton</CODE> have been accounted
	 *         for in the determination of disjoint sets.
	 */
	private boolean accountedForAllStates(final Automaton automaton) {
		if (getUnaccountedForState(automaton) == null) {
			return true;
		}
		return false;
	}

	/**
	 * Adds the states in the set <CODE>states</CODE> to the set of states
	 * accounted for in the determination of the disjoint sets.
	 *
	 * @param states
	 *            the set of states to account for
	 */
	private void accountForStates(final HashSet<State> states) {
		final Iterator<State> it = states.iterator();
		while (it.hasNext()) {
			final State state = it.next();
			if (!STATES_IN_A_SET.contains(state)) {
				STATES_IN_A_SET.add(state);
			}
		}
	}

	/**
	 * Adds all contents of <CODE>toAdd</CODE> that are not in <CODE>set</CODE>
	 * to <CODE>list</CODE>.
	 *
	 * @param toAdd
	 *            a list of states
	 * @param set
	 *            a set of states
	 * @param list
	 *            a list of states
	 */
	private void addAllNotInSetToList(final ArrayList<State> toAdd, final HashSet<State> set,
			final ArrayList<State> list) {
		final Iterator<State> it = toAdd.iterator();
		while (it.hasNext()) {
			final State state = it.next();
			if (!set.contains(state)) {
				list.add(state);
			}
		}
	}

	/**
	 * Returns true if <CODE>s1</CODE> and <CODE>s2</CODE> from
	 * <CODE>automaton</CODE> are directly connected. (i.e. there is either a
	 * transition from <CODE>s1</CODE> to <CODE>s2</CODE> or a transition from
	 * <CODE>s2</CODE> to <CODE>s1</CODE>.
	 *
	 * @param s1
	 *            a state
	 * @param s2
	 *            a state
	 * @param automaton
	 *            the automaton
	 * @return true if <CODE>s1</CODE> and <CODE>s2</CODE> from
	 *         <CODE>automaton</CODE> are directly connected. (i.e. there is
	 *         either a transition from <CODE>s1</CODE> to <CODE>s2</CODE> or a
	 *         transition from <CODE>s2</CODE> to <CODE>s1</CODE>.
	 */
	private boolean areDirectlyConnected(final State s1, final State s2, final Automaton automaton) {
		if (s1 == s2) {
			return false;
		}
		if (automaton.getTransitionsFromStateToState(s1, s2).size() == 0
				&& automaton.getTransitionsFromStateToState(s2, s1).size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * Returns an array of all the disjoint sets of states in
	 * <CODE>automaton</CODE>.
	 *
	 * @param automaton
	 *            the automaton
	 * @return an array of all the disjoint sets of states in
	 *         <CODE>automaton</CODE>.
	 */
	public List<HashSet<State>> getDisjointSets(final Automaton automaton) {
		final ArrayList<HashSet<State>> list = new ArrayList<>();
		STATES_IN_A_SET = new ArrayList<>();

		while (!accountedForAllStates(automaton)) {
			final State state = getUnaccountedForState(automaton);
			final HashSet<State> set = getSetIncludingState(state, automaton);
			accountForStates(set);
			list.add(set);
		}
		return list;
	}

	/**
	 * Returns a set containing all states in <CODE>automaton</CODE>, including
	 * <CODE>state</CODE>, that are connected to <CODE>state</CODE>.
	 *
	 * @param state
	 *            the state
	 * @param automaton
	 *            the automaton
	 * @return a set containing all states in <CODE>automaton</CODE>, including
	 *         <CODE>state</CODE>, that are connected to <CODE>state</CODE>.
	 */
	public HashSet<State> getSetIncludingState(final State state, final Automaton automaton) {
		final HashSet<State> set = new HashSet<>();
		final ArrayList<State> list = new ArrayList<>();
		list.add(state);
		while (!list.isEmpty()) {
			final ArrayList<State> toAdd = new ArrayList<>();
			final Iterator<State> it = list.iterator();
			while (it.hasNext()) {
				final State s = it.next();
				toAdd.addAll(getStatesConnectedToState(s, automaton));
				set.add(s);
				it.remove();
			}
			addAllNotInSetToList(toAdd, set, list);
		}

		return set;
	}

	/**
	 * Returns a list of states in <CODE>automaton</CODE> that are connected
	 * directly to <CODE>state</CODE>.
	 *
	 * @param state
	 *            the state
	 * @param automaton
	 *            the automaton
	 * @return a list of states in <CODE>automaton</CODE> that are connected
	 *         directly to <CODE>state</CODE>.
	 */
	private ArrayList<State> getStatesConnectedToState(final State state, final Automaton automaton) {
		final ArrayList<State> list = new ArrayList<>();
		final List<State> states = automaton.getStates();
		for (final State stati : states) {
			if (areDirectlyConnected(state, stati, automaton)) {
				list.add(stati);
			}
		}
		return list;
	}

	/**
	 * Returns a state in <CODE>automaton</CODE> that has not yet been accounted
	 * for in the determination of disjoint sets.
	 *
	 * @param automaton
	 *            the automaton
	 * @return a state in <CODE>automaton</CODE> that has not yet been accounted
	 *         for in the determination of disjoint sets.
	 */
	public State getUnaccountedForState(final Automaton automaton) {
		final List<State> states = automaton.getStates();
		for (final State state : states) {
			if (!isAccountedFor(state)) {
				return state;
			}
		}
		return null;
	}

	/**
	 * Returns true if <CODE>state</CODE> has been accounted for in the
	 * determination of disjoint sets
	 *
	 * @param state
	 *            the state
	 */
	private boolean isAccountedFor(final State state) {
		if (STATES_IN_A_SET.contains(state)) {
			return true;
		}
		return false;
	}
}
