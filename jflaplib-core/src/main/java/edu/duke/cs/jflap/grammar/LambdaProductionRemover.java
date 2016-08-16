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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.grammar.cfg.ContextFreeGrammar;

/**
 * The lambda production remover object can be used to convert a grammar to an
 * equivalent grammar with no lambda productions. This operation consists of 3
 * steps: 1. Add all variables that have lamba productions (e.g. A->lambda) to
 * lambdaSet. 2. Add all variables that have productions that are reducable to
 * lambda (if B->A1A2...Am and Ai are all in lambdaSet, then put B in lambdaSet)
 * to lambdaSet. 3. Construct a new grammar with productions such that for each
 * production in the original grammar A->x1x2x3...xm, put all productions formed
 * when xj is replaced by lambda for all xj in lambdaSet. So, if there are more
 * than 1 variable in a given production from the original grammar in the
 * lambdaSet, you must account for all permutations (i.e. all combinations of
 * which variables are replaced by lambda and which aren't). Each of these three
 * steps can be performed immediately or step by step. You can perform step 1
 * immediately by calling {@link #addVariablesWithLambdaProductions} after
 * creating an empty lambdaSet by calling {@link #getNewLambdaSet}. Or you can
 * add the variables to lambdaSet step by step while
 * {@link #areMoreVariablesWithLambdaProductions} is true, calling
 * {@link #getNewVariableWithLambdaProduction} and adding it to lambdaSet. If
 * the user tries to add a variable to lambdaSet, you can check if it has a
 * lambda production by calling {@link #isVariableWithLambdaProduction} before
 * adding it to the lambdaSet. Step 2 can also be completed immediately by
 * calling {@link #getCompleteLambdaSet}. Or you can do it step by step by
 * continually calling getNewVariableThatBelongsInLambdaSet while
 * areMoreVariablesThatBelongInLambdaSet returns true. Or if the user tries to
 * add a variable to lambdaSet, you can see if it belongs by calling
 * belongsInLambdaSet. Once the lambdaSet has been completely filled with all
 * variables that have lambda productions and all variables that have
 * productions that can reduce to lambda, we move to step 3--constructing the
 * new grammar. You can perform this step immediately by calling
 * getLamdaProductionlessGrammar, or you can build it step by step by calling
 * getProductionsToAddForProduction for each production in the original grammar
 * and adding all returned productions to the new grammar.
 *
 * @author Ryan Cavalcante
 */
public class LambdaProductionRemover {
	/** the string for zero. */
	protected static final String ZERO = "0";

	/** the char '1' . */
	protected static final char ONE_CHAR = '1';

	/**
	 * Adds all variables in <CODE>grammar</CODE> that have lambda transitions
	 * to <CODE>lambdaSet</CODE>.
	 *
	 * @param grammar
	 *            the grammar
	 * @param lambdaSet
	 *            the set of variables that have lambda transitions (upon
	 *            returning from this method it will contain all variables in
	 *            grammar that have lambda transitions).
	 */
	public static void addVariablesWithLambdaProductions(final Grammar grammar, final Set<String> lambdaSet) {
		while (areMoreVariablesWithLambdaProductions(grammar, lambdaSet)) {
			final String variable = getNewVariableWithLambdaProduction(grammar, lambdaSet);
			addVariableToLambdaSet(variable, lambdaSet);
		}
	}

	/**
	 * Adds <CODE>variable</CODE> to <CODE>lambdaSet</CODE>.
	 *
	 * @param variable
	 *            the string to add to set
	 * @param lambdaSet
	 *            the set to add to
	 */
	public static void addVariableToLambdaSet(final String variable, final Set<String> lambdaSet) {
		if (!lambdaSet.contains(variable)) {
			lambdaSet.add(variable);
		}
	}

	/**
	 * Returns true if there are more variables in <CODE>grammar</CODE> that
	 * belong in the lambda set. This includes variables that have lambda
	 * productions and variables that have productions that are reducable to
	 * lambda.
	 *
	 * @param grammar
	 *            the grammar
	 * @param lambdaSet
	 *            the set of all variables that either have lambda productions
	 *            or productions that are reducable to lambda.
	 * @return true if there are more variables in <CODE>grammar</CODE> that
	 *         belong in the lambda set. This includes variables that have
	 *         lambda productions and variables that have productions that are
	 *         reducable to lambda.
	 */
	public static boolean areMoreVariablesToAddToLambdaSet(final Grammar grammar, final Set<String> lambdaSet) {
		if (getNewVariableThatBelongsInLambdaSet(grammar, lambdaSet) == null) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if there are more variables in <CODE>grammar</CODE> that are
	 * not already in <CODE>lambdaSet</CODE> but belong there because they have
	 * lambda productions (e.g. A->lambda)
	 *
	 * @param grammar
	 *            the grammar
	 * @param lambdaSet
	 *            the set of variables in grammar that are on the left hand side
	 *            of productions that go to lambda.
	 * @return true if there are more variables in <CODE>grammar</CODE> that are
	 *         not already in <CODE>lambdaSet</CODE> but belong there because
	 *         they have lambda productions (e.g. A->lambda)
	 */
	public static boolean areMoreVariablesWithLambdaProductions(final Grammar grammar, final Set<String> lambdaSet) {
		if (getNewVariableWithLambdaProduction(grammar, lambdaSet) == null) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if either <CODE>variable</CODE> in <CODE>grammar</CODE> has
	 * a lambda production or a production that is reducable to lambda.
	 *
	 * @param variable
	 *            the variable
	 * @param grammar
	 *            the grammar
	 * @param lambdaSet
	 *            the set of all variables that either have lambda productions
	 *            or productions that are reducable to lambda.
	 * @return true if either <CODE>variable</CODE> in <CODE>grammar</CODE> has
	 *         a lambda production or a production that is reducable to lambda.
	 */
	public static boolean belongsInLambdaSet(final String variable, final Grammar grammar,
			final Set<String> lambdaSet) {
		if (isVariableWithLambdaProduction(variable, grammar)) {
			return true;
		}
		final List<Production> productions = GrammarChecker.getProductionsOnVariable(variable, grammar);
		for (int k = 0; k < productions.size(); k++) {
			if (isReducableToLambdaProduction(productions.get(k), lambdaSet)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the set of all variables in <CODE>grammar</CODE> that either have
	 * lambda productions or productions that are reducable to lambda.
	 *
	 * @param grammar
	 *            the grammar
	 * @return the set of all variables in <CODE>grammar</CODE> that either have
	 *         lambda productions or productions that are reducable to lambda.
	 */
	public static HashSet<String> getCompleteLambdaSet(final Grammar grammar) {
		final HashSet<String> lambdaSet = getNewLambdaSet();
		while (areMoreVariablesToAddToLambdaSet(grammar, lambdaSet)) {
			final String variable = getNewVariableThatBelongsInLambdaSet(grammar, lambdaSet);
			addVariableToLambdaSet(variable, lambdaSet);
		}
		return lambdaSet;
	}

	public static Grammar getLambdaProductionlessGrammar(final Grammar grammar) {
		final Grammar g = new ContextFreeGrammar();
		g.addProductions(getProductionsToAddToGrammar(grammar, getCompleteLambdaSet(grammar)));
		return g;
	}

	/**
	 * Returns a grammar equivalent to <CODE>grammar</CODE> that has no lambda
	 * productions.
	 *
	 * @param grammar
	 *            the grammar
	 * @param lambdaSet
	 *            the set of all variables that either have lambda productions
	 *            or productions that are reducable to lambda.
	 */
	public static Grammar getLambdaProductionlessGrammar(final Grammar grammar, final Set<String> lambdaSet) {
		final Grammar g = new ContextFreeGrammar();
		g.addProductions(getProductionsToAddToGrammar(grammar, lambdaSet));
		return g;
	}

	/**
	 * Returns an empty hash set
	 */
	public static HashSet<String> getNewLambdaSet() {
		return new HashSet<>();
	}

	/**
	 * Returns a variable that is not already in <CODE>lambdaSet</CODE> but
	 * belongs there (i.e. a variable that has either a lambda production or a
	 * production that is reducable to lambda).
	 *
	 * @param grammar
	 *            the grammar.
	 * @param lambdaSet
	 *            the set of all variables that either have lambda productions
	 *            or productions that are reducable to lambda.
	 * @return a variable that is not already in <CODE>lambdaSet</CODE> but
	 *         belongs there (i.e. a variable that has either a lambda
	 *         production or a production that is reducable to lambda).
	 */
	public static String getNewVariableThatBelongsInLambdaSet(final Grammar grammar, final Set<String> lambdaSet) {
		final List<String> variables = grammar.getVariables();
		for (int k = 0; k < variables.size(); k++) {
			if (!isInLambdaSet(variables.get(k), lambdaSet)
					&& belongsInLambdaSet(variables.get(k), grammar, lambdaSet)) {
				return variables.get(k);
			}
		}
		return null;
	}

	/**
	 * Returns a new variable (i.e. one that is not already in
	 * <CODE>lambdaSet</CODE> from <CODE>grammar</CODE> that has a lambda
	 * production (i.e. a production with the variable on the left hand side and
	 * lambda on the right hand side.)
	 *
	 * @param grammar
	 *            the grammar
	 * @param lambdaSet
	 *            the set of variables that have lambda productions
	 * @return a new variable (i.e. one that is not already in
	 *         <CODE>lambdaSet</CODE> from <CODE>grammar</CODE> that has a
	 *         lambda production (i.e. a production with the variable on the
	 *         left hand side and lambda on the right hand side.)
	 */
	public static String getNewVariableWithLambdaProduction(final Grammar grammar, final Set<String> lambdaSet) {
		final List<String> variables = grammar.getVariables();
		for (int k = 0; k < variables.size(); k++) {
			if (!lambdaSet.contains(variables.get(k)) && isVariableWithLambdaProduction(variables.get(k), grammar)) {
				return variables.get(k);
			}
		}
		return null;
	}

	/**
	 * Returns all non lambda productions in <CODE>grammar</CODE>.
	 *
	 * @param grammar
	 *            the grammar
	 * @return all non lambda productions in <CODE>grammar</CODE>.
	 */
	public static List<Production> getNonLambdaProductions(final Grammar grammar) {
		final List<Production> list = new ArrayList<>();
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (!ProductionChecker.isLambdaProduction(productions.get(k))) {
				list.add(productions.get(k));
			}
		}
		return list;
	}

	/**
	 * Returns a list of productions to add to a new grammar to replace
	 * <CODE>production</CODE>. The returned list of productions are all
	 * permutations of <CODE>production</CODE>. Each variable on the right hand
	 * side of <CODE>production</CODE> that is in <CODE>lambdaSet</CODE> can be
	 * replaced by lambda. So, in order to remove the lambda productions, we
	 * need to account for all permutations of production where different
	 * variables go to lambda. (e.g. if the production is S->AB and both A and B
	 * are in lambdaSet, then this would return S->AB, S->A, and S->B).
	 *
	 * @param production
	 *            the production to replace
	 * @param lambdaSet
	 *            the set of all variables that either have lambda productions
	 *            or productions that are reducable to lambda.
	 * @return a list of productions to add to a new grammar to replace
	 *         <CODE>production</CODE>. The returned list of productions are all
	 *         permutations of <CODE>production</CODE>.
	 */
	public static List<Production> getProductionsToAddForProduction(final Production production,
			final Set<String> lambdaSet) {
		// Stupid...
		/*
		 * ProductionChecker pc = new ProductionChecker(); String[] variables =
		 * production.getVariablesOnRHS(); ArrayList list = new ArrayList();
		 * for(int k = 0; k < variables.size(); k++) {
		 * if(isInLambdaSet(variables[k],lambdaSet)) list.add(variables[k]); }
		 * String[] lambdaVar = (String[]) list combs =
		 * getCombinations(lambdaVar); ArrayList productions = new ArrayList();
		 * for(int k = 0; k < combs.size(); k++) { Production p =
		 * getProductionForCombination(production,combs[k]);
		 * if(!pc.isLambdaProduction(p)) productions.add(p); } return
		 * (Production[]) productions
		 */

		List<String> start = Lists.newArrayList("");
		final String rhs = production.getRHS();
		for (int i = 0; i < rhs.length(); i++) {
			final String v = rhs.substring(i, i + 1);
			if (lambdaSet.contains(v)) {
				final List<String> s = new ArrayList<>(Collections.nCopies(2 * start.size(), null));
				for (int j = 0; j < start.size(); j++) {
					s.add(start.get(j) + v);
					s.set(j + start.size(), start.get(j));
				}
				start = s;
			} else {
				for (int j = 0; j < start.size(); j++) {
					start.set(j, start.get(j) + v);
				}
			}
		}
		start.sort((x, y) -> x.compareTo(y));
		final ArrayList<Production> list = new ArrayList<>();
		final String lhs = production.getLHS();
		for (int i = (start.get(0).length() == 0) ? 1 : 0; i < start.size(); i++) {
			list.add(new Production(lhs, start.get(i)));
		}
		return list;
	}

	/**
	 * Returns all productions created by replacing each production in
	 * <CODE>grammar</CODE> based on the variables in <CODE>lambdaSet</CODE>.
	 *
	 * @param grammar
	 *            the grammar
	 * @param lambdaSet
	 *            the set of all variables that either have lambda productions
	 *            or productions that are reducable to lambda.
	 * @return all productions created by replacing each production in
	 *         <CODE>grammar</CODE> based on the variables in
	 *         <CODE>lambdaSet</CODE>.
	 */
	public static List<Production> getProductionsToAddToGrammar(final Grammar grammar, final Set<String> lambdaSet) {
		final List<Production> list = new ArrayList<>();
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			final List<Production> prods = getProductionsToAddForProduction(productions.get(k), lambdaSet);
			for (int j = 0; j < prods.size(); j++) {
				list.add(prods.get(j));
			}
		}
		return list;
	}

	/**
	 * Returns true if <CODE>variable</CODE> is in <CODE>lambdaSet</CODE>.
	 *
	 * @param variable
	 *            the variable
	 * @param lambdaSet
	 *            the set
	 * @return true if <CODE>variable</CODE> is in <CODE>lambdaSet</CODE>.
	 */
	public static boolean isInLambdaSet(final String variable, final Set<String> lambdaSet) {
		return lambdaSet.contains(variable);
	}

	/**
	 * Returns true if <CODE>production</CODE> can be reduced to a lambda
	 * production (i.e. it is a production that goes either directly to lambda,
	 * or to a series of variables that all have lambda productions, or that
	 * themselves could be reducable to lambda productions).
	 *
	 * @param production
	 *            the production
	 * @param lambdaSet
	 *            the set of all variables that have lambda productions and
	 *            variables that have productions that are reducable to lambda
	 *            productions.
	 * @return true if <CODE>production</CODE> can be reduced to a lambda
	 *         production (i.e. it is a production that goes either directly to
	 *         lambda, or to a series of variables that all have lambda
	 *         productions, or that themselves could be reducable to lambda
	 *         productions).
	 */
	public static boolean isReducableToLambdaProduction(final Production production, final Set<String> lambdaSet) {
		if (ProductionChecker.areTerminalsOnRHS(production)) {
			return false;
		}
		final List<String> variables = production.getVariablesOnRHS();
		for (int j = 0; j < variables.size(); j++) {
			if (!isInLambdaSet(variables.get(j), lambdaSet)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if there is a production in <CODE>grammar</CODE> with
	 * <CODE>variable</CODE> on the left hand side and lambda on the right hand
	 * side.
	 *
	 * @param variable
	 *            the variable
	 * @param grammar
	 *            the grammar.
	 * @return true if there is a production in <CODE>grammar</CODE> with
	 *         <CODE>variable</CODE> on the left hand side and lambda on the
	 *         right hand side.
	 */
	public static boolean isVariableWithLambdaProduction(final String variable, final Grammar grammar) {
		final List<Production> productions = GrammarChecker.getProductionsOnVariable(variable, grammar);
		for (int k = 0; k < productions.size(); k++) {
			if (ProductionChecker.isLambdaProduction(productions.get(k))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates instance of <CODE>LambdaProductionRemover</CODE>.
	 */
	private LambdaProductionRemover() {
	}
}
