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

package edu.duke.cs.jflap.gui.regular;

import java.awt.Point;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.fsa.FSAToRegularExpressionConverter;
import edu.duke.cs.jflap.automata.fsa.FSATransition;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;
import edu.duke.cs.jflap.regular.RegularExpression;

/**
 * This object monitors and guides the user actions in the conversion of an FSA
 * to a regular expression.
 *
 * @author Thomas Finley
 */
public class FSAToREController {
	/**
	 * The state IDs of each of the steps. Fine, this sucks. So sue me.
	 */
	private static final int CREATE_SINGLE_FINAL = 0, TRANSITIONS_TO_SINGLE_FINAL = 1, CONVERT_TRANSITIONS = 2,
			CREATE_EMPTY_TRANSITIONS = 3, COLLAPSE_STATES = 4, FINISHED = 200;

	/** The current step of the conversion process. */
	private int currentStep = -1;

	/** The automaton that's being converted. */
	private final FiniteStateAutomaton automaton;

	/** The selection drawer that the editor holds. */
	private final SelectionDrawer drawer;

	/** The main step label. */
	private final JLabel mainStep;

	/** The detail step label. */
	private final JLabel detailStep;

	/** The frame holding all this. */
	private final JFrame frame;

	/**
	 * The number of things left to do. This can be used by different steps.
	 */
	private int remaining = 0;

	/**
	 * The window holding the list of transitions for state collapsing.
	 */
	private TransitionWindow transitionWindow = null;

	/** The state last selected for state collapsing. */
	private State collapseState = null;

	/** The final answer, or null if not done. */
	private String computedRE = null;

	/**
	 * Instantiates a new <CODE>FSAToREController</CODE>.
	 *
	 * @param automaton
	 *            the automaton that is in the process of being converted
	 * @param drawer
	 *            the selection drawer in the editor
	 * @param mainStep
	 *            the label holding the description of the main step
	 * @param detailStep
	 *            the label holding the detail description of whatever the user
	 *            must do now
	 * @param frame
	 *            the window that this is all happening in
	 */
	public FSAToREController(final FiniteStateAutomaton automaton, final SelectionDrawer drawer, final JLabel mainStep,
			final JLabel detailStep, final JFrame frame) {
		this.automaton = automaton;
		this.drawer = drawer;
		this.mainStep = mainStep;
		this.detailStep = detailStep;
		this.frame = frame;

		nextStep();
	}

	/**
	 * For the collapsing of multiple transitions between states, this counts
	 * the number of collapses that must take place on the automaton before all
	 * possible ordered pairs of states have at most one transition from the
	 * first to the second. This method just counts the number of
	 * <CODE>(from,to)</CODE> pairs with more than one transition between them
	 *
	 * @return the number of collapses needed
	 */
	protected int collapsesNeeded() {
		final List<State> states = automaton.getStates();
		int needed = 0;
		for (final State si : states) {
			for (final State sj : states) {
				if (automaton.getTransitionsFromStateToState(si, sj).size() > 1) {
					needed++;
				}
			}
		}
		return needed;
	}

	/**
	 * For the creation of empty transitions between states, this counts the
	 * number of empty transitions needed.
	 *
	 * @return the number of empty transitions needed
	 */
	protected int emptyNeeded() {
		final List<State> states = automaton.getStates();
		int needed = 0;
		for (final State si : states) {
			for (final State sj : states) {
				if (automaton.getTransitionsFromStateToState(si, sj).size() == 0) {
					needed++;
				}
			}
		}
		return needed;
	}

	/**
	 * This will export the regular expression.
	 */
	public void export() {
		if (computedRE == null) {
			JOptionPane.showMessageDialog(frame, "The conversion has not yet finished.", "Not Finished",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		FrameFactory.createFrame(new RegularExpression(computedRE));
	}

	/**
	 * This will export the current automaton. Used for special purposes.
	 */
	void exportAutomaton() {
		final Environment e = ((EnvironmentFrame) frame).getEnvironment();
		final AutomatonPane a = new AutomatonPane(drawer);
		e.add(a, "Current FA");
		e.setActive(a);
	}

	/**
	 * This finalizes a state remove. This will remove whatever state was
	 * selected.
	 */
	public void finalizeStateRemove() {
		if (collapseState == null) {
			JOptionPane.showMessageDialog(frame, "A valid state has not been selected yet!", "No State Selected",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		FSAToRegularExpressionConverter.removeState(collapseState, transitionWindow.getTransitions(), automaton);
		remaining--;
		nextStep();
		collapseState = null;
		drawer.clearSelected();
		drawer.clearSelectedTransitions();
		// transitionWindow.setTransitions(new Transition[0]);
		transitionWindow.setVisible(false);
		transitionWindow.dispose();
		// transitionWindow.hide();
	}

	/**
	 * This will automatically perform the actions to move the conversion to the
	 * next step.
	 */
	public void moveNextStep() {
		switch (currentStep) {
		case CREATE_SINGLE_FINAL:
			JOptionPane.showMessageDialog(frame, "Just create a state.\nIt's not too difficult.", "Create the State",
					JOptionPane.ERROR_MESSAGE);
			return;
		case TRANSITIONS_TO_SINGLE_FINAL:
			final List<State> states = drawer.getSelected();
			final State finalState = automaton.getFinalStates().get(0);
			for (final State si : states) {
				transitionCreate(si, finalState);
			}
			break;
		case CONVERT_TRANSITIONS: {
			final List<State> s = automaton.getStates();
			for (final State si : s) {
				for (final State sj : s) {
					if (automaton.getTransitionsFromStateToState(si, sj).size() > 1) {
						transitionCollapse(si, sj);
					}
				}
			}
			break;
		}
		case CREATE_EMPTY_TRANSITIONS: {
			final List<State> s = automaton.getStates();
			for (final State si : s) {
				for (final State sj : s) {
					if (automaton.getTransitionsFromStateToState(si, sj).size() == 0) {
						transitionCreate(si, sj);
					}
				}
			}
			break;
		}
		case COLLAPSE_STATES:
			final List<State> s = automaton.getStates();
			for (final State si : s) {
				if (automaton.getFinalStates().get(0) == si || automaton.getInitialState() == si) {
					continue;
				}
				final List<Transition> t = FSAToRegularExpressionConverter.getTransitionsForRemoveState(si, automaton);
				FSAToRegularExpressionConverter.removeState(si, t, automaton);
			}
			remaining = 0;
			nextStep();
			break;
		case FINISHED:
			JOptionPane.showMessageDialog(frame, "You're done.  Go away.", "You're Done!", JOptionPane.ERROR_MESSAGE);
			return;
		default:
			JOptionPane.showMessageDialog(frame, "This shouldn't happen!  Notify Thomas.", "Uh Oh, I'm Stupid!",
					JOptionPane.ERROR_MESSAGE);
		}
		// nextStep();
	}

	/**
	 * Moves the converter controller to the next step. This will skip any
	 * unnecessary steps, and set the messages.
	 */
	protected void nextStep() {
		switch (currentStep) {
		case -1:
		case CREATE_SINGLE_FINAL:
			currentStep = CREATE_SINGLE_FINAL;
			mainStep.setText("Make Single Noninitial Final State");
			detailStep.setText("Create a new state to make a single final state.");
			if (automaton.getFinalStates().size() != 1
					|| automaton.getFinalStates().get(0) == automaton.getInitialState()) {
				return;
			}
			currentStep = TRANSITIONS_TO_SINGLE_FINAL;
		case TRANSITIONS_TO_SINGLE_FINAL:
			detailStep.setText(
					"Put " + Universe.curProfile.getEmptyString() + "-transitions from old final states to new.");
			// We know we're done when...
			if (drawer.numberSelected() != 0) {
				return;
			}
			currentStep = CONVERT_TRANSITIONS;
			remaining = collapsesNeeded();
		case CONVERT_TRANSITIONS:
			mainStep.setText("Reform Transitions");
			detailStep.setText("Use the collapse tool to turn multiple transitions to one." + " " + remaining
					+ " more collapses needed.");
			if (remaining != 0) {
				return;
			}
			currentStep = CREATE_EMPTY_TRANSITIONS;
			remaining = emptyNeeded();
		case CREATE_EMPTY_TRANSITIONS:
			detailStep.setText("Put empty transitions between states with no transitions." + " " + remaining
					+ " more empty transitions needed.");
			if (remaining != 0) {
				return;
			}
			remaining = automaton.getStates().size() - 2;
			currentStep = COLLAPSE_STATES;
		case COLLAPSE_STATES:
			mainStep.setText("Remove States");
			detailStep.setText("Use the collapse state tool to remove nonfinal, noninitial " + "states. " + remaining
					+ " more removals needed.");
			if (remaining != 0) {
				return;
			}
			if (transitionWindow != null) {
				transitionWindow.setVisible(false);
				transitionWindow.dispose();
			}
			drawer.clearSelected();
			drawer.clearSelectedTransitions();
			currentStep = FINISHED;
		case FINISHED:
			mainStep.setText("Generalized Transition Graph Finished!");
			computedRE = FSAToRegularExpressionConverter.getExpressionFromGTG(automaton);
			detailStep.setText(computedRE);
		}
	}

	/**
	 * This method should be called when the user undertakes an action that is
	 * inappropriate for the current step. This merely displays a small dialog
	 * to the user informing him of this fact, and takes no further action.
	 */
	protected void outOfOrder() {
		JOptionPane.showMessageDialog(frame, "That action is inappropriate for this step!", "Out of Order",
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * This takes a state, and prepares to remove it. Note that this does not
	 * actually remove the state, but notifies the user of what will appear.
	 *
	 * @param state
	 *            the state that was selected for removal
	 * @return <CODE>false</CODE> if this state cannot be removed because it is
	 *         initial or final or because this is the wrong time for this
	 *         operation, <CODE>true</CODE> otherwise
	 */
	public boolean stateCollapse(final State state) {
		if (currentStep != COLLAPSE_STATES) {
			outOfOrder();
			return false;
		}
		if (automaton.getInitialState() == state) {
			JOptionPane.showMessageDialog(frame, "The initial state cannot be removed!", "Initial State Selected",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (automaton.getFinalStates().get(0) == state) {
			JOptionPane.showMessageDialog(frame, "The final state cannot be removed!", "Final State Selected",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		collapseState = state;
		drawer.clearSelected();
		drawer.addSelected(collapseState);
		transitionWindow = new TransitionWindow(this);
		transitionWindow.setTransitions(FSAToRegularExpressionConverter.getTransitionsForRemoveState(state, automaton));
		transitionWindow.setVisible(true);
		// transitionWindow.show();
		return true;
	}

	/**
	 * Creates a state at the specified location. This method is called by an
	 * external state creation tool for the purposes of creating a single final
	 * state only. If this is such an event, the state will be created, made
	 * final, all other final states will be made nonfinal, and selected, and
	 * the machine will move to the next phase.
	 *
	 * @param point
	 *            the point that the state creation tool was clicked at
	 * @return the state that was created, or <CODE>null</CODE> if it is not the
	 *         time to create a state
	 */
	public State stateCreate(final Point point) {
		if (currentStep != CREATE_SINGLE_FINAL) {
			outOfOrder();
			return null;
		}
		final List<State> finals = automaton.getFinalStates();
		drawer.clearSelected();
		for (final State si : finals) {
			automaton.removeFinalState(si);
			drawer.addSelected(si);
		}
		final State newState = automaton.createState(point);
		automaton.addFinalState(newState);
		frame.repaint();
		nextStep();
		return newState;
	}

	/**
	 * If a transition is selected in the transition window, this method is told
	 * about it.
	 *
	 * @param transition
	 *            the transition that was selected, or <CODE>null</CODE> if less
	 *            or more than one transition is selected
	 */
	public void tableTransitionSelected(final Transition transition) {
		drawer.clearSelectedTransitions();
		if (transition == null || collapseState == null) {
			return;
		}
		final State from = transition.getFromState();
		final State to = transition.getToState();
		final Transition a = automaton.getTransitionsFromStateToState(from, collapseState).get(0);
		final Transition b = automaton.getTransitionsFromStateToState(from, to).get(0);
		final Transition c = automaton.getTransitionsFromStateToState(collapseState, collapseState).get(0);
		final Transition d = automaton.getTransitionsFromStateToState(collapseState, to).get(0);
		drawer.addSelected(a);
		drawer.addSelected(b);
		drawer.addSelected(c);
		drawer.addSelected(d);
		frame.repaint();
	}

	/**
	 * This takes all the transitions from one state to another, and combines
	 * them into a single transition.
	 *
	 * @param from
	 *            the from state
	 * @param to
	 *            the to state
	 * @return the newly created super transition that replaced all the
	 *         transitions that used to go from <CODE>from</CODE> to
	 *         </CODE>to</CODE>, or <CODE>null</CODE> if the transitions could
	 *         not be collapsed (either because there is already only one, or
	 *         there are none, or if this isn't the right time to collapse)
	 */
	public Transition transitionCollapse(final State from, final State to) {
		if (currentStep != CONVERT_TRANSITIONS) {
			outOfOrder();
			return null;
		}
		final List<Transition> ts = automaton.getTransitionsFromStateToState(from, to);
		if (ts.size() <= 1) {
			JOptionPane.showMessageDialog(frame, "Collapse requires 2 or more transitions!", "Too Few Transitions",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		final Transition t = FSAToRegularExpressionConverter.combineToSingleTransition(from, to, ts, automaton);
		remaining--;
		frame.repaint();
		nextStep();
		return t;
	}

	/**
	 * Creates a new transition. There are two times when this would be
	 * appropriate: first, when creating the labmda transitions from previously
	 * final states to the new final state, and two, when creating the empty set
	 * transitions between states that do not have transitions between
	 * themselves already. Otherwise, this action should not be undertaken.
	 * These transition creations do not require any user input since in either
	 * case, what must go in the label is clear.
	 *
	 * @param from
	 *            the from state
	 * @param to
	 *            the to state
	 * @return the newly created transition from <CODE>from</CODE> to
	 *         </CODE>to</CODE>, or <CODE>null</CODE> if a transition is
	 *         inappropriate for this circumstance
	 */
	public Transition transitionCreate(final State from, final State to) {
		if (currentStep == TRANSITIONS_TO_SINGLE_FINAL) {
			if (automaton.getFinalStates().get(0) != to) {
				JOptionPane.showMessageDialog(frame, "Transitions must go to the new final state!", "Bad Destination",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
			if (!drawer.isSelected(from)) {
				JOptionPane.showMessageDialog(frame, "Transitions must come from an old final state!", "Bad Source",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
			final Transition t = new FSATransition(from, to, "");
			drawer.removeSelected(from);
			automaton.addTransition(t);
			frame.repaint();
			if (drawer.numberSelected() == 0) {
				nextStep();
			}
			return t;
		}
		if (currentStep == CREATE_EMPTY_TRANSITIONS) {
			if (automaton.getTransitionsFromStateToState(from, to).size() != 0) {
				JOptionPane.showMessageDialog(frame, "Transitions must go between" + "states with no transitions!",
						"Transition Already Exists", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			final Transition t = FSAToRegularExpressionConverter.addTransitionOnEmptySet(from, to, automaton);
			remaining--;
			nextStep();
			frame.repaint();
			return t;
		}
		outOfOrder();
		return null;
	}
}
