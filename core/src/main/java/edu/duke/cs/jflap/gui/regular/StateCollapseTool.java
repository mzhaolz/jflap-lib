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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.gui.editor.Tool;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * A tool that handles the removal of states. This calls the
 * {@link FSAToREController#stateCollapse} method on the clicked state.
 *
 * @see edu.duke.cs.jflap.gui.regular.FSAToREController#transitionCreate
 *
 * @author Thomas Finley
 */
public class StateCollapseTool extends Tool {
	/** The regular conversion controller. */
	private final FSAToREController controller;

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
	public StateCollapseTool(final AutomatonPane view, final AutomatonDrawer drawer,
			final FSAToREController controller) {
		super(view, drawer);
		this.controller = controller;
	}

	/**
	 * Returns the tool icon.
	 *
	 * @return the state tool icon
	 */
	@Override
	protected Icon getIcon() {
		final java.net.URL url = getClass().getResource("/ICON/state_collapse.gif");
		return new ImageIcon(url);
	}

	/**
	 * Returns the keystroke to switch to this tool, C.
	 *
	 * @return the keystroke for this tool
	 */
	@Override
	public KeyStroke getKey() {
		return KeyStroke.getKeyStroke('o');
	}

	/**
	 * Gets the tool tip for this tool.
	 *
	 * @return the tool tip for this tool
	 */
	@Override
	public String getToolTip() {
		return "State Collapser";
	}

	/**
	 * When we press the mouse, the convert controller should be told that
	 * transitions are done.
	 *
	 * @param event
	 *            the mouse event
	 */
	@Override
	public void mousePressed(final MouseEvent event) {
		final State s = getDrawer().stateAtPoint(event.getPoint());
		if (s != null) {
			controller.stateCollapse(s);
		}
	}
}
