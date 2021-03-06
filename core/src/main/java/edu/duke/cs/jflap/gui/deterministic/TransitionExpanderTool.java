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

import javax.swing.Icon;
import javax.swing.ImageIcon;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.gui.editor.TransitionTool;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * This is a specialized transition tool that handles the expansion of a group
 * on a terminal. The user clicks on a state, and drags possibly to another
 * state if he wants the default in the input dialog to be the destination
 * state's set of NFA states, or to empty space. The user is then prompted for
 * the terminal to expand on, and then asked to specify those states which are
 * to appear.
 *
 * @author Thomas Finley
 */
public class TransitionExpanderTool extends TransitionTool {
	/** The conversion to DFA controller. */
	private final ConversionController controller;

	/**
	 * Instantiates a new <CODE>TransitionExpanderTool</CODE>.
	 *
	 * @param view
	 *            the view where the automaton is drawn
	 * @param drawer
	 *            the object that draws the automaton
	 * @param controller
	 *            the NFA to DFA controller object
	 */
	public TransitionExpanderTool(final AutomatonPane view, final AutomatonDrawer drawer,
			final ConversionController controller) {
		super(view, drawer);
		this.controller = controller;
	}

	/**
	 * Returns the tool icon.
	 *
	 * @return the group expander tool icon
	 */
	@Override
	protected Icon getIcon() {
		final java.net.URL url = getClass().getResource("/ICON/expand_group.gif");
		return new ImageIcon(url);
	}

	/**
	 * Gets the tool tip for this tool.
	 *
	 * @return the tool tip for this tool
	 */
	@Override
	public String getToolTip() {
		return "Expand Group on Terminal";
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
		controller.expandState(first, event.getPoint(), state);
		first = null;
		getView().repaint();
	}
}
