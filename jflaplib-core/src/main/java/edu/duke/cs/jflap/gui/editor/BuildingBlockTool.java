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

/*
 * Created on Jun 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.duke.cs.jflap.gui.editor;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * @author Andrew
 *
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class BuildingBlockTool extends Tool {
	/** The first clicked state. */
	protected State first;

	/** The point over which we are hovering. */
	protected Point hover;

	/** The state that was created. */
	edu.duke.cs.jflap.automata.turing.TMState block = null;

	/**
	 * Instantiates a new transition tool. The transition creator is obtained
	 * from whatever is returned from the transition creator factory.
	 *
	 * @see edu.duke.cs.jflap.gui.editor.TransitionCreator#creatorForAutomaton
	 */
	public BuildingBlockTool(final AutomatonPane view, final AutomatonDrawer drawer) {
		super(view, drawer);
	}

	/**
	 * Instantiates a new transition tool.
	 *
	 * @param view
	 *            the view where the automaton is drawn
	 * @param drawer
	 *            the object that draws the automaton
	 * @param creator
	 *            the transition creator for the type of automata we are editing
	 */
	public BuildingBlockTool(final AutomatonPane view, final AutomatonDrawer drawer, final TransitionCreator creator) {
		super(view, drawer);
	}

	/**
	 * Returns the tool icon.
	 *
	 * @return the transition tool icon
	 */
	@Override
	protected Icon getIcon() {
		final java.net.URL url = getClass().getResource("/ICON/blocks.gif");
		return new javax.swing.ImageIcon(url);
	}

	/**
	 * When we release the mouse, a transition from the start state to this
	 * released state must be formed.
	 *
	 * @param event
	 *            the mouse event
	 */
	// public void mouseReleased(MouseEvent event) {
	// // Did we even start at a state?
	// if (first == null) return;
	// State state = getDrawer().stateAtPoint(event.getPoint());
	// if (state != null) {
	// creator.createTransition(first, state);
	// /*if (t != null)
	// getAutomaton().addTransition(t);*/
	// }
	// first = null;
	// getView().repaint();
	// }
	/**
	 * Returns the keystroke to switch to this tool, the B key.
	 *
	 * @return the keystroke to switch to this tool
	 */
	@Override
	public KeyStroke getKey() {
		return KeyStroke.getKeyStroke('b');
	}

	/**
	 * Gets the tool tip for this tool.
	 *
	 * @return the tool tip for this tool
	 */
	@Override
	public String getToolTip() {
		return "Building Block Creator";
	}

	/**
	 * When the mouse is dragged someplace, updates the "hover" point so the
	 * line from the state to the mouse can be drawn.
	 *
	 * @param event
	 *            the mouse event
	 */
	@Override
	public void mouseDragged(final MouseEvent event) {
		if (first == null) {
			return;
		}
		hover = event.getPoint();
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
		// MERLIN MERLIN MERLIN MERLIN MERLIN//

		block = ((TuringMachine) getAutomaton()).createBlock(event.getPoint());
		getView().repaint();
	}
}
