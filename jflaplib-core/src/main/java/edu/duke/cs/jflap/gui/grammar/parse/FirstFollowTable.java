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

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.gui.LeftTable;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * This table is an table specifically for the <CODE>FirstFollowModel</CODE> for
 * handling user entry of first and follow sets.
 *
 * @author Thomas Finley
 */
public class FirstFollowTable extends LeftTable {
	/**
	 * The modified table cell renderer.
	 */
	private static class SetsCellRenderer extends DefaultTableCellRenderer {
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
			l.setText(getSetString((String) value));
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
				renderer = new SetsCellRenderer();
				renderer.setBackground(new Color(255, 150, 150));
			}
			return renderer;
		}
	};

	/** The sets cell renderer. */
	private static final TableCellRenderer RENDERER = new SetsCellRenderer();

	/**
	 * Converts a string to a set string.
	 */
	private static String getSetString(final String s) {
		if (s == null) {
			return "{ }";
		}
		final StringBuffer sb = new StringBuffer("{ ");
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '!') {
				c = Universe.curProfile.getEmptyString().charAt(0);
			}
			sb.append(c);
			if (i != s.length() - 1) {
				sb.append(',');
			}
			sb.append(' ');
		}
		sb.append('}');
		return sb.toString();
	}

	/** The table model. */
	private final FirstFollowModel model;

	/**
	 * Instantiates a new first follow table for a grammar.
	 *
	 * @param grammar
	 *            the grammar to create the table
	 */
	public FirstFollowTable(final Grammar grammar) {
		super(new FirstFollowModel(grammar));
		model = (FirstFollowModel) getModel();

		getColumnModel().getColumn(1).setCellRenderer(RENDERER);
		getColumnModel().getColumn(2).setCellRenderer(RENDERER);

		setCellSelectionEnabled(true);
	}

	/**
	 * Returns the first follow table model.
	 *
	 * @return the table model
	 */
	public FirstFollowModel getFFModel() {
		return model;
	}

	/** Modified to use the set renderer highlighter. */
	@Override
	public void highlight(final int row, final int column) {
		highlight(row, column, THRG);
	}
}
