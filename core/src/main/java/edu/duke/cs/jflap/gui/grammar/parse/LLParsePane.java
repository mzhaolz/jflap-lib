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

import javax.swing.JComponent;
import javax.swing.JTable;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.parse.LLParseTable;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.tree.SelectNodeDrawer;

/**
 * This is a parse pane for LL grammars.
 *
 * @author Thomas Finley
 */
public class LLParsePane extends ParsePane {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The parse table. */
	final LLParseTable table;

	/** The parse table panel. */
	LLParseTablePane tablePanel;

	/** The controller object. */
	protected LLParseController controller = new LLParseController(this);

	/** The selection node drawer. */
	SelectNodeDrawer nodeDrawer = new SelectNodeDrawer();

	/**
	 * Instantiaes a new LL parse pane.
	 *
	 * @param environment
	 *            the grammar environment
	 * @param grammar
	 *            the augmented grammar
	 * @param table
	 *            the LL parse table
	 */
	public LLParsePane(final GrammarEnvironment environment, final Grammar grammar, final LLParseTable table) {
		super(environment, grammar);
		this.table = new LLParseTable(table) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(final int r, final int c) {
				return false;
			}
		};
		initView();
	}

	/**
	 * Inits a parse table.
	 *
	 * @return a table to hold the parse table
	 */
	@Override
	protected JTable initParseTable() {
		tablePanel = new LLParseTablePane(table);
		return tablePanel;
	}

	/**
	 * Inits a new tree panel. This overriding adds a selection node drawer so
	 * certain nodes can be highlighted.
	 *
	 * @return a new display for the parse tree
	 */
	@Override
	protected JComponent initTreePanel() {
		treeDrawer.setNodeDrawer(nodeDrawer);
		return super.initTreePanel();
	}

	/**
	 * This method is called when there is new input to parse.
	 *
	 * @param string
	 *            a new input string
	 */
	@Override
	protected void input(final String string) {
		controller.initialize(string);
	}

	/**
	 * This method is called when the step button is pressed.
	 */
	@Override
	protected boolean step() {
		controller.step();
		return true;
	}
}
