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
import java.util.List;

import edu.duke.cs.jflap.automata.AlphabetRetriever;
import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.Transition;

/**
 * The FSA alphabet retriever object can be used to find the alphabet for a
 * given finite state automaton. The method of determining the alphabet for
 * automaton involves examining all transitions in the automaton and adding each
 * new character on a transition label to the alphabet.
 *
 * @author Ryan Cavalcante
 */
public class FSAAlphabetRetriever extends AlphabetRetriever {
	/**
	 * Creates an instance of <CODE>FSAAlphabetRetriever</CODE>.
	 */
	public FSAAlphabetRetriever() {
	}

	/**
	 * Returns the alphabet of <CODE>automaton</CODE> by analyzing all
	 * transitions and their labels.
	 *
	 * @param automaton
	 *            the automaton
	 * @return the alphabet, in a string[].
	 */
	@Override
	public List<String> getAlphabet(final Automaton automaton) {
		final List<String> list = new ArrayList<>();
		final List<Transition> transitions = automaton.getTransitions();
		for (int k = 0; k < transitions.size(); k++) {
			final FSATransition transition = (FSATransition) transitions.get(k);
			final String label = transition.getLabel();
			if (!label.equals("") && !list.contains(label)) {
				list.add(label);
			}
		}
		return list;
	}
}
