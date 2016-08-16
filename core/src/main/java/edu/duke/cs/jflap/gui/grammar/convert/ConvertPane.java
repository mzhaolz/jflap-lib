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

package edu.duke.cs.jflap.gui.grammar.convert;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.TableTextSizeSlider;
import edu.duke.cs.jflap.gui.editor.ArrowNontransitionTool;
import edu.duke.cs.jflap.gui.editor.EditorPane;
import edu.duke.cs.jflap.gui.editor.Tool;
import edu.duke.cs.jflap.gui.editor.ToolBox;
import edu.duke.cs.jflap.gui.editor.TransitionTool;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * This is a graphical component to aid in the conversion of a context free
 * grammar to some form of pushdown automaton.
 *
 * @author Thomas Finley
 */
public class ConvertPane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = -5288648720223862725L;

	/** The grammar viewer. */
	private GrammarViewer grammarViewer;

	/** The automaton selection drawer. */
	private SelectionDrawer automatonDrawer;

	/** The editor pane. */
	private EditorPane editorPane;

	/**
	 * Instantiates a <CODE>ConvertPane</CODE>.
	 *
	 * @param grammar
	 *            the grammar to convert
	 * @param automaton
	 *            a "starting automaton" that may already have some start points
	 *            predefined
	 * @param productionsToTransitions
	 *            the mapping of productions to transitions, which should be one
	 *            to one
	 * @param env
	 *            the environment to which this pane will be added
	 */
	public ConvertPane(final Grammar grammar, final Automaton automaton,
			final Map<Production, Transition> productionsToTransitions, final Environment env) {
		setLayout(new BorderLayout());
		final JSplitPane split = SplitPaneFactory.createSplit(env, true, .4, null, null);
		this.add(split, BorderLayout.CENTER);

		grammarViewer = new GrammarViewer(grammar);
		this.add(new TableTextSizeSlider(grammarViewer), BorderLayout.NORTH);
		final JScrollPane scroller = new JScrollPane(grammarViewer);
		split.setLeftComponent(scroller);
		// Create the right view.

		automatonDrawer = new SelectionDrawer(automaton);
		final EditorPane ep = new EditorPane(automatonDrawer, (ToolBox) (view, drawer) -> {
			final LinkedList<Tool> tools = new LinkedList<>();
			tools.add(new ArrowNontransitionTool(view, drawer));
			tools.add(new TransitionTool(view, drawer));
			return tools;
		});
		// Create the controller device.
		final ConvertController controller = new ConvertController(grammarViewer, automatonDrawer,
				productionsToTransitions, this);
		controlPanel(ep.getToolBar(), controller);
		split.setRightComponent(ep);
		editorPane = ep;
	}

	/**
	 * Initializes the control objects in the editor pane's tool bar.
	 *
	 * @param controller
	 *            the controller object
	 */
	private void controlPanel(final JToolBar bar, final ConvertController controller) {
		bar.addSeparator();
		bar.add(new AbstractAction("Show All") {
			/**
			 *
			 */
			private static final long serialVersionUID = 976825934777026919L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				controller.complete();
			}
		});
		bar.add(new AbstractAction("Create Selected") {
			/**
			 *
			 */
			private static final long serialVersionUID = -3148925991091992877L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				controller.createForSelected();
			}
		});
		bar.add(new AbstractAction("Done?") {
			/**
			 *
			 */
			private static final long serialVersionUID = 7142173663791405435L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				controller.isDone();
			}
		});
		bar.add(new AbstractAction("Export") {
			/**
			 *
			 */
			private static final long serialVersionUID = 9189517666052681184L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				controller.export();
			}
		});
	}

	/**
	 *
	 * /** Returns the editor pane.
	 *
	 * @return the editor pane
	 */
	public EditorPane getEditorPane() {
		return editorPane;
	}
}
