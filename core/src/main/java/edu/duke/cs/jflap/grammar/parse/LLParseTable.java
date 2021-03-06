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

package edu.duke.cs.jflap.grammar.parse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import edu.duke.cs.jflap.grammar.Grammar;

/**
 * The <CODE>LLParseTable</CODE> is a parse table for LL grammars. It also has
 * the ability to function as a <CODE>TableModel</CODE> for a
 * <CODE>javax.swing.JTable</CODE>. Variables are in the rows, lookahead
 * terminals in the columns. In its capacity as a <CODE>TableModel</CODE> the
 * first row is taken up with the names of the variables.
 *
 * @author Thomas Finley
 */
public class LLParseTable extends AbstractTableModel implements Serializable, Cloneable {
	private static final long serialVersionUID = 11000L;

	/** The list of terminals, each corresponding to a column. */
	private final List<String> terminals;

	/** The variables in the grammar, each corresponding to a row. */
	private final List<String> variables;

	/** The entries in the parse table. */
	private final Table<Integer, Integer, SortedSet<String>> entries;

	/** Is the table noneditable? */
	private boolean frozen = false;

	/**
	 * Instantiates a new <CODE>LLParseTable</CODE> for a given grammar.
	 *
	 * @param grammar
	 *            the grammar to create the table for
	 */
	public LLParseTable(final Grammar grammar) {
		variables = grammar.getVariables();
		variables.sort((x, y) -> x.compareTo(y));
		terminals = grammar.getTerminals();
		terminals.sort((x, y) -> x.compareTo(y));
		entries = HashBasedTable.<Integer, Integer, SortedSet<String>>create();
		for (int i = 0; i < variables.size(); ++i) {
			for (int j = 0; j < terminals.size() + 1; ++j) {
				entries.put(i, j, new TreeSet<>());
			}
		}
	}

	/**
	 * Copy constructor for a LL parse table.
	 *
	 * @param table
	 *            the table to copy
	 */
	public LLParseTable(final LLParseTable table) {
		variables = table.variables;
		terminals = table.terminals;
		entries = HashBasedTable.<Integer, Integer, SortedSet<String>>create();
		entries.putAll(table.entries);
	}

	/**
	 * Adds a new entry to the LL parse table
	 *
	 * @param variable
	 *            the stack variable
	 * @param lookahead
	 *            the lookahead terminal
	 * @param expansion
	 *            what to expand the stack variable on when the lookahead
	 *            terminal is <CODE>lookahead</CODE>
	 * @return the number of expansion entries for this variable and lookahead
	 *         after this value was added
	 * @throws IllegalArgumentException
	 *             if either variable or lookahead is not a variable or terminal
	 *             (or $) respectively in the grammar
	 */
	public int addEntry(final String variable, final String lookahead, final String expansion) {
		final List<Integer> r = getLocation(variable, lookahead);
		entries.get(r.get(0), r.get(1)).add(expansion);
		fireTableCellUpdated(r.get(0), r.get(1) + 1);
		return entries.get(r.get(0), r.get(1)).size();
	}

	/**
	 * This will clear all entries in the table.
	 */
	public void clear() {
		for (final Cell<Integer, Integer, SortedSet<String>> c : entries.cellSet()) {
			c.getValue().clear();
		}
		fireTableDataChanged();
	}

	/**
	 * This will clear the entry in the table for the given variable and
	 * lookahead.
	 *
	 * @param variable
	 *            the stack variable
	 * @param lookahead
	 *            the lookahead terminal
	 * @throws IllegalArgumentException
	 *             if either variable or lookahead is not a variable or terminal
	 *             (or $) respectively in the grammar
	 */
	public void clear(final String variable, final String lookahead) {
		final List<Integer> r = getLocation(variable, lookahead);
		entries.get(r.get(0), r.get(1)).clear();
		fireTableCellUpdated(r.get(0), r.get(1) + 1);
	}

	/**
	 * Clone method for the LL parse table.
	 *
	 * @return a copy of this table
	 */
	@Override
	public Object clone() {
		return new LLParseTable(this);
	}

	/**
	 * Given a whitespace delimited string and a set, this clears the set and
	 * adds all distinct tokens from the string to the set.
	 *
	 * @param string
	 *            the string to process
	 * @param set
	 *            the set to add to
	 * @return the number of elements processed
	 */
	private int despaceSet(final String string, final Set<String> set) {
		set.clear();
		final StringTokenizer st = new StringTokenizer(string);
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s.equals("!")) {
				s = "";
			}
			set.add(s);
		}
		return set.size();
	}

	/**
	 * Returns <CODE>true</CODE> if this object equals another.
	 *
	 * @param object
	 *            the object to compare against
	 */
	@Override
	public boolean equals(final Object object) {
		try {
			final LLParseTable other = (LLParseTable) object;
			if (!variables.equals(other.variables)) {
				return false;
			}
			if (!terminals.equals(other.terminals)) {
				return false;
			}
			if (!entries.equals(other.entries)) {
				return false;
			}
			return true;
		} catch (final ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns the set of mapping for a variable and lookahead.
	 *
	 * @param variable
	 *            the stack variable
	 * @param lookahead
	 *            the lookahead terminal
	 * @throws IllegalArgumentException
	 *             if either variable or lookahead is not a variable or terminal
	 *             (or $) respectively in the grammar
	 */
	public SortedSet<String> get(final String variable, final String lookahead) {
		final List<Integer> r = getLocation(variable, lookahead);
		return Collections.unmodifiableSortedSet(entries.get(r.get(0), r.get(1)));
	}

	/**
	 * Returns the column location in the table model of the lookahead terminal.
	 *
	 * @throws IllegalArgumentException
	 *             if either variable or lookahead is not a variable or terminal
	 *             (or $) respectively in the grammar
	 */
	public int getColumn(final String lookahead) {
		int column = terminals.size();
		if (!lookahead.equals("$")) {
			column = Collections.binarySearch(terminals, lookahead);
		}
		if (column < 0) {
			throw new IllegalArgumentException(lookahead + " is not a terminal!");
		}
		return column + 1;
	}

	/**
	 * Returns the number of columns, equal to the number of terminals plus 1
	 * for the $ symbol plus 1 for the column devoted to variables.
	 */
	@Override
	public int getColumnCount() {
		return terminals.size() + 2;
	}

	// ABSTRACT TABLE MODEL METHODS BELOW

	/**
	 * Returns the name of a column.
	 *
	 * @param column
	 *            the index for a column
	 */
	@Override
	public String getColumnName(final int column) {
		if (column == 0) {
			return " ";
		}
		if (column == terminals.size() + 1) {
			return "$";
		}
		return terminals.get(column - 1);
	}

	/**
	 * Given another parse table with the same variable and lookahead, return a
	 * listing of those pairs of variables and terminals where there are
	 * differences.
	 *
	 * @param table
	 *            the other parse table
	 * @return an array, each element of which is a 2-array of the variable and
	 *         lookahead, in that order, of those entries in the table which do
	 *         not match
	 * @throws IllegalArgumentException
	 *             if the other parse table does not have the same variables and
	 *             lookahead terminals
	 */
	public List<List<String>> getDifferences(final LLParseTable table) {
		if (!variables.equals(table.variables) || !terminals.equals(table.terminals)) {
			throw new IllegalArgumentException("Tables differ in variables or terminals.");
		}
		final List<List<String>> differences = new ArrayList<>();
		for (final Cell<Integer, Integer, SortedSet<String>> c : entries.cellSet()) {
			if (!c.getValue().equals(table.entries.get(c.getRowKey(), c.getColumnKey()))) {
				differences.add(Lists.newArrayList(variables.get(c.getRowKey()), "$"));
			} else {
				differences.add(Lists.newArrayList(variables.get(c.getRowKey()), terminals.get(c.getColumnKey())));
			}
		}
		return differences;
	}

	/**
	 * Returns a 2 integer array of the row and column of a variable and
	 * lookahead, in that order.
	 *
	 * @param variable
	 *            the variable to look for
	 * @param lookahead
	 *            the lookahead terminal
	 * @throws IllegalArgumentException
	 *             if either variable or lookahead is not a variable or terminal
	 *             (or $) respectively in the grammar
	 */
	private List<Integer> getLocation(final String variable, final String lookahead) {
		final List<Integer> r = new ArrayList<>();
		r.add(getRow(variable));
		r.add(getColumn(lookahead) - 1);
		return r;
	}

	/**
	 * Returns the row location in the table model of the variable.
	 *
	 * @param variable
	 *            the variable to find the row for
	 * @return the row where this variable is
	 * @throws IllegalArgumentException
	 *             if the variable is not a variable in the grammar
	 */
	public int getRow(final String variable) {
		final int row = Collections.binarySearch(variables, variable);
		if (row < 0) {
			throw new IllegalArgumentException(variable + " is not a variable!");
		}
		return row;
	}

	/**
	 * Returns the number of rows, equal to the number of variables.
	 *
	 * @return the number of rows
	 */
	@Override
	public int getRowCount() {
		return variables.size();
	}

	/**
	 * Returns the value for the table.
	 *
	 * @param row
	 *            the row to get data for
	 * @param column
	 *            the column to get data for
	 * @return <CODE>M[ variables[row], terminals[column-1] ]</CODE> if
	 *         </CODE>column != 0</CODE>, or if <CODE>column == 0</CODE> returns
	 *         the variable for the row.
	 */
	@Override
	public Object getValueAt(final int row, final int column) {
		if (column == 0) {
			return variables.get(row);
		}
		return spaceSet(entries.get(row, column - 1));
	}

	/**
	 * Returns a hashcode for this table.
	 *
	 * @return the hashcode for this parse table
	 */
	@Override
	public int hashCode() {
		// Lazy, stupid, and dangerous, but unlike me has the virtue
		// of working...
		return variables.size() ^ terminals.size();
	}

	/**
	 * All cells are editable except those of the first column.
	 *
	 * @param row
	 *            the row index
	 * @param column
	 *            the column index
	 */
	@Override
	public boolean isCellEditable(final int row, final int column) {
		if (frozen) {
			return false;
		}
		return column != 0;
	}

	/**
	 * Removes an entry from the LL parse table
	 *
	 * @param variable
	 *            the stack variable
	 * @param lookahead
	 *            the lookahead terminal
	 * @param expansion
	 *            what to expand the stack variable on when the lookahead
	 *            terminal is <CODE>lookahead</CODE>
	 * @return if the expansion was even in this entry to start with
	 * @throws IllegalArgumentException
	 *             if either variable or lookahead is not a variable or terminal
	 *             (or $) respectively in the grammar
	 */
	public boolean removeEntry(final String variable, final String lookahead, final String expansion) {
		final List<Integer> r = getLocation(variable, lookahead);
		final boolean removed = entries.get(r.get(0), r.get(1)).remove(expansion);
		fireTableCellUpdated(r.get(0), r.get(1) + 1);
		return removed;
	}

	/**
	 * Defines the set of mapping for a variable and lookahead.
	 *
	 * @param productions
	 *            the new set for the entry
	 * @param variable
	 *            the stack variable
	 * @param lookahead
	 *            the lookahead terminal
	 * @throws IllegalArgumentException
	 *             if either variable or lookahead is not a variable or terminal
	 *             (or $) respectively in the grammar
	 */
	public void set(final Set<String> productions, final String variable, final String lookahead) {
		final List<Integer> r = getLocation(variable, lookahead);
		entries.get(r.get(0), r.get(1)).clear();
		entries.get(r.get(0), r.get(1)).addAll(productions);
	}

	/**
	 * Sets whether any entries in this table should be editable.
	 *
	 * @param editable
	 *            <CODE>true</CODE> if entries in this table should be editable,
	 *            <CODE>false</CODE> if the entries should be frozen
	 */
	public void setEditable(final boolean editable) {
		frozen = !editable;
	}

	/**
	 * Sets the value at a particular entry of the table.
	 *
	 * @param value
	 *            the new value for an entry
	 */
	@Override
	public void setValueAt(final Object value, final int row, final int column) {
		despaceSet((String) value, entries.get(row, column - 1));
		fireTableCellUpdated(row, column);
	}

	/**
	 * Takes a set, and returns a string with the entries of that set space
	 * delimited.
	 *
	 * @param set
	 *            the set to put in a space delimited string
	 */
	private String spaceSet(final Set<String> set) {
		final Iterator<String> it = set.iterator();
		boolean first = true;
		final StringBuffer sb = new StringBuffer();
		while (it.hasNext()) {
			if (!first) {
				sb.append(" ");
			}
			final String s = it.next();
			sb.append(s.equals("") ? "!" : s);
			first = false;
		}
		return sb.toString();
	}
}
