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
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.KeyStroke;

import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * This action handles quitting.
 *
 * @author Thomas Finley
 */
public class QuitAction extends RestrictedAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This begins the process of quitting. If this method returns, you know it
	 * did not succeed. Quitting may not succeed if there is an unsaved document
	 * and the user elects to cancel the process.
	 */
	public static void beginQuit() {
		final List<EnvironmentFrame> frames = Universe.frames();
		for (int i = 0; i < frames.size(); i++) {
			if (!frames.get(i).close()) {
				return;
			}
		}

		// modified by Moti Ben-Ari
		if (edu.duke.cs.jflap.gui.Main.getDontQuit()) {
			NewAction.closeNew();
		} else {
			System.exit(0);
		}
	}

	/**
	 * Instantiates a new <CODE>QuitAction</CODE>.
	 */
	public QuitAction() {
		super("Quit", null);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, MAIN_MENU_MASK));
	}

	/**
	 * In repsonding to events, it cycles through all registered windows in the
	 * <CODE>gui.environment.Universe</CODE> and closes them all, or at least
	 * until the user does something that stops a close, at which point the quit
	 * terminates.
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		beginQuit();
	}
}
