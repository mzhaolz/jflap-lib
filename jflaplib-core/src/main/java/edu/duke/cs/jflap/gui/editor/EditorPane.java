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

package edu.duke.cs.jflap.gui.editor;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.Note;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * This is a view that holds a tool bar and the canvas where the automaton is
 * displayed.
 *
 * @author Thomas Finley
 */
public class EditorPane extends JComponent {
	private static final long serialVersionUID = 5L;

	/** The automaton. */
	protected Automaton automaton;

	/** The automaton drawer. */
	protected AutomatonDrawer drawer;

	/** The automaton pane. */
	protected EditCanvas pane;

	/** The tool bar. */
	protected edu.duke.cs.jflap.gui.editor.ToolBar toolbar;

	/**
	 * Instantiates a new editor pane for the given automaton.
	 *
	 * @param automaton
	 *            the automaton to create the editor pane for
	 */
	public EditorPane(final Automaton automaton) {
		this(new SelectionDrawer(automaton));
	}

	/**
	 * Instantiates a new editor pane with a tool box.
	 */
	public EditorPane(final Automaton automaton, final ToolBox box) {
		this(new SelectionDrawer(automaton), box);
	}

	/**
	 * Instantiates a new editor pane with a given automaton drawer.
	 *
	 * @param drawer
	 *            the special automaton drawer for this editor
	 */
	public EditorPane(final AutomatonDrawer drawer) {
		this(drawer, new DefaultToolBox());
	}

	/**
	 * Instantiates a new editor pane with a given automaton drawer.
	 *
	 * @param drawer
	 *            the special automaton drawer for this editor
	 * @param box
	 *            the tool box to get the tools from
	 */
	public EditorPane(final AutomatonDrawer drawer, final ToolBox box) {
		this(drawer, box, false);
	}

	/**
	 * Instantiates a new editor pane with a given automaton drawer.
	 *
	 * @param drawer
	 *            the special automaton drawer for this editor
	 * @param box
	 *            the tool box to get teh tools from
	 * @param fit
	 *            <CODE>true</CODE> if the editor should resize its view to fit
	 *            the automaton; note that this can be <I>very</I> annoying if
	 *            the automaton changes
	 */
	public EditorPane(final AutomatonDrawer drawer, final ToolBox box, final boolean fit) {
		pane = new EditCanvas(drawer, fit);
		pane.setCreator(this);
		this.drawer = drawer;
		automaton = drawer.getAutomaton();
		setLayout(new BorderLayout());

		final JPanel superpane = new JPanel();
		superpane.setLayout(new BorderLayout());
		superpane.add(new JScrollPane(pane, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);
		superpane.setBorder(new BevelBorder(BevelBorder.LOWERED));

		toolbar = new edu.duke.cs.jflap.gui.editor.ToolBar(pane, drawer, box);
		pane.setToolBar(toolbar);

		this.add(superpane, BorderLayout.CENTER);
		this.add(toolbar, BorderLayout.NORTH);
		this.add(new AutomatonSizeSlider(pane, drawer), BorderLayout.SOUTH);

		final List<Note> notes = drawer.getAutomaton().getNotes();
		for (int k = 0; k < notes.size(); k++) {
			notes.get(k).initializeForView(pane);
		}
	}

	/**
	 * Returns the automaton pane.
	 *
	 * @return the automaton pane
	 */
	public Automaton getAutomaton() {
		return automaton;
	}

	/**
	 * Returns the automaton pane.
	 *
	 * @return the automaton pane
	 */
	public EditCanvas getAutomatonPane() {
		return pane;
	}

	/**
	 * Returns the automaton drawer for the editor pane canvas.
	 *
	 * @return the drawer that draws the automaton being edited
	 */
	public AutomatonDrawer getDrawer() {
		return pane.getDrawer();
	}

	/**
	 * Returns the toolbar for this editor pane.
	 *
	 * @return the toolbar of this editor pane
	 */
	public edu.duke.cs.jflap.gui.editor.ToolBar getToolBar() {
		return toolbar;
	}

	/**
	 * Children are not painted here.
	 *
	 * @param g
	 *            the graphics object to paint to
	 */
	@Override
	public void printChildren(final Graphics g) {
	}

	/**
	 * Prints this component. This will print only the automaton section of the
	 * component.
	 *
	 * @param g
	 *            the graphics object to paint to
	 */
	@Override
	public void printComponent(final Graphics g) {
		pane.print(g);
	}
}
