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

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * A <CODE>ConvertController</CODE> object is a controller used in the
 * conversion of a grammar to some sort of automaton. It monitors both the
 * grammar and the automaton being built, as well as their respective views. It
 * intervenes as necessary, and as it is a controller object moderates between
 * the views, and the automaton and grammar.
 *
 * @see edu.duke.cs.jflap.grammar.Grammar
 * @see automaton.Automaton
 * @see edu.duke.cs.jflap.gui.grammar.GrammarView
 * @see edu.duke.cs.jflap.gui.viewer.SelectionDrawer
 *
 * @author Thomas Finley
 */
class ConvertController {
	/** The grammar view. */
	protected GrammarViewer grammarView;

	/** The automaton drawer. */
	protected SelectionDrawer drawer;

	/** The grammar. */
	protected Grammar grammar;

	/** The automaton. */
	protected Automaton automaton;

	/** The map of productions to transitions the user should come up with. */
	protected Map<Production, Transition> pToT;

	/** The map of transitions to productions. */
	protected Map<Transition, Production> tToP;

	/**
	 * The set of productions whose transitions have already been added.
	 */
	protected Set<Production> alreadyDone = new HashSet<>();

	/** The parent component. */
	protected Component parent;

	/**
	 * Instantiates a <CODE>ConvertController</CODE> object.
	 *
	 * @param grammarView
	 *            the grammar view
	 * @param drawer
	 *            the automaton selection drawer
	 * @param productionsToTransitions
	 *            a map from productions to the corresponding transitions the
	 *            user should come up with... this maping must be one-to-one
	 * @param parent
	 *            some parent object so that the controller knows where to put
	 *            its message boxes, which may be null
	 */
	public ConvertController(final GrammarViewer grammarView, final SelectionDrawer drawer,
			final Map<Production, Transition> productionsToTransitions, final Component parent) {
		this.grammarView = grammarView;
		this.drawer = drawer;
		this.parent = parent;
		grammar = grammarView.getGrammar();
		automaton = drawer.getAutomaton();

		initListeners();
		pToT = productionsToTransitions;
		tToP = invert(pToT);
	}

	/**
	 * Puts all of the remaining uncreated transitions into the automaton.
	 */
	public void complete() {
		final Collection<?> productions = new HashSet<Object>(pToT.keySet());
		final Iterator<?> it = productions.iterator();
		while (it.hasNext()) {
			final Production p = (Production) it.next();
			if (alreadyDone.contains(p)) {
				continue;
			}
			final Transition t = pToT.get(p);
			automaton.addTransition(t);
		}
	}

	/**
	 * Puts all of the transitions for the selected productions in the
	 * automaton.
	 */
	public void createForSelected() {
		final List<Production> p = grammarView.getSelected();
		for (int i = 0; i < p.size(); i++) {
			if (alreadyDone.contains(p.get(i))) {
				continue;
			}
			final Transition t = pToT.get(p.get(i));
			automaton.addTransition(t);
		}
	}

	/**
	 * If the conversion is done, this takes the automaton and makes it editable
	 * in a new window.
	 */
	public void export() {
		final boolean done = (pToT.size() - alreadyDone.size()) == 0;
		if (!done) {
			javax.swing.JOptionPane.showMessageDialog(parent, "The conversion is not completed yet!");
			return;
		}
		final Automaton toExport = automaton.clone();
		edu.duke.cs.jflap.gui.environment.FrameFactory.createFrame(toExport);
	}

	/**
	 * This initializes the listeners to the GUI objects, as well as the
	 * automata.
	 */
	private void initListeners() {
		automaton.addTransitionListener(e -> {
			if (!e.isAdd()) {
				return;
			}
			final Transition transition = e.getTransition();
			if (!tToP.containsKey(transition) || alreadyDone.contains(tToP.get(transition))) {
				javax.swing.JOptionPane.showMessageDialog(parent, "That transition is not correct!");
				automaton.removeTransition(transition);
			} else {
				final Production p = tToP.get(transition);
				alreadyDone.add(p);
				grammarView.setChecked(p, true);
			}
		});

		grammarView.addSelectionListener(event -> {
			final List<Production> p = grammarView.getSelected();
			drawer.clearSelected();
			for (int i = 0; i < p.size(); i++) {
				drawer.addSelected(pToT.get(p.get(i)));
			}
			parent.repaint();
		});
	}

	/**
	 * Returns a map containing the inverse of the passed in map.
	 *
	 * @param map
	 *            the map, which should be one to one
	 * @return the inverse of the passed in map, or <CODE>null</CODE> if an
	 *         error occurred
	 */
	private Map<Transition, Production> invert(final Map<Production, Transition> map) {
		// TODO: Guavify this
		final Map<Transition, Production> inverse = new HashMap<>();
		for (final Production prods : map.keySet()) {
			inverse.put(map.get(prods), prods);
		}
		return inverse;
	}

	/**
	 * Displays and returns if the automaton is done yet.
	 *
	 * @return <CODE>true</CODE> if the automaton is done, <CODE>false</CODE> if
	 *         it is not
	 */
	public boolean isDone() {
		final int toDo = pToT.size() - alreadyDone.size();
		final String message = toDo == 0 ? "The conversion is finished!"
				: toDo + " more transition" + (toDo == 1 ? "" : "s") + " must be added.";
		javax.swing.JOptionPane.showMessageDialog(parent, message);

		return toDo == 0;
	}
}
