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

/**
 * This subclass of <CODE>Automaton</CODE> is specifically for a definition of a
 * Pushdown Automaton.
 *
 * @author Ryan Cavalcante
 */
public class PushdownAutomaton extends Automaton {
	/**
	 *
	 */
	private static final long serialVersionUID = -4694673281116724215L;
	public boolean singleInputPDA = false;

	public PushdownAutomaton() {
		super();
	}

	/**
	 * Creates a pushdown automaton with no states and no transitions.
	 */
	public PushdownAutomaton(final boolean singleinput) {
		super();
		singleInputPDA = singleinput;
	}

	/**
	 * Returns the class of <CODE>Transition</CODE> this automaton must accept.
	 *
	 * @return the <CODE>Class</CODE> object for
	 *         <CODE>automata.pda.PDATransition</CODE>
	 */
	@Override
	protected Class<PDATransition> getTransitionClass() {
		return edu.duke.cs.jflap.automata.pda.PDATransition.class;
	}
}
