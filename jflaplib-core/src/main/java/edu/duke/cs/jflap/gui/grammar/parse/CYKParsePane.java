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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.parse.CYKParser;
import edu.duke.cs.jflap.grammar.parse.CYKTracer;
import edu.duke.cs.jflap.grammar.parse.ParseNode;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.TableTextSizeSlider;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.grammar.GrammarTable;
import edu.duke.cs.jflap.gui.sim.multiple.InputTableModel;

/**
 * CYK Parse Pane
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class CYKParsePane extends BruteParsePane {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The parser that is going to be used **/
	private final CYKParser myParser;

	/** The action for the stepping control. */
	private Action myStepAction;

	/** Target string that user is trying to derive **/
	private String myTarget;

	/** CNF Grammar that is transformed from the original grammar */
	private final Grammar myCNFGrammar;

	/**
	 * Boolean variable telling whether grammar is accepted or not (If accepted,
	 * we can get trace)
	 */
	private boolean myTraceAvailable;

	private ParseNode myCurrentAnswerNode;

	private List<Production> myAnswers;

	private LinkedList<ParseNode> myQueue;

	private int myIndex;

	/**
	 * Constructor for CYK Control Parse Pane calls the super class's
	 * constructor
	 *
	 * @param environment
	 * @param grammar
	 */
	public CYKParsePane(final GrammarEnvironment environment, final Grammar original, final Grammar cnf) {
		super(environment, original, null);
		myCNFGrammar = cnf;
		myParser = new CYKParser(myCNFGrammar);
	}

	/**
	 * Constructor for CYK Control Parse Pane calls the super class's
	 * constructor
	 *
	 * @param environment
	 * @param grammar
	 */
	public CYKParsePane(final GrammarEnvironment environment, final Grammar original, final Grammar cnf,
			final InputTableModel model) {
		super(environment, original, model);
		myCNFGrammar = cnf;
		myParser = new CYKParser(myCNFGrammar);
		myModel = model;
	}

	// adding Trace button to the GUI
	@Override
	protected JToolBar initInputToolbar() {
		final JToolBar toolbar = new JToolBar();
		toolbar.add(startAction);
		myStepAction = new AbstractAction("Step") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				stepForward();
			}
		};
		myStepAction.setEnabled(false);
		toolbar.add(myStepAction);

		// Set up the view customizer controls.
		toolbar.addSeparator();

		final JComboBox<?> box = new JComboBox<Object>(getViewChoices());
		box.setSelectedIndex(0);
		final ActionListener listener = e -> changeView((String) box.getSelectedItem());
		box.addActionListener(listener);
		toolbar.add(box);
		return toolbar;
	}

	/**
	 * Inits a new tree panel.
	 *
	 * @return a new display for the parse tree
	 */
	@Override
	protected JComponent initTreePanel() {
		treePanel = new SelectableUnrestrictedTreePanel(this);
		return treePanel;
	}

	/**
	 * Initialize the view
	 */
	@Override
	protected void initView() {
		initTreePanel();

		// Sets up the displays.
		final JComponent pt = initParseTable();
		final JScrollPane parseTable = pt == null ? null : new JScrollPane(pt);
		final GrammarTable g = initGrammarTable(grammar);
		final JScrollPane grammarTable = new JScrollPane(g);

		treeDerivationPane.add(initTreePanel(), "0");
		derivationPane = new JScrollPane(initDerivationTable());
		treeDerivationPane.add(derivationPane, "1");
		bottomSplit = SplitPaneFactory.createSplit(environment, true, 0.3, grammarTable, treeDerivationPane);
		topSplit = SplitPaneFactory.createSplit(environment, true, 0.4, parseTable, initInputPanel());
		mainSplit = SplitPaneFactory.createSplit(environment, false, 0.3, topSplit, bottomSplit);
		add(mainSplit, BorderLayout.CENTER);
		add(statusDisplay, BorderLayout.SOUTH);
		add(new TableTextSizeSlider(g), BorderLayout.NORTH);
	}

	/**
	 * This method is called when there is new input to parse.
	 *
	 * @param string
	 *            a new input string
	 */
	@Override
	public void input(final String string) {
		statusDisplay.setText("");
		myTarget = string;
		treePanel.setAnswer(null);
		treePanel.repaint();
		derivationModel.setRowCount(0);
		myStepAction.setEnabled(false);
		if (myParser.solve(string)) {
			progress.setText("String is Accepted!");
			//// System.out.println(myParser.getTrace());
			myTraceAvailable = true;
			myStepAction.setEnabled(true);
			traceBack();
		} else {
			progress.setText("String is Rejected!");
		}
	}

	/**
	 * Method for Multiple Parsing
	 */
	@Override
	public void parseMultiple() {
		final String[][] inputs = myModel.getInputs();
		row = -1;
		while (row < (inputs.length - 1)) {
			// System.out.println("ROW = "+row);
			row++;
			// System.out.println("String is = "+inputs[row][0]);
			if (myParser.solve(inputs[row][0])) {
				myModel.setResult(row, "Accept", null, environment.myTransducerStrings, row);
			} else {
				myModel.setResult(row, "Reject", null, environment.myTransducerStrings, row);
			}
		}
	}

	private void stepForward() {
		// TODO Auto-generated method stub
		treePanel.setAnswer(myCurrentAnswerNode);
		treePanel.repaint();
		if (myCurrentAnswerNode.getDerivation().equals(myTarget)) {
			myStepAction.setEnabled(false);
			return;
		}

		final ParseNode node = myQueue.removeFirst();

		String deriv = node.getDerivation();
		/*
		 * System.out.println("DERIV => "+deriv);
		 * System.out.println("PROD => "+myAnswers[myIndex]);
		 * System.out.println("LHS => "+myAnswers[myIndex].getLHS());
		 */
		final int index = deriv.indexOf(myAnswers.get(myIndex).getLHS());
		if (index == -1) {
			myStepAction.setEnabled(false);
			return;
		}
		deriv = deriv.substring(0, index) + myAnswers.get(myIndex).getRHS() + deriv.substring(index + 1);
		ParseNode pNode = new ParseNode(deriv, Lists.newArrayList(myAnswers.get(myIndex)), Lists.newArrayList(index));
		pNode = new ParseNode(pNode);
		node.add(pNode);
		myQueue.add(pNode);
		myCurrentAnswerNode = pNode;
		myIndex++;
	}

	/**
	 * Method for getting the original Productions back NOTE: STILL UNDER
	 * CONSTRUCTION BETA VERSION ONLY!
	 */
	public void traceBack() {
		if (!myTraceAvailable) {
			return;
		}
		final CYKTracer cykTracer = new CYKTracer(grammar, myParser.getTrace());
		cykTracer.traceBack();
		myAnswers = cykTracer.getAnswer();

		/*
		 * System.out.println("Answer is "); for (int i=0; i<myAnswers.length;
		 * i++)
		 * System.out.println(myAnswers[i].getLHS()+" -> "+myAnswers[i].getRHS()
		 * );
		 */
		if (!myAnswers.get(0).getLHS().equals(grammar.getStartVariable())) {

			for (int i = 1; i < myAnswers.size(); i++) {
				if (myAnswers.get(i).getLHS().equals(grammar.getStartVariable())) {
					final Production p = myAnswers.get(0);
					myAnswers.set(i, myAnswers.get(i));
					myAnswers.set(i, p);
					break;
				}
			}
		}

		/*
		 * System.out.println("After is "); for (int i=0; i<myAnswers.length;
		 * i++)
		 * System.out.println(myAnswers[i].getLHS()+" -> "+myAnswers[i].getRHS()
		 * );
		 */
		myCurrentAnswerNode = new ParseNode(grammar.getStartVariable(), new ArrayList<>(), new ArrayList<>());
		myQueue = new LinkedList<>();
		myQueue.add(myCurrentAnswerNode);
		myIndex = 0;
		stepForward();
	}
}
