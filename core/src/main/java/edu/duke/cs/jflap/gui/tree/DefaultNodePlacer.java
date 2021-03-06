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

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * A <CODE>DefaultNodePlacer</CODE> places the nodes in a tree in a rather
 * simple "top down" pattern.
 *
 * @author Thomas Finley
 */
public class DefaultNodePlacer implements NodePlacer {
	/**
	 * Given a <CODE>TreeModel</CODE> that contains <CODE>TreeNode</CODE>
	 * objects, this method returns a map from all <CODE>TreeNode</CODE> objects
	 * to <CODE>Dimension2D</CODE> points. This placer works according to a
	 * rather simple algorithm that places all nodes at a particular depth in a
	 * tree at regular intervals.
	 *
	 * @param tree
	 *            the tree model
	 * @param drawer
	 *            the object that draws the nodes in the tree
	 * @return a map from the nodes of the tree to points where those nodes
	 *         should be drawn
	 */
	@Override
	public Map<TreeNode, Float> placeNodes(final TreeModel tree, final NodeDrawer drawer) {
		final HashMap<TreeNode, Float> nodeToPoint = new HashMap<>();
		final List<Integer> width = Trees.width(tree);
		final List<Integer> sofar = new ArrayList<>(Collections.nCopies(width.size(), 0));
		setPoints((TreeNode) tree.getRoot(), width.size() - 1, 0, width, sofar, nodeToPoint);
		return nodeToPoint;
	}

	/**
	 * Recursively sets the points of the tree going the
	 * <CODE>nodeToPoint</CODE> structure as it goes.
	 *
	 * @param node
	 *            the current node in the tree
	 * @param depth
	 *            the total depth of the tree
	 * @param thisDepth
	 *            the depth of this particular node
	 * @param width
	 *            the array of all widths
	 * @param sofar
	 *            the widths sofar
	 * @param nodeToPoint
	 *            the mapping of nodes to points built
	 */
	private void setPoints(final TreeNode node, final int depth, int thisDepth, final List<Integer> width,
			final List<Integer> sofar, final Map<TreeNode, Float> nodeToPoint) {
		// Scale points along ([0,1], [0,1]).
		final float x = (float) (sofar.get(thisDepth) + 1) / (float) (width.get(thisDepth) + 1);
		final float y = (float) (thisDepth + 1) / (float) (depth + 2);
		nodeToPoint.put(node, new Point2D.Float(x, y));
		// Check the maximum width.
		// max_width = Math.max(max_width, width[thisDepth]);
		// Update the depth and width figures.
		sofar.set(++thisDepth, sofar.get(thisDepth) + 1);
		// Recurse on children.
		final List<TreeNode> children = Trees.children(node);
		for (int i = 0; i < children.size(); i++) {
			setPoints(children.get(i), depth, thisDepth, width, sofar, nodeToPoint);
		}
	}
}
