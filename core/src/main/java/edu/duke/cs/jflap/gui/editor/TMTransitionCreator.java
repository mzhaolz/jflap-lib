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

package edu.duke.cs.jflap.gui.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.turing.TMTransition;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * This is the creator of transitions in turing machines.
 *
 * @author Thomas Finley
 */
public class TMTransitionCreator extends TableTransitionCreator {
	/** The directions. */
	private static List<String> DIRS = Lists.newArrayList("R", "S", "L"); // made
																			// this
	// non-static
	// to allow
	// for
	// switching
	// option

	/** The direction field combo box. */
	private static JComboBox<String> BOX = new JComboBox<>(DIRS.toArray(new String[0]));

	/** The array of keystrokes for the direction field. */
	private static List<KeyStroke> STROKES;

	static {
		STROKES = new ArrayList<>();
		for (int i = 0; i < STROKES.size(); i++) {
			STROKES.add(KeyStroke.getKeyStroke("shift " + DIRS.get(i)));
		}
	}

	/** The action for the strokes for the direction field. */
	private static final Action CHANGE_ACTION = new AbstractAction() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JComboBox<?> box = (JComboBox<?>) e.getSource();
			box.setSelectedItem(e.getActionCommand().toUpperCase());
		}
	};

	public static void setDirs(final boolean allowStay) {
		if (allowStay) {
			DIRS = Lists.newArrayList("R", "S", "L"); // made this non-static to
		} else {
			// EDebug.print("Reduction");
			DIRS = Lists.newArrayList("R", "L"); // made this non-static to
													// allow
			// for switching option
		}

		STROKES = new ArrayList<>(); // we shall see ...arrays can't
		// change size though
		for (int i = 0; i < STROKES.size(); i++) {
			STROKES.add(KeyStroke.getKeyStroke("shift " + DIRS.get(i)));
		}

		BOX.removeAllItems();
		for (int i = 0; i < DIRS.size(); i++) {
			BOX.addItem(DIRS.get(i));
		}
	}

	private boolean blockTransition = false;

	/** The Turing machine. */
	private final TuringMachine machine;

	/**
	 * Instantiates a new <CODE>TMTransitionCreator</CODE>.
	 *
	 * @param parent
	 *            the parent of whatever dialogs/windows get brought up by this
	 *            creator
	 */
	public TMTransitionCreator(final AutomatonPane parent) {
		super(parent);
		machine = (TuringMachine) parent.getDrawer().getAutomaton();
	}

	/**
	 * Given a transition, returns the arrays the editing table model needs.
	 *
	 * @param transition
	 *            the transition to build the arrays for
	 * @return the arrays for the editing table model
	 */
	private Table<Integer, Integer, String> arraysForTransition(final TMTransition transition) {
		final Table<Integer, Integer, String> s = HashBasedTable.<Integer, Integer, String>create();
		for (int i = machine.tapes() - 1; i >= 0; i--) {
			s.put(i, 0, transition.getRead(i));
			s.put(i, 1, transition.getWrite(i));
			if (s.get(i, 0).equals(TMTransition.BLANK)) {
				s.put(i, 0, "");
			}
			if (s.get(i, 1).equals(TMTransition.BLANK)) {
				s.put(i, 1, "");
			}
			s.put(i, 2, transition.getDirection(i));
		}
		return s;
	}

	/**
	 * Creates a new table model.
	 *
	 * @param transition
	 *            the transition to create the model for
	 * @return a table model for the transition
	 */
	@Override
	protected TableModel createModel(final Transition transition) {
		final TMTransition t = (TMTransition) transition;
		return new AbstractTableModel() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			Table<Integer, Integer, String> s = arraysForTransition(t);

			String name[] = { "Read", "Write", "Direction" };

			@Override
			public int getColumnCount() {
				if (!blockTransition) {
					return 3;
				} else {
					return 1;
				}
			}

			@Override
			public String getColumnName(final int c) {
				return name[c];
			}

			@Override
			public int getRowCount() {
				return machine.tapes();
			}

			@Override
			public Object getValueAt(final int row, final int column) {
				return s.get(row, column);
			}

			@Override
			public boolean isCellEditable(final int r, final int c) {
				if (!blockTransition) {
					return true;
				} else if (c == 0) {
					return true;
				} else {
					return false;
				}
			}

			@Override
			public void setValueAt(final Object o, final int r, final int c) {
				s.put(r, r, (String) o);
			}
		};
	}

	/**
	 * Creates the table.
	 */
	@Override
	protected JTable createTable(final Transition transition) {
		final JTable table = super.createTable(transition);
		if (!blockTransition) {
			final TableColumn directionColumn = table.getColumnModel().getColumn(2);
			directionColumn.setCellEditor(new DefaultCellEditor(BOX) {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTableCellEditorComponent(final JTable table, final Object value,
						final boolean isSelected, final int row, final int column) {
					final JComboBox<?> c = (JComboBox<?>) super.getTableCellEditorComponent(table, value, isSelected,
							row, column);
					final InputMap imap = c.getInputMap();
					final ActionMap amap = c.getActionMap();
					final Object o = new Object();
					amap.put(o, CHANGE_ACTION);
					for (int i = 0; i < STROKES.size(); i++) {
						imap.put(STROKES.get(i), o);
					}
					return c;
				}
			});
		}
		return table;
	}

	/**
	 * Initializes a new empty transition.
	 *
	 * @param from
	 *            the from state
	 * @param to
	 *            to too state
	 */
	@Override
	protected Transition initTransition(final State from, final State to) {
		if (!blockTransition) {
			return initTransition(from, to, "R");
		} else {
			return initTransition(from, to, "S");
		}
	}

	/**
	 * Initializes a new empty transition.
	 *
	 * @param from
	 *            the from state
	 * @param to
	 *            to too state
	 */
	protected Transition initTransition(final State from, final State to, final String directionString) {
		final List<String> read = new ArrayList<>(Collections.nCopies(machine.tapes(), ""));
		final List<String> write = blockTransition ? Collections.nCopies(machine.tapes(), "~") : read;
		final List<String> direction = new ArrayList<>(Collections.nCopies(machine.tapes(), directionString));
		final TMTransition t = new TMTransition(from, to, read, write, direction);
		t.setBlockTransition(blockTransition);
		return t;
	}

	public boolean isBlockTransition() {
		return blockTransition;
	}

	/**
	 * Modifies a transition according to what's in the table.
	 */
	@Override
	public Transition modifyTransition(final Transition transition, final TableModel model) {
		final TMTransition t = (TMTransition) transition;
		try {
			final List<String> reads = new ArrayList<>();
			final List<String> writes = new ArrayList<>();
			final List<String> dirs = new ArrayList<>();
			for (int i = 0; i < machine.tapes(); i++) {
				reads.add((String) model.getValueAt(i, 0));
				writes.add((String) model.getValueAt(i, 1));
				dirs.add((String) model.getValueAt(i, 2));
			}
			final TMTransition newTrans = new TMTransition(t.getFromState(), t.getToState(), reads, writes, dirs);
			if (transition instanceof TMTransition) {
				final TMTransition oldTrans = (TMTransition) transition;
				newTrans.setBlockTransition(oldTrans.isBlockTransition());
			}
			return newTrans;
		} catch (final IllegalArgumentException e) {
			reportException(e);
			return null;
		}
	}

	public void setBlockTransition(final boolean block) {
		blockTransition = block;
	}

}
