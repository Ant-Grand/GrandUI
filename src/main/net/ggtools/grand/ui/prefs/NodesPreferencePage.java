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
package net.ggtools.grand.ui.prefs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * @author Christophe Labouisse
 */
public class NodesPreferencePage extends PreferencePage implements PreferenceKeys {

    private static final String[][] SUPPORTED_SHAPES = new String[][]{{"Octagon", "octagon"},
            {"Oval", "oval"}, {"Box", "box"}, {"Triangle", "triangle"}, {"Square", "square"},
            {"Hexagon", "hexagon"}, {"Circle", "circle"}};

    /**
     * Initialize a preferences store with sensible defaults values for the
     * nodes.
     * @param prefs
     */
    public static void setDefaults(final IPreferenceStore prefs) {
        PreferenceConverter
                .setDefault(prefs, NODE_PREFIX+"default.fgcolor", ColorConstants.black.getRGB());
        PreferenceConverter.setDefault(prefs, NODE_PREFIX+"default.fillcolor", ColorConstants.white
                .getRGB());
        //prefs.setDefault("nodes.default.font",4);
        prefs.setDefault(NODE_PREFIX+"default.shape", "oval");
        prefs.setDefault(NODE_PREFIX+"default.linewidth", 1);

        PreferenceConverter.setDefault(prefs, NODE_PREFIX+"main.fgcolor", ColorConstants.black.getRGB());
        PreferenceConverter.setDefault(prefs, NODE_PREFIX+"main.fillcolor", ColorConstants.cyan.getRGB());
        //prefs.setDefault(NODE_PREFIX+"main.font",4);
        prefs.setDefault(NODE_PREFIX+"main.shape", "box");
        prefs.setDefault(NODE_PREFIX+"main.linewidth", 1);

        PreferenceConverter.setDefault(prefs, NODE_PREFIX+"start.fgcolor", ColorConstants.black.getRGB());
        PreferenceConverter.setDefault(prefs, NODE_PREFIX+"start.fillcolor", ColorConstants.yellow
                .getRGB());
        //prefs.setDefault(NODE_PREFIX+"default.font",4);
        prefs.setDefault(NODE_PREFIX+"start.shape", "octagon");
        prefs.setDefault(NODE_PREFIX+"start.linewidth", 2);

        PreferenceConverter.setDefault(prefs, NODE_PREFIX+"missing.fgcolor", ColorConstants.gray.getRGB());
        PreferenceConverter.setDefault(prefs, NODE_PREFIX+"missing.fillcolor", ColorConstants.lightGray
                .getRGB());
        //prefs.setDefault(NODE_PREFIX+"default.font",4);
        prefs.setDefault(NODE_PREFIX+"missing.shape", "oval");
        prefs.setDefault(NODE_PREFIX+"missing.linewidth", 1);

    }

    private List fields = new ArrayList();

    /**
     * Creates a new NodesPreferencePage instance using the default
     * <em>Nodes</em> name.
     */
    NodesPreferencePage() {
        super("Nodes");
    }

    /**
     * The field editor preference page implementation of this
     * <code>PreferencePage</code> method saves all field editors by calling
     * <code>FieldEditor.store</code>. Note that this method does not save
     * the preference store itself; it just stores the values back into the
     * preference store.
     * 
     * @see FieldEditor#store()
     */
    public boolean performOk() {
        if (fields != null) {
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                final FieldEditor fieldEditor = (FieldEditor) iter.next();
                fieldEditor.store();
            }
        }
        return true;
    }

    /**
     * Calculates the number of columns needed to host all field editors.
     * 
     * @return the number of columns
     */
    private int calcNumberOfColumns(final List tabFields) {
        int result = 0;
        if (tabFields != null) {
            Iterator e = tabFields.iterator();
            while (e.hasNext()) {
                FieldEditor pe = (FieldEditor) e.next();
                result = Math.max(result, pe.getNumberOfControls());
            }
        }
        return result;
    }

    /**
     * @param tabFolder
     */
    private void createNodeTab(final TabFolder tabFolder, final String title, final String nodeType) {
        final String prefix = NODE_PREFIX + nodeType;
        final TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText(title);

        final Composite parent = new Composite(tabFolder, SWT.NONE);
        tabItem.setControl(parent);

        final List tabFields = new LinkedList();
        final ColorFieldEditor fgcolorField = new ColorFieldEditor(prefix + "fgcolor",
                "Foreground", parent);
        tabFields.add(fgcolorField);
        final ColorFieldEditor fillcolorField = new ColorFieldEditor(prefix + "fillcolor",
                "Shape filling", parent);
        tabFields.add(fillcolorField);
        // FIXME enable font preferences.
        //final FontFieldEditor fontField = new FontFieldEditor(prefix + "font", "Node font", parent);
        //tabFields.add(fontField);
        final RadioGroupFieldEditor shapeField = new RadioGroupFieldEditor(prefix + "shape",
                "Shape", 4, SUPPORTED_SHAPES, parent);
        tabFields.add(shapeField);
        final IntegerFieldEditor lineWidthField = new IntegerFieldEditor(prefix + "linewidth",
                "Line width", parent);
        lineWidthField.setValidRange(1, 5);
        tabFields.add(lineWidthField);
        final GridLayout layout = (GridLayout) parent.getLayout();

        layout.numColumns = calcNumberOfColumns(tabFields);
        if (tabFields != null) {
            for (Iterator iter = tabFields.iterator(); iter.hasNext();) {
                final FieldEditor fieldEditor = (FieldEditor) iter.next();
                if (fieldEditor.getNumberOfControls() < layout.numColumns)
                        fieldEditor.fillIntoGrid(parent, layout.numColumns);
                fieldEditor.setPage(this);
                //pe.setPropertyChangeListener(this);
                fieldEditor.setPreferenceStore(getPreferenceStore());
                fieldEditor.load();
            }
        }

        fields.addAll(tabFields);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FillLayout());
        final TabFolder tabFolder = new TabFolder(composite, SWT.TOP);

        createNodeTab(tabFolder, "Default nodes", "default.");
        createNodeTab(tabFolder, "Main nodes", "main.");
        createNodeTab(tabFolder, "Start node", "start.");
        createNodeTab(tabFolder, "Missing nodes", "missing.");

        return composite;
    }

    /**
     * The field editor preference page implementation of a
     * <code>PreferencePage</code> method loads all the field editors with
     * their default values.
     */
    protected void performDefaults() {
        if (fields != null) {
            Iterator e = fields.iterator();
            while (e.hasNext()) {
                FieldEditor pe = (FieldEditor) e.next();
                pe.loadDefault();
            }
        }
        super.performDefaults();
    }

}
