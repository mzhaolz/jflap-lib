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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.parse.LLParseTable;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * This is the parse controller for an LL parse pane.
 *
 * @author Thomas Finley
 */
class LLParseController {
	/** The modes for the step function. */
	private static final int INITIALIZE = 1, NORMAL = 2, REPLACING = 3, ERROR = 4, SUCCESS = 5;

	String ENTRY;

	int ENTRYP;

	private int P;

	private String STRING;

	private int NODECOUNT;

	private Stack<TreeNode> STACK;

	private List<? extends TreeNode> NODES;

	/** The current derivation string. */
	private String derivationString;

	/** The parse pane. */
	private final LLParsePane pane;

	/** The parse tree. */
	private DefaultTreeModel tree;

	// VARIABLES FOR THE PARSING STEPPING
	// These would be local variables in a parse function...

	/** The current mode for the step function. */
	private int stepMode = INITIALIZE;

	/**
	 * Instantiates a new LL parse controller.
	 *
	 * @param pane
	 *            the LL parse pane
	 */
	public LLParseController(final LLParsePane pane) {
		this.pane = pane;
	}

	/**
	 * Dehighlights stuff.
	 */
	private void dehighlight() {
		pane.tablePanel.dehighlight();
		pane.grammarTable.dehighlight();
		pane.tablePanel.repaint();
		pane.grammarTable.repaint();
	}

	/**
	 * Returns the rule for a particular lookahead and variable of the LL parse
	 * table.
	 *
	 * @param variable
	 *            the variable to look under
	 * @param lookahead
	 *            the lookahead to look under
	 * @return the rule for the grammar, or <CODE>null</CODE> if no such entry
	 *         exists
	 */
	private String get(final String variable, final String lookahead) {
		try {
			return pane.table.get(variable, lookahead).first();
		} catch (final IllegalArgumentException e) {
			return null;
		} catch (final NoSuchElementException e) {
			return null;
		}
	}

	/**
	 * Hightlights the cell in the parse table indexed by a variable and
	 * terminal.
	 *
	 * @param id
	 *            the state id
	 * @param symbol
	 *            the grammar symbol
	 */
	private void highlight(final String variable, final String terminal) {
		final int row = pane.table.getRow(variable);
		final int column = pane.table.getColumn(terminal);
		pane.tablePanel.highlight(row, column);
		pane.tablePanel.repaint();
		pane.grammarTable.repaint();
	}

	/**
	 * Sets up for new input.
	 *
	 * @param string
	 *            the new string to parse
	 */
	public void initialize(final String string) {
		dehighlight();
		final ArrayList<MutableTreeNode> nodes = new ArrayList<>();
		tree = parseTree(string, pane.grammar, pane.table, nodes);
		pane.treeDrawer.setModel(tree);
		pane.treeDrawer.hideAll();
		pane.treePanel.repaint();
		pane.stepAction.setEnabled(true);
		pane.derivationModel.setRowCount(0);
		// Initialize those global structures! :)
		NODES = nodes;
		STRING = string + "$";
		STACK = new Stack<>();
		P = 0;
		NODECOUNT = 0;
		stepMode = INITIALIZE;
		updateStatus();
		pane.statusDisplay.setText("Press step to begin.");
	}

	/**
	 * This action will perform parsing of a string.
	 *
	 * @param string
	 *            the string to parse
	 * @param grammar
	 *            the augmented grammar
	 * @param table
	 *            the parse table
	 * @return the parse tree
	 */
	private DefaultTreeModel parseTree(String string, final Grammar grammar, final LLParseTable table,
			final List<MutableTreeNode> nodes) {
		int p = 0;
		string = string + "$";
		final Stack<MutableTreeNode> stack = new Stack<>();
		final MutableTreeNode root = new DefaultMutableTreeNode(grammar.getStartVariable());
		stack.push(root);
		nodes.add(root);
		final DefaultTreeModel tree = new DefaultTreeModel(root);
		String read = string.substring(p, p + 1);
		p++;
		while (!stack.empty()) {
			final String top = stack.peek().toString();
			if (pane.grammar.isTerminal(top)) {
				if (top.equals(read)) {
					stack.pop();
					read = string.substring(p, p + 1);
					p++;
				} else {
					return tree;
				}
			} else if (pane.grammar.isVariable(top)) {
				final String entry = get(top, read);
				if (entry == null) {
					return tree;
				} else {
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) stack.pop();
					if (entry.length() == 0) {
						final MutableTreeNode child = new DefaultMutableTreeNode(Universe.curProfile.getEmptyString());
						node.insert(child, 0);
						nodes.add(child);
					} else {
						for (int i = entry.length() - 1; i >= 0; i--) {
							final MutableTreeNode child = new DefaultMutableTreeNode(entry.substring(i, i + 1));
							node.insert(child, 0);
							stack.push(child);
							nodes.add(child);
						}
					}
				}
			} else {
				// This should never happen.
			}
		}
		return tree;
	}

	/**
	 * Returns the stack string.
	 */
	private String stackString() {
		final Stack<TreeNode> o = STACK;
		final StringBuffer sb = new StringBuffer();
		for (int i = o.size() - 1; i >= 0; i--) {
			sb.append(o.get(i));
		}
		return sb.toString();
	}

	/**
	 * The step function. Yay!
	 */
	public void step() {
		final String read = STRING.substring(P, P + 1);
		switch (stepMode) {
		case INITIALIZE:
			STACK.push(NODES.get(0)); // Push the root.
			pane.treeDrawer.show(NODES.get(0));
			pane.treePanel.repaint();
			NODECOUNT++;
			stepMode = NORMAL;
			updateStatus();
			pane.statusDisplay.setText("Initialization complete.");
			derivationString = pane.grammar.getStartVariable();
			pane.derivationModel.addRow(new String[] { "", derivationString });
			break;
		case NORMAL:
			dehighlight();
			if (STACK.empty()) {
				stepMode = SUCCESS;
				step();
				return;
			}
			final String top = STACK.peek().toString();
			if (pane.grammar.isTerminal(top)) {
				if (top.equals(read)) {
					final TreeNode node = STACK.pop();
					pane.nodeDrawer.clearSelected();
					pane.nodeDrawer.setSelected(node, true);
					pane.treePanel.repaint();
					P++;
					pane.statusDisplay.setText("Matched " + read + ".");
				} else {
					stepMode = ERROR;
					pane.statusDisplay.setText("Stack and input don't match.");
				}
				updateStatus();
				return;
			}

			if (pane.grammar.isVariable(top)) {
				final TreeNode node = STACK.pop();
				pane.nodeDrawer.clearSelected();
				pane.nodeDrawer.setSelected(node, true);
				pane.treePanel.repaint();

				ENTRY = get(top, read);
				if (ENTRY == null) {
					stepMode = ERROR;
					pane.statusDisplay.setText("No rule for " + top + " with " + read + " as lookahead.");
					updateStatus();
					return;
				}
				highlight(top, read);
				// Now the derivation table garbage.
				final String rule = (new Production(top, ENTRY)).toString();
				final int first = derivationString.indexOf(top.charAt(0));
				derivationString = derivationString.substring(0, first) + ENTRY + derivationString.substring(first + 1);
				pane.derivationModel.addRow(new String[] { rule, derivationString });
				// What? About? Lambda?
				if (ENTRY.length() == 0) {
					ENTRY = Universe.curProfile.getEmptyString();
				}
				ENTRYP = ENTRY.length() - 1;
				pane.statusDisplay.setText("Replacing " + top + " with " + ENTRY + ".");

				stepMode = REPLACING;
			}
			updateStatus();
			return;
		case REPLACING:
			if (ENTRYP < 0) {
				stepMode = NORMAL;
				step();
				return;
			}
			final TreeNode node = NODES.get(NODECOUNT++);
			pane.treeDrawer.show(node);
			pane.treePanel.repaint();
			if (!node.toString().equals(Universe.curProfile.getEmptyString())) {
				STACK.push(node);
			}
			ENTRYP--;
			updateStatus();
			return;
		case ERROR:
			dehighlight();
			pane.statusDisplay.setText("String rejected.");
			pane.stepAction.setEnabled(false);
			return;
		case SUCCESS:
			dehighlight();
			// The stack may be empty... but is it really correct?
			if (!read.equals("$")) {
				pane.statusDisplay.setText("The stack is empty, but the input is not.");
				stepMode = ERROR;
			} else {
				pane.stepAction.setEnabled(false);
				pane.statusDisplay.setText("String successfully parsed!");
			}
			return;
		default:
		}
	}

	/**
	 * Sets the current stack and remaining input displays in the view to
	 * reflect current realities.
	 */
	private void updateStatus() {
		pane.stackDisplay.setText(stackString());
		pane.inputDisplay.setText(STRING.substring(P));
	}
}
