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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Christophe Labouisse
 */
public class GraphPreferencePage extends FieldEditorPreferencePage
        implements PreferenceKeys {
    public static void setDefaults(final IPreferenceStore prefs) {
        prefs.setDefault(GRAPH_BUS_ENABLED_DEFAULT, false);
        prefs.setDefault(GRAPH_BUS_IN_THRESHOLD, 5);
        prefs.setDefault(GRAPH_BUS_OUT_THRESHOLD, 5);
    }

    /**
     *  
     */
    public GraphPreferencePage() {
        super("Graph", GRID);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected final void createFieldEditors() {
        final Composite parent = getFieldEditorParent();
        final BooleanFieldEditor enableBusRouting =
                new BooleanFieldEditor(GRAPH_BUS_ENABLED_DEFAULT,
                        "Bus routing enabled on graph loading", parent);
        addField(enableBusRouting);
        final IntegerFieldEditor inThreshold =
                new IntegerFieldEditor(GRAPH_BUS_IN_THRESHOLD,
                        "Bus routing in threshold", parent);
        addField(inThreshold);
        final IntegerFieldEditor outThreadshold =
                new IntegerFieldEditor(GRAPH_BUS_OUT_THRESHOLD,
                        "Bus routing out threshold", parent);
        addField(outThreadshold);
    }

}
