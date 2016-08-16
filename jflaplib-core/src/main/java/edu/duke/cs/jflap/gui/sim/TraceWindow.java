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

package edu.duke.cs.jflap.gui.sim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import edu.duke.cs.jflap.automata.Configuration;

/**
 * A class that epitomizes the ultimate in bad design: a fusion of model, view,
 * and controller. Look upon my works, ye mighty, and despair.
 *
 * @author Thomas Finley
 */
public class TraceWindow extends JFrame {
	/** The pane that displays the past configurations. */
	public static class PastPane extends JComponent {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		private static final int ARROW_LENGTH = 20;

		private List<Icon> icons;

		public PastPane(final Configuration last) {
			setConfiguration(last);
		}

		public final void drawArrow(final Graphics g) {
			final int center = getWidth() >> 1;
			g.setColor(Color.black);
			g.drawLine(center, 0, center, ARROW_LENGTH);
			g.drawLine(center, ARROW_LENGTH, center - 10, ARROW_LENGTH - 10);
			g.drawLine(center, ARROW_LENGTH, center + 10, ARROW_LENGTH - 10);
			g.translate(0, ARROW_LENGTH);
		}

		public final void drawIcon(final Graphics g, final Icon icon) {
			icon.paintIcon(this, g, (getWidth() - icon.getIconWidth()) >> 1, 0);
			g.translate(0, icon.getIconHeight());
		}

		@Override
		public void paintComponent(Graphics g) {
			final Rectangle visible = getVisibleRect();
			final int height = ARROW_LENGTH + icons.get(0).getIconHeight();
			final int max = icons.size() - 1 - visible.y / height;
			int min = icons.size() - 1 - (visible.y + visible.height) / height;
			try {
				min = Math.max(min, 0);
				g = g.create();
				g.translate(0, height * (icons.size() - 1 - max));
				for (int i = max; i >= min; i--) {
					drawArrow(g);
					drawIcon(g, icons.get(i));
				}
				g.dispose();
			} catch (final Throwable e) {
				System.err.println(e);
			}
		}

		public void setConfiguration(Configuration last) {
			final java.util.List<Icon> list = new LinkedList<>();
			int height = 0;
			int width = 0;
			while (last != null) {
				final Icon icon = ConfigurationIconFactory.iconForConfiguration(last);
				width = Math.max(width, icon.getIconWidth());
				height += icon.getIconHeight() + ARROW_LENGTH;
				list.add(icon);
				last = last.getParent();
			}
			icons = list;
			setPreferredSize(new Dimension(width, height));
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The maximum height these trace displays should get to. */
	private static final int MAXHEIGHT = 400;

	/**
	 * Returns a component that displays the ancestry of a configuration.
	 *
	 * @param configuration
	 *            the configuration whose ancestry we want to display
	 * @return a component with the ancestry of the configuration contained
	 *         within a scroll pane
	 */
	public static JScrollPane getPastPane(final Configuration configuration) {
		final JScrollPane sp = new JScrollPane(new PastPane(configuration),
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.validate();
		if (sp.getSize().height > MAXHEIGHT) {
			sp.setSize(sp.getSize().width, MAXHEIGHT);
		}
		return sp;
	}

	/**
	 * Returns a component that displays the ancestry of a configuration.
	 *
	 * @param configuration
	 *            the configuration whose ancestry we want to display
	 * @return a component with the ancestry of the configuration, not contained
	 *         within any scroll pane
	 */
	public static PastPane getPastPaneNoScroll(final Configuration configuration) {
		return new PastPane(configuration);
	}

	/**
	 * Instantiates a new step window with the given configuration.
	 *
	 * @param last
	 *            the last configuration that we are tracing; we display it
	 *            along with all its ancestors
	 */
	public TraceWindow(final Configuration last) {
		super("Traceback");
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getPastPane(last));
		pack();
		if (getSize().height > MAXHEIGHT) {
			setSize(getSize().width, MAXHEIGHT);
		}
		setVisible(true);
	}
}
