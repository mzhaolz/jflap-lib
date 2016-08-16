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

package edu.duke.cs.jflap.automata;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JTextArea;

import edu.duke.cs.jflap.gui.editor.DeleteTool;
import edu.duke.cs.jflap.gui.editor.EditorPane;
import edu.duke.cs.jflap.gui.editor.Tool;
import edu.duke.cs.jflap.gui.environment.AutomatonEnvironment;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * A class that represents notes on the JFLAP canvas.
 */
public class Note extends JTextArea {
	private static final long serialVersionUID = 2L;
	private Point myAutoPoint = null;
	public boolean moving = false;
	protected Point initialPointState;
	protected Point initialPointClick;
	protected AutomatonPane myView;
	public Point myViewPoint = new Point(0, 0);

	public Note(final Point point) {
		setLocationManually(point);
	}

	public Note(final Point p, final String message) {
		setLocationManually(p);
		setText(message);
	}

	/**
	 * Creates an instance of <CODE>Note</CODE> with a specified message.
	 */
	public Note(final String message) {
		setText(message);
	}

	public Point getAutoPoint() {
		return myAutoPoint;
	}

	/**
	 * Gets the AutomatonPane that the Note belongs to.
	 *
	 * @return the AutomatonPane that the Note belongs to.
	 */
	public AutomatonPane getView() {
		return myView;
	}

	/**
	 * Initializes the note with relevant properties.
	 */
	public void initializeForView(final AutomatonPane view) {
		myView = view;
		setLocationManually(myAutoPoint);
		setDisabledTextColor(Color.BLACK);
		setBackground(new Color(255, 255, 150));
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					return;
				}
				if (!((Note) e.getSource()).isEditable()) {
					final int diffX = e.getPoint().x - initialPointClick.x;
					final int diffY = e.getPoint().y - initialPointClick.y;

					final int nowAtX = initialPointState.x + diffX;
					final int nowAtY = initialPointState.y + diffY;
					((Note) e.getSource()).setLocationManually(new Point(nowAtX, nowAtY));
					initialPointState = new Point(((Note) e.getSource()).getAutoPoint());
				} else {
					// do normal select functionality
				}
				myView.repaint();
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (myView.getDrawer().getAutomaton().getEnvironmentFrame() != null) {
					((AutomatonEnvironment) myView.getDrawer().getAutomaton().getEnvironmentFrame().getEnvironment())
							.saveStatus();
				}
				((Note) e.getComponent()).setEnabled(true);
				((Note) e.getComponent()).setEditable(true);
				((Note) e.getComponent()).setCaretColor(null);
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(final MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(final MouseEvent e) {
				if (myView.getDrawer().getAutomaton().getEnvironmentFrame() != null) {
					((AutomatonEnvironment) myView.getDrawer().getAutomaton().getEnvironmentFrame().getEnvironment())
							.saveStatus();
					((AutomatonEnvironment) myView.getDrawer().getAutomaton().getEnvironmentFrame().getEnvironment())
							.setDirty();
				}

				initialPointState = new Point(((Note) e.getSource()).getAutoPoint());
				initialPointClick = new Point(e.getPoint().x, e.getPoint().y);

				// delete the text box
				final EditorPane pane = myView.getCreator();
				final Tool curTool = pane.getToolBar().getCurrentTool();
				if (curTool instanceof DeleteTool) {
					myView.remove((Note) e.getSource());
					myView.getDrawer().getAutomaton().deleteNote((Note) e.getSource());
					myView.repaint();
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
		myView.add(this);
		setEnabled(true);
		setEditable(true);
		setCaretColor(null);
		setSelectionStart(0);
		setSelectionEnd(getColumnWidth());
		this.requestFocus();
	}

	@Override
	public void setLocation(final int x, final int y) {
		if (moving) {
			super.setLocation(x, y);
		}
		moving = false;
	}

	@Override
	public void setLocation(final Point p) {
		if (moving) {
			if (myView != null) {
				myViewPoint = p;
				super.setLocation(p);
			}
		}
	}

	/**
	 * Sets the Note manually to a specified Point.
	 *
	 * @param point
	 */
	public void setLocationManually(final Point point) {
		moving = true;
		myAutoPoint = point;
		if (myView != null) {
			setLocation(myView.transformFromAutomatonToView(point));
		}
	}

	/*
	 * For the undo part of cloning, we need a way to store the view without
	 * becoming visible / active.
	 */
	public void setView(final AutomatonPane view) {
		myView = view;
	}

	public int specialHash() {
		// EDebug.print(myAutoPoint.hashCode() + getText().hashCode());
		return myAutoPoint == null ? -1 : myAutoPoint.hashCode() + this.getText().hashCode();
	}

	public void updateView() {
		setLocationManually(myAutoPoint);
	}
}
