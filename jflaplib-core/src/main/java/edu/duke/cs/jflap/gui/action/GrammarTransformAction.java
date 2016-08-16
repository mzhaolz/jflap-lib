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

package edu.duke.cs.jflap.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import edu.duke.cs.jflap.grammar.CNFConverter;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.LambdaProductionRemover;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.UnitProductionRemover;
import edu.duke.cs.jflap.grammar.UselessProductionRemover;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;
import edu.duke.cs.jflap.gui.grammar.transform.ChomskyPane;
import edu.duke.cs.jflap.gui.grammar.transform.LambdaPane;
import edu.duke.cs.jflap.gui.grammar.transform.UnitPane;
import edu.duke.cs.jflap.gui.grammar.transform.UselessPane;

/**
 * This is an action to transform a grammar.
 *
 * @author Thomas Finley
 */
public class GrammarTransformAction extends GrammarAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static void hypothesizeChomsky(final GrammarEnvironment env, final Grammar g) {
		CNFConverter converter = null;
		try {
			converter = new CNFConverter(g);
		} catch (final IllegalArgumentException e) {
			JOptionPane.showMessageDialog(env, e.getMessage(), "Illegal Grammar", JOptionPane.ERROR_MESSAGE);
			return;
		}
		final List<Production> p = g.getProductions();
		boolean chomsky = true;
		for (int i = 0; i < p.size(); i++) {
			chomsky &= converter.isChomsky(p.get(i));
		}
		if (chomsky) {
			JOptionPane.showMessageDialog(env, "The grammar is already in Chomsky NF.", "Already in CNF",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		final ChomskyPane cp = new ChomskyPane(env, g);
		env.add(cp, "Chomsky Converter", new CriticalTag() {
		});
		env.setActive(cp);
	}

	// Changed to support the Lambda removal for Grammar, which Start variable
	// derives Lambda.
	public static void hypothesizeLambda(final GrammarEnvironment env, final Grammar g) {
		final Set<?> lambdaDerivers = LambdaProductionRemover.getCompleteLambdaSet(g);
		if (lambdaDerivers.contains(g.getStartVariable())) {
			JOptionPane.showMessageDialog(env,
					"WARNING : The start variable derives lambda.\n" + "New Grammar will not produce lambda String.",
					"Start Derives Lambda", JOptionPane.ERROR_MESSAGE);
		}
		if (lambdaDerivers.size() > 0) {
			final LambdaPane lp = new LambdaPane(env, g);
			env.add(lp, "Lambda Removal", new CriticalTag() {
			});
			env.setActive(lp);
			return;
		}
		hypothesizeUnit(env, g);
	}

	public static void hypothesizeUnit(final GrammarEnvironment env, final Grammar g) {
		if (UnitProductionRemover.getUnitProductions(g).size() > 0) {
			final UnitPane up = new UnitPane(env, g);
			env.add(up, "Unit Removal", new CriticalTag() {
			});
			env.setActive(up);
			return;
		}
		hypothesizeUseless(env, g);
	}

	public static void hypothesizeUseless(final GrammarEnvironment env, final Grammar g) {
		/*
		 * Grammar g2 = remover.getUselessProductionlessGrammar(g); if
		 * (g2.getProductions().length < g.getProductions().length) {
		 * UselessPane up = new UselessPane(env, g); env.add(up, "Useless
		 * Removal", new CriticalTag() {}); env.setActive(up); return; }
		 * hypothesizeChomsky(env, g);
		 */
		final Grammar g2 = UselessProductionRemover.getUselessProductionlessGrammar(g);
		final List<Production> p1 = g.getProductions();
		final List<Production> p2 = g2.getProductions();
		if (p1.size() > p2.size()) {
			final UselessPane up = new UselessPane(env, g);
			env.add(up, "Useless Removal", new CriticalTag() {
			});
			env.setActive(up);
			return;
		}
		hypothesizeChomsky(env, g);
	}

	/** The grammar environment. */
	private final GrammarEnvironment environment;

	/**
	 * Instantiates a new <CODE>GrammarTransformAction</CODE>.
	 *
	 * @param environment
	 *            the grammar environment
	 */
	public GrammarTransformAction(final GrammarEnvironment environment) {
		super("Transform Grammar", null);
		this.environment = environment;
	}

	/**
	 * Performs the action.
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final Grammar g = environment.getGrammar();
		if (g == null) {
			return;
		}
		hypothesizeLambda(environment, g);
	}
}
