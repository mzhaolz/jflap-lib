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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import edu.duke.cs.jflap.grammar.parse.LLParseTable;
import edu.duke.cs.jflap.gui.LeftTable;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * This holds a LL parse table.
 *
 * @author Thomas Finley
 */
public class LLParseTablePane extends LeftTable {
	/**
	 * The modified table cell renderer.
	 */
	private static class LambdaCellRenderer extends DefaultTableCellRenderer {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
				final boolean hasFocus, final int row, final int column) {
			final JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);
			if (hasFocus && table.isCellEditable(row, column)) {
				return l;
			}
			l.setText(((String) value).replace('!', Universe.curProfile.getEmptyString().charAt(0)));
			return l;
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The built in highlight renderer generator. */
	private static final edu.duke.cs.jflap.gui.HighlightTable.TableHighlighterRendererGenerator THRG = new TableHighlighterRendererGenerator() {
		private DefaultTableCellRenderer renderer = null;

		@Override
		public TableCellRenderer getRenderer(final int row, final int column) {
			if (renderer == null) {
				renderer = new LambdaCellRenderer();
				renderer.setBackground(new Color(255, 150, 150));
			}
			return renderer;
		}
	};

	/** The sets cell renderer. */
	private static final TableCellRenderer RENDERER = new LambdaCellRenderer();

	/** The parse table for this pane. */
	private final LLParseTable table;

	/**
	 * Instantiates a new parse table pane for a parse table.
	 *
	 * @param table
	 *            the table pane's parse table
	 */
	public LLParseTablePane(final LLParseTable table) {
		super(table);
		this.table = table;
		setCellSelectionEnabled(true);

		for (int i = 1; i < getColumnCount(); i++) {
			getColumnModel().getColumn(i).setCellRenderer(RENDERER);
		}
	}

	/**
	 * Retrieves the parse table in this pane.
	 *
	 * @return the parse table in this pane
	 */
	public LLParseTable getParseTable() {
		return table;
	}

	/** Modified to use the set renderer highlighter. */
	@Override
	public void highlight(final int row, final int column) {
		highlight(row, column, THRG);
	}
}
