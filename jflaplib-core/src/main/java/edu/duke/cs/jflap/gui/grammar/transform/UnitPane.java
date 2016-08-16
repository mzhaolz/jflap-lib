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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
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
import javax.swing.event.TableModelEvent;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
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
 * This is the pane where the removal of unit productions takes place.
 *
 * @author Thomas Finley
 */
public class UnitPane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The grammar environment. */
	GrammarEnvironment environment;

	/** The grammar to remove unit productions on. */
	Grammar grammar;

	/** The controller object. */
	UnitController controller;

	/** The grammar table. */
	GrammarTable grammarTable;

	/** The main instruction label. */
	JLabel mainLabel = new JLabel(" ");

	/** The detail instruction label. */
	JLabel detailLabel = new JLabel(" ");

	/** The editor pane. */
	EditorPane vdgEditor;

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

	// These are some of the data structures relevant.

	AbstractAction proceedAction = new AbstractAction("Proceed") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			GrammarTransformAction.hypothesizeUseless(environment, getGrammar());
		}
	};

	AbstractAction exportAction = new AbstractAction("Export") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			FrameFactory.createFrame(getGrammar());
		}
	};

	/** Simple kludge to allow us to add stuff to the table without fear. */
	boolean editingActive = false;

	// These are some of the graphical elements.

	/** The editing row in the table. */
	private int editingRow = -1;

	/** Which columns of the editing row have been edited yet? */
	private final List<Boolean> editingColumn = new ArrayList<>(Collections.nCopies(2, false));

	/** The editing grammar table mode. */
	GrammarTableModel editingGrammarModel = new GrammarTableModel() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(final int r, final int c) {
			if (!editingActive) {
				return false;
			}
			if (controller.step != UnitController.PRODUCTION_MODIFY) {
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
			controller.doSelected();
		}
	};

	/**
	 * Instantiates a new unit removing pane.
	 *
	 * @param environment
	 *            the grammar environment this pane will belong to
	 * @param grammar
	 *            the grammar to do the unit removal on
	 */
	public UnitPane(final GrammarEnvironment environment, final Grammar grammar) {
		this.environment = environment;
		this.grammar = grammar;
		controller = new UnitController(this, grammar);
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
		if (controller.step != UnitController.PRODUCTION_MODIFY) {
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
	 * Returns a nice sorted grammar.
	 */
	public Grammar getGrammar() {
		final Grammar g = editingGrammarView.getGrammar(grammar.getClass());
		final List<Production> p = g.getProductions();
		final String S = grammar.getStartVariable();
		p.sort((p1, p2) -> {
			if (S.equals(p1.getLHS())) {
				if (p1.getLHS().equals(p2.getLHS())) {
					return 0;
				} else {
					return -1;
				}
			}
			if (S.equals(p2.getLHS())) {
				return 1;
			}
			return p1.getLHS().compareTo(p2.getRHS());
		});
		Grammar g2 = null;
		try {
			g2 = g.getClass().newInstance();
			g2.addProductions(p);
			g2.setStartVariable(S);
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
		editingGrammarModel.addTableModelListener(event -> {
			if (!editingActive) {
				return;
			}
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
		/*
		 * grammarTable.getSelectionModel().addListSelectionListener (new
		 * ListSelectionListener() { public void valueChanged(ListSelectionEvent
		 * event) { updateCompleteSelectedEnabledness(); }});
		 */
		editingGrammarView.getSelectionModel().addListSelectionListener(event -> {
			updateDeleteEnabledness();
			updateCompleteSelectedEnabledness();
		});
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
		initEditingGrammarTable();

		// Sets up the editor pane.

		vdgDrawer = new SelectionDrawer(controller.vdg);
		vdgEditor = new EditorPane(vdgDrawer, (view, drawer) -> {
			final List<Tool> t = new LinkedList<>();
			t.add(new ArrowNontransitionTool(view, drawer) {
				@Override
				public void mouseClicked(final MouseEvent e) {
					super.mouseClicked(e);
					final State s = vdgDrawer.stateAtPoint(e.getPoint());
					if (controller.step == UnitController.PRODUCTION_MODIFY) {
						controller.stateClicked(s, e);
					}
				}
			});
			t.add(new TransitionTool(view, drawer));
			return t;
		}, true);
		// Grammar editor?
		final JPanel grammarEditor = new JPanel(new BorderLayout());
		final JToolBar editingBar = new JToolBar();
		editingBar.setAlignmentX(0.0f);
		editingBar.setFloatable(false);
		editingBar.add(deleteAction);
		editingBar.add(completeSelectedAction);
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
	 * Updates the complete selected action enabledness.
	 */
	void updateCompleteSelectedEnabledness() {
		if (controller.step != UnitController.PRODUCTION_MODIFY) {
			completeSelectedAction.setEnabled(false);
			return;
		}
		final int min = editingGrammarView.getSelectionModel().getMinSelectionIndex();
		if (min == -1 || min >= editingGrammarModel.getRowCount() - 1) {
			completeSelectedAction.setEnabled(false);
			return;
		}
		completeSelectedAction.setEnabled(true);
	}

	/**
	 * Updates the delete action enabledness.
	 */
	void updateDeleteEnabledness() {
		if (controller.step != UnitController.PRODUCTION_MODIFY) {
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
