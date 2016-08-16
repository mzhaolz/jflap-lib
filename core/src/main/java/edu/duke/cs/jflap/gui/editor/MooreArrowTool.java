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

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.gui.environment.AutomatonEnvironment;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * This is a subclass of a <code>MealyArrowTool</code> for Moore machines that
 * allows the arrow tool to change the output of a state. It is identical in
 * every way except that, when a mouse is clicked on a state, instead of doing
 * nothing, it prompts the user for a new state output. Other mouse methods for
 * right-clicking and click-and-drag remain the same.
 *
 * @see #mouseClicked(MouseEvent)
 * @see edu.duke.cs.jflap.automata.mealy.MooreMachine
 * @see MooreStateTool
 * @see MooreToolBox
 * @author Jinghui Lim
 *
 */
public class MooreArrowTool extends MealyArrowTool {
	/**
	 * Instantiates a new arrow tool.
	 *
	 * @param view
	 *            the view where the automaton is drawn
	 * @param drawer
	 *            the object that draws the automaton
	 */
	public MooreArrowTool(final AutomatonPane view, final AutomatonDrawer drawer) {
		super(view, drawer);
	}

	/**
	 * Instantiates a new arrow tool.
	 *
	 * @param view
	 *            the view where the automaton is drawn
	 * @param drawer
	 *            the object that draws the automaton
	 * @param creator
	 *            the transition creator used for editing transitions
	 */
	public MooreArrowTool(final AutomatonPane view, final AutomatonDrawer drawer, final TransitionCreator creator) {
		super(view, drawer, creator);
	}

	/**
	 * Checks if the mouse was clicked on a state, and offers to change the
	 * state output if so. Otherwise, {@link ArrowTool#mouseClicked(MouseEvent)}
	 * is called so that other mouse events proceed as specified in
	 * <code>ArrowTool</code>.
	 *
	 * @param event
	 *            the mouse event
	 * @see ArrowTool#mouseClicked(MouseEvent)
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		((AutomatonEnvironment) getDrawer().getAutomaton().getEnvironmentFrame().getEnvironment()).saveStatus();
		if (event.getButton() == MouseEvent.BUTTON1) {
			final State state = getDrawer().stateAtPoint(event.getPoint());
			if (state == null) {
				super.mouseClicked(event);
			} else {
				MooreStateTool.editState(state);
			}
		}
	}
}
