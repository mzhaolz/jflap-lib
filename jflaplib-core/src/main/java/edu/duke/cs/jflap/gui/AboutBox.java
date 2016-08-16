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

package edu.duke.cs.jflap.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.OverlayLayout;
import javax.swing.border.EmptyBorder;

// import java.applet.Applet;
// import java.applet.AudioClip;

/**
 * The <TT>AboutBox</TT> is the about box for JFLAP.
 *
 * @author Thomas Finley
 */
public class AboutBox extends JWindow {
	/**
	 * This listens for clicks on the box. When it receives them, the box is
	 * dismissed.
	 */
	private class BoxDismisser extends MouseAdapter {
		@Override
		public void mouseClicked(final MouseEvent e) {
			dismissBox();
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1931685156030518714L;

	/** A simple object to get the class off for resource reading. */
	private static Object OBJECT = new Object();

	/** The image to display in the about box. */
	private static Image IMAGE = null;

	/** The version string. */
	public static final String VERSION = "6.4";

	static {
		try {
			IMAGE = Toolkit.getDefaultToolkit().getImage(OBJECT.getClass().getResource("/MEDIA/about.png"));
		} catch (final NullPointerException e) {

		}
	}

	/**
	 * Some simple test code for the about box.
	 */
	public static void main(final String args[]) {
		final AboutBox box = new AboutBox();
		box.displayBox();
	}

	/**
	 * Instantiates a new <TT>AboutBox</TT> with no specified owner.
	 */
	public AboutBox() {
		this((Frame) null);
	}

	/**
	 * Instantiates a new <TT>AboutBox</TT>.
	 *
	 * @param owner
	 *            the owner of this about box
	 */
	public AboutBox(final Frame owner) {
		super(owner);
		getContentPane().setLayout(new OverlayLayout(getContentPane()));
		final JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.setBorder(new EmptyBorder(3, 3, 3, 3));
		final JPanel fullPanel = new JPanel(new BorderLayout());
		fullPanel.setOpaque(false);
		panel.add(fullPanel, BorderLayout.SOUTH);
		getContentPane().add(panel);
		getContentPane().add(new ImageDisplayComponent(IMAGE));
		addMouseListener(new BoxDismisser());
	}

	/**
	 * Dismisses this about box, and stops the clip.
	 */
	public void dismissBox() {
		dispose();
		// CLIP.stop();
	}

	/**
	 * Displays this about box, and plays the clip.
	 */
	public void displayBox() {
		pack();
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension boxSize = getPreferredSize();
		setLocation((screenSize.width - boxSize.width) >> 1, (screenSize.height - boxSize.height) >> 1);
		toFront();
		setVisible(true);
	}
}
