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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import com.google.common.collect.ImmutableList;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * This is a table model for user definition of the first and follow sets for a
 * grammar. The first column are the variable names. The second column are the
 * first sets, the third column the follow sets.
 *
 * @author Thomas Finley
 */
public class FirstFollowModel extends AbstractTableModel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The lambda string. */
	public static String LAMBDA = Universe.curProfile.getEmptyString();

	/** The names of columns. */
	public static List<String> COLUMN_NAMES = ImmutableList.of(" ", "FIRST", "FOLLOW");

	/** The variables. */
	private final List<String> variables;

	/** The user defined first sets strings. */
	private final List<String> firstSets;

	/** The user defined follow sets strings. */
	private final List<String> followSets;

	/** The permissions to edit each column. */
	private final List<Boolean> canEditColumn = new ArrayList<>(Collections.nCopies(3, false));

	/**
	 * Instantiates a new <CODE>FirstFollowModel</CODE>.
	 *
	 * @param grammar
	 *            the grammar for the first follow model, from which the
	 *            variables are extracted for the first column
	 */
	public FirstFollowModel(final Grammar grammar) {
		variables = grammar.getVariables();
		variables.sort((x, y) -> x.compareTo(y));

		firstSets = new ArrayList<>(Collections.nCopies(variables.size(), ""));
		followSets = new ArrayList<>(Collections.nCopies(variables.size(), ""));
	}

	/**
	 * There are always three columns for the variables, the first sets, and the
	 * follow sets.
	 *
	 * @return 3
	 */
	@Override
	public int getColumnCount() {
		return 3;
	}

	/**
	 * Returns the name of a particular column.
	 *
	 * @param column
	 *            the index of a column to get the name for
	 */
	@Override
	public String getColumnName(final int column) {
		return COLUMN_NAMES.get(column);
	}

	/**
	 * Retrives the first sets as shown in the table.
	 *
	 * @return the first sets, a map from all single symbols A in the grammar to
	 *         the set of symbols that represent FIRST(A)
	 */
	public Map<?, ?> getFirst() {
		return null;
	}

	/**
	 * Retrieves the follow sets as shown in the table.
	 *
	 * @return the follow sets, a map from all single variables A in the grammar
	 *         to the set of symbols that represent FOLLOW(A)
	 */
	public Map<?, ?> getFollow() {
		return null;
	}

	/**
	 * There are as many rows as there are variables.
	 *
	 * @return the number of variables
	 */
	@Override
	public int getRowCount() {
		return variables.size();
	}

	/**
	 * Returns the character set at a particular location.
	 *
	 * @param row
	 *            the row to get the set for; this will be a set for the
	 *            variable at this row
	 * @param column
	 *            the column to get the set for, which will be 1 for first and 2
	 *            for follow
	 */
	public Set<String> getSet(final int row, final int column) {
		final String s = (String) getValueAt(row, column);
		final Set<String> set = new TreeSet<>();
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '!') {
				set.add("");
				continue;
			}
			set.add(s.substring(i, i + 1));
		}
		return set;
	}

	/**
	 * Returns the value at each column.
	 *
	 * @param row
	 *            the row to get data for
	 * @param column
	 *            the column to get data for
	 * @return the data for this column
	 */
	@Override
	public Object getValueAt(final int row, final int column) {
		switch (column) {
		case 0:
			return variables.get(row);
		case 1:
			return firstSets.get(row);
		case 2:
			return followSets.get(row);
		}
		return null;
	}

	/**
	 * Returns if a table cell can be edited.
	 *
	 * @param row
	 *            the row of the cell
	 * @param column
	 *            the column of the cell
	 */
	@Override
	public boolean isCellEditable(final int row, final int column) {
		return canEditColumn.get(column);
	}

	/**
	 * When a string value is used to define a set, it's possible that the
	 * string value will contain multiple characters. This will take a string,
	 * and return a string with not duplicate characters. Only the first
	 * instance of a character is preserved, and others are discarded. So, if
	 * one were to pass in <I>abaacb</I>, this would return the string
	 * <I>abc</I>.
	 *
	 * @param s
	 *            the string to process
	 * @return the same string as was passed in but with duplicate characters
	 *         removed
	 */
	private String removeDuplicateCharacters(final String s) {
		final Set<Character> characters = new HashSet<>();
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			final Character c = new Character(s.charAt(i));
			if (characters.add(c)) {
				sb.append(c.charValue());
			}
		}
		return sb.toString();
	}

	/**
	 * Set if the user can edit the first sets at this time.
	 *
	 * @param canEdit
	 *            <CODE>true</CODE> if editing is allowed
	 */
	public void setCanEditFirst(final boolean canEdit) {
		canEditColumn.set(1, canEdit);
	}

	/**
	 * Set if the user can edit the follow sets at this time.
	 *
	 * @param canEdit
	 *            <CODE>true</CODE> if editing is allowed
	 */
	public void setCanEditFollow(final boolean canEdit) {
		canEditColumn.set(2, canEdit);
	}

	/**
	 * Sets the set at a particular cell.
	 *
	 * @param set
	 *            the set for the cell
	 * @param row
	 *            the row index of the cell to set the set for
	 * @param column
	 *            the column index of the cell to set the set for
	 */
	public void setSet(final Set<?> set, final int row, final int column) {
		final StringBuffer sb = new StringBuffer();
		final Iterator<?> it = set.iterator();
		while (it.hasNext()) {
			String element = (String) it.next();
			if (element.length() == 0) {
				element = "!";
			}
			sb.append(element);
		}
		setValueAt(sb.toString(), row, column);
	}

	/**
	 * Sets the value at each column.
	 *
	 * @param value
	 *            the new value
	 * @param row
	 *            the row to change
	 * @param column
	 *            the column to change
	 */
	@Override
	public void setValueAt(final Object value, final int row, final int column) {
		switch (column) {
		case 0:
			variables.set(row, (String) value);
			break;
		case 1:
			firstSets.set(row, removeDuplicateCharacters((String) value));
			break;
		case 2:
			followSets.set(row, removeDuplicateCharacters((String) value));
			break;
		}
	}
}
