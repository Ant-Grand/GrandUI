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
package net.ggtools.grand.ui.log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Quick 'n dirty class to load a saved log from disk and display it in a log
 * viewer.
 *
 * @author Christophe Labouisse
 */
public class Analyzer extends ApplicationWindow {

    /**
     * Field logViewer.
     */
    private LogViewer logViewer;

    /**
     *
     */
    public Analyzer() {
        super(null);
        addMenuBar();
    }

    /**
     * Method configureShell.
     * @param shell Shell
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected final void configureShell(final Shell shell) {
        super.configureShell(shell);
        shell.setText("ggTools log analyzer");
    }

    /**
     * Method createMenuManager.
     * @return MenuManager
     * @see org.eclipse.jface.window.ApplicationWindow#createMenuManager()
     */
    @Override
    protected final MenuManager createMenuManager() {
        final MenuManager manager = new MenuManager();
        final MenuManager fileMenu = new MenuManager("File");
        manager.add(fileMenu);
        fileMenu.add(new Action("Load") {
            @Override
            public int getAccelerator() {
                return (SWT.getPlatform().equals("cocoa") ? SWT.MOD1 : SWT.CONTROL) | 'L';
            }

            @Override
            public void run() {
                if (logViewer != null) {
                    final FileDialog dialog =
                            new FileDialog(getShell(), SWT.NONE);
                    dialog.setFilterExtensions(new String[]{"*.glg", "*.log", "*"});
                    final String logFileName = dialog.open();
                    if (logFileName != null) {
                        ObjectInputStream ois = null;
                        try {
                            ois = new ObjectInputStream(new FileInputStream(logFileName));
                            logViewer.setLogBuffer((LogEventBuffer) ois.readObject());
                        } catch (final ClassNotFoundException | IOException e) {
                            // TODO auto-generated catch block
                            e.printStackTrace();
                        } finally {
                            if (ois != null) {
                                try {
                                    ois.close();
                                } catch (final IOException exception) {
                                    throw new RuntimeException("Cannot close " + logFileName,
                                            exception);
                                }
                            }
                        }
                    }

                }
            }
        });

        fileMenu.add(new Action("Quit") {
            @Override
            public int getAccelerator() {
                return (SWT.getPlatform().equals("cocoa") ? SWT.MOD1 : SWT.CONTROL) | 'Q';
            }

            @Override
            public void run() {
                System.exit(0);
            }
        });
        manager.setVisible(true);
        return manager;
    }

    /**
     * Method createContents.
     * @param parent Composite
     * @return Control
     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected final Control createContents(final Composite parent) {
        // Allow the LogViewer to display trace & debug events.
        logViewer = new LogViewer(parent, SWT.NONE);
        return logViewer;
    }

    /**
     * Method main.
     * @param args String[]
     */
    public static void main(final String[] args) {
        final Analyzer analyzer = new Analyzer();
        analyzer.setBlockOnOpen(true);
        analyzer.open();
        System.exit(0);
    }

}
