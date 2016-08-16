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

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * This is a cell renderer that displays a specified character if the quantity
 * to display is the empty string.
 *
 * @author Thomas Finley
 */
public class LambdaCellRenderer extends DefaultTableCellRenderer {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The string to substitute for the empty string. */
	private final String toSubstitute;

	/**
	 * Instantiates a new lambda cell renderer where the unicode string for
	 * lambda is substituted for the empty string when displaying the empty
	 * string.
	 */
	public LambdaCellRenderer() {
		this(Universe.curProfile.getEmptyString());
	}

	/**
	 * Instantiates a new lambda cell renderer with the specified string to
	 * substitute for the empty string in the event that we display the empty
	 * string.
	 *
	 * @param string
	 *            the string to display in lieu of the empty string
	 */
	public LambdaCellRenderer(final String string) {
		toSubstitute = string;
	}

	/**
	 * Returns the string this renderer substitutes for the empty string.
	 *
	 * @return the string displayed in lieu of the empty string
	 */
	public final String getEmpty() {
		return toSubstitute;
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {
		final JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (hasFocus && table.isCellEditable(row, column)) {
			return l;
		}
		if (!"".equals(value)) {
			return l;
		}
		l.setText(toSubstitute);
		return l;
	}
}
