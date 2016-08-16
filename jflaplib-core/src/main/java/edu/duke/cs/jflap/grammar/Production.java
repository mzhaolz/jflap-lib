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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * A Production object is a simple abstract class that represents a production
 * rule in a grammar.
 *
 * @author Ryan Cavalcante
 */
public class Production implements Serializable {
	private static final long serialVersionUID = 110L;

	/** the left hand side of the production. */
	protected String myLHS;

	/** the right hand side of the production. */
	protected String myRHS;

	/**
	 * Creates an instance of <CODE>Production</CODE>.
	 *
	 * @param lhs
	 *            the left hand side of the production rule.
	 * @param rhs
	 *            the right hand side of the production rule.
	 */
	public Production(String lhs, String rhs) {
		if (lhs == null) {
			lhs = "";
		}
		if (rhs == null) {
			rhs = "";
		}
		myLHS = lhs;
		myRHS = rhs;
	}

	/**
	 * Returns true if <CODE>production</CODE> is equivalent to this production
	 * (i.e. they have identical left and right hand sides).
	 *
	 * @param production
	 *            the production being compared to this production
	 * @return true if <CODE>production</CODE> is equivalent to this production
	 *         (i.e. they have identical left and right hand sides).
	 */
	@Override
	public boolean equals(final Object production) {
		if (production instanceof Production) {
			final Production p = (Production) production;
			return getRHS().equals(p.getRHS()) && getLHS().equals(p.getLHS());
		}
		return false;
	}

	/**
	 * Returns a string representation of the left hand side of the production
	 * rule.
	 *
	 * @return a string representation of the lhs.
	 */
	public String getLHS() {
		return myLHS;
	}

	/**
	 * Returns a string representation of the right hand side of the production
	 * rule.
	 *
	 * @return a string representation of the rhs.
	 */
	public String getRHS() {
		return myRHS;
	}

	/**
	 * Returns all symbols (both variables in terminals) in a production.
	 *
	 * @return all symbols in a production
	 */
	public List<String> getSymbols() {
		final SortedSet<String> symbols = new TreeSet<>();
		symbols.addAll(getVariables());
		symbols.addAll(getTerminals());
		return Lists.newArrayList(symbols);
	}

	/**
	 * Returns the sequence of symbols in either the left or right hand side.
	 * For example, for the production <CODE>A -> BCD</CODE> this would return
	 * the array of strings <CODE>{"B","C","D"}</CODE>.
	 */
	public List<String> getSymbolsOnRHS() {
		final List<String> list = new ArrayList<>();
		for (int i = 0; i < myRHS.length(); i++) {
			list.add(myRHS.substring(i, i + 1));
		}
		return list;
	}

	/**
	 * Returns all terminals in the production.
	 *
	 * @return all terminals in the production.
	 */
	public List<String> getTerminals() {
		final List<String> list = new ArrayList<>();
		final List<String> rhsTerminals = getTerminalsOnRHS();
		for (int k = 0; k < rhsTerminals.size(); k++) {
			if (!list.contains(rhsTerminals.get(k))) {
				list.add(rhsTerminals.get(k));
			}
		}

		final List<String> lhsTerminals = getTerminalsOnLHS();
		for (int i = 0; i < lhsTerminals.size(); i++) {
			if (!list.contains(lhsTerminals.get(i))) {
				list.add(lhsTerminals.get(i));
			}
		}

		return list;
	}

	/**
	 * Returns all terminals on the left hand side of the production.
	 *
	 * @return all terminals on the left hand side of the production.
	 */
	public List<String> getTerminalsOnLHS() {
		final List<String> list = new ArrayList<>();
		for (int i = 0; i < myLHS.length(); i++) {
			final char c = myLHS.charAt(i);
			if (ProductionChecker.isTerminal(c)) {
				list.add(myLHS.substring(i, i + 1));
			}
		}
		return list;
	}

	/**
	 * Returns all terminals on the right hand side of the production.
	 *
	 * @return all terminals on the right hand side of the production.
	 */
	public List<String> getTerminalsOnRHS() {
		final List<String> list = new ArrayList<>();
		for (int i = 0; i < myRHS.length(); i++) {
			final char c = myRHS.charAt(i);
			if (ProductionChecker.isTerminal(c)) {
				list.add(myRHS.substring(i, i + 1));
			}
		}
		return list;
	}

	/**
	 * Returns all variables in the production.
	 *
	 * @return all variables in the production.
	 */
	public List<String> getVariables() {

		final List<String> list = new ArrayList<>();
		final List<String> rhsVariables = getVariablesOnRHS();
		for (int k = 0; k < rhsVariables.size(); k++) {
			if (!list.contains(rhsVariables.get(k))) {
				list.add(rhsVariables.get(k));
			}
		}

		final List<String> lhsVariables = getVariablesOnLHS();
		for (int i = 0; i < lhsVariables.size(); i++) {
			if (!list.contains(lhsVariables.get(i))) {
				list.add(lhsVariables.get(i));
			}
		}

		return list;
	}

	/**
	 * Returns all variables on the left hand side of the production.
	 *
	 * @return all variables on the left hand side of the production.
	 */
	public List<String> getVariablesOnLHS() {
		final List<String> list = new ArrayList<>();
		if (myLHS == null) {
			return Collections.emptyList();
		}
		for (int i = 0; i < myLHS.length(); i++) {
			final char c = myLHS.charAt(i);
			if (ProductionChecker.isVariable(c)) {
				list.add(myLHS.substring(i, i + 1));
			}
		}
		return list;
	}

	/**
	 * Returns all variables on the right hand side of the production.
	 *
	 * @return all variables on the right hand side of the production.
	 */
	public List<String> getVariablesOnRHS() {
		final List<String> list = new ArrayList<>();
		for (int i = 0; i < myRHS.length(); i++) {
			final char c = myRHS.charAt(i);
			if (ProductionChecker.isVariable(c)) {
				list.add(myRHS.substring(i, i + 1));
			}
		}
		return list;
	}

	/**
	 * Returns a hashcode for this production.
	 *
	 * @return the hashcode for this production
	 */
	@Override
	public int hashCode() {
		return myRHS.hashCode() ^ myLHS.hashCode();
	}

	/**
	 * Sets the left hand side of production to <CODE>lhs</CODE>.
	 *
	 * @param lhs
	 *            the left hand side
	 */
	public void setLHS(final String lhs) {
		myLHS = lhs;
	}

	/**
	 * Sets the right hand side of production to <CODE>rhs</CODE>.
	 *
	 * @param rhs
	 *            the right hand side
	 */
	public void setRHS(final String rhs) {
		myRHS = rhs;
	}

	/**
	 * Returns a string representation of the production object.
	 *
	 * @return a string representation of the production object.
	 */
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append(getLHS());
		// buffer.append("->");
		buffer.append('\u2192');
		final String rhs = getRHS();
		buffer.append(rhs.length() == 0 ? Universe.curProfile.getEmptyString() : rhs);
		// buffer.append('\n');
		return buffer.toString();
	}
}
