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

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * This is a view that holds a tool bar and the canvas where the automaton is
 * displayed.
 *
 * @author Thomas Finley
 */
public class EditBlockPane extends EditorPane {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	protected State myBlock = null;

	protected State myOldBlock = null;

	/**
	 * Instantiates a new editor pane for the given automaton.
	 *
	 * @param automaton
	 *            the automaton to create the editor pane for
	 */
	public EditBlockPane(final Automaton automaton) {
		super(new SelectionDrawer(automaton));
	}

	public State getBlock() {
		return myBlock;
	}

	public State getOldBlock() {
		return myOldBlock;
	}

	public void setBlock(final State state) {
		myBlock = state;
	}

	public void setOldBlock(final State state) {
		myOldBlock = state;
	}
}
