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

package edu.duke.cs.jflap.gui.deterministic;

import java.awt.event.MouseEvent;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.gui.editor.TransitionTool;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * Useful in creating transitions to the trap state
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class TrapTransitionTool extends TransitionTool {

	/** The adding trap state controller. */
	private final AddTrapStateController myController;

	/**
	 * Instantiates a new transition tool.
	 *
	 * @param view
	 *            the view where the automaton is drawn
	 * @param drawer
	 *            the object that draws the automaton
	 * @param controller
	 *            the controller object for the transition from an FSA to an RE
	 */
	public TrapTransitionTool(final AutomatonPane view, final AutomatonDrawer drawer,
			final AddTrapStateController controller) {
		super(view, drawer);
		myController = controller;
	}

	/**
	 * When we release the mouse, a transition from the start state to this
	 * released state must be formed.
	 *
	 * @param event
	 *            the mouse event
	 */
	@Override
	public void mouseReleased(final MouseEvent event) {
		// Did we even start at a state?
		if (first == null) {
			return;
		}
		final State state = getDrawer().stateAtPoint(event.getPoint());
		if (state != null) {
			myController.transitionCreate(first, state);
		}
		first = null;
		getView().repaint();
	}
}