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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.parse.ParseNode;
import edu.duke.cs.jflap.grammar.parse.UserParser;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.TableTextSizeSlider;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.grammar.GrammarTable;

/**
 * This class created GUI pane for UserControl Parsing. This pane is similar to
 * BruteParsePane, but it has more additional features
 *
 * @author Kyung Min (Jason) Lee
 */
public class UserControlParsePane extends BruteParsePane {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The parser that is going to be used **/
	private UserParser myParser;

	/** Index of the selected production rule **/
	private int mySelectedProductionIndex = -1;

	/** The action for the stepping control. */
	private Action myPreviousAction;

	/** Variable to store how many times Step button is clicked **/
	private int myStepCount = 0;

	/** Variable to store how many times Previous button is clicked **/
	private int myPreviousCount = 0;

	/** DefaultListModel to show current string at the bottom of the pane **/
	private DefaultListModel<String> myJListModel;

	/**
	 * JList to show the current string and allow user to click on the variables
	 **/
	private JList<String> myStringJList;

	/** Target string that user is trying to derive **/
	private String myTarget;

	/**
	 * Constructor for User Control Parse Pane calls the super class's
	 * constructor
	 *
	 * @param environment
	 * @param grammar
	 */
	public UserControlParsePane(final GrammarEnvironment environment, final Grammar grammar) {
		super(environment, grammar, null);
		intializeGrammarTableSetting();
	}

	/**
	 * Add current string to the JList to show to the user
	 *
	 * @param answer
	 *            the current String that is going to be displayed at the bottom
	 *            of the pane
	 */
	private void addAnswerToList(final String answer) {
		myJListModel.removeAllElements();
		for (int i = 0; i < answer.length(); i++) {
			myJListModel.addElement(answer.substring(i, i + 1));
		}
	}

	/**
	 * Add multiple selection to be possible without clicking Ctrl or Shift
	 * button
	 *
	 */
	private void addMultipleSelectionToJList() {
		myStringJList.setSelectionModel(new DefaultListSelectionModel() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void setSelectionInterval(final int index0, final int index1) {
				if (isSelectedIndex(index0)) {
					super.removeSelectionInterval(index0, index1);
				} else {
					super.addSelectionInterval(index0, index1);
				}
			}
		});
	}

	/**
	 * Returns the tool bar for the main user input panel.
	 *
	 * @return the tool bar for the main user input panel
	 */
	@Override
	protected JToolBar initInputToolbar() {
		final JToolBar toolbar = new JToolBar();
		toolbar.add(startAction);
		myPreviousAction = new AbstractAction("Previous") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				previous();
			}
		};
		myPreviousAction.setEnabled(false);
		toolbar.add(myPreviousAction);
		stepAction.setEnabled(false);
		toolbar.add(stepAction);

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

		myJListModel = new DefaultListModel<>();
		myStringJList = new JList<>(myJListModel);
		myStringJList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

		addMultipleSelectionToJList();
		myStringJList.setVisibleRowCount(1);

		final JScrollPane scroll = new JScrollPane(myStringJList);

		treeDerivationPane.add(initTreePanel(), "0");
		derivationPane = new JScrollPane(initDerivationTable());
		treeDerivationPane.add(derivationPane, "1");
		bottomSplit = SplitPaneFactory.createSplit(environment, true, 0.3, grammarTable, treeDerivationPane);
		topSplit = SplitPaneFactory.createSplit(environment, true, 0.4, parseTable, initInputPanel());
		bottomSplit = SplitPaneFactory.createSplit(environment, false, 0.52, bottomSplit, scroll);
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
		myTarget = string;
		startParseInput(string, null);
	}

	/**
	 * Initialize the grammar table setting, and add Listeners so that we can
	 * track what user has clicked.
	 *
	 */
	private void intializeGrammarTableSetting() {
		grammarTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		grammarTable.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(final KeyEvent e) {
			}

			// when user realease a key that means user might or might not
			// selected an item from the list
			@Override
			public void keyReleased(final KeyEvent e) {
				mySelectedProductionIndex = grammarTable.getSelectedRow();
				if (mySelectedProductionIndex > -1 && mySelectedProductionIndex < grammarTable.getRowCount() - 1
						&& myTarget != null) {
					stepAction.setEnabled(true);
				} else {
					stepAction.setEnabled(false);
				}
			}

			@Override
			public void keyTyped(final KeyEvent e) {
			}
		});

		grammarTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(final MouseEvent e) {
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
			}

			@Override
			public void mouseExited(final MouseEvent e) {
			}

			@Override
			public void mousePressed(final MouseEvent e) {
			}

			// selection will only occur when user releases the button.
			@Override
			public void mouseReleased(final MouseEvent e) {
				mySelectedProductionIndex = grammarTable.getSelectedRow();
				if (mySelectedProductionIndex > -1 && mySelectedProductionIndex < grammarTable.getRowCount() - 1
						&& myTarget != null) {
					stepAction.setEnabled(true);
				} else {
					stepAction.setEnabled(false);
				}
			}
		});
	}

	/**
	 * Paints the result in the tree panel. By doing so, the derivation table is
	 * updated automatically
	 *
	 */
	private void paintTree() {
		addAnswerToList(myParser.getAnswer().getDerivation());
		final ParseNode answer = myParser.getAnswer();

		treePanel.setAnswer(answer);
		treePanel.repaint();
		if (answer.getProductions().size() > 0) {
			statusDisplay.setText("Derived current Strings using " + answer.getProductions().get(0) + " production");
		} else {
			statusDisplay.setText("");
		}
		if (myParser.isStringTerminal(answer.getDerivation())) {
			progress.setText("No Additional Production is Possible");
			stepAction.setEnabled(false);
		}
	}

	/**
	 * This method allow the pane to display the previous step's String and Tree
	 * panel/Derivation table
	 *
	 */
	protected void previous() {
		final ParseNode previous = myParser.getPreviousAnswer();
		addAnswerToList(previous.getDerivation());
		treePanel.setAnswer(previous);
		treePanel.repaint();
		if (previous.getProductions().size() > 0) {
			statusDisplay.setText("Derived current Strings using " + previous.getProductions().get(0) + " production");
		} else {
			statusDisplay.setText("");
		}
		myPreviousCount++;

		if (myStepCount == myPreviousCount) {
			myPreviousAction.setEnabled(false);
			myStepCount = 0;
			myPreviousCount = 0;
		}
		stepAction.setEnabled(true);
		progress.setText(null);
	}

	/**
	 * Overriding the super-class's parseInput method and intialize the view
	 * settings whenever user inptus new String
	 */
	public void startParseInput(final String string, final UserParser newParser) {
		if (string.equals("")) {
			return;
		}
		if (newParser == null) {
			try {
				myParser = UserParser.get(grammar, string);
			} catch (final IllegalArgumentException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Bad Input", JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {
			myParser = newParser;
		}
		progress.setText("");
		myPreviousAction.setEnabled(false);
		stepAction.setEnabled(false);
		grammarTable.clearSelection();
		myStepCount = 0;
		myPreviousCount = 0;
		myParser.start();
		paintTree();
	}

	/**
	 * This method is called when the step button is pressed.
	 */
	@Override
	public boolean step() {
		// controller.step();
		final int count = myParser.checkValidAndParse(grammarTable.getSelectedRow());
		final String lhs = myParser.getLHSForProduction(grammarTable.getSelectedRow());
		final int length = lhs.length();
		int index = -1;
		if (count > 0) {
			if (count > 1) {
				// Multiple variable but user did not specify one
				if (myStringJList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(this,
							"Multiple Variable Detected, Please click the Variable you want to continue",
							"Select Variable", JOptionPane.ERROR_MESSAGE);
					return false;
				} else {
					String temp = "";
					final List<Integer> tempIndices = Arrays.stream(myStringJList.getSelectedIndices()).boxed()
							.collect(Collectors.toList());
					int tempCount = 0;
					for (final int tempIndice : tempIndices) {
						if (myJListModel.getElementAt(tempIndice).equals(lhs)) {
							tempCount++;
						}
					}
					// this means single production rule is going to be applied
					// to multiple Variables at same time
					if (tempCount == tempIndices.size()) {
						myParser.subsitute(tempIndices);
						paintTree();
						if (myParser.isFinished()) {
							progress.setText("String Accepted!");
							stepAction.setEnabled(false);
						}
						myPreviousAction.setEnabled(true);
						myStepCount++;
						return true;
					}

					if (tempIndices.size() != length) {
						JOptionPane.showMessageDialog(this,
								"Multiple Variable Detected, Please click the Variable you want to continue",
								"Select Variable", JOptionPane.ERROR_MESSAGE);
						return false;
					}

					for (int i = 0; i < length; i++) {
						temp = temp + myJListModel.getElementAt(myStringJList.getSelectedIndex() + i);
					}

					// Production's LHS does not match with what user had
					// selected
					if (!lhs.equals(temp)) {
						JOptionPane.showMessageDialog(this,
								"Multiple Variable Detected, Please click the Variable you want to continue",
								"Select Variable", JOptionPane.ERROR_MESSAGE);
						return false;
					} else {
						index = myStringJList.getSelectedIndex();
					}
				}
			}

			myParser.parse(index);
			paintTree();
			if (myParser.isFinished()) {
				progress.setText("String Accepted!");
				stepAction.setEnabled(false);
			}
			myPreviousAction.setEnabled(true);
			myStepCount++;
			return true;
		} else {
			JOptionPane.showMessageDialog(this, "Previous Derivation does not support this Production", "Bad Input",
					JOptionPane.ERROR_MESSAGE);
			stepAction.setEnabled(false);
			return false;
		}
	}
}
