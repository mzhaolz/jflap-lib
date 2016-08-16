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

import edu.duke.cs.jflap.gui.environment.Universe;

public class OpenURLAction extends RestrictedAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public OpenURLAction() {
		super("Open URL", null);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
	}

	@Override
	public boolean isEnabled() {
		if (Universe.CHOOSER == null) {
			return true;
		}
		return false;
	}
}
