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

package edu.duke.cs.jflap.gui.grammar.transform;

import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.vdg.VariableDependencyGraph;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.ProductionChecker;
import edu.duke.cs.jflap.grammar.UnitProductionRemover;
import edu.duke.cs.jflap.gui.grammar.GrammarTableModel;

/**
 * This is the controller for the unit panel. Bleh.
 *
 * @author Thomas Finley
 */
public class UnitController {
	/** The steps available. */
	static final int VARAIBLE_GRAPH = 1, PRODUCTION_MODIFY = 2, FINISHED = 3;

	/** The unit pane. */
	UnitPane pane;

	/** The grammar being converted. */
	Grammar grammar;

	/** The variable dependency graph. */
	VariableDependencyGraph vdg;

	/** The set of transitions that should be added to the VDG. */
	Set<Transition> vdgTransitions = new HashSet<>();

	/**
	 * The set of productions that should comprise the grammar, those that
	 * currently do, and those that should be removed.
	 */
	Set<Production> desiredProductions = new HashSet<>(), currentProductions = new HashSet<>(),
			unitProductions = new HashSet<>();

	/** The current step. */
	int step = 0;

	/**
	 * This instantiates a new unit controller.
	 *
	 * @param pane
	 *            the unit panel
	 * @param grammar
	 *            the grammar to produce
	 */
	public UnitController(final UnitPane pane, final Grammar grammar) {
		this.pane = pane;
		this.grammar = grammar;
		nextStep();
	}

	/**
	 * Does all steps.
	 */
	public void doAll() {
		while (step != FINISHED) {
			doStep();
		}
	}

	/**
	 * Does the expansion of the production for those states selected.
	 */
	void doSelected() {
		pane.editingActive = false;

		// Which of the selected rows are selected unit productions?
		final int[] selectedRows = pane.editingGrammarView.getSelectedRows();
		final GrammarTableModel model = pane.editingGrammarModel;
		final Set<Production> selectedUnitProductions = new HashSet<>();
		for (int i = selectedRows.length - 1; i >= 0; i--) {
			final Production p = model.getProduction(selectedRows[i]);
			if (!ProductionChecker.isUnitProduction(p)) {
				continue;
			}
			selectedUnitProductions.add(p);
			pane.editingGrammarModel.deleteRow(selectedRows[i]);
			unitProductions.remove(p);
			currentProductions.remove(p);
		}

		// Determine what productions need to be added as a result of
		// each selected unit production.
		final Set<Production> toAdd = new HashSet<>();
		for (final Production unit : selectedUnitProductions) {
			for (final Production p : desiredProductions) {
				if (p.getLHS().equals(unit.getRHS())) {
					toAdd.add(new Production(unit.getLHS(), p.getRHS()));
				}
			}
		}

		// Add those productions!
		for (final Production p : toAdd) {
			if (!currentProductions.add(p)) {
				continue;
			}
			pane.editingGrammarModel.addProduction(p);
		}
		pane.editingActive = true;

		if (currentProductions.equals(desiredProductions)) {
			nextStep();
		} else {
			updateDisplay();
		}
	}

	/**
	 * Does the current step.
	 */
	public void doStep() {
		switch (step) {
		case VARAIBLE_GRAPH:
			final Set<Transition> t = vdgTransitions;
			t.forEach(trans -> vdg.addTransition(trans));
			break;
		case PRODUCTION_MODIFY:
			for (int i = pane.editingGrammarModel.getRowCount() - 2; i >= 0; i--) {
				final Production p = pane.editingGrammarModel.getProduction(i);
				if (unitProductions.contains(p)) {
					pane.editingGrammarModel.deleteRow(i);
					unitProductions.remove(p);
				}
			}
			pane.editingActive = false;
			final Set<Production> p = desiredProductions;
			for (final Production pi : p) {
				if (!currentProductions.add(pi)) {
					continue;
				}
				pane.editingGrammarModel.addProduction(pi);
			}
			nextStep();
			break;
		case FINISHED:
			break;
		}
	}

	public Grammar getGrammar() {
		return pane.getGrammar();
	}

	// Variables related to the VDG.

	/**
	 * This is called to move the lambda controller to the next step.
	 */
	private void nextStep() {
		if (step != FINISHED) {
			step++;
		}
		switch (step) {
		case VARAIBLE_GRAPH: {
			pane.mainLabel.setText("Complete unit production visualization.");
			pane.detailLabel.setText("For every unit production, connect start and end.");
			// Make the VDG.
			vdg = new VariableDependencyGraph();
			UnitProductionRemover.initializeDependencyGraph(vdg, grammar);
			// Cache the transitions we have to add.
			final List<Production> p = grammar.getProductions();
			for (int i = 0; i < p.size(); i++) {
				if (ProductionChecker.isUnitProduction(p.get(i))) {
					vdgTransitions.add(UnitProductionRemover.getTransitionForUnitProduction(p.get(i), vdg));
				}
			}
			// Set up the listener so we know when new actions get
			// added to the VDG.
			vdg.addTransitionListener(e -> {
				if (!e.isAdd()) {
					return;
				}
				if (vdgTransitions.contains(e.getTransition())) {
					vdgTransitions.remove(e.getTransition());
					updateDisplay();
					return;
				}
				JOptionPane.showMessageDialog(pane, "Transition is not part of VDG.", "Bad Transition",
						JOptionPane.ERROR_MESSAGE);
				vdg.removeTransition(e.getTransition());
			});
			// Set the actions.
			pane.deleteAction.setEnabled(false);
			pane.completeSelectedAction.setEnabled(false);
			pane.doStepAction.setEnabled(true);
			pane.doAllAction.setEnabled(true);
			pane.proceedAction.setEnabled(false);
			pane.exportAction.setEnabled(false);
			updateDisplay();
			break;
		}
		case PRODUCTION_MODIFY: {
			pane.updateDeleteEnabledness();
			pane.updateCompleteSelectedEnabledness();
			pane.mainLabel.setText("Modify the grammar to remove unit productions.");
			List<Production> p = grammar.getProductions();
			for (final Production pi : p) {
				pane.editingGrammarModel.addProduction(pi);
				currentProductions.add(pi);
				if (ProductionChecker.isUnitProduction(pi)) {
					unitProductions.add(pi);
					continue;
				}
			}
			// Get the desired productions.
			final Grammar desiredGrammar = UnitProductionRemover.getUnitProductionlessGrammar(grammar, vdg);
			p = desiredGrammar.getProductions();
			p.forEach(prod -> desiredProductions.add(prod));
			updateDisplay();
			pane.editingActive = true;
			break;
		}
		case FINISHED: {
			pane.editingActive = false;
			pane.deleteAction.setEnabled(false);
			pane.completeSelectedAction.setEnabled(false);
			pane.mainLabel.setText("Unit removal complete.");
			pane.detailLabel.setText("\"Proceed\" or \"Export\" available.");

			pane.doStepAction.setEnabled(false);
			pane.doAllAction.setEnabled(false);
			pane.proceedAction.setEnabled(true);
			pane.exportAction.setEnabled(true);
			break;
		}
		}
	}

	/**
	 * When a production is added manually by the user, this is told about it.
	 *
	 * @param production
	 *            the production added
	 * @param row
	 *            the row that was added
	 * @return if this production should be accepted
	 */
	boolean productionAdded(final Production production, final int row) {
		if (currentProductions.contains(production)) {
			// We already have it.
			JOptionPane.showMessageDialog(pane, "This production is already in the grammar.", "Production Already Here",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (!desiredProductions.contains(production)) {
			// We don't have it, and don't want it!
			JOptionPane.showMessageDialog(pane, "This production is not part of the reformed grammar.",
					"Production Not Desired", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// We want it. We want it so bad.
		currentProductions.add(production);
		updateDisplay();
		return true;
	}

	/**
	 * When a production is chosen to be removed, this is told about it. This
	 * happens before the deletion occurs.
	 *
	 * @param production
	 *            the production chosen to be removed
	 * @param row
	 *            the row for this production
	 * @return if this production should be deleted
	 */
	boolean productionDeleted(final Production production, final int row) {
		if (!ProductionChecker.isUnitProduction(production)) {
			return false;
		}
		unitProductions.remove(production);
		currentProductions.remove(production);
		return true;
	}

	/**
	 * When a state is clicked during the step of production modification, this
	 * method is told about it.
	 *
	 * @param state
	 *            the state that was clicked
	 * @param event
	 *            the mouse event associated with the click
	 */
	void stateClicked(final State state, final MouseEvent event) {
		if (event.isShiftDown()) {
			if (state == null) {
				return;
			}
			if (pane.vdgDrawer.isSelected(state)) {
				pane.vdgDrawer.removeSelected(state);
			} else {
				pane.vdgDrawer.addSelected(state);
			}
		} else {
			if (state == null) {
				pane.vdgDrawer.clearSelected();
			} else {
				if (!pane.vdgDrawer.isSelected(state)) {
					pane.vdgDrawer.clearSelected();
					pane.vdgDrawer.addSelected(state);
				}
			}
		}
		pane.vdgEditor.repaint();
		pane.completeSelectedAction.setEnabled(pane.vdgDrawer.numberSelected() > 0);
	}

	/**
	 * Updates the detail display to show how many more removes, and additions
	 * are needed in the grammar modification step.
	 */
	void updateDisplay() {
		switch (step) {
		case VARAIBLE_GRAPH: {
			final int toAdd = vdgTransitions.size();
			pane.detailLabel.setText(toAdd + " more transition(s) needed.");
			if (toAdd == 0) {
				nextStep();
			}
			break;
		}
		case PRODUCTION_MODIFY: {
			final int toRemove = unitProductions.size();
			final int toAdd = desiredProductions.size() - currentProductions.size() + toRemove;
			pane.detailLabel.setText(toRemove + " more remove" + (toRemove == 1 ? "" : "s") + ", and " + toAdd
					+ " more addition" + (toAdd == 1 ? "" : "s") + " needed.");
			if (toAdd == 0 && toRemove == 0) {
				nextStep();
			}
			break;
		}
		}
	}
}
