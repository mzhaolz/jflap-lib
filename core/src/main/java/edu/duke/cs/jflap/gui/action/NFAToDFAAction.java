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

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import edu.duke.cs.jflap.automata.AutomatonChecker;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.gui.deterministic.ConversionPane;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;

/**
 * This is a simple action for showing the DFA form of an NFA.
 *
 * @author Thomas Finley
 */
public class NFAToDFAAction extends FSAAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The automaton. */
	private final FiniteStateAutomaton automaton;

	/** The environment. */
	private final Environment environment;

	/**
	 * Instantiates a new <CODE>NFAToDFAAction</CODE>.
	 *
	 * @param automaton
	 *            the automaton that input will be simulated on
	 * @param environment
	 *            the environment object that we shall add our simulator pane to
	 */
	public NFAToDFAAction(final FiniteStateAutomaton automaton, final Environment environment) {
		super("Convert to DFA", null);
		this.automaton = automaton;
		this.environment = environment;
		/*
		 * putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_R,
		 * MAIN_MENU_MASK+InputEvent.SHIFT_MASK));
		 */
	}

	/**
	 * Puts the DFA form in another window.
	 *
	 * @param e
	 *            the action event
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		if (automaton.getInitialState() == null) {
			JOptionPane.showMessageDialog(environment, "The automaton needs an initial state.", "No Initial State",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		final AutomatonChecker ac = new AutomatonChecker();
		if (!ac.isNFA(automaton)) {
			JOptionPane.showMessageDialog(environment, "This is not an NFA!", "Not an NFA", JOptionPane.ERROR_MESSAGE);
			return;
		}

		final ConversionPane convert = new ConversionPane((FiniteStateAutomaton) automaton.clone(), environment);
		environment.add(convert, "NFA to DFA", new CriticalTag() {
		});
		environment.setActive(convert);
	}
}
