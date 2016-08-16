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

package edu.duke.cs.jflap.gui.grammar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import edu.duke.cs.jflap.debug.EDebug;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.gui.HighlightTable;
import edu.duke.cs.jflap.gui.TableTextSizeSlider;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * The <CODE>GrammarTable</CODE> is a simple extension to the
 * <CODE>JTable</CODE> that standardizes how grammar tables look.
 *
 * @author Thomas Finley
 */
public class GrammarTable extends HighlightTable {
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
			if (column != 2) {
				return l;
			}
			if (!value.equals("")) {
				return l;
			}
			if (table.getModel().getValueAt(row, 0).equals("")) {
				return l;
			}
			l.setText(Universe.curProfile.getEmptyString());
			return l;
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The built in highlight renderer generator, modified. */
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

	/** The lambda cell renderer. */
	private static final TableCellRenderer RENDERER = new LambdaCellRenderer();

	/**
	 * Instantiates a <CODE>GrammarTable</CODE> with an empty grammar.
	 */
	public GrammarTable() {
		super(new GrammarTableModel());
		initView();
	}

	/**
	 * Instantiates a <CODE>GrammarTable</CODE> with a given table model.
	 *
	 * @param model
	 *            the table model for the new grammar table
	 */
	public GrammarTable(final GrammarTableModel model) {
		super(model);
		initView();
	}

	/**
	 * Returns the grammar that has been defined through this
	 * <CODE>GrammarTable</CODE>, where the grammar is an instance of the class
	 * passed into this function.
	 *
	 * @param grammarClass
	 *            the type of grammar that is passed in
	 * @return a grammar of the variant returned by this grammar, or
	 *         <CODE>null</CODE> if some sort of error with a production is
	 *         encountered
	 * @throws IllegalArgumentException
	 *             if the grammar class passed in could not be instantiated with
	 *             an empty constructor, or is not even a subclass of
	 *             <CODE>Grammar</CODE>.
	 */
	public Grammar getGrammar(final Class<?> grammarClass) {
		Grammar grammar = null;
		try {
			grammar = (Grammar) grammarClass.newInstance();
		} catch (final NullPointerException e) {
			EDebug.print("Throwing a Null Pointer Back at YOU.");
			throw e;
		} catch (final Throwable e) {
			throw new IllegalArgumentException("Bad grammar class " + grammarClass);
		}
		final GrammarTableModel model = getGrammarModel();
		// Make sure we're not editing anything anymore.
		if (getCellEditor() != null) {
			getCellEditor().stopCellEditing();
		}
		// Add the productions.
		for (int row = 0; row < model.getRowCount(); row++) {
			final Production p = model.getProduction(row);
			if (p == null) {
				continue;
			}
			try {
				grammar.addProduction(p);
				if (grammar.getStartVariable() == null) {
					grammar.setStartVariable(p.getLHS());
				}
			} catch (final IllegalArgumentException e) {
				setRowSelectionInterval(row, row);
				JOptionPane.showMessageDialog(this, e.getMessage(), "Production Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		return grammar;
	}

	/**
	 * Returns the model for this grammar table.
	 *
	 * @return the grammar table model for this table
	 */
	public GrammarTableModel getGrammarModel() {
		return (GrammarTableModel) super.getModel();
	}

	/**
	 * Handles the highlighting of a particular row.
	 *
	 * @param row
	 *            the row to highlight
	 */
	public void highlight(final int row) {
		highlight(row, 0);
		highlight(row, 2);
	}

	/** Modified to use the set renderer highlighter. */
	@Override
	public void highlight(final int row, final int column) {
		highlight(row, column, THRG);
	}

	/**
	 * This constructor helper function customizes the view of the table.
	 */
	private void initView() {
		setTableHeader(new JTableHeader(getColumnModel()));
		getTableHeader().setReorderingAllowed(false);
		getTableHeader().setResizingAllowed(true);
		final TableColumn lhs = getColumnModel().getColumn(0);
		final TableColumn arrows = getColumnModel().getColumn(1);
		final TableColumn rhs = getColumnModel().getColumn(2);
		lhs.setHeaderValue("LHS");
		getTableHeader().resizeAndRepaint();
		rhs.setHeaderValue("RHS");
		getTableHeader().resizeAndRepaint();
		getColumnModel().getColumn(0).setPreferredWidth(70);
		lhs.setMaxWidth(200);
		// lhs.setMinWidth(200);
		arrows.setMaxWidth(30);
		arrows.setMinWidth(30);
		getColumnModel().getColumn(1).setPreferredWidth(30);
		setShowGrid(true);
		setGridColor(Color.lightGray);
		rowHeight = 30;
		setFont(new Font("TimesRoman", Font.PLAIN, 20));

		getColumnModel().getColumn(2).setCellRenderer(RENDERER);
		add(new TableTextSizeSlider(this), BorderLayout.NORTH);
	}
}
