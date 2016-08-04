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
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

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
  public ConvertPane(
      Grammar grammar,
      Automaton automaton,
      Map<Production, Transition> productionsToTransitions,
      Environment env) {
    this.setLayout(new BorderLayout());
    JSplitPane split = SplitPaneFactory.createSplit(env, true, .4, null, null);
    this.add(split, BorderLayout.CENTER);

    grammarViewer = new GrammarViewer(grammar);
    this.add(new TableTextSizeSlider(grammarViewer), BorderLayout.NORTH);
    JScrollPane scroller = new JScrollPane(grammarViewer);
    split.setLeftComponent(scroller);
    // Create the right view.

    automatonDrawer = new SelectionDrawer(automaton);
    EditorPane ep =
        new EditorPane(
            automatonDrawer,
            new ToolBox() {
              public List<Tool> tools(AutomatonPane view, AutomatonDrawer drawer) {
                LinkedList<Tool> tools = new LinkedList<Tool>();
                tools.add(new ArrowNontransitionTool(view, drawer));
                tools.add(new TransitionTool(view, drawer));
                return tools;
              }
            });
    // Create the controller device.
    ConvertController controller =
        new ConvertController(grammarViewer, automatonDrawer, productionsToTransitions, this);
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
  private void controlPanel(JToolBar bar, final ConvertController controller) {
    bar.addSeparator();
    bar.add(
        new AbstractAction("Show All") {
          /**
           *
           */
          private static final long serialVersionUID = 976825934777026919L;

          public void actionPerformed(ActionEvent e) {
            controller.complete();
          }
        });
    bar.add(
        new AbstractAction("Create Selected") {
          /**
           *
           */
          private static final long serialVersionUID = -3148925991091992877L;

          public void actionPerformed(ActionEvent e) {
            controller.createForSelected();
          }
        });
    bar.add(
        new AbstractAction("Done?") {
          /**
           *
           */
          private static final long serialVersionUID = 7142173663791405435L;

          public void actionPerformed(ActionEvent e) {
            controller.isDone();
          }
        });
    bar.add(
        new AbstractAction("Export") {
          /**
           *
           */
          private static final long serialVersionUID = 9189517666052681184L;

          public void actionPerformed(ActionEvent e) {
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

  /** The grammar viewer. */
  private GrammarViewer grammarViewer;

  /** The automaton selection drawer. */
  private SelectionDrawer automatonDrawer;

  /** The editor pane. */
  private EditorPane editorPane;
}
