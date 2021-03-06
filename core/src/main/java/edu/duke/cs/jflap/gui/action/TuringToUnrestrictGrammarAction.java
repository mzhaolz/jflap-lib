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

package edu.duke.cs.jflap.gui.action;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.gui.environment.AutomatonEnvironment;
import edu.duke.cs.jflap.gui.grammar.automata.ConvertController;
import edu.duke.cs.jflap.gui.grammar.automata.ConvertPane;
import edu.duke.cs.jflap.gui.grammar.automata.TuringConvertController;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * Class for converting Turing Machine to Unrestricted Grammar
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class TuringToUnrestrictGrammarAction extends ConvertAutomatonToGrammarAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static boolean isApplicable(final Object object) {
		return object instanceof TuringMachine;
	}

	public TuringToUnrestrictGrammarAction(final AutomatonEnvironment automatonEnvironment) {
		super("Convert to Unrestricted Grammar", automatonEnvironment);
		// TODO Auto-generated constructor stub
	}

	@Override
	// TODO: Need to put error check, make sure there is only one letter popping
	// and placing
	// More error check?
	protected boolean checkAutomaton() {
		// TODO Auto-generated method stub
		// return true for now
		return true;
	}

	@Override
	protected ConvertController initializeController(final ConvertPane pane, final SelectionDrawer drawer,
			final Automaton automaton) {
		// TODO Auto-generated method stub
		return new TuringConvertController(pane, drawer, (TuringMachine) automaton);
	}
}
