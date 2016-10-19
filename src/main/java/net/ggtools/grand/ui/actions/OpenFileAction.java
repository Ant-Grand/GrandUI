// $Id$
/* ====================================================================
 * Copyright (c) 2002-2003, Christophe Labouisse
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.ggtools.grand.ui.actions;

import net.ggtools.grand.ui.widgets.GraphWindow;
import net.ggtools.grand.ui.widgets.OpenFileWizard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;

/**
 * Open a build file allowing setting some properties.
 *
 * @author Christophe Labouisse
 * @see org.eclipse.jface.action.Action
 */
public class OpenFileAction extends Action {

    /**
     * Field log.
     */
    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(OpenFileAction.class);

    /**
     * Field DEFAULT_ACTION_NAME.
     * (value is ""Open with properties"")
     */
    private static final String DEFAULT_ACTION_NAME = "Open with properties";

    /**
     * Field window.
     */
    private final GraphWindow window;

    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    @Override
    public final void run() {
        final IWizard wizard = new OpenFileWizard(window);
        final WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.create();
        dialog.open();
    }

    /**
     * Creates a new QuickOpenFileAction object.
     *
     * @param parent GraphWindow
     */
    public OpenFileAction(final GraphWindow parent) {
        super(DEFAULT_ACTION_NAME);
        window = parent;
        setAccelerator(SWT.SHIFT | (SWT.getPlatform().equals("cocoa") ? SWT.MOD1 : SWT.CONTROL) | 'O');
    }

    /**
     * Creates a new QuickOpenFileAction object with specific name.
     *
     * @param name String
     * @param parent GraphWindow
     */
    public OpenFileAction(final String name, final GraphWindow parent) {
        super(name);
        window = parent;
    }
}
