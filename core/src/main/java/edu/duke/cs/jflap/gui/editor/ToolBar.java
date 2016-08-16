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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;

/**
 * A tool bar for editing and manipulating an automaton.
 *
 * @author Thomas Finley
 */
public class ToolBar extends JToolBar implements ActionListener {
	/**
	 * The action that clicks a button.
	 */
	private class ButtonClicker extends AbstractAction {
		private static final long serialVersionUID = 7L;

		AbstractButton button;

		public ButtonClicker(final AbstractButton button) {
			this.button = button;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			button.doClick();
		}
	}

	private static final long serialVersionUID = 6L;

	private final Component view;

	private final AutomatonDrawer drawer;

	private final List<Tool> tools;

	private final HashMap<JToggleButton, Tool> buttonsToTools = new HashMap<>();

	private final ToolAdapter adapter;

	private Tool currentTool = null;

	/**
	 * Instantiates a new tool bar.
	 *
	 * @param view
	 *            the view the automaton is displayed in
	 * @param drawer
	 *            the automaton drawer
	 * @param box
	 *            the toolbox to get the initial tools to put in the bar
	 */
	public ToolBar(final EditCanvas view, final AutomatonDrawer drawer, final ToolBox box) {
		super();
		adapter = new ToolAdapter(view);
		this.view = view;
		this.drawer = drawer;
		tools = box.tools(view, drawer);
		initBar();
		view.addMouseListener(adapter);
		view.addMouseMotionListener(adapter);
	}

	/**
	 * If a tool is clicked, sets the new current tool.
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final Tool tool = buttonsToTools.get(e.getSource());
		if (tool != null) {
			adapter.setAdapter(tool);
			currentTool = tool;
			view.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		if (tool instanceof DeleteTool) {
			final Toolkit toolkit = Toolkit.getDefaultToolkit();
			// Image image =
			// toolkit.getImage("/JFLAP09CVS/ICON/deletecursor.gif");
			final URL url = getClass().getResource("/ICON/deletecursor.gif");
			final Image image = getToolkit().getImage(url);
			final Point hotSpot = new Point(5, 5);
			final Cursor cursor = toolkit.createCustomCursor(image, hotSpot, "Delete");
			view.setCursor(cursor);
			// Cursor hourglassCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
			// view.setCursor(hourglassCursor);
		}
	}

	/**
	 * Draws the tool view.
	 *
	 * @param g
	 *            the graphics object to draw upon
	 */
	public void drawTool(final Graphics g) {
		if (currentTool == null) {
			return;
		}
		currentTool.draw(g);
	}

	public Tool getCurrentTool() {
		return currentTool;
	}

	/**
	 * Returns the automaton drawer for the automaton.
	 *
	 * @return the automaton drawer for the automaton
	 */
	protected AutomatonDrawer getDrawer() {
		return drawer;
	}

	/**
	 * Returns the view that the automaton is drawn in.
	 *
	 * @return the view that the automaton is drawn in
	 */
	protected Component getView() {
		return view;
	}

	/**
	 * Initializes the tool bar.
	 */
	private void initBar() {
		final ButtonGroup group = new ButtonGroup();
		JToggleButton button = null;
		final Iterator<Tool> it = tools.iterator();
		KeyStroke key;
		while (it.hasNext()) {
			final Tool tool = it.next();
			button = new JToggleButton(tool.getIcon());
			buttonsToTools.put(button, tool);
			button.setToolTipText(tool.getShortcutToolTip());
			group.add(button);
			this.add(button);
			button.addActionListener(this);
			key = tool.getKey();
			if (key == null) {
				continue;
			}
			final InputMap imap = button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			final ActionMap amap = button.getActionMap();
			final Object o = new Object();
			imap.put(key, o);
			amap.put(o, new ButtonClicker(button));
		}
	}
}
