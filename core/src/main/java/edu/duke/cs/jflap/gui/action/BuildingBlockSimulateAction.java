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
 * Created on Jun 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.duke.cs.jflap.gui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.AutomatonSimulator;
import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.automata.turing.TMSimulator;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;
import edu.duke.cs.jflap.gui.sim.SimulatorPane;

/**
 * @author Andrew
 *
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class BuildingBlockSimulateAction extends SimulateAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This particular action may only be applied to finite state automata.
	 *
	 * @param object
	 *            the object to test for applicability
	 * @return <CODE>true</CODE> if the passed in object is a finite state
	 *         automaton, <CODE>false</CODE> otherwise
	 */
	public static boolean isApplicable(final Serializable object) {
		return object instanceof TuringMachine;
	}

	/** The automaton this simulate action runs simulations on! */
	private final Automaton automaton;

	/** The environment. */
	private Environment environment = null;

	/**
	 * Instantiates a new <CODE>NoInteractionSimulateAction</CODE>.
	 *
	 * @param automaton
	 *            the automaton that input will be simulated on
	 * @param environment
	 *            the environment object that we shall add our simulator pane to
	 */
	public BuildingBlockSimulateAction(final Automaton automaton, final Environment environment) {
		super(automaton, environment);
		putValue(NAME, "Step by BuildingBlock");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, MAIN_MENU_MASK));
		this.automaton = automaton;
		this.environment = environment;
	}

	/**
	 * Performs the action.
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		if (automaton.getInitialState() == null) {
			JOptionPane.showMessageDialog((Component) e.getSource(),
					"Simulation requires an automaton\n" + "with an initial state!", "No Initial State",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		final String[] input = initialInput((Component) e.getSource(), "");
		if (input == null) {
			return;
		}
		List<Configuration> configs = null;
		final AutomatonSimulator simulator = getSimulator(automaton);
		// Get the initial configurations.
		if (getObject() instanceof TuringMachine) {
			final String[] s = input;
			configs = ((TMSimulator) simulator).getInitialConfigurations(Arrays.asList(s));
		} else {
			final String s = input[0];
			configs = simulator.getInitialConfigurations(s);
		}
		handleInteraction(automaton, simulator, configs, input);
	}

	/**
	 * Given initial configurations, the simulator, and the automaton, takes any
	 * further action that may be necessary. In the case of stepwise operation,
	 * which is the default, an additional tab is added to the environment
	 *
	 * @param automaton
	 *            the automaton input is simulated on
	 * @param simulator
	 *            the automaton simulator for this automaton
	 * @param configurations
	 *            the initial configurations generated
	 * @param initialInput
	 *            the object that represents the initial input; this is a String
	 *            object in most cases, but may differ for multiple tape turing
	 *            machines
	 */
	@Override
	public void handleInteraction(final Automaton automaton, final AutomatonSimulator simulator,
			final List<Configuration> configurations, Object initialInput) {
		final SimulatorPane simpane = new SimulatorPane(automaton, simulator, configurations, environment, true);
		if (initialInput instanceof String[]) {
			initialInput = java.util.Arrays.asList((String[]) initialInput);
		}
		environment.add(simpane, "Simulate: " + initialInput, new CriticalTag() {
		});
		environment.setActive(simpane);
	}
}
