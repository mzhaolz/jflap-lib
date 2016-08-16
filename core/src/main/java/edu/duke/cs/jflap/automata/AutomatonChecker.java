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

import java.util.List;

import edu.duke.cs.jflap.automata.fsa.FSANondeterminismDetector;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;

/**
 * The Automaton checker can be used to determine certain properties about
 * automata.
 *
 * @author Ryan Cavalcante
 */
public class AutomatonChecker {
	/**
	 * Creates instance of <CODE>AutomatonChecker</CODE>.
	 */
	public AutomatonChecker() {
	}

	/**
	 * Returns true if <CODE>automaton</CODE> is a non-deterministic finite
	 * state automaton.
	 *
	 * @param automaton
	 *            the automaton.
	 * @return true if <CODE>automaton</CODE> is a non-deterministic finite
	 *         state automaton.
	 */
	public boolean isNFA(final Automaton automaton) {
		if (!(automaton instanceof FiniteStateAutomaton)) {
			return false;
		}
		final NondeterminismDetector nd = new FSANondeterminismDetector();
		final List<State> nondeterministicStates = nd.getNondeterministicStates(automaton);
		return nondeterministicStates.size() > 0;
	}
}
