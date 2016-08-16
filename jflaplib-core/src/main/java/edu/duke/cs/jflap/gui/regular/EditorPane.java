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

package edu.duke.cs.jflap.gui.regular;

import edu.duke.cs.jflap.regular.RegularExpression;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The editor pane for a regular expression allows the user to change the
 * regular expression.
 *
 * @author Thomas Finley
 */
public class EditorPane extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new editor pane for a given regular expression.
     *
     * @param expression
     *            the regular expression
     */
    public EditorPane(RegularExpression expression) {
        // super(new BorderLayout());
        this.expression = expression;
        field.setText(expression.asString());
        field.addActionListener(event -> updateExpression());
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateExpression();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateExpression();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateExpression();
            }
        });
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;

        add(new JLabel("Edit the regular expression below:"), c);
        add(field, c);
    }

    /**
     * This is called when the regular expression should be updated to accord
     * with the field.
     */
    private void updateExpression() {
        expression.change(ref);
    }

    /** The regular expression. */
    private RegularExpression expression;

    /** The field where the expression is displayed and edited. */
    private JTextField field = new JTextField("");

    /** The reference object. */
    private Reference<?> ref = new WeakReference<Object>(null) {
        @Override
        public Object get() {
            return field.getText();
        }
    };
}
