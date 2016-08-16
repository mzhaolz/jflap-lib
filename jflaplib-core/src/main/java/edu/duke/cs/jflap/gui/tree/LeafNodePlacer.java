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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import com.google.common.collect.Lists;

/**
 * A <CODE>LeafNodePlacer</CODE> places leaves so that they are evenly spaced
 * horizontally.
 *
 * @author Thomas Finley
 */
public class LeafNodePlacer implements NodePlacer {
	/**
	 * Given a <CODE>TreeModel</CODE> that contains <CODE>TreeNode</CODE>
	 * objects, this method returns a map from all <CODE>TreeNode</CODE> objects
	 * to <CODE>Point2D</CODE> points. This placer works according to a rather
	 * simple algorithm that places all nodes at a particular depth in a tree at
	 * regular intervals.
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
		Trees.width(tree);
		final List<TreeNode> leaves = Trees.leaves(tree);
		final int depth = Trees.depth(tree);
		setPoints((TreeNode) tree.getRoot(), depth, 0, leaves.size(), Lists.newArrayList(0), nodeToPoint);
		return nodeToPoint;
	}

	/**
	 * This will place the nodes. Oooo.
	 *
	 * @param node
	 *            the current node being placed
	 * @param depth
	 *            the depth of the tree
	 * @param thisDepth
	 *            the depth of this node
	 * @param leaves
	 *            the total number of leaves
	 * @param arrayList
	 *            the number of leaves placed sofar
	 * @param nodeToPoint
	 *            the mapping of nodes to points
	 */
	private void setPoints(final TreeNode node, final int depth, final int thisDepth, final int leaves,
			final ArrayList<Integer> arrayList, final Map<TreeNode, Float> nodeToPoint) {
		final List<TreeNode> children = Trees.children(node);
		final float y = (float) (thisDepth + 1) / (float) (depth + 2);
		if (children.size() == 0) {
			// It is a leaf!
			final float x = (float) (arrayList.get(0) + 1) / (float) (leaves + 1);
			nodeToPoint.put(node, new Point2D.Float(x, y));
			arrayList.set(0, arrayList.get(0) + 1);
			return;
		}
		// Not a leaf!
		for (int i = 0; i < children.size(); i++) {
			setPoints(children.get(i), depth, thisDepth + 1, leaves, arrayList, nodeToPoint);
		}
		final Point2D leftmost = nodeToPoint.get(children.get(0));
		final Point2D rightmost = nodeToPoint.get(children.get(children.size() - 1));
		final float x = (float) ((leftmost.getX() + rightmost.getX()) / 2.0);
		nodeToPoint.put(node, new Point2D.Float(x, y));
	}
}
