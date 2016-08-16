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

import java.util.ArrayList;

import edu.duke.cs.jflap.pumping.cf.AiBjCk;
import edu.duke.cs.jflap.pumping.cf.AkBnCnDj;
import edu.duke.cs.jflap.pumping.cf.AnBjAnBj;
import edu.duke.cs.jflap.pumping.cf.AnBn;
import edu.duke.cs.jflap.pumping.cf.AnBnCn;
import edu.duke.cs.jflap.pumping.cf.NaNbNc;
import edu.duke.cs.jflap.pumping.cf.NagNbeNc;
import edu.duke.cs.jflap.pumping.cf.W1BnW2;
import edu.duke.cs.jflap.pumping.cf.W1CW2CW3CW4;
import edu.duke.cs.jflap.pumping.cf.W1VVrW2;
import edu.duke.cs.jflap.pumping.cf.WW;
import edu.duke.cs.jflap.pumping.cf.WW1WrEquals;
import edu.duke.cs.jflap.pumping.cf.WW1WrGrtrThanEq;

/**
 * A <code>CFPumpingLemmaChooser</code> is a
 * {@link edu.duke.cs.jflap.gui.pumping.PumpingLemmaChooser} for
 * {@link edu.duke.cs.jflap.pumping.ContextFreePumpingLemma}s.
 *
 * @author Jinghui Lim
 *
 */
public class CFPumpingLemmaChooser extends PumpingLemmaChooser {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Adds all the context-free pumping lemmas.
	 *
	 */
	public CFPumpingLemmaChooser() {
		myList = new ArrayList<>();

		// old languages
		myList.add(new AnBnCn());
		myList.add(new WW());
		myList.add(new AnBjAnBj());
		myList.add(new NaNbNc());
		myList.add(new NagNbeNc());
		myList.add(new AiBjCk());
		myList.add(new AnBn());

		// new languages (JFLAP 6.2)
		myList.add(new AkBnCnDj());
		myList.add(new WW1WrGrtrThanEq());
		myList.add(new WW1WrEquals());
		myList.add(new W1BnW2());
		myList.add(new W1CW2CW3CW4());
		myList.add(new W1VVrW2());
	}
}
