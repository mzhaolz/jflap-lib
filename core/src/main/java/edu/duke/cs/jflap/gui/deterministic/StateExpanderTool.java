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
import javax.swing.KeyStroke;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.gui.editor.Tool;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * This is a tool that expands a state completely.
 *
 * @author Thomas Finley
 */
public class StateExpanderTool extends Tool {
	/** The deterministic NFA to DFA controller. */
	private final ConversionController controller;

	/**
	 * Instantiates a new state tool.
	 */
	public StateExpanderTool(final AutomatonPane view, final AutomatonDrawer drawer,
			final ConversionController controller) {
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
		final java.net.URL url = getClass().getResource("/ICON/state_expander.gif");
		return new ImageIcon(url);
	}

	/**
	 * Returns the keystroke to switch to this tool, S.
	 *
	 * @return the keystroke for this tool
	 */
	@Override
	public KeyStroke getKey() {
		return KeyStroke.getKeyStroke('s');
	}

	/**
	 * Gets the tool tip for this tool.
	 *
	 * @return the tool tip for this tool
	 */
	@Override
	public String getToolTip() {
		return "State Expander";
	}

	/**
	 * When the user clicks, one creates a state.
	 *
	 * @param event
	 *            the mouse event
	 */
	@Override
	public void mousePressed(final MouseEvent event) {
		final State state = getDrawer().stateAtPoint(event.getPoint());
		if (state == null) {
			return;
		}
		controller.expandState(state);
	}
}
