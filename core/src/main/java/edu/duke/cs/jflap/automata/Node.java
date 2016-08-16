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

package edu.duke.cs.jflap.automata;

/**
 * A node object is a data structure used in graph algorithms. It stores a State
 * object and a color. This object is used to find the unreachable states in an
 * automaton.
 *
 * @see edu.duke.cs.jflap.automata.State
 *
 * @author Ryan Cavalcante
 */
public class Node {
	/** String for white. */
	protected static final String WHITE = "white";

	/** String for grey. */
	protected static final String GREY = "grey";

	/** String for black. */
	protected static final String BLACK = "black";

	/** Color of node. */
	protected String myColor;

	/** State of node. */
	protected State myState;

	/**
	 * Creates a Node object with no state and no color.
	 */
	public Node() {
		myState = null;
		myColor = "";
	}

	/**
	 * Creates a Node object with <CODE>state</CODE> and no color.
	 *
	 * @param state
	 *            the state contained by the Node object
	 */
	public Node(final State state) {
		myState = state;
		myColor = "";
	}

	/**
	 * Creates a Node object with <CODE>state</CODE> and <CODE>color</CODE>.
	 *
	 * @param state
	 *            the state contained by the Node object
	 * @param color
	 *            the color of the node.
	 */
	public Node(final State state, final String color) {
		myState = state;
		myColor = color;
	}

	/**
	 * Colors the Node black.
	 */
	public void colorBlack() {
		myColor = BLACK;
	}

	/**
	 * Colors the Node grey.
	 */
	public void colorGrey() {
		myColor = GREY;
	}

	/**
	 * Colors the node white.
	 */
	public void colorWhite() {
		myColor = WHITE;
	}

	/**
	 * Returns the color of the Node object.
	 *
	 * @return the color of the Node object.
	 */
	public String getColor() {
		return myColor;
	}

	/**
	 * Returns the state contained by the Node object.
	 *
	 * @return the state contained by the Node object.
	 */
	public State getState() {
		return myState;
	}

	/**
	 * Returns true if the Node is black.
	 *
	 * @return true if the Node is black.
	 */
	public boolean isBlack() {
		if (myColor.equals(BLACK)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the Node is grey.
	 *
	 * @return true if the Node is grey.
	 */
	public boolean isGrey() {
		if (myColor.equals(GREY)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the Node is white.
	 *
	 * @return true if the Node is white.
	 */
	public boolean isWhite() {
		if (myColor.equals(WHITE)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns a string representation of the Node object, returning a string
	 * representation of its state and color.
	 *
	 * @return a string representation of the Node object.
	 */
	@Override
	public String toString() {
		return "STATE: " + myState.toString() + " COLOR: " + myColor;
	}
}
