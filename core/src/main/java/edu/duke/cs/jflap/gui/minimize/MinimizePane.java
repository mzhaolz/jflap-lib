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

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.automata.fsa.MinimizeTreeNode;
import edu.duke.cs.jflap.automata.fsa.Minimizer;
import edu.duke.cs.jflap.automata.graph.AutomatonGraph;
import edu.duke.cs.jflap.automata.graph.layout.GEMLayoutAlgorithm;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.editor.EditorPane;
import edu.duke.cs.jflap.gui.editor.Tool;
import edu.duke.cs.jflap.gui.editor.ToolBox;
import edu.duke.cs.jflap.gui.editor.TransitionTool;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.tree.SelectTreeDrawer;
import edu.duke.cs.jflap.gui.tree.TreePanel;
import edu.duke.cs.jflap.gui.tree.Trees;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * The <CODE>MinimizePane</CODE> is a view created to minimize a DFA using some
 * minimizing tree structure.
 *
 * @author Thomas Finley
 */
public class MinimizePane extends JPanel {
	/**
	 * This extension of the arrow tool does not allow the editing of
	 * transitions.
	 */
	private class ArrowMinimizeTool extends edu.duke.cs.jflap.gui.editor.ArrowNontransitionTool {
		/**
		 * Instantiates a new <CODE>ArrowMinimizeTool</CODE>.
		 *
		 * @param view
		 *            the view the automaton is drawn in
		 * @param drawer
		 *            the automaton drawer
		 */
		public ArrowMinimizeTool(final AutomatonPane view, final AutomatonDrawer drawer) {
			super(view, drawer);
		}

		/**
		 * On a mouse click, this simply returns,
		 *
		 * @param event
		 *            the mouse event
		 */
		@Override
		public void mouseClicked(final java.awt.event.MouseEvent event) {
			super.mouseClicked(event);
			final State s = automatonDrawer.stateAtPoint(event.getPoint());
			// If we're still building the tree...
			if (builderController == null) {
				controller.stateDown(s, event);
			}
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 5651430058368644401L;

	/** The object that handles the grit of the minimization. */
	Minimizer minimizer;

	/** The drawer for the original automaton. */
	SelectionDrawer automatonDrawer;

	/** The drawer for the tree. */
	SelectTreeDrawer treeDrawer;

	/** The minimize controller. */
	MinimizeController controller;

	/** The minimum automaton builder controller. */
	BuilderController builderController = null;

	/** The view for this pane. */
	JSplitPane split;

	/** The toolbar. */
	ControlPanel controlPanel;

	/** The message label. */
	JLabel messageLabel = new JLabel(" ");

	/**
	 * Instantiates a <CODE>MinimizePane</CODE>.
	 *
	 * @param dfa
	 *            a DFA to minimize
	 * @param environment
	 *            the environment this minimize pane will be added to
	 */
	public MinimizePane(FiniteStateAutomaton dfa, final Environment environment) {
		// Set up the minimizable automaton, and the minimize tree.
		minimizer = new Minimizer();
		minimizer.initializeMinimizer();
		dfa = (FiniteStateAutomaton) minimizer.getMinimizeableAutomaton(dfa);
		// minimizer.initializeMinimizer();
		final TreeModel tree = minimizer.getInitializedTree(dfa);
		// Set up the drawers.
		automatonDrawer = new SelectionDrawer(dfa);
		treeDrawer = new SelectTreeDrawer(tree);
		// Set up the minimize node drawer.
		final MinimizeNodeDrawer nodeDrawer = new MinimizeNodeDrawer();
		treeDrawer.setNodeDrawer(nodeDrawer);
		final List<TreeNode> groups = Trees.children((MinimizeTreeNode) tree.getRoot());
		for (int i = 0; i < groups.size(); i++) {
			final MinimizeTreeNode group = (MinimizeTreeNode) groups.get(i);
			final State[] states = (State[]) group.getUserObject();
			if (states.length == 0) {
				continue;
			}
			if (dfa.isFinalState(states[0])) {
				nodeDrawer.setLabel(group, "Final");
			} else {
				nodeDrawer.setLabel(group, "Nonfinal");
			}
		}

		// Set up the controller object.
		controller = new MinimizeController(this, automatonDrawer, treeDrawer, minimizer);
		final JPanel right = new JPanel(new BorderLayout());
		right.add(initTreePane(), BorderLayout.CENTER);
		controlPanel = new ControlPanel(treeDrawer, controller);
		/*
		 * right.add(new ControlPanel(treeDrawer, controller),
		 * BorderLayout.SOUTH);
		 */

		// Finally, initialize the view.
		split = SplitPaneFactory.createSplit(environment, true, 0.5, initAutomatonPane(), right);
		setLayout(new BorderLayout());
		add(split, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.NORTH);
		add(messageLabel, BorderLayout.SOUTH);
		split.setResizeWeight(0.5);
		controller.setEnabledness();
	}

	/**
	 * Tells the pane to replace the tree pane with a pane to build the
	 * minimized automaton. This should be called once the tree is completed and
	 * the user has elected to move on to the building of the minimized
	 * automaton.
	 *
	 * @param dfa
	 *            the finite state automaton we're minimizing
	 * @param tree
	 *            the completed minimized tree; results will be unpredictable if
	 *            this tree is not truly minimized
	 */
	public void beginMinimizedAutomaton(final FiniteStateAutomaton dfa, final DefaultTreeModel tree) {
		// Create the new view.
		remove(controlPanel);
		final FiniteStateAutomaton newAutomaton = new FiniteStateAutomaton();
		minimizer.createStatesForMinimumDfa(dfa, newAutomaton, tree);
		final SelectionDrawer drawer = new SelectionDrawer(newAutomaton);
		final EditorPane ep = new EditorPane(drawer, (ToolBox) (view, drawer1) -> {
			final java.util.List<Tool> tools = new LinkedList<>();
			tools.add(new ArrowMinimizeTool(view, drawer1));
			tools.add(new TransitionTool(view, drawer1));
			return tools;
		});
		// Remove all selected stuff.
		automatonDrawer.clearSelected();
		// Set up the controller device.
		builderController = new BuilderController(dfa, newAutomaton, drawer, minimizer, tree, split);
		// Set the view in the right hand side.
		final JPanel right = new JPanel(new BorderLayout());
		right.add(ep, BorderLayout.CENTER);
		/*
		 * right.add(new BuilderControlPanel(builderController),
		 * BorderLayout.SOUTH);
		 */
		ep.getToolBar().addSeparator();
		BuilderControlPanel.initView(ep.getToolBar(), builderController);
		split.setRightComponent(right);
		invalidate();
		repaint();

		// Do graph layout.
		final AutomatonGraph graph = new AutomatonGraph(newAutomaton);
		graph.addVertex(newAutomaton.getInitialState(), new Point(0, 0));
		final Iterator<?> it = builderController.remainingTransitions.iterator();
		while (it.hasNext()) {
			final Transition t = (Transition) it.next();
			graph.addEdge(t.getFromState(), t.getToState());
		}
		final GEMLayoutAlgorithm<State> layout = new GEMLayoutAlgorithm<>();
		final Set<State> constantStates = new HashSet<>();
		constantStates.add(newAutomaton.getInitialState());
		layout.layout(graph, constantStates);
		graph.moveAutomatonStates();
		validate();
		ep.getAutomatonPane().fitToBounds(10);
	}

	/**
	 * Initializes the automaton pane.
	 */
	public AutomatonPane initAutomatonPane() {
		final AutomatonPane apane = new AutomatonPane(automatonDrawer);
		final edu.duke.cs.jflap.gui.SuperMouseAdapter a = new ArrowMinimizeTool(apane, automatonDrawer);
		apane.addMouseListener(a);
		apane.addMouseMotionListener(a);
		return apane;
	}

	/**
	 * Initializes the tree pane.
	 */
	public TreePanel initTreePane() {
		final TreePanel tpane = new TreePanel(treeDrawer);
		final edu.duke.cs.jflap.gui.SuperMouseAdapter a = new edu.duke.cs.jflap.gui.SuperMouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent event) {
				final TreeNode n = tpane.nodeAtPoint(event.getPoint());
				controller.nodeClicked((MinimizeTreeNode) n, event);
			}

			@Override
			public void mousePressed(final MouseEvent event) {
				final TreeNode n = tpane.nodeAtPoint(event.getPoint());
				controller.nodeDown((MinimizeTreeNode) n, event);
			}
		};
		tpane.addMouseListener(a);
		tpane.addMouseMotionListener(a);
		return tpane;
	}
}
