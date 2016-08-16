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

package edu.duke.cs.jflap.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;

/**
 * The grammar to automaton converter can be used to convert a grammar to an
 * equivalent automaton. This is an abstract class that is extended by
 * converters for specific types of grammars (e.g. regular) To utilize this
 * converter, you must determine what type of grammar you are trying to convert
 * by using a GrammarChecker object and then instantiate the appropriate
 * converter (e.g. RegularGrammarToFSAConverter). Then you must instantiate an
 * automaton (of the type that the converter will create), and then call
 * convertToAutomaton. Or you can do it yourself by calling the
 * createStatesForConversion method. Then you can call the
 * getTransitionsForProduction method for each production in the grammar and add
 * them to your automaton.
 *
 * @author Ryan Cavalcante
 */
public abstract class GrammarToAutomatonConverter {
	protected HashMap<String, State> MAP;

	protected String BOTTOM_OF_STACK = "Z";

	/**
	 * Instantiates a <CODE>GrammarToAutomatonConverter</CODE>.
	 */
	public GrammarToAutomatonConverter() {
	}

	/**
	 * Returns an automaton that is equivalent to <CODE>grammar</CODE> in that
	 * they will accept the same language.
	 *
	 * @param grammar
	 *            the grammar
	 * @return an automaton that is equivalent to <CODE>grammar</CODE> in that
	 *         they will accept the same language.
	 */
	public Automaton convertToAutomaton(final Grammar grammar) {
		final List<Transition> list = new ArrayList<>();
		final Automaton automaton = new Automaton();
		createStatesForConversion(grammar, automaton);
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			list.add(getTransitionForProduction(productions.get(k)));
		}

		final Iterator<Transition> it = list.iterator();
		while (it.hasNext()) {
			final Transition transition = it.next();
			automaton.addTransition(transition);
		}
		return automaton;
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
	public abstract void createStatesForConversion(Grammar grammar, Automaton automaton);

	/**
	 * Returns the State object mapped to <CODE>variable</CODE>
	 *
	 * @param variable
	 *            the variable
	 * @return the State object mapped to <CODE>variable</CODE>
	 */
	public State getStateForVariable(final String variable) {
		return MAP.get(variable);
	}

	/**
	 * Returns the transition created by converting <CODE>production</CODE> to
	 * its equivalent transition.
	 *
	 * @param production
	 *            the production
	 * @return the equivalent transition.
	 */
	public abstract Transition getTransitionForProduction(Production production);

	/**
	 * Initializes the converter for a new conversion by clearing its map.
	 */
	public void initialize() {
		MAP = new HashMap<>();
	}

	/**
	 * Maps <CODE>state</CODE> to <CODE>variable</CODE>
	 *
	 * @param state
	 *            the state
	 * @param variable
	 *            the variable
	 */
	public void mapStateToVariable(final State state, final String variable) {
		MAP.put(variable, state);
	}
}
