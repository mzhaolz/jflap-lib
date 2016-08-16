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

package edu.duke.cs.jflap.grammar.reg;

import java.awt.Point;
import java.util.List;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.StatePlacer;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.fsa.FSATransition;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.GrammarToAutomatonConverter;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.ProductionChecker;

/**
 * The right linear grammar converter can be used to convert regular grammars,
 * specifically right-linear grammars, to their equivalent finite state
 * automata. You can do the conversion all at once by calling convertToAutomaton
 * (a method inherited from super class GrammarToAutomatonConverter), or you can
 * do the conversion step by step by first calling createStatesForConversion to
 * create a state for each variable in the grammar. Then, one by one, for each
 * Production rule in the grammar, you can call getTransitionForProduction and
 * add the returned transition to the fsa that you are building. Once you do
 * this for each production in the grammar, you will have the equivalent fsa.
 *
 * @author Ryan Cavalcante
 */
public class RightLinearGrammarToFSAConverter extends GrammarToAutomatonConverter {
	protected String FINAL_STATE = "FINAL";

	/**
	 * Creates an instance of <CODE>RightLinearGrammarToFSAConverter</CODE>.
	 */
	public RightLinearGrammarToFSAConverter() {
	}

	/**
	 * Adds all states to <CODE>automaton</CODE> necessary for the conversion of
	 * <CODE>grammar</CODE> to its equivalent automaton. This creates a state
	 * for each variable in <CODE>grammar</CODE> and maps each created state to
	 * the variable it was created for by calling mapStateToVariable.
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
		final List<String> variables = grammar.getVariables();
		for (int k = 0; k < variables.size(); k++) {
			final String variable = variables.get(k);
			final Point point = sp.getPointForState(automaton);
			final State state = automaton.createState(point);
			if (variable.equals(grammar.getStartVariable())) {
				automaton.setInitialState(state);
			}
			state.setLabel(variable);
			mapStateToVariable(state, variable);
		}

		final Point pt = sp.getPointForState(automaton);
		final State finalState = automaton.createState(pt);
		automaton.addFinalState(finalState);
		mapStateToVariable(finalState, FINAL_STATE);
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
		final State from = getStateForVariable(lhs);

		/** if of the form A->xB */
		if (ProductionChecker.isRightLinearProductionWithVariable(production)) {
			final List<String> variables = production.getVariablesOnRHS();
			final String variable = variables.get(0);
			final State to = getStateForVariable(variable);
			final String rhs = production.getRHS();
			final String label = rhs.substring(0, rhs.length() - 1);
			final FSATransition trans = new FSATransition(from, to, label);
			return trans;
		}
		/** if of the form A->x */
		else if (ProductionChecker.isLinearProductionWithNoVariable(production)) {
			final String transLabel = production.getRHS();
			final State finalState = getStateForVariable(FINAL_STATE);
			final FSATransition ftrans = new FSATransition(from, finalState, transLabel);
			return ftrans;
		}
		return null;
	}
}
