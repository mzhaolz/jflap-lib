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

package edu.duke.cs.jflap.gui.grammar.parse;

import javax.swing.tree.DefaultMutableTreeNode;

class UnrestrictedTreeNode extends DefaultMutableTreeNode {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The text! */
	private final String text;

	/** The weight. */
	public double weight = 1.0;

	/** The highest row. */
	public int highest = 0;

	/** The lowest row. */
	public int lowest = 0;

	/**
	 * Creates a new unrestricted tree node.
	 *
	 * @param text
	 *            the label for this unrestricted tree node
	 */
	public UnrestrictedTreeNode(final String text) {
		super(text);
		this.text = text;
	}

	/**
	 * Returns the text.
	 *
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns the length of this node, which is the length of the text.
	 */
	public int length() {
		return text.length();
	}

	/**
	 * Returns a string representation of the node.
	 *
	 * @return a string representation of the node
	 */
	@Override
	public String toString() {
		return super.toString();
		// return "("+text+", "+weight+")";
	}
}
