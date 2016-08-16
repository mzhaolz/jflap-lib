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

import javax.swing.JTable;
import javax.swing.table.TableModel;

import edu.duke.cs.jflap.gui.action.BatchMultipleSimulateAction;
import edu.duke.cs.jflap.gui.action.MultipleSimulateAction;

public class JTableExtender extends JTable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final MultipleSimulateAction myMultSimAct;

	public JTableExtender(final TableModel model, final BatchMultipleSimulateAction mult) {
		super(model);
		myMultSimAct = mult;
	}

	public JTableExtender(final TableModel model, final MultipleSimulateAction mult) {
		super(model);
		myMultSimAct = mult;
	}

	@Override
	public void changeSelection(final int row, final int column, final boolean toggle, final boolean extend) {
		super.changeSelection(row, column, toggle, extend);
		myMultSimAct.viewAutomaton(this);
	}
}
