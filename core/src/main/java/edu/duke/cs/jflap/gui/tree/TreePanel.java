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

package edu.duke.cs.jflap.gui.tree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * The <CODE>TreePanel</CODE> is a graphical component that draws a tree using a
 * <CODE>TreeDrawer</CODE> object.
 *
 * @see javax.swing.tree.TreeModel
 *
 * @author Thomas Finley
 */
public class TreePanel extends JComponent {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The tree drawing object. */
	private TreeDrawer treeDrawer;

	/**
	 * Instantiates a <CODE>TreePanel</CODE> to draws a tree with a given
	 * <CODE>TreeDrawer</CODE>.
	 *
	 * @param drawer
	 *            the tree drawer to draw a tree with
	 */
	public TreePanel(final TreeDrawer drawer) {
		treeDrawer = drawer;
	}

	/**
	 * Instantiates a <CODE>TreePanel</CODE> to draws the specified tree with a
	 * default <CODE>TreeDrawer</CODE> object.
	 *
	 * @param tree
	 *            the tree to draw
	 */
	public TreePanel(final TreeModel tree) {
		treeDrawer = new DefaultTreeDrawer(tree);
	}

	/**
	 * Returns the <CODE>TreeDrawer</CODE> for this treepanel.
	 *
	 * @return the <CODE>TreeDrawer</CODE> for this treepanel
	 */
	public TreeDrawer getTreeDrawer() {
		return treeDrawer;
	}

	/**
	 * Returns the node at a particular point.
	 *
	 * @param point
	 *            the point to check for nodeness
	 * @return the treenode at a particular point, or <CODE>null</CODE> if there
	 *         is no treenode at that point
	 */
	public TreeNode nodeAtPoint(final Point2D point) {
		return treeDrawer.nodeAtPoint(point, getSize());
	}

	/**
	 * Prints the component.
	 *
	 * @param gr
	 *            the graphics interface for the printer device
	 */
	/*
	 * public void printComponent(Graphics gr) { Graphics2D g = (Graphics2D) gr;
	 * Rectangle c = g.getClipBounds(); g.translate(c.x, c.y);
	 * g.setColor(java.awt.Color.white); g.fillRect(0, 0, c.width, c.height);
	 * treeDrawer.draw(g, new Dimension(c.width, c.height)); }
	 */

	/**
	 * Paints the component.
	 *
	 * @param gr
	 *            the graphics object to draw on
	 */
	@Override
	public void paintComponent(final Graphics gr) {
		final Graphics2D g = (Graphics2D) gr;
		super.paintComponent(g);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.white);
		final Dimension d = getSize();
		g.fillRect(0, 0, d.width, d.height);
		g.setColor(Color.black);
		treeDrawer.draw(g, d);
	}

	/**
	 * Sets a new <CODE>TreeDrawer</CODE> for this treepanel.
	 *
	 * @param drawer
	 *            the new treedrawer
	 */
	public void setTreeDrawer(final TreeDrawer drawer) {
		treeDrawer = drawer;
		repaint();
	}
}
