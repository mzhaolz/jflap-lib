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

package edu.duke.cs.jflap.automata.fsa;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.automata.State;

/**
 * An <CODE>FSAConfiguration</CODE> object is a <CODE>Configuration</CODE>
 * object with an additional field for the input string. The current state of
 * the automaton and the unprocessed input are the only necessary data for the
 * simulation of an FSA.
 *
 * @author Ryan Cavalcante
 */
public class FSAConfiguration extends Configuration {
	/** The total input. */
	private final String myInput;

	/** The unprocessed input. */
	private String myUnprocessedInput;

	/**
	 * Instantiates a new FSAConfiguration.
	 *
	 * @param state
	 *            the state the automaton is currently in.
	 * @param parent
	 *            the configuration that is the immediate ancestor of this
	 *            configuration
	 * @param input
	 *            the input
	 * @param unprocessed
	 *            the unprocessed input
	 */
	public FSAConfiguration(final State state, final FSAConfiguration parent, final String input,
			final String unprocessed) {
		super(state, parent);
		myInput = input;
		myUnprocessedInput = unprocessed;
	}

	/**
	 * Checks for equality. Two FSAConfigurations are equal if they have the
	 * same unprocessed input, and satisfy the .equals() test of the base
	 * <CODE>Configuration</CODE> class.
	 *
	 * @see edu.duke.cs.jflap.automata.Configuration#equals
	 * @param configuration
	 *            the configuration to check against this one for equality
	 * @return <CODE>true</CODE> if the two configurations are equal,
	 *         <CODE>false</CODE> otherwise
	 */
	@Override
	public boolean equals(final Object configuration) {
		if (configuration == this) {
			return true;
		}
		try {
			return super.equals(configuration)
					&& myUnprocessedInput.equals(((FSAConfiguration) configuration).myUnprocessedInput);
		} catch (final ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns the total input.
	 */
	public String getInput() {
		return myInput;
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
	 * Returns a hashcode for this object.
	 *
	 * @return a hashcode for this object
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ myUnprocessedInput.hashCode();
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
		if (getUnprocessedInput().length() != 0) {
			return false;
		}
		final State s = getCurrentState();
		final Automaton a = s.getAutomaton();
		return a.isFinalState(s);
	}

	/**
	 * Changes the unprocessed input.
	 *
	 * @param input
	 *            the string that will represent the unprocessed input of the
	 *            FSA.
	 */
	public void setUnprocessedInput(final String input) {
		myUnprocessedInput = input;
	}

	/**
	 * Returns a string representation of this object.
	 *
	 * @return a string representation of this object.
	 */
	@Override
	public String toString() {
		return super.toString() + ": " + getUnprocessedInput();
	}
}
