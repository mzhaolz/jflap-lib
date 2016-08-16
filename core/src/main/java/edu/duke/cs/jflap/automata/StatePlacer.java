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

import java.awt.Point;

/**
 * The state placer object can be used to determine the location (on the canvas)
 * to place a State. Currently, the placement algorithm is simply choosing
 * random x and y coordinates in the range of 0 to X_MAX and Y_MAX respectively.
 *
 * @author Ryan Cavalcante
 */
public class StatePlacer {
	/** The maximum value for the X-coordinate. */
	protected final static int X_MAX = 600;

	/** The maximum value for the Y-coordinate. */
	protected final static int Y_MAX = 600;

	/**
	 * Instantiates a <CODE>StatePlacer</CODE>.
	 */
	public StatePlacer() {
	}

	/**
	 * Returns a Point object that represents where to place the State on the
	 * canvas.
	 *
	 * @param automaton
	 *            the automaton.
	 * @return a Point object that represents where to place the State on the
	 *         canvas.
	 */
	public Point getPointForState(final Automaton automaton) {
		final double xcoord = Math.random() * X_MAX;
		final int x = (int) xcoord;
		final double ycoord = Math.random() * Y_MAX;
		final int y = (int) ycoord;
		return new Point(x, y);
	}
}
