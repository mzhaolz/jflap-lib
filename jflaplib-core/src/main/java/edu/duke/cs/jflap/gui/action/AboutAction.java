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

import edu.duke.cs.jflap.gui.AboutBox;

/**
 * This action will display a small about box that lists the tool version
 * number, and other version.
 *
 * @author Thomas Finley
 */
public class AboutAction extends RestrictedAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final AboutBox BOX = new AboutBox();

	/**
	 * Instantiates a new <CODE>AboutAction</CODE>.
	 */
	public AboutAction() {
		super("About...", null);
	}

	/**
	 * Shows the about box.
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		BOX.displayBox();
	}
}
