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
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * The Grammar checker object can be used to check certain properties of grammar
 * objects.
 *
 * @author Ryan Cavalcante
 */
public class GrammarChecker {
	/**
	 * Returns all productions in <CODE>grammar</CODE> that have
	 * <CODE>variable</CODE> as the only character on the left hand side and
	 * that are not unit productions.
	 *
	 * @param variable
	 *            the variable
	 * @param grammar
	 *            the grammar
	 * @return all productions in <CODE>grammar</CODE> that have
	 *         <CODE>variable</CODE> as the only character on the left hand side
	 *         and are not unit productions.
	 */
	public static List<Production> getNonUnitProductionsOnVariable(final String variable, final Grammar grammar) {
		final List<Production> list = new ArrayList<>();
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (variable.equals(productions.get(k).getLHS())
					&& !ProductionChecker.isUnitProduction(productions.get(k))) {
				list.add(productions.get(k));
			}
		}
		return list;
	}

	/**
	 * Returns all productions in <CODE>grammar</CODE> whose lhs is
	 * <CODE>variable</CODE>.
	 *
	 * @param variable
	 *            the variable
	 * @param grammar
	 *            the grammar
	 * @return all productions in <CODE>grammar</CODE> whose lhs is
	 *         <CODE>variable</CODE>.
	 */
	public static List<Production> getProductionsOnVariable(final String variable, final Grammar grammar) {
		final List<Production> list = new ArrayList<>();
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (variable.equals(productions.get(k).getLHS())) {
				list.add(productions.get(k));
			}
		}
		return list;
	}

	/**
	 * Returns all productions in <CODE>grammar</CODE> that have
	 * <CODE>variable</CODE> in them, either on the rhs or lhs.
	 *
	 * @param variable
	 *            the variable
	 * @param grammar
	 *            the grammar
	 * @return all productions in <CODE>grammar</CODE> that have
	 *         <CODE>variable</CODE> in them, either on the rhs or lhs.
	 */
	public static List<Production> getProductionsWithVariable(final String variable, final Grammar grammar) {
		final List<Production> list = new ArrayList<>();
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (ProductionChecker.isVariableInProduction(variable, productions.get(k))) {
				list.add(productions.get(k));
			}
		}
		return list;
	}

	/**
	 * Returns all productions in <CODE>grammar</CODE> that have
	 * <CODE>variable</CODE> on the right hand side.
	 *
	 * @param variable
	 *            the variable
	 * @param grammar
	 *            the grammar
	 * @return all productions in <CODE>grammar</CODE> that have
	 *         <CODE>variable</CODE> on the right hand side.
	 */
	public static List<Production> getProductionsWithVariableOnRHS(final String variable, final Grammar grammar) {
		final List<Production> list = new ArrayList<>();
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (ProductionChecker.isVariableOnRHS(productions.get(k), variable)) {
				list.add(productions.get(k));
			}
		}
		return list;
	}

	/**
	 * Returns a list of those variables which are unresolved, i.e., which
	 * appears in the right hand side but do not appear in the left hand side.
	 *
	 * @param grammar
	 *            the grammar to check
	 * @return an array of the unresolved variables
	 */
	public static List<String> getUnresolvedVariables(final Grammar grammar) {
		final List<String> variables = grammar.getVariables();
		final HashSet<String> variableSet = new HashSet<>();
		for (int i = 0; i < variables.size(); i++) {
			variableSet.add(variables.get(i));
		}
		final List<Production> productions = grammar.getProductions();
		for (int i = 0; i < productions.size(); i++) {
			final List<String> lhsVariables = productions.get(i).getVariablesOnLHS();
			for (int j = 0; j < lhsVariables.size(); j++) {
				variableSet.remove(lhsVariables.get(j));
			}
		}
		return Lists.newArrayList(variableSet);
	}

	/**
	 * Returns true if <CODE>grammar</CODE> is a context-free grammar (i.e. all
	 * productions are of the form A->x).
	 *
	 * @param grammar
	 *            the grammar.
	 * @return true if <CODE>grammar</CODE> is a context-free grammar.
	 */
	public static boolean isContextFreeGrammar(final Grammar grammar) {
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (!ProductionChecker.isRestrictedOnLHS(productions.get(k))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if <CODE>grammar</CODE> is a left-linear grammar (i.e. all
	 * productions are of the form A->Bx or A->x).
	 *
	 * @param grammar
	 *            the grammar.
	 * @return true if <CODE>grammar</CODE> is a left-linear grammar.
	 */
	public static boolean isLeftLinearGrammar(final Grammar grammar) {
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (!ProductionChecker.isLeftLinear(productions.get(k))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if <CODE>production</CODE>, or an identical production, is
	 * already in <CODE>grammar</CODE>.
	 *
	 * @param production
	 *            the production
	 * @param grammar
	 *            the grammar
	 * @return true if <CODE>production</CODE>, or an identical production, is
	 *         already in <CODE>grammar</CODE>.
	 */
	public static boolean isProductionInGrammar(final Production production, final Grammar grammar) {
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (production.equals(productions.get(k))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if <CODE>grammar</CODE> is a regular grammar (i.e. if it is
	 * either a right or left linear grammar).
	 *
	 * @param grammar
	 *            the grammar.
	 * @return true if <CODE>grammar</CODE> is a regular grammar.
	 */
	public static boolean isRegularGrammar(final Grammar grammar) {
		if (isRightLinearGrammar(grammar) || isLeftLinearGrammar(grammar)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if <CODE>grammar</CODE> is a right-linear grammar (i.e. all
	 * productions are of the form A->xB or A->x).
	 *
	 * @param grammar
	 *            the grammar.
	 * @return true if <CODE>grammar</CODE> is a right-linear grammar.
	 */
	public static boolean isRightLinearGrammar(final Grammar grammar) {
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (!ProductionChecker.isRightLinear(productions.get(k))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if <CODE>terminal</CODE> is in any production, either on the
	 * right or left hand side of the production, of <CODE>grammar</CODE>.
	 *
	 * @param terminal
	 *            the terminal.
	 * @return true if <CODE>terminal</CODE> is in any production in
	 *         <CODE>grammar</CODE>.
	 */
	public static boolean isTerminalInProductions(final Grammar grammar, final String terminal) {
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (ProductionChecker.isTerminalInProduction(terminal, productions.get(k))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if <CODE>variable</CODE> is in any production, either on the
	 * right or left hand side of the production, of <CODE>grammar</CODE>.
	 *
	 * @param variable
	 *            the variable.
	 * @return true if <CODE>variable</CODE> is in any production of
	 *         <CODE>grammar</CODE>.
	 */
	public static boolean isVariableInProductions(final Grammar grammar, final String variable) {
		final List<Production> productions = grammar.getProductions();
		for (int k = 0; k < productions.size(); k++) {
			if (ProductionChecker.isVariableInProduction(variable, productions.get(k))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates an instance of <CODE>GrammarChecker</CODE>.
	 */
	public GrammarChecker() {
	}
}
