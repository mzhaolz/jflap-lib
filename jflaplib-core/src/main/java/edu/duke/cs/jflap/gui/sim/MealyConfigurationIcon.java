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

package edu.duke.cs.jflap.gui.sim;

import java.awt.Component;
import java.awt.Graphics2D;

import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.automata.mealy.MealyConfiguration;

/**
 *
 * @author Jinghui Lim
 *
 */
public class MealyConfigurationIcon extends ConfigurationIcon {
	public MealyConfigurationIcon(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public void paintConfiguration(final Component c, final Graphics2D g, final int width, final int height) {
		super.paintConfiguration(c, g, width, height);
		final MealyConfiguration config = (MealyConfiguration) getConfiguration();
		// Draw the torn tape with the rest of the input.
		Torn.paintString(g, config.getInput(), RIGHT_STATE.x + 5.0f, (super.getIconHeight()) * 0.5f, Torn.MIDDLE,
				width - RIGHT_STATE.x - 5.0f, false, true,
				config.getInput().length() - config.getUnprocessedInput().length());
		// Draw the stack.
		Torn.paintString(g, config.getOutput(), BELOW_STATE.x, BELOW_STATE.y + 5.0f, Torn.TOP, getIconWidth(), false,
				true, -1);
	}
}
