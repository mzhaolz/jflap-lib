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

package edu.duke.cs.jflap.gui.lsystem;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.gui.GrowableTableModel;

/**
 * A mapping of parameters to values.
 *
 * @author Thomas Finley
 */
public class ParameterTableModel extends GrowableTableModel<String> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1656054894151911750L;

	/**
	 * Constructs an empty parameter table model.
	 */
	public ParameterTableModel() {
		super(2);
	}

	/**
	 * Constructs a parameter table model out of the map.
	 *
	 * @param parameters
	 *            the mapping of parameter names to parameter objects
	 */
	public ParameterTableModel(final Map<String, String> parameters) {
		this();
		final Iterator<Map.Entry<String, String>> it = parameters.entrySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			final Map.Entry<String, String> entry = it.next();
			setValueAt(entry.getKey(), i, 0);
			setValueAt(entry.getValue(), i, 1);
			i++;
		}
	}

	/**
	 * Returns the column name.
	 *
	 * @param column
	 *            the index of the column
	 * @return the name of a particular column
	 */
	@Override
	public String getColumnName(final int column) {
		return column == 0 ? "Name" : "Parameter";
	}

	/**
	 * Returns the mapping of names of parameters.
	 *
	 * @return the mapping from parameter names to parameters (i.e., map of
	 *         contents of the left column to contents of the right column)
	 */
	public Map<String, String> getParameters() {
		final TreeMap<String, String> map = new TreeMap<>();
		for (int i = 0; i < getRowCount() - 1; i++) {
			final String o = getValueAt(i, 0);
			if (o.equals("")) {
				continue;
			}
			map.put(o, getValueAt(i, 1));
		}
		return map;
	}

	/**
	 * Initializes a row. In this object, a row is two empty strings.
	 *
	 * @return an array with two empty strings
	 */
	@Override
	public List<String> initializeRow(final int row) {
		return Lists.newArrayList("", "");
	}

	/**
	 * Values in the table are editable.
	 *
	 * @param row
	 *            the row index
	 * @param column
	 *            the column index
	 * @return <CODE>true</CODE> always
	 */
	@Override
	public boolean isCellEditable(final int row, final int column) {
		return true;
	}
}
