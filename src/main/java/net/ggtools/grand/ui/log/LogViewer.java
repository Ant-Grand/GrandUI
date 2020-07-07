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

    /**
     * @author Christophe Labouisse
     */
    private final class LogEventFilter extends ViewerFilter {
        /**
         * Method select.
         * @param v Viewer
         * @param parentElement Object
         * @param element Object
         * @return boolean
         */
        @Override
        public boolean select(final Viewer v, final Object parentElement, final Object element) {
            return element instanceof LogEvent
                    && ((LogEvent) element).getLevel().value >= minLogLevel;
        }
    }

    /**
     * @author Christophe Labouisse
     */
    private final class LogEventRefreshListener implements LogEventListener {
        /**
         * Method logEventReceived.
         * @param event LogEvent
         * @see net.ggtools.grand.ui.log.LogEventListener#logEventReceived(LogEvent)
         */
        public void logEventReceived(final LogEvent event) {
            if (refreshEnabled && (event.getLevel().value >= minLogLevel)) {
                synchronized (refreshThread) {
                    nextEvent = event;
                    refreshThread.notify();
                }
            }
        }
    }

    /**
     * @author Christophe Labouisse
     */
    private static final class LogEventTooltipListener extends TableTooltipListener {

        /**
         * Field CI_CLASS.
         */
        private final int ciCLASS;

        /**
         * Field CI_MESSAGE.
         */
        private final int ciMESSAGE;

        /**
         * Field table.
         */
        private final Table table;

        /**
         * Constructor for LogEventTooltipListener.
         * @param table Table
         * @param ciClass int
         * @param ciMessage int
         */
        private LogEventTooltipListener(final Table table, final int ciClass, final int ciMessage) {
            super(table);
            this.table = table;
            this.ciCLASS = ciClass;
            this.ciMESSAGE = ciMessage;
        }

        /**
         * Method createTooltipContents.
         * @param tooltipParent Composite
         * @param item TableItem
         * @return Control
         * @see net.ggtools.grand.ui.log.TableTooltipListener#createTooltipContents(org.eclipse.swt.widgets.Composite,
         *      org.eclipse.swt.widgets.TableItem)
         */
        @Override
        protected Control createTooltipContents(final Composite tooltipParent, final TableItem item) {
            final Composite composite = (Composite) super
                    .createTooltipContents(tooltipParent, item);

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
            date.setText(item.getText(ciCLASS));

            final Label message = new Label(composite, SWT.READ_ONLY | SWT.WRAP);
            message.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
            message.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            message.setText(item.getText(ciMESSAGE));
            final GridData msgGridData = new GridData(GridData.GRAB_HORIZONTAL);
            msgGridData.horizontalSpan = 2;
            msgGridData.widthHint = Math.min(400, message.computeSize(SWT.DEFAULT, SWT.DEFAULT).x
                    + parentGridLayout.marginWidth);
            message.setLayoutData(msgGridData);

            return composite;
        }
    }

    /**
     * @author Christophe Labouisse
     */
    private final class LogSaver extends SelectionAdapter {
        /**
         * Method widgetSelected.
         * @param e SelectionEvent
         * @see org.eclipse.swt.events.SelectionListener#widgetSelected(SelectionEvent)
         */
        @Override
        public void widgetSelected(final SelectionEvent e) {
            if (e.widget instanceof Button) {
                final FileDialog dialog = new FileDialog(viewer.getTable().getShell(), SWT.SAVE);
                dialog.setFilterExtensions(new String[]{"*.glg", "*.log", "*"});
                final String logFileName = dialog.open();
                if (logFileName != null) {
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(logFileName))) {
                        oos.writeObject(LogEventBufferImpl.getInstance());
                    } catch (final IOException exception) {
                        throw new RuntimeException("Cannot save log to " + logFileName, exception);
                    }
                }
            }
        }
    }

    /**
     * @author Christophe Labouisse
     */
    private final class ViewerRefreshThread extends Thread {

        /**
         * Field display.
         */
        private final Display display = getDisplay();

        /**
         * Field keepRunning.
         */
        private boolean keepRunning = true;

        /**
         * Method run.
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            while (keepRunning) {
                final LogEvent myEvent;
                synchronized (this) {
                    myEvent = nextEvent;
                    nextEvent = null;
                }

                if (myEvent != null) {
                    display.syncExec(LogViewer.this::refreshViewer);
                }

                try {
                    sleep(1000L);
                    synchronized (this) {
                        wait();
                    }
                } catch (final InterruptedException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Thread interrupted", e);
                    }
                }
            }
        }

        /**
         * Method stopThread.
         */
        private void stopThread() {
            keepRunning = false;
            interrupt();
        }
    }

    /**
     * Field DEFAULT_NUM_LINES.
     * (value is {@value #DEFAULT_NUM_LINES})
     */
    private static final int DEFAULT_NUM_LINES = 10;

    /**
     * Field HEADER_EXTRA_WIDTH.
     * (value is {@value #HEADER_EXTRA_WIDTH})
     */
    private static final int HEADER_EXTRA_WIDTH = 10;

    /**
     * Logger for this class.
     */
    private static final Log LOG = LogFactory.getLog(LogViewer.class);

    /**
     * Field CI_CLASS.
     * (value is {@value #CI_CLASS})
     */
    static final int CI_CLASS = 2;

    /**
     * Field CI_DATE.
     * (value is {@value #CI_DATE})
     */
    static final int CI_DATE = 1;

    // Indexes for table columns.
    /**
     * Field CI_LEVEL.
     * (value is {@value #CI_LEVEL})
     */
    static final int CI_LEVEL = 0;

    /**
     * Field CI_MESSAGE.
     * (value is {@value #CI_MESSAGE})
     */
    static final int CI_MESSAGE = 3;

    /**
     * Field COLUMN_NAMES.
     */
    private static final String[] COLUMN_NAMES =
            new String[]{"Lvl", "Date", "Class", "Message"};

    /**
     * Field eventLevelFilter.
     */
    private LogEventFilter eventLevelFilter;

    /**
     * Field logBuffer.
     */
    private LogEventBuffer logBuffer;

    /**
     * Field minLogLevel.
     */
    private int minLogLevel = LogEvent.INFO.value;

    /**
     * Field nextEvent.
     */
    private LogEvent nextEvent = null;

    /**
     * Field refreshEnabled.
     */
    private boolean refreshEnabled = true;

    /**
     * Field refreshListener.
     */
    private LogEventRefreshListener refreshListener;

    /**
     * Field refreshThread.
     */
    private final ViewerRefreshThread refreshThread;

    /**
     * Field table.
     */
    private Table table;

    /**
     * Field viewer.
     */
    private TableViewer viewer;

    /**
     * @param parent Composite
     * @param style int
     */
    public LogViewer(final Composite parent, final int style) {
        super(parent, style);
        refreshThread = new ViewerRefreshThread();
        createContents(this);
    }

    /**
     * Method dispose.
     */
    @Override
    public final void dispose() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Disposing LogViewer");
        }
        stopRefreshThread();
        super.dispose();
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
    public final void setLogBuffer(final LogEventBuffer newLogBuffer) {
        if (logBuffer != null) {
            logBuffer.removeListener(refreshListener);
        }

        logBuffer = newLogBuffer;
        logBuffer.addListener(refreshListener);
        viewer.setInput(logBuffer.getEventList());
    }

    /**
     * @param parent Composite
     */
    private void createCommands(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Label label = new Label(composite, SWT.NONE);
        layout.numColumns++;
        label.setText("Minimum log level: ");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        final Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        layout.numColumns++;
        combo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        fillUpLevelCombo(combo);
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (e.widget instanceof Combo) {
                    final Combo selectedCombo = (Combo) e.widget;
                    minLogLevel = comboIndexToLogLevel(selectedCombo.getSelectionIndex());
                    viewer.refresh(false);
                }
            }

        });

        final Button refreshToggle = new Button(composite, SWT.CHECK);
        layout.numColumns++;
        refreshToggle.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
        refreshToggle.setText("Auto Refresh");
        refreshToggle.setSelection(refreshEnabled);

        final Button refreshButton = new Button(composite, SWT.NONE);
        layout.numColumns++;
        refreshButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
        refreshButton.setText("Refresh");
        refreshButton.setEnabled(!refreshEnabled);
        refreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                refreshViewer();
            }
        });
        refreshToggle.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (e.widget instanceof Button) {
                    final Button button = (Button) e.widget;
                    refreshEnabled = button.getSelection();
                    refreshButton.setEnabled(!refreshEnabled);
                    if (refreshEnabled) {
                        refreshViewer();
                    }
                }
            }
        });

        final Button saveButton = new Button(composite, SWT.NONE);
        layout.numColumns++;
        saveButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
        saveButton.setText("Save log");
        saveButton.addSelectionListener(new LogSaver());

        final Button clearButton = new Button(composite, SWT.NONE);
        layout.numColumns++;
        clearButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        clearButton.setText("Clear log");
        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (e.widget instanceof Button) {
                    logBuffer.clearLogEvents();
                    viewer.refresh();
                }
            }
        });
    }

    /**
     * Method createContents.
     * @param composite Composite
     */
    private void createContents(final Composite composite) {
        final GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createCommands(composite);
        createViewer(composite);
        refreshListener = new LogEventRefreshListener();
        refreshThread.start();
    }

    /**
     * Method createViewer.
     * @param parent Composite
     */
    @SuppressWarnings("unchecked")
    private void createViewer(final Composite parent) {
        viewer = new TableViewer(parent, SWT.READ_ONLY | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.HIDE_SELECTION);
        final LogLabelProvider logLabelProvider = new LogLabelProvider();
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(logLabelProvider);
        eventLevelFilter = new LogEventFilter();
        viewer.addFilter(eventLevelFilter);

        table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.pack();
        final GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = table.getHeaderHeight() * DEFAULT_NUM_LINES;
        table.setLayoutData(gridData);

        final TableTooltipListener tableListener =
                new LogEventTooltipListener(table, CI_CLASS, CI_MESSAGE);
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
            column.setMoveable(true);
        }
        gc.dispose();

        table.addDisposeListener(e -> {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Table disposed");
            }
            stopRefreshThread();
        });

        viewer.addDoubleClickListener(event -> {
            final ISelection s = event.getSelection();
            if (s instanceof IStructuredSelection) {
                final IStructuredSelection selection = (IStructuredSelection) s;
                for (final Iterator<LogEvent> iter = selection.iterator(); iter.hasNext();) {
                    final LogEvent logEvent = iter.next();
                    final LogEventDetailDialog window = new LogEventDetailDialog(getShell(),
                            logEvent);
                    window.setBlockOnOpen(false);
                    window.open();
                }
            }
        });
    }

    /**
     *
     */
    private void refreshViewer() {
        if (!table.isDisposed()) {
            viewer.refresh(false);
            table.showItem(table.getItem(table.getItemCount() - 1));
        } else {
            LOG.warn("Table is disposed");
        }
    }

    /**
     *
     */
    private void stopRefreshThread() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Stopping refresh thread");
        }
        refreshThread.stopThread();
        try {
            refreshThread.join();
        } catch (final InterruptedException e) {
            LOG.warn("Caught exception stopping refresh thread", e);
        }
    }

    /**
     * @param comboIndex int
     * @return int
     */
    protected final int comboIndexToLogLevel(final int comboIndex) {
        return comboIndex + LogEvent.TRACE.value;
    }

    /**
     * Add value to the level selection combo.
     * @param combo Combo
     */
    protected final void fillUpLevelCombo(final Combo combo) {
        combo.add(LogEvent.TRACE.name);
        combo.add(LogEvent.DEBUG.name);
        combo.add(LogEvent.INFO.name);
        combo.add(LogEvent.WARNING.name);
        combo.add(LogEvent.ERROR.name);
        combo.add(LogEvent.FATAL.name);
        combo.select(LogEvent.INFO.value - LogEvent.TRACE.value);
    }
}
