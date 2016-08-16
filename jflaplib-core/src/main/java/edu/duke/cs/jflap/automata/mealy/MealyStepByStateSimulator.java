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

package edu.duke.cs.jflap.automata.mealy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.AutomatonSimulator;
import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;

/**
 * The Mealy machine step by state simulator simulates the behavior of a Mealy
 * machine. It takes a <code>MealyMachine</code> object and runs an input string
 * on the object.
 *
 * <p>
 * It simulates the machine's behavior by stepping through one state at a time.
 * Output of the machine can be accessed through
 * {@link MealyConfiguration#getOutput()} and is printed out on the tape in the
 * simulation window. This does not deal with lambda transitions.
 *
 * @author Jinghui Lim
 * @see edu.duke.cs.jflap.automata.mealy.MealyConfiguration
 *
 */
public class MealyStepByStateSimulator extends AutomatonSimulator {
	/**
	 * Creates a Mealy machine step by state simulator for the given automaton.
	 *
	 * @param automaton
	 *            the machine to simulate
	 */
	public MealyStepByStateSimulator(final Automaton automaton) {
		super(automaton);
	}

	/**
	 * Returns a <code>MealyConfiguration</code> that represents the initial
	 * configuration of the Mealy machine, before any input has been processed.
	 * This returns an array of length one.
	 *
	 * @param input
	 *            the input string to simulate
	 */
	@Override
	public List<Configuration> getInitialConfigurations(final String input) {
		final Configuration config = new MealyConfiguration(myAutomaton.getInitialState(), null, input, input, "");
		final List<Configuration> configs = new ArrayList<>();
		configs.add(config);
		return configs;
	}

	/**
	 * Returns <code>true</code> if all the input has been processed and output
	 * generated. This calls the {@link MealyConfiguration#isAccept()}. It
	 * returns <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if all input has been processed, <code>false
	 * </code> otherwise
	 */
	@Override
	public boolean isAccepted() {
		final Iterator<Configuration> it = myConfigurations.iterator();
		while (it.hasNext()) {
			final MealyConfiguration config = (MealyConfiguration) it.next();
			if (config.isAccept()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Simulated the input in the machine.
	 *
	 * @param input
	 *            the input string to run on the machine
	 * @return <code>true</code> once the entire input string has been
	 *         processed.
	 * @see #isAccepted()
	 */
	@Override
	public boolean simulateInput(final String input) {
		myConfigurations.clear();
		final List<Configuration> initialConfigs = getInitialConfigurations(input);
		myConfigurations.addAll(initialConfigs);

		while (!myConfigurations.isEmpty()) {
			if (isAccepted()) {
				return true;
			}
			final ArrayList<MealyConfiguration> configurationsToAdd = new ArrayList<>();
			final Iterator<Configuration> it = myConfigurations.iterator();
			while (it.hasNext()) {
				final MealyConfiguration config = (MealyConfiguration) it.next();
				configurationsToAdd.addAll(stepConfiguration(config));
				it.remove();
			}
			myConfigurations.addAll(configurationsToAdd);
		}
		return false;
	}

	/**
	 * Simulates one step for a particular configuration, adding all possible
	 * configurations reachable in one step to a list of possible
	 * configurations.
	 *
	 * @param configuration
	 *            the configuration simulate one step on
	 */
	@Override
	public List<MealyConfiguration> stepConfiguration(final Configuration configuration) {
		final List<MealyConfiguration> list = new ArrayList<>();
		final MealyConfiguration config = (MealyConfiguration) configuration;

		final String unprocessedInput = config.getUnprocessedInput();
		final String totalInput = config.getInput();
		final State currentState = config.getCurrentState();

		final List<Transition> transitions = myAutomaton.getTransitionsFromState(currentState);
		for (int i = 0; i < transitions.size(); i++) {
			final MealyTransition trans = (MealyTransition) transitions.get(i);
			final String transLabel = trans.getLabel();
			if (unprocessedInput.startsWith(transLabel)) {
				String input = "";
				if (transLabel.length() < unprocessedInput.length()) {
					input = unprocessedInput.substring(transLabel.length());
				}
				final State toState = trans.getToState();
				final String output = config.getOutput() + trans.getOutput();
				final MealyConfiguration configToAdd = new MealyConfiguration(toState, config, totalInput, input,
						output);
				list.add(configToAdd);
			}
		}
		return list;
	}
}
