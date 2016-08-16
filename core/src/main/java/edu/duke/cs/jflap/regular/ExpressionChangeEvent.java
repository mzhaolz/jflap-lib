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

package edu.duke.cs.jflap.regular;

import java.util.EventObject;

/**
 * This event should be distributed when a regular expression object changes.
 *
 * @author Thomas Finley
 */
public class ExpressionChangeEvent extends EventObject {
	/**
	 *
	 */
	private static final long serialVersionUID = 8094984580797690783L;

	/** The changed RE. */
	private final RegularExpression expression;

	/** The old string representation of the RE. */
	private final String old;

	/**
	 * Instantiates a change event.
	 *
	 * @param expression
	 *            the expression object that was changed
	 * @param old
	 *            the string representing the old regular expression
	 */
	public ExpressionChangeEvent(final RegularExpression expression, final String old) {
		super(expression);
		this.expression = expression;
		this.old = old;
	}

	/**
	 * Returns the regular expression that was changed.
	 *
	 * @return the regular expression that was changed
	 */
	public RegularExpression getExpression() {
		return expression;
	}

	/**
	 * Returns the old string representation of the expression.
	 *
	 * @return the old string representation of the expression
	 */
	public String getOld() {
		return old;
	}
}
