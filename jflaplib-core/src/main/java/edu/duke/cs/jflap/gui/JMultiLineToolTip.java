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

// I lifted this code directly from
// http://www.codeguru.com/java/articles/122.shtml.
// JMultiLineToolTip.java
package edu.duke.cs.jflap.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

/**
 * @author Zafir Anjum
 */
public class JMultiLineToolTip extends JToolTip {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	String tipText;

	JComponent component;

	protected int columns = 0;

	protected int fixedwidth = 0;

	public JMultiLineToolTip() {
		updateUI();
	}

	public int getColumns() {
		return columns;
	}

	public int getFixedWidth() {
		return fixedwidth;
	}

	public void setColumns(final int columns) {
		this.columns = columns;
		fixedwidth = 0;
	}

	public void setFixedWidth(final int width) {
		fixedwidth = width;
		columns = 0;
	}

	@Override
	public void updateUI() {
		setUI(MultiLineToolTipUI.createUI(this));
	}
}

class MultiLineToolTipUI extends BasicToolTipUI {
	static MultiLineToolTipUI sharedInstance = new MultiLineToolTipUI();

	static JToolTip tip;

	private static JTextArea textArea;

	public static ComponentUI createUI(final JComponent c) {
		return sharedInstance;
	}

	Font smallFont;

	protected CellRendererPane rendererPane;

	public MultiLineToolTipUI() {
		super();
	}

	@Override
	public Dimension getMaximumSize(final JComponent c) {
		return getPreferredSize(c);
	}

	@Override
	public Dimension getMinimumSize(final JComponent c) {
		return getPreferredSize(c);
	}

	@Override
	public Dimension getPreferredSize(final JComponent c) {
		final String tipText = ((JToolTip) c).getTipText();
		if (tipText == null) {
			return new Dimension(0, 0);
		}
		textArea = new JTextArea(tipText);
		rendererPane.removeAll();
		rendererPane.add(textArea);
		textArea.setWrapStyleWord(true);
		final int width = ((JMultiLineToolTip) c).getFixedWidth();
		final int columns = ((JMultiLineToolTip) c).getColumns();

		if (columns > 0) {
			textArea.setColumns(columns);
			textArea.setSize(0, 0);
			textArea.setLineWrap(true);
			textArea.setSize(textArea.getPreferredSize());
		} else if (width > 0) {
			textArea.setLineWrap(true);
			final Dimension d = textArea.getPreferredSize();
			d.width = width;
			d.height++;
			textArea.setSize(d);
		} else {
			textArea.setLineWrap(false);
		}

		final Dimension dim = textArea.getPreferredSize();

		dim.height += 1;
		dim.width += 1;
		return dim;
	}

	@Override
	public void installUI(final JComponent c) {
		super.installUI(c);
		tip = (JToolTip) c;
		rendererPane = new CellRendererPane();
		c.add(rendererPane);
	}

	@Override
	public void paint(final Graphics g, final JComponent c) {
		final Dimension size = c.getSize();

		if (textArea == null) {
			return;
		}

		textArea.setBackground(c.getBackground());
		rendererPane.paintComponent(g, textArea, c, 1, 1, size.width - 1, size.height - 1, true);
	}

	@Override
	public void uninstallUI(final JComponent c) {
		super.uninstallUI(c);

		c.remove(rendererPane);
		rendererPane = null;
	}
}
