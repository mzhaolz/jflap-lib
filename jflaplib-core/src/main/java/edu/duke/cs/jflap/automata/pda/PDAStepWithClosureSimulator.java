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

package edu.duke.cs.jflap.automata.pda;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.ClosureTaker;
import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;

import java.util.ArrayList;
import java.util.List;

public class PDAStepWithClosureSimulator extends PDAStepByStateSimulator {

  public PDAStepWithClosureSimulator(Automaton automaton) {
    super(automaton);
  }

  /**
   * Returns a PDAConfiguration array that represents the initial
   * configuration of the PDA, before any input has been processed. It returns
   * an array of length one.
   *
   * @param input
   *            the input string.
   */
  @Override
public List<Configuration> getInitialConfigurations(String input) {
    /** The stack should contain the bottom of stack marker. */
    State init = myAutomaton.getInitialState();
    List<State> closure = ClosureTaker.getClosure(init, myAutomaton);
    List<Configuration> configs = new ArrayList<>();
    for (int k = 0; k < closure.size(); k++) {
      CharacterStack stack = new CharacterStack();
      stack.push("Z");
      configs.add(new PDAConfiguration(closure.get(k), null, input, input, stack, myAcceptance));
    }
    return configs;
  }

  /**
   * Simulates one step for a particular configuration, adding all possible
   * configurations reachable in one step to set of possible configurations.
   *
   * @param config
   *            the configuration to simulate the one step on
   */
  @Override
public ArrayList<PDAConfiguration> stepConfiguration(Configuration config) {
    ArrayList<PDAConfiguration> list = new ArrayList<PDAConfiguration>();
    PDAConfiguration configuration = (PDAConfiguration) config;
    /** get all information from configuration. */
    String unprocessedInput = configuration.getUnprocessedInput();
    String totalInput = configuration.getInput();
    State currentState = configuration.getCurrentState();
    List<Transition> transitions = myAutomaton.getTransitionsFromState(currentState);
    for (int k = 0; k < transitions.size(); k++) {
      PDATransition transition = (PDATransition) transitions.get(k);
      /** get all information from transition. */
      String inputToRead = transition.getInputToRead();
      String stringToPop = transition.getStringToPop();
      CharacterStack tempStack = configuration.getStack();
      /** copy stack object so as to not alter original. */
      CharacterStack stack = new CharacterStack(tempStack);
      String stackContents = stack.pop(stringToPop.length());
      if (unprocessedInput.startsWith(inputToRead) && stringToPop.equals(stackContents)) {
        String input = "";
        if (inputToRead.length() < unprocessedInput.length()) {
          input = unprocessedInput.substring(inputToRead.length());
        }
        State toState = transition.getToState();
        stack.push(transition.getStringToPush());
        List<State> closure = ClosureTaker.getClosure(toState, myAutomaton);
        for (int i = 0; i < closure.size(); i++) {
          CharacterStack cstack = new CharacterStack(stack);
          PDAConfiguration configurationToAdd =
              new PDAConfiguration(
                  closure.get(i), configuration, totalInput, input, cstack, myAcceptance);
          list.add(configurationToAdd);
        }
      }
    }
    return list;
  }
}
