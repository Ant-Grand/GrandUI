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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;

/**
 * @author Christophe Labouisse
 * @see org.eclipse.jface.action.Action
 */
public class PrintAction extends GraphControlerAction {

    private static final Log log = LogFactory.getLog(PrintAction.class);

    private static final String DEFAULT_ACTION_NAME = "Print";

    private final GraphWindow window;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        final PrintDialog dialog = new PrintDialog(window.getShell());
        final PrinterData printerData = dialog.open();
        log.debug("Dialog returned " + printerData);
        if (printerData != null) {
            final Printer printer = new Printer(printerData);
            getGraphControler().print(printer);
            printer.dispose();
        }
        /*
         * else { log.error("No printer available, disabling print");
         * setEnabled(false); }
         */
        if (SWT.getPlatform().equals("gtk")) {
            getGraphControler().dotPrint();
        }
    }

    /**
     * Creates a new OpenFileAction object.
     * 
     * @param parent
     */
    public PrintAction(final GraphWindow parent) {
        this(parent,DEFAULT_ACTION_NAME);
    }

    /**
     * Creates a new OpenFileAction object with specific name.
     * 
     * @param name
     * @param parent
     */
    public PrintAction(final GraphWindow parent,final String name) {
        super(parent,name);
        window = parent;
    }

    public int getAccelerator() {
        return SWT.CONTROL | 'P';
    }
}
