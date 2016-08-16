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

package edu.duke.cs.jflap.gui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.UnrestrictedGrammar;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;

/**
 * This action creates a new brute force parser for the grammar.
 *
 * @author Thomas Finley
 */
public class MultipleBruteParseAction extends GrammarAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The grammar environment. */
	private final GrammarEnvironment environment;

	/**
	 * Instantiates a new <CODE>BruteParseAction</CODE>.
	 *
	 * @param environment
	 *            the grammar environment
	 */
	public MultipleBruteParseAction(final GrammarEnvironment environment) {
		super("Multiple Brute Force Parse", null);
		this.environment = environment;
	}

	/**
	 * Performs the action.
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		performAction((Component) e.getSource());
	}

	public void performAction(final Component source) {
		final Grammar g = environment.getGrammar(UnrestrictedGrammar.class);
		if (g == null) {
			return;
		}
		final MultipleSimulateAction mult = new MultipleSimulateAction(g, environment);
		mult.performAction(source);
	}
}
