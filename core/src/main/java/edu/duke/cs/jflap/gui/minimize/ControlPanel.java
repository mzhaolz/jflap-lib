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

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.tree.TreeNode;

import edu.duke.cs.jflap.automata.fsa.MinimizeTreeNode;
import edu.duke.cs.jflap.gui.TooltipAction;
import edu.duke.cs.jflap.gui.tree.SelectTreeDrawer;

/**
 * The <CODE>ControlPanel</CODE> contains buttons whose actions call methods on
 * the minimize controller.
 *
 * @author Thomas Finley
 */
class ControlPanel extends JToolBar {
	/**
	 *
	 */
	private static final long serialVersionUID = 1502736623241702582L;

	/**
	 * The message to display when there is more than one node or no nodes
	 * selected.
	 */
	private static final String NODE_ERROR = "Exactly one node must be selected!";

	/** The tree drawer. */
	private final SelectTreeDrawer treeDrawer;

	/** The minimize controller to call methods on. */
	private final MinimizeController controller;

	TooltipAction setTerminalAction, autoPartitionAction, completeSubtreeAction, checkNodeAction, addChildAction,
			removeAction, finishAction;

	/**
	 * Instantiates a new <CODE>ControlPanel</CODE>.
	 *
	 * @param treeDrawer
	 *            the tree drawer to get selected nodes from
	 * @param controller
	 *            the minimize controller
	 */
	public ControlPanel(final SelectTreeDrawer treeDrawer, final MinimizeController controller) {
		super();
		this.treeDrawer = treeDrawer;
		this.controller = controller;
		initView();
	}

	/**
	 * Returns the selected node in the tree drawer.
	 *
	 * @return the selected node, or <CODE>null</CODE> if no or multiple nodes
	 *         are selected
	 */
	private MinimizeTreeNode getNode() {
		final List<TreeNode> selected = treeDrawer.getSelected();
		if (selected.size() != 1) {
			return null;
		}
		return (MinimizeTreeNode) selected.get(0);
	}

	/**
	 * Initializes the components of the view.
	 */
	private void initView() {
		setTerminalAction = new TooltipAction("Set Terminal", "Begins the split of a group on a terminal.") {
			/**
			 *
			 */
			private static final long serialVersionUID = -3445189389952982103L;

			@Override
			public void actionPerformed(final ActionEvent event) {
				final MinimizeTreeNode node = getNode();
				if (node == null) {
					JOptionPane.showMessageDialog(ControlPanel.this, NODE_ERROR);
					return;
				}
				controller.splitOnTerminal(node);
				controller.setEnabledness();
			}
		};
		add(setTerminalAction);

		autoPartitionAction = new TooltipAction("Auto Partition", "Does the split of a group for you.") {
			/**
			 *
			 */
			private static final long serialVersionUID = 2396044313276565099L;

			@Override
			public void actionPerformed(final ActionEvent event) {
				final MinimizeTreeNode node = getNode();
				if (node == null) {
					JOptionPane.showMessageDialog(ControlPanel.this, NODE_ERROR);
					return;
				}
				controller.splitWithoutInput(node);
				controller.setEnabledness();
			}
		};
		add(autoPartitionAction);

		completeSubtreeAction = new TooltipAction("Complete Subtree",
				"Does the split of this group and any subgroups for you.") {
			/**
			 *
			 */
			private static final long serialVersionUID = -8005109949177890385L;

			@Override
			public void actionPerformed(final ActionEvent event) {
				final MinimizeTreeNode node = getNode();
				if (node == null) {
					JOptionPane.showMessageDialog(ControlPanel.this, NODE_ERROR);
					return;
				}
				controller.splitSubtree(node);
				controller.setEnabledness();
			}
		};
		add(completeSubtreeAction);

		checkNodeAction = new TooltipAction("Check Node", "Verifies that a split of a group is correct.") {
			/**
			 *
			 */
			private static final long serialVersionUID = 6094149826872361113L;

			@Override
			public void actionPerformed(final ActionEvent event) {
				controller.check();
				controller.setEnabledness();
			}
		};
		add(checkNodeAction);

		addChildAction = new TooltipAction("Add Child", "Adds a new partition a group being split.") {
			/**
			 *
			 */
			private static final long serialVersionUID = -1207792629025600376L;

			@Override
			public void actionPerformed(final ActionEvent event) {
				controller.addChild();
				controller.setEnabledness();
			}
		};
		add(addChildAction);

		removeAction = new TooltipAction("Remove", "Removes a partition from a group being split.") {
			/**
			 *
			 */
			private static final long serialVersionUID = -2415907778321643162L;

			@Override
			public void actionPerformed(final ActionEvent event) {
				final List<TreeNode> selected = treeDrawer.getSelected();
				for (final TreeNode seli : selected) {
					controller.removeNode((MinimizeTreeNode) seli);
				}
				controller.setEnabledness();
			}
		};
		add(removeAction);

		finishAction = new TooltipAction("Finish", "If the tree is done, begins building the automaton.") {
			/**
			 *
			 */
			private static final long serialVersionUID = -732801539899143007L;

			@Override
			public void actionPerformed(final ActionEvent event) {
				controller.finished();
				controller.setEnabledness();
			}
		};
		add(finishAction);
	}
}
