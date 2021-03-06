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
import edu.duke.cs.jflap.gui.editor.StateTool;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * Aids in creating new trap state
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class TrapStateTool extends StateTool {

	/** The state that was created. */
	private State myTrapState = null;

	/** The controller object. */
	private final AddTrapStateController myController;

	/**
	 * Instantiates a new trap state tool.
	 *
	 * @param view
	 *            the view that the automaton is drawn in
	 * @param drawer
	 *            the automaton drawer for the view
	 * @param controller
	 *            the controller object we report to
	 */
	public TrapStateTool(final AutomatonPane view, final AutomatonDrawer drawer,
			final AddTrapStateController controller) {
		super(view, drawer);
		myController = controller;
	}

	/**
	 * When the user drags, one moves the created state.
	 *
	 * @param event
	 *            the mouse event
	 */
	@Override
	public void mouseDragged(final MouseEvent event) {
		if (myTrapState == null) {
			return;
		}
		myTrapState.setPoint(event.getPoint());
		getView().repaint();
	}

	/**
	 * When the user clicks, one creates a state.
	 *
	 * @param event
	 *            the mouse event
	 */
	@Override
	public void mousePressed(final MouseEvent event) {
		if ((myTrapState = myController.stateCreate(event.getPoint())) == null) {
			return;
		}
		getView().repaint();
	}
}
