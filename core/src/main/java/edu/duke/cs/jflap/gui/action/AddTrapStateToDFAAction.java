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
import java.util.TreeSet;

import javax.swing.JOptionPane;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.AutomatonChecker;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.gui.deterministic.AddTrapStatePane;
import edu.duke.cs.jflap.gui.environment.AutomatonEnvironment;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;

/**
 * Add a trap state to existing DFA or NFA
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class AddTrapStateToDFAAction extends FSAAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The automaton. */
	private final Automaton automaton;

	/** The environment. */
	private final AutomatonEnvironment environment;

	/**
	 * Instantiates a new <CODE>MinimizeTreeAction</CODE>.
	 *
	 * @param automaton
	 *            the automaton that the tree will be shown for
	 * @param environment
	 *            the environment object that we shall add our simulator pane to
	 */
	public AddTrapStateToDFAAction(final AutomatonEnvironment environment) {
		super("Add Trap State to DFA", null);
		this.environment = environment;
		automaton = environment.getAutomaton();
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
			JOptionPane.showMessageDialog(Universe.frameForEnvironment(environment),
					"The automaton should have " + "an initial state.");
			return;
		}
		final AutomatonChecker ac = new AutomatonChecker();
		if (ac.isNFA(automaton)) {
			JOptionPane.showMessageDialog(Universe.frameForEnvironment(environment), "This isn't a DFA!");
			return;
		}
		final boolean isComplete = checkIfDFAisComplete();
		if (isComplete) {
			JOptionPane.showMessageDialog(Universe.frameForEnvironment(environment),
					"DFA is complete. No need for the Trap State");

			return;
		}
		final AddTrapStatePane trapPane = new AddTrapStatePane(environment);
		environment.add(trapPane, "Adding Trap State", new CriticalTag() {
		});
		environment.setActive(trapPane);
	}

	/**
	 * Check if DFA already has trap state and complete
	 *
	 * @return True if DFA already has a trap state and complete
	 */
	private boolean checkIfDFAisComplete() {
		final List<Transition> t = automaton.getTransitions();
		final List<State> s = automaton.getStates();
		final TreeSet<String> reads = new TreeSet<>();
		for (int i = 0; i < t.size(); i++) {
			reads.add(t.get(i).getDescription());
		}
		int count = 0;
		for (int i = 0; i < s.size(); i++) {
			final List<Transition> tt = automaton.getTransitionsFromState(s.get(i));
			if (tt.size() < reads.size()) {
				count++;
			}
		}
		if (count == 0) {
			return true;
		}
		return false;
	}
}
