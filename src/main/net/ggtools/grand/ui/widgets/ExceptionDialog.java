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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog specialized in displaying exception. Quite minimal at the moment, except for a high
 * level openException taking directly an exception.
 * 
 * @author Christophe Labouisse
 */
public class ExceptionDialog extends ErrorDialog {
    private ExceptionDialog(final Shell parentShell, final String dialogTitle, final String message, final IStatus status,
            final int displayMask) {
        super(parentShell, dialogTitle, message, status, displayMask);
    }

    /**
     * Opens a dialog to display an error caused by an exception. This method is intented
     * to work regardless of the current thread.
     * 
     * @param parent
     * @param message
     * @param e
     */
    public static void openException(final Shell parent, final String message, final Throwable e) {
        final MultiStatus topStatus = new MultiStatus("GrandUI", 0, message, e);
        for (Throwable nested = e; nested != null; nested = nested.getCause()) {
            final IStatus status = new Status(IStatus.ERROR, "GraphUI", 0, nested.getMessage(),
                    nested);
            topStatus.add(status);
        }
        Display display;
        if (parent == null) {
            display = Display.getCurrent();
        }
        else {
            display = parent.getDisplay();
        }
        display.syncExec(new Runnable() {
            public void run() {
                ErrorDialog.openError(parent, message, e.getMessage(), topStatus);
            }
        });
    }
}
