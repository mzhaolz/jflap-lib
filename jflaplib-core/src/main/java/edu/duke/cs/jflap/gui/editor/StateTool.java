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

package edu.duke.cs.jflap.gui.editor;

import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import edu.duke.cs.jflap.gui.environment.AutomatonEnvironment;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * A tool that handles the creation of states.
 *
 * @author Thomas Finley
 */
public class StateTool extends Tool {
	/** The state that was created. */
	edu.duke.cs.jflap.automata.State state = null;

	/**
	 * Instantiates a new state tool.
	 */
	public StateTool(final AutomatonPane view, final AutomatonDrawer drawer) {
		super(view, drawer);
	}

	/**
	 * Returns the tool icon.
	 *
	 * @return the state tool icon
	 */
	@Override
	protected Icon getIcon() {
		final java.net.URL url = getClass().getResource("/ICON/state.gif");
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
		return "State Creator";
	}

	/**
	 * When the user drags, one moves the created state.
	 *
	 * @param event
	 *            the mouse event
	 */
	@Override
	public void mouseDragged(final MouseEvent event) {
		state.setPoint(event.getPoint());
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
		if (getDrawer().getAutomaton().getEnvironmentFrame() != null) {
			((AutomatonEnvironment) getDrawer().getAutomaton().getEnvironmentFrame().getEnvironment()).saveStatus();
		}
		state = getAutomaton().createState(event.getPoint());
		getView().repaint();
	}
}
