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

import org.eclipse.jface.wizard.Wizard;

/**
 * @author Christophe Labouisse
 */
public class OpenFileWizard extends Wizard {

    /**
     * @author Christophe Labouisse
     */
    interface SelectedFileProvider {
        /**
         * Method addListener.
         * @param listener SelectedFileListener
         */
        void addListener(SelectedFileListener listener);

        /**
         * Method removeListener.
         * @param listener SelectedFileListener
         */
        void removeListener(SelectedFileListener listener);
    }

    /**
     * @author Christophe Labouisse
     */
    interface SelectedFileListener {
        /**
         * Method fileSelected.
         * @param selectedFile File
         */
        void fileSelected(File selectedFile);
    }

    /**
     * Field window.
     */
    private final GraphWindow window;

    /**
     * Field propertySettingPage.
     */
    private PropertySettingPage propertySettingPage;

    /**
     * Field fileSelectionPage.
     */
    private FileSelectionPage fileSelectionPage;

    /**
     *
     * @param window GraphWindow
     */
    public OpenFileWizard(final GraphWindow window) {
        super();
        this.window = window;
    }

    /**
     * Method addPages.
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    @Override
    public final void addPages() {
        fileSelectionPage = new FileSelectionPage();
        addPage(fileSelectionPage);
        propertySettingPage = new PropertySettingPage(fileSelectionPage);
        addPage(propertySettingPage);
    }

    /**
     * Method performFinish.
     * @return boolean
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    @Override
    public final boolean performFinish() {
        boolean rc = false;
        final File selectedFile = fileSelectionPage.getSelectedFile();
        if (selectedFile != null) {
            window.openGraphInNewDisplayer(selectedFile,
                    propertySettingPage.getProperties());
            rc = true;
        }

        return rc;
    }

    /**
     * Method canFinish.
     * @return boolean
     * @see org.eclipse.jface.wizard.IWizard#canFinish()
     */
    @Override
    public final boolean canFinish() {
        return fileSelectionPage.getSelectedFile() != null;
    }
}
