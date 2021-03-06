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

package edu.duke.cs.jflap.gui.pumping;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.pumping.Case;
import edu.duke.cs.jflap.pumping.PumpingLemma;

/**
 * A <code>CasePanel</code> manages the graphical representation of the various
 * {@link edu.duke.cs.jflap.pumping.Case}s in a pumping lemma. It is associated
 * with a {@link edu.duke.cs.jflap.gui.pumping.PumpingLemmaInputPane}and a
 * {@link edu.duke.cs.jflap.pumping.PumpingLemma}.
 *
 * @author Jinghui Lim
 *
 */
public class CasePanel extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The preferred size of the <code>CasePanel</code>. The width is set to the
	 * maximum length of the longest description of any case.
	 */
	private static Dimension PREFERRED_SIZE = new Dimension(350, 480);
	/**
	 * An <code>ArrayList</code> of <code>String</code>s each of which describes
	 * a done case.
	 */
	private ArrayList<?> myCases;
	/**
	 * Each row of this <code>JTable</code> displays a case.
	 */
	private JTable myTable;
	/**
	 * The pumping lemma we are exploring.
	 */
	private final PumpingLemma myLemma;
	/**
	 * The last decomposition chosen. This is used when {@link #addCase()} is
	 * called.
	 */
	private List<Integer> tempDecomposition;
	/**
	 * The last <i>i</i> used.
	 */
	private int tempI;
	/**
	 * A text area that displays any relevant information.
	 */
	private JTextArea myMessage;
	/**
	 * The <code>PumpingLemmaInputPane</code> that is the "parent" of this
	 * <code>CasePanel</code>. This is used when
	 * {@link #setDecomposition(int[])} is called.
	 */
	private final PumpingLemmaInputPane myPane;
	/**
	 * The button that shows all cases.
	 */
	private JButton myShowAll;
	/**
	 * The button that clears all cases.
	 */
	private JButton myClearAll;
	/**
	 * The button that clears an individual case.
	 */
	private JButton myClearCase;
	/**
	 * The button that shows a case in the <code>myPane</code>.
	 */
	private JButton myShowCase;
	/**
	 * The button that adds a case.
	 */
	private JButton myAddCase;
	/**
	 * The button that checks if the user has explored all the cases.
	 */
	private JButton myDone;
	/**
	 * The button that allows the user to replace a case.
	 */
	private JButton myReplace;

	/**
	 * Constructs a <code>CasePanel</code> that is linked to a
	 * <code>PumpingLemma</code> and a <code>PumpingLemmaInputPane</code>.
	 *
	 * @param l
	 *            the pumping lemma we are demonstrating
	 * @param p
	 *            the input pane that deals with the user input
	 */
	public CasePanel(final PumpingLemma l, final PumpingLemmaInputPane p) {
		myPane = p;
		myLemma = l;
		myCases = new ArrayList<>();
		setLayout(new BorderLayout());
		add(new JLabel("Cases:"), BorderLayout.NORTH);
		add(initTable(), BorderLayout.CENTER);
		add(initButtons(), BorderLayout.SOUTH);
		setPreferredSize(PREFERRED_SIZE);
		refresh();
	}

	/**
	 * Adds the most recent decomposition and <i>i</i>.
	 *
	 * @see #setDecomposition(int[])
	 * @see #setI(int)
	 * @see #addCase(int[], int)
	 */
	protected void addCase() {
		addCase(tempDecomposition, tempI);
	}

	/**
	 * Adds the decomposition to the "done" cases of the pumping lemma we are
	 * working with.
	 *
	 * @param decomposition
	 *            the decomposition we wish to add
	 * @param i
	 *            the <i>i</i> corresponding to the decomposition
	 * @see edu.duke.cs.jflap.pumping.ContextFreePumpingLemma#addCase(int[],
	 *      int)
	 */
	protected void addCase(final List<Integer> decomposition, final int i) {
		int ret = myLemma.addCase(decomposition, i);
		/*
		 * refresh() must be called before the rest of the code so we will
		 * update myCases and myCases.size() etc.
		 */
		refresh();

		if (ret == -1) {
			/*
			 * "Illegal" decomposition. This should not happen as the input pane
			 * checks if the decomposition is legal.
			 */
			myMessage.setText("Illegal decomposition!");
			return;
		} else if (ret >= myCases.size()) {
			/*
			 * New case.
			 */
			ret = myCases.size() - 1;
			myMessage.setText("Case added.");
		} else {
			/*
			 * Case that has already been done.
			 */
			myMessage.setText("This case is similar to #" + (ret + 1) + ".");
		}

		/*
		 * Highlight (select) the last case added or the case that it is similar
		 * to.
		 */
		myTable.getSelectionModel().setSelectionInterval(ret, ret);
	}

	/**
	 * Clears all the cases that are done, restarting the whole process.
	 *
	 */
	public void clearAll() {
		myLemma.clearDoneCases();
		myMessage.setText("All cases cleared.");
		refresh();
	}

	/**
	 * Clears an individual case whose location is index <code>i</code>.
	 *
	 * @param i
	 *            the position of the case we wish to clear
	 */
	protected void clearCase(final int i) {
		try {
			myLemma.clearCase(i);
			myCases.remove(i);
			myMessage.setText("Case #" + (i + 1) + " deleted.");
			refresh();
		} catch (final ArrayIndexOutOfBoundsException e) {
			/*
			 * This should not happen because the button should be disabled if
			 * there is no selection.
			 */
			myMessage.setText("Please select a case to clear.");
		}
	}

	/**
	 * Initializes all the buttons and returns a <code>JScrollPane</code> that
	 * contains the buttons. The <code>JScrollPane</code> allows the user to
	 * reach all the buttons even if the window becomes very narrow. It thus
	 * allows the user to make this part of the <code>JSplitPane</code> smaller.
	 *
	 * @return the <code>JComponent</code> that contains all the buttons.
	 */
	protected JComponent initButtons() {
		final JPanel q = new JPanel();
		q.setLayout(new BoxLayout(q, BoxLayout.Y_AXIS));

		myMessage = new JTextArea();
		myMessage.setEditable(false);
		q.add(myMessage);

		final JPanel topRow = new JPanel();
		final JPanel bottomRow = new JPanel();

		myAddCase = new JButton("Add");
		myAddCase.addActionListener(e -> addCase());
		myAddCase.setToolTipText("Add the current case to the list");
		myAddCase.setEnabled(false);
		topRow.add(myAddCase);

		myReplace = new JButton("Replace");
		myReplace.addActionListener(e -> replaceCase(myTable.getSelectedRow()));
		myReplace.setToolTipText("Replace the selected case with the current case");
		myReplace.setEnabled(false);
		topRow.add(myReplace);

		myShowAll = new JButton("List");
		myShowAll.addActionListener(e -> listAll());
		myShowAll.setEnabled(false);
		myShowAll.setToolTipText("List all possible cases");
		bottomRow.add(myShowAll);

		myShowCase = new JButton("Show");
		myShowCase.addActionListener(e -> showCase(myTable.getSelectedRow()));
		myShowCase.setEnabled(false);
		myShowCase.setToolTipText("Display the selected case");
		topRow.add(myShowCase);

		myClearCase = new JButton("Delete");
		myClearCase.addActionListener(e -> clearCase(myTable.getSelectedRow()));
		myClearCase.setEnabled(false);
		myClearCase.setToolTipText("Delete the selected case");
		topRow.add(myClearCase);

		myClearAll = new JButton("Clear");
		myClearAll.addActionListener(e -> clearAll());
		myClearAll.setToolTipText("Clear all cases");
		bottomRow.add(myClearAll);

		myDone = new JButton("Done?");
		myDone.addActionListener(e -> {
			final int numLeft = myLemma.numCasesTotal() - myCases.size();
			if (numLeft == 1) {
				myMessage.setText("1 case left.");
			} else if (numLeft > 1) {
				myMessage.setText(numLeft + " cases left.");
			} else {
				myMessage.setText("All cases done.");
			}
		});
		myDone.setToolTipText("Check if all cases are done");
		bottomRow.add(myDone);

		/*
		 * Enables and disables myShowCase and myClearCase depending on whether
		 * there is a selection.
		 */
		myTable.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) {
				return;
			}

			final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			if (lsm.isSelectionEmpty()) {
				myShowCase.setEnabled(false);
				myClearCase.setEnabled(false);
				myReplace.setEnabled(false);
			} else {
				myShowCase.setEnabled(true);
				myClearCase.setEnabled(true);
				myReplace.setEnabled(myAddCase.isEnabled());
			}
		});

		q.add(topRow);
		q.add(bottomRow);
		final JScrollPane r = new JScrollPane(q);
		return r;
	}

	/**
	 * Initializes the <code>JTable</code> to display the different cases and
	 * returns the <code>JScrollPane</code> it is in. Using a
	 * <code>JScrollPane</code> allows us to scroll through the cases if there
	 * are too many.
	 *
	 * @return the <code>JComponent</code> that contains the table
	 */
	protected JComponent initTable() {
		myTable = new JTable(new AbstractTableModel() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;
			public final List<String> COLUMN_NAMES = Lists.newArrayList("#", "Description");

			@Override
			public int getColumnCount() {
				return COLUMN_NAMES.size();
			}

			@Override
			public String getColumnName(final int c) {
				return COLUMN_NAMES.get(c);
			}

			@Override
			public int getRowCount() {
				return myCases.size();
			}

			@Override
			public Object getValueAt(final int r, final int c) {
				if (c == 0) {
					return Integer.toString(r + 1);
				} else {
					return myCases.get(r);
				}
			}

			@Override
			public boolean isCellEditable(final int r, final int c) {
				return false;
			}

			@Override
			public void setValueAt(final Object value, final int r, final int c) {
			}
		});

		/*
		 * Only allow selecting one row at the time.
		 */
		final ListSelectionModel m = myTable.getSelectionModel();
		m.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		/*
		 * First column with numbers should not be too wide.
		 */
		final TableColumn c = myTable.getColumnModel().getColumn(0);
		c.setMaxWidth(25);

		final JScrollPane p = new JScrollPane(myTable);
		return p;
	}

	/**
	 * Shows all the cases.
	 *
	 */
	protected void listAll() {
		if (myCases.size() == myLemma.numCasesTotal()) {
			myMessage.setText("All cases for m = " + myLemma.getM() + " are already shown.");
		} else {
			myLemma.doAll();
			myMessage.setText("All cases for m = " + myLemma.getM() + " shown.");
			refresh();
		}
	}

	/**
	 * Refreshes the panel.
	 *
	 */
	public void refresh() {
		myCases = myLemma.getDoneDescriptions();
		/*
		 * Let the table know that data has changed.
		 */
		((AbstractTableModel) myTable.getModel()).fireTableDataChanged();

		if (myCases.size() == 0) {
			myDone.setEnabled(false);
			myClearAll.setEnabled(false);
		} else {
			myDone.setEnabled(true);
			myClearAll.setEnabled(true);
		}
		repaint();
	}

	/**
	 * Replaces a case.
	 *
	 * @param index
	 *            the position of the case to replace
	 */
	protected void replaceCase(final int index) {
		if (myLemma.replaceCase(tempDecomposition, tempI, index)) {
			myMessage.setText("Case #" + (index + 1) + " replaced.");
		} else {
			myMessage.setText("Wrong case selected.");
		}
	}

	/**
	 * Set the ability to add a case or replace an old one.
	 *
	 * @param b
	 *            the value we wish to set it to
	 */
	public void setAddReplaceButtonsEnabled(final boolean b) {
		myAddCase.setEnabled(b);
		if (myShowCase.isEnabled()) {
			myReplace.setEnabled(b);
		}
	}

	/**
	 * "Remembers" the most recent decomposition so that we can add it at a
	 * later time.
	 *
	 * @param decomposition
	 *            the decomposition we wish to remember
	 * @see #addCase()
	 */
	protected void setDecomposition(final List<Integer> decomposition) {
		tempDecomposition = decomposition;
	}

	/**
	 * "Remembers" the most recent <i>i</i> so that we can use it at a later
	 * time.
	 *
	 * @param i
	 *            the <i>i</i> we wish to remember
	 * @see #addCase()
	 */
	protected void setI(final int i) {
		tempI = i;
	}

	/**
	 * Set the ability to show all cases. Not enabled for
	 * <code>ComputerFirstPane</code> instances because preset list values could
	 * conflict with the user input <i>w</i> value.
	 *
	 * @param b
	 *            the value we wish to set it to
	 */
	public void setListButtonEnabled(final boolean b) {
		myShowAll.setEnabled(b);
	}

	/**
	 * Set the message displayed.
	 *
	 * @param message
	 *            the message we wish to display
	 */
	protected void setMessage(final String message) {
		myMessage.setText(message);
	}

	/**
	 * Displays the decomposition of the case selected in the
	 * <code>PumpingLemmaInputPane</code>.
	 *
	 * @param i
	 *            the case we wish to display
	 * @see edu.duke.cs.jflap.gui.pumping.PumpingLemmaInputPane#setDecomposition(int[])
	 */
	protected void showCase(final int i) {
		try {
			final Case c = myLemma.getCase(i);

			if (c.getI() != -1) {
				myPane.setDecomposition(c.getInput(), c.getI());
				myPane.displayIEnd();
				myPane.setVisibilityStages(4, true);
				myPane.setCanvas();
				setAddReplaceButtonsEnabled(true);
			} else {
				myPane.setDecomposition(c.getInput());
				myPane.setVisibilityStages(4, false);
				setAddReplaceButtonsEnabled(false);
			}
			refresh();
			/*
			 * Highlight (select) the case shown.
			 */
			myTable.getSelectionModel().setSelectionInterval(i, i);
			myMessage.setText("Showing case #" + (i + 1) + ".");

		} catch (final ArrayIndexOutOfBoundsException e) {
			/*
			 * This should not happen because the button should be disabled if
			 * there is no selection.
			 */
			myMessage.setText("Please select a case to show.");
		}
	}
}
