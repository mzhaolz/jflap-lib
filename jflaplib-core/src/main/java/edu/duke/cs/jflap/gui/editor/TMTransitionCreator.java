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

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.turing.TMTransition;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

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

/**
 * This is the creator of transitions in turing machines.
 *
 * @author Thomas Finley
 */
public class TMTransitionCreator extends TableTransitionCreator {
    /**
     * Instantiates a new <CODE>TMTransitionCreator</CODE>.
     *
     * @param parent
     *            the parent of whatever dialogs/windows get brought up by this
     *            creator
     */
    public TMTransitionCreator(AutomatonPane parent) {
        super(parent);
        machine = (TuringMachine) parent.getDrawer().getAutomaton();
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
    protected Transition initTransition(State from, State to) {
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
    protected Transition initTransition(State from, State to, String directionString) {
        List<String> read = Collections.nCopies(machine.tapes(), "");
        List<String> write = blockTransition ? Collections.nCopies(machine.tapes(), "~") : read;
        List<String> direction = Collections.nCopies(machine.tapes(), directionString);
        TMTransition t = new TMTransition(from, to, read, write, direction);
        t.setBlockTransition(blockTransition);
        return t;
    }

    /**
     * Given a transition, returns the arrays the editing table model needs.
     *
     * @param transition
     *            the transition to build the arrays for
     * @return the arrays for the editing table model
     */
    private Table<Integer, Integer, String> arraysForTransition(TMTransition transition) {
        Table<Integer, Integer, String> s = HashBasedTable.<Integer, Integer, String>create();
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
    protected TableModel createModel(Transition transition) {
        final TMTransition t = (TMTransition) transition;
        return new AbstractTableModel() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public Object getValueAt(int row, int column) {
                return s.get(row, column);
            }

            @Override
            public void setValueAt(Object o, int r, int c) {
                s.put(r, r, (String) o);
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                if (!blockTransition) {
                    return true;
                } else if (c == 0) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public int getRowCount() {
                return machine.tapes();
            }

            @Override
            public int getColumnCount() {
                if (!blockTransition) {
                    return 3;
                } else {
                    return 1;
                }
            }

            @Override
            public String getColumnName(int c) {
                return name[c];
            }

            Table<Integer, Integer, String> s = arraysForTransition(t);

            String name[] = { "Read", "Write", "Direction" };
        };
    }

    /**
     * Creates the table.
     */
    @Override
    protected JTable createTable(Transition transition) {
        JTable table = super.createTable(transition);
        if (!blockTransition) {
            TableColumn directionColumn = table.getColumnModel().getColumn(2);
            directionColumn.setCellEditor(new DefaultCellEditor(BOX) {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public Component getTableCellEditorComponent(JTable table, Object value,
                        boolean isSelected, int row, int column) {
                    final JComboBox<?> c = (JComboBox<?>) super.getTableCellEditorComponent(table,
                            value, isSelected, row, column);
                    InputMap imap = c.getInputMap();
                    ActionMap amap = c.getActionMap();
                    Object o = new Object();
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
     * Modifies a transition according to what's in the table.
     */
    @Override
    public Transition modifyTransition(Transition transition, TableModel model) {
        TMTransition t = (TMTransition) transition;
        try {
            List<String> reads = new ArrayList<>();
            List<String> writes = new ArrayList<>();
            List<String> dirs = new ArrayList<>();
            for (int i = 0; i < machine.tapes(); i++) {
                reads.add((String) model.getValueAt(i, 0));
                writes.add((String) model.getValueAt(i, 1));
                dirs.add((String) model.getValueAt(i, 2));
            }
            TMTransition newTrans = new TMTransition(t.getFromState(), t.getToState(), reads,
                    writes, dirs);
            if (transition instanceof TMTransition) {
                TMTransition oldTrans = (TMTransition) transition;
                newTrans.setBlockTransition(oldTrans.isBlockTransition());
            }
            return newTrans;
        } catch (IllegalArgumentException e) {
            reportException(e);
            return null;
        }
    }

    public boolean isBlockTransition() {
        return blockTransition;
    }

    public void setBlockTransition(boolean block) {
        blockTransition = block;
    }

    public static void setDirs(boolean allowStay) {
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
    private TuringMachine machine;

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
        public void actionPerformed(ActionEvent e) {
            JComboBox<?> box = (JComboBox<?>) e.getSource();
            box.setSelectedItem(e.getActionCommand().toUpperCase());
        }
    };

}
