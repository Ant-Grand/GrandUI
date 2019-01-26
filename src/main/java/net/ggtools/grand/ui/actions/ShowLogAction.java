// $Id$
/*
 * ====================================================================
 * Copyright (c) 2002-2003, Christophe Labouisse All rights reserved.
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

package net.ggtools.grand.ui.actions;

import net.ggtools.grand.ui.widgets.GraphWindow;
import net.ggtools.grand.ui.widgets.LogWindow;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;

/**
 *
 *
 * @author Christophe Labouisse
 */
public class ShowLogAction extends Action {

    /**
     * @author Christophe Labouisse
     */
    private final class DialogDisposeListener implements DisposeListener {
        /**
         * Field shell.
         */
        private final Shell shell;

        /**
         * Constructor for DialogDisposeListener.
         * @param shell Shell
         */
        private DialogDisposeListener(final Shell shell) {
            super();
            this.shell = shell;
        }

        /**
         * Method widgetDisposed.
         * @param e DisposeEvent
         * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(DisposeEvent)
         */
        public void widgetDisposed(final DisposeEvent e) {
            setChecked(false);
            shell.removeDisposeListener(this);
            dialog = null;
        }
    }

    /**
     * Field DEFAULT_ACTION_NAME.
     * (value is {@value #DEFAULT_ACTION_NAME})
     */
    private static final String DEFAULT_ACTION_NAME = "Log window";

    /**
     * Field window.
     */
    private final GraphWindow window;

    /**
     * Field dialog.
     */
    private LogWindow dialog;

    /**
     * Constructor for ShowLogAction.
     * @param parent GraphWindow
     */
    public ShowLogAction(final GraphWindow parent) {
        super(DEFAULT_ACTION_NAME);
        setChecked(false);
        window = parent;
        setAccelerator((SWT.getPlatform().equals("cocoa") ? SWT.MOD1 : SWT.CONTROL) | 'L');
    }

    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    @Override
    public final void run() {
        if (isChecked()) {
            if ((dialog != null) && dialog.getShell().isDisposed()) {
                dialog = null;
            }

            if (dialog == null) {
                dialog = new LogWindow(window.getShell());
            }
            dialog.open();
            final Shell shell = dialog.getShell();
            shell.addDisposeListener(new DialogDisposeListener(shell));
        } else {
            if (dialog != null) {
                dialog.close();
                dialog = null;
            }
        }
    }
}
