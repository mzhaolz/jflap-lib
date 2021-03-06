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

import java.util.Comparator;

/**
 * This is a comparator for productions that sorts productions lexigraphically,
 * with the exception that the start variable is always ordered first on the
 * LHS.
 *
 * @author Thomas Finley
 */
public class ProductionComparator implements Comparator<Production> {
	/** The start variable. */
	private String start = null;

	/**
	 * Instantiates a comparator, getting the start variable from a given
	 * grammar.
	 *
	 * @param grammar
	 *            the grammar
	 * @throws IllegalArgumentException
	 *             if the grammar does not have a start variable
	 */
	public ProductionComparator(final Grammar grammar) {
		this(grammar.getStartVariable());
	}

	/**
	 * Instantiates a comparator, with the start variable is passed in
	 * explicitly.
	 *
	 * @param variable
	 *            the start variable
	 */
	public ProductionComparator(final String variable) {
		start = variable;
		if (start == null) {
			throw new IllegalArgumentException("Null start variable!");
		}
	}

	/**
	 * Compares two productions.
	 */
	@Override
	public int compare(final Production p1, final Production p2) {
		if (start.equals(p1.getLHS())) {
			if (p1.getLHS().equals(p2.getLHS())) {
				return 0;
			} else {
				return -1;
			}
		}
		if (start.equals(p2.getLHS())) {
			return 1;
		}
		return p1.getLHS().compareTo(p2.getRHS());
	}

	/**
	 * Two production comparators are equal if they have the same start
	 * variable.
	 *
	 * @param object
	 *            the object to test for equality
	 * @return if the two objects are equal
	 */
	@Override
	public boolean equals(final Object object) {
		try {
			final ProductionComparator c = (ProductionComparator) object;
			return c.start.equals(start);
		} catch (final ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns the start variable this comparator keys on.
	 *
	 * @return the start variable
	 */
	public String getStartVariable() {
		return start;
	}

	/**
	 * Included to ensure compatibility with equals.
	 *
	 * @return a hash code for this object
	 */
	@Override
	public int hashCode() {
		return getClass().hashCode() ^ start.hashCode();
	}
}
