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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.UnreachableStatesDetector;
import edu.duke.cs.jflap.automata.vdg.VDGTransition;
import edu.duke.cs.jflap.automata.vdg.VariableDependencyGraph;
import edu.duke.cs.jflap.grammar.cfg.ContextFreeGrammar;

/**
 * The Unit Production remover can be used to convert a grammar to an equivalent
 * grammar that doesn't have any unit productions (i.e productions of the form
 * A->B). This conversion consists of 3 steps: 1. drawing a variable dependency
 * graph of all variables that have unit productions. 2. putting all non-unit
 * productions from the original grammar into the new gramamr. 3. adding
 * productions to the new grammar to replace the unit productions in the
 * original grammar. Each step can be performed immediately or step by step. You
 * can perform step 1 immediately by calling getVariableDependencyGraph, or you
 * can build the dependency graph step by step by first calling
 * initializeDependencyGraph which will add nodes for each variable in the
 * grammar to your dependency graph. Then, you actually represent the
 * dependencies of the variables by examining only the unit productions in the
 * grammar. To get the transition (for the graph) that represents the dependency
 * of a specific unit production, call getTransitionForUnitProduction. Then you
 * can add the returned transition to your variable dependency graph. After you
 * do this for every unit production in the grammar, the dependency graph will
 * be complete. You can perform step 2 immediately by calling
 * addAllNonUnitProductionsToGrammar. Or you can do this step by step by
 * creating a ProductionChecker object to check each production in the original
 * grammar to see if it is a unit production. So, if the user selects a
 * production from the original grammar and tries to add it to the new grammar,
 * you must check (using the ProductionChecker) if the production is a unit
 * production. If not, you can add it to the new grammar by simply calling
 * addProduction on the grammar. Once you've added all the Non-unit productions
 * from the original grammar to the new grammar, you can finish the conversion
 * by performing step 3. You can perform step 3 immediately by calling
 * addAllProductionsToGrammar, or you can perform it step by step by getting the
 * dependencies for each variable in the grammar by calling getDependencies.
 * Then, for each variable that said variable is dependent on, you need to get
 * all the non-unit productions in the grammar on that variable (by using a
 * GrammarChecker and calling getNonUnitProductionsOnVariable, and then call
 * getNewProductions to get the productions necessary to replace the unit
 * productions on said variable. This will return a list of new productions on
 * said variable that accounts for the removal of the unit production to its
 * dependent variable.
 *
 * @author Ryan Cavalcante
 */
public class UnitProductionRemover {
	/**
	 * Adds all productions to <CODE>newGrammar</CODE> required to account for
	 * removing all unit productions from <CODE>oldGrammar</CODE>.
	 *
	 * @param oldGrammar
	 *            the original grammar
	 * @param newGrammar
	 *            the new grammar
	 * @param graph
	 *            the variable dependency graph of the original grammar
	 */
	public static void addAllNewProductionsToGrammar(final Grammar oldGrammar, final Grammar newGrammar,
			final VariableDependencyGraph graph) {
		final List<String> variables = oldGrammar.getVariables();
		for (int k = 0; k < variables.size(); k++) {
			final String v1 = variables.get(k);
			final List<String> dep = getDependencies(v1, oldGrammar, graph);
			for (int i = 0; i < dep.size(); i++) {
				final List<Production> prods = GrammarChecker.getNonUnitProductionsOnVariable(dep.get(i), oldGrammar);
				newGrammar.addProductions(getNewProductions(v1, prods));
			}
		}
	}

	/**
	 * Adds all non-unit productions in <CODE>oldGrammar</CODE> to
	 * <CODE>newGrammar</CODE>.
	 *
	 * @param oldGrammar
	 *            a grammar
	 * @param newGrammar
	 *            a grammar
	 */
	public static void addAllNonUnitProductionsToGrammar(final Grammar oldGrammar, final Grammar newGrammar) {
		final List<Production> productions = getNonUnitProductions(oldGrammar);
		for (int k = 0; k < productions.size(); k++) {
			newGrammar.addProduction(productions.get(k));
		}
	}

	/**
	 * Returns all variables that <CODE>variable</CODE> is dependent on (i.e.
	 * all variables whose states can be reached from the state that represents
	 * <CODE>variable</CODE> in <CODE>graph</CODE>.
	 *
	 * @param variable
	 *            the variable whose dependencies are being found
	 * @param grammar
	 *            the grammar
	 * @param graph
	 *            the dependency graph
	 * @return all variables that <CODE>variable</CODE> is dependent on (i.e.
	 *         all variables whose states can be reached from the state that
	 *         represents <CODE>variable</CODE> in <CODE>graph</CODE>.
	 */
	public static List<String> getDependencies(final String variable, final Grammar grammar,
			final VariableDependencyGraph graph) {
		final List<String> list = new ArrayList<>();
		final List<String> variables = grammar.getVariables();
		for (int k = 0; k < variables.size(); k++) {
			if (!variable.equals(variables.get(k))) {
				if (isDependentOn(variable, variables.get(k), graph)) {
					list.add(variables.get(k));
				}
			}
		}
		return list;
	}

	/**
	 * Returns a list of productions created by taking <CODE>variable</CODE> as
	 * their left hand side, and the right hand side of a production in
	 * <CODE>oldProductions</CODE> as their right hand sides.
	 *
	 * @param variable
	 *            the left hand side of the created productions
	 * @param prods
	 *            the set of productions whose right hand sides are used as the
	 *            right hand sides of the created productions
	 * @return a list of productions created by taking <CODE>variable</CODE> as
	 *         their left hand side, and the right hand side of a production in
	 *         <CODE>oldProductions</CODE> as their right hand sides.
	 */
	public static List<Production> getNewProductions(final String variable, final List<Production> prods) {
		final List<Production> list = new ArrayList<>();
		for (int k = 0; k < prods.size(); k++) {
			list.add(new Production(variable, prods.get(k).getRHS()));
		}
		return list;
	}

	/**
	 * Returns all non-unit productions in <CODE>grammar</CODE>.
	 *
	 * @param grammar
	 *            the grammar
	 * @return all non-unit productions in <CODE>grammar</CODE>.
	 */
	public static List<Production> getNonUnitProductions(final Grammar grammar) {
		final List<Production> list = new ArrayList<>();

		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (!ProductionChecker.isUnitProduction(productions.get(k))) {
				list.add(productions.get(k));
			}
		}
		return list;
	}

	/**
	 * Returns the state in <CODE>graph</CODE> that represents
	 * <CODE>variable</CODE> (i.e. the state whose label is
	 * <CODE>variable</CODE>).
	 *
	 * @param variable
	 *            the variable
	 * @param graph
	 *            the graph
	 * @return the state in <CODE>graph</CODE> that represents
	 *         <CODE>variable</CODE> (i.e. the state whose label is
	 *         <CODE>variable</CODE>).
	 */
	public static State getStateForVariable(final String variable, final VariableDependencyGraph graph) {
		final List<State> states = graph.getStates();
		for (int k = 0; k < states.size(); k++) {
			if (states.get(k).getName().equals(variable)) {
				return states.get(k);
			}
		}
		return null;
	}

	/**
	 * Returns the transition for <CODE>graph</CODE> that represents the
	 * dependency of the variables in the unit production
	 * <CODE>production</CODE>.
	 *
	 * @param production
	 *            the unit production
	 * @param graph
	 *            the graph
	 * @return the transition for <CODE>graph</CODE> that represents the
	 *         dependency of the variables in the unit production
	 *         <CODE>production</CODE>.
	 */
	public static Transition getTransitionForUnitProduction(final Production production,
			final VariableDependencyGraph graph) {
		if (!ProductionChecker.isUnitProduction(production)) {
			return null;
		}
		final String lhs = production.getLHS();
		final String rhs = production.getRHS();
		final State from = getStateForVariable(lhs, graph);
		final State to = getStateForVariable(rhs, graph);
		return new VDGTransition(from, to);
	}

	public static Grammar getUnitProductionlessGrammar(final Grammar grammar) {
		final Grammar uplgrammar = new ContextFreeGrammar();
		addAllNonUnitProductionsToGrammar(grammar, uplgrammar);
		addAllNewProductionsToGrammar(grammar, uplgrammar, getVariableDependencyGraph(grammar));
		return uplgrammar;
	}

	/**
	 * Returns a unit production-less grammar equivalent to
	 * <CODE>grammar</CODE>.
	 *
	 * @param grammar
	 *            the grammar
	 * @param graph
	 *            the variable dependency graph of <CODE>grammar</CODE>.
	 * @return a unit production-less grammar equivalent to
	 *         <CODE>grammar</CODE>.
	 */
	public static Grammar getUnitProductionlessGrammar(final Grammar grammar, final VariableDependencyGraph graph) {
		final Grammar uplgrammar = new ContextFreeGrammar();
		addAllNonUnitProductionsToGrammar(grammar, uplgrammar);
		addAllNewProductionsToGrammar(grammar, uplgrammar, graph);
		return uplgrammar;
	}

	/**
	 * Returns all unit productions in <CODE>grammar</CODE>.
	 *
	 * @param grammar
	 *            the grammar.
	 * @return all unit productions in <CODE>grammar</CODE>.
	 */
	public static List<Production> getUnitProductions(final Grammar grammar) {
		final List<Production> list = new ArrayList<>();

		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (ProductionChecker.isUnitProduction(productions.get(k))) {
				list.add(productions.get(k));
			}
		}
		return list;
	}

	/**
	 * Returns the variable dependency graph for the variables in
	 * <CODE>grammar</CODE>.
	 *
	 * @param grammar
	 *            the grammar.
	 * @return the variable dependency graph for the variables in
	 *         <CODE>grammar</CODE>.
	 */
	public static VariableDependencyGraph getVariableDependencyGraph(final Grammar grammar) {
		final VariableDependencyGraph graph = new VariableDependencyGraph();
		initializeDependencyGraph(graph, grammar);
		final List<Production> uprods = getUnitProductions(grammar);
		for (int k = 0; k < uprods.size(); k++) {
			graph.addTransition(getTransitionForUnitProduction(uprods.get(k), graph));
		}
		return graph;
	}

	/**
	 * Adds a state for each variable in <CODE>grammar</CODE> to
	 * <CODE>graph</CODE>.
	 *
	 * @param graph
	 *            the graph to add the states to.
	 * @param grammar
	 *            the grammar
	 */
	public static void initializeDependencyGraph(final VariableDependencyGraph graph, final Grammar grammar) {
		// StatePlacer sp = new StatePlacer();
		final List<String> variables = grammar.getVariables();
		for (int k = 0; k < variables.size(); k++) {
			// Point point = sp.getPointForState(graph);
			final double theta = 2.0 * Math.PI * k / variables.size();
			final Point point = new Point(200 + (int) (180.0 * Math.cos(theta)), 200 + (int) (180.0 * Math.sin(theta)));
			final State state = graph.createState(point);
			state.setName(variables.get(k));
		}
	}

	/**
	 * Returns true if <CODE>variable1</CODE> is dependent on
	 * <CODE>variable2</CODE>. (i.e. there is a path in <CODE>graph</CODE> from
	 * <CODE>variable1</CODE> to <CODE>variable2</CODE>).
	 *
	 * @param variable1
	 *            the first variable; the start of the path.
	 * @param variable2
	 *            the second variable; the destination of the path.
	 * @param graph
	 *            the variable dependency graph.
	 * @return true if <CODE>variable1</CODE> is dependent on
	 *         <CODE>variable2</CODE>. (i.e. there is a path in
	 *         <CODE>graph</CODE> from <CODE>variable1</CODE> to
	 *         <CODE>variable2</CODE>).
	 */
	public static boolean isDependentOn(final String variable1, final String variable2,
			final VariableDependencyGraph graph) {
		final State v1 = getStateForVariable(variable1, graph);
		final State v2 = getStateForVariable(variable2, graph);
		graph.setInitialState(v1);
		final UnreachableStatesDetector usd = new UnreachableStatesDetector(graph);
		final List<State> states = usd.getUnreachableStates();
		graph.setInitialState(null);
		for (int k = 0; k < states.size(); k++) {
			if (v2 == states.get(k)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates instance of <CODE>UnitProductionRemover</CODE>.
	 */
	private UnitProductionRemover() {
	}
}
