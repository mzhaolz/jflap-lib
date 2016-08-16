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

import java.util.List;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;

/**
 * The FSA label handler is an object that can convert a finite state automaton
 * with transition labels of more than one character in length into an
 * equivalent finite state automaton with all transition labels exactly one
 * character in length.
 *
 * @author Ryan Cavalcante
 */
public class FSALabelHandler {
	/**
	 * Changes <CODE>transition</CODE> in <CODE>automaton</CODE> to several
	 * transitions each with labels of one character in length. This algorithm
	 * introduces new states in <CODE>automaton</CODE>.
	 *
	 * @param transition
	 *            the transition to break up into several transitions of one
	 *            character (in length) a piece.
	 * @param automaton
	 *            the automaton that has the transition.
	 */
	public static void handleLabel(final Transition transition, final Automaton automaton) {
		/*
		 * FSATransition trans = (FSATransition) transition; String label =
		 * trans.getLabel(); String firstChar = label.substring(0,1); String
		 * restOfLabel = label.substring(1);
		 *
		 * StatePlacer sp = new StatePlacer();
		 *
		 * State newState =
		 * automaton.createState(sp.getPointForState(automaton)); Transition
		 * newTrans1 = new FSATransition(trans.getFromState(), newState,
		 * firstChar); Transition newTrans2 = new FSATransition(newState,
		 * trans.getToState(), restOfLabel); automaton.addTransition(newTrans1);
		 * automaton.addTransition(newTrans2);
		 * automaton.removeTransition(transition); if(restOfLabel.length() > 1)
		 * handleLabel(newTrans2, automaton);
		 */

		final FSATransition trans = (FSATransition) transition;
		State from = transition.getFromState();
		final State f = from, to = transition.getToState();
		automaton.removeTransition(trans);
		final String label = trans.getLabel();
		final int length = label.length();
		for (int i = 0; i < length; i++) {
			final State going = i == length - 1 ? to
					: automaton.createState(
							new java.awt.Point((f.getPoint().x * (length - i - 1) + to.getPoint().x * (i + 1)) / length,
									(f.getPoint().y * (length - i - 1) + to.getPoint().y * (i + 1)) / length));
			final Transition newTrans = new FSATransition(from, going, label.substring(i, i + 1));
			automaton.addTransition(newTrans);
			from = going;
		}
	}

	/**
	 * Returns true if <CODE>automaton</CODE> has labels with multiple
	 * characters, instead of single character labels.
	 *
	 * @param automaton
	 *            the automaton.
	 * @return true if <CODE>automaton</CODE> has labels with multiple
	 *         characters, instead of single character labels.
	 */
	public static boolean hasMultipleCharacterLabels(final Automaton automaton) {
		final List<Transition> transitions = automaton.getTransitions();
		for (int k = 0; k < transitions.size(); k++) {
			final FSATransition transition = (FSATransition) transitions.get(k);
			final String label = transition.getLabel();
			if (label.length() > 1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Changes all transitions in <CODE>automaton</CODE> into transitions with
	 * at most one character per label. This could introduce more states into
	 * <CODE>automaton</CODE>.
	 *
	 * @param automaton
	 *            the automaton.
	 */
	public static FiniteStateAutomaton removeMultipleCharacterLabels(final Automaton automaton) {
		final FiniteStateAutomaton fsa = (FiniteStateAutomaton) automaton.clone();
		final List<Transition> transitions = fsa.getTransitions();
		for (int k = 0; k < transitions.size(); k++) {
			final FSATransition transition = (FSATransition) transitions.get(k);
			final String label = transition.getLabel();
			if (label.length() > 1) {
				handleLabel(transition, fsa);
			}
		}
		return fsa;
	}

	/**
	 * Changes all transitions in <CODE>automaton</CODE> into transitions with
	 * at most one character per label. This could introduce more states into
	 * <CODE>automaton</CODE>.
	 *
	 * @param automaton
	 *            the automaton.
	 */
	public static void removeMultipleCharacterLabelsFromAutomaton(final Automaton automaton) {
		final List<Transition> transitions = automaton.getTransitions();
		for (int k = 0; k < transitions.size(); k++) {
			final FSATransition transition = (FSATransition) transitions.get(k);
			final String label = transition.getLabel();
			if (label.length() > 1) {
				handleLabel(transition, automaton);
			}
		}
	}

	public static void splitLabel(final Transition transition, final Automaton automaton) {
		final FSATransition trans = (FSATransition) transition;
		final State from = transition.getFromState(), to = transition.getToState();
		automaton.removeTransition(trans);
		final String label = trans.getLabel();
		for (int i = label.charAt(label.indexOf("[") + 1); i <= label.charAt(label.indexOf("[") + 3); i++) {
			final Transition newTrans = new FSATransition(from, to, Character.toString((char) i));
			automaton.addTransition(newTrans);
		}
	}

	/**
	 * Creates an instance of <CODE>FSALabelHandler</CODE>.
	 */
	private FSALabelHandler() {
	}
}
