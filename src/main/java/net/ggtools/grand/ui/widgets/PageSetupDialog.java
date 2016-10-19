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

import net.ggtools.grand.ui.graph.GraphController;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * TODO the whole thing is crappy, change it when the Prefs API will be there.
 * @author Christophe Labouisse
 */
public class PageSetupDialog extends Dialog {

    /**
     * Field combo.
     */
    private Combo combo;

    /**
     * @param parentShell Shell
     */
    public PageSetupDialog(final Shell parentShell) {
        super(parentShell);
    }

    /**
     * Method cancelPressed.
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    protected final void cancelPressed() {
        super.cancelPressed();
    }

    /**
     * Method createDialogArea.
     * @param parent Composite
     * @return Control
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected final Control createDialogArea(final Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        final GridLayout layout = new GridLayout();
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.numColumns = 2;
        composite.setLayout(layout);
        final Label label = new Label(composite, SWT.NONE);
        label.setText("Print mode: ");
        label.setAlignment(SWT.RIGHT);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setItems(new String[]{"Tile", "Fit on one page",
                "Fit on one page horizontally", "Fit one page vertically"});
        combo.select(GraphController.getPrintMode() - 1);
        combo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                        | GridData.GRAB_HORIZONTAL));
        return composite;
    }

    /**
     * Method okPressed.
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected final void okPressed() {
        GraphController.setPrintMode(combo.getSelectionIndex() + 1);
        super.okPressed();
    }

    /**
     * Method configureShell.
     * @param newShell Shell
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected final void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Page Setup");
    }
}
