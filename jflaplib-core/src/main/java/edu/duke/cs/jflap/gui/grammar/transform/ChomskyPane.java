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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import edu.duke.cs.jflap.grammar.CNFConverter;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.grammar.GrammarTable;
import edu.duke.cs.jflap.gui.grammar.GrammarTableModel;

/**
 * The pane for converting a grammar to Chomsky normal form.
 *
 * @author Thomas Finley
 */
public class ChomskyPane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The environment. */
	GrammarEnvironment environment;

	/** The grammar. */
	Grammar grammar;

	/** The converter object. */
	CNFConverter converter;

	/**
	 * The array of rows that need to be done. This will be updated every
	 * turn... I guess.
	 */
	private List<Integer> need = new ArrayList<>();

	/** The grammar table. */
	GrammarTable grammarTable;

	/** The grammar table. */
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

	/** The grammar table. */
	GrammarTable editingGrammarView = new GrammarTable(editingGrammarModel);

	/** The main label. */
	JLabel mainLabel = new JLabel(" ");

	/** The direction label. */
	JLabel directionLabel = new JLabel(" ");

	/** The convert action. */
	AbstractAction convertAction = new AbstractAction("Convert Selected") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			convertSelected();
		}
	};

	/** The do all action. */
	AbstractAction doAllAction = new AbstractAction("Do All") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			doAll();
		}
	};

	/** The highlight remaining action. */
	AbstractAction highlightAction = new AbstractAction("What's Left?") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			highlightRemaining();
		}
	};

	/** The export action. */
	AbstractAction exportAction = new AbstractAction("Export") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			export();
		}
	};

	/**
	 * Instantiates a Chomsky pane.
	 *
	 * @param environment
	 *            the environment that this pane will become a part of
	 * @param grammar
	 *            the grammar to convert
	 */
	public ChomskyPane(final GrammarEnvironment environment, final Grammar grammar) {
		this.environment = environment;
		this.grammar = grammar;
		converter = new CNFConverter(grammar);
		mainLabel.setText("Welcome to the Chomsky converter.");
		mainLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent event) {
				if (event.getClickCount() > 10) {
					mainLabel.setText("Click on me again, and I'll kick your ass.");
				}
			}
		});
		initView();
		updateDisplay();
	}

	/**
	 * Converts the selected rows.
	 */
	private void convertSelected() {
		if (!convertAction.isEnabled()) {
			return;
		}
		final int[] r = editingGrammarView.getSelectedRows();
		int unneeded = 0;
		final ArrayList<Integer> list = new ArrayList<>();
		editingGrammarView.dehighlight();

		for (int i = r.length - 1; i >= 0; i--) {
			final Production p = editingGrammarModel.getProduction(r[i]);
			if (p == null) {
				return;
			}
			List<Production> ps = null;
			try {
				ps = converter.replacements(p);
			} catch (final IllegalArgumentException e) {
				unneeded++;
				continue;
			}
			editingGrammarModel.deleteRow(r[i]);
			for (int j = ps.size() - 1; j >= 0; j--) {
				editingGrammarModel.addProduction(ps.get(j), r[i]);
				final Integer integer = new Integer(r[i]);
				list.add(0, integer);
			}
		}
		if (unneeded > 0) {
			JOptionPane.showMessageDialog(this, "Conversion unneeded on " + unneeded + " production(s).\n"
					+ (r.length - unneeded) + " production(s) converted.", "Conversion Unneeded",
					JOptionPane.ERROR_MESSAGE);
		}
		int last = -1, adjust = 0;
		for (int i = 0; i < list.size(); i++) {
			int toHighlight = list.get(i).intValue() + i;
			if (last != -1 && last != toHighlight - 1) {
				adjust++;
			}
			last = toHighlight;
			toHighlight -= adjust;
			editingGrammarView.highlight(toHighlight);
		}
		if (list.size() != 0) {
			editingGrammarView.repaint();
			mainLabel.setText("Replacement production(s) highlighted.");
		}
		updateDisplay();
	}

	/**
	 * Does everything.
	 */
	public void doAll() {
		final ListSelectionModel model = editingGrammarView.getSelectionModel();
		while (need.size() != 0) {
			model.clearSelection();
			need.forEach(i -> model.addSelectionInterval(i, i));
			convertSelected();
		}
		mainLabel.setText("All productions completed.");
		editingGrammarView.dehighlight();
	}

	/**
	 * Takes the grammar, and attempts to export it.
	 */
	private void export() {
		List<Production> p = editingGrammarModel.getProductions();
		/*
		 * System.out.println("PRINTTITTING"); for (int i=0; i<p.length; i++) {
		 * System.out.println(p[i]); }
		 */
		try {
			p = CNFConverter.convert(p);
		} catch (final UnsupportedOperationException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			final Grammar g = grammar.getClass().newInstance();
			g.addProductions(p);
			g.setStartVariable(grammar.getStartVariable());
			FrameFactory.createFrame(g);
		} catch (final Throwable e) {
			System.err.println(e);
		}
	}

	public Grammar getGrammar() {
		List<Production> p = editingGrammarModel.getProductions();
		try {
			p = CNFConverter.convert(p);
		} catch (final UnsupportedOperationException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "CNF Conversion Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		try {
			final Grammar g = grammar.getClass().newInstance();
			g.addProductions(p);
			g.setStartVariable(grammar.getStartVariable());
			return g;
		} catch (final Throwable e) {
			System.err.println(e);
		}
		return null;
	}

	/**
	 * Returns the array of rows that need further reduction.
	 *
	 * @return an array of row indices that need reduction
	 */
	private List<Integer> getWhatNeedsDone() {
		final ArrayList<Integer> list = new ArrayList<>();
		for (int i = 0; i < editingGrammarModel.getRowCount() - 1; i++) {
			if (!converter.isChomsky(editingGrammarModel.getProduction(i))) {
				list.add(new Integer(i));
			}
		}
		return list;
	}

	/**
	 * Highlights the remaining rows.
	 */
	private void highlightRemaining() {
		editingGrammarView.dehighlight();
		mainLabel.setText("Productions to convert are selected.");
		need.forEach(i -> editingGrammarView.highlight(i));
	}

	/**
	 * Initializes the editing grammar view.
	 */
	private void initEditingGrammarTable() {
		editingGrammarView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					convertSelected();
				}
			}
		});
		grammar.getProductions().forEach(prod -> editingGrammarModel.addProduction(prod));
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
	 *
	 * @return an initialized right panel
	 */
	private JPanel initRightPanel() {
		final JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		initEditingGrammarTable();
		mainLabel.setAlignmentX(0.0f);
		directionLabel.setAlignmentX(0.0f);
		right.add(mainLabel);
		right.add(directionLabel);
		right.add(new JScrollPane(editingGrammarView));

		final JPanel biggie = new JPanel(new BorderLayout());
		biggie.add(right, BorderLayout.CENTER);
		final JToolBar bar = new JToolBar();
		bar.add(convertAction);
		bar.add(doAllAction);
		bar.add(highlightAction);
		bar.addSeparator();
		bar.add(exportAction);
		biggie.add(bar, BorderLayout.NORTH);
		return biggie;
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
		add(mainSplit, BorderLayout.CENTER);
	}

	/**
	 * Updates the display.
	 */
	private void updateDisplay() {
		need = getWhatNeedsDone();
		final boolean done = need.size() == 0;
		convertAction.setEnabled(!done);
		doAllAction.setEnabled(!done);
		highlightAction.setEnabled(!done);
		exportAction.setEnabled(done);
		if (done) {
			directionLabel.setText("Conversion done.  Press \"Export\" to use.");
		} else {
			directionLabel.setText(need.size() + " production(s) must be converted.");
		}
	}
}
