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
import javax.swing.KeyStroke;

import edu.duke.cs.jflap.gui.environment.AutomatonEnvironment;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * First, let's make it work, then we'll make the interface so you don't have to
 * click undo and then click randomly.
 *
 * @author Henry Qin
 */
public class UndoTool extends Tool {
	/**
	 * Instantiates a new delete tool.
	 */
	public UndoTool(final AutomatonPane view, final AutomatonDrawer drawer) {
		super(view, drawer);
	}

	/**
	 * Returns the tool icon.
	 *
	 * @return the delete tool icon
	 */
	@Override
	protected Icon getIcon() {
		final java.net.URL url = getClass().getResource("/ICON/undo2.jpg");
		return new javax.swing.ImageIcon(url);
	}

	/**
	 * Returns the key stroke to switch to this tool, the D key.
	 *
	 * @return the key stroke to switch to this tool
	 */
	@Override
	public KeyStroke getKey() {
		return KeyStroke.getKeyStroke('u');
	}

	/**
	 * Gets the tool tip for this tool.
	 *
	 * @return the tool tip for this tool
	 */
	@Override
	public String getToolTip() {
		return "Undoer - Click anywhere in the editor pane after clicking me.";
	}

	/**
	 * When the user clicks, we delete either the state or, if no state, the
	 * transition found at this point. If there's nothing at this point, nothing
	 * happens.
	 *
	 * @param event
	 *            the mouse event
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		// do nothing
		((AutomatonEnvironment) getDrawer().getAutomaton().getEnvironmentFrame().getEnvironment()).restoreStatus();
		// getView().repaint();
	}
}
