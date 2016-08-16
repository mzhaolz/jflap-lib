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

package edu.duke.cs.jflap.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * The <TT>WebFrame</TT> class provides a simple method for poping up a
 * miniature web browser that has only back, forward, and home buttons, and no
 * URL entry form.
 *
 * @author Justin Cross
 * @author Thomas Finley
 */

/*
 * This code was modified considerably from some freely distributed code kindly
 * provided by Justin Cross during the spring 2001 semester of the CPS 108
 * class.
 */

public class WebFrame extends JFrame {
	/**
	 * An action to browse back.
	 */
	private class BackAction implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			goBack();
		}
	}

	/**
	 * An action to browse forward.
	 */
	private class ForwardAction implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			goForward();
		}
	}

	/**
	 * An action to browse back to the start.
	 */
	private class HomeAction implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			goHome();
		}
	}

	/**
	 * This listener listenens for hyperlink clicks, and updates the frame to
	 * the new contents.
	 */
	public class Hyperactive implements HyperlinkListener {
		@Override
		public void hyperlinkUpdate(final HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				final JEditorPane pane = (JEditorPane) e.getSource();
				if (e instanceof HTMLFrameHyperlinkEvent) {
					final HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
					final HTMLDocument doc = (HTMLDocument) pane.getDocument();
					doc.processHTMLFrameHyperlinkEvent(evt);
				} else {
					try {
						goNew(e.getURL().toString());
					} catch (final Throwable t) {
						t.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The JEditorPane display. */
	private final JEditorPane myBrowserDisplay = new JEditorPane();

	/** The vector that holds old addresses. */
	private final ArrayList<String> myURLHistory = new ArrayList<>();

	/** Our current position in the vector of addresses. */
	private int myCurrentPosition = -1;

	/** The back button. */
	private JButton myBackButton;

	/** The forward button. */
	private JButton myForwardButton;

	/** The start button. */
	private JButton myStartButton;

	/**
	 * This constructs a new <TT>WebFrame</TT> that initializes its display to
	 * the location shown.
	 *
	 * @param myHtmlFile
	 *            the URL to load, either in the form of a web page (starting
	 *            with `http') or some sort of file (starting, I suppose,
	 *            without the `http').
	 */
	public WebFrame(final String myHtmlFile) {
		setTitle("Help Browser");
		final JPanel mainpanel = new JPanel(new BorderLayout());

		final Hyperactive hyper = new Hyperactive();
		myBrowserDisplay.setEditable(false);
		myBrowserDisplay.addHyperlinkListener(hyper);
		final JScrollPane htmlscrollpane = new JScrollPane(myBrowserDisplay);
		mainpanel.add(htmlscrollpane, BorderLayout.CENTER);
		mainpanel.add(getToolBar(), BorderLayout.NORTH);

		String url = myHtmlFile;
		if (!myHtmlFile.startsWith("http://")) {
			final URL u = this.getClass().getResource(myHtmlFile);
			url = u == null ? "" : u.toString();
		}
		setContentPane(mainpanel);
		pack();
		setSize(600, 700);
		setLocation(50, 50);
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		goNew(url);
	}

	/**
	 * Returns the toolbar for browsing this help display.
	 *
	 * @return the toolbar for browsing this help display
	 */
	private JToolBar getToolBar() {
		final JToolBar toReturn = new JToolBar();
		toReturn.setFloatable(false);
		myBackButton = makeButton("Back", "left.gif", new BackAction(), null);
		myForwardButton = makeButton("Forward", "right.gif", new ForwardAction(), null);
		myStartButton = makeButton("Main Index", "start.gif", new HomeAction(), null);
		toReturn.add(myBackButton);
		toReturn.add(myForwardButton);
		toReturn.add(myStartButton);
		return toReturn;
	}

	/**
	 * Goes to the previous address in the history.
	 */
	private void goBack() {
		try {
			myCurrentPosition--;
			final String url = myURLHistory.get(myCurrentPosition);
			setDisplay(url);
		} catch (final Throwable e) {
			myCurrentPosition++;
		}
	}

	/**
	 * Goes to the next address in the history.
	 */
	private void goForward() {
		try {
			myCurrentPosition++;
			final String url = myURLHistory.get(myCurrentPosition);
			setDisplay(url);
		} catch (final Throwable e) {
			myCurrentPosition--;
		}
	}

	/**
	 * Goes to the start address.
	 */
	private void goHome() {
		final int oldIndex = myCurrentPosition;
		try {
			myCurrentPosition = 0;
			final String url = myURLHistory.get(myCurrentPosition);
			setDisplay(url);
		} catch (final Throwable e) {
			myCurrentPosition = oldIndex;
		}
	}

	/**
	 * Go to a completely new page, clearing all visited history past this
	 * point.
	 *
	 * @param url
	 *            the new url to go to
	 */
	private void goNew(final String url) {
		myCurrentPosition++;
		try {
			while (true) {
				myURLHistory.remove(myCurrentPosition);
			}
		} catch (final Throwable e) {

		}
		myURLHistory.add(url);
		setDisplay(url);
	}

	/**
	 * Goes to a particular page.
	 *
	 * @param url
	 *            the url to go to
	 */
	public void gotoURL(String url) {
		if (!url.startsWith("http://")) {
			final URL u = this.getClass().getResource(url);
			url = u == null ? "" : u.toString();
		}
		goNew(url);
	}

	/**
	 * Makes a web browser button with the specified attributes.
	 *
	 * @param label
	 *            the label on the button
	 * @param iconName
	 *            the icon name for the button in the web browser icon directory
	 * @param listener
	 *            the action listener for the button
	 * @param tooltip
	 *            the tool tip for the button
	 */
	private JButton makeButton(final String label, final String iconName, final ActionListener listener,
			final String tooltip) {
		final ImageIcon icon = new ImageIcon(getClass().getResource("/ICON/web/" + iconName));
		final JButton button = new JButton(label, icon);
		button.addActionListener(listener);
		button.setToolTipText(tooltip);
		return button;
	}

	/**
	 * Sets the display of the browser to the URL.
	 *
	 * @param url
	 *            the name of the url
	 */
	private void setDisplay(final String url) {
		try {
			myBrowserDisplay.setPage(url);
		} catch (final IOException e) {
			// Display an alert to that effect.
			System.err.println(e);
			JOptionPane.showMessageDialog(this, "Could not access URL " + url + "!", "Web Error",
					JOptionPane.ERROR_MESSAGE);
			myURLHistory.remove(myCurrentPosition);
			myCurrentPosition--;
		}
		setEnabledStates();
	}

	/**
	 * Sets the enabled states of the browsing buttons.
	 */
	private void setEnabledStates() {
		myBackButton.setEnabled(myCurrentPosition != 0);
		myStartButton.setEnabled(myCurrentPosition != 0);
		myForwardButton.setEnabled(myCurrentPosition != myURLHistory.size() - 1);
	}
}
