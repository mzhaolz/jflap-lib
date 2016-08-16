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

import javax.swing.JComponent;
import javax.swing.JMenu;

import edu.duke.cs.jflap.gui.environment.Environment;

/**
 * The <CODE>SaveGraphPNGAction</CODE> is an action to save the graph in window
 * to a PNG image file always using a dialog box.
 *
 * @author Jonathan Su
 */
public class SaveGraphPNGAction extends RestrictedAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/** The environment that this save action gets it's object from. */
	protected Environment environment;

	protected JMenu myMenu;

	/**
	 * Instantiates a new <CODE>SaveGraphPNGAction</CODE>.
	 *
	 * @param environment
	 *            the environment that holds the action
	 */
	public SaveGraphPNGAction(final Environment environment, final JMenu menu) {
		super("Save Graph as PNG", null);
		this.environment = environment;
		myMenu = menu;
	}

	/**
	 * Displays JFileChooser for location to save the graph canvas as png image.
	 *
	 * @param arg0
	 *            the action event
	 */
	@Override
	public void actionPerformed(final ActionEvent arg0) {

		final Component apane = environment.tabbed.getSelectedComponent();
		final JComponent c = (JComponent) environment.getActive();
		SaveGraphUtility.saveGraph(apane, c, "PNG files", "png");
	}
}
