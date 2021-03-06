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

package edu.duke.cs.jflap.gui.lsystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;

import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.UnboundGrammar;
import edu.duke.cs.jflap.grammar.lsystem.LSystem;
import edu.duke.cs.jflap.gui.HighlightTable;
import edu.duke.cs.jflap.gui.TableTextSizeSlider;
import edu.duke.cs.jflap.gui.grammar.GrammarInputPane;

/**
 * The <CODE>LSystemInputPane</CODE> is a pane used to input and display the
 * textual representation of an L-system.
 *
 * @author Thomas Finley
 */
public class LSystemInputPane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** An empty L-system. */
	private static final LSystem SYSTEM = new LSystem();

	/**
	 * Given a list of objects, this converts it to a space delimited string.
	 *
	 * @param list
	 *            the list to convert to a string
	 * @return a string containing the elements of the list
	 */
	public static String listAsString(final java.util.List<?> list) {
		final Iterator<?> it = list.iterator();
		if (!it.hasNext()) {
			return "";
		}
		final StringBuffer sb = new StringBuffer();
		sb.append(it.next());
		while (it.hasNext()) {
			sb.append(' ');
			sb.append(it.next());
		}
		return sb.toString();
	}

	/** The axiom text field. */
	private JTextField axiomField;

	/** The production view. */
	private GrammarInputPane productionInputPane;

	/** The parameter table model. */
	private ParameterTableModel parameterModel;

	/** The parameter table view. */
	private HighlightTable parameterTable;

	/** The set of input listeners. */
	private final Set<LSystemInputListener> lSystemInputListeners = new HashSet<>();

	/** The event reused in firing off the notifications. */
	private final LSystemInputEvent reusedEvent = new LSystemInputEvent(this);

	/** The cached L-system. Firing an L-S input event invalidates this. */
	private LSystem cachedSystem = null;

	/**
	 * Instantiates an empty <CODE>LSystemInputPane</CODE>.
	 */
	public LSystemInputPane() {
		this(SYSTEM);
	}

	/**
	 * Instantiates an <CODE>LSystemInputPane</CODE> for a given
	 * <CODE>LSystem</CODE>.
	 *
	 * @param lsystem
	 *            the lsystem to display
	 */
	public LSystemInputPane(final LSystem lsystem) {
		super(new BorderLayout());
		initializeStructures(lsystem);
		initializeListener();
		initializeView();
	}

	/**
	 * Adds an L-system input listener.
	 *
	 * @param listener
	 *            the listener to start sending change events to
	 */
	public void addLSystemInputListener(final LSystemInputListener listener) {
		lSystemInputListeners.add(listener);
	}

	/**
	 * Fires a notification to listeners that the L-system was changed.
	 */
	protected void fireLSystemInputEvent() {
		cachedSystem = null;
		final Iterator<LSystemInputListener> it = lSystemInputListeners.iterator();
		while (it.hasNext()) {
			(it.next()).lSystemChanged(reusedEvent);
		}
	}

	/**
	 * Returns the L-system this pane displays.
	 *
	 * @return the L-system this pane displays
	 */
	public LSystem getLSystem() {
		// Make sure we're not editing anything.
		if (productionInputPane.getTable().getCellEditor() != null) {
			productionInputPane.getTable().getCellEditor().stopCellEditing();
		}
		if (parameterTable.getCellEditor() != null) {
			parameterTable.getCellEditor().stopCellEditing();
		}
		// Do we already have a cached copy?
		try {
			if (cachedSystem == null) {
				cachedSystem = new LSystem(axiomField.getText(), productionInputPane.getGrammar(UnboundGrammar.class),
						parameterModel.getParameters());
			}
		} catch (final IllegalArgumentException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "L-System Error", JOptionPane.ERROR_MESSAGE);
		}
		return cachedSystem;
	}

	/**
	 * Creates the listener to update the edited-ness.
	 */
	public void initializeListener() {
		axiomField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(final DocumentEvent e) {
				fireLSystemInputEvent();
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				fireLSystemInputEvent();
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				fireLSystemInputEvent();
			}
		});
		final TableModelListener tml = e -> fireLSystemInputEvent();
		parameterModel.addTableModelListener(tml);
		productionInputPane.getTable().getModel().addTableModelListener(tml);
	}

	/**
	 * Initializes the data structures and the subviews.
	 *
	 * @param lsystem
	 *            the L-system to initialize the views on
	 */
	private void initializeStructures(final LSystem lsystem) {
		// Create the axiom text field.
		axiomField = new JTextField(listAsString(lsystem.getAxiom()));
		// Create the grammar view that holds replacement productions.
		final Set<?> replacements = lsystem.getSymbolsWithReplacements();
		final Iterator<?> it = replacements.iterator();
		final UnboundGrammar g = new UnboundGrammar();
		while (it.hasNext()) {
			final String symbol = (String) it.next();
			final List<List<String>> r = lsystem.getReplacements(symbol);
			for (int i = 0; i < r.size(); i++) {
				final Production p = new Production(symbol, listAsString(r.get(i)));
				g.addProduction(p);
			}
		}
		productionInputPane = new GrammarInputPane(g);
		// Create the parameter table model.
		parameterModel = new ParameterTableModel(lsystem.getValues());
		// We may as well use this as our cached system.
		cachedSystem = lsystem;
	}

	/**
	 * Lays out the subviews in this view.
	 */
	private void initializeView() {
		// Create the view for the axiom text field.
		final JPanel axiomView = new JPanel(new BorderLayout());
		axiomView.add(new JLabel("Axiom: "), BorderLayout.WEST);
		axiomView.add(axiomField, BorderLayout.CENTER);
		add(axiomView, BorderLayout.NORTH);
		// Create the view for the grammar pane and the rest.
		parameterTable = new HighlightTable(parameterModel);
		parameterTable.add(new TableTextSizeSlider(parameterTable), BorderLayout.SOUTH);
		final JScrollPane scroller = new JScrollPane(parameterTable);
		final Dimension bestSize = new Dimension(400, 200);
		/*
		 * parameterTable.setPreferredSize(bestSize);
		 * productionInputPane.getTable().setPreferredSize(bestSize);
		 */
		productionInputPane.setPreferredSize(bestSize);
		scroller.setPreferredSize(bestSize);
		final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, productionInputPane, scroller);
		add(split, BorderLayout.CENTER);
		// Finally, show the grid.
		parameterTable.setShowGrid(true);
		parameterTable.setGridColor(Color.lightGray);

		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		final JPopupMenu menu = new JPopupMenu();
		final ActionListener listener = e -> setEditing(e.getActionCommand());
		final Set<String> words = Renderer.ASSIGN_WORDS;
		for (final String s : words) {
			menu.add(s).addActionListener(listener);
		}
		final JPanel c = new JPanel();
		c.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				menu.show((Component) e.getSource(), e.getPoint().x, e.getPoint().y);
			}
		});
		scroller.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, c);
	}

	/**
	 * Removes an L-system input listener.
	 *
	 * @param listener
	 *            the listener to stop sending change events to
	 */
	public void removeLSystemInputListener(final LSystemInputListener listener) {
		lSystemInputListeners.remove(listener);
	}

	/**
	 * This will edit the value for a particular parameter in the parameter
	 * table. If no such value exists yet it shall be created. The value field
	 * in the table shall be edited.
	 *
	 * @param item
	 *            the key of the value we want to edit
	 */
	private void setEditing(final String item) {
		int i;
		for (i = 0; i < parameterModel.getRowCount(); i++) {
			if (parameterModel.getValueAt(i, 0).equals(item)) {
				break;
			}
		}
		if (i == parameterModel.getRowCount()) {
			parameterModel.setValueAt(item, --i, 0);
		}
		final int column = parameterTable.convertColumnIndexToView(1);
		parameterTable.editCellAt(i, column);
		parameterTable.requestFocus();
	}
}
