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
import java.util.ArrayList;
import java.util.Collections;
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
import javax.swing.event.TableModelEvent;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.TableTextSizeSlider;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.grammar.GrammarTable;
import edu.duke.cs.jflap.gui.grammar.GrammarTableModel;

/**
 * This is the pane where the removal of lambda productions takes place.
 *
 * @author Thomas Finley
 */
public class LambdaPane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The grammar environment. */
	GrammarEnvironment environment;

	/** The grammar to remove lambdas on. */
	Grammar grammar;

	/** The controller object. */
	LambdaController controller;

	/** The grammar table. */
	GrammarTable grammarTable;

	/** The main instruction label. */
	JLabel mainLabel = new JLabel(" ");

	/** The detail instruction label. */
	JLabel detailLabel = new JLabel(" ");

	/** The lambda deriving variable labels. */
	JLabel lambdaDerivingLabel = new JLabel(" ");

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

	AbstractAction proceedAction = new AbstractAction("Proceed") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			edu.duke.cs.jflap.gui.action.GrammarTransformAction.hypothesizeUnit(environment, getGrammar());
		}
	};

	// These are some of the data structures relevant.

	AbstractAction exportAction = new AbstractAction("Export") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			FrameFactory.createFrame(editingGrammarView.getGrammar(grammar.getClass()));
		}
	};

	/** Simple kludge to allow us to add stuff to the table without fear. */
	boolean editingActive = false;

	/** The editing row in the table. */
	private int editingRow = -1;

	// These are some of the graphical elements.

	/** Which columsn of the editing row have been edited yet? */
	private final List<Boolean> editingColumn = new ArrayList<>(Collections.nCopies(2, false));

	/** The editing grammar table mode. */
	GrammarTableModel editingGrammarModel = new GrammarTableModel() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(final int r, final int c) {
			if (controller.step != LambdaController.PRODUCTION_MODIFY) {
				return false;
			}
			if (c == 1) {
				return false;
			}
			if (editingRow == -1) {
				if (r == getRowCount() - 1) {
					editingRow = r;
					editingColumn.set(0, false);
					editingColumn.set(1, false);
					return true;
				}
				return false;
			} else {
				return editingRow == r;
			}
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

	/** The complete selected action. */
	AbstractAction completeSelectedAction = new AbstractAction("Complete Selected") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			cancelEditing();
			for (int i = 0; i < grammarTable.getRowCount() - 1; i++) {
				if (grammarTable.isRowSelected(i)) {
					controller.expandRowProduction(i);
				}
			}
		}
	};

	/**
	 * Instantiates a new lambda pane.
	 *
	 * @param environment
	 *            the grammar environment this pane will belong to
	 * @param grammar
	 *            the grammar to do the lambda removal on
	 */
	public LambdaPane(final GrammarEnvironment environment, final Grammar grammar) {
		this.environment = environment;
		this.grammar = grammar;
		controller = new LambdaController(this, grammar);
		initView();
	}

	/**
	 * Calling this method discontinues any editing taking place.
	 */
	void cancelEditing() {
		if (editingGrammarView.getCellEditor() != null) {
			editingGrammarView.getCellEditor().stopCellEditing();
		}
		if (editingRow != -1) {
			editingGrammarModel.deleteRow(editingRow);
			editingRow = -1;
		}
	}

	/**
	 * This method should be called when the deletion method is called.
	 */
	private void deleteActivated() {
		if (controller.step != LambdaController.PRODUCTION_MODIFY) {
			return;
		}
		cancelEditing();
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

	// These are some of the special structures relevant to the
	// grammar editing table.

	/**
	 * Returns the grammar that results.
	 *
	 * @return the grammar that results
	 */
	public Grammar getGrammar() {
		return editingGrammarView.getGrammar(grammar.getClass());
	}

	/**
	 * Initializes the editing grammar view.
	 */
	private void initEditingGrammarTable() {
		editingGrammarModel.addTableModelListener(event -> {
			if (!editingActive) {
				return;
			}
			// cancelEditing();
			final int r = event.getFirstRow();
			if (event.getType() != TableModelEvent.UPDATE) {
				// If we're editing anything, we have to get
				// out of the funk.
				return;
			}
			editingColumn.set(event.getColumn() >> 1, true);
			if (editingColumn.get(0) && editingColumn.get(1)) {
				final Production p = editingGrammarModel.getProduction(r);
				if (p == null) {
					return;
				}
				if (!controller.productionAdded(p, r)) {
					editingGrammarModel.deleteRow(r);
				}
				editingRow = -1;
			}
		});
		editingGrammarView.getSelectionModel().addListSelectionListener(event -> updateDeleteEnabledness());
		final Object o = new Object();
		editingGrammarView.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), o);
		editingGrammarView.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), o);
		editingGrammarView.getActionMap().put(o, deleteAction);
	}

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
		grammarTable.getSelectionModel().addListSelectionListener(event -> updateCompleteSelectedEnabledness());
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
		lambdaDerivingLabel.setAlignmentX(0.0f);

		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(mainLabel);
		panel.add(detailLabel);
		panel.add(lambdaDerivingLabel);
		initEditingGrammarTable();

		final JToolBar editingBar = new JToolBar();
		editingBar.setAlignmentX(0.0f);
		editingBar.setFloatable(false);
		editingBar.add(deleteAction);
		editingBar.add(completeSelectedAction);
		panel.add(editingBar);

		panel.add(new JScrollPane(editingGrammarView));

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
		this.add(new TableTextSizeSlider(grammarTable), BorderLayout.NORTH);
	}

	/**
	 * Updates the complete selected action enabledness.
	 */
	void updateCompleteSelectedEnabledness() {
		if (controller.step != LambdaController.PRODUCTION_MODIFY) {
			completeSelectedAction.setEnabled(false);
			return;
		}
		final int min = grammarTable.getSelectionModel().getMinSelectionIndex();
		if (min == -1 || min >= grammarTable.getGrammarModel().getRowCount() - 1) {
			completeSelectedAction.setEnabled(false);
			return;
		}
		completeSelectedAction.setEnabled(true);
	}

	/**
	 * Updates the delete action enabledness.
	 */
	void updateDeleteEnabledness() {
		if (controller.step != LambdaController.PRODUCTION_MODIFY) {
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
