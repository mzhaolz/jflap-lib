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

import java.awt.event.ActionEvent;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.UnrestrictedGrammar;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;
import edu.duke.cs.jflap.gui.grammar.parse.BruteParsePane;

/**
 * This action creates a new brute force parser for the grammar.
 *
 * @author Thomas Finley
 */
public class BruteParseAction extends GrammarAction {
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
	public BruteParseAction(final GrammarEnvironment environment) {
		super("Brute Force Parse", null);
		this.environment = environment;
	}

	/**
	 * Performs the action.
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final Grammar g = environment.getGrammar(UnrestrictedGrammar.class);
		if (g == null) {
			return;
		}
		final BruteParsePane bpp = new BruteParsePane(environment, g, null);
		environment.add(bpp, "Brute Parser", new CriticalTag() {
		});
		environment.setActive(bpp);
	}
}
