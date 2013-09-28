// $Id$
/*
 * ====================================================================
 * Copyright (c) 2002-2004, Christophe Labouisse All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ggtools.grand.ui.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog to display the details of a log event.
 *
 * @author Christophe Labouisse
 */
public class LogEventDetailDialog extends Dialog {

    /**
     * Field ICONS_FOR_LEVELS.
     */
    private static final int[] ICONS_FOR_LEVELS = {SWT.ICON_INFORMATION, SWT.ICON_INFORMATION,
            SWT.ICON_INFORMATION, SWT.ICON_INFORMATION, SWT.ICON_WARNING, SWT.ICON_ERROR,
            SWT.ICON_ERROR};

    /**
     * Logger for this class.
     */
    private static final Log LOG = LogFactory.getLog(LogEventDetailDialog.class);

    /**
     * Field details.
     */
    private Control details;

    /**
     * The Details button.
     */
    private Button detailsButton;

    /**
     * Field display.
     */
    private Display display;

    /**
     * Field event.
     */
    private final LogEvent event;

    /**
     * Creates a new LogEventDetailDialog.
     *
     * @param parentShell Shell
     * @param event LogEvent
     */
    public LogEventDetailDialog(final Shell parentShell, final LogEvent event) {
        super(parentShell);
        setShellStyle(SWT.SHELL_TRIM);
        this.event = event;
    }

    /**
     * Creates a key/value widget pair with the value widget spanning on 1
     * column.
     *
     * @param composite Composite
     * @param key String
     * @param value String
     */
    private void addKeyValue(final Composite composite, final String key,
            final String value) {
        addKeyValue(composite, key, value, 1);
    }

    /**
     * Add a key/value widget pair.
     *
     * @param composite Composite
     * @param key String
     * @param value String
     * @param valueColumnSpan int
     */
    private void addKeyValue(final Composite composite, final String key,
            final String value, final int valueColumnSpan) {
        if (value == null) {
            LOG.warn("Value is null, skipping");
            return;
        }

        final Label header = new Label(composite, SWT.BOLD);
        header.setText(key);
        header.setFont(JFaceResources.getDefaultFont());

        GridData layoutData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        header.setLayoutData(layoutData);

        final Label content = new Label(composite, SWT.WRAP);
        content.setText(value);
        content.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        content.setFont(JFaceResources.getTextFont());

        final GC gc = new GC(content);
        layoutData = new GridData(GridData.GRAB_HORIZONTAL);
        layoutData.widthHint = Math.min(gc.stringExtent("em").x * 30, gc.stringExtent(value).x);
        layoutData.horizontalSpan = valueColumnSpan;
        content.setLayoutData(layoutData);
        gc.dispose();
    }

    /**
     * Creates the detail widget for an exception. The widget will be made of a
     * {@link ScrolledComposite}containing a read only text control.
     *
     * @param exception Throwable
     * @return the detail control
     */
    private Control createDetailWidget(final Throwable exception) {
        final ScrolledComposite textComposite = new ScrolledComposite((Composite) getContents(),
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        textComposite.setExpandHorizontal(true);
        textComposite.setExpandVertical(true);
        final GridData layoutData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL);
        layoutData.horizontalSpan = 2;
        textComposite.setLayoutData(layoutData);

        final Text exceptionStack = new Text(textComposite, SWT.READ_ONLY | SWT.MULTI);
        exceptionStack.setFont(JFaceResources.getTextFont());
        textComposite.setContent(exceptionStack);

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        exceptionStack.setText(stringWriter.getBuffer().toString());
        printWriter.close();

        // Adjust composite size and layout data.
        layoutData.heightHint = Math.min(200,
                exceptionStack.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        textComposite.setMinSize(exceptionStack.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        return textComposite;
    }

    /**
     * Displays or hide the stack trace of the potential exception.
     */
    private void toggleExceptionDetail() {
        final Shell shell = getShell();
        final Point windowSize = shell.getSize();
        final Point minimumWindowSize = shell.getMinimumSize();
        final Point oldSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        if (details != null) {
            details.dispose();
            details = null;
            detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
        } else {
            details = createDetailWidget(event.getException());
            detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
        }

        final Point newSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        shell.setMinimumSize(new Point(minimumWindowSize.x, minimumWindowSize.y
                + (newSize.y - oldSize.y)));
        shell.setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
    }

    /**
     * Method buttonPressed.
     * @param buttonId int
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected final void buttonPressed(final int buttonId) {
        if (IDialogConstants.DETAILS_ID == buttonId) {
            toggleExceptionDetail();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    /**
     * Method configureShell.
     * @param newShell Shell
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected final void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Log event details");
        display = newShell.getDisplay();
    }

    /**
     * Method createButtonsForButtonBar.
     * @param parent Composite
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected final void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        if (event.getException() != null) {
            detailsButton = createButton(parent, IDialogConstants.DETAILS_ID,
                    IDialogConstants.SHOW_DETAILS_LABEL, false);
        }
    }

    /**
     * Method createContents.
     * @param parent Composite
     * @return Control
     * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected final Control createContents(final Composite parent) {
        final Control contents = super.createContents(parent);
        getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return contents;
    }

    /**
     * Method createDialogArea.
     * @param parent Composite
     * @return Control
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected final Control createDialogArea(final Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        final GridData compositeLayoutData = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(compositeLayoutData);

        final GridLayout gridLayout = new GridLayout(3, false);
        composite.setLayout(gridLayout);
        final Label icon = new Label(composite, SWT.NONE);
        final GridData layoutData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        layoutData.verticalSpan = 3;
        icon.setLayoutData(layoutData);
        icon.setImage(display.getSystemImage(ICONS_FOR_LEVELS[event.getLevel().value]));
        addKeyValue(composite, "Level:", event.getLevel().name);
        addKeyValue(composite, "Class:", event.getClass().getName());
        addKeyValue(composite, "Date:", new Date(event.getTime()).toString());
        addKeyValue(composite, "Message:", event.getMessage().toString(), 2);

        final Throwable exception = event.getException();
        if (exception != null) {
            addKeyValue(composite, "Exception:", exception.getClass().getName(), 2);
            addKeyValue(composite, "", exception.getMessage(), 2);
        }

        compositeLayoutData.minimumHeight = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT, false).y;
        return composite;
    }
}
