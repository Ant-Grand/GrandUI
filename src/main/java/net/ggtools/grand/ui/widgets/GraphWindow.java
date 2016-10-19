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
import java.util.Properties;

import net.ggtools.grand.ui.actions.AboutAction;
import net.ggtools.grand.ui.actions.PreferenceAction;
import net.ggtools.grand.ui.event.Dispatcher;
import net.ggtools.grand.ui.event.EventManager;
import net.ggtools.grand.ui.graph.GraphController;
import net.ggtools.grand.ui.graph.GraphControllerListener;
import net.ggtools.grand.ui.graph.GraphControllerProvider;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Christophe Labouisse
 */
public class GraphWindow extends ApplicationWindow
        implements GraphControllerProvider {

    /**
     * Field log.
     */
    private static final Log LOG = LogFactory.getLog(GraphWindow.class);

    /**
     * Field controllerAvailableDispatcher.
     */
    private final Dispatcher controllerAvailableDispatcher;

    /**
     * Field controllerEventManager.
     */
    private final EventManager controllerEventManager;

    /**
     * Field controllerRemovedDispatcher.
     */
    private final Dispatcher controllerRemovedDispatcher;

    /**
     * Field display.
     */
    private Display display;

    /**
     * Field manager.
     */
    private MenuManager manager;

    /**
     * Field outlinePanelVisible.
     */
    private boolean outlinePanelVisible = false;

    /**
     * Field sourcePanelVisible.
     */
    private boolean sourcePanelVisible = true;

    /**
     * Field tabFolder.
     */
    private CTabFolder tabFolder;

    /**
     * Constructor for GraphWindow.
     */
    public GraphWindow() {
        this(null);
    }

    /**
     * Constructor for GraphWindow.
     * @param parent Shell
     */
    public GraphWindow(final Shell parent) {
        super(parent);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new GraphWindow");
        }

        controllerEventManager = new EventManager("GraphController Availability Event");
        try {
            controllerAvailableDispatcher = controllerEventManager
                    .createDispatcher(GraphControllerListener.class.getDeclaredMethod(
                            "controllerAvailable", GraphController.class));
            controllerRemovedDispatcher = controllerEventManager
                    .createDispatcher(GraphControllerListener.class.getDeclaredMethod(
                            "controllerRemoved", GraphController.class));
        } catch (final SecurityException e) {
            LOG.fatal("Caught exception initializing GraphController", e);
            throw new RuntimeException("Cannot instantiate GraphController", e);
        } catch (final NoSuchMethodException e) {
            LOG.fatal("Caught exception initializing GraphController", e);
            throw new RuntimeException("Cannot instantiate GraphController", e);
        }

        addStatusLine();
        addMenuBar();
        if (SWT.getPlatform().equals("cocoa")) {
            Menu systemMenu = Display.getDefault().getSystemMenu();

            for (MenuItem systemItem : systemMenu.getItems())
            {
                if (systemItem.getID() == SWT.ID_PREFERENCES)
                {
                    systemItem.addListener(SWT.Selection, new Listener()
                    {
                        public void handleEvent(final Event event)
                        {
                            runPreferencesAction();
                        }
                    });
                }
                if (systemItem.getID() == SWT.ID_ABOUT)
                {
                    systemItem.addListener(SWT.Selection, new Listener()
                    {
                        public void handleEvent(final Event event)
                        {
                            runAboutAction();
                        }
                    });
                }
            }
        }
    }

    /**
     * Method runPreferencesAction.
     */
    private void runPreferencesAction() {
        PreferenceAction preferenceAction = new PreferenceAction(this);
        preferenceAction.run();
    }

    /**
     * Method runAboutAction.
     */
    private void runAboutAction() {
        AboutAction aboutAction = new AboutAction(this);
        aboutAction.run();
    }

    /**
     * Method addControllerListener.
     * @param listener GraphControllerListener
     * @see GraphControllerProvider#addControllerListener(GraphControllerListener)
     */
    public final void addControllerListener(final GraphControllerListener listener) {
        controllerEventManager.subscribe(listener);
    }

    /**
     * Method getController.
     * @return GraphController
     * @see GraphControllerProvider#getController()
     */
    public final GraphController getController() {
        if (tabFolder != null) {
            final GraphTabItem selectedTab = (GraphTabItem) tabFolder.getSelection();
            if (selectedTab == null) {
                return null;
            } else {
                return selectedTab.getController();
            }
        }
        return null;
    }

    /**
     * Create a new displayer for a controller.
     *
     * @param controller GraphController
     * @return GraphDisplayer
     */
    public final GraphDisplayer newDisplayer(final GraphController controller) {
        final GraphTabItem graphTabItem = new GraphTabItem(tabFolder, SWT.CLOSE, controller);
        graphTabItem.setSourcePanelVisible(sourcePanelVisible);
        graphTabItem.setOutlinePanelVisible(outlinePanelVisible);
        tabFolder.setSelection(graphTabItem);
        controllerAvailableDispatcher.dispatch(controller);
        controller.setProgressMonitor(new SafeProgressMonitor(getStatusLineManager()
                .getProgressMonitor(), display));
        return graphTabItem;
    }

    /**
     * Open an ant file in a new window.
     * @param buildFile File
     * @param properties
     *            a set of properties to be preset when opening the graph or
     *            <code>null</code> if no properties should be preset.
     */
    public final void openGraphInNewDisplayer(final File buildFile,
            final Properties properties) {
        openGraphInNewDisplayer(buildFile, null, properties);
    }

    /**
     * Open an ant file in a new window, focusing on a specific target.
     * @param buildFile File
     * @param targetName
     *            the target to scroll to or <code>null</code> not to focus on
     *            any target.
     * @param properties
     *            a set of properties to be preset when opening the graph or
     *            <code>null</code> if no properties should be preset.
     */
    public final void openGraphInNewDisplayer(final File buildFile,
            final String targetName, final Properties properties) {
        final GraphController controller = new GraphController(this);
        try {
            new ProgressMonitorDialog(getShell()).run(true, false,
                    new IRunnableWithProgress() {
                            public void run(final IProgressMonitor monitor)
                                    throws InvocationTargetException,
                                            InterruptedException {
                    controller.setProgressMonitor(monitor);
                    controller.openFile(buildFile, properties);
                    if (targetName != null) {
                        controller.focusOn(targetName);
                    }
                }
            });
        } catch (final InvocationTargetException e) {
            LOG.error("Caught exception opening file", e);
        } catch (final InterruptedException e) {
            LOG.info("Loading cancelled", e);
        }
    }

    /**
     * Method removeControllerListener.
     * @param listener GraphControllerListener
     * @see GraphControllerProvider#removeControllerListener(GraphControllerListener)
     */
    public final void removeControllerListener(final GraphControllerListener listener) {
        controllerEventManager.unSubscribe(listener);
    }

    /**
     * @param outlinePanelVisible
     *            The outlinePanelVisible to set.
     */
    public final void setOutlinePanelVisible(final boolean outlinePanelVisible) {
        if (outlinePanelVisible != this.outlinePanelVisible) {
            this.outlinePanelVisible = outlinePanelVisible;
            final CTabItem[] children = tabFolder.getItems();
            for (final CTabItem current : children) {
                if (current instanceof GraphTabItem) {
                    final GraphTabItem tab = (GraphTabItem) current;
                    tab.setOutlinePanelVisible(outlinePanelVisible);
                }
            }
        }
    }

    /**
     * @param sourcePanelVisible
     *            The sourcePanelVisible to set.
     */
    public final void setSourcePanelVisible(final boolean sourcePanelVisible) {
        if (sourcePanelVisible != this.sourcePanelVisible) {
            this.sourcePanelVisible = sourcePanelVisible;
            final CTabItem[] children = tabFolder.getItems();
            for (final CTabItem current : children) {
                if (current instanceof GraphTabItem) {
                    final GraphTabItem tab = (GraphTabItem) current;
                    tab.setSourcePanelVisible(sourcePanelVisible);
                }
            }
        }
    }

    /**
     * Method configureShell.
     * @param shell Shell
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected final void configureShell(final Shell shell) {
        super.configureShell(shell);
        shell.setText("Grand");
    }

    /**
     * Method createContents.
     * @param parent Composite
     * @return Control
     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected final Control createContents(final Composite parent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating contents");
        }
        tabFolder = new CTabFolder(parent, SWT.BORDER | SWT.TOP);
        tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.custom.CTabFolder2Adapter#close(org.eclipse.swt.custom.CTabFolderEvent)
             */
            @Override
            public void close(final CTabFolderEvent event) {
                LOG.debug("Got " + event);
                final Widget item = event.item;
                if (item instanceof GraphTabItem) {
                    controllerRemovedDispatcher.dispatch(((GraphTabItem) item).getController());
                }
            }
        });
        tabFolder.addSelectionListener(new SelectionAdapter() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(final SelectionEvent e) {
                LOG.debug("Got " + e);
                final Widget widget = e.widget;
                if (widget instanceof CTabFolder) {
                    final CTabFolder folder = (CTabFolder) widget;
                    final CTabItem selection = folder.getSelection();
                    if (selection instanceof GraphTabItem) {
                        controllerAvailableDispatcher.dispatch(((GraphTabItem) selection)
                                .getController());
                    }
                }
            }
        });
        display = parent.getDisplay();
        return tabFolder;
    }

    /**
     * Method createMenuManager.
     * @return MenuManager
     * @see org.eclipse.jface.window.ApplicationWindow#createMenuManager()
     */
    @Override
    protected final MenuManager createMenuManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating menu manager");
        }
        manager = new MenuManager();
        manager.add(new FileMenuManager(this));
        manager.add(new ViewMenu(this));
        manager.add(new GraphMenu(this));
        manager.add(new HelpMenu(this));
        manager.setVisible(true);
        return manager;
    }

    /**
     * @return Returns the outlinePanelVisible.
     */
    public final boolean isOutlinePanelVisible() {
        return outlinePanelVisible;
    }

    /**
     * @return Returns the sourcePanelVisible.
     */
    public final boolean isSourcePanelVisible() {
        return sourcePanelVisible;
    }
}
