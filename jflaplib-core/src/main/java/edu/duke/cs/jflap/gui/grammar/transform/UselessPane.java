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

package edu.duke.cs.jflap.gui.grammar.transform;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.ProductionComparator;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.action.GrammarTransformAction;
import edu.duke.cs.jflap.gui.editor.ArrowNontransitionTool;
import edu.duke.cs.jflap.gui.editor.EditorPane;
import edu.duke.cs.jflap.gui.editor.Tool;
import edu.duke.cs.jflap.gui.editor.TransitionTool;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.grammar.GrammarTable;
import edu.duke.cs.jflap.gui.grammar.GrammarTableModel;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * This is the pane where the removal of useless productions takes place.
 *
 * @author Thomas Finley
 */
public class UselessPane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The grammar environment. */
	GrammarEnvironment environment;

	/** The grammar to remove useless productions on. */
	Grammar grammar;

	/** The controller object. */
	UselessController controller;

	/** The grammar table. */
	GrammarTable grammarTable;

	/** The main instruction label. */
	JLabel mainLabel = new JLabel(" ");

	/** The detail instruction label. */
	JLabel detailLabel = new JLabel(" ");

	/** The terminal introduction label. */
	JLabel terminalLabel = new JLabel(" ");

	/** The pane where */
	/** The editor pane. */
	EditorPane vdgEditor;

	// These are some of the data structures relevant.

	/** The vdg drawer. */
	SelectionDrawer vdgDrawer;

	// These are general controls.
	AbstractAction doStepAction = new AbstractAction("Do Step") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			controller.doStep();
		}
	};

	AbstractAction doAllAction = new AbstractAction("Do All") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			controller.doAll();
		}
	};

	// These are some of the graphical elements.

	AbstractAction proceedAction = new AbstractAction("Proceed") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Grammar g = getGrammar();
			if (g == null) {
				JOptionPane.showMessageDialog(environment, "The grammar is empty.  Cannot proceed.", "Bad Grammar",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			GrammarTransformAction.hypothesizeChomsky(environment, getGrammar());
		}
	};

	AbstractAction exportAction = new AbstractAction("Export") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Grammar g = getGrammar();
			if (g == null) {
				JOptionPane.showMessageDialog(environment, "The grammar is empty.  Cannot proceed.", "Bad Grammar",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			FrameFactory.createFrame(getGrammar());
		}
	};

	/** Simple kludge to allow us to add stuff to the table without fear. */
	boolean editingActive = false;

	/** The editing grammar table mode. */
	GrammarTableModel editingGrammarModel = new GrammarTableModel() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(final int r, final int c) {
			return false;
		}
	};

	/** The editing grammar table view. */
	GrammarTable editingGrammarView = new GrammarTable(editingGrammarModel);

	/** The delete action for deleting rows. */
	AbstractAction deleteAction = new AbstractAction("Delete") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			deleteActivated();
		}
	};

	/**
	 * Instantiates a new useless production removing pane.
	 *
	 * @param environment
	 *            the grammar environment this pane will belong to
	 * @param grammar
	 *            the grammar to remove useless productions from
	 */
	public UselessPane(final GrammarEnvironment environment, final Grammar grammar) {
		this.environment = environment;
		this.grammar = grammar;
		controller = new UselessController(this, grammar);
		initView();
	}

	/**
	 * This method should be called when the deletion method is called.
	 */
	private void deleteActivated() {
		if (controller.step != UselessController.PRODUCTION_MODIFY) {
			return;
		}
		int deleted = 0, kept = 0;
		for (int i = editingGrammarModel.getRowCount() - 2; i >= 0; i--) {
			if (!editingGrammarView.isRowSelected(i)) {
				continue;
			}
			final Production p = editingGrammarModel.getProduction(i);
			if (controller.productionDeleted(p, i)) {
				editingGrammarModel.deleteRow(i);
				deleted++;
			} else {
				kept++;
			}
		}
		if (kept != 0) {
			JOptionPane.showMessageDialog(this, kept + " production(s) selected should not be removed.\n" + deleted
					+ " production(s) were removed.", "Bad Selection", JOptionPane.ERROR_MESSAGE);
		}
		if (deleted != 0) {
			controller.updateDisplay();
		}
	}

	/**
	 * Returns a nice sorted grammar.
	 */
	public Grammar getGrammar() {
		final Grammar g = editingGrammarView.getGrammar(grammar.getClass());
		final List<Production> p = g.getProductions();
		p.sort(new ProductionComparator(grammar));
		if (p.size() == 0 || !p.get(0).getLHS().equals(grammar.getStartVariable())) {
			return null;
		}
		Grammar g2 = null;
		try {
			g2 = g.getClass().newInstance();
			g2.addProductions(p);
			g2.setStartVariable(grammar.getStartVariable());
		} catch (final Throwable e) {
			System.err.println("BADNESS!");
			System.err.println(e);
			return g2;
		}
		return g2;
	}

	/**
	 * Initializes the editing grammar view.
	 */
	private void initEditingGrammarTable() {
		editingGrammarView.getSelectionModel().addListSelectionListener(event -> updateDeleteEnabledness());
		final Object o = new Object();
		editingGrammarView.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), o);
		editingGrammarView.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), o);
		editingGrammarView.getActionMap().put(o, deleteAction);
	}

	// These are some of the special structures relevant to the
	// grammar editing table.

	/**
	 * Initializes a table for the grammar.
	 *
	 * @return a table to display the grammar
	 */
	private GrammarTable initGrammarTable() {
		grammarTable = new GrammarTable(new GrammarTableModel(grammar) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(final int r, final int c) {
				return false;
			}
		});
		grammarTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent event) {
				final GrammarTable gt = (GrammarTable) event.getSource();
				final Point at = event.getPoint();
				final int row = gt.rowAtPoint(at);
				if (row == -1) {
					return;
				}
				if (row == gt.getGrammarModel().getRowCount() - 1) {
					return;
				}
				final Production p = gt.getGrammarModel().getProduction(row);
				controller.productionClicked(p, event);
			}
		});
		return grammarTable;
	}

	/**
	 * Initializes the right panel.
	 */
	private JPanel initRightPanel() {
		final JPanel right = new JPanel(new BorderLayout());

		// Sets the alignments.
		mainLabel.setAlignmentX(0.0f);
		detailLabel.setAlignmentX(0.0f);

		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(mainLabel);
		panel.add(detailLabel);
		panel.add(terminalLabel);
		initEditingGrammarTable();

		// Sets up the editor pane.
		vdgDrawer = new SelectionDrawer(controller.vdg);
		vdgEditor = new EditorPane(vdgDrawer, (view, drawer) -> {
			final List<Tool> t = new LinkedList<>();
			t.add(new ArrowNontransitionTool(view, drawer));
			t.add(new TransitionTool(view, drawer));
			return t;
		}, true);
		// Grammar editor?
		final JPanel grammarEditor = new JPanel(new BorderLayout());
		final JToolBar editingBar = new JToolBar();
		editingBar.setAlignmentX(0.0f);
		editingBar.setFloatable(false);
		editingBar.add(deleteAction);
		grammarEditor.add(editingBar, BorderLayout.NORTH);
		grammarEditor.add(new JScrollPane(editingGrammarView), BorderLayout.CENTER);
		final JSplitPane rightSplit = SplitPaneFactory.createSplit(environment, false, 0.5, vdgEditor, grammarEditor);
		panel.add(rightSplit);

		final JToolBar toolbar = new JToolBar();
		toolbar.setAlignmentX(0.0f);
		toolbar.add(doStepAction);
		toolbar.add(doAllAction);
		toolbar.addSeparator();
		toolbar.add(proceedAction);
		toolbar.add(exportAction);
		right.add(toolbar, BorderLayout.NORTH);

		right.add(panel, BorderLayout.CENTER);

		return right;
	}

	/**
	 * Initializes the GUI components of this pane.
	 */
	private void initView() {
		super.setLayout(new BorderLayout());
		initGrammarTable();
		final JPanel rightPanel = initRightPanel();
		final JSplitPane mainSplit = SplitPaneFactory.createSplit(environment, true, 0.4, new JScrollPane(grammarTable),
				rightPanel);
		this.add(mainSplit, BorderLayout.CENTER);
	}

	/**
	 * Updates the delete action enabledness.
	 */
	void updateDeleteEnabledness() {
		if (controller.step != UselessController.PRODUCTION_MODIFY) {
			deleteAction.setEnabled(false);
			return;
		}
		final int min = editingGrammarView.getSelectionModel().getMinSelectionIndex();
		if (min == -1 || min >= editingGrammarModel.getRowCount() - 1) {
			deleteAction.setEnabled(false);
			return;
		}
		deleteAction.setEnabled(true);
	}
}
