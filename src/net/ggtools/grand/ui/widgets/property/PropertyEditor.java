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
package net.ggtools.grand.ui.widgets.property;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import net.ggtools.grand.ui.widgets.ExceptionDialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Christophe Labouisse
 */
public class PropertyEditor {

    private static final class PropertyListContentProvider implements IStructuredContentProvider,
            PropertyChangedListener {
        /**
         * Logger for this class
         */
        private static final Log log = LogFactory.getLog(PropertyListContentProvider.class);

        private PropertyList currentPropertyList;

        private TableViewer tableViewer;

        public void clearedProperties(Object fillerParameter) {
            tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }

        public void dispose() {
            if (currentPropertyList != null) {
                currentPropertyList.removePropertyChangedListener(this);
            }
        }

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof PropertyList) {
                PropertyList pList = (PropertyList) inputElement;
                return pList.toArray();
            }
            else {
                return null;
            }
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            this.tableViewer = (TableViewer) viewer;

            if (oldInput != null) {
                ((PropertyList) oldInput).removePropertyChangedListener(this);
            }

            if (newInput != null) {
                currentPropertyList = ((PropertyList) newInput);
                currentPropertyList.addPropertyChangedListener(this);
            }
        }

        public void propertyAdded(final PropertyPair propertyPair) {
            tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }

        public void propertyChanged(final PropertyPair propertyPair) {
            tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.update(propertyPair, null);
                }
            });
        }

        public void propertyRemoved(final PropertyPair propertyPair) {
            tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }
    }

    private static final int BUTTON_WIDTH = 80;

    private static final int DEFAULT_NUM_LINES = 10;

    private static final String[] FILTER_EXTENSIONS = new String[]{"*.properties", "*"};

    private static final int GRID_LAYOUT_COLUMNS = 6;

    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(PropertyEditor.class);

    private static final String NAME_COLUMN = "Name";

    private static final String VALUE_COLUMN = "Value";

    /**
     * Main method to launch the window
     */
    public static void main(String[] args) {

        Shell shell = new Shell();
        shell.setText("Property List - TableViewer Example");
        shell.setLayout(new FillLayout());
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new FillLayout());
        final PropertyEditor propertyViewer = new PropertyEditor(composite, SWT.NONE);
        Properties props = new Properties();
        props.setProperty("ga", "azerty");
        props.setProperty("bu", "aqsdfzerty");
        props.setProperty("zo", "12345");
        propertyViewer.setInput(props);

        // Ask the shell to display its content
        shell.open();
        propertyViewer.run(shell);
        System.err.println(propertyViewer.propertyList);
        System.exit(0);
    }

    // Set column names
    private String[] columnNames = new String[]{NAME_COLUMN, VALUE_COLUMN};

    private final PropertyList propertyList;

    private Table table;

    private TableViewer tableViewer;

    /**
     * 
     */
    public PropertyEditor(final Composite parent, final int style) {
        propertyList = new PropertyList();
        createContents(parent, style);
        tableViewer.setInput(propertyList);
    }

    public Properties getValues() {
        return propertyList.getAsProperties();
    }

    /**
     * @param properties
     */
    public void setInput(final Map properties) {
        propertyList.clear();
        if (properties != null) propertyList.addAll(properties);
    }

    /**
     * Add the "Add" and "Delete" buttons
     * 
     * @param parent
     *            the parent composite
     */
    private void createButtons(final Composite parent) {

        final Button load = new Button(parent, SWT.PUSH | SWT.CENTER);
        load.setText("Load");

        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = BUTTON_WIDTH;
        load.setLayoutData(gridData);
        load.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                final FileDialog dialog = new FileDialog(table.getShell());
                dialog.setFilterExtensions(FILTER_EXTENSIONS);
                final String fileName = dialog.open();
                if (fileName != null) {
                    FileInputStream fileInputStream = null;
                    try {
                        final Properties props = new Properties();
                        fileInputStream = new FileInputStream(fileName);
                        props.load(fileInputStream);
                        setInput(props);
                    } catch (IOException e) {
                        final String message = "Cannot load from " + fileName;
                        log.error(message, e);
                        ExceptionDialog.openException(table.getShell(), message, e);
                    } finally {
                        if (fileInputStream != null) try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            log.warn("Error closing file", e);
                        }
                    }
                }
            }
        });

        final Button save = new Button(parent, SWT.PUSH | SWT.CENTER);
        save.setText("Save");

        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = BUTTON_WIDTH;
        save.setLayoutData(gridData);
        save.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                final FileDialog dialog = new FileDialog(table.getShell(), SWT.SAVE);
                dialog.setFilterExtensions(FILTER_EXTENSIONS);
                final String fileName = dialog.open();
                if (fileName != null) {
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(fileName);
                        getValues().store(fileOutputStream, null);
                    } catch (IOException e) {
                        final String message = "Cannot save to " + fileName;
                        log.error(message, e);
                        ExceptionDialog.openException(table.getShell(), message, e);
                    } finally {
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                log.warn("Error closing file", e);
                            }
                        }
                    }
                }
            }
        });

        final Label filler = new Label(parent, SWT.NO_BACKGROUND);
        gridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        filler.setLayoutData(gridData);

        final Button add = new Button(parent, SWT.PUSH | SWT.CENTER);
        add.setText("Add");

        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.widthHint = BUTTON_WIDTH;
        add.setLayoutData(gridData);
        add.addSelectionListener(new SelectionAdapter() {

            // Add a task to the ExampleTaskList and refresh the view
            public void widgetSelected(SelectionEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("widgetSelected() - Adding new element");
                }
                propertyList.addProperty();
            }
        });

        final Button delete = new Button(parent, SWT.PUSH | SWT.CENTER);
        delete.setText("Delete");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.widthHint = BUTTON_WIDTH;
        delete.setLayoutData(gridData);

        delete.addSelectionListener(new SelectionAdapter() {

            // Remove the selection and refresh the view
            public void widgetSelected(SelectionEvent e) {
                PropertyPair pair = (PropertyPair) ((IStructuredSelection) tableViewer
                        .getSelection()).getFirstElement();
                if (pair != null) {
                    propertyList.remove(pair);
                }
            }
        });

        final Button clear = new Button(parent, SWT.PUSH | SWT.CENTER);
        clear.setText("Clear");

        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.widthHint = BUTTON_WIDTH;
        clear.setLayoutData(gridData);
        clear.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                propertyList.clear();
            }
        });

    }

    private void createContents(Composite parent, int style) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH);
        composite.setLayoutData(gridData);
        final GridLayout layout = new GridLayout(GRID_LAYOUT_COLUMNS, false);
        layout.marginWidth = 4;
        composite.setLayout(layout);

        createTable(composite);
        createTableViewer();
        tableViewer.setContentProvider(new PropertyListContentProvider());
        tableViewer.setLabelProvider(new PropertyListLabelProvider());
        createButtons(composite);
    }

    private void createTable(Composite parent) {
        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
                | SWT.HIDE_SELECTION;

        table = new Table(parent, style);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.pack(); // Required as it makes table compute the header size.

        final GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = GRID_LAYOUT_COLUMNS;
        gridData.heightHint = table.getHeaderHeight() * DEFAULT_NUM_LINES;
        table.setLayoutData(gridData);

        TableColumn column;
        column = new TableColumn(table, SWT.LEFT);
        column.setText(NAME_COLUMN);
        column.setWidth(200);
        column.setMoveable(true);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(VALUE_COLUMN);
        column.setWidth(200);
        column.setMoveable(true);
    }

    /**
     * Create the TableViewer
     */
    private void createTableViewer() {
        tableViewer = new TableViewer(table);
        tableViewer.setUseHashlookup(true);

        tableViewer.setColumnProperties(columnNames);

        // Create the cell editors
        CellEditor[] editors = new CellEditor[columnNames.length];

        // Column 1 : Name (Free text)
        TextCellEditor textEditor = new TextCellEditor(table);
        editors[0] = textEditor;

        // Column 2 : Value (Free text)
        textEditor = new TextCellEditor(table);
        editors[1] = textEditor;

        // Assign the cell editors to the viewer
        tableViewer.setCellEditors(editors);
        // Set the cell modifier for the viewer
        tableViewer.setCellModifier(new ICellModifier() {

            public boolean canModify(Object element, String property) {
                return true;
            }

            public Object getValue(Object element, String property) {
                if (element instanceof PropertyPair) {
                    PropertyPair pair = (PropertyPair) element;
                    if (NAME_COLUMN.equals(property)) {
                        return pair.getName();
                    }
                    else {
                        return pair.getValue();
                    }
                }
                return null;
            }

            public void modify(Object element, String property, Object value) {
                final PropertyPair pair = (PropertyPair) ((TableItem) element).getData();

                if (log.isDebugEnabled()) {
                    log.debug("modify() - Modifying property : pair = " + pair);
                }

                if (NAME_COLUMN.equals(property)) {
                    pair.setName(value.toString());
                }
                else {
                    pair.setValue(value.toString());
                }
                propertyList.update(pair);
            }
        });
        // Set the default sorter for the viewer
        tableViewer.setSorter(new ViewerSorter());
    }

    /**
     * Run and wait for a close event
     * 
     * @param shell
     *            Instance of Shell
     */
    private void run(Shell shell) {
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }
}
