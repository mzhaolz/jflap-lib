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
import edu.duke.cs.jflap.gui.grammar.parse.UserControlParsePane;

/**
 * Action for User Controlling Parsing
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class UserControlParseAction extends GrammarAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static boolean isApplicable(final Object object) {
		return object instanceof Grammar;
	}

	/** The grammar environment. */
	private final GrammarEnvironment environment;

	/**
	 * Instantiates a new <CODE>BruteParseAction</CODE>.
	 *
	 * @param environment
	 *            the grammar environment
	 */
	public UserControlParseAction(final GrammarEnvironment environment) {
		super("User Control Parse", null);
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
		final UserControlParsePane userPane = new UserControlParsePane(environment, g);
		environment.add(userPane, "User Control Parser", new CriticalTag() {
		});
		environment.setActive(userPane);
	}
}
