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
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * The <CODE>DefaultTreeDrawer</CODE> object is used to draw a
 * <CODE>TreeModel</CODE> in a given space with some built in basic
 * functionality.
 *
 * @author Thomas Finley
 */
public class DefaultTreeDrawer implements TreeDrawer, TreeModelListener {
	/** Is the root at the base (true) or top (false)? */
	private boolean inverted = false;

	/** Are our structures valid? */
	private boolean valid = false;

	/** The tree that this <CODE>TreeDrawer</CODE> is drawing. */
	private TreeModel tree;

	/** The mapping of nodes to points. */
	private Map<TreeNode, Float> nodeToPoint = new HashMap<>();

	/**
	 * True if visible set denotes invisible nodes (default is visible), false
	 * if visible (default is invisible).
	 */
	private boolean defaultVisible = true;

	/** The set of visible/invisible nodes. */
	private final WeakHashMap<TreeNode, ?> visibleNodes = new WeakHashMap<>();

	/** The drawer for the nodes. */
	private NodeDrawer nodeDrawer = new DefaultNodeDrawer();

	/** The placer for the nodes. */
	private NodePlacer nodePlacer = new DefaultNodePlacer();

	/**
	 * Instantiates a <CODE>DefaultTreeDrawer</CODE> for a given tree.
	 *
	 * @param tree
	 *            the tree to draw, assumed to have all nodes as
	 *            <CODE>javax.swing.tree.TreeNode</CODE> objects.
	 * @see javax.swing.tree.TreeNode
	 */
	public DefaultTreeDrawer(final TreeModel tree) {
		this.tree = tree;
		tree.addTreeModelListener(this);
	}

	/**
	 * Draws the tree in the indicated amount of space.
	 *
	 * @param g
	 *            the graphics object to draw upon
	 * @param size
	 *            the bounds for the space the tree has to draw itself in in the
	 *            current graphics, assumed to be a rectangle with a corner at
	 *            0,0.
	 */
	@Override
	public void draw(Graphics2D g, final Dimension2D size) {
		if (!valid) {
			revalidate();
		}
		g = (Graphics2D) g.create();
		// Draw the nodes.
		g.setFont(g.getFont().deriveFont(10.0f));
		g.setColor(Color.black);
		draw(g, (TreeNode) tree.getRoot(), size, null);
		g.dispose();
	}

	/**
	 * Draws the given node, as well as its subnodes.
	 *
	 * @param g
	 *            the graphics object to draw upon
	 * @param node
	 *            the node to draw
	 * @param size
	 *            the dimension of the area the graph is drawn in
	 * @param parent
	 *            the point that the parent was drawn at, or <CODE>null</CODE>
	 *            if this is the root node
	 */
	protected void draw(final Graphics2D g, final TreeNode node, final Dimension2D size, final Point2D parent) {
		final boolean visible = isVisible(node);
		final Point2D p = scalePoint(nodeToPoint.get(node), size);
		// Draw the tree-connections in preorder.
		if (visible && parent != null) {
			g.drawLine((int) parent.getX(), (int) parent.getY(), (int) p.getX(), (int) p.getY());
		}
		// Recurse on the children.
		final List<TreeNode> c = Trees.children(node);
		for (int i = 0; i < c.size(); i++) {
			draw(g, c.get(i), size, visible ? p : null);
		}
		// Draw the node in postorder.
		if (!visible) {
			return;
		}
		final Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(p.getX(), p.getY());
		g2.setColor(getNodeColor(node));
		nodeDrawer.draw(g2, node);
		g2.dispose();
	}

	/**
	 * Returns the <CODE>TreeModel</CODE> that this <CODE>TreeDrawer</CODE>
	 * draws.
	 *
	 * @return the tree model this drawer draws
	 */
	@Override
	public TreeModel getModel() {
		return tree;
	}

	/**
	 * This method returns the color for a particular node.
	 *
	 * @param node
	 *            the node to color
	 * @return the color for this node, which by default is always yellow
	 */
	protected Color getNodeColor(final TreeNode node) {
		return Color.yellow;
	}

	/**
	 * Returns the node drawer for this drawer.
	 *
	 * @return the node drawer for this drawer
	 */
	@Override
	public NodeDrawer getNodeDrawer() {
		return nodeDrawer;
	}

	/**
	 * Returns the node placer for this drawer.
	 *
	 * @return the node placer for this drawer
	 */
	@Override
	public NodePlacer getNodePlacer() {
		return nodePlacer;
	}

	/**
	 * Hides a node.
	 *
	 * @param node
	 *            the node to set as invisible
	 */
	public void hide(final TreeNode node) {
		defaultVisible = !defaultVisible;
		show(node);
		defaultVisible = !defaultVisible;
	}

	/**
	 * Sets all nodes to hidden.
	 */
	public void hideAll() {
		defaultVisible = false;
		visibleNodes.clear();
	}

	/**
	 * This marks the structure as uninitialized.
	 */
	@Override
	public void invalidate() {
		valid = false;
	}

	/**
	 * Returns if the display of the tree is inverted.
	 *
	 * @return <CODE>true</CODE> if the root is at the base, <CODE>false</CODE>
	 *         if the root is at the top
	 */
	public boolean isInverted() {
		return inverted;
	}

	/**
	 * Returns if a node is visible. This function is undefined if the node in
	 * question is not in the tree.
	 *
	 * @param node
	 *            the node to get visibility of
	 * @return the node is visible
	 */
	public boolean isVisible(final TreeNode node) {
		return defaultVisible ^ visibleNodes.containsKey(node);
	}

	/**
	 * Returns the node at a particular point.
	 *
	 * @param point
	 *            the point to check for the presence of a node
	 * @param size
	 *            the size that the tree, if drawn, would be drawn in
	 */
	@Override
	public TreeNode nodeAtPoint(final Point2D point, final Dimension2D size) {
		final Iterator<Map.Entry<TreeNode, Point2D.Float>> it = nodeToPoint.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<TreeNode, Point2D.Float> entry = it.next();
			final Point2D p = scalePoint(entry.getValue(), size);
			final TreeNode node = entry.getKey();
			if (nodeDrawer.onNode(node, point.getX() - p.getX(), point.getY() - p.getY())) {
				return node;
			}
		}
		return null;
	}

	/**
	 * This initializes whatever structures need to be reinitialized after there
	 * is some change in the tree.
	 */
	@Override
	public void revalidate() {
		valid = true;
		nodeToPoint = nodePlacer.placeNodes(tree, nodeDrawer);
	}

	/**
	 * Given a point and a size, returns the point scaled to the size.
	 *
	 * @param point
	 *            a point in ([0,1], [0,1])
	 * @param size
	 *            some dimensions
	 * @return a point scaled to the size, or POSSIBLY null if an error occurred
	 *         (which it never should)
	 */
	private Point2D scalePoint(final Point2D point, final Dimension2D size) {
		Point2D scale = null;
		try {
			scale = point.getClass().newInstance();
			if (inverted) {
				scale.setLocation(point.getX() * size.getWidth(), (1.0 - point.getY()) * size.getHeight());
			} else {
				scale.setLocation(point.getX() * size.getWidth(), point.getY() * size.getHeight());
			}
		} catch (final Throwable e) {
			System.err.println("BADNESS SCALING THE POINT IN TREEDRAWER!");
			System.err.println(e);
		}
		return scale;
	}

	/**
	 * Sets the display of the tree to inverted or uninverted.
	 *
	 * @param inverted
	 *            <CODE>true</CODE> if the root should be at the base,
	 *            <CODE>false</CODE> if the root should be at the top
	 */
	public void setInverted(final boolean inverted) {
		this.inverted = inverted;
	}

	/**
	 * Sets the <CODE>TreeModel</CODE> that this <CODE>TreeDrawer</CODE> draws.
	 *
	 * @param model
	 *            the new tree model
	 */
	public void setModel(final TreeModel model) {
		tree = model;
		invalidate();
	}

	/**
	 * Sets the node drawer for this drawer. By default this is set to an
	 * instance of <CODE>DefaultNodeDrawer</CODE>.
	 *
	 * @param drawer
	 *            the new node drawer
	 * @see edu.duke.cs.jflap.gui.tree.DefaultNodeDrawer
	 */
	@Override
	public void setNodeDrawer(final NodeDrawer drawer) {
		nodeDrawer = drawer;
	}

	/**
	 * Sets the node placer for this drawer. By default this is set to an
	 * instance of <CODE>DefaultNodePlacer</CODE>.
	 *
	 * @param placer
	 *            the new node placer
	 * @see edu.duke.cs.jflap.gui.tree.DefaultNodePlacer
	 */
	@Override
	public void setNodePlacer(final NodePlacer placer) {
		nodePlacer = placer;
		invalidate();
	}

	/**
	 * Shows a node.
	 *
	 * @param node
	 *            the node to set as visible
	 */
	public void show(final TreeNode node) {
		if (defaultVisible) {
			visibleNodes.remove(node);
		} else {
			visibleNodes.put(node, null);
		}
	}

	/**
	 * Sets all nodes to visible.
	 */
	public void showAll() {
		defaultVisible = true;
		visibleNodes.clear();
	}

	/**
	 * Invoked after nodes have changed.
	 */
	@Override
	public void treeNodesChanged(final TreeModelEvent e) {
		invalidate();
	}

	/**
	 * Invoked after nodes have been inserted.
	 */
	@Override
	public void treeNodesInserted(final TreeModelEvent e) {
		invalidate();
	}

	/**
	 * Invoked after nodes have been removed.
	 */
	@Override
	public void treeNodesRemoved(final TreeModelEvent e) {
		invalidate();
	}

	/**
	 * Invoked after the structure of a tree has changed.
	 */
	@Override
	public void treeStructureChanged(final TreeModelEvent e) {
		invalidate();
	}
}
