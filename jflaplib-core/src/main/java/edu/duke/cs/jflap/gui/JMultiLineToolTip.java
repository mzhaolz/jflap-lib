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

    public JMultiLineToolTip() {
        updateUI();
    }

    @Override
    public void updateUI() {
        setUI(MultiLineToolTipUI.createUI(this));
    }

    public void setColumns(int columns) {
        this.columns = columns;
        fixedwidth = 0;
    }

    public int getColumns() {
        return columns;
    }

    public void setFixedWidth(int width) {
        fixedwidth = width;
        columns = 0;
    }

    public int getFixedWidth() {
        return fixedwidth;
    }

    protected int columns = 0;

    protected int fixedwidth = 0;
}

class MultiLineToolTipUI extends BasicToolTipUI {
    static MultiLineToolTipUI sharedInstance = new MultiLineToolTipUI();

    Font smallFont;

    static JToolTip tip;

    protected CellRendererPane rendererPane;

    private static JTextArea textArea;

    public static ComponentUI createUI(JComponent c) {
        return sharedInstance;
    }

    public MultiLineToolTipUI() {
        super();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        tip = (JToolTip) c;
        rendererPane = new CellRendererPane();
        c.add(rendererPane);
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);

        c.remove(rendererPane);
        rendererPane = null;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Dimension size = c.getSize();

        if (textArea == null) {
            return;
        }

        textArea.setBackground(c.getBackground());
        rendererPane.paintComponent(g, textArea, c, 1, 1, size.width - 1, size.height - 1, true);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        String tipText = ((JToolTip) c).getTipText();
        if (tipText == null) {
            return new Dimension(0, 0);
        }
        textArea = new JTextArea(tipText);
        rendererPane.removeAll();
        rendererPane.add(textArea);
        textArea.setWrapStyleWord(true);
        int width = ((JMultiLineToolTip) c).getFixedWidth();
        int columns = ((JMultiLineToolTip) c).getColumns();

        if (columns > 0) {
            textArea.setColumns(columns);
            textArea.setSize(0, 0);
            textArea.setLineWrap(true);
            textArea.setSize(textArea.getPreferredSize());
        } else if (width > 0) {
            textArea.setLineWrap(true);
            Dimension d = textArea.getPreferredSize();
            d.width = width;
            d.height++;
            textArea.setSize(d);
        } else {
            textArea.setLineWrap(false);
        }

        Dimension dim = textArea.getPreferredSize();

        dim.height += 1;
        dim.width += 1;
        return dim;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
    }
}
