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
package net.ggtools.grand.ui.widgets;

import java.io.File;
import java.util.Properties;

import net.ggtools.grand.ui.RecentFilesManager;
import net.ggtools.grand.ui.widgets.OpenFileWizard.SelectedFileListener;
import net.ggtools.grand.ui.widgets.OpenFileWizard.SelectedFileProvider;
import net.ggtools.grand.ui.widgets.property.PropertyEditor;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Christophe Labouisse
 */
public class PropertySettingPage extends WizardPage implements SelectedFileListener {

    private PropertyEditor editor;

    private final SelectedFileProvider fileProvider;

    /**
     * @param pageName
     */
    public PropertySettingPage(final OpenFileWizard.SelectedFileProvider fileProvider) {
        super("propertySetting", "Property setting", null);
        setDescription("Set the properties for the file");
        this.fileProvider = fileProvider;
        fileProvider.addListener(this);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);
        composite.setLayout(new FillLayout());
        editor = new PropertyEditor(composite, SWT.NONE);
    }

    public Properties getProperties() {
        return editor.getValues();
    }
    
    @Override
    public void dispose() {
        fileProvider.removeListener(this);
        super.dispose();
    }

    public void fileSelected(final File selectedFile) {
        if (editor != null) {
            editor.setInput(RecentFilesManager.getInstance().getProperties(
                    selectedFile));
        }
    }

}
