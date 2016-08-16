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

package edu.duke.cs.jflap.gui.grammar.parse;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.tree.TreeNode;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.parse.BruteParser;
import edu.duke.cs.jflap.grammar.parse.BruteParserEvent;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.sim.multiple.InputTableModel;

/**
 * This is a brute force parse pane.
 *
 * @author Thomas Finley
 */
public class TMBruteParsePane extends BruteParsePane {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final Grammar myTrimmedGrammar;

	/**
	 * Instantiates a new brute force parse pane.
	 *
	 * @param environment
	 *            the grammar environment
	 * @param grammar
	 *            the augmented grammar
	 */
	public TMBruteParsePane(final GrammarEnvironment environment, final Grammar orig, final Grammar trim,
			final HashMap<String, String> map, final InputTableModel model) {
		super(environment, orig);
		treePanel = new UnrestrictedTreePanel(this, map);
		initView();
		myTrimmedGrammar = trim;
		myTrimmedGrammar.setStartVariable("S");
		myModel = model;
	}

	/**
	 * Returns the choices for the view.
	 *
	 * @return an array of strings for the choice of view
	 */
	@Override
	protected String[] getViewChoices() {
		return new String[] { "Noninverted Tree", "Derivation Table" };
	}

	/**
	 * Returns the interface that holds the input area.
	 */
	@Override
	protected JPanel initInputPanel() {
		final JPanel bigger = new JPanel(new BorderLayout());
		final JPanel panel = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gridbag);

		c.fill = GridBagConstraints.BOTH;

		c.weightx = 0.0;
		panel.add(new JLabel("Input"), c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(inputField, c);
		inputField.addActionListener(startAction);
		// c.weightx = 0.0;
		// JButton startButton = new JButton(startAction);
		// startButton.addActionListener(listener);
		// panel.add(startButton, c);

		panel.add(progress, c);

		bigger.add(panel, BorderLayout.CENTER);
		bigger.add(initInputToolbar(), BorderLayout.NORTH);

		return bigger;
	}

	/**
	 * Inits a parse table.
	 *
	 * @return a table to hold the parse table
	 */
	@Override
	protected JTable initParseTable() {
		return null;
	}

	/**
	 * Inits a new tree panel. This overriding adds a selection node drawer so
	 * certain nodes can be highlighted.
	 *
	 * @return a new display for the parse tree
	 */
	@Override
	protected JComponent initTreePanel() {
		return treePanel;
	}

	/**
	 * This method is called when there is new input to parse.
	 *
	 * @param string
	 *            a new input string
	 */
	@Override
	public void input(final String string) {
		if (parser != null) {
			parser.pause();
		}
		parseInput(string, null);
	}

	/**
	 * Parse one input string
	 */
	@Override
	public void parseInput(final String string, final BruteParser newParser) {
		if (string.equals("")) {
			return;
		}
		if (newParser == null) {
			try {
				parser = BruteParser.get(myTrimmedGrammar, string);
			} catch (final IllegalArgumentException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Bad Input", JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {
			parser = newParser;
		}
		final Timer timer = new Timer(10, e -> {
			if (parser == null) {
				return;
			}
			final String nodeCount = "Nodes generated: " + parser.getTotalNodeCount() + "("
					+ parser.getConsiderationNodeCount() + ")";
			progress.setText("Parser running.  " + nodeCount);
		});
		parser.addBruteParserListener(e -> {
			synchronized (e.getParser()) {
				final String nodeCount = e.getParser().getTotalNodeCount() + " nodes generated.";
				String status = null;
				switch (e.getType()) {
				case BruteParserEvent.START:
					pauseResumeAction.setEnabled(true);
					pauseResumeAction.putValue(Action.NAME, "Pause");
					timer.start();
					status = "Parser started.";
					statusDisplay.setText(status);
					break;
				case BruteParserEvent.REJECT:
					pauseResumeAction.setEnabled(false);
					timer.stop();
					status = "String rejected.";
					if (myModel != null) {
						final String[][] inputs = myModel.getInputs();
						int size = 1;
						if (environment.myObjects != null) {
							size = environment.myObjects.size();
						}
						final int uniqueInputs = inputs.length / size;
						myModel.setResult(row, "Reject", null, environment.myTransducerStrings,
								(row % uniqueInputs) * 2);
						parseMultiple();
					}
					break;
				case BruteParserEvent.PAUSE:
					timer.stop();
					pauseResumeAction.putValue(Action.NAME, "Resume");
					pauseResumeAction.setEnabled(true);
					status = "Parser paused.";
					statusDisplay.setText(status);
					break;
				case BruteParserEvent.ACCEPT:
					pauseResumeAction.setEnabled(false);
					stepAction.setEnabled(true);
					timer.stop();
					status = "String accepted!";
					if (myModel != null) {
						myModel.setResult(row, "Accept", null, environment.myTransducerStrings, row);
						parseMultiple();
					}
					break;
				}
				progress.setText(status + "  " + nodeCount);
				if (parser.isFinished()) {
					// parser = null;

					if (!e.isAccept()) {
						// Rejected!
						treePanel.setAnswer(null);
						treePanel.repaint();
						stepAction.setEnabled(false);
						statusDisplay.setText("Try another string.");
						return;
					}
					TreeNode node = e.getParser().getAnswer();
					do {
						node = node.getParent();
					} while (node != null);
					statusDisplay.setText("Press step to show derivations.");
					treePanel.setAnswer(e.getParser().getAnswer());
					treePanel.repaint();
				}
			}
		});
		parser.start();
	}

	/**
	 * Parse multiple strings
	 */
	@Override
	public void parseMultiple() {
		final String[][] inputs = myModel.getInputs();
		int size = 1;
		if (environment.myObjects != null) {
			size = environment.myObjects.size();
		}
		final int uniqueInputs = inputs.length / size;
		Grammar currentGram = grammar;
		if (environment.myObjects != null) {
			currentGram = (Grammar) environment.myObjects.get(0);
		}
		if (row < (inputs.length - 1)) {
			row++;
			if (row % uniqueInputs == 0 && environment.myObjects != null) {
				currentGram = (Grammar) environment.myObjects.get(row / uniqueInputs);
				grammar = currentGram;
			}

			parser = BruteParser.get(myTrimmedGrammar, inputs[row][0]);
			parseInput(inputs[row][0], parser);
		}
	}

	/**
	 * This method is called when the step button is pressed.
	 */
	@Override
	public boolean step() {
		// controller.step();
		boolean worked = false;
		if (treePanel.next()) {
			stepAction.setEnabled(false);
			worked = true;
		}

		treePanel.repaint();
		return worked;
	}
}
