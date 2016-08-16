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

package edu.duke.cs.jflap.gui.grammar.automata;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.TableTextSizeSlider;
import edu.duke.cs.jflap.gui.editor.ArrowDisplayOnlyTool;
import edu.duke.cs.jflap.gui.environment.AutomatonEnvironment;
import edu.duke.cs.jflap.gui.grammar.GrammarTable;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * This <CODE>ConvertPane</CODE> exists for the user to convert an automaton to
 * a grammar.
 *
 * @author Thomas Finley
 */
public class ConvertPane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The automaton pane. */
	private final AutomatonPane automatonPane;

	/** The grammar table. */
	private final GrammarTable table = new GrammarTable(new edu.duke.cs.jflap.gui.grammar.GrammarTableModel() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(final int r, final int c) {
			return false;
		}
	});

	/** The drawer for the automaton. */
	private final SelectionDrawer drawer;

	/**
	 * Instantiates a new <CODE>ConvertPane</CODE>.
	 */
	public ConvertPane(final AutomatonEnvironment environment, final Automaton automaton) {
		super(new BorderLayout());
		drawer = new SelectionDrawer(automaton);
		automatonPane = new AutomatonPane(drawer);
		final JSplitPane split = SplitPaneFactory.createSplit(environment, true, 0.6, automatonPane,
				new JScrollPane(table));
		automatonPane.addMouseListener(new ArrowDisplayOnlyTool(automatonPane, automatonPane.getDrawer()));
		add(split, BorderLayout.CENTER);
		add(new TableTextSizeSlider(table), BorderLayout.SOUTH);
	}

	/**
	 * Returns the <CODE>AutomatonPane</CODE> that does the drawing.
	 *
	 * @return the <CODE>AutomatonPane</CODE> that does the drawing
	 */
	public AutomatonPane getAutomatonPane() {
		return automatonPane;
	}

	/**
	 * Returns the <CODE>SelectionDrawer</CODE> for the automaton pane.
	 *
	 * @return the <CODE>SelectionDrawer</CODE>
	 */
	public SelectionDrawer getDrawer() {
		return drawer;
	}

	/**
	 * Returns the <CODE>GrammarTable</CODE> where the grammar is being built.
	 *
	 * @return the <CODE>GrammarTable</CODE>
	 */
	public GrammarTable getTable() {
		return table;
	}
}
