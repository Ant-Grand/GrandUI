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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author Christophe Labouisse
 */
public class LogViewer extends Composite {

    private TableViewer viewer;

    private LogEventFilter eventLevelFilter;

    private LogEventRefreshListener refreshListener;

    private int minLogLevel = LogEvent.INFO.value;

    private LogEventBuffer logBuffer;

    private final class LogSaver extends SelectionAdapter {
        public void widgetSelected(SelectionEvent e) {
            if (e.widget instanceof Button) {
                final Button button = (Button) e.widget;
                final FileDialog dialog = new FileDialog(viewer.getTable().getShell(),SWT.SAVE);
                dialog.setFilterExtensions(new String [] {"*.glg","*.log","*"});
                final String logFileName = dialog.open();
                if (logFileName != null) {
                    ObjectOutputStream oos = null;
                    try {
                        oos = new ObjectOutputStream(new FileOutputStream(logFileName));
                        oos.writeObject(LogEventBufferImpl.getInstance());
                    } catch (IOException exception) {
                        throw new RuntimeException("Cannot save log to "+logFileName,exception);
                    }
                    finally {
                        if (oos != null) try {
                            oos.close();
                        } catch (IOException exception) {
                            throw new RuntimeException("Cannot close "+logFileName,exception);
                        }
                    }
                }
            }

        }
    }

    private final class LogEventRefreshListener implements LogEventListener {
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

    private final class LogEventFilter extends ViewerFilter {
        public boolean select(Viewer v, Object parentElement, Object element) {
            if (element instanceof LogEvent) {
                final LogEvent event = (LogEvent) element;
                if (event.getLevel().value >= minLogLevel) return true;
            }
            return false;
        }
    }

    /**
     * @param parent
     * @param style
     */
    public LogViewer(Composite parent, int style) {
        super(parent, style);
        createContents(this);
    }

    private void createContents(final Composite composite) {
        final GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createCommands(composite);
        createViewer(composite);
        refreshListener = new LogEventRefreshListener();
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

    private void createViewer(final Composite parent) {
        viewer = new TableViewer(parent, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.HIDE_SELECTION);
        final LogLabelProvider logLabelProvider = new LogLabelProvider();
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(logLabelProvider);
        eventLevelFilter = new LogEventFilter();
        viewer.addFilter(eventLevelFilter);

        Table table = viewer.getTable();
        for (int i = 0; i < LogLabelProvider.COLUMN_NAMES.length; i++) {
            final String header = LogLabelProvider.COLUMN_NAMES[i];
            final TableColumn column = new TableColumn(table, SWT.LEFT);
            column.setText(header);
            column.setWidth(100);
        }

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    /**
     * @return Returns the logBuffer.
     */
    public final LogEventBuffer getLogBuffer() {
        return logBuffer;
    }
    
    /**
     * @param newLogBuffer The logBuffer to set.
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
     * @param comboIndex
     * @return
     */
    protected int comboIndexToLogLevel(int comboIndex) {
        return comboIndex + LogEvent.INFO.value;
    }
}
