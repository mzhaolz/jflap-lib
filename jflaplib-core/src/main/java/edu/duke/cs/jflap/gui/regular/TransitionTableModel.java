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

package edu.duke.cs.jflap.gui.regular;

import edu.duke.cs.jflap.automata.Transition;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * The <CODE>TransitionTableModel</CODE> is a model for displaying a table of
 * transitions. This will display one transition per row with, in order, the
 * from state ID number, the to state ID number, and the transition description.
 *
 * @author Thomas Finley
 */
public class TransitionTableModel extends AbstractTableModel {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new <CODE>TransitionTableModel</CODE>.
   *
   * @param list
   *            the array of transitions to create the table for
   */
  public TransitionTableModel(List<Transition> list) {
    this.transitions = list;
  }

  /**
   * Instantiates a new <CODE>TransitionTableModel</CODE> with no contents.
   */
  public TransitionTableModel() {
    this(new ArrayList<>());
  }

  /**
   * This table model has three columns.
   *
   * @return the number 3!
   */
  public int getColumnCount() {
    return 3;
  }

  /**
   * This table model has as many rows are there are transitions.
   *
   * @return the number of transitions
   */
  public int getRowCount() {
    return transitions.size();
  }

  /**
   * Returns the transition for a row. This method will fail if there isn't a
   * transition for this index.
   *
   * @param row
   *            the row to get the transition for
   * @return the transition for this row
   */
  public Transition getTransition(int row) {
    return transitions.get(row);
  }

  /**
   * Returns the contents of a cell.
   *
   * @param row
   *            the number of the transition to display
   * @param column
   *            controls which parameter of the transition shall be displayed,
   *            for 0 through 2, this is from, to, and description, in that
   *            order
   * @return the string description for the appropriate part of a transition
   */
  public Object getValueAt(int row, int column) {
    switch (column) {
      case 0:
        return "" + transitions.get(row).getFromState().getID();
      case 1:
        return "" + transitions.get(row).getToState().getID();
      case 2:
        return transitions.get(row).getDescription();
      default:
        return null;
    }
  }

  /**
   * Returns the name for a column.
   *
   * @param column
   *            the index of the column to get the name for
   * @return the name for the indicated column
   */
  public String getColumnName(int column) {
    return COLUMN_NAMES.get(column);
  }

  /** The list of transitions displayed in the table. */
  private List<Transition> transitions;

  /** The column names. */
  private static final List<String> COLUMN_NAMES = ImmutableList.of("From", "To", "Label");
}
