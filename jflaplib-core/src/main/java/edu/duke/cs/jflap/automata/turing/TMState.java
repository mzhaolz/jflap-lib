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

import java.awt.Point;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;

/**
 * This class represents the TuringMachine-specific aspects of states, such as
 * the ability to hold inner machines.
 *
 *
 * @author Henry Qin
 */
public class TMState extends State {
	/**
	 *
	 */
	private static final long serialVersionUID = 2825886205807641375L;
	private TuringMachine myInnerTuringMachine;

	private String myInternalName = null;

	public TMState(final int id, final Point point, final Automaton tm) { // do
																			// we
																			// really
																			// need
																			// a
		// pointer to the
		// parent?
		super(id, point, tm);
		checkArgument(tm instanceof TuringMachine);
		myInnerTuringMachine = new TuringMachine();
		myInnerTuringMachine.setParent(this);
	}

	public TMState(final TMState copyMe) { // do we really need a pointer to the
		// parent?
		this(copyMe.getID(), (Point) copyMe.getPoint().clone(), copyMe.getAutomaton());

		myInnerTuringMachine = copyMe.getInnerTM().clone(); // this
		// should
		// result
		// in
		// recursion
		// until
		// we
		// reach
		// a
		// TMState
		// whose
		// inner
		// TM
		// does
		// not
		// contain
		// states.
	}

	public TuringMachine getInnerTM() {
		return myInnerTuringMachine;
	}

	public String getInternalName() { // just for trying to preserve old way of
		// saving.
		// ASSUME that ID's are Independent
		return myInternalName == null ? myInternalName = "Machine" + getID() : myInternalName; // create
		// an
		// internal
		// name
		// if
		// one
		// has
		// not
		// been
		// assigned
		// explicitly
	}

	public void setInnerTM(final TuringMachine tm) {
		myInnerTuringMachine = tm;
		myInnerTuringMachine.setParent(this);
		checkArgument(myInnerTuringMachine.getParent() == this);
	}

	public void setInternalName(final String s) { // just for trying to preserve
													// old
		// way of saving.
		myInternalName = s;
	}
}
