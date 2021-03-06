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
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import edu.duke.cs.jflap.automata.UselessStatesDetector;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.automata.graph.FSAEqualityChecker;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * This tests to see if two finite state automatons accept the same language.
 *
 * @author Thomas Finley
 */
public class DFAEqualityAction extends FSAAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The equality checker. */
	private static FSAEqualityChecker checker = new FSAEqualityChecker();

	/** The environment. */
	private final Environment environment;

	/**
	 * Instantiates a new <CODE>DFAEqualityAction</CODE>.
	 *
	 * @param automaton
	 *            the automaton that input will be simulated on
	 * @param environment
	 *            the environment object that we shall add our simulator pane to
	 */
	public DFAEqualityAction(final FiniteStateAutomaton automaton, final Environment environment) {
		super("Compare Equivalence", null);
		this.environment = environment;
		/*
		 * putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_R,
		 * MAIN_MENU_MASK+InputEvent.SHIFT_MASK));
		 */
	}

	/**
	 * Runs a comparison with another automaton.
	 *
	 * @param e
	 *            the action event
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final JComboBox<EnvironmentFrame> combo = new JComboBox<>();
		// Figure out what existing environments in the program have
		// the type of structure that we need.
		final List<EnvironmentFrame> frames = Universe.frames();
		for (int i = 0; i < frames.size(); i++) {
			if (!isApplicable(frames.get(i).getEnvironment().getObject())
					|| frames.get(i).getEnvironment() == environment) {
				continue;
			}
			combo.addItem(frames.get(i));
		}
		// Set up our automaton.
		FiniteStateAutomaton automaton = (FiniteStateAutomaton) environment.getObject();

		if (combo.getItemCount() == 0) {
			JOptionPane.showMessageDialog(Universe.frameForEnvironment(environment), "No other FAs around!");
			return;
		}
		if (automaton.getInitialState() == null) {
			JOptionPane.showMessageDialog(Universe.frameForEnvironment(environment),
					"This automaton has no initial state!");
			return;
		}
		// Prompt the user.
		final int result = JOptionPane.showOptionDialog(Universe.frameForEnvironment(environment), combo,
				"Compare against FA", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (result != JOptionPane.YES_OPTION && result != JOptionPane.OK_OPTION) {
			return;
		}
		FiniteStateAutomaton other = (FiniteStateAutomaton) ((EnvironmentFrame) combo.getSelectedItem())
				.getEnvironment().getObject();
		if (other.getInitialState() == null) {
			JOptionPane.showMessageDialog(Universe.frameForEnvironment(environment),
					"The other automaton has no initial state!");
			return;
		}
		other = (FiniteStateAutomaton) UselessStatesDetector.cleanAutomaton(other);
		automaton = (FiniteStateAutomaton) UselessStatesDetector.cleanAutomaton(automaton);
		final String checkedMessage = checker.equals(other, automaton) ? "They ARE equivalent!"
				: "They AREN'T equivalent!";
		JOptionPane.showMessageDialog(Universe.frameForEnvironment(environment), checkedMessage);
	}
}
