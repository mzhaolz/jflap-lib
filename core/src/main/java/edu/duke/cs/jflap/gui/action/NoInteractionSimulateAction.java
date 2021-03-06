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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.AutomatonSimulator;
import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * This is the action used for the simulation of input on an automaton with no
 * interaction. This method can operate on any automaton. It uses a special
 * exception for the case of the multiple tape Turing machine.
 *
 * @author Thomas Finley
 */
public class NoInteractionSimulateAction extends SimulateAction {
	private static final long serialVersionUID = 21L;

	/** The steps in warnings. */
	protected static final int WARNING_STEP = 500;

	/** The environment. */
	private Environment environment = null;

	/**
	 * Instantiates a new <CODE>NoInteractionSimulateAction</CODE>.
	 *
	 * @param auto
	 *            the automaton that input will be simulated on
	 * @param environment
	 *            the environment object that we shall add our simulator pane to
	 */
	public NoInteractionSimulateAction(final Automaton automaton, final Environment environment) {
		super(automaton, environment);
		putValue(NAME, "Fast Run...");
		putValue(ACCELERATOR_KEY, null);
		this.environment = environment;
	}

	public NoInteractionSimulateAction(final Grammar gram, final Environment environment) {
		super(gram, environment);
		putValue(NAME, "Fast Run...");
		putValue(ACCELERATOR_KEY, null);
		this.environment = environment;
	}

	/**
	 * Confirms if the user wants to keep searching. This should be called
	 * periodically to give the user a chance to break out of infinite loops.
	 *
	 * @param generated
	 *            the number of configurations generated sofar
	 * @param component
	 *            the parent component of dialogs brought up
	 * @return <CODE>true</CODE> if we should continue searching, or
	 *         <CODE>false</CODE> if we should halt
	 */
	protected boolean confirmContinue(final int generated, final Component component) {
		final int result = JOptionPane.showConfirmDialog(component,
				generated + " configurations have been generated.  " + "Should we continue?");
		return result == JOptionPane.YES_OPTION;
	}

	/**
	 * This will search configurations for an accepting configuration.
	 *
	 * @param automaton
	 *            the automaton input is simulated on
	 * @param simulator
	 *            the automaton simulator for this automaton
	 * @param configs
	 *            the initial configurations generated
	 * @param initialInput
	 *            the object that represents the initial input; this is a String
	 *            object in most cases, but may differ for multiple tape turing
	 *            machines
	 */
	@Override
	public void handleInteraction(final Automaton automaton, final AutomatonSimulator simulator,
			List<Configuration> configs, final Object initialInput) {
		final JFrame frame = Universe.frameForEnvironment(environment);
		// How many configurations have we had?
		int numberGenerated = 0;
		// When should the next warning be?
		int warningGenerated = WARNING_STEP;
		// How many have accepted?
		int numberAccepted = 0;
		while (configs.size() > 0) {
			numberGenerated += configs.size();
			// Make sure we should continue.
			if (numberGenerated >= warningGenerated) {
				if (!confirmContinue(numberGenerated, frame)) {
					return;
				}
				while (numberGenerated >= warningGenerated) {
					warningGenerated *= 2;
				}
			}
			// Get the next batch of configurations.
			final List<Configuration> next = new ArrayList<>();
			for (int i = 0; i < configs.size(); i++) {
				if (configs.get(i).isAccept()) {
					numberAccepted++;
					if (!reportConfiguration(configs.get(i), frame)) {
						return;
					}
				} else {
					next.addAll(simulator.stepConfiguration(configs.get(i)));
				}
			}
			configs = next;
		}
		if (numberAccepted == 0) {
			JOptionPane.showMessageDialog(frame, "The input was rejected.");
			return;
		}
		JOptionPane.showMessageDialog(frame, numberAccepted + " configuration" + (numberAccepted == 1 ? "" : "s")
				+ " accepted, and\nother possibilities are exhausted.");
	}

	/**
	 * Reports a configuration that accepted.
	 *
	 * @param configuration
	 *            the configuration that accepted
	 * @param component
	 *            the parent component of dialogs brought up
	 * @return <CODE>true</CODE> if we should continue searching, or
	 *         <CODE>false</CODE> if we should halt
	 */
	protected boolean reportConfiguration(final Configuration configuration, final Component component) {
		final JComponent past = edu.duke.cs.jflap.gui.sim.TraceWindow.getPastPane(configuration);
		past.setPreferredSize(new java.awt.Dimension(300, 400));
		final String[] options = { "Keep looking", "I'm done" };
		final int result = JOptionPane.showOptionDialog(component, past, "Accepting configuration found!",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
		return result == 0;
	}
}
