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

package edu.duke.cs.jflap.gui.deterministic;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.TooltipAction;
import edu.duke.cs.jflap.gui.editor.ArrowNontransitionTool;
import edu.duke.cs.jflap.gui.editor.EditorPane;
import edu.duke.cs.jflap.gui.editor.Tool;
import edu.duke.cs.jflap.gui.editor.ToolBox;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.viewer.AutomatonDraggerPane;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * This is the pane where the user defines all that is needed for the conversion
 * of an NFA to a DFA.
 *
 * @author Thomas Finley
 */
public class ConversionPane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 2853526626829551932L;

	/** The controller object. */
	private final ConversionController controller;

	/** The editor pane. */
	EditorPane editor;

	/**
	 * Instantiates a new <CODE>ConversionPane</CODE>.
	 *
	 * @param nfa
	 *            the NFA we are converting to a DFA
	 * @param environment
	 *            the environment this pane will be added to
	 */
	public ConversionPane(final FiniteStateAutomaton nfa, final Environment environment) {
		super(new BorderLayout());
		final FiniteStateAutomaton dfa = new FiniteStateAutomaton();
		controller = new ConversionController(nfa, dfa, this);
		// Create the left view of the original NFA.
		final AutomatonPane nfaPane = new AutomatonDraggerPane(nfa);
		// Put it all together.
		final JSplitPane split = SplitPaneFactory.createSplit(environment, true, .25, nfaPane, createEditor(dfa));
		add(split, BorderLayout.CENTER);

		// When the component is first shown, perform layout.
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(final ComponentEvent event) {
				// We may now lay out the states...
				controller.performFirstLayout();
				editor.getAutomatonPane().repaint();
			}
		});
	}

	/**
	 * Adds the extra controls to the toolbar for the editorpane.
	 *
	 * @param toolbar
	 *            the tool bar to add crap to
	 */
	private void addExtras(final JToolBar toolbar) {
		toolbar.addSeparator();
		toolbar.add(new TooltipAction("Complete", "This will finish all expansion.") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1587659531575512768L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				controller.complete();
			}
		});
		toolbar.add(new TooltipAction("Done?", "Are we finished?") {
			/**
			 *
			 */
			private static final long serialVersionUID = -3054950168721834336L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				controller.done();
			}
		});
	}

	/**
	 * Creates the editor pane for the DFA.
	 *
	 * @param dfa
	 *            the dfa to create the editor pane for
	 */
	private EditorPane createEditor(final FiniteStateAutomaton dfa) {
		final SelectionDrawer drawer = new SelectionDrawer(dfa);
		editor = new EditorPane(drawer, (ToolBox) (view, drawer1) -> {
			final List<Tool> tools = new LinkedList<>();
			tools.add(new ArrowNontransitionTool(view, drawer1));
			tools.add(new TransitionExpanderTool(view, drawer1, controller));
			tools.add(new StateExpanderTool(view, drawer1, controller));
			return tools;
		});
		addExtras(editor.getToolBar());
		return editor;
	}
}
