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

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.automata.State;

/**
 * A <CODE>PSAConfiguration</CODE> object is a <CODE>Configuration</CODE> object
 * with additional fields for the input string and the stack contents. The
 * current state of the automaton, the stack contents, and the unprocessed input
 * are the only necessary data for the simulation of a PDA.
 *
 * @author Ryan Cavalcante
 */
public class PDAConfiguration extends Configuration {

	/** The variable to represent accept by empty stack. */
	protected static final int EMPTY_STACK = 0;

	/** The variable to represent accept by final state. */
	protected static final int FINAL_STATE = 1;

	/** The mode of acceptance (either by final state or empty stack). */
	protected int myAcceptance;

	/** The original input. */
	protected String myInput;

	/** The unprocessed input. */
	protected String myUnprocessedInput;

	/** The stack of the PDA. */
	protected CharacterStack myStack;

	/**
	 * Instantiates a new PDAConfiguration.
	 *
	 * @param state
	 *            the state the automaton is currently in.
	 * @param parent
	 *            the immediate ancestor for this configuration
	 * @param input
	 *            the original input.
	 * @param unprocessed
	 *            the unprocessed input.
	 * @param stack
	 *            the stack contents
	 */
	public PDAConfiguration(final State state, final PDAConfiguration parent, final String input,
			final String unprocessed, final CharacterStack stack, final int acceptance) {
		super(state, parent);
		myInput = input;
		myUnprocessedInput = unprocessed;
		myStack = stack;
		myAcceptance = acceptance;
	}

	/**
	 * Determines whether this configuration equals another configuration. Two
	 * PDA configurations are equal if they have the same stack, and if they
	 * satisfy the <CODE>Configuration.equals()</CODE> method.
	 *
	 * @see edu.duke.cs.jflap.automata.Configuration#equals
	 * @param configuration
	 *            the configuration to check for equality
	 * @return <CODE>true</CODE> if the configuration is equal to this one,
	 *         <CODE>false</CODE> if it is not
	 */
	@Override
	public boolean equals(final Object configuration) {
		if (configuration == this) {
			return true;
		}
		try {
			return super.equals(configuration)
					&& myUnprocessedInput.equals(((PDAConfiguration) configuration).myUnprocessedInput)
					&& myStack.equals(((PDAConfiguration) configuration).myStack);
		} catch (final ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns the original input.
	 *
	 * @return the original input.
	 */
	public String getInput() {
		return myInput;
	}

	/**
	 * Returns the stack.
	 *
	 * @return the stack.
	 */
	public CharacterStack getStack() {
		return myStack;
	}

	/**
	 * Returns the unprocessed input.
	 *
	 * @return the unprocessed input.
	 */
	public String getUnprocessedInput() {
		return myUnprocessedInput;
	}

	/**
	 * Returns a hash code for this configuration.
	 *
	 * @return a hash code for this configuration
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ myStack.hashCode() ^ myUnprocessedInput.hashCode();
	}

	/**
	 * Returns <CODE>true</CODE> if this configuration is an accepting
	 * configuration, which in this case means that there is no more input and
	 * our state is an accept state.
	 *
	 * @return <CODE>true</CODE> if this configuration is accepting,
	 *         <CODE>false</CODE> otherwise
	 */
	@Override
	public boolean isAccept() {
		if (myAcceptance == FINAL_STATE) {
			if (getUnprocessedInput().length() != 0) {
				return false;
			}
			final State s = getCurrentState();
			final Automaton a = s.getAutomaton();
			return a.isFinalState(s);
		} else if (myAcceptance == EMPTY_STACK) {
			final CharacterStack stack = getStack();
			if (getUnprocessedInput() == "" && stack.height() == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Changes the unprocessed input.
	 *
	 * @param input
	 *            the string that will represent the unprocessed input of the
	 *            PDA.
	 */
	public void setUnprocessedInput(final String input) {
		myUnprocessedInput = input;
	}

	/**
	 * Returns a string representation of this object. This is the same as the
	 * string representation for a regular configuration object, with the
	 * additional fields tacked on.
	 *
	 * @see edu.duke.cs.jflap.automata.Configuration#toString
	 * @return a string representation of this object.
	 */
	@Override
	public String toString() {
		return super.toString() + " INPUT: " + getUnprocessedInput() + " STACK: " + myStack.toString();
	}
}
