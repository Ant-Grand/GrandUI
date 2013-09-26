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

    /**
     * @author Christophe Labouisse
     */
    private final class CellModifier implements ICellModifier {
        /**
         * Method canModify.
         * @param element Object
         * @param property String
         * @return boolean
         * @see org.eclipse.jface.viewers.ICellModifier#canModify(Object, String)
         */
        public boolean canModify(final Object element, final String property) {
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

        /**
         * Method getValue.
         * @param element Object
         * @param property String
         * @return Object
         * @see org.eclipse.jface.viewers.ICellModifier#getValue(Object, String)
         */
        public Object getValue(final Object element, final String property) {

            if (columnExists(property) && (element instanceof PropertyPair)) {
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

        /**
         * Method modify.
         * @param element Object
         * @param property String
         * @param value Object
         * @see org.eclipse.jface.viewers.ICellModifier#modify(Object, String, Object)
         */
        public void modify(final Object element, final String property, final Object value) {
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

    /**
     * @author Christophe Labouisse
     */
    private static final class PropertyListContentProvider implements IStructuredContentProvider,
            PropertyChangedListener {
        /**
         * Logger for this class
         */
        @SuppressWarnings("unused")
        private static final Log log = LogFactory.getLog(PropertyListContentProvider.class);

        /**
         * Field currentPropertyList.
         */
        private PropertyList currentPropertyList;

        /**
         * Field tableViewer.
         */
        private TableViewer tableViewer;

        /**
         * Method allPropertiesChanged.
         * @param fillerParameter Object
         * @see net.ggtools.grand.ui.widgets.property.PropertyChangedListener#allPropertiesChanged(Object)
         */
        public void allPropertiesChanged(final Object fillerParameter) {
            tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }

        /**
         * Method clearedProperties.
         * @param fillerParameter Object
         * @see net.ggtools.grand.ui.widgets.property.PropertyChangedListener#clearedProperties(Object)
         */
        public void clearedProperties(final Object fillerParameter) {
            tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }

        /**
         * Method dispose.
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
            if (currentPropertyList != null) {
                currentPropertyList.removePropertyChangedListener(this);
            }
        }

        /**
         * Method getElements.
         * @param inputElement Object
         * @return Object[]
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
         */
        public Object[] getElements(final Object inputElement) {
            if (inputElement instanceof PropertyList) {
                final PropertyList pList = (PropertyList) inputElement;
                return pList.toArray();
            }
            else {
                return null;
            }
        }

        /**
         * Method inputChanged.
         * @param viewer Viewer
         * @param oldInput Object
         * @param newInput Object
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
         */
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            tableViewer = (TableViewer) viewer;

            if (oldInput != null) {
                ((PropertyList) oldInput).removePropertyChangedListener(this);
            }

            if (newInput != null) {
                currentPropertyList = ((PropertyList) newInput);
                currentPropertyList.addPropertyChangedListener(this);
            }
        }

        /**
         * Method propertyAdded.
         * @param propertyPair PropertyPair
         * @see net.ggtools.grand.ui.widgets.property.PropertyChangedListener#propertyAdded(PropertyPair)
         */
        public void propertyAdded(final PropertyPair propertyPair) {
            tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }

        /**
         * Method propertyChanged.
         * @param propertyPair PropertyPair
         * @see net.ggtools.grand.ui.widgets.property.PropertyChangedListener#propertyChanged(PropertyPair)
         */
        public void propertyChanged(final PropertyPair propertyPair) {
            tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.update(propertyPair, null);
                }
            });
        }

        /**
         * Method propertyRemoved.
         * @param propertyPair PropertyPair
         * @see net.ggtools.grand.ui.widgets.property.PropertyChangedListener#propertyRemoved(PropertyPair)
         */
        public void propertyRemoved(final PropertyPair propertyPair) {
            tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }
    }

    /**
     * @author Christophe Labouisse
     */
    private static final class Sorter extends ViewerSorter {
        /**
         * Field NAME_COLUMN.
         * (value is 1)
         */
        private final static int NAME_COLUMN = 1;

        /**
         * Field VALUE_COLUMN.
         * (value is 2)
         */
        private final static int VALUE_COLUMN = 2;

        /**
         * Field column.
         */
        private int column = NAME_COLUMN;

        /**
         * Method compare.
         * @param viewer Viewer
         * @param e1 Object
         * @param e2 Object
         * @return int
         */
        @Override
        public int compare(final Viewer viewer, final Object e1, final Object e2) {
            if ((e1 instanceof PropertyPair) && (e2 instanceof PropertyPair)) {
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

        /**
         * Method sortByName.
         */
        public void sortByName() {
            column = NAME_COLUMN;
        }

        /**
         * Method sortByValue.
         */
        public void sortByValue() {
            column = VALUE_COLUMN;
        }
    }

    /**
     * Field BUTTON_WIDTH.
     * (value is 80)
     */
    private static final int BUTTON_WIDTH = 80;

    /**
     * Field DEFAULT_NUM_LINES.
     * (value is 10)
     */
    private static final int DEFAULT_NUM_LINES = 10;

    /**
     * Field FILTER_EXTENSIONS.
     */
    private static final String[] FILTER_EXTENSIONS = new String[]{"*.properties", "*"};

    /**
     * Field GRID_LAYOUT_COLUMNS.
     * (value is 6)
     */
    private static final int GRID_LAYOUT_COLUMNS = 6;

    /**
     * Logger for this class.
     */
    private static final Log log = LogFactory.getLog(PropertyEditor.class);

    /**
     * Field STATUS_COLUMN.
     * (value is ""Status"")
     */
    private static final String STATUS_COLUMN = "Status";

    /**
     * Field NAME_COLUMN.
     * (value is ""Name"")
     */
    private static final String NAME_COLUMN = "Name";

    /**
     * Field VALUE_COLUMN.
     * (value is ""Value"")
     */
    private static final String VALUE_COLUMN = "Value";

    /**
     * Field STATUS_COLUMN_NUM.
     * (value is 0)
     */
    static final int STATUS_COLUMN_NUM = 0;

    /**
     * Field NAME_COLUMN_NUM.
     * (value is 1)
     */
    static final int NAME_COLUMN_NUM = 1;

    /**
     * Field VALUE_COLUMN_NUM.
     * (value is 2)
     */
    static final int VALUE_COLUMN_NUM = 2;

    /**
     * Field columnNamesToNumMap.
     */
    private static Map<String, Integer> columnNamesToNumMap = null;

    // Set column names
    /**
     * Field columnNames.
     */
    private final String[] columnNames = new String[]{STATUS_COLUMN, NAME_COLUMN, VALUE_COLUMN};

    /**
     * Field propertyList.
     */
    private final PropertyList propertyList;

    /**
     * Field table.
     */
    private Table table;

    /**
     * Field tableViewer.
     */
    private TableViewer tableViewer;

    /**
     * Field viewerSorter.
     */
    private Sorter viewerSorter;

    /**
     *
     * @param parent Composite
     * @param style int
     */
    public PropertyEditor(final Composite parent, final int style) {
        if (columnNamesToNumMap == null) {
            columnNamesToNumMap = new HashMap<String, Integer>();
            columnNamesToNumMap.put(STATUS_COLUMN, new Integer(STATUS_COLUMN_NUM));
            columnNamesToNumMap.put(NAME_COLUMN, new Integer(NAME_COLUMN_NUM));
            columnNamesToNumMap.put(VALUE_COLUMN, new Integer(VALUE_COLUMN_NUM));
        }

        propertyList = new PropertyList();
        viewerSorter = new Sorter();
        createContents(parent, style);
        tableViewer.setInput(propertyList);
    }

    /**
     * Method getValues.
     * @return Properties
     */
    public final Properties getValues() {
        return propertyList.getAsProperties();
    }

    /**
     * Method getPropertyList.
     * @return PropertyList
     */
    final PropertyList getPropertyList() {
        return propertyList;
    }

    /**
     * @param properties Properties
     */
    public final void setInput(final Properties properties) {
        propertyList.clear();
        if (properties != null) {
            propertyList.addAll(properties);
        }
    }

    /**
     * Add the "Add" and "Delete" buttons.
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

            @Override
            public void widgetSelected(final SelectionEvent event) {
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
                    } catch (final IOException e) {
                        final String message = "Cannot load from " + fileName;
                        log.error(message, e);
                        ExceptionDialog.openException(table.getShell(), message, e);
                    } finally {
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (final IOException e) {
                                log.warn("Error closing file", e);
                            }
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

            @Override
            public void widgetSelected(final SelectionEvent event) {
                final FileDialog dialog = new FileDialog(table.getShell(), SWT.SAVE);
                dialog.setFilterExtensions(FILTER_EXTENSIONS);
                final String fileName = dialog.open();
                if (fileName != null) {
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(fileName);
                        getValues().store(fileOutputStream, null);
                    } catch (final IOException e) {
                        final String message = "Cannot save to " + fileName;
                        log.error(message, e);
                        ExceptionDialog.openException(table.getShell(), message, e);
                    } finally {
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (final IOException e) {
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
            @Override
            public void widgetSelected(final SelectionEvent e) {
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
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final PropertyPair pair = (PropertyPair) ((IStructuredSelection)
                        tableViewer.getSelection()).getFirstElement();
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

            @Override
            public void widgetSelected(final SelectionEvent e) {
                propertyList.clear();
            }
        });

    }

    /**
     * Method createContents.
     * @param parent Composite
     * @param style int
     */
    private void createContents(final Composite parent, final int style) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.FILL_BOTH);
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

    /**
     * Method createTable.
     * @param parent Composite
     */
    private void createTable(final Composite parent) {
        final int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

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
            @Override
            public void widgetSelected(final SelectionEvent e) {
                // viewerSorter.sortByName();
                if (tableViewer != null) {
                    tableViewer.refresh(false);
                }
            }
        });

        column = new TableColumn(table, SWT.LEFT);
        column.setText(NAME_COLUMN);
        column.setWidth(200);
        column.setMoveable(true);
        column.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                viewerSorter.sortByName();
                if (tableViewer != null) {
                    tableViewer.refresh(false);
                }
            }
        });

        column = new TableColumn(table, SWT.LEFT);
        column.setText(VALUE_COLUMN);
        column.setWidth(200);
        column.setMoveable(true);
        column.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                viewerSorter.sortByValue();
                if (tableViewer != null) {
                    tableViewer.refresh(false);
                }
            }
        });

    }

    /**
     * Create the TableViewer.
     */
    private void createTableViewer() {
        tableViewer = new TableViewer(table);
        tableViewer.setUseHashlookup(true);

        tableViewer.setColumnProperties(columnNames);

        // Create the cell editors
        final CellEditor[] editors = new CellEditor[columnNames.length];

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
     * @param columnName String
     * @return int
     */
    int getColumnNumber(final String columnName) {
        return columnNamesToNumMap.get(columnName).intValue();
    }

    /**
     * @param columnName String
     * @return boolean
     */
    boolean columnExists(final String columnName) {
        return columnNamesToNumMap.containsKey(columnName);
    }

}
