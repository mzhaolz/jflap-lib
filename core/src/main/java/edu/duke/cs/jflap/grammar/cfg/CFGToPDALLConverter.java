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
 * The CFG to PDA (LL parsing) converter can be used to convert a context free
 * grammar to a pushdown automaton that can be used for LL parsing. You can do
 * the conversion simply by calling convertToAutomaton, or you can do the
 * conversion step by step by first calling createStatesForConversion, which
 * will create all the states in the pushdown automaton necessary for the
 * conversion, and then calling getTransitionForProduction for each production
 * in the grammar. You must of course add each Transition returned by this call
 * to your pushdown automaton. When you have done this for each production in
 * your grammar, the equivalent PDA will be complete.
 *
 * @author Ryan Cavalcante
 */
public class CFGToPDALLConverter extends GrammarToAutomatonConverter {
	/** the intermediate state in the automaton. */
	protected State INTERMEDIATE_STATE;

	/**
	 * Creates an instance of <CODE>CFGToPDALLConverter</CODE>.
	 */
	public CFGToPDALLConverter() {
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

		final State intermediateState = automaton.createState(sp.getPointForState(automaton));
		INTERMEDIATE_STATE = intermediateState;

		final State finalState = automaton.createState(sp.getPointForState(automaton));
		automaton.addFinalState(finalState);

		final String startVariable = grammar.getStartVariable();
		final String temp = startVariable.concat(BOTTOM_OF_STACK);
		final PDATransition trans1 = new PDATransition(initialState, intermediateState, "", BOTTOM_OF_STACK, temp);
		automaton.addTransition(trans1);
		final PDATransition trans2 = new PDATransition(intermediateState, finalState, "", BOTTOM_OF_STACK, "");
		automaton.addTransition(trans2);

		final List<String> terminals = grammar.getTerminals();
		for (int k = 0; k < terminals.size(); k++) {
			final PDATransition trans = new PDATransition(intermediateState, intermediateState, terminals.get(k),
					terminals.get(k), "");
			automaton.addTransition(trans);
		}
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
		final Transition transition = new PDATransition(INTERMEDIATE_STATE, INTERMEDIATE_STATE, "", lhs, rhs);
		return transition;
	}
}
