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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.parse.LLParseTable;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.grammar.GrammarTable;

/**
 * This is the view for the derivation of a LL parse table from a grammar.
 *
 * @author Thomas Finley
 */
public class LLParseTableDerivationPane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private LLParseDerivationController controller;

	private LLParseTablePane parseTable;

	/**
	 * Instantiates a new derivation pane for a grammar environment.
	 *
	 * @param environment
	 *            the grammar environment
	 */
	public LLParseTableDerivationPane(final GrammarEnvironment environment) {
		super(new BorderLayout());
		final Grammar g = environment.getGrammar();
		final JPanel right = new JPanel(new BorderLayout());

		final JLabel description = new JLabel();
		right.add(description, BorderLayout.NORTH);

		// FirstFollowModel ffmodel = new FirstFollowModel(g);
		final FirstFollowTable fftable = new FirstFollowTable(g);
		fftable.getColumnModel().getColumn(0).setPreferredWidth(30);
		fftable.getFFModel().setCanEditFirst(true);
		fftable.getFFModel().setCanEditFollow(true);

		final LLParseTable parseTableModel = new LLParseTable(g) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(final int r, final int c) {
				return controller.step != LLParseDerivationController.FINISHED && super.isCellEditable(r, c);
			}
		};
		parseTable = new LLParseTablePane(parseTableModel);
		parseTable.getColumnModel().getColumn(0).setPreferredWidth(30);

		final JSplitPane rightSplit = SplitPaneFactory.createSplit(environment, false, 0.5, new JScrollPane(fftable),
				new JScrollPane(parseTable));
		right.add(rightSplit, BorderLayout.CENTER);

		controller = new LLParseDerivationController(g, environment, fftable, parseTable, description);

		final GrammarTable table = new GrammarTable(new edu.duke.cs.jflap.gui.grammar.GrammarTableModel(g) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(final int r, final int c) {
				return false;
			}
		});
		final JSplitPane pane = SplitPaneFactory.createSplit(environment, true, 0.3, table, right);
		this.add(pane, BorderLayout.CENTER);

		// Make the tool bar.
		final JToolBar toolbar = new JToolBar();
		toolbar.add(controller.doSelectedAction);
		toolbar.add(controller.doStepAction);
		toolbar.add(controller.doAllAction);
		toolbar.addSeparator();
		toolbar.add(controller.nextAction);
		toolbar.addSeparator();
		toolbar.add(controller.parseAction);
		this.add(toolbar, BorderLayout.NORTH);
	}

	void makeParseUneditable() {
		try {
			parseTable.getCellEditor().stopCellEditing();
		} catch (final NullPointerException e) {
		}
	}
}
