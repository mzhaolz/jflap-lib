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

package edu.duke.cs.jflap.pumping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>PumpingLemma</code> contains the information needed to guide the user
 * through a pumping lemma proof.
 *
 * @author Jinghui Lim & Chris Morgan
 *
 */
public abstract class PumpingLemma implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -5006082787532388783L;
	/**
	 * Tag for when the computer goes first.
	 */
	public static final String COMPUTER = "Computer";
	/**
	 * Tag for when the user goes first.
	 */
	public static final String HUMAN = "Human";
	/**
	 * String representing "{<i>a</i>, <i>b</i>}*".
	 */
	protected static String AB_STAR = "{<i>a</i>, <i>b</i>}*";
	/**
	 * String representing "not equals", "&#8800;".
	 */
	protected static String NOT_EQUAL = "&#8800;";
	/**
	 * HTML code for "element of", "&#8712;".
	 */
	protected static String ELEMENT_OF = "&#8712;";
	/**
	 * HTML code for "greater than or equal to", "&#8805;".
	 */
	protected static String GREATER_OR_EQ = "&#8805;";
	/**
	 * HTML code for "grater than", "&#62;".
	 */
	protected static String GREATER_THAN = "&#62;";
	/**
	 * HTML code for "less than", "&#60;".
	 */
	protected static String LESS_THAN = "&#60;";
	/**
	 * HTML code for "less than or equal to", "&#8804;".
	 */
	protected static String LESS_OR_EQ = "&#8804;";

	/**
	 * Returns a string repeated i times, <i>s<sup>i</sup></i>.
	 *
	 * @param s
	 *            the string to repeat
	 * @param i
	 *            the number of times to repeat it
	 * @return the string <i>s<sup>i</sup></i>
	 */
	protected static String pumpString(final String s, final int i) {
		final StringBuffer sb = new StringBuffer();
		for (int n = i; n > 0; n--) {
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * States whether there is a valid partition for the language in question.
	 */
	protected boolean partitionIsValid;
	/**
	 * A description of why this language does or does not have a valid
	 * partition.
	 */
	protected String explanation;
	/**
	 * States who the first player is, the human or the computer.
	 */
	protected String firstPlayer;
	/**
	 * The <i>m</i> value used by this pumping lemma.
	 */
	protected int m;
	/**
	 * The <i>w</i> value used by this pumping lemma.
	 */
	protected String w;
	/**
	 * The <i>w</i> value used by this pumping lemma.
	 */
	protected int i;
	/**
	 * All the possible cases. Cases should not be removed from this list.
	 */
	protected ArrayList<Case> myAllCases;
	/**
	 * Cases that the user has already done. Cases should not be removed from
	 * <code>myAllCases</code> to add to this, they should just be added.
	 */
	protected ArrayList<Case> myDoneCases;
	/**
	 * Stores all the decompositions and <i>i</i> values the user has already
	 * attempted in separate int[] entries.
	 */
	protected ArrayList<String> myAttempts;
	/**
	 * A suggested range for <i>m</i>.
	 */
	protected List<Integer> myRange;

	/**
	 * The current decomposition of this lemma.
	 */
	protected List<Integer> myDecomposition;

	/**
	 * Constructs a new <code>PumpingLemma</code>.
	 *
	 */
	public PumpingLemma() {
		myDoneCases = new ArrayList<>();
		myAllCases = new ArrayList<>();
		myAttempts = new ArrayList<>();
		setRange();
		setDescription();
		reset();
		addCases();
	}

	/**
	 * Adds the given attempt to the list of attempts.
	 *
	 * @param attempt
	 *            the attempt to be added.
	 */
	public void addAttempt(final String attempt) {
		myAttempts.add(attempt);
	}

	/**
	 * Adds the decomposition to the list that the user has done. It should only
	 * be called after {@link #setDecomposition(int[])} to ensure the fields are
	 * set up properly. If the case is not a legal decomposition, the it returns
	 * <code>-1</code>. If the case has already been done, it returns the index
	 * of the case without changing the case. If it hasn't been done, it moves
	 * the case to the "done" list and returns its position in the done list.
	 *
	 * @see Case#isCase(String, String)
	 * @see #replaceCase(int[], int, int)
	 * @param decomposition
	 *            the decomposition we wish to add
	 * @param num
	 *            the value of <i>i</i>
	 * @return <code>-1</code> if it is an illegal decomposition, the index of
	 *         the decomposition if it has already been done, or a number bigger
	 *         than or equal to the total number of cases, which can be found by
	 *         calling {@link #numCasesTotal()}.
	 */
	public abstract int addCase(List<Integer> decomposition, int num);

	/**
	 * Initializes all the possible cases for this pumping lemma.
	 *
	 */
	protected abstract void addCases();

	/**
	 * The computer chooses an acceptable decomposition for the string when
	 * given an acceptable 'w' value. 'w' is known to be in the lemma before
	 * this method is called. Regular lemmas will have two values (x-y, y-z)
	 * dividers, while context-free lemmas will have four. Called when the
	 * computer goes first.
	 */
	public abstract void chooseDecomposition();

	/**
	 * The computer chooses and returns a suitable number of times to pump the
	 * string, <i>i</i>. Called when the user goes first.
	 */
	public abstract void chooseI();

	/**
	 * The computer chooses a sutiable value for m. Called when the computer
	 * goes first
	 */
	public void chooseM() {
		m = LemmaMath.fetchRandInt(myRange.get(0), myRange.get(1));
	}

	/**
	 * The computer chooses a suitable string <i>w</i>. Called when the user
	 * goes first.
	 */
	protected abstract void chooseW();

	/**
	 * Clears all existing attempts from the list of attempts.
	 */
	public void clearAttempts() {
		myAttempts.clear();
	}

	/**
	 * Removes a particular case from the "done" pile.
	 *
	 * @param n
	 *            the index of the case to be removed
	 */
	public void clearCase(final int n) {
		myDoneCases.remove(n).reset();
	}

	/**
	 * Clears all cases that the user has done. The user set decompositions of
	 * those cases are also cleared.
	 */
	public void clearDoneCases() {
		myDoneCases.clear();
		for (int i = 0; i < myAllCases.size(); i++) {
			myAllCases.get(i).reset();
		}
	}

	/**
	 * Returns the pumped string according the the decomposition and choice of
	 * <i>i</i>.
	 *
	 * @return the pumped string
	 */
	public abstract String createPumpedString();

	/**
	 * Does all the remaining undone cases.
	 */
	public void doAll() {
		for (int i = 0; i < myAllCases.size(); i++) {
			if (!myDoneCases.contains(myAllCases.get(i))) {
				myDoneCases.add(myAllCases.get(i));
			}
		}
	}

	/**
	 * Returns the list of attempts already made.
	 *
	 * @return the current list of attempts
	 */
	public ArrayList<String> getAttempts() {
		return myAttempts;
	}

	/**
	 * Returns the case at <code>index</code> that was added with
	 * {@link #addCase(int[], int)}.
	 *
	 * @param index
	 *            the index of the decomposition to be retrieved
	 * @return the case at <code>index</code>
	 */
	public Case getCase(final int index) {
		return myDoneCases.get(index);
	}

	/**
	 * Returns the current decomposition as an int[].
	 *
	 * @return the current decomposition
	 */
	public List<Integer> getDecomposition() {
		return myDecomposition;
	}

	/**
	 * Returns the decomposition as a string with explicit labeling of which
	 * variables in the decomposition are assigned to what substrings.
	 */
	public abstract String getDecompositionAsString();

	/**
	 * Returns the list of done {@link edu.duke.cs.jflap.pumping.Case}s.
	 *
	 * @return the list of done <code>Case</code>s
	 */
	public ArrayList<?> getDoneCases() {
		return myDoneCases;
	}

	/**
	 * Returns an <code>ArrayList</code> of <code>String</code>s that describe
	 * each case.
	 *
	 * @return descriptions of each case
	 */
	public ArrayList<String> getDoneDescriptions() {
		final ArrayList<String> ret = new ArrayList<>();
		for (int i = 0; i < myDoneCases.size(); i++) {
			ret.add(myDoneCases.get(i).toString());
		}
		return ret;
	}

	/**
	 * Returns the explanation of this language. The explanation has html tags,
	 * so the explanation should only be shown in an html text editor.
	 *
	 * @return the explanation
	 */
	public String getExplanation() {
		return explanation;
	}

	/**
	 * Returns who is the first player, the human or the computer.
	 *
	 * @return the first player
	 */
	public String getFirstPlayer() {
		return firstPlayer;
	}

	/**
	 * Returns a title with HTML tags that will allow for a better
	 * representation of the language of the lemma.
	 *
	 * @return a title with HTML tags
	 */
	public abstract String getHTMLTitle();

	/**
	 * Returns the number of times to pump the string, <i>i</i>.
	 *
	 * @return the current <i>i</i> value
	 */
	public int getI() {
		return i;
	}

	/**
	 * Returns the current <i>m</i> value.
	 *
	 * @return the current <i>m</i> value
	 */
	public int getM() {
		return m;
	}

	/**
	 * Returns whether a valid partition can be applied to this function,
	 * meaning there is a valid repeatable decomposition.
	 *
	 * @return the partition's validity
	 */
	public boolean getPartitionValidity() {
		return partitionIsValid;
	}

	/**
	 * Returns the recommended range for<i>m</i>.
	 *
	 * @return the recommended range for<i>m</i>
	 */
	public List<Integer> getRange() {
		return myRange;
	}

	/**
	 * Returns a string title of the pumping lemma.
	 *
	 * @return the title of the lemma
	 */
	public abstract String getTitle();

	/**
	 * Returns the current <i>w</i> value.
	 *
	 * @return the current <i>w</i> value
	 */
	public String getW() {
		return w;
	}

	/**
	 * Tests if the given string is in the language represented by this <code>
	 * PumpingLemma</code>.
	 *
	 * @return <code>true</code> if the pumped string is in the language,
	 *         <code>false</code> otherwise
	 */
	public abstract boolean isInLang(String s);

	/**
	 * Returns the total number of cases.
	 *
	 * @return the total number of cases
	 */
	public int numCasesTotal() {
		return myAllCases.size();
	}

	/**
	 * Replaces the decomposition in the list of that the user has done. It
	 * should only be called after {@link #setDecomposition(int[])} to ensure
	 * the fields are set up properly. If the user has not done the case, it
	 * returns <code>false</code>.
	 *
	 * @see Case#isCase(String, String)
	 * @see #addCase(int[], int)
	 * @param tempDecomposition
	 *            the decomposition we wish to add
	 * @param num
	 *            the value of <i>i</i>
	 * @param index
	 *            the place of the decomposition we wish to have replaced
	 * @return <code>true</code> if the decomposition and case match,
	 *         <code>false<code> otherwise
	 */
	public abstract boolean replaceCase(List<Integer> tempDecomposition, int num, int index);

	/**
	 * Clears all information the user has entered and other fields, including
	 * <i>m</i>, <i>w</i>, and its decomposition.
	 *
	 */
	public abstract void reset();

	/**
	 * Sets the decomposition, with the length of each part of the decomposition
	 * in the corresponding space of the input array, then chooses an acceptable
	 * <i>i</i>.
	 *
	 * @see #setDecomposition(int[], int)
	 * @param decomposition
	 *            the array that contains the length of each part of the
	 *            decomposition
	 * @return <code>true</code> if this decomposition is legal,
	 *         <code>false</code> otherwise
	 */
	public abstract boolean setDecomposition(List<Integer> decomposition);

	/**
	 * Sets <i>i</i> and sets the decomposition given.
	 *
	 * @param list
	 *            the decomposition to set for this lemma
	 * @param num
	 *            the number to set <i>i</i> to
	 * @return <code>true</code> if this deocmposition is legal,
	 *         <code>false</code> otherwise
	 */
	public abstract boolean setDecomposition(List<Integer> list, int num);

	/**
	 * Sets the <code>isInClassification</code> and <code>explanation</code>
	 * values.
	 */
	protected abstract void setDescription();

	/**
	 * Sets who the first player is.
	 */
	public void setFirstPlayer(final String s) {
		firstPlayer = s;
	}

	/**
	 * Sets the <i>i</i> this instance of the pumping lemma uses.
	 *
	 * @param num
	 *            the value <i>i</i> will be set to
	 */
	public void setI(final int num) {
		i = num;
	}

	/**
	 * Sets <i>m</i> to the number given.
	 *
	 * @param n
	 *            the number <i>m</i> will be set to
	 */
	public void setM(final int n) {
		reset();
		m = n;
		chooseW();
	}

	/**
	 * Checks to see whether every case can be generated with the current
	 * <i>w</i> value. It also clears the list of done cases, so this should be
	 * called only when the list is empty or when one wishes to clear the list.
	 *
	 * @return whether every case can be generated
	 */
	/*
	 * public boolean checkAllCasesPossible() { //Uncomment this method if using
	 * cases when the computer goes first. int[] currentDecomposition =
	 * myDecomposition; clearDoneCases(); for (int i=0; i<myAllCases.size();
	 * i++) { chooseDecomposition(); addCase(myDecomposition, -1); }
	 *
	 * int numCasesReached = myDoneCases.size(); clearDoneCases();
	 * setDecomposition(currentDecomposition); if (myAllCases.size() ==
	 * numCasesReached) return true; return false;
	 *
	 * }
	 */

	/**
	 * Sets a recommended range for <i>m</i>.
	 *
	 */
	protected abstract void setRange();

	/**
	 * /** Sets <i>w</i> to the number given.
	 *
	 * @param s
	 *            the string <i>w</i> will be set to
	 */
	public void setW(final String s) {
		w = s;
	}
}
