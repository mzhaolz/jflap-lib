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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.LambdaProductionRemover;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.gui.grammar.GrammarTableModel;

/**
 * This is the controller for the lambda panel.
 *
 * @author Thomas Finley
 */
public class LambdaController {
	/** The steps available. */
	static final int VARAIBLE_SELECT = 1, PRODUCTION_MODIFY = 2, FINISHED = 3;

	/** The lambda pane. */
	LambdaPane pane;

	/** The grammar being converted. */
	Grammar grammar;

	/** The set of variables that derive lambda, and those discovered. */
	Set<String> lambdaVariables, derivedLambdaVariables;

	/**
	 * The set of productions that should comprise the grammar, those that
	 * currently do, and those that should be removed.
	 */
	Set<Production> desiredProductions = new HashSet<>(), currentProductions = new HashSet<>(),
			lambdaProductions = new HashSet<>();

	/**
	 * The mapping of productions to those elements they are supposed to add.
	 */
	Map<Production, List<Production>> productionsToExpansion = new HashMap<>();

	/** The current step. */
	int step = 0;

	/**
	 * This instantiates a new lambda controller.
	 *
	 * @param pane
	 *            the lambda panel
	 * @param grammar
	 *            the grammar to produce
	 */
	public LambdaController(final LambdaPane pane, final Grammar grammar) {
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
	 * Does the current step.
	 */
	public void doStep() {
		switch (step) {
		case VARAIBLE_SELECT:
			derivedLambdaVariables = lambdaVariables;
			pane.lambdaDerivingLabel.setText("Set that derives lambda: " + derivedLambdaVariables);
			nextStep();
			break;
		case PRODUCTION_MODIFY:
			for (int i = pane.editingGrammarModel.getRowCount() - 2; i >= 0; i--) {
				final Production p = pane.editingGrammarModel.getProduction(i);
				if (lambdaProductions.contains(p)) {
					pane.editingGrammarModel.deleteRow(i);
					lambdaProductions.remove(p);
				}
			}
			final GrammarTableModel m = pane.grammarTable.getGrammarModel();
			for (int i = 0; i < m.getRowCount() - 1; i++) {
				expandRowProduction(i);
			}
			nextStep();
			break;
		case FINISHED:
			break;
		}
	}

	/**
	 * Does the expansion of the production in the given row of the left grammar
	 * panel.
	 *
	 * @param row
	 *            the row of the production to expand
	 */
	public void expandRowProduction(final int row) {
		final Production p = pane.grammarTable.getGrammarModel().getProduction(row);
		final List<Production> ps = productionsToExpansion.get(p);
		if (ps == null) {
			return;
		}
		pane.editingActive = false;
		for (final Production psi : ps) {
			if (!currentProductions.contains(psi) && desiredProductions.contains(psi)) {
				pane.editingGrammarModel.addProduction(psi);
				currentProductions.add(psi);
			}
			productionsToExpansion.remove(p); // May as well...
		}
		pane.editingActive = true;
		updateDisplay();
	}

	public Map<Production, List<Production>> getExpansionMap() {
		return productionsToExpansion;
	}

	public Grammar getGrammar() {
		return pane.getGrammar();
	}

	public Set<Production> getLambdaSet() {
		return lambdaProductions;
	}

	/**
	 * This is called to move the lambda controller to the next step.
	 */
	private void nextStep() {
		if (step != FINISHED) {
			step++;
		}
		switch (step) {
		case VARAIBLE_SELECT:
			pane.mainLabel.setText("Select variables that derive lambda.");
			pane.detailLabel.setText("Click productions; the LHS variable will be added.");
			lambdaVariables = LambdaProductionRemover.getCompleteLambdaSet(grammar);
			derivedLambdaVariables = new TreeSet<>();
			pane.deleteAction.setEnabled(false);
			pane.completeSelectedAction.setEnabled(false);

			pane.doStepAction.setEnabled(true);
			pane.doAllAction.setEnabled(true);
			pane.proceedAction.setEnabled(false);
			pane.exportAction.setEnabled(false);
			break;
		case PRODUCTION_MODIFY:
			pane.updateDeleteEnabledness();
			pane.updateCompleteSelectedEnabledness();
			pane.mainLabel.setText("Modify the grammar to remove lambdas.");
			final List<Production> p = grammar.getProductions();
			for (final Production pi : p) {
				pane.editingGrammarModel.addProduction(pi);
				currentProductions.add(pi);
				if (pi.getRHS().length() == 0) {
					lambdaProductions.add(pi);
					continue;
				}
				final List<Production> p2 = LambdaProductionRemover.getProductionsToAddForProduction(pi,
						lambdaVariables);
				desiredProductions.add(pi);
				productionsToExpansion.put(pi, p2);
				p2.forEach(prod -> desiredProductions.add(prod));
			}
			pane.editingActive = true;
			updateDisplay();
			break;
		case FINISHED:
			pane.deleteAction.setEnabled(false);
			pane.completeSelectedAction.setEnabled(false);
			pane.mainLabel.setText("Lambda removal complete.");
			pane.detailLabel.setText("\"Proceed\" or \"Export\" available.");

			pane.doStepAction.setEnabled(false);
			pane.doAllAction.setEnabled(false);
			pane.proceedAction.setEnabled(true);
			pane.exportAction.setEnabled(true);
			break;
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
	 * When a production is clicked in the grammar table, this method is told
	 * about it.
	 *
	 * @param production
	 *            the production clicked in the table
	 * @param event
	 *            the mouse event that was the clicked
	 */
	void productionClicked(final Production production, final MouseEvent event) {
		switch (step) {
		case VARAIBLE_SELECT:
			final String var = production.getLHS();
			if (derivedLambdaVariables.contains(var)) {
				// Already here!
				pane.detailLabel.setText(var + " already selected!  "
						+ (lambdaVariables.size() - derivedLambdaVariables.size()) + " more variable(s) needed.");
				return;
			}
			if (lambdaVariables.contains(var)) {
				// Not here, but should be added!
				derivedLambdaVariables.add(var);
				pane.detailLabel.setText(var + " added!  " + (lambdaVariables.size() - derivedLambdaVariables.size())
						+ " more variable(s) needed.");
				pane.lambdaDerivingLabel.setText("Set that derives lambda: " + derivedLambdaVariables);
				if (derivedLambdaVariables.size() == lambdaVariables.size()) {
					nextStep();
				}
				return;
			}
			pane.detailLabel.setText(var + " does not derive lambda!  "
					+ (lambdaVariables.size() - derivedLambdaVariables.size()) + " more variable(s) needed.");
			break;
		case PRODUCTION_MODIFY:
			break;
		default:
			break;
		}
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
		if (!lambdaProductions.contains(production)) {
			return false;
		}
		lambdaProductions.remove(production);
		currentProductions.remove(production);
		return true;
	}

	/**
	 * Updates the detail display to show how many more removes, and additions
	 * are needed in the grammar modification step.
	 */
	void updateDisplay() {
		final int toRemove = lambdaProductions.size();
		final int toAdd = desiredProductions.size() - currentProductions.size() + lambdaProductions.size();
		pane.detailLabel.setText(toRemove + " more remove(s), and " + toAdd + " more addition(s) needed.");
		if (toAdd == 0 && toRemove == 0) {
			nextStep();
		}
	}
}
