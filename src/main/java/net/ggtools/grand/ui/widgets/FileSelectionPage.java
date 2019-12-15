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
import java.util.Collection;
import java.util.HashSet;

import net.ggtools.grand.ui.RecentFilesManager;
import net.ggtools.grand.ui.widgets.OpenFileWizard.SelectedFileListener;
import net.ggtools.grand.ui.widgets.OpenFileWizard.SelectedFileProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

/**
 * @author Christophe Labouisse
 */
public class FileSelectionPage extends WizardPage
        implements SelectedFileProvider {
    /**
     * Logger for this class.
     */
    private static final Log LOG = LogFactory.getLog(FileSelectionPage.class);

    /**
     * Field FILTER_EXTENSIONS.
     */
    private static final String[] FILTER_EXTENSIONS = new String[]{"*.xml", "*"};

    /**
     * Field selectedFileName.
     */
    private String selectedFileName;

    /**
     * Field selectedFile.
     */
    private File selectedFile;

    /**
     * Field subscribers.
     */
    private final Collection<SelectedFileListener> subscribers;

    /**
     */
    public FileSelectionPage() {
        super("fileselect", "Build file selection", null);
        setDescription("Select the build file to be opened");
        subscribers = new HashSet<>();
    }

    /**
     * Method createControl.
     * @param parent Composite
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public final void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        setControl(composite);

        final CCombo combo = new CCombo(composite, SWT.BORDER);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        combo.add(""); // Default: no file.
        // Fill up the combo with the recent files
        for (final String fileName : RecentFilesManager.getInstance().getRecentFiles()) {
            combo.add(fileName);
        }

        combo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(final SelectionEvent e) {
                updateSelectedFile(combo.getText());

                if (LOG.isDebugEnabled()) {
                    LOG.debug("widgetSelected() - Changing file : selectionFile = "
                            + selectedFileName);
                }
            }

            public void widgetDefaultSelected(final SelectionEvent e) {
                widgetSelected(e);
                if (combo.indexOf(selectedFileName) == -1) {
                    combo.add(selectedFileName);
                }
            }

        });

        updateSelectedFile(combo.getItem(0));
        setPageComplete(false);

        final Button openFileButton = new Button(composite, SWT.PUSH);
        openFileButton.setText("...");
        openFileButton.addSelectionListener(new SelectionAdapter() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final FileDialog dialog = new FileDialog(getShell());
                dialog.setFilterExtensions(FILTER_EXTENSIONS);
                dialog.setFilterPath(selectedFileName);
                final String buildFileName = dialog.open();
                LOG.debug("Dialog returned " + buildFileName);
                if (buildFileName != null) {
                    combo.add(buildFileName);
                    combo.select(combo.getItemCount() - 1);
                    updateSelectedFile(combo.getText());
                }
            }
        });
    }

    /**
     * @param text String
     */
    private void updateSelectedFile(final String text) {
        selectedFileName = text;
        if (selectedFileName == null || selectedFileName.isEmpty()) {
            selectedFile = null;
        } else {
            selectedFile = new File(selectedFileName);
            final boolean isSelectedFileValid = selectedFile.isFile();
            setPageComplete(isSelectedFileValid);
            if (isSelectedFileValid) {
                setErrorMessage(null);
            } else {
                selectedFile = null;
                setErrorMessage(selectedFileName + " is not a valid build file");
            }
        }
        notifyListeners();
    }

    /**
     * Method getSelectedFile.
     * @return File
     */
    public final File getSelectedFile() {
        return selectedFile;
    }

    /**
     * Method addListener.
     * @param listener OpenFileWizard.SelectedFileListener
     * @see SelectedFileProvider#addListener(OpenFileWizard.SelectedFileListener)
     */
    public final void addListener(final OpenFileWizard.SelectedFileListener listener) {
        if (!subscribers.contains(listener)) {
            subscribers.add(listener);
            listener.fileSelected(selectedFile);
        }
    }

    /**
     * Method removeListener.
     * @param listener OpenFileWizard.SelectedFileListener
     * @see SelectedFileProvider#removeListener(OpenFileWizard.SelectedFileListener)
     */
    public final void removeListener(final OpenFileWizard.SelectedFileListener listener) {
        subscribers.remove(listener);
    }

    /**
     * Method notifyListeners.
     */
    private void notifyListeners() {
        for (final SelectedFileListener listener : subscribers) {
            listener.fileSelected(selectedFile);
        }
    }

}
