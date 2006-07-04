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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Christophe Labouisse
 */
public class LinksPreferencePage extends FieldEditorPreferencePage implements PreferenceKeys {
    public static void setDefaults(final IPreferenceStore prefs) {
        PreferenceConverter.setDefault(prefs, LINK_DEFAULT_COLOR, ColorConstants.black.getRGB());
        prefs.setDefault(LINK_DEFAULT_LINEWIDTH, 1);
        PreferenceConverter.setDefault(prefs, LINK_WEAK_COLOR, ColorConstants.lightGray.getRGB());
        prefs.setDefault(LINK_WEAK_LINEWIDTH, 1);
        PreferenceConverter.setDefault(prefs, LINK_SUBANT_COLOR, ColorConstants.lightGray.getRGB());
        prefs.setDefault(LINK_SUBANT_LINEWIDTH, 2);
    }

    /**
     *  
     */
    public LinksPreferencePage() {
        super("Links", GRID);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();
        final ColorFieldEditor defaultLinkColor = new ColorFieldEditor(LINK_DEFAULT_COLOR,
                "Link color", parent);
        addField(defaultLinkColor);
        final IntegerFieldEditor defaultLinkLineWidth = new IntegerFieldEditor(LINK_DEFAULT_LINEWIDTH,
                "Link width", parent);
        defaultLinkLineWidth.setValidRange(1, 5);
        addField(defaultLinkLineWidth);
        final ColorFieldEditor weakLinkColor = new ColorFieldEditor(LINK_WEAK_COLOR,
                "Weak link color", parent);
        addField(weakLinkColor);
        final IntegerFieldEditor weakLinkLineWidth = new IntegerFieldEditor(LINK_WEAK_LINEWIDTH,
                "Weak link width", parent);
        weakLinkLineWidth.setValidRange(1, 5);
        addField(weakLinkLineWidth);
        final ColorFieldEditor subantLinkColor = new ColorFieldEditor(LINK_SUBANT_COLOR,
                "subant link color", parent);
        addField(subantLinkColor);
        final IntegerFieldEditor subantLinkLineWidth = new IntegerFieldEditor(LINK_SUBANT_LINEWIDTH,
                "subant link width", parent);
        subantLinkLineWidth.setValidRange(1, 5);
        addField(subantLinkLineWidth);
    }
}
