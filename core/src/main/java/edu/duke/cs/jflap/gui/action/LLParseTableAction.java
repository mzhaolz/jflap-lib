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

import javax.swing.JOptionPane;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.parse.Operations;
import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;
import edu.duke.cs.jflap.gui.grammar.parse.LLParseTableDerivationPane;

/**
 * This is an action to build an LL(1) parse table for a grammar.
 *
 * @author Thomas Finley
 */
public class LLParseTableAction extends GrammarAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The grammar environment. */
	private final GrammarEnvironment environment;

	/** The frame for the grammar environment. */
	private final EnvironmentFrame frame;

	/**
	 * Instantiates a new <CODE>GrammarOutputAction</CODE>.
	 *
	 * @param environment
	 *            the grammar environment
	 */
	public LLParseTableAction(final GrammarEnvironment environment) {
		super("Build LL(1) Parse Table", null);
		this.environment = environment;
		frame = Universe.frameForEnvironment(environment);
	}

	/**
	 * Performs the action.
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final Grammar g = environment.getGrammar();
		if (g == null) {
			return;
		}
		if (!Operations.isLL1(g)) {
			if (JOptionPane.showConfirmDialog(frame, "The grammar is not LL(1).\nContinue anyway?", "Grammar not LL(1)",
					JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				return;
			}
		}

		final LLParseTableDerivationPane ptdp = new LLParseTableDerivationPane(environment);
		environment.add(ptdp, "Build LL(1) Parse", new CriticalTag() {
		});
		environment.setActive(ptdp);
	}
}
