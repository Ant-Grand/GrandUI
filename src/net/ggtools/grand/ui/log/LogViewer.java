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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Christophe Labouisse
 */
public class LogViewer extends Composite {
    private static final int DEFAULT_NUM_LINES = 10;

    private static final int HEADER_EXTRA_WIDTH = 10;

    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(LogViewer.class);

    static final String[] COLUMN_NAMES = new String[]{"Lvl", "Date", "Class", "Message"};

    // Indexes for table columns.
    static final int CI_LEVEL = 0;

    static final int CI_DATE = 1;

    static final int CI_CLASS = 2;

    static final int CI_MESSAGE = 3;

    private static final class LogEventTooltipListener extends TableTooltipListener {
        private final Table table;

        private final int CI_CLASS;

        private final int CI_MESSAGE;

        private LogEventTooltipListener(Table table, int CI_CLASS, int CI_MESSAGE) {
            super(table);
            this.table = table;
            this.CI_CLASS = CI_CLASS;
            this.CI_MESSAGE = CI_MESSAGE;
        }

        /*
         * (non-Javadoc)
         * @see net.ggtools.grand.ui.log.LogViewer.TableTooltipListener#createTooltipContents(org.eclipse.swt.widgets.Composite,
         *      org.eclipse.swt.widgets.TableItem)
         */
        protected Control createTooltipContents(Composite tooltipParent, TableItem item) {
            final Composite composite = (Composite) super.createTooltipContents(tooltipParent,
                    item);

            final GridLayout parentGridLayout = ((GridLayout) composite.getLayout());
            parentGridLayout.numColumns = 2;

            final Display display = table.getShell().getDisplay();

            final Label icon = new Label(composite, SWT.NONE);
            icon.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
            icon.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            icon.setImage(item.getImage());

            final Label date = new Label(composite, SWT.NO_BACKGROUND);
            date.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
            date.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            date.setText(item.getText(CI_CLASS));

            final Label message = new Label(composite, SWT.READ_ONLY | SWT.WRAP);
            message.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
            message.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            message.setText(item.getText(CI_MESSAGE));
            final GridData msgGridData = new GridData(GridData.GRAB_HORIZONTAL);
            msgGridData.horizontalSpan = 2;
            msgGridData.widthHint = Math.min(400,
                    message.computeSize(SWT.DEFAULT, SWT.DEFAULT).x
                            + parentGridLayout.marginWidth);
            message.setLayoutData(msgGridData);

            return composite;
        }
    }

    private final class LogEventFilter extends ViewerFilter {
        /**
         * Logger for this class
         */
        private final Log log = LogFactory.getLog(LogEventFilter.class);

        public boolean select(Viewer v, Object parentElement, Object element) {
            if (element instanceof LogEvent) {
                final LogEvent event = (LogEvent) element;
                if (event.getLevel().value >= minLogLevel) return true;
            }
            return false;
        }
    }

    private final class LogEventRefreshListener implements LogEventListener {
        /**
         * Logger for this class
         */
        private final Log log = LogFactory.getLog(LogEventRefreshListener.class);

        private final Display display = LogViewer.this.getDisplay();

        public void logEventReceived(final LogEvent event) {
            if (event.getLevel().value >= minLogLevel) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        viewer.refresh(false);
                        viewer.reveal(event);
                    }
                });
            }
        }
    }

    private final class LogSaver extends SelectionAdapter {
        /**
         * Logger for this class
         */
        private final Log log = LogFactory.getLog(LogSaver.class);

        public void widgetSelected(SelectionEvent e) {
            if (e.widget instanceof Button) {
                final Button button = (Button) e.widget;
                final FileDialog dialog = new FileDialog(viewer.getTable().getShell(), SWT.SAVE);
                dialog.setFilterExtensions(new String[]{"*.glg", "*.log", "*"});
                final String logFileName = dialog.open();
                if (logFileName != null) {
                    ObjectOutputStream oos = null;
                    try {
                        oos = new ObjectOutputStream(new FileOutputStream(logFileName));
                        oos.writeObject(LogEventBufferImpl.getInstance());
                    } catch (IOException exception) {
                        throw new RuntimeException("Cannot save log to " + logFileName, exception);
                    } finally {
                        if (oos != null) try {
                            oos.close();
                        } catch (IOException exception) {
                            throw new RuntimeException("Cannot close " + logFileName, exception);
                        }
                    }
                }
            }
        }
    }

    private LogEventFilter eventLevelFilter;

    private LogEventBuffer logBuffer;

    private int minLogLevel = LogEvent.INFO.value;

    private LogEventRefreshListener refreshListener;

    private TableViewer viewer;

    /**
     * @param parent
     * @param style
     */
    public LogViewer(Composite parent, int style) {
        super(parent, style);
        createContents(this);
    }

    /**
     * @return Returns the logBuffer.
     */
    public final LogEventBuffer getLogBuffer() {
        return logBuffer;
    }

    /**
     * @param newLogBuffer
     *            The logBuffer to set.
     */
    public final void setLogBuffer(LogEventBuffer newLogBuffer) {
        if (logBuffer != null) {
            logBuffer.removeListener(refreshListener);
        }

        logBuffer = newLogBuffer;
        logBuffer.addListener(refreshListener);
        viewer.setInput(logBuffer.getEventList());
    }

    /**
     * @param composite
     */
    private void createCommands(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(composite, SWT.NONE);
        layout.numColumns++;
        label.setText("Minimum log level: ");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        layout.numColumns++;
        combo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        fillUpLevelCombo(combo);
        combo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget instanceof Combo) {
                    Combo selectedCombo = (Combo) e.widget;
                    minLogLevel = comboIndexToLogLevel(selectedCombo.getSelectionIndex());
                    viewer.refresh(false);
                }
            }

        });

        Button saveButton = new Button(composite, SWT.NONE);
        layout.numColumns++;
        saveButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
        saveButton.setText("Save log");
        saveButton.addSelectionListener(new LogSaver());

        Button clearButton = new Button(composite, SWT.NONE);
        layout.numColumns++;
        clearButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        clearButton.setText("Clear log");
        clearButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget instanceof Button) {
                    Button button = (Button) e.widget;
                    logBuffer.clearLogEvents();
                    viewer.refresh();
                }
            }
        });
    }

    private void createContents(final Composite composite) {
        final GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createCommands(composite);
        createViewer(composite);
        refreshListener = new LogEventRefreshListener();
    }

    private void createViewer(final Composite parent) {
        viewer = new TableViewer(parent, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.HIDE_SELECTION);
        final LogLabelProvider logLabelProvider = new LogLabelProvider();
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(logLabelProvider);
        eventLevelFilter = new LogEventFilter();
        viewer.addFilter(eventLevelFilter);

        final Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        final GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = table.getHeaderHeight() * DEFAULT_NUM_LINES;
        table.setLayoutData(gridData);

        final TableTooltipListener tableListener = new LogEventTooltipListener(table, CI_CLASS, CI_MESSAGE);
        tableListener.activateTooltips();

        final GC gc = new GC(table);
        gc.setFont(table.getFont());
        for (int columnIndex = 0; columnIndex < COLUMN_NAMES.length; columnIndex++) {
            final String header = COLUMN_NAMES[columnIndex];
            final TableColumn column = new TableColumn(table, SWT.LEFT);
            int columnWidth;
            switch (columnIndex) {
            case CI_DATE:
                columnWidth = gc.stringExtent(new Date().toString()).x;
                break;

            case CI_CLASS:
                // Approximately 20 chars
                columnWidth = gc.stringExtent("em").x * 7;
                break;

            case CI_MESSAGE:
                // Approximately 60 chars
                columnWidth = gc.stringExtent("em").x * 20;
                break;

            default:
                // Default behavior is header width.
                columnWidth = gc.stringExtent(header).x;
                break;
            }

            columnWidth += HEADER_EXTRA_WIDTH;
            column.setText(header);
            column.setWidth(columnWidth);
        }
        gc.dispose();

        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                final ISelection s = event.getSelection();
                if (s instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection) s;
                    for (Iterator iter = selection.iterator(); iter.hasNext();) {
                        final LogEvent logEvent = (LogEvent) iter.next();
                        final LogEventDetailDialog window = new LogEventDetailDialog(getShell(),
                                logEvent);
                        window.setBlockOnOpen(false);
                        window.open();
                    }
                }
            }
        });
    }

    /**
     * @param comboIndex
     * @return
     */
    protected int comboIndexToLogLevel(int comboIndex) {
        return comboIndex + LogEvent.INFO.value;
    }

    /**
     * Add value to the level selection combo.
     * @param combo
     */
    protected void fillUpLevelCombo(Combo combo) {
        combo.add(LogEvent.INFO.name);
        combo.add(LogEvent.WARNING.name);
        combo.add(LogEvent.ERROR.name);
        combo.add(LogEvent.FATAL.name);
        combo.select(0);
    }
}
