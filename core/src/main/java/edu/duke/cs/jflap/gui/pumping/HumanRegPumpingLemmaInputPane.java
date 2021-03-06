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

import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.pumping.RegularPumpingLemma;

/**
 * This is a subclass of a <code>HumanFirstPane</code> that deals with regular
 * pumping lemmas.
 *
 * @author Jinghui Lim & Chris Morgan
 * @see edu.duke.cs.jflap.pumping.RegularPumpingLemma
 *
 */
public class HumanRegPumpingLemmaInputPane extends HumanFirstPane {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a <code>HumanRegPumpingInputPane</code> for a
	 * <code>RegularPumpingLemma</code>.
	 *
	 * @param l
	 *            the <code>RegularPumpingLemma</code> we want to run
	 */
	public HumanRegPumpingLemmaInputPane(final RegularPumpingLemma l) {
		super(l, "<i>L</i> = {" + l.getHTMLTitle() + "} Regular Pumping Lemma");
	}

	@Override
	protected void addDecompPanelGameFeatures(final JPanel n) {
		n.setMaximumSize(new Dimension(MAX_SIZE.width, 3 * MAX_SIZE.height / 10));
		n.setPreferredSize(new Dimension(MAX_SIZE.width, 3 * MAX_SIZE.height / 10));
		decompButtonTitle = new String("Set xyz");
		n.setBorder(BorderFactory.createTitledBorder("3. Select decomposition of w into xyz."));
	}

	/**
	 * Creates an HTML string <i>xy<sup>i</sup>z</i>, with the real value of
	 * <i>i</i> instead of the variable <i>i</i>.
	 *
	 * @return a string representing <i>xy<sup>i</sup>z</i>
	 */
	@Override
	protected String createXYZ() {
		return "<i>xy</i><sup>" + myLemma.getI() + "</sup><i>z</i>";
	}

	/**
	 * After <i>m</i> is chosen, prompts the lemma for <i>w</i> and displays it,
	 * and sets up the sliders and table accordingly.
	 *
	 */
	@Override
	protected void mEnteredReset() {
		final String s = myLemma.getW();
		myWDisplay.setText(s);
		myXPanel.setSliderMax(s.length());
		myXPanel.setVal(0);
		myXPanel.setText(myLemma.getW());
		myYPanel.setSliderMax(s.length());
		myYPanel.setVal(0);
		myYPanel.setText(myLemma.getW());
		refresh();
		updateTable();
		leftPanel.revalidate();
	}

	/**
	 * Resets the various fields of <i>x</i>, <i>y</i>, and <i>z</i>, the
	 * display for <i>i</i> and the pumped string, and the canvas.
	 */
	@Override
	protected void refresh() {
		try {
			myXPanel.setRange(0, myYPanel.getVal() - 1);
			myYPanel.setRange(myXPanel.getVal(), myLemma.getM());
			myZDisplay.setText(myLemma.getW().substring(myYPanel.getVal()));
			myZLength.setText(Integer.toString(myZDisplay.getText().length()));

			myIDisplay.setText("");
			myPumpedStringDisplay.setText("");
			myLastWord.setText("");
			myCanvas.reset();

			if (myXPanel.getVal() >= myYPanel.getVal()) {
				myZDisplay.setText("");
				stageMessages[3].setText("Condition violated: |y| > 0");
				myZLength.setText("");
				myDecompButton.setEnabled(false);
			} else if (myYPanel.getVal() > myLemma.getM()) {
				myZDisplay.setText("");
				stageMessages[3].setText("Condition violated: |xy| <= m");
				myZLength.setText("");
				myDecompButton.setEnabled(false);
			} else {
				stageMessages[3].setText("Click \"Set xyz\" to set decomposition.");
				myDecompButton.setEnabled(true);
			}
		} catch (final StringIndexOutOfBoundsException e) {
			/*
			 * Refresh is triggered whenever the slider is moved or the range is
			 * reset. This can cause multiple exceptions when we are merely
			 * changing the max of the slider or adjusting the slider. Thus, the
			 * exception is ignored.
			 */
		}
	}

	@Override
	protected void resetDecompPanel() {
		myXPanel.reset();
		myYPanel.reset();
		myZDisplay.setText("");
		myZLength.setText("");
		updateTable();
	}

	/**
	 * Initializes the animation canvas with the values of <i>x</i>, <i>y</i>,
	 * and <i>z</i>.
	 */
	@Override
	protected void setCanvas() {
		stages[5].setVisible(true);
		myCanvas.reset();
		myCanvas.addText("w =");
		myCanvas.addText(((RegularPumpingLemma) myLemma).getX(), "x");
		myCanvas.addText(((RegularPumpingLemma) myLemma).getY(), "y");
		myCanvas.addText(((RegularPumpingLemma) myLemma).getZ(), "z");
		myCanvas.moveText(Lists.newArrayList(0, 1, myLemma.getI(), 1));
		myStepAnimation.setEnabled(true);
		myStartAnimation.setEnabled(false);
		repaint();
	}

	@Override
	public void setDecomposition(final List<Integer> decomposition) {
		myXPanel.setVal(decomposition.get(0));
		myYPanel.setVal(decomposition.get(0) + decomposition.get(1));
	}

	/**
	 * Calculates <i>i</i> and the string xy<sup>i</sup>z</i>. based on the
	 * user's decomposition of <i>xyz</i>..
	 */
	@Override
	protected void setI() {
		final List<Integer> d = Lists.newArrayList(myXPanel.getVal(), myYPanel.getVal() - myXPanel.getVal());
		myLemma.setDecomposition(d);
		myLemma.chooseI();
		if (myCases != null) {
			myCases.setDecomposition(d);
			myCases.setI(myLemma.getI());
		}
	}

	@Override
	public void update() {

		final RegularPumpingLemma pl = (RegularPumpingLemma) myLemma;
		stageMessages[0].setText("File loaded.");
		updateTopPane(false);

		if (pl.getM() == -1) {
			return;
		}

		myMDisplay.setText("" + pl.getM());
		myWDisplay.setText(pl.getW());

		myXPanel.setText(pl.getW());
		myXPanel.setSliderMax(pl.getW().length());
		myYPanel.setText(pl.getW());
		myYPanel.setSliderMax(pl.getW().length());

		setDecomposition(Lists.newArrayList(pl.getX().length(), pl.getY().length()), pl.getI());
		updateTable();

		stages[2].setVisible(true);
		stages[3].setVisible(true);

		if (pl.getX().length() == 0 && pl.getY().length() == 0) // not yet set
		// decomposition
		{
			myDecompButton.setEnabled(false);
		} else {
			stages[4].setVisible(true);
			stages[5].setVisible(true);
			stageMessages[5].setText("Click \"Restart\" to restart the animation.");
			stageMessages[5].setVisible(true);
			displayIEnd();
			myCanvas.setRestartEnabled(true);
		}
	}
}
