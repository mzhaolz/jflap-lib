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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.TableTextSizeSlider;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.grammar.GrammarTable;
import edu.duke.cs.jflap.gui.grammar.GrammarTableModel;
import edu.duke.cs.jflap.gui.tree.DefaultTreeDrawer;
import edu.duke.cs.jflap.gui.tree.LeafNodePlacer;
import edu.duke.cs.jflap.gui.tree.TreePanel;

/**
 * The parse pane is an abstract class that defines the interface common between
 * parsing panes.
 *
 * @author Thomas Finley
 */
abstract class ParsePane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The label that displays the remaining input. */
	JTextField inputDisplay = new JTextField();

	/** The label that displays the stack. */
	JTextField stackDisplay = new JTextField();

	/** The label that displays the current status of the parse. */
	JLabel statusDisplay = new JLabel("Input a string to begin.");

	/** The input text field. */
	public JTextField inputField = new JTextField();

	/** The grammar being displayed. */
	public Grammar grammar;

	/** The display for the grammar. */
	GrammarTable grammarTable;

	/** The environment. */
	GrammarEnvironment environment;

	/** The action for the stepping control. */
	public AbstractAction stepAction = new AbstractAction("Step") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			step();
		}
	};

	/** The action for the start control. */
	AbstractAction startAction = new AbstractAction("Start") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			input(inputField.getText());
		}
	};

	/** A default tree drawer. */
	DefaultTreeDrawer treeDrawer = new DefaultTreeDrawer(new DefaultTreeModel(new DefaultMutableTreeNode())) {
		private final Color INNER = new Color(100, 200, 120), LEAF = new Color(255, 255, 100);

		@Override
		protected Color getNodeColor(final TreeNode node) {
			return node.isLeaf() ? LEAF : INNER;
		}
	};

	/** A default tree display. */
	JComponent treePanel = new TreePanel(treeDrawer);

	/** The table model for the derivations. */
	DefaultTableModel derivationModel = new DefaultTableModel(new String[] { "Production", "Derivation" }, 0) {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(final int r, final int c) {
			return false;
		}
	};

	/** The split views. */
	JSplitPane mainSplit, topSplit, bottomSplit;

	/** The card layout. */
	CardLayout treeDerivationLayout = new CardLayout();

	/** The derivation/parse tree view. */
	public JPanel treeDerivationPane = new JPanel(treeDerivationLayout);

	/** The derivation view. */
	JScrollPane derivationPane;

	/**
	 * Instantiates a new parse pane. This will not place components. A call to
	 * {@link #initView} by a subclass is necessary.
	 *
	 * @param grammar
	 *            the grammar that is being parsed
	 */
	public ParsePane(final GrammarEnvironment environment, final Grammar grammar) {
		super(new BorderLayout());
		this.grammar = grammar;
		this.environment = environment;
	}

	/**
	 * Changes the view.
	 *
	 * @param name
	 *            the view button name that was pressed
	 */
	protected void changeView(final String name) {
		if (name.equals("Noninverted Tree")) {
			treeDerivationLayout.first(treeDerivationPane);
			treeDrawer.setInverted(false);
			treePanel.repaint();
		} else if (name.equals("Inverted Tree")) {
			treeDerivationLayout.first(treeDerivationPane);
			treeDrawer.setInverted(true);
			treePanel.repaint();
		} else if (name.equals("Derivation Table")) {
			treeDerivationLayout.last(treeDerivationPane);
		}
	}

	/**
	 * Returns the choices for the view.
	 *
	 * @return an array of strings for the choice of view
	 */
	protected String[] getViewChoices() {
		return new String[] { "Noninverted Tree", "Inverted Tree", "Derivation Table" };
	}

	/**
	 * Inits a new derivation table.
	 *
	 * @return a new display for the derivation of the parse
	 */
	protected JTable initDerivationTable() {
		final JTable table = new JTable(derivationModel);
		table.setGridColor(Color.lightGray);
		return table;
	}

	/**
	 * Initializes a table for the grammar.
	 *
	 * @param grammar
	 *            the grammar
	 * @return a table to display the grammar
	 */
	protected GrammarTable initGrammarTable(final Grammar grammar) {
		grammarTable = new GrammarTable(new GrammarTableModel(grammar) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(final int r, final int c) {
				return false;
			}
		}) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(final java.awt.event.MouseEvent event) {
				try {
					final int row = rowAtPoint(event.getPoint());
					return getGrammarModel().getProduction(row).toString() + " is production " + row;
				} catch (final Throwable e) {
					return null;
				}
			}
		};
		return grammarTable;
	}

	/**
	 * Returns the interface that holds the input area.
	 */
	protected JPanel initInputPanel() {
		final JPanel bigger = new JPanel(new BorderLayout());
		final JPanel panel = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gridbag);

		c.fill = GridBagConstraints.BOTH;

		c.weightx = 0.0;
		panel.add(new JLabel("Input"), c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(inputField, c);
		inputField.addActionListener(startAction);
		// c.weightx = 0.0;
		// JButton startButton = new JButton(startAction);
		// panel.add(startButton, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		panel.add(new JLabel("Input Remaining"), c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		inputDisplay.setEditable(false);
		panel.add(inputDisplay, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		panel.add(new JLabel("Stack"), c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		stackDisplay.setEditable(false);
		panel.add(stackDisplay, c);

		bigger.add(panel, BorderLayout.CENTER);
		bigger.add(initInputToolbar(), BorderLayout.NORTH);

		return bigger;
	}

	/**
	 * Returns the tool bar for the main user input panel.
	 *
	 * @return the tool bar for the main user input panel
	 */
	protected JToolBar initInputToolbar() {
		final JToolBar toolbar = new JToolBar();
		toolbar.add(startAction);
		stepAction.setEnabled(false);
		toolbar.add(stepAction);

		// Set up the view customizer controls.
		toolbar.addSeparator();

		final JComboBox<?> box = new JComboBox<Object>(getViewChoices());
		box.setSelectedIndex(0);
		final ActionListener listener = e -> changeView((String) box.getSelectedItem());
		box.addActionListener(listener);
		toolbar.add(box);
		return toolbar;
	}

	/**
	 * Inits a parse table.
	 *
	 * @return a table to hold the parse table
	 */
	protected abstract JTable initParseTable();

	/**
	 * Inits a new tree panel.
	 *
	 * @return a new display for a parse tree
	 */
	protected JComponent initTreePanel() {
		treeDrawer.hideAll();
		treeDrawer.setNodePlacer(new LeafNodePlacer());
		return treePanel;
	}

	/**
	 * Initializes the GUI.
	 */
	protected void initView() {
		treePanel = initTreePanel();

		// Sets up the displays.
		final JComponent pt = initParseTable();
		final JScrollPane parseTable = pt == null ? null : new JScrollPane(pt);
		final GrammarTable g = initGrammarTable(grammar);
		final JScrollPane grammarTable = new JScrollPane(g);

		treeDerivationPane.add(initTreePanel(), "0");
		derivationPane = new JScrollPane(initDerivationTable());
		treeDerivationPane.add(derivationPane, "1");
		bottomSplit = SplitPaneFactory.createSplit(environment, true, 0.3, grammarTable, treeDerivationPane);
		topSplit = SplitPaneFactory.createSplit(environment, true, 0.4, parseTable, initInputPanel());
		mainSplit = SplitPaneFactory.createSplit(environment, false, 0.3, topSplit, bottomSplit);
		add(mainSplit, BorderLayout.CENTER);
		add(statusDisplay, BorderLayout.SOUTH);
		add(new TableTextSizeSlider(g), BorderLayout.NORTH);
	}

	/**
	 * This method is called when there is new input to parse.
	 *
	 * @param string
	 *            a new input string
	 */
	protected abstract void input(String string);

	/**
	 * Children are not painted here.
	 *
	 * @param g
	 *            the graphics object to paint to
	 */
	@Override
	public void printChildren(final Graphics g) {
	}

	/**
	 * Prints this component. This will print only the tree section of the
	 * component.
	 *
	 * @param g
	 *            the graphics object to print to
	 */
	@Override
	public void printComponent(final Graphics g) {
		treeDerivationPane.print(g);
	}

	/**
	 * This method is called when the step button is pressed.
	 */
	protected abstract boolean step();
}
