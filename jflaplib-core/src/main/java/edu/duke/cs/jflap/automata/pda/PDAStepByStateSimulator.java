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

package edu.duke.cs.jflap.automata.pda;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.AutomatonSimulator;
import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;

/**
 * The PDA simulator object simulates the behavior of a pushdown automaton.
 * Given a PDA object and an input string, it can determine whether the machine
 * accepts the input or not.
 *
 * @author Ryan Cavalcante
 */
public class PDAStepByStateSimulator extends AutomatonSimulator {
	/** The variable to represent accept by empty stack. */
	protected static final int EMPTY_STACK = 0;

	/** The variable to represent accept by final state. */
	protected static final int FINAL_STATE = 1;

	/** The mode of acceptance (either by final state or empty stack). */
	protected int myAcceptance;

	/**
	 * Creates a PDA simulator for the given automaton.
	 *
	 * @param automaton
	 *            the machine to simulate
	 */
	public PDAStepByStateSimulator(final Automaton automaton) {
		super(automaton);
		/** default acceptance is by final state. */
		final Object[] possibleValues = { "Final State", "Empty Stack" };
		final Object selectedValue = JOptionPane.showInputDialog(null, "Accept by", "Input",
				JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
		if (selectedValue.equals(possibleValues[0])) {
			myAcceptance = FINAL_STATE;
			// EDebug.print("fstate");
		} else if (selectedValue.equals(possibleValues[1])) {
			myAcceptance = EMPTY_STACK;
			// EDebug.print("estack");
		}
		// myAcceptance = FINAL_STATE;
		// myAcceptance=selectedValue;
	}

	/**
	 * Returns a PDAConfiguration array that represents the initial
	 * configuration of the PDA, before any input has been processed. It returns
	 * an array of length one.
	 *
	 * @param input
	 *            the input string.
	 */
	@Override
	public List<Configuration> getInitialConfigurations(final String input) {
		/** The stack should contain the bottom of stack marker. */
		final List<Configuration> configs = new ArrayList<>();
		final CharacterStack stack = new CharacterStack();
		stack.push("Z");
		configs.add(new PDAConfiguration(myAutomaton.getInitialState(), null, input, input, stack, myAcceptance));
		return configs;
	}

	/**
	 * Returns true if the simulation of the input string on the automaton left
	 * the machine in a final state. If the entire input string is processed and
	 * the machine is in a final state, return true.
	 *
	 * @return true if the simulation of the input string on the automaton left
	 *         the machine in a final state.
	 */
	@Override
	public boolean isAccepted() {
		final Iterator<Configuration> it = myConfigurations.iterator();
		while (it.hasNext()) {
			final PDAConfiguration configuration = (PDAConfiguration) it.next();
			if (myAcceptance == FINAL_STATE) {
				final State currentState = configuration.getCurrentState();
				if (configuration.getUnprocessedInput() == "" && myAutomaton.isFinalState(currentState)) {
					return true;
				}
			} else if (myAcceptance == EMPTY_STACK) {
				final CharacterStack stack = configuration.getStack();
				if (configuration.getUnprocessedInput() == "" && stack.height() == 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Sets acceptance to accept by empty stack.
	 */
	public void setAcceptByEmptyStack() {
		myAcceptance = EMPTY_STACK;
	}

	/**
	 * Sets acceptance to accept by final state.
	 */
	public void setAcceptByFinalState() {
		myAcceptance = FINAL_STATE;
	}

	/**
	 * Runs the automaton on the input string.
	 *
	 * @param input
	 *            the input string to be run on the automaton
	 * @return true if the automaton accepts the input
	 */
	@Override
	public boolean simulateInput(final String input) {
		/** clear the configurations to begin new simulation. */
		myConfigurations.clear();
		final List<Configuration> initialConfigs = getInitialConfigurations(input);
		for (int k = 0; k < initialConfigs.size(); k++) {
			final PDAConfiguration initialConfiguration = (PDAConfiguration) initialConfigs.get(k);
			myConfigurations.add(initialConfiguration);
		}
		int count = 0;
		while (!myConfigurations.isEmpty()) {
			if (isAccepted()) {
				return true;
			}
			final List<PDAConfiguration> configurationsToAdd = new ArrayList<>();
			final Iterator<Configuration> it = myConfigurations.iterator();
			while (it.hasNext()) {
				final PDAConfiguration configuration = (PDAConfiguration) it.next();
				final List<? extends Configuration> configsToAdd = stepConfiguration(configuration);
				for (final Configuration config : configsToAdd) {
					configurationsToAdd.add((PDAConfiguration) config);
				}
				it.remove();
				count++;
				if (count > 10000) {
					final int result = JOptionPane.showConfirmDialog(null,
							"JFLAP has generated 10000 configurations. Continue?");
					switch (result) {
					case JOptionPane.CANCEL_OPTION:
						continue;
					case JOptionPane.NO_OPTION:
						return false;
					default:
					}
				}
			}
			myConfigurations.addAll(configurationsToAdd);
		}
		return false;
	}

	/**
	 * Simulates one step for a particular configuration, adding all possible
	 * configurations reachable in one step to set of possible configurations.
	 *
	 * @param config
	 *            the configuration to simulate the one step on
	 */
	@Override
	public List<? extends Configuration> stepConfiguration(final Configuration config) {
		final List<Configuration> list = new ArrayList<>();
		final PDAConfiguration configuration = (PDAConfiguration) config;
		/** get all information from configuration. */
		final String unprocessedInput = configuration.getUnprocessedInput();
		final String totalInput = configuration.getInput();
		final State currentState = configuration.getCurrentState();
		final List<Transition> transitions = myAutomaton.getTransitionsFromState(currentState);
		for (int k = 0; k < transitions.size(); k++) {
			final PDATransition transition = (PDATransition) transitions.get(k);
			/** get all information from transition. */
			final String inputToRead = transition.getInputToRead();
			final String stringToPop = transition.getStringToPop();
			final CharacterStack tempStack = configuration.getStack();
			/** copy stack object so as to not alter original. */
			final CharacterStack stack = new CharacterStack(tempStack);
			final String stackContents = stack.pop(stringToPop.length());
			if (unprocessedInput.startsWith(inputToRead) && stringToPop.equals(stackContents)) {
				String input = "";
				if (inputToRead.length() < unprocessedInput.length()) {
					input = unprocessedInput.substring(inputToRead.length());
				}
				final State toState = transition.getToState();
				stack.push(transition.getStringToPush());
				final PDAConfiguration configurationToAdd = new PDAConfiguration(toState, configuration, totalInput,
						input, stack, myAcceptance);
				list.add(configurationToAdd);
			}
		}

		return list;
	}
}
