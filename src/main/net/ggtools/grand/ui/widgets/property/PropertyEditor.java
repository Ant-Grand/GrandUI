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
import java.util.HashMap;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Christophe Labouisse
 */
public class PropertyEditor {

    private final class CellModifier implements ICellModifier {
        public boolean canModify(Object element, String property) {
            if (columnExists(property)) {
                final int columnNumber = getColumnNumber(property);
                switch (columnNumber) {
                case STATUS_COLUMN_NUM:
                    return false;

                default:
                    return true;
                }
            }

            return false;
        }

        public Object getValue(Object element, String property) {

            if (columnExists(property) && element instanceof PropertyPair) {
                final int columnNumber = getColumnNumber(property);
                final PropertyPair pair = (PropertyPair) element;
                switch (columnNumber) {
                case NAME_COLUMN_NUM:
                    return pair.getName();

                case VALUE_COLUMN_NUM:
                    return pair.getValue();
                }
            }
            return null;
        }

        public void modify(Object element, String property, Object value) {
            if (columnExists(property)) {
                final PropertyPair pair = (PropertyPair) ((TableItem) element).getData();

                if (log.isDebugEnabled()) {
                    log.debug("modify() - Modifying property : pair = " + pair);
                }

                final int columnNumber = getColumnNumber(property);
                switch (columnNumber) {
                case NAME_COLUMN_NUM:
                    pair.setName(value.toString());
                    propertyList.update(pair);
                    break;

                case VALUE_COLUMN_NUM:
                    pair.setValue(value.toString());
                    propertyList.update(pair);
                    break;
                }
            }
        }
    }

    private static final class PropertyListContentProvider implements IStructuredContentProvider,
            PropertyChangedListener {
        /**
         * Logger for this class
         */
        private static final Log log = LogFactory.getLog(PropertyListContentProvider.class);

        private PropertyList currentPropertyList;

        private TableViewer tableViewer;

        public void allPropertiesChanged(Object fillerParameter) {
            tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }

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

    private static final class Sorter extends ViewerSorter {
        private final static int NAME_COLUMN = 1;

        private final static int VALUE_COLUMN = 2;

        private int column = NAME_COLUMN;

        public int compare(Viewer viewer, Object e1, Object e2) {
            if (e1 instanceof PropertyPair && e2 instanceof PropertyPair) {
                final PropertyPair p1 = (PropertyPair) e1;
                final PropertyPair p2 = (PropertyPair) e2;

                String name1 = null;
                String name2 = null;

                switch (column) {
                case NAME_COLUMN:
                    name1 = p1.getName();
                    name2 = p2.getName();
                    break;

                case VALUE_COLUMN:
                    name1 = p1.getValue();
                    name2 = p2.getValue();
                    break;

                }
                return collator.compare(name1, name2);
            }
            else {
                return super.compare(viewer, e1, e2);
            }
        }

        public void sortByName() {
            column = NAME_COLUMN;
        }

        public void sortByValue() {
            column = VALUE_COLUMN;
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

    private static final String STATUS_COLUMN = "Status";

    private static final String NAME_COLUMN = "Name";

    private static final String VALUE_COLUMN = "Value";

    static final int STATUS_COLUMN_NUM = 0;

    static final int NAME_COLUMN_NUM = 1;

    static final int VALUE_COLUMN_NUM = 2;

    private static Map columnNamesToNumMap = null;

    // Set column names
    private String[] columnNames = new String[]{STATUS_COLUMN, NAME_COLUMN, VALUE_COLUMN};

    private final PropertyList propertyList;

    private Table table;

    private TableViewer tableViewer;

    private Sorter viewerSorter;

    /**
     * 
     */
    public PropertyEditor(final Composite parent, final int style) {
        if (columnNamesToNumMap == null) {
            columnNamesToNumMap = new HashMap();
            columnNamesToNumMap.put(STATUS_COLUMN, new Integer(STATUS_COLUMN_NUM));
            columnNamesToNumMap.put(NAME_COLUMN, new Integer(NAME_COLUMN_NUM));
            columnNamesToNumMap.put(VALUE_COLUMN, new Integer(VALUE_COLUMN_NUM));
        }

        propertyList = new PropertyList();
        viewerSorter = new Sorter();
        createContents(parent, style);
        tableViewer.setInput(propertyList);
    }

    public Properties getValues() {
        return propertyList.getAsProperties();
    }
    
    PropertyList getPropertyList() {
        return propertyList;
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
        // column.setText(STATUS_COLUMN);
        column.setWidth(20);
        column.setMoveable(true);
        column.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                // viewerSorter.sortByName();
                if (tableViewer != null) tableViewer.refresh(false);
            }
        });

        column = new TableColumn(table, SWT.LEFT);
        column.setText(NAME_COLUMN);
        column.setWidth(200);
        column.setMoveable(true);
        column.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                viewerSorter.sortByName();
                if (tableViewer != null) tableViewer.refresh(false);
            }
        });

        column = new TableColumn(table, SWT.LEFT);
        column.setText(VALUE_COLUMN);
        column.setWidth(200);
        column.setMoveable(true);
        column.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                viewerSorter.sortByValue();
                if (tableViewer != null) tableViewer.refresh(false);
            }
        });

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

        // Column 1: Not editable
        editors[STATUS_COLUMN_NUM] = null;

        // Column 2: Name (Free text)
        TextCellEditor textEditor = new TextCellEditor(table);
        editors[NAME_COLUMN_NUM] = textEditor;

        // Column 3: Value (Free text)
        textEditor = new TextCellEditor(table);
        editors[VALUE_COLUMN_NUM] = textEditor;

        // Assign the cell editors to the viewer
        tableViewer.setCellEditors(editors);
        // Set the cell modifier for the viewer
        tableViewer.setCellModifier(new CellModifier());
        tableViewer.setSorter(viewerSorter);
    }

    /**
     * @param columnName
     * @return
     */
    int getColumnNumber(String columnName) {
        return ((Integer) columnNamesToNumMap.get(columnName)).intValue();
    }

    /**
     * @param columnName
     * @return
     */
    boolean columnExists(String columnName) {
        return columnNamesToNumMap.containsKey(columnName);
    }

}
