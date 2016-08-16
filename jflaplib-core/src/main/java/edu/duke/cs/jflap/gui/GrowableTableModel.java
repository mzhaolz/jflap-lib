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

package edu.duke.cs.jflap.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * The <CODE>GrowableTableModel</CODE> is a table model that grows automatically
 * whenever the last line is edited. Basically, whenever the
 * <CODE>setValueAt</CODE> method is called on the last row, the table grows
 * itself by one line.
 *
 * @author Thomas Finley
 */
public abstract class GrowableTableModel<T> extends AbstractTableModel implements Cloneable {
	/**
	 *
	 */
	private static final long serialVersionUID = 7046174616169431065L;

	/** This holds the number of columns. */
	protected int columns;

	/** Each row is stored as an array of objects in this list. */
	protected List<List<T>> data = new ArrayList<>();

	/**
	 * The copy constructor for this table model. This will do a shallow copy of
	 * all elements in the data.
	 *
	 * @param model
	 *            the model to copy
	 */
	public GrowableTableModel(final GrowableTableModel<T> model) {
		copy(model);
	}

	/**
	 * This instantiates a <CODE>GrowableTableModel</CODE>.
	 *
	 * @param columns
	 *            the number of columns for this model
	 */
	public GrowableTableModel(final int columns) {
		this.columns = columns;
		clear();
	}

	/**
	 * This initializes the table so that it is completely blank except for
	 * having one row. The number of columns remains unchanged.
	 */
	public void clear() {
		data.clear();
		data.add(initializeRow(0));
		fireTableDataChanged();
	}

	/**
	 * This is a copy method to copy all of the data for a given table model to
	 * this table model.
	 *
	 * @param model
	 *            the model to copy
	 */
	public void copy(final GrowableTableModel<T> model) {
		columns = model.getColumnCount();
		data.clear();
		final Iterator<List<T>> it = model.data.iterator();
		while (it.hasNext()) {
			// TODO: this was previous, and still isn't, a deep copy.
			// Not clear what they wanted.
			final List<T> oldRow = it.next();
			data.add(oldRow);
		}
		fireTableDataChanged();
	}

	/**
	 * Deletes a particular row.
	 *
	 * @param row
	 *            the row index to delete
	 * @return if the row was able to be deleted
	 */
	public boolean deleteRow(final int row) {
		if (row < 0 || row > data.size() - 2) {
			return false;
		}
		data.remove(row);
		fireTableRowsDeleted(row, row);
		return true;
	}

	/**
	 * Returns the number of columns in this table.
	 *
	 * @return the number of columns in this table
	 */
	@Override
	public final int getColumnCount() {
		return columns;
	}

	/**
	 * Returns the number of rows currently in this table.
	 *
	 * @return the number of rows currently in this table
	 */
	@Override
	public final int getRowCount() {
		return data.size();
	}

	/**
	 * Returns the object at a particular location in the model.
	 *
	 * @param row
	 *            the row of the object to retrieve
	 * @param column
	 *            the column of the object to retrieve
	 * @return the object at that location
	 */
	@Override
	public T getValueAt(final int row, final int column) {
		return data.get(row).get(column);
	}

	/**
	 * Initializes a new row. This should not modify any data or any state
	 * whatsoever, but should simply return an initialized row.
	 *
	 * @param row
	 *            the number of the row that's being initialized
	 * @return an object array that will hold, in column order, the contents of
	 *         that particular row; this by default simply returns an unmodified
	 *         <CODE>Object</CODE> array of size equal to the number of columns
	 *         with contents set to <CODE>null</CODE>
	 */
	protected List<T> initializeRow(final int row) {
		final List<T> newRow = new ArrayList<>(Collections.nCopies(getColumnCount(), null));
		return newRow;
	}

	/**
	 * Inserts data at a particular row.
	 *
	 * @param newData
	 *            the array of new data for a row
	 * @param row
	 *            the row index to insert the new data at
	 * @throws IllegalArgumentException
	 *             if the data array length differs from the number of columns
	 */
	public void insertRow(final List<T> newData, final int row) {
		if (newData.size() != columns) {
			throw new IllegalArgumentException("Data length is " + newData.size() + ", should be " + columns + ".");
		}
		data.add(row, newData);
		fireTableRowsInserted(row, row);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setValueAt(final Object newData, final int row, final int column) {
		data.get(row).set(column, (T) newData);
		if (row + 1 == getRowCount()) {
			data.add(initializeRow(row + 1));
			fireTableRowsInserted(row + 1, row + 1);
		}
		if (row >= getRowCount()) {
			data.add(initializeRow(row));
			fireTableRowsInserted(row, row);
		}
		fireTableCellUpdated(row, column);
	}
}
