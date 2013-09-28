//$Id$
/*
 * ====================================================================
 * Copyright (c) 2002-2005, Christophe Labouisse All rights reserved.
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
package net.ggtools.grand.ui.widgets;

import java.util.Properties;

import net.ggtools.grand.ui.widgets.property.PropertyEditor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 *
 * @author Christophe Labouisse
 */
public class PropertyEditionDialog extends Dialog {

    /**
     * Field propertyEditor.
     */
    private PropertyEditor propertyEditor;
    /**
     * Field propertiesToLoad.
     */
    private Properties propertiesToLoad;

    /**
     * Constructor for PropertyEditionDialog.
     * @param parentShell Shell
     */
    public PropertyEditionDialog(final Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    /**
     * Method createDialogArea.
     * @param parent Composite
     * @return Control
     */
    @Override
    protected final Control createDialogArea(final Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        propertyEditor = new PropertyEditor(composite, SWT.NONE);
        if (propertiesToLoad != null) {
            propertyEditor.setInput(propertiesToLoad);
            propertiesToLoad = null;
        }
        return composite;
    }

    /**
     * Method configureShell.
     * @param newShell Shell
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected final void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Property Edition");
    }

    /**
     * Method setProperties.
     * @param properties Properties
     */
    public final void setProperties(final Properties properties) {
        if (propertyEditor == null) {
            propertiesToLoad = properties;
        } else {
            propertyEditor.setInput(properties);
        }
    }

    /**
     * Method getProperties.
     * @return Properties
     */
    public final Properties getProperties() {
        if (propertyEditor == null) {
            return null;
        }

        return propertyEditor.getValues();
    }
}
