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

package edu.duke.cs.jflap.gui.regular;

import java.awt.event.MouseEvent;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.gui.editor.TransitionTool;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * A tool that handles the creation of transitions for the regular expression to
 * FSA conversion. This simply calls the appropriate
 * <CODE>REToFSAController</CODE> method.
 *
 * @see edu.duke.cs.jflap.gui.regular.REToFSAController#transitionCreate
 *
 * @author Thomas Finley
 */
public class RegularToAutomatonTransitionTool extends TransitionTool {
	/** The regular conversion controller. */
	private final REToFSAController controller;

	/**
	 * Instantiates a new transition tool.
	 *
	 * @param view
	 *            the view where the automaton is drawn
	 * @param drawer
	 *            the object that draws the automaton
	 * @param controller
	 *            the RE to FSA controller object
	 */
	public RegularToAutomatonTransitionTool(final AutomatonPane view, final AutomatonDrawer drawer,
			final REToFSAController controller) {
		super(view, drawer);
		this.controller = controller;
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
			controller.transitionCreate(first, state);
		}
		first = null;
		getView().repaint();
	}
}
