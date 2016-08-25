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

package edu.duke.cs.jflap.gui.minimize;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.tree.TreeNode;

import com.google.common.base.Joiner;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.fsa.MinimizeTreeNode;
import edu.duke.cs.jflap.gui.tree.DefaultNodeDrawer;

/**
 * The <CODE>MinimizeNodeDrawer</CODE> is used to draw
 * <CODE>MinimizeTreeNode</CODE> objects.
 *
 * @see edu.duke.cs.jflap.automata.fsa.MinimizeTreeNode
 *
 * @author Thomas Finley
 */
public class MinimizeNodeDrawer extends DefaultNodeDrawer {
	/**
	 * Returns the state list of the node as a string.
	 *
	 * @param node
	 *            the node to get the state list for
	 * @return a string of the states IDs of the node
	 */
	private static String getStateString(final MinimizeTreeNode node) {
		final State[] states = (State[]) node.getUserObject();
		final Integer[] ids = new Integer[states.length];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = states[i].getID();
		}
		Arrays.sort(ids);
		final StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < ids.length; i++) {
			if (i != 0) {
				buffer.append(", ");
			}
			buffer.append(ids[i]);
		}
		return Joiner.on(", ").join(ids);
	}

	/** The map of nodes to labels. */
	private final HashMap<TreeNode, String> labels = new HashMap<>();

	/**
	 * This draws a node. The fill color is the color of the graphics object
	 * before this method was called.
	 *
	 * @param g
	 *            the graphics object to draw the node on
	 * @param node
	 *            the node to draw
	 */
	@Override
	public void draw(final Graphics2D g, final TreeNode node) {
		g.fill(NODE_SHAPE);
		final Color c = g.getColor();
		g.setColor(Color.black);
		g.draw(NODE_SHAPE);

		final MinimizeTreeNode m = (MinimizeTreeNode) node;
		g.setColor(c);
		drawBox(g, getStateString(m), new Point2D.Float(0.0f, 0.0f));
		g.setColor(c);
		drawBox(g, m.getTerminal(), new Point2D.Float(0.0f, 20.0f));
		g.setColor(c);
		final String label = getLabel(node);
		if (label == null) {
			return;
		}
		drawBox(g, label, new Point2D.Float(0.0f, -20.0f));
		g.setColor(c);
	}

	/**
	 * Draws a string in a box centered at the given coordinates.
	 *
	 * @param g
	 *            the graphics object to draw upon
	 * @param string
	 *            the string to draw
	 * @param point
	 *            the
	 */
	private void drawBox(final Graphics2D g, final String string, final Point2D point) {
		if (string.equals("")) {
			return;
		}

		final Rectangle2D rect = g.getFontMetrics().getStringBounds(string, g);
		final double x = point.getX() - rect.getWidth() / 2.0;
		final double y = point.getY() - rect.getY() - rect.getHeight() / 2.0;

		g.fillRect((int) x - 2, (int) (y + rect.getY()) - 2, (int) rect.getWidth() + 4, (int) rect.getHeight() + 4);
		g.setColor(Color.black);
		g.drawRect((int) x - 2, (int) (y + rect.getY()) - 2, (int) rect.getWidth() + 4, (int) rect.getHeight() + 4);
		g.drawString(string, (float) x, (float) y);
		// g.drawRect();
	}

	/**
	 * Returns the label for a particular node.
	 *
	 * @param node
	 *            the node to get the label for
	 * @return the label for a particular node, or <CODE>null</CODE> if there is
	 *         no label for this node
	 */
	public String getLabel(final TreeNode node) {
		return labels.get(node);
	}

	/**
	 * Sets a label to appear above a node for a particular node.
	 *
	 * @param node
	 *            the node to set the label for
	 * @param label
	 *            the label to set for the node, or <CODE>null</CODE> if there
	 *            should be no label for this node
	 */
	public void setLabel(final TreeNode node, final String label) {
		if (label == null) {
			labels.remove(node);
		} else {
			labels.put(node, label);
		}
	}
}
