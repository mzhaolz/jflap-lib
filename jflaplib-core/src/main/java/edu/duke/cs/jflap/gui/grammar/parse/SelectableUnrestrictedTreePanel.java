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

package edu.duke.cs.jflap.gui.grammar.parse;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.TreeNode;

import edu.duke.cs.jflap.grammar.parse.ParseNode;
import edu.duke.cs.jflap.gui.tree.DefaultNodeDrawer;

/**
 * This class allows user to select items in the tree Panel. This class was
 * intended to be used with UserControlParsePane, however we decided to not to
 * use it.
 *
 * However, this class is still called from UserControlParsePane for drawing.
 *
 * Could be useful in the future.
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class SelectableUnrestrictedTreePanel extends UnrestrictedTreePanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private boolean myClicked = false;
	private Point2D myClickedNodePoint;

	/**
	 * Constructor for SelectableUnrestrictedTreePanel
	 *
	 * @param pane
	 *            pane that is going to contain this tree panel
	 */
	public SelectableUnrestrictedTreePanel(final BruteParsePane pane) {
		super(pane);
	}

	public Point2D getPointofSelectedNode() {
		if (myClicked) {
			return myClickedNodePoint;
		}
		return null;
	}

	/**
	 * Returns the node at a particular point.
	 *
	 * @param point
	 *            the point to check for nodeness
	 * @return the treenode at a particular point, or <CODE>null</CODE> if there
	 *         is no treenode at that point
	 */
	@Override
	public TreeNode nodeAtPoint(final Point2D point) {
		final double x1 = point.getX();
		final double y1 = point.getY();
		final Iterator<Map.Entry<UnrestrictedTreeNode, Point2D>> it = nodeToPoint.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<UnrestrictedTreeNode, Point2D> e = it.next();
			final Point2D tempPoint = e.getValue();
			final double x2 = tempPoint.getX();
			final double y2 = tempPoint.getY();
			if (Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2) <= Math.pow(DefaultNodeDrawer.NODE_RADIUS, 2)) {
				myClicked = true;
				myClickedNodePoint = new Point2D.Double(x2, y2);
				// repaint();
				return e.getKey();
			}
		}
		return null;
	}

	@Override
	public void paintNode(final Graphics2D g, final UnrestrictedTreeNode node, final Point2D p) {
		// System.out.println("node out : "+node.getText());

		g.setColor(node.lowest == top.size() - 1 ? LEAF : INNER);
		if (node.getText().toUpperCase().equals(node.getText()) && !node.getText().equals("")) {
			g.setColor(INNER);
		}

		/*
		 * else { // System.out.println("node : "+node.getText()); //
		 * System.out.println("myPrev = "+myPrev); if (myPrev==false)
		 * g.setColor(INNER); }
		 * //System.out.println("node out : "+node.getText());
		 *
		 * if (node.getText().equals("(")) myPrev=false; if
		 * (node.getText().equals(")")) myPrev=true;
		 */
		g.translate(p.getX(), p.getY());
		nodeDrawer.draw(g, node);
		g.translate(-p.getX(), -p.getY());
	}

	/**
	 * Sets the answer to this tree panel.
	 *
	 * @param answer
	 *            the end result of a parse tree derivation, or
	 *            <CODE>null</CODE> if no answer should be displayed
	 */
	@Override
	public void setAnswer(final ParseNode answer) {
		if (answer == null) {
			top = null;
			return;
		}
		super.setAnswer(answer);
		for (int i = 1; i < solutionParseNodes.size(); i++) {
			for (int j = 0; j < solutionParseNodes.get(i).getSubstitutions().size(); j++) {
				next();
			}
		}
	}

	// private boolean myPrev=true;
}
