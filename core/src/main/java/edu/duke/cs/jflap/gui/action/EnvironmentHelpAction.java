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

import edu.duke.cs.jflap.gui.environment.Environment;

/**
 * The <CODE>EnvironmentHelpAction</CODE> is an extension of the
 * <CODE>HelpAction</CODE> that, whenever an action is received, determines what
 * should be displayed based on the currently active pane in the environment.
 * Basically, it simply calls <CODE>HelpAction.displayHelp</CODE> on
 * <CODE>Environment.getActive</CODE>.
 *
 * Any components in an environment that wish to have help should register
 * themselves, or preferably their <CODE>Class</CODE> objects (so that it only
 * happens once), with whatever particular webpage they wish to display whenever
 * help is activated.
 *
 * @author Thomas Finley
 */
public class EnvironmentHelpAction extends HelpAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The environment this help action is for. */
	private final Environment environment;

	/**
	 * Instantiates an <CODE>EnvironmentHelpAction</CODE>.
	 *
	 * @param environment
	 *            the environment that this help action will get the current
	 *            panel from
	 */
	public EnvironmentHelpAction(final Environment environment) {
		this.environment = environment;
	}

	/**
	 * Displays help according to the current display of the automaton.
	 *
	 * @param event
	 *            the action event
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		displayHelp(environment.getActive());
	}
}
