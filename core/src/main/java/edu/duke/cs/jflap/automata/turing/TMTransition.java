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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;

/**
 * The <CODE>TMTransition</CODE> is a transition for a Turing machine.
 *
 * @see edu.duke.cs.jflap.automata.turing.TuringMachine
 *
 * @author Thomas Finley
 */
public class TMTransition extends Transition {
	private static final long serialVersionUID = 1000L;

	/** The blank symbol. */
	public static final String BLANK = "" + Tape.BLANK;

	// added for turing to grammar conversion
	private int tapes;

	private boolean blockTransition = false;

	/** The read symbols. */
	private final List<String> toRead;

	/** The write symbols. */
	private final List<String> toWrite;

	/** The direction fields. */
	private final List<String> direction;

	/**
	 * Produces a Turing transition. The number of tapes for this Turing machine
	 * transition is inferred from the number of elements in the arrays
	 *
	 * @param from
	 *            the state this transition comes from
	 * @param to
	 *            the state this transition goes to
	 * @param toReadArray
	 *            the strings that the machine should satisfy in each tape
	 *            before moving on to the next state
	 * @param toWriteArray
	 *            the strings that the machine should write on each tape
	 * @param directionArray
	 *            the direction to move the read/write tape head on each tape
	 * @throws IllegalArgumentException
	 *             if the number of elements in each array is not the same, or
	 *             the arrays are empty
	 */
	public TMTransition(final State from, final State to, final List<String> toReadArray,
			final List<String> toWriteArray, final List<String> directionArray) {
		super(from, to);
		checkArgument(toReadArray.size() != toWriteArray.size() || directionArray.size() != toReadArray.size(),
				"Read symbols, write symbols, and directions must have equal numbers of elements!");
		checkArgument(toReadArray.isEmpty(), "Attempted to create a transition with 0 tapes!");
		toRead = new ArrayList<>();
		toWrite = new ArrayList<>();
		direction = new ArrayList<>();
		for (int i = 0; i < tapes; i++) {
			toRead.add("");
			toWrite.add("");
			direction.add("");
			setRead(toReadArray.get(i), i);
			setWrite(toWriteArray.get(i), i);
			setDirection(directionArray.get(i), i);
		}
	}

	/**
	 * Produces a one tape Turing transition.
	 *
	 * @param from
	 *            the state this transition comes from
	 * @param to
	 *            the state this transition goes to
	 * @param ntoRead
	 *            the string that the machine should satisfy before moving on to
	 *            the next state
	 * @param ntoWrite
	 *            the string that the machine should write on to the tape
	 * @param ndirection
	 *            the direction to move the read/write tape head
	 */
	public TMTransition(final State from, final State to, final String ntoRead, final String ntoWrite,
			final String ndirection) {
		this(from, to, Lists.newArrayList(ntoRead), Lists.newArrayList(ntoWrite), Lists.newArrayList(ndirection));
	}

	/**
	 * Produces a copy of this transition with new from and to states.
	 *
	 * @param from
	 *            the new from state
	 * @param to
	 *            the new to state
	 * @return a copy of this transition with the new states
	 */
	@Override
	public Transition copy(final State from, final State to) {
		return new TMTransition(from, to, toRead, toWrite, direction);
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
			final TMTransition t = (TMTransition) object;
			return super.equals(object) && toRead.equals(t.toRead) && toWrite.equals(t.toWrite)
					&& direction.equals(t.direction);
		} catch (final ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns the description for this transition.
	 *
	 * @return the description, in this case, the input to read from the tape,
	 *         the string to write to the tape, and the direction to move the
	 *         read/write tape head for each tape
	 */
	@Override
	public String getDescription() {
		final StringBuffer sb = new StringBuffer();
		final int t = tapes();
		for (int i = 0; i < t; i++) {
			if (i != 0) {
				sb.append(" | ");
			}
			sb.append(toRead.get(i));
			if (!blockTransition) {
				sb.append(" ; ");
				sb.append(toWrite.get(i));
				sb.append(" , ");
				sb.append(direction.get(i));
			}
		}
		return sb.toString();
	}

	/**
	 * Returns the direction to move the tape head for one of the tapes.
	 *
	 * @param tape
	 *            the tape index which this direction will affect
	 * @return the transition's direction for this tape
	 */
	public String getDirection(final int tape) {
		return direction.get(tape);
	}

	/**
	 * Returns the input to read for the given tape.
	 *
	 * @param tape
	 *            the tape index to retrieve
	 */
	public String getRead(final int tape) {
		return toRead.get(tape);
	}

	/**
	 * Gets the number of tapes in the Turing Machine.
	 *
	 * @return tapes number of tapes in machine.
	 */
	public int getTapeLength() {
		return tapes;
	}

	/**
	 * Returns the string to write to tape portion of the transition label for
	 * this transition.
	 *
	 * @param tape
	 *            the tape to return the
	 */
	public String getWrite(final int tape) {
		return toWrite.get(tape);
	}

	/**
	 * Returns the hashcode for this transition.
	 *
	 * @return the hashcode for this transition
	 */
	@Override
	public int hashCode() {
		final int code = super.hashCode() ^ toRead.hashCode() ^ toWrite.hashCode() ^ direction.hashCode();
		return code;
	}

	/**
	 * Is the transition a block transition?
	 *
	 * @return boolean whether the Transition is a Block Transition.
	 */
	public boolean isBlockTransition() {
		return blockTransition;
	}

	public void setBlockTransition(final boolean block) {
		blockTransition = block;
	}

	/**
	 * Sets the direction to move the read/write tape head.
	 *
	 * @param newDirection
	 *            the new direction to move the tape head
	 * @param tape
	 *            the tape index to change the direction for, indexed 0 through
	 *            one less than the number of tapes
	 */
	protected void setDirection(final String newDirection, final int tape) {
		if (!(newDirection.equals("L") || newDirection.equals("R") || newDirection.equals("S"))) {
			throw new IllegalArgumentException("Direction must be L, R, or S!");
		}
		direction.set(tape, newDirection);
	}

	public void setRead(final int tape, final String symbol) {
		toRead.set(tape, symbol);
	}

	/**
	 * Sets the input to read for a given tape.
	 *
	 * @param stringToRead
	 *            the input to read for a given tape
	 * @param tape
	 *            the tape index
	 */
	protected void setRead(String stringToRead, final int tape) {
		if (stringToRead.length() == 0) {
			stringToRead = BLANK;
		}
		if (stringToRead.equals("!")) {
			stringToRead = "!" + BLANK;
		}
		if (stringToRead.length() != 1
				&& !(stringToRead.startsWith("!") || stringToRead.equals("~") || stringToRead.indexOf("}") != -1)) {
			throw new IllegalArgumentException("Read string must have exactly one character!");
		}
		if (stringToRead.indexOf("}") != -1 && stringToRead.indexOf("!") != -1) {
			throw new IllegalArgumentException(
					"Read string cannot cannot mix variable assignment in the NOT (!) operator.");
		}
		toRead.set(tape, stringToRead);
	}

	public void setWrite(final int tape, final String symbol) {
		toWrite.set(tape, symbol);
	}

	/**
	 * Sets the string to write to tape for the given tape.
	 *
	 * @param stringToWrite
	 *            the string to write to tape
	 * @param tape
	 *            which tape to write
	 */
	protected void setWrite(String stringToWrite, final int tape) {
		if (stringToWrite.length() == 0) {
			stringToWrite = BLANK;
		}
		if (stringToWrite.length() != 1) {
			throw new IllegalArgumentException("Write string must have exactly one character!");
		}
		toWrite.set(tape, stringToWrite);
	}

	/**
	 * Returns the number of tapes this Turing machine transition acts on.
	 *
	 * @return the number of tapes
	 */
	public int tapes() {
		return toRead.size();
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
		return super.toString() + ": \"" + getDescription() + "\"";
	}
}
