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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.grammar.GrammarTable;
import edu.duke.cs.jflap.gui.grammar.GrammarTableModel;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * The <CODE>ConvertController</CODE> abstract class handles the operation to
 * convert an automaton to a PDA. At its most basic level, it simply maps
 * objects in the automaton to a set of productions in a grammar.
 *
 * @author Thomas Finley
 */
public abstract class ConvertController {
	/**
	 * The mapping of either states or transitions to an array of productions.
	 * If there are no productions for an object, the map will not contain the
	 * key.
	 */
	protected HashMap<Serializable, List<Production>> objectToProduction = new HashMap<>();

	/**
	 * The mapping of productions to whatever object they correspond to, which
	 * will be either a state or a transition.
	 */
	protected HashMap<Production, Serializable> productionToObject = new HashMap<>();

	/** Which objects have already been added? */
	protected HashSet<Object> alreadyDone = new HashSet<>();

	/**
	 * The convert pane that holds the automaton pane and the grammar table.
	 */
	protected ConvertPane convertPane;

	/** The automaton we're converting. */
	private final Automaton automaton;

	/** The selection drawer. */
	private final SelectionDrawer drawer;

	/** The grammar table where the productions are stored. */
	private final GrammarTable table;

	/**
	 * Instantiates a <CODE>ConvertController</CODE> for an automaton.
	 *
	 * @param pane
	 *            the convert pane that holds the automaton pane and the grammar
	 *            table
	 * @param drawer
	 *            the selection drawer where the automaton is made
	 * @param automaton
	 *            the automaton to build the <CODE>ConvertController</CODE> for;
	 *            this automaton should be editable
	 * @see #fillMap
	 */
	public ConvertController(final ConvertPane pane, final SelectionDrawer drawer, final Automaton automaton) {
		convertPane = pane;
		this.automaton = automaton;
		table = pane.getTable();
		this.drawer = drawer;

		table.getSelectionModel().addListSelectionListener(e -> changeSelection());
	}

	/**
	 * Adds productions to the grammar pane, and makes them selected.
	 *
	 * @param productions
	 *            the collection that holds productions to add
	 */
	private void addProductions(final Collection<Production> productions) {
		final Iterator<Production> it = productions.iterator();
		if (!it.hasNext()) {
			return;
		}
		final GrammarTableModel model = table.getGrammarModel();
		int min = 1000000000, max = 0;
		while (it.hasNext()) {
			final Production p = it.next();
			final int row = model.addProduction(p);
			min = Math.min(min, row);
			max = Math.max(max, row);
		}
		table.setRowSelectionInterval(min, max);
	}

	/**
	 * Changes the selection in the automaton selection pane. This method is
	 * usually called as a result of the list selection changing.
	 */
	protected void changeSelection() {
		final ListSelectionModel model = table.getSelectionModel();
		int min = model.getMinSelectionIndex();
		final int max = model.getMaxSelectionIndex();
		drawer.clearSelected();
		if (min == -1) {
			convertPane.getAutomatonPane().repaint();
			return;
		}
		for (; min <= max; min++) {
			if (!model.isSelectedIndex(min)) {
				continue;
			}
			final Production p = table.getGrammarModel().getProduction(min);
			final Object o = productionToObject.get(p);
			if (o == null) {
				continue;
			}
			if (o instanceof State) {
				drawer.addSelected((State) o);
			} else {
				drawer.addSelected((Transition) o);
			}
		}
		convertPane.getAutomatonPane().repaint();
	}

	/**
	 * Exports the grammar defined to a new window, or fails if the grammar is
	 * not yet converted.
	 *
	 * @return the grammar that was converted as returned by
	 *         <CODE>getGrammar</CODE>; if there was an error in
	 *         <CODE>getGrammar</CODE> or if the conversion is unfinished,
	 *         <CODE>null</CODE> is returned
	 * @see #getGrammar
	 */
	public Grammar exportGrammar() {
		// Are any yet unconverted?
		if (objectToProduction.keySet().size() != alreadyDone.size()) {
			highlightUntransformed();
			JOptionPane.showMessageDialog(convertPane, "Conversion unfinished!  Objects to convert are highlighted.",
					"Conversion Unfinished", JOptionPane.ERROR_MESSAGE);
			changeSelection();
			return null;
		}
		try {
			final Grammar g = getGrammar();
			FrameFactory.createFrame(g);
			return g;
		} catch (final GrammarCreationException e) {
			JOptionPane.showMessageDialog(convertPane, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * Fills the maps. This method should be called by subclasses after the
	 * constructor, whenever the controller is ready to produce the productions.
	 */
	protected void fillMap() {
		final List<State> states = automaton.getStates();
		for (final State si : states) {
			final List<Production> prods = getProductions(si);
			if (prods.size() == 0) {
				continue;
			}
			objectToProduction.put(si, prods);
			for (int j = 0; j < prods.size(); j++) {
				productionToObject.put(prods.get(j), si);
			}
		}
		// Now let's get the other cannon!
		final List<Transition> transitions = automaton.getTransitions();
		for (final Transition transi : transitions) {
			final List<Production> prods = getProductions(transi);
			if (prods.size() == 0) {
				continue;
			}
			objectToProduction.put(transi, prods);
			for (int j = 0; j < prods.size(); j++) {
				productionToObject.put(prods.get(j), transi);
			}
		}
	}

	/**
	 * Returns the <CODE>Automaton</CODE>.
	 *
	 * @return the <CODE>Automaton</CODE> for this controller
	 */
	protected Automaton getAutomaton() {
		return automaton;
	}

	/**
	 * Returns the grammar that resulted from the conversion. This method should
	 * only be called once the conversion has finished.
	 *
	 * @return the grammar that resulted from the conversion
	 * @throws GrammarCreationException
	 *             if there is some impediment to the creation of the grammar
	 */
	protected abstract Grammar getGrammar();

	/**
	 * Returns the table model for the grammar table.
	 *
	 * @return the table model for the grammar table
	 */
	protected GrammarTableModel getModel() {
		return table.getGrammarModel();
	}

	/**
	 * Returns the productions for a particular state. This method will only be
	 * called once.
	 *
	 * @param state
	 *            the state to get the productions for
	 * @return an array containing the productions that correspond to a
	 *         particular state
	 */
	protected List<Production> getProductions(final State state) {
		return new ArrayList<>();
	}

	/**
	 * Returns the productions for a particular transition. This method will
	 * only be called once.
	 *
	 * @param transition
	 *            the transition to get the productions for
	 * @return an array containing the productions that correspond to a
	 *         particular transition
	 */
	protected List<Production> getProductions(final Transition transition) {
		return new ArrayList<>();
	}

	/**
	 * This method sets all objects that may be tranformed to productions and as
	 * yet have been unselected to be selected.
	 *
	 * @return an array of the objects which as yet have not been transformed
	 */
	public List<Serializable> highlightUntransformed() {
		final HashSet<Serializable> unselectedSet = new HashSet<>(objectToProduction.keySet());
		unselectedSet.removeAll(alreadyDone);
		final List<Serializable> unselected = new ArrayList<>(unselectedSet);
		drawer.clearSelected();
		for (final Serializable s : unselected) {
			if (s instanceof State) {
				drawer.addSelected((State) s);
			} else {
				drawer.addSelected((Transition) s);
			}
		}
		convertPane.getAutomatonPane().repaint();
		return unselected;
	}

	/**
	 * This will reveal all the productions for all objects remaining to be
	 * revealed.
	 *
	 * @return the number of objects revealed
	 */
	public int revealAllProductions() {
		final Set<Serializable> remaining = new HashSet<>(objectToProduction.keySet());
		remaining.removeAll(alreadyDone);
		final int number = remaining.size();
		final Iterator<Serializable> it = remaining.iterator();
		final Collection<Production> ps = new ArrayList<>();
		while (it.hasNext()) {
			final List<Production> p = objectToProduction.get(it.next());
			ps.addAll(p);
		}
		addProductions(ps);
		alreadyDone.addAll(remaining);
		return number;
	}

	/**
	 * This method reveals the productions for a particular object, whether it
	 * be a state or transition.
	 *
	 * @param object
	 *            the object whose productions we should reveal
	 * @return a non-empty array of productions revealed, <CODE>null</CODE> if
	 *         there are no productions for this object, or an empty array if
	 *         there are productions for this object and they have already been
	 *         revealed
	 */
	public List<Production> revealObjectProductions(final Object object) {
		final List<Production> p = objectToProduction.get(object);
		if (p == null || p.size() == 0) {
			// There are no productions!
			JOptionPane.showMessageDialog(convertPane, "There are no productions for that object!");
			return null;
		}
		if (alreadyDone.contains(object)) {
			// Been there, done that.
			JOptionPane.showMessageDialog(convertPane, "This object has already been converted!");
			return new ArrayList<>();
		}
		alreadyDone.add(object);
		addProductions(p);
		return p;
	}

	/**
	 * This will reveal the productions for one object chosen at quasirandom
	 * (i.e. whatever comes first).
	 *
	 * @return the object whose productions were revealed, or <CODE>null</CODE>
	 *         if no object remains to have its productions revealed
	 */
	public Object revealRandomProductions() {
		final Iterator<Map.Entry<Serializable, List<Production>>> it = objectToProduction.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<Serializable, List<Production>> entry = it.next();
			final Serializable key = entry.getKey();
			if (alreadyDone.contains(key)) {
				continue;
			}
			final List<Production> p = objectToProduction.get(key);
			addProductions(p);
			alreadyDone.add(entry.getKey());
			return key;
		}
		return null;
	}
}
