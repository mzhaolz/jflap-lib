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

package edu.duke.cs.jflap.regular;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This object encapsulates a regular expression.
 *
 * @author Thomas Finley
 */
public class RegularExpression implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 3838500080597257761L;

	/** The string of the regular expression. */
	private String string;

	/** The set of objects that are regular expressions. */
	private transient Set<ExpressionChangeListener> listeners = new HashSet<>();

	/** The referrence object that holds the change. */
	private Reference<?> reference = null;

	/**
	 * Instantiates a blank regular expression.
	 */
	public RegularExpression() {
		this("");
	}

	/**
	 * Instantiates a regular expression from another regular expression.
	 *
	 * @param expression
	 *            the expression to copy
	 */
	public RegularExpression(final RegularExpression expression) {
		this(expression.asString());
	}

	/**
	 * Instantiates a regular expression from the given string.
	 *
	 * @param string
	 *            the string to treat as a regular expression
	 */
	public RegularExpression(final String string) {
		change(string);
	}

	/**
	 * Adds a listener to this object.
	 *
	 * @param listener
	 *            the new listener
	 */
	public void addExpressionListener(final ExpressionChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Checks if the parentheses are balanced in a string.
	 *
	 * @param string
	 *            the string to check
	 * @return if the parentheses are balanced
	 */
	private boolean areParenthesesBalanced(final String string) {
		int count = 0;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == '(') {
				count++;
			} else if (string.charAt(i) == ')') {
				count--;
			}
			if (count < 0) {
				return false;
			}
		}
		return count == 0;
	}

	/**
	 * Returns the error checked string representation.
	 *
	 * @return the string version of the exception
	 * @throws UnsupportedOperationException
	 *             if the expression is not properly formed
	 */
	public String asCheckedString() {
		string = asString();
		if (string.length() == 0) {
			throw new UnsupportedOperationException("The expression must be nonempty.");
		}
		if (!areParenthesesBalanced(string)) {
			throw new UnsupportedOperationException("The parentheses are unbalanced!");
		}
		switch (string.charAt(0)) {
		case ')':
		case '+':
		case '*':
			throw new UnsupportedOperationException("Operators are poorly formatted.");
		}
		for (int i = 1; i < string.length(); i++) {
			final char c = string.charAt(i);
			char p = string.charAt(i - 1);
			switch (c) {
			case '+':
				if (i == string.length() - 1) {
					throw new UnsupportedOperationException("Operators are poorly formatted.");
				}
			case ')':
			case '*':
				if (p == '(' || p == '+') {
					throw new UnsupportedOperationException("Operators are poorly formatted.");
				}
				break;
			case '!':
				if (p != '(' && p != '+') {
					throw new UnsupportedOperationException("Lambda character must not cat with anything else.");
				}
				if (i == string.length() - 1) {
					break;
				}
				p = string.charAt(i + 1);
				if (p != ')' && p != '+' && p != '*') {
					throw new UnsupportedOperationException("Lambda character must not cat with anything else.");
				}
				break;
			}
		}
		return string;
	}

	/**
	 * Returns the expression as a string.
	 *
	 * @return the expression as a string
	 */
	public String asString() {
		try {
			final String old = (String) reference.get();
			if (old.equals(string)) {
				return string;
			}
			string = old;
			reference = null;
			distributeChangeEvent(old);
		} catch (final NullPointerException e) {
		}
		return string;
	}

	/**
	 * Changes the regular expression based on a reference. The reference will
	 * only be resolved later, when the information is requested.
	 *
	 * @param ref
	 *            the reference to a string to change the regular expression to
	 */
	public void change(final Reference<?> ref) {
		reference = ref;
	}

	/**
	 * Changes the regular expression.
	 *
	 * @param string
	 *            the string to change the regular expression to
	 */
	public void change(final String string) {
		try {
			if (this.string.equals(string)) {
				return;
			}
		} catch (final NullPointerException e) {
		}
		final String old = this.string;
		this.string = string;
		distributeChangeEvent(old);
	}

	/**
	 * Fires an event that the expression has been changed.
	 *
	 * @param old
	 *            the old string representation of the regular expression
	 */
	protected void distributeChangeEvent(final String old) {
		final ExpressionChangeEvent e = new ExpressionChangeEvent(this, old);
		final Iterator<ExpressionChangeListener> it = listeners.iterator();
		while (it.hasNext()) {
			final ExpressionChangeListener l = it.next();
			l.expressionChanged(e);
		}
	}

	/**
	 * This handles deserialization so that the listener sets are reset to avoid
	 * null pointer exceptions when one tries to add listeners to the object.
	 *
	 * @param in
	 *            the input stream for the object
	 */
	private void readObject(final java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		in.defaultReadObject();
		// Reset those transient listener guys.
		listeners = new HashSet<>();
	}

	/**
	 * Removes a listener from this object.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeExpressionListener(final ExpressionChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Returns the string representiation of the object.
	 *
	 * @return the regular expression as string
	 */
	@Override
	public String toString() {
		return asString();
	}

	/**
	 * This handles serialization so that the reference, if it exists, is
	 * resolved to a string rather than being stored itself.
	 *
	 * @param out
	 *            the object output stream
	 */
	private void writeObject(final java.io.ObjectOutputStream out) throws java.io.IOException {
		// Force the reference to be resolved, and invalidated.
		asString();
		// Now we may call the defauult writer.
		out.defaultWriteObject();
	}
}
