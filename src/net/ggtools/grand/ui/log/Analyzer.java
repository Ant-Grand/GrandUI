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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
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

    private LogViewer logViewer;

    /**
     * 
     */
    public Analyzer() {
        super(null);
        addMenuBar();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("ggTools log analyzer");
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.window.ApplicationWindow#createMenuManager()
     */
    protected MenuManager createMenuManager() {
        final MenuManager manager = new MenuManager();
        final MenuManager fileMenu = new MenuManager("File");
        manager.add(fileMenu);
        fileMenu.add(new Action("Load") {
            public int getAccelerator() {
                return SWT.CONTROL | 'L';
            }

            public void run() {
                if (logViewer != null) {
                    final FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
                    dialog.setFilterExtensions(new String[]{"*.glg", "*.log", "*"});
                    final String logFileName = dialog.open();
                    if (logFileName != null) {
                        ObjectInputStream ois = null;
                        try {
                            ois = new ObjectInputStream(new FileInputStream(logFileName));
                            logViewer.setLogBuffer((LogEventBuffer) ois.readObject());
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } finally {
                            if (ois != null)
                                try {
                                    ois.close();
                                } catch (IOException exception) {
                                    throw new RuntimeException("Cannot close " + logFileName,
                                            exception);
                                }
                        }
                    }

                }
            }
        });

        fileMenu.add(new Action("Quit") {
            public int getAccelerator() {
                return SWT.CONTROL | 'Q';
            }

            public void run() {
                System.exit(0);
            }
        });
        manager.setVisible(true);
        return manager;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        // Allow the LogViewer to display trace & debug events.
        logViewer = new LogViewer(parent, SWT.NONE) {
            /**
             * @param comboIndex
             * @return
             */
            protected int comboIndexToLogLevel(int comboIndex) {
                return comboIndex + LogEvent.TRACE.value;
            }

            /**
             * Add value to the level selection combo.
             * @param combo
             */
            protected void fillUpLevelCombo(Combo combo) {
                combo.add(LogEvent.TRACE.name);
                combo.add(LogEvent.DEBUG.name);
                combo.add(LogEvent.INFO.name);
                combo.add(LogEvent.WARNING.name);
                combo.add(LogEvent.ERROR.name);
                combo.add(LogEvent.FATAL.name);
                combo.select(0);
            }
        };
        return logViewer;
    }

    public static void main(String[] args) {
        Analyzer analyzer = new Analyzer();
        analyzer.setBlockOnOpen(true);
        analyzer.open();
    }

}
