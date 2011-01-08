/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation (Snippet 320)
 *     Laurent CARON (laurent.caron@gmail.com) - Make a widget from the snippet
 *******************************************************************************/
package org.mihalis.swordfish.TextAssist;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * Instances of this class are selectable user interface objects that allow the
 * user to enter and modify text. The difference with the Text widget is that
 * when the user types something, some propositions are displayed.
 * 
 * @see org.eclipse.swt.widgets.Text
 */
public class TextAssist extends Composite {

	private final Text text;
	private final Shell popup;
	private final Table table;
	private TextAssistContentProvider contentProvider;
	private int numberOfLines;

	/**
	 * Constructs a new instance of this class given its parent and a style
	 * value describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
	 * style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * 
	 * @param parent a composite control which will be the parent of the new
	 *            instance (cannot be null)
	 * @param style the style of control to construct
	 * @param contentProvider the content provider
	 * 
	 * @exception IllegalArgumentException <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 * 
	 * @see SWT#SINGLE
	 * @see SWT#MULTI
	 * @see SWT#READ_ONLY
	 * @see SWT#WRAP
	 * @see SWT#LEFT
	 * @see SWT#RIGHT
	 * @see SWT#CENTER
	 * @see SWT#PASSWORD
	 * @see SWT#SEARCH
	 * @see SWT#ICON_SEARCH
	 * @see SWT#ICON_CANCEL
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public TextAssist(final Composite parent, final int style, final TextAssistContentProvider contentProvider) {
		super(parent, style);
		this.contentProvider = contentProvider;
		this.contentProvider.setTextAssist(this);

		this.setLayout(new FillLayout());
		this.numberOfLines = 10;
		this.text = new Text(this, style);
		this.popup = new Shell(this.getDisplay(), SWT.ON_TOP);
		this.popup.setLayout(new FillLayout());
		this.table = new Table(this.popup, SWT.SINGLE);

		this.text.addListener(SWT.KeyDown, createKeyDownListener());
		this.text.addListener(SWT.Modify, createModifyListener());
		this.text.addListener(SWT.FocusOut, createFocusOutListener());

		this.table.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				TextAssist.this.text.setText(TextAssist.this.table.getSelection()[0].getText());
				TextAssist.this.popup.setVisible(false);
			}
		});
		this.table.addListener(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (event.keyCode == SWT.ESC) {
					TextAssist.this.popup.setVisible(false);
				}
			}
		});

		this.table.addListener(SWT.FocusOut, createFocusOutListener());

		getShell().addListener(SWT.Move, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				TextAssist.this.popup.setVisible(false);
			}
		});
	}

	/**
	 * @return a listener for the keydown event
	 */
	private Listener createKeyDownListener() {
		return new Listener() {
			@Override
			public void handleEvent(final Event event) {
				switch (event.keyCode) {
				case SWT.ARROW_DOWN:
					int index = (TextAssist.this.table.getSelectionIndex() + 1) % TextAssist.this.table.getItemCount();
					TextAssist.this.table.setSelection(index);
					event.doit = false;
					break;
				case SWT.ARROW_UP:
					index = TextAssist.this.table.getSelectionIndex() - 1;
					if (index < 0) {
						index = TextAssist.this.table.getItemCount() - 1;
					}
					TextAssist.this.table.setSelection(index);
					event.doit = false;
					break;
				case SWT.CR:
					if (TextAssist.this.popup.isVisible() && TextAssist.this.table.getSelectionIndex() != -1) {
						TextAssist.this.text.setText(TextAssist.this.table.getSelection()[0].getText());
						TextAssist.this.popup.setVisible(false);
					}
					break;
				case SWT.ESC:
					TextAssist.this.popup.setVisible(false);
					break;
				}
			}
		};
	}

	/**
	 * @return a listener for the modify event
	 */
	private Listener createModifyListener() {
		return new Listener() {
			@Override
			public void handleEvent(final Event event) {
				final String string = TextAssist.this.text.getText();
				if (string.length() == 0) {
					TextAssist.this.popup.setVisible(false);
					return;
				}

				List<String> values = TextAssist.this.contentProvider.getContent(string);
				if (values == null || values.isEmpty()) {
					TextAssist.this.popup.setVisible(false);
					return;
				}

				if (values.size() > TextAssist.this.numberOfLines) {
					values = values.subList(0, TextAssist.this.numberOfLines);
				}

				TextAssist.this.table.removeAll();
				final int numberOfRows = Math.min(values.size(), TextAssist.this.numberOfLines);
				for (int i = 0; i < numberOfRows; i++) {
					final TableItem tableItem = new TableItem(TextAssist.this.table, SWT.NONE);
					tableItem.setText(values.get(i));
				}

				final Point point = TextAssist.this.text.toDisplay(TextAssist.this.text.getLocation().x, TextAssist.this.text.getSize().y + TextAssist.this.text.getBorderWidth() - 3);
				int x = point.x;
				int y = point.y;

				final Rectangle displayRect = getMonitor().getClientArea();
				final Rectangle parentRect = getDisplay().map(getParent(), null, getBounds());
				TextAssist.this.popup.pack();
				final int width = TextAssist.this.popup.getBounds().width;
				final int height = TextAssist.this.popup.getBounds().height;

				if (y + height > displayRect.y + displayRect.height) {
					y = parentRect.y - height;
				}
				if (x + width > displayRect.x + displayRect.width) {
					x = displayRect.x + displayRect.width - width;
				}

				TextAssist.this.popup.setLocation(x, y);
				TextAssist.this.popup.setVisible(true);

			}
		};
	}

	/**
	 * @return a listener for the FocusOut event
	 */
	private Listener createFocusOutListener() {
		return new Listener() {
			@Override
			public void handleEvent(final Event event) {
				/* async is needed to wait until focus reaches its new Control */
				TextAssist.this.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (TextAssist.this.getDisplay().isDisposed()) {
							return;
						}
						final Control control = TextAssist.this.getDisplay().getFocusControl();
						if (control == null || control != TextAssist.this.text && control != TextAssist.this.table) {
							TextAssist.this.popup.setVisible(false);
						}
					}
				});
			}
		};
	}

	/**
	 * @return the contentProvider
	 */
	public TextAssistContentProvider getContentProvider() {
		return this.contentProvider;
	}

	/**
	 * @param contentProvider the contentProvider to set
	 */
	public void setContentProvider(final TextAssistContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}

	/**
	 * @return the numberOfLines
	 */
	public int getNumberOfLines() {
		return this.numberOfLines;
	}

	/**
	 * @param numberOfLines the numberOfLines to set
	 */
	public void setNumberOfLines(final int numberOfLines) {
		this.numberOfLines = numberOfLines;
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#addModifyListener(org.eclipse.swt.events.ModifyListener)
	 */
	public void addModifyListener(final ModifyListener listener) {
		this.text.addModifyListener(listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#addSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
	public void addSelectionListener(final SelectionListener listener) {
		this.text.addSelectionListener(listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#addVerifyListener(org.eclipse.swt.events.VerifyListener)
	 */
	public void addVerifyListener(final VerifyListener listener) {
		this.text.addVerifyListener(listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#append(java.lang.String)
	 */
	public void append(final String string) {
		this.text.append(string);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#clearSelection()
	 */
	public void clearSelection() {
		this.text.clearSelection();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		return this.text.computeSize(wHint, hHint, changed);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#computeTrim(int, int, int, int)
	 */
	@Override
	public Rectangle computeTrim(final int x, final int y, final int width, final int height) {
		return super.computeTrim(x, y, width, height);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#copy()
	 */
	public void copy() {
		this.text.copy();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#cut()
	 */
	public void cut() {
		this.text.cut();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getCaretLineNumber()
	 */
	public int getCaretLineNumber() {
		return this.text.getCaretLineNumber();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getCaretLocation()
	 */
	public Point getCaretLocation() {
		return this.text.getCaretLocation();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getCaretPosition()
	 */
	public int getCaretPosition() {
		return this.text.getCaretPosition();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getCharCount()
	 */
	public int getCharCount() {
		return this.text.getCharCount();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getDoubleClickEnabled()
	 */
	public boolean getDoubleClickEnabled() {
		return this.text.getDoubleClickEnabled();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getEchoChar()
	 */
	public char getEchoChar() {
		return this.text.getEchoChar();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getEditable()
	 */
	public boolean getEditable() {
		return this.text.getEditable();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getLineCount()
	 */
	public int getLineCount() {
		return this.text.getLineCount();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getLineDelimiter()
	 */
	public String getLineDelimiter() {
		return this.text.getLineDelimiter();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getLineHeight()
	 */
	public int getLineHeight() {
		return this.text.getLineHeight();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getMessage()
	 */
	public String getMessage() {
		return this.text.getMessage();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getOrientation()
	 */
	public int getOrientation() {
		return this.text.getOrientation();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getSelection()
	 */
	public Point getSelection() {
		return this.text.getSelection();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getSelectionCount()
	 */
	public int getSelectionCount() {
		return this.text.getSelectionCount();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getSelectionText()
	 */
	public String getSelectionText() {
		return this.text.getSelectionText();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getTabs()
	 */
	public int getTabs() {
		return this.text.getTabs();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getText()
	 */
	public String getText() {
		return this.text.getText();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getText(int, int)
	 */
	public String getText(final int start, final int end) {
		return this.text.getText(start, end);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getTextLimit()
	 */
	public int getTextLimit() {
		return this.text.getTextLimit();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getTopIndex()
	 */
	public int getTopIndex() {
		return this.text.getTopIndex();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#getTopPixel()
	 */
	public int getTopPixel() {
		return this.text.getTopPixel();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#insert(java.lang.String)
	 */
	public void insert(final String string) {
		this.text.insert(string);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#paste()
	 */
	public void paste() {
		this.text.paste();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#removeModifyListener(org.eclipse.swt.events.ModifyListener)
	 */
	public void removeModifyListener(final ModifyListener listener) {
		this.text.removeModifyListener(listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#removeSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		this.text.removeSelectionListener(listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#removeVerifyListener(org.eclipse.swt.events.VerifyListener)
	 */
	public void removeVerifyListener(final VerifyListener listener) {
		this.text.removeVerifyListener(listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#selectAll()
	 */
	public void selectAll() {
		this.text.selectAll();
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setDoubleClickEnabled(boolean)
	 */
	public void setDoubleClickEnabled(final boolean doubleClick) {
		this.text.setDoubleClickEnabled(doubleClick);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setEchoChar(char)
	 */
	public void setEchoChar(final char echo) {
		this.text.setEchoChar(echo);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setEditable(boolean)
	 */
	public void setEditable(final boolean editable) {
		this.text.setEditable(editable);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setFont(org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setFont(final Font font) {
		this.text.setFont(font);
		this.table.setFont(font);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setMessage(java.lang.String)
	 */
	public void setMessage(final String string) {
		this.text.setMessage(string);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setOrientation(int)
	 */
	public void setOrientation(final int orientation) {
		this.text.setOrientation(orientation);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setRedraw(boolean)
	 */
	@Override
	public void setRedraw(final boolean redraw) {
		this.text.setRedraw(redraw);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setSelection(int, int)
	 */
	public void setSelection(final int start, final int end) {
		this.text.setSelection(start, end);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setSelection(int)
	 */
	public void setSelection(final int start) {
		this.text.setSelection(start);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setSelection(org.eclipse.swt.graphics.Point)
	 */
	public void setSelection(final Point selection) {
		this.text.setSelection(selection);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setTabs(int)
	 */
	public void setTabs(final int tabs) {
		this.text.setTabs(tabs);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setText(java.lang.String)
	 */
	public void setText(final String text) {
		this.text.setText(text);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setTextLimit(int)
	 */
	public void setTextLimit(final int textLimit) {
		this.text.setTextLimit(textLimit);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#setTopIndex(int)
	 */
	public void setTopIndex(final int topIndex) {
		this.text.setTopIndex(topIndex);
	}

	/**
	 * @see org.eclipse.swt.widgets.Text#showSelection()
	 */
	public void showSelection() {
		this.text.showSelection();
	}

}
