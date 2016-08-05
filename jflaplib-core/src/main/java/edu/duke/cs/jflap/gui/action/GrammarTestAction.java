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
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;
import java.awt.event.ActionEvent;

/**
 * This is a simple test action for grammars.
 *
 * @author Thomas Finley
 */
public class GrammarTestAction extends GrammarAction {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new <CODE>GrammarOutputAction</CODE>.
   *
   * @param environment
   *            the grammar environment
   */
  public GrammarTestAction(GrammarEnvironment environment) {
    super("Grammar Test", null);
    this.environment = environment;
  }

  /**
   * Performs the action.
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    Grammar g = environment.getGrammar(UnrestrictedGrammar.class);
    if (g == null) return;
    ChomskyPane cp = new ChomskyPane(environment, g);
    environment.add(cp, "Test", new CriticalTag() {});
    environment.setActive(cp);
  }

  /** The grammar environment. */
  private GrammarEnvironment environment;
}
