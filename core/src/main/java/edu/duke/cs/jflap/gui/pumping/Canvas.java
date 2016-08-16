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

package edu.duke.cs.jflap.gui.pumping;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * A <code>Canvas</code> is an area where strings,
 * {@link edu.duke.cs.jflap.gui.pumping.Text}, are animated as
 * {@link edu.duke.cs.jflap.gui.pumping.MovingText} and will move to construct
 * the pumped string.
 *
 * @author Jinghui Lim
 * @see edu.duke.cs.jflap.gui.pumping.PumpingLemmaInputPane
 *
 */
public class Canvas extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * A point marking the first row, where the initial text is painted.
	 */
	private static final Point2D.Double FIRST_ROW = new Point2D.Double(40, 40);
	/**
	 * A point marking the second row, where the animated text is moved to.
	 */
	private static final Point2D.Double SECOND_ROW = new Point2D.Double(80, 80);
	/**
	 * The minimum size of the animation area.
	 */
	private static final Dimension MIN_SIZE = new Dimension(300, 80);
	/**
	 * A list of labels.
	 */
	private final ArrayList<Text> myLabelText;
	/**
	 * A list of the initial text on the canvas.
	 */
	private final ArrayList<Text> myInitialText;
	/**
	 * A list of the text that should be animated.
	 */
	private final ArrayList<Text> myMovingText;
	/**
	 * A list of the text that has already moved (animated) to its final
	 * position.
	 */
	private final ArrayList<Text> myFinalText;
	/**
	 * A button that moves the animation forward one step.
	 */
	private JButton myStepButton;
	/**
	 * A button that restarts the animation.
	 */
	private JButton myRestartButton;
	/**
	 * A variable that tells us whether the animation should be running.
	 */
	private boolean wait;

	/**
	 * Constructs a canvas.
	 *
	 */
	public Canvas() {
		super(new BorderLayout());

		myLabelText = new ArrayList<>();
		myInitialText = new ArrayList<>();
		myMovingText = new ArrayList<>();
		myFinalText = new ArrayList<>();
		wait = true;
		setMinimumSize(MIN_SIZE);
		setPreferredSize(MIN_SIZE);
	}

	/**
	 * Add a string to the initial text of this canvas.
	 *
	 * @param s
	 *            string to add
	 * @return the new <code>Text</code> object of the string just added
	 */
	public Text addText(final String s) {
		Point2D p;
		if (myInitialText.isEmpty()) {
			p = FIRST_ROW;
		} else {
			final Text t = myInitialText.get(myInitialText.size() - 1);
			final Point2D q = t.getPos();
			p = new Point2D.Double(q.getX() + t.getWidth(getGraphics()) + Text.SPACE.getWidth(getGraphics()), q.getY());
		}
		Text u = new Text(s, p);
		if (u.toString().length() == 0) {
			u = new Text(Text.SPACE.toString(), p);
		}
		myInitialText.add(u);
		return u;
	}

	/**
	 * Add a string to the initial text of this canvas that has a label.
	 *
	 * @param s
	 *            string to add
	 * @param label
	 *            label of the string
	 * @return the new <code>Text</code> object of the string just added
	 */
	public Text addText(final String s, final String label) {
		final Text u = addText(s);
		myLabelText.add(Text.getLabel(getGraphics(), u, label));
		return u;
	}

	/**
	 * Creates a set of moves for the initial text to the final position. The
	 * position of the final text calculated. Array <code>n</code> states how
	 * many copies of each string, previously placed with
	 * {@link #addText(String)} or {@link #addText(String, String)} the final
	 * text should be. The numbers in the array should be the order that the
	 * text was added in.
	 *
	 * @param arrayList
	 *            the number of copies of final text
	 */
	public void moveText(final List<Integer> arrayList) {
		Point2D p;
		double distance = 0;
		for (int i = 0; i < myInitialText.size(); i++) {
			final Text s = myInitialText.get(i);
			for (int j = 0; j < arrayList.get(i); j++) {
				if (s.toString().length() == 0 || s.toString().equals(Text.SPACE.toString())) {
					continue;
				}

				if (myMovingText.isEmpty()) {
					p = SECOND_ROW;
				} else {
					p = new Point2D.Double(SECOND_ROW.x + distance, SECOND_ROW.y);
				}
				distance += s.getWidth(getGraphics()) + 2; // + 2 is a
				// little extra
				// space between
				// words
				final Text t = new MovingText(s, p);
				myMovingText.add(t);
			}
		}
	}

	/**
	 * Paints all the text on the canvas and executes a move if the animation
	 * has been started.
	 *
	 * @see #start()
	 * @see #stop()
	 */
	@Override
	public void paintComponent(final Graphics pen) {
		for (int i = 0; i < myLabelText.size(); i++) {
			final Text l = (myLabelText.get(i));
			l.paint(pen);
		}
		for (int i = 0; i < myInitialText.size(); i++) {
			final Text t = (myInitialText.get(i));
			t.paint(pen);
		}

		for (int i = 0; i < myFinalText.size(); i++) {
			final Text t = (myFinalText.get(i));
			t.paint(pen);
		}

		if (wait) {
			return;
		} else {
			myRestartButton.setEnabled(true);
		}

		if (!myMovingText.isEmpty()) {
			final MovingText m = (MovingText) myMovingText.get(0);
			if (m.move()) {
				myFinalText.add(m.finalText());
				for (int i = 0; i < myFinalText.size(); i++) {
					final Text t = (myFinalText.get(i));
					t.paint(pen);
				}
				myMovingText.remove(0);
				if (myMovingText.isEmpty()) {
					myStepButton.setEnabled(false);
				}
				return;
			}
			m.paint(pen);
		}

		getRootPane().repaint();
	}

	/**
	 * Resets the canvas for new input.
	 *
	 */
	public void reset() {
		stop();
		myLabelText.clear();
		myInitialText.clear();
		myMovingText.clear();
		myFinalText.clear();
		myStepButton.setEnabled(false);
		myRestartButton.setEnabled(false);
	}

	/**
	 * Sets the restart animation button of this canvas.
	 *
	 * @param b
	 *            the button to set it to
	 */
	public void setRestartButton(final JButton b) {
		myRestartButton = b;
	}

	/**
	 * Sets the "enabled-ness" of the restart button.
	 *
	 * @param b
	 *            "enabled-ness" of the restart button
	 */
	public void setRestartEnabled(final boolean b) {
		myRestartButton.setEnabled(b);
	}

	/**
	 * Sets the step animation button of this canvas.
	 *
	 * @param b
	 *            the button to set it to
	 */
	public void setStepButton(final JButton b) {
		myStepButton = b;
	}

	/**
	 * Starts the animation. This should be first called when the decomposition
	 * of w is set.
	 *
	 */
	public void start() {
		wait = false;
		getRootPane().repaint();
	}

	/**
	 * Halts the animation.
	 *
	 */
	public void stop() {
		wait = true;
	}
}
