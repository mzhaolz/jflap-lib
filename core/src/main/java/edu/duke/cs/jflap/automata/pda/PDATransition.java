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

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * A <CODE>PDATransition</CODE> is a <CODE>Transition</CODE> object with
 * additional fields for the label (input to read), the string to pop off the
 * stack, and the string to push on the stack.
 *
 * @see edu.duke.cs.jflap.automata.pda.PushdownAutomaton
 *
 * @author Ryan Cavalcante
 */
public class PDATransition extends Transition {
	/**
	 *
	 */
	private static final long serialVersionUID = 3490505062662556709L;

	/** The input to read portion of the transition label. */
	protected String myInputToRead;

	/** The string to pop off the stack. */
	protected String myStringToPop;

	/** The string to push on the stack. */
	protected String myStringToPush;

	/**
	 * Instantiates a new <CODE>PDATransition</CODE> object.
	 *
	 * @param from
	 *            the state this transition comes from
	 * @param to
	 *            the state this transition goes to
	 * @param inputToRead
	 *            the string that the machine should satisfy before moving on to
	 *            the next state.
	 * @param stringToPop
	 *            the string that the machine should pop from the stack.
	 * @param stringToPush
	 *            the string that the machine should push on to the stack.
	 */
	public PDATransition(final State from, final State to, final String inputToRead, final String stringToPop,
			final String stringToPush) {
		super(from, to);
		setInputToRead(inputToRead);
		setStringToPop(stringToPop);
		setStringToPush(stringToPush);
	}

	/**
	 * Returns a copy of this transition with new from and to states.
	 *
	 * @param from
	 *            the new from state for the returned transition
	 * @param to
	 *            the new to state for the returned transition
	 * @return a copy of this trnasition with the new from and to states
	 */
	@Override
	public Transition copy(final State from, final State to) {
		return new PDATransition(from, to, getInputToRead(), getStringToPop(), getStringToPush());
	}

	/**
	 * Tests this transition against another object for equality.
	 *
	 * @param object
	 *            the object to test for equality
	 * @return <CODE>true</CODE> if this transition equals the passed in object,
	 *         <CODE>false</CODE> otherwise
	 */
	@Override
	public boolean equals(final Object object) {
		try {
			final PDATransition t = (PDATransition) object;
			return super.equals(object) && myInputToRead.equals(t.myInputToRead)
					&& myStringToPop.equals(t.myStringToPop) && myStringToPush.equals(t.myStringToPush);
		} catch (final ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns the description for this transition.
	 *
	 * @return the description, in this case, the input to read, the string to
	 *         pop off the stack, and the string to push on the stack.
	 */
	@Override
	public String getDescription() {
		String input = getInputToRead();
		if (input.length() == 0) {
			input = Universe.curProfile.getEmptyString();
		}
		String toPop = getStringToPop();
		if (toPop.length() == 0) {
			toPop = Universe.curProfile.getEmptyString();
		}
		String toPush = getStringToPush();
		if (toPush.length() == 0) {
			toPush = Universe.curProfile.getEmptyString();
		}
		return input + " , " + toPop + " ; " + toPush;
	}

	/**
	 * Returns the input to read portion of the transition label for this
	 * transition.
	 */
	public String getInputToRead() {
		return myInputToRead;
	}

	/**
	 * Returns the string to pop from stack portion of the transition label for
	 * this transition.
	 */
	public String getStringToPop() {
		return myStringToPop;
	}

	/**
	 * Returns the string to push on to the stack portion of the transition
	 * label for this transition.
	 */
	public String getStringToPush() {
		return myStringToPush;
	}

	/**
	 * Returns the hashcode for this transition.
	 *
	 * @return the hashcode for this transition
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ myInputToRead.hashCode() ^ myStringToPop.hashCode() ^ myStringToPush.hashCode();
	}

	/**
	 * Sets the input to read portion of the transition label for this
	 * transition.
	 *
	 * @param inputToRead
	 *            the input to read portion of the transition label.
	 */
	protected void setInputToRead(final String inputToRead) {
		/*
		 * if (!automata.StringChecker.isAlphanumeric(inputToRead)) throw new
		 * IllegalArgumentException("Label must be alphanumeric!");
		 */
		myInputToRead = inputToRead;
	}

	/**
	 * Sets the string to pop from stack portion of the transition label for
	 * this transition.
	 *
	 * @param stringToPop
	 *            the string to pop from the stack.
	 */
	protected void setStringToPop(final String stringToPop) {
		/*
		 * if (!automata.StringChecker.isAlphanumeric(stringToPop)) throw new
		 * IllegalArgumentException("Pop string must "+ "be alphanumeric!");
		 */
		final PushdownAutomaton myPDA = (PushdownAutomaton) getAutomaton();
		if (myPDA.singleInputPDA && stringToPop.length() > 1) {
			throw new IllegalArgumentException("Pop string must have no more than one character!");
		}
		myStringToPop = stringToPop;
	}

	/**
	 * Sets the string to push on to the stack portion of the transition label
	 * for this transition.
	 *
	 * @param stringToPush
	 *            the string to push on to the stack.
	 */
	protected void setStringToPush(final String stringToPush) {
		/*
		 * if (!automata.StringChecker.isAlphanumeric(stringToPush)) throw new
		 * IllegalArgumentException("Push string must "+ "be alphanumeric!");
		 */
		final PushdownAutomaton myPDA = (PushdownAutomaton) getAutomaton();
		if (myPDA.singleInputPDA && stringToPush.length() > 1) {
			throw new IllegalArgumentException("Push string must have no more than one character!");
		}
		myStringToPush = stringToPush;
	}

	/**
	 * Returns a string representation of this object. This is the same as the
	 * string representation for a regular transition object, with the
	 * additional fields tacked on.
	 *
	 * @see edu.duke.cs.jflap.automata.Transition#toString
	 * @return a string representation of this object
	 */
	@Override
	public String toString() {
		return super.toString() + ": \"" + getInputToRead() + "\"" + ": \"" + getStringToPop() + "\"" + ": \""
				+ getStringToPush() + "\"";
	}
}
