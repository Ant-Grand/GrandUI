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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Christophe Labouisse
 */
public class Splash {
    private final Display display;

    private final Image image;

    private final Shell shell;

    public Splash(final Display display, final String versionString) {
        this.display = display;
        shell = new Shell(display, SWT.NO_TRIM | SWT.NO_BACKGROUND | SWT.ON_TOP);
        shell.setLayout(new FillLayout());
        image = new Image(display, getClass().getResourceAsStream(
                "/net/ggtools/grand/ui/resource/splash.png"));
        final Label label = new Label(shell, SWT.NONE);
        label.setImage(image);
        final Rectangle displayBounds = display.getPrimaryMonitor().getBounds();
        final Rectangle imageBounds = image.getBounds();
        final GC gc = new GC(image);
        gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        final Point size = gc.stringExtent(versionString);
        gc.drawText(versionString, 210, imageBounds.height - size.y - 10, true);
        gc.dispose();
        shell.setBounds(displayBounds.x + ((displayBounds.width - imageBounds.width) / 2),
                displayBounds.y + ((displayBounds.height - imageBounds.height) / 2),
                imageBounds.width, imageBounds.height);
        // shell.addPaintListener(this);
    }

    public void close() {
        shell.close();
    }

    public void dispose() {
        shell.dispose();
        image.dispose();
    }

    public void open() {
        shell.open();
    }
}
