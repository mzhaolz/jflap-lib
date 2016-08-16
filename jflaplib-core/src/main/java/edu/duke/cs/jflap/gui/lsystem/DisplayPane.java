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

package edu.duke.cs.jflap.gui.lsystem;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import edu.duke.cs.jflap.grammar.lsystem.Expander;
import edu.duke.cs.jflap.grammar.lsystem.LSystem;
import edu.duke.cs.jflap.gui.ImageDisplayComponent;
import edu.duke.cs.jflap.gui.transform.Matrix;

/**
 * The L-system display pane has the interface to display an L-system.
 *
 * @author Thomas Finley
 */
public class DisplayPane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The L-system we are displaying here. */
	private final LSystem lsystem;

	/** The current expander. */
	private Expander expander = null;

	/** The renderer. */
	private final Renderer renderer = new Renderer();

	/** The image display component. */
	private final ImageDisplayComponent imageDisplay = new ImageDisplayComponent();

	/** The spinner model. */
	private final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 200, 1);

	/** The text field which displays the expansion. */
	private final JTextField expansionDisplay = new JTextField();

	/** The progress indicator. */
	private final JProgressBar progressBar = new JProgressBar(0, 1);

	/** The spinner models for the transforms. */
	private final SpinnerNumberModel pitchModel = new SpinnerNumberModel(0, 0, 359, 15),
			rollModel = new SpinnerNumberModel(0, 0, 359, 15), yawModel = new SpinnerNumberModel(0, 0, 359, 15);

	/**
	 * Implements a display pane.
	 *
	 * @param lsystem
	 *            the L-system to display
	 */
	public DisplayPane(final LSystem lsystem) {
		super(new BorderLayout());
		this.lsystem = lsystem;

		expander = new Expander(lsystem);
		// We can't edit the expansion, of course.
		expansionDisplay.setEditable(false);
		// The user has to be able to change the recursion depth.
		final JSpinner spinner = new JSpinner(spinnerModel);
		spinnerModel.addChangeListener(e -> updateDisplay());
		// Now, for the angle at which the damn thing is viewed...
		final JSpinner s1 = new JSpinner(pitchModel), s2 = new JSpinner(rollModel), s3 = new JSpinner(yawModel);
		final ChangeListener c = e -> updateDisplay();
		pitchModel.addChangeListener(c);
		rollModel.addChangeListener(c);
		yawModel.addChangeListener(c);

		// Lay out the component.
		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(spinner, BorderLayout.EAST);
		topPanel.add(expansionDisplay, BorderLayout.CENTER);
		topPanel.add(progressBar, BorderLayout.WEST);
		add(topPanel, BorderLayout.NORTH);
		final JPanel bottomPanel = new JPanel();
		bottomPanel.add(new JLabel("Pitch"));
		bottomPanel.add(s1);
		bottomPanel.add(new JLabel("Roll"));
		bottomPanel.add(s2);
		bottomPanel.add(new JLabel("Yaw"));
		bottomPanel.add(s3);
		// bottomPanel.setBackground(Color.WHITE);
		final JScrollPane scroller = new JScrollPane(imageDisplay);
		add(scroller, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		// Finally, set the initial display.
		updateDisplay();
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
	 * Prints the current displayed L-system.
	 *
	 * @param g
	 *            the graphics interface for the printer device
	 */
	@Override
	public void printComponent(final Graphics g) {
		final int recursionDepth = spinnerModel.getNumber().intValue();
		final List<String> expansion = expander.expansionForLevel(recursionDepth);
		// Now, set the display.
		final Map<String, String> parameters = lsystem.getValues();
		final Matrix m = new Matrix();
		final double pitch = pitchModel.getNumber().doubleValue(), roll = rollModel.getNumber().doubleValue(),
				yaw = yawModel.getNumber().doubleValue();
		m.pitch(pitch);
		m.roll(roll);
		m.yaw(yaw);
		renderer.render(expansion, parameters, m, (Graphics2D) g, new Point());
	}

	/**
	 * Updates the display.Graphics2D;
	 */
	private void updateDisplay() {
		final int recursionDepth = spinnerModel.getNumber().intValue();
		final List<?> expansion = expander.expansionForLevel(recursionDepth);
		progressBar.setMaximum(expansion.size() * 2);
		imageDisplay.setImage(null);
		final javax.swing.Timer t = new javax.swing.Timer(30, e -> {
			final int i = renderer.getDoneSymbols() - 1;
			progressBar.setValue(i);
			progressBar.repaint();
		});

		final Thread drawThread = new Thread() {
			@Override
			public void run() {
				if (expansion.size() < 70) {
					final String expansionString = LSystemInputPane.listAsString(expansion);
					expansionDisplay.setText(expansionString);
				} else {
					expansionDisplay.setText("Suffice to say, quite long.");
				}
				// Now, set the display.
				final Map<String, String> parameters = lsystem.getValues();

				t.start();
				final Matrix m = new Matrix();
				final double pitch = pitchModel.getNumber().doubleValue(), roll = rollModel.getNumber().doubleValue(),
						yaw = yawModel.getNumber().doubleValue();
				m.pitch(pitch);
				m.roll(roll);
				m.yaw(yaw);
				final Point origin = new Point(); // Ignored, for now.
				final Image image = renderer.render(expansion, parameters, m, null, origin);
				imageDisplay.setImage(image);
				t.stop();
				imageDisplay.repaint();
				imageDisplay.revalidate();
				progressBar.setValue(progressBar.getMaximum());
			}
		};
		drawThread.start();
	}
}
