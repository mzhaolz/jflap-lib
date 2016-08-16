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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.openjdk.jmh.annotations.Benchmark;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.parse.ParseNode;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.tree.DefaultNodeDrawer;
import edu.duke.cs.jflap.gui.tree.TreePanel;

/**
 * This is the special drawer for an unrestricted parse tree. Woe betide any
 * that try to understand its inner workings.
 *
 * @author Thomas Finley
 */
public class UnrestrictedTreePanel extends TreePanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** Colors. */
	protected static final Color INNER = new Color(100, 200, 120), LEAF = new Color(255, 255, 100),
			BRACKET = new Color(150, 150, 255), BRACKET_OUT = BRACKET.darker().darker();

	private HashMap<String, String> myVariableMap;

	double realWidth, realHeight, metaWidth = -1.0, metaHeight;

	/** The brute parse pane. */
	protected BruteParsePane brutePane;

	/** The parse nodes. */
	protected List<ParseNode> solutionParseNodes;

	/** The tops. */
	protected Map<Integer, Table<Integer, Integer, UnrestrictedTreeNode>> top = null;

	/** The bottoms. */
	protected Map<Integer, Table<Integer, Integer, UnrestrictedTreeNode>> bottom = null;

	/** The mapping of nodes to the center weight points of parent edges. */
	protected Map<UnrestrictedTreeNode, Double> nodeToParentWeights = new HashMap<>();

	/** The mapping of nodes to their parent group. */
	protected Map<UnrestrictedTreeNode, List<UnrestrictedTreeNode>> nodeToParentGroup = new HashMap<>();

	protected Map<UnrestrictedTreeNode, Point2D> nodeToPoint;

	/** The node drawer. */
	protected DefaultNodeDrawer nodeDrawer = new DefaultNodeDrawer();

	/** Current level. */
	int level = 0;

	/** Current group. */
	int group = 0;

	/** Current production. */
	int production = -1;

	/**
	 * Instantiates an unrestricted tree panel.
	 *
	 * @param pane
	 *            the brute parse pane
	 */
	public UnrestrictedTreePanel(final BruteParsePane pane) {
		super(new DefaultTreeModel(new DefaultMutableTreeNode("")));
		brutePane = pane;
	}

	public UnrestrictedTreePanel(final BruteParsePane pane, final HashMap<String, String> map) {
		super(new DefaultTreeModel(new DefaultMutableTreeNode("")));
		brutePane = pane;
		myVariableMap = map;
	}

	/**
	 * This function assigns proper weights to edges on levels
	 * <CODE>level</CODE> and <CODE>level+1</CODE>. This function may be called
	 * more than once per level; it is intended to operate in a somewhat
	 * iterative fashion.
	 *
	 * @param level
	 *            the level to assign
	 * @param need
	 *            the array of needs
	 */
	private boolean assignWeights(final int level, final boolean[] need) {
		if (!need[level]) {
			return false;
		}
		need[level] = false;
		boolean changed = false;
		double total = 0.0;
		for (int i = 0; i < bottom.get(level).size(); i++) {
			final Map<Integer, UnrestrictedTreeNode> s = bottom.get(level).row(i);
			final Map<Integer, UnrestrictedTreeNode> c = top.get(level + 1).row(i);
			double cSum = 0.0, sSum = 0.0;
			for (final UnrestrictedTreeNode element : s.values()) {
				sSum += element.weight;
			}
			if (!ends(level, i)) {
				total += sSum;
				continue;
			}
			for (final UnrestrictedTreeNode element : c.values()) {
				cSum += element.weight;
			}
			final Double TOTAL = new Double(total + Math.max(sSum, cSum) / 2.0);
			for (final UnrestrictedTreeNode element : c.values()) {
				nodeToParentWeights.put(element, TOTAL);
			}
			total += Math.max(sSum, cSum);

			if (cSum > sSum) {
				final double ratio = cSum / sSum;
				for (int j = 0; j < s.size(); j++) {
					s.get(j).weight *= ratio;
				}
				if (level != 0) {
					need[level - 1] = true;
				}
				changed = true;
			} else if (cSum < sSum) {
				final double ratio = sSum / cSum;
				for (int j = 0; j < c.size(); j++) {
					c.get(j).weight *= ratio;
				}
				if (level != 0) {
					need[level + 1] = true;
				}
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Returns if a group starts on a particular level. For each level, these
	 * are the top group numbers.
	 *
	 * @param level
	 *            the level number
	 * @param group
	 *            the group number for that elvel
	 * @return if a group starts on a particular level
	 */
	private boolean begins(final int level, final int group) {
		if (level == 0) {
			return true; // Everything starts at beginning.
		}
		return ends(level - 1, group);
	}

	private void bridgeTo(final int level) {
		final List<UnrestrictedTreeNode> prev = levelNodes(level - 1);
		final List<Production> prods = solutionParseNodes.get(level).getProductions();
		final List<Integer> prodStarts = solutionParseNodes.get(level).getSubstitutions();
		int length = 0, prodNum = 0;
		final Table<Integer, Integer, UnrestrictedTreeNode> bottomList = HashBasedTable.<Integer, Integer, UnrestrictedTreeNode>create();
		final Table<Integer, Integer, UnrestrictedTreeNode> topList = HashBasedTable.<Integer, Integer, UnrestrictedTreeNode>create();
		for (int i = 0; i < prev.size(); i++) {
			if (prodNum >= prods.size() || length < prodStarts.get(prodNum) || prev.get(i).toString().equals("")) {
				// Symbol doesn't change. We bring it down.
				final List<UnrestrictedTreeNode> a = Lists.newArrayList(prev.get(i));
				for (int j = 0; i < a.size(); ++j) {
					bottomList.put(i, j, a.get(j));
					topList.put(i, j, a.get(j));
				}
				length += prev.get(i).length();
				prev.get(i).lowest = level;
			} else if (length == prodStarts.get(prodNum)) {
				// Starting a production.

				final List<UnrestrictedTreeNode> currentBottom = new LinkedList<>();
				final List<UnrestrictedTreeNode> currentTop = new LinkedList<>();
				final String rhs = prods.get(prodNum).getRHS();
				final String lhs = prods.get(prodNum).getLHS();
				while (length < prodStarts.get(prodNum) + lhs.length()) {
					currentBottom.add(prev.get(i));
					prev.get(i).lowest = level - 1;
					length += prev.get(i).length();
					i++;
				}
				final List<UnrestrictedTreeNode> b = currentBottom;
				i--;
				for (int j = 0; j < rhs.length(); j++) {
					final UnrestrictedTreeNode node = new UnrestrictedTreeNode("" + rhs.charAt(j));
					node.highest = node.lowest = level;
					currentTop.add(node);
					if (j == rhs.length() - 1) {
						nodeToParentGroup.put(node, b);
					}
				}

				if (rhs.length() == 0) {
					final UnrestrictedTreeNode node = new UnrestrictedTreeNode("");
					node.highest = node.lowest = level;
					currentTop.add(node);
					nodeToParentGroup.put(node, b);
				}
				for (int j = 0; j < b.size(); ++j) {
					bottomList.put(i, j, b.get(j));
				}
				for (int j = 0; j < currentTop.size(); ++i) {
					topList.put(i, j, currentTop.get(j));
				}
				prodNum++;
			}
		}
		bottom.put(level - 1, bottomList);
		top.put(level, topList);
	}

	/**
	 * Returns if a group ends on a particular level. For each level, these are
	 * the bottom group number.
	 *
	 * @param level
	 *            the level number
	 * @param group
	 *            the group number for that level
	 * @return if a group ends on a particular level
	 */
	private boolean ends(final int level, final int group) {
		try {
			if (level == bottom.size() - 1) {
				return true; // Everything ends at last.
			}
			return !bottom.get(level).row(group).equals(top.get(level + 1).row(group));
		} catch (final ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Level " + level + ", group " + group + " is out of range!");
		}
	}

	private String getDerivation(final int level, int num) {
		final StringBuffer b = new StringBuffer(solutionParseNodes.get(level - 1).getDerivation());
		final List<Integer> subs = solutionParseNodes.get(level).getSubstitutions();
		final List<Production> ps = solutionParseNodes.get(level).getProductions();
		do {
			b.delete(subs.get(num), subs.get(num) + ps.get(num).getLHS().length());
			b.insert(subs.get(num), ps.get(num).getRHS());
		} while (--num >= 0);
		return b.toString();
	}

	/**
	 * Returns a point corresponding to a given row, and weight.
	 *
	 * @param row
	 *            the row
	 * @param weight
	 *            the weight sum for the given point
	 * @param point
	 *            the point to store the result in
	 */
	protected Point2D getPoint(final int row, final double weight, Point2D p) {
		if (p == null) {
			p = new Point2D.Double();
		}
		p.setLocation(realWidth * weight / metaWidth, realHeight * (row + 0.5) / metaHeight);
		return p;
	}

	/**
	 * Returns the string representation of the tops and bottoms.
	 */
	public String getTB() {
		final StringBuffer total = new StringBuffer();
		for (int i = 0; i < top.size(); i++) {
			final List<List<UnrestrictedTreeNode>> t = new LinkedList<>();
			final List<List<UnrestrictedTreeNode>> b = new LinkedList<>();
			for (int j = 0; j < top.get(i).rowKeySet().size(); j++) {
				t.add(ImmutableList.copyOf(top.get(i).row(j).values()));
			}
			for (int j = 0; j < bottom.get(i).rowKeySet().size(); j++) {
				b.add(ImmutableList.copyOf(bottom.get(i).row(j).values()));
			}
			total.append("T." + i + ": " + t + "\n");
			total.append("B." + i + ": " + b + "\n");
		}
		return total.toString();
	}

	private List<UnrestrictedTreeNode> levelNodes(final int level) {
		final List<UnrestrictedTreeNode> list = new ArrayList<>();
		if (top.get(level) != null) {
			for (int i = 0; i < top.get(level).rowKeySet().size(); i++) {
				for (int j = 0; j < top.get(level).row(i).size(); j++) {
					list.add(top.get(level).get(i, j));
				}
			}
		}
		return list;
	}

	/**
	 * This method should be called to go to the next part.
	 */
	public boolean next() {
		Production p = null;
		final List<Production> ps = solutionParseNodes.get(level).getProductions();
		String derivation = null;
		production++;
		if (production >= ps.size()) {
			production = 0;
			p = solutionParseNodes.get(level + 1).getProductions().get(0);
			derivation = getDerivation(level + 1, 0);
		} else {
			p = ps.get(production);
			derivation = getDerivation(level, production);
		}
		if (myVariableMap != null) {
			final String[] lhs = p.toString().split("");
			final String[] rhs = derivation.split("");
			String new_lhs = "";
			String new_rhs = "";
			for (final String lh : lhs) {
				if (myVariableMap.containsKey(lh)) {
					new_lhs = new_lhs + myVariableMap.get(lh);
				} else {
					new_lhs = new_lhs + lh;
				}
			}
			for (final String rh : rhs) {
				if (myVariableMap.containsKey(rh)) {
					new_rhs = new_rhs + myVariableMap.get(rh);
				} else {
					new_rhs = new_rhs + rh;
				}
			}
			brutePane.derivationModel.addRow(new String[] { new_lhs + "", new_rhs });
		} else {
			brutePane.derivationModel.addRow(new String[] { p + "", derivation });
		}
		do {
			group++;
			if (group >= top.get(level).rowKeySet().size()) {
				group = 0;
				level++;
			}
			if (level >= top.size()) {
				level = top.size() - 1;
				group = top.get(level).rowKeySet().size() - 1;
				break;
			}
			if (level == top.size() - 1 && group == top.get(level).rowKeySet().size() - 1) {
				break;
			}
		} while (!begins(level, group));
		String lhs = p.getRHS();
		if (lhs.length() == 0) {
			lhs = Universe.curProfile.getEmptyString();
		}
		String text = "Derived " + lhs + " from " + p.getLHS() + ".";
		if (level == top.size() - 1 && production == solutionParseNodes.get(level).getProductions().size() - 1) {
			text += "  Derivations complete.";
			brutePane.statusDisplay.setText(text);
			return true;
		}
		brutePane.statusDisplay.setText(text);

		return false;
	}

	/**
	 * Paints the component.
	 *
	 * @param gr
	 *            the graphics object to draw on
	 */
	@Override
	public void paintComponent(final Graphics gr) {
		// super.paintComponent(g);
		final Graphics2D g = (Graphics2D) gr.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.white);
		final Dimension d = getSize();
		g.fillRect(0, 0, d.width, d.height);
		g.setColor(Color.black);
		if (top != null) {
			paintTree(g);
		}
		g.dispose();
	}

	/**
	 * Paints a node at a particular point.
	 *
	 * @param g
	 *            the graphics object
	 * @param node
	 *            the node to paint
	 * @param p
	 *            the point to paint at
	 */
	public void paintNode(final Graphics2D g, UnrestrictedTreeNode node, final Point2D p) {

		g.setColor(node.lowest == top.size() - 1 ? LEAF : INNER);
		g.translate(p.getX(), p.getY());

		if (myVariableMap == null) {
			nodeDrawer.draw(g, node);
		} else {
			if (myVariableMap.containsKey(node.toString())) {
				node = new UnrestrictedTreeNode(myVariableMap.get(node.toString()));
				nodeDrawer.draw(g, node, true);
			} else {
				nodeDrawer.draw(g, node);
			}
		}
		g.translate(-p.getX(), -p.getY());
	}

	/**
	 * Paints the tree.
	 *
	 * @param g
	 *            the graphics object
	 */
	private void paintTree(final Graphics2D g) {

		final Dimension d = getSize();
		realWidth = d.width;
		realHeight = d.height;
		if (metaWidth == -1.0) {
			setMetaWidth();
		}
		metaHeight = top.size();
		final Point2D p = new Point2D.Double();
		nodeToPoint = new HashMap<>();
		for (int l = 0; l <= level; l++) {
			double total = 0.0;
			final Table<Integer, Integer, UnrestrictedTreeNode> GG = l < level ? bottom.get(l) : top.get(l);
			for (int gr = 0; gr < GG.rowKeySet().size() && (level != l || gr <= group); gr++) {
				final Map<Integer, UnrestrictedTreeNode> G = GG.row(gr);
				if (l <= level - 2 || (l == level - 1 && gr <= group)) {
					// Want the node on the bottom level.
					for (final UnrestrictedTreeNode element : G.values()) {
						if (l == element.lowest) {
							// This group is drawn on the bottom.
							// Draw the line.
							final Point2D point = getPoint(element.lowest, total + element.weight / 2.0, null);
							getPoint(element.highest, total + element.weight / 2.0, p);
							g.drawLine((int) point.getX(), (int) point.getY(), (int) p.getX(), (int) p.getY());
							// Make the mapping.
							nodeToPoint.put(element, point);
						}
						if (l == element.highest) {
							// This group is just starting.
							final Point2D point = getPoint(element.highest, total + element.weight / 2.0, null);
							final Double D = nodeToParentWeights.get(element);
							if (D != null) {
								final double pweight = D.doubleValue();
								getPoint(l - 1, pweight, p);
								g.drawLine((int) point.getX(), (int) point.getY(), (int) p.getX(), (int) p.getY());
							}
							// Draw the brackets.
							final List<UnrestrictedTreeNode> parent = nodeToParentGroup.get(element);
							if (parent != null && parent.size() != 1) {
								final Point2D alpha = nodeToPoint.get(parent.get(0));
								final Point2D beta = nodeToPoint.get(parent.get(parent.size() - 1));
								g.setColor(BRACKET);
								final int radius = (int) DefaultNodeDrawer.NODE_RADIUS;
								final int ax = (int) (alpha.getX() - radius - 3);
								final int ay = (int) (alpha.getY() - radius - 3);
								g.fillRoundRect(ax, ay, (int) (beta.getX() + radius + 3) - ax,
										(int) (beta.getY() + radius + 3) - ay, 2 * radius + 6, 2 * radius + 6);
								g.setColor(BRACKET_OUT);
								g.drawRoundRect(ax, ay, (int) (beta.getX() + radius + 3) - ax,
										(int) (beta.getY() + radius + 3) - ay, 2 * radius + 6, 2 * radius + 6);
								g.setColor(Color.black);
							}
							// Make the map.
							nodeToPoint.put(element, point);
						}
						total += element.weight;
					}
				} else if (l <= level) {
					// We're going to get the top level.
					for (final UnrestrictedTreeNode element : G.values()) {
						if (l == element.highest) {
							// This node is just starting too.
							final Point2D point = getPoint(element.highest, total + element.weight / 2.0, null);
							final Double D = nodeToParentWeights.get(element);
							if (D != null) {
								final double pweight = D.doubleValue();
								getPoint(l - 1, pweight, p);
								g.drawLine((int) point.getX(), (int) point.getY(), (int) p.getX(), (int) p.getY());
							}
							// Draw the brackets.
							final List<UnrestrictedTreeNode> parent = nodeToParentGroup.get(element);
							if (parent != null && parent.size() != 1) {
								final Point2D alpha = nodeToPoint.get(parent.get(0));
								final Point2D beta = nodeToPoint.get(parent.get(parent.size() - 1));
								g.setColor(BRACKET);
								final int radius = (int) DefaultNodeDrawer.NODE_RADIUS;
								final int ax = (int) (alpha.getX() - radius - 3);
								final int ay = (int) (alpha.getY() - radius - 3);
								g.fillRoundRect(ax, ay, (int) (beta.getX() + radius + 3) - ax,
										(int) (beta.getY() + radius + 3) - ay, 2 * radius + 6, 2 * radius + 6);
								g.setColor(BRACKET_OUT);
								g.drawRoundRect(ax, ay, (int) (beta.getX() + radius + 3) - ax,
										(int) (beta.getY() + radius + 3) - ay, 2 * radius + 6, 2 * radius + 6);
								g.setColor(Color.black);
							}
							// Make the map.
							nodeToPoint.put(element, point);
						}
						total += element.weight;
					}
				} else {
					System.err.println("Badness in the drawer!");
				}
			}
		}
		// Do the drawing of the nodes.
		final Iterator<Map.Entry<UnrestrictedTreeNode, Point2D>> it = nodeToPoint.entrySet().iterator();

		while (it.hasNext()) {
			final Map.Entry<UnrestrictedTreeNode, Point2D> e = it.next();
			paintNode(g, (e.getKey()), e.getValue());
		}
	}

	/**
	 * Sets the answer to this tree panel.
	 *
	 * @param answer
	 *            the end result of a parse tree derivation, or
	 *            <CODE>null</CODE> if no answer should be displayed
	 */
	@Benchmark
	public void setAnswer(ParseNode answer) {
		// TODO: this method has become absurdly slow.
		long start = System.nanoTime();
		System.out.println("setAnswer start.");
		if (answer == null) {
			top = null;
			return;
		}

		metaWidth = -1.0;
		solutionParseNodes = new ArrayList<>(Collections.nCopies(answer.getLevel() + 1, null));
		for (; answer != null; answer = (ParseNode) answer.getParent()) {
			solutionParseNodes.set(answer.getLevel(), answer);
		}
		
		top = new HashMap<>();
		bottom = new HashMap<>();
		
		IntStream.range(0, solutionParseNodes.size())
			.forEach(i -> {
				top.put(i, null);
				bottom.put(i, null);
			});
		// Initialize the top of the top.
		Table<Integer, Integer, UnrestrictedTreeNode> table = HashBasedTable.<Integer, Integer, UnrestrictedTreeNode>create();
		table.put(0, 0, new UnrestrictedTreeNode(solutionParseNodes.get(0).getDerivation()));
		top.put(0, table); // new UnrestrictedTreeNode[1][];
		// Create the nodes.

		for (int i = 1; i < top.size(); i++) {
			bridgeTo(i);
		}
		bottom.put(bottom.size() - 1, top.get(top.size() - 1));
		// Assign the weights.
		final boolean[] need = new boolean[top.size()];
		for (int i = 0; i < need.length; i++) {
			need[i] = true;
		}
		boolean changed = true;
		for (int max = 0; changed && max < top.size() * 2; max++) {
			changed = false;
			for (int i = 0; i < top.size() - 1; i++) {
				changed |= assignWeights(i, need);
			}
		}
		level = group = 0;
		brutePane.derivationModel.setRowCount(0);
		brutePane.derivationModel.addRow(new String[] { "", solutionParseNodes.get(0).getDerivation() });
		long end = System.nanoTime() - start;
		System.out.println("setAnswer end: " + String.valueOf(end));
		return;
	}

	protected void setMetaWidth() {
		for (int i = 0; i < top.size(); i++) {
			final List<UnrestrictedTreeNode> nodes = levelNodes(i);
			double total = 0.0;
			if (nodes != null) {
				for (final UnrestrictedTreeNode node : nodes) {
					total += node.weight;
				}
			}
			metaWidth = Math.max(total, metaWidth);
		}
	}
}
