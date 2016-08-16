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

package edu.duke.cs.jflap.automata.turing;

import java.util.List;

import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.automata.State;

/**
 * A <CODE>TMConfiguration</CODE> object is a <CODE>Configuration</CODE> object
 * with additional fields for the input string and the tape contents. The
 * current state of the automaton and the tape contents, are the only necessary
 * data for the simulation of a 1-tape Turing machine.
 *
 * @author Ryan Cavalcante
 */
public class TMConfiguration extends Configuration implements Cloneable {
	/** The tapes. */
	protected List<Tape> myTapes;

	private final List<AcceptanceFilter> myFilters; // constructed outside and
													// passed
	// in
	// in the constructor. //Constructed
	// once and passed to multiple people.

	// MERLIN MERLIN MERLIN MERLIN MERLIN//
	private boolean isHalted = false; // this is a special flag which is checked
	// by the accept by halt. The first time
	// that step-configuration method of
	// TMSimulator cannot go forth, it will
	// set this flag, and return the thing
	// that it was handed. The second time, it
	// will see this flag, and return an empty
	// list to indicate failure, if the
	// configuration was not previously
	// accepted by the filter (that is, if the
	// filter was not activated)

	/**
	 * Instantiates a new TMConfiguration.
	 *
	 * @param state
	 *            the state the automaton is currently in
	 * @param parent
	 *            the immediate ancestor for this configuration
	 * @param tapes
	 *            the read/write tapes
	 */
	public TMConfiguration(final State state, final TMConfiguration parent, final List<Tape> tapes,
			final List<AcceptanceFilter> myFilters2) {
		super(state, parent);
		myTapes = tapes;
		myFilters = myFilters2;
	}

	/*
	 * private boolean isFinalStateInAutomaton(Automaton auto, State state){
	 * State[] finals = auto.getFinalStates(); for(int m = 0; m < finals.length;
	 * m++){ if(finals[m]==state){ return true; } } return false; }
	 */

	@Override
	public Object clone() {
		final TMConfiguration newConfig = new TMConfiguration(getCurrentState(), (TMConfiguration) getParent(), myTapes,
				myFilters);
		newConfig.setFocused(getFocused());
		newConfig.setHalted(isHalted());
		return newConfig;
	}

	/**
	 * Compares two TM configurations for equality. Two configurations are equal
	 * if the tapes are equal, and if they arose from the same configuration and
	 * are at the same state.
	 *
	 * @param configuration
	 *            the configuration to test for equality
	 * @return <CODE>true</CODE> if the configurations are equal,
	 *         <CODE>false</CODE> if they are not
	 */
	@Override
	public boolean equals(final Object configuration) {
		if (configuration == this) {
			return true;
		}
		try {
			if (!super.equals(configuration)) {
				return false;
			}
			final List<Tape> tapes = ((TMConfiguration) configuration).myTapes;
			if (tapes.size() != myTapes.size()) {
				return false;
			}
			for (int i = 0; i < tapes.size(); i++) {
				if (!tapes.get(i).equals(myTapes.get(i))) {
					return false;
				}
			}
			return true;
		} catch (final ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns the tapes.
	 *
	 * @return the tapes
	 */
	public List<Tape> getTapes() {
		return myTapes;
	}

	/**
	 * Returns a hash code for this configuration.
	 *
	 * @return a hash code for this configuration
	 */
	@Override
	public int hashCode() {
		int code = super.hashCode();
		for (int i = 0; i < myTapes.size(); i++) {
			code = code ^ myTapes.get(i).hashCode();
		}
		return code;
	}

	/**
	 * Returns <CODE>true</CODE> if this configuration is an accepting
	 * configuration, based on the chosen criteria. Currently, we look at accept
	 * by halting and accept by final state. //MERLIN MERLIN MERLIN MERLIN
	 * MERLIN//
	 *
	 * @return <CODE>true</CODE> if this configuration is accepting,
	 *         <CODE>false</CODE> otherwise
	 */
	@Override
	public boolean isAccept() {

		for (int i = 0; i < myFilters.size(); i++) {
			if (myFilters.get(i).accept(this)) {
				return true;
			}
		}
		return false;
	}

	public boolean isHalted() {
		return isHalted;
	}

	public void setHalted(final boolean b) {
		isHalted = b;
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
		final StringBuffer sb = new StringBuffer(super.toString());
		for (int i = 0; i < myTapes.size(); i++) {
			sb.append(" TAPE ");
			sb.append(i);
			sb.append(": ");
			sb.append(myTapes.get(i).toString());
		}
		return sb.toString();
	}
}
