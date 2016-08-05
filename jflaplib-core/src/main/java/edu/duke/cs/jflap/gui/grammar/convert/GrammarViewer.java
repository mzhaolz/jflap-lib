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

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.gui.event.SelectionEvent;
import edu.duke.cs.jflap.gui.event.SelectionListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

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
   *
   */
  private static final long serialVersionUID = 877608254800510232L;

  /**
   * Instantiates a new <CODE>GrammarViewer</CODE>.
   *
   * @param grammar
   *            the grammar to display in this view
   */
  public GrammarViewer(Grammar grammar) {
    setModel(new GrammarTableModel());
    this.grammar = grammar;
    // setLayout(new BorderLayout());
    Production[] prods = grammar.getProductions();
    data = new Object[prods.length][2];
    Object[] columnNames = {"Production", "Created"};

    for (int i = 0; i < prods.length; i++) {
      data[i][0] = prods[i];
      data[i][1] = Boolean.FALSE;
      productionToRow.put(prods[i], new Integer(i));
    }
    DefaultTableModel model = (DefaultTableModel) getModel();
    model.setDataVector(data, columnNames);

    // Set the listener to the selectedness.
    getSelectionModel().addListSelectionListener(listSelectListener);
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
   * Adds a selection listener to this grammar viewer. The listener will
   * receive events whenever the selection changes.
   *
   * @param listener
   *            the selection listener to add
   */
  public void addSelectionListener(SelectionListener listener) {
    selectionListeners.add(listener);
  }

  /**
   * Removes a selection listener from this grammar viewer.
   *
   * @param listener
   *            the selection listener to remove
   */
  public void removeSelectionListener(SelectionListener listener) {
    selectionListeners.remove(listener);
  }

  /**
   * Distributes a selection event.
   */
  protected void distributeSelectionEvent() {
    Iterator<SelectionListener> it = selectionListeners.iterator();
    while (it.hasNext()) {
      SelectionListener listener = it.next();
      listener.selectionChanged(EVENT);
    }
  }

  /**
   * Returns the currently selected productions.
   *
   * @return the currently selected productions
   */
  public List<Production> getSelected() {
    int[] rows = getSelectedRows();
    Production[] selected = new Production[rows.length];
    for (int i = 0; i < rows.length; i++) selected[i] = (Production) data[rows[i]][0];
    return selected;
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
  public void setChecked(Production production, boolean checked) {
    Integer r = productionToRow.get(production);
    if (r == null) return;
    int row = r.intValue();
    Boolean b = checked ? Boolean.TRUE : Boolean.FALSE;
    data[row][1] = b;
    ((DefaultTableModel) getModel()).setValueAt(b, row, 1);
  }

  /** The grammar to display. */
  private Grammar grammar;

  /** The data of the table. */
  private List<Object>[] data;

  /** The mapping of productions to a row (rows stored as Integer). */
  private Map<Production, Integer> productionToRow = new HashMap<>();

  /** The selection event. */
  private SelectionEvent EVENT = new SelectionEvent(this);

  /** The set of selection listeners. */
  private Set<SelectionListener> selectionListeners = new HashSet<>();

  private ListSelectionListener listSelectListener =
      new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          distributeSelectionEvent();
        }
      };

  /**
   * The model for this table.
   */
  private class GrammarTableModel extends DefaultTableModel {
    /**
     *
     */
    private static final long serialVersionUID = -1542867493683971894L;

    public boolean isCellEditable(int row, int column) {
      return false;
    }

    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex == 1) return Boolean.class;
      return super.getColumnClass(columnIndex);
    }
  }
}
