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

package net.ggtools.grand.ui.widgets;

import net.ggtools.grand.ui.AppData;
import net.ggtools.grand.ui.graph.GraphControler;
import net.ggtools.grand.ui.graph.GraphControlerProvider;
import net.ggtools.grand.ui.graph.GraphDisplayer;
import net.ggtools.grand.ui.menu.FileMenuManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Christophe Labouisse
 */
public class GraphWindow extends ApplicationWindow implements GraphControlerProvider {

    private static final Log log = LogFactory.getLog(GraphWindow.class);

    private Display display;

    private MenuManager manager;

    private CTabFolder tabFolder;

    public GraphWindow() {
        this(null);
    }

    public GraphWindow(Shell parent) {
        super(parent);
        setBlockOnOpen(true);
        addStatusLine();
        addMenuBar();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String,
     *      int)
     */
    public void beginTask(final String name, final int totalWork) {
        final StatusLineManager slManager = getStatusLineManager();
        final IProgressMonitor monitor = slManager.getProgressMonitor();
        display.asyncExec(new Runnable() {

            public void run() {
                monitor.beginTask(name, totalWork);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#done()
     */
    public void done() {
        final StatusLineManager slManager = getStatusLineManager();
        final IProgressMonitor monitor = slManager.getProgressMonitor();
        display.asyncExec(new Runnable() {

            public void run() {
                monitor.done();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
     */
    public void subTask(final String name) {
        final StatusLineManager slManager = getStatusLineManager();
        final IProgressMonitor monitor = slManager.getProgressMonitor();
        display.asyncExec(new Runnable() {

            public void run() {
                monitor.subTask(name);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
     */
    public void worked(final int work) {
        final StatusLineManager slManager = getStatusLineManager();
        final IProgressMonitor monitor = slManager.getProgressMonitor();
        display.asyncExec(new Runnable() {

            public void run() {
                monitor.worked(work);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        // We can load the resources since the display is initialized.
        AppData.getInstance().initResources();
        shell.setText("Grand");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        log.debug("Creating contents");
        tabFolder = new CTabFolder(parent, SWT.BORDER | SWT.TOP);
        //tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter());
        display = parent.getDisplay();
        return tabFolder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.ApplicationWindow#createMenuManager()
     */
    protected MenuManager createMenuManager() {
        log.debug("Creating menu manager");
        manager = new MenuManager();
        manager.add(new FileMenuManager(this));
        //manager.add(new GraphMenu(this));
        manager.setVisible(true);
        return manager;
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphControlerProvider#getControler()
     */
    public GraphControler getControler() {
        final GraphTabItem selectedTab = (GraphTabItem) tabFolder.getSelection();
        if (selectedTab == null) {
            return null;
        }
        else {
            return selectedTab.getControler();
        }
    }

    /**
     * @return
     */
    public GraphDisplayer newDisplayer() {
        final GraphTabItem graphTabItem = new GraphTabItem(tabFolder,SWT.CLOSE);
        tabFolder.setSelection(graphTabItem);
        return graphTabItem;
    }
}