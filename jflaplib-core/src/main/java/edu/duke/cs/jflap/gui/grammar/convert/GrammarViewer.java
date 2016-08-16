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

package edu.duke.cs.jflap.gui.grammar.convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.gui.event.SelectionEvent;
import edu.duke.cs.jflap.gui.event.SelectionListener;

/**
 * The <CODE>GrammarViewer</CODE> is a class for the graphical non-editable
 * viewing of grammars, with an extra field for a checkbox to indicate that a
 * production has been "processed," though what exactly that means is left to
 * the context in which this component is used.
 *
 * @author Thomas Finley
 */
public class GrammarViewer extends JTable {
	/**
	 * The model for this table.
	 */
	private class GrammarTableModel extends DefaultTableModel {
		/**
		 *
		 */
		private static final long serialVersionUID = -1542867493683971894L;

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			if (columnIndex == 1) {
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}

		@Override
		public boolean isCellEditable(final int row, final int column) {
			return false;
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 877608254800510232L;

	/** The grammar to display. */
	private final Grammar grammar;

	/** The data of the table. */
	private final List<List<Object>> data;

	/** The mapping of productions to a row (rows stored as Integer). */
	private final Map<Production, Integer> productionToRow = new HashMap<>();

	/** The selection event. */
	private final SelectionEvent EVENT = new SelectionEvent(this);

	/** The set of selection listeners. */
	private final Set<SelectionListener> selectionListeners = new HashSet<>();

	private final ListSelectionListener listSelectListener = e -> distributeSelectionEvent();

	/**
	 * Instantiates a new <CODE>GrammarViewer</CODE>.
	 *
	 * @param grammar
	 *            the grammar to display in this view
	 */
	public GrammarViewer(final Grammar grammar) {
		setModel(new GrammarTableModel());
		this.grammar = grammar;
		// setLayout(new BorderLayout());
		final List<Production> prods = grammar.getProductions();
		data = new ArrayList<>();
		final Object[] columnNames = { "Production", "Created" };

		for (int i = 0; i < prods.size(); i++) {
			final List<Object> dat = new ArrayList<>();
			dat.add(prods.get(i));
			dat.add(Boolean.FALSE);
			data.add(dat);
			productionToRow.put(prods.get(i), new Integer(i));
		}
		final DefaultTableModel model = (DefaultTableModel) getModel();
		// TODO: wtf
		final Object[][] trueData = new Object[prods.size()][2];
		for (int i = 0; i < prods.size(); ++i) {
			for (int j = 0; j < 2; ++j) {
				trueData[i][j] = data.get(i).get(j);
			}
		}
		model.setDataVector(trueData, columnNames);

		// Set the listener to the selectedness.
		getSelectionModel().addListSelectionListener(listSelectListener);
	}

	/**
	 * Adds a selection listener to this grammar viewer. The listener will
	 * receive events whenever the selection changes.
	 *
	 * @param listener
	 *            the selection listener to add
	 */
	public void addSelectionListener(final SelectionListener listener) {
		selectionListeners.add(listener);
	}

	/**
	 * Distributes a selection event.
	 */
	protected void distributeSelectionEvent() {
		final Iterator<SelectionListener> it = selectionListeners.iterator();
		while (it.hasNext()) {
			final SelectionListener listener = it.next();
			listener.selectionChanged(EVENT);
		}
	}

	/**
	 * Returns the <CODE>Grammar</CODE> that this <CODE>GrammarViewer</CODE>
	 * displays.
	 *
	 * @return this viewer's grammar
	 */
	public Grammar getGrammar() {
		return grammar;
	}

	/**
	 * Returns the currently selected productions.
	 *
	 * @return the currently selected productions
	 */
	public List<Production> getSelected() {
		final int[] rows = getSelectedRows();
		final Production[] selected = new Production[rows.length];
		for (int i = 0; i < rows.length; i++) {
			selected[i] = (Production) data.get(rows[i]).get(0);
		}
		return Arrays.asList(selected);
	}

	/**
	 * Removes a selection listener from this grammar viewer.
	 *
	 * @param listener
	 *            the selection listener to remove
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		selectionListeners.remove(listener);
	}

	/**
	 * Sets the indicated production as either checked or unchecked
	 * appropriately.
	 *
	 * @param production
	 *            the production to set the "checkyness" for
	 * @param checked
	 *            <CODE>true</CODE> if the production should be marked as
	 *            checked, <CODE>false</CODE> if unchecked
	 */
	public void setChecked(final Production production, final boolean checked) {
		final Integer r = productionToRow.get(production);
		if (r == null) {
			return;
		}
		final int row = r.intValue();
		data.get(row).set(1, checked);
		((DefaultTableModel) getModel()).setValueAt(checked, row, 1);
	}
}
