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

package edu.duke.cs.jflap.grammar.cfg;

import java.util.List;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.StatePlacer;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.pda.PDATransition;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.GrammarToAutomatonConverter;
import edu.duke.cs.jflap.grammar.Production;

/**
 * The CFG to PDA (LR parsing) converter can be used to convert a context free
 * grammar to an equivalent pushdown automaton that can be used for LR parsing.
 * You can perform this conversion simply by calling convertToAutomaton, or you
 * can do it step by step by first calling createStatesForConversion, which will
 * add the necessary states for the conversion to your pushdown automaton, and
 * then calling getTransitionForProduction for each production in the grammar.
 * Of course you must then add the returned transition to your pushdown
 * automaton for each call to getTransitionForProduction. When you have done
 * this for every production in your grammar, you will have an equivalent
 * pushdown automaton.
 *
 * @author Ryan Cavalcante
 */
public class CFGToPDALRConverter extends GrammarToAutomatonConverter {
	/** The start state. */
	protected State START_STATE;

	/**
	 * Creates an instance of <CODE>CFGToPDALRConverter</CODE>.
	 */
	public CFGToPDALRConverter() {
	}

	/**
	 * Adds all states to <CODE>automaton</CODE> necessary for the conversion of
	 * <CODE>grammar</CODE> to its equivalent automaton. This creates three
	 * states--an initial state, an intermediate state, and a final state. It
	 * also adds transitions connecting the three states, and transitions for
	 * each terminal in <CODE>grammar</CODE>
	 *
	 * @param grammar
	 *            the grammar being converted.
	 * @param automaton
	 *            the automaton being created.
	 */
	@Override
	public void createStatesForConversion(final Grammar grammar, final Automaton automaton) {
		initialize();
		final StatePlacer sp = new StatePlacer();

		final State initialState = automaton.createState(sp.getPointForState(automaton));
		automaton.setInitialState(initialState);
		START_STATE = initialState;

		final State intermediateState = automaton.createState(sp.getPointForState(automaton));

		final State finalState = automaton.createState(sp.getPointForState(automaton));
		automaton.addFinalState(finalState);

		final String startVariable = grammar.getStartVariable();
		final PDATransition trans1 = new PDATransition(initialState, intermediateState, "", startVariable, "");
		automaton.addTransition(trans1);
		final PDATransition trans2 = new PDATransition(intermediateState, finalState, "", BOTTOM_OF_STACK, "");
		automaton.addTransition(trans2);

		final List<String> terminals = grammar.getTerminals();
		for (int k = 0; k < terminals.size(); k++) {
			final PDATransition trans = new PDATransition(initialState, initialState, terminals.get(k), "",
					terminals.get(k));
			automaton.addTransition(trans);
		}
	}

	/**
	 * Returns the reverse of <CODE>string</CODE> e.g. it would return "cba" for
	 * "abc".
	 *
	 * @param string
	 *            the string
	 * @return the reverse of <CODE>string</CODE>
	 */
	private String getReverse(final String string) {
		final StringBuffer buffer = new StringBuffer();
		for (int k = string.length() - 1; k >= 0; k--) {
			buffer.append(string.charAt(k));
		}
		return buffer.toString();
	}

	/**
	 * Returns the transition created by converting <CODE>production</CODE> to
	 * its equivalent transition.
	 *
	 * @param production
	 *            the production
	 * @return the equivalent transition.
	 */
	@Override
	public Transition getTransitionForProduction(final Production production) {
		final String lhs = production.getLHS();
		final String rhs = production.getRHS();
		final String rrhs = getReverse(rhs);
		final Transition transition = new PDATransition(START_STATE, START_STATE, "", rrhs, lhs);
		return transition;
	}
}
