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

package edu.duke.cs.jflap.gui.editor;

import java.awt.Graphics;
import java.awt.Graphics2D;

import edu.duke.cs.jflap.gui.environment.AutomatonEnvironment;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * An <CODE>EditCanvas</CODE> is an extension of <CODE>AutomatonPane</CODE> more
 * suitable for use as a place where automatons may be edited.
 *
 * @author Thomas Finley
 */
public class EditCanvas extends AutomatonPane {
	private static final long serialVersionUID = 8L;

	/** The toolbar that is used for this edit canvas. */
	private ToolBar toolbar;

	/**
	 * Instantiates a new <CODE>EditCanvas</CODE>.
	 *
	 * @param drawer
	 *            the automaton drawer
	 */
	public EditCanvas(final AutomatonDrawer drawer) {
		this(drawer, false);
	}

	/**
	 * Instantiates a new <CODE>EditCanvas</CODE>.
	 *
	 * @param drawer
	 *            the automaton drawer
	 * @param fit
	 *            <CODE>true</CODE> if the automaton should change its size to
	 *            fit in the automaton; this can be very annoying
	 */
	public EditCanvas(final AutomatonDrawer drawer, final boolean fit) {
		super(drawer, fit);
	}

	/**
	 * Paints the component. In addition to what the automaton pane does, this
	 * also calls the current tool's draw method.
	 *
	 * @param g
	 *            the graphics object to draw upon
	 */
	@Override
	public void paintComponent(final Graphics g) {
		if (getCreator().automaton.getEnvironmentFrame() != null) {
			if (!((AutomatonEnvironment) (getCreator().automaton.getEnvironmentFrame().getEnvironment()))
					.shouldPaint()) {
				return;
			}
		}
		// EDebug.print(Thread.currentThread().getName());
		super.paintComponent(g);
		toolbar.drawTool(g);
		final Graphics2D g2 = (Graphics2D) g;
		final double newXScale = 1.0 / transform.getScaleX();
		final double newYScale = 1.0 / transform.getScaleY();
		g2.scale(newXScale, newYScale);
		g2.translate(-transform.getTranslateX(), -transform.getTranslateY());
	}

	/**
	 * Sets the toolbar for this edit canvas.
	 *
	 * @param toolbar
	 *            the toolbar for this edit canvas
	 */
	public void setToolBar(final ToolBar toolbar) {
		this.toolbar = toolbar;
	}
}
