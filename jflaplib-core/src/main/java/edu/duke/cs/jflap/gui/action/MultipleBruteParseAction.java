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

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.UnrestrictedGrammar;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;

import java.awt.Component;
import java.awt.event.ActionEvent;

/**
 * This action creates a new brute force parser for the grammar.
 *
 * @author Thomas Finley
 */
public class MultipleBruteParseAction extends GrammarAction {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new <CODE>BruteParseAction</CODE>.
   *
   * @param environment
   *            the grammar environment
   */
  public MultipleBruteParseAction(GrammarEnvironment environment) {
    super("Multiple Brute Force Parse", null);
    this.environment = environment;
  }

  /**
   * Performs the action.
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    performAction((Component) e.getSource());
  }

  public void performAction(Component source) {
    Grammar g = environment.getGrammar(UnrestrictedGrammar.class);
    if (g == null) return;
    MultipleSimulateAction mult = new MultipleSimulateAction(g, environment);
    mult.performAction(source);
  }

  /** The grammar environment. */
  private GrammarEnvironment environment;
}
