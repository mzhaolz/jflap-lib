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
import java.awt.Graphics2D;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.tree.TreeNode;

import com.google.common.collect.Lists;

/**
 * Adding this to a drawer allows one to select nodes, and have them appear to
 * be selected.
 *
 * @author Thomas Finley
 */
public class SelectNodeDrawer extends DefaultNodeDrawer {
	/** The selected nodes, with keys as nodes. */
	protected WeakHashMap<TreeNode, ?> selectedNodes = new WeakHashMap<>();

	/**
	 * Sets all nodes as deselected.
	 */
	public void clearSelected() {
		selectedNodes.clear();
	}

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
		final Color c = g.getColor();
		if (isSelected(node)) {
			g.setColor(c.darker());
		}
		super.draw(g, node);
		g.setColor(c);
	}

	/**
	 * Returns an array containing the list of all selected nodes.
	 *
	 * @return an array containing the list of all selected nodes
	 */
	public List<TreeNode> getSelected() {
		return Lists.newArrayList(selectedNodes.keySet());
	}

	/**
	 * Determines if a node is selected.
	 *
	 * @param node
	 *            the node to check for selectedness
	 * @return <CODE>true</CODE> if the node is selected, <CODE>false</CODE>
	 *         otehrwise
	 */
	public boolean isSelected(final TreeNode node) {
		return selectedNodes.containsKey(node);
	}

	/**
	 * Sets the selectedness of a node.
	 *
	 * @param node
	 *            the node to select or deselect
	 * @param select
	 *            if true, then select the node, otherwise deselect
	 */
	public void setSelected(final TreeNode node, final boolean select) {
		if (select) {
			selectedNodes.put(node, null);
		} else {
			selectedNodes.remove(node);
		}
	}
}
