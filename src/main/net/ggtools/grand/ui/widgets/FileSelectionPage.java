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
import java.util.Iterator;

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
public class FileSelectionPage extends WizardPage implements SelectedFileProvider {
    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(FileSelectionPage.class);

    private static final String[] FILTER_EXTENSIONS = new String[]{"*.xml", "*"};

    private String selectedFileName;

    private File selectedFile;

    private final Collection<SelectedFileListener> subscribers;

    /**
     * @param pageName
     */
    public FileSelectionPage() {
        super("fileselect", "Build file selection", null);
        setDescription("Select the build file to be opened");
        subscribers = new HashSet<SelectedFileListener>();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        setControl(composite);

        final CCombo combo = new CCombo(composite, SWT.NONE);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        combo.add(""); // Default: no file.
        // Fill up the combo with the recent files
        for (final Iterator<String> iter = RecentFilesManager.getInstance().getRecentFiles().iterator(); iter
                .hasNext();) {
            final String fileName = iter.next();
            combo.add(fileName);
        }

        combo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(final SelectionEvent e) {
                updateSelectedFile(combo.getText());

                if (log.isDebugEnabled()) {
                    log.debug("widgetSelected() - Changing file : selectionFile = "
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
                log.debug("Dialog returned " + buildFileName);
                if (buildFileName != null) {
                    combo.add(buildFileName);
                    combo.select(combo.getItemCount() - 1);
                    updateSelectedFile(combo.getText());
                }
            }
        });
    }

    /**
     * @param text
     */
    private void updateSelectedFile(final String text) {
        selectedFileName = text;
        if ("".equals(selectedFileName)) {
            selectedFile = null;
        }
        else {
            selectedFile = new File(selectedFileName);
            final boolean isSelectedFileValid = selectedFile.isFile();
            setPageComplete(isSelectedFileValid);
            if (isSelectedFileValid) {
                setErrorMessage(null);
            }
            else {
                selectedFile = null;
                setErrorMessage(selectedFileName + " is not a valid build file");
            }
        }
        notifyListeners();
    }

    public final File getSelectedFile() {
        return selectedFile;
    }

    public void addListener(final OpenFileWizard.SelectedFileListener listener) {
        if (!subscribers.contains(listener)) {
            subscribers.add(listener);
            listener.fileSelected(selectedFile);
        }
    }

    public void removeListener(final OpenFileWizard.SelectedFileListener listener) {
        subscribers.remove(listener);
    }

    private void notifyListeners() {
        for (final Iterator<SelectedFileListener> iter = subscribers.iterator(); iter.hasNext();) {
            final OpenFileWizard.SelectedFileListener listener = iter.next();
            listener.fileSelected(selectedFile);
        }
    }

}
