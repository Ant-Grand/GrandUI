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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import net.ggtools.grand.ui.event.Dispatcher;
import net.ggtools.grand.ui.event.EventManager;
import net.ggtools.grand.ui.graph.GraphControler;
import net.ggtools.grand.ui.graph.GraphControlerListener;
import net.ggtools.grand.ui.graph.GraphControlerProvider;
import net.ggtools.grand.ui.graph.GraphDisplayer;
import net.ggtools.grand.ui.menu.FileMenuManager;
import net.ggtools.grand.ui.menu.GraphMenu;
import net.ggtools.grand.ui.menu.HelpMenu;
import net.ggtools.grand.ui.menu.ViewMenu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Christophe Labouisse
 */
public class GraphWindow extends ApplicationWindow implements GraphControlerProvider {

    private static final Log log = LogFactory.getLog(GraphWindow.class);

    private final Dispatcher controlerAvailableDispatcher;

    private final EventManager controlerEventManager;

    private final Dispatcher controlerRemovedDispatcher;

    private Display display;

    private MenuManager manager;

    private boolean outlinePanelVisible = true;

    private boolean sourcePanelVisible = true;

    private CTabFolder tabFolder;

    public GraphWindow() {
        this(null);
    }

    public GraphWindow(Shell parent) {
        super(parent);
        log.debug("Creating new GraphWindow");

        controlerEventManager = new EventManager("GraphControler Availability Event");
        try {
            controlerAvailableDispatcher = controlerEventManager
                    .createDispatcher(GraphControlerListener.class.getDeclaredMethod(
                            "controlerAvailable", new Class[]{GraphControler.class}));
            controlerRemovedDispatcher = controlerEventManager
                    .createDispatcher(GraphControlerListener.class.getDeclaredMethod(
                            "controlerRemoved", new Class[]{GraphControler.class}));
        } catch (SecurityException e) {
            log.fatal("Caught exception initializing GraphControler", e);
            throw new RuntimeException("Cannot instanciate GraphControler", e);
        } catch (NoSuchMethodException e) {
            log.fatal("Caught exception initializing GraphControler", e);
            throw new RuntimeException("Cannot instanciate GraphControler", e);
        }

        addStatusLine();
        addMenuBar();
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphControlerProvider#addControlerListener(net.ggtools.grand.ui.graph.GraphControlerListener)
     */
    public void addControlerListener(GraphControlerListener listener) {
        controlerEventManager.subscribe(listener);
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphControlerProvider#getControler()
     */
    public GraphControler getControler() {
        if (tabFolder != null) {
            final GraphTabItem selectedTab = (GraphTabItem) tabFolder.getSelection();
            if (selectedTab == null) {
                return null;
            }
            else {
                return selectedTab.getControler();
            }
        }
        return null;
    }

    /**
     * Create a new displayer for a controler.
     * 
     * @param controler
     * @return
     */
    public GraphDisplayer newDisplayer(final GraphControler controler) {
        final GraphTabItem graphTabItem = new GraphTabItem(tabFolder, SWT.CLOSE, controler);
        graphTabItem.setSourcePanelVisible(sourcePanelVisible);
        tabFolder.setSelection(graphTabItem);
        controlerAvailableDispatcher.dispatch(controler);
        controler.setProgressMonitor(new SafeProgressMonitor(getStatusLineManager()
                .getProgressMonitor(), display));
        return graphTabItem;
    }

    /**
     * Open an ant file in a new window.
     * 
     * @param buildFile
     */
    public void openGraphInNewDisplayer(final File buildFile) {
        openGraphInNewDisplayer(buildFile, null);
    }

    /**
     * Open an ant file in a new window, focusing on a specific target.
     * 
     * @param buildFile
     * @param targetName
     *            the target to scroll to or <code>null</code> not to focus on
     *            any target.
     */
    public void openGraphInNewDisplayer(final File buildFile, final String targetName) {
        final GraphControler controler = new GraphControler(this);
        try {
            new ProgressMonitorDialog(getShell()).run(true, false, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException,
                        InterruptedException {
                    controler.setProgressMonitor(monitor);
                    controler.openFile(buildFile, true);
                    if (targetName != null) {
                        controler.focusOn(targetName);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            log.error("Caugh exception opening file", e);
        } catch (InterruptedException e) {
            log.info("Loading cancelled", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphControlerProvider#removeControlerListener(net.ggtools.grand.ui.graph.GraphControlerListener)
     */
    public void removeControlerListener(GraphControlerListener listener) {
        controlerEventManager.unSubscribe(listener);
    }

    /**
     * @param outlinePanelVisible
     *            The outlinePanelVisible to set.
     */
    public final void setOutlinePanelVisible(boolean outlinePanelVisible) {
        if (outlinePanelVisible != this.outlinePanelVisible) {
            this.outlinePanelVisible = outlinePanelVisible;
        }
    }

    /**
     * @param sourcePanelVisible
     *            The sourcePanelVisible to set.
     */
    public final void setSourcePanelVisible(boolean sourcePanelVisible) {
        if (sourcePanelVisible != this.sourcePanelVisible) {
            this.sourcePanelVisible = sourcePanelVisible;
            final CTabItem[] children = tabFolder.getItems();
            for (int i = 0; i < children.length; i++) {
                final CTabItem current = children[i];
                if (current instanceof GraphTabItem) {
                    final GraphTabItem tab = (GraphTabItem) current;
                    tab.setSourcePanelVisible(sourcePanelVisible);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Grand");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        if (log.isDebugEnabled()) log.debug("Creating contents");
        tabFolder = new CTabFolder(parent, SWT.BORDER | SWT.TOP);
        tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.custom.CTabFolder2Adapter#close(org.eclipse.swt.custom.CTabFolderEvent)
             */
            public void close(CTabFolderEvent event) {
                log.debug("Got " + event);
                final Widget item = event.item;
                if (item instanceof GraphTabItem) {
                    controlerRemovedDispatcher.dispatch(((GraphTabItem) item).getControler());
                }
            }
        });
        tabFolder.addSelectionListener(new SelectionAdapter() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                log.debug("Got " + e);
                final Widget widget = e.widget;
                if (widget instanceof CTabFolder) {
                    final CTabFolder folder = (CTabFolder) widget;
                    final CTabItem selection = folder.getSelection();
                    if (selection instanceof GraphTabItem) {
                        controlerAvailableDispatcher.dispatch(((GraphTabItem) selection)
                                .getControler());
                    }
                }
            }
        });
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
        manager.add(new ViewMenu(this));
        manager.add(new GraphMenu(this));
        manager.add(new HelpMenu(this));
        manager.setVisible(true);
        return manager;
    }
}