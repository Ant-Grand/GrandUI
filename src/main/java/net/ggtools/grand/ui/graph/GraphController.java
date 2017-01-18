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

package net.ggtools.grand.ui.graph;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.ggtools.grand.ant.AntTargetNode;
import net.ggtools.grand.exceptions.GrandException;
import net.ggtools.grand.filters.GraphFilter;
import net.ggtools.grand.graph.Graph;
import net.ggtools.grand.ui.Application;
import net.ggtools.grand.ui.GrandUiPrefStore;
import net.ggtools.grand.ui.RecentFilesManager;
import net.ggtools.grand.ui.event.Dispatcher;
import net.ggtools.grand.ui.event.EventManager;
import net.ggtools.grand.ui.graph.draw2d.Draw2dGraph;
import net.ggtools.grand.ui.graph.draw2d.Draw2dGraphRenderer;
import net.ggtools.grand.ui.graph.draw2d.Draw2dNode;
import net.ggtools.grand.ui.prefs.PreferenceKeys;
import net.ggtools.grand.ui.widgets.ExceptionDialog;
import net.ggtools.grand.ui.widgets.GraphWindow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.PrintFigureOperation;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import sf.jzgraph.IDotGraph;
import sf.jzgraph.dot.impl.Dot;

/**
 * A class responsible of interfacing the Grand graph objects to the GrandUi
 * display.
 *
 * @author Christophe Labouisse
 */
public class GraphController implements DotGraphAttributes, SelectionManager,
        IPropertyChangeListener {
    /**
     * Field log.
     */
    private static final Log LOG = LogFactory.getLog(GraphController.class);

    // FIXME ok that's bad, it'll probably have to go to the Prefs API.
    /**
     * Field printMode.
     */
    private static int printMode = PrintFigureOperation.FIT_PAGE;

    /**
     * @return Returns the printMode.
     */
    public static final int getPrintMode() {
        return printMode;
    }

    /**
     * @param printMode
     *            The printMode to set.
     */
    public static final void setPrintMode(final int printMode) {
        GraphController.printMode = printMode;
    }

    /**
     * Field busRoutingEnabled.
     */
    private boolean busRoutingEnabled;

    /**
     * If <code>true</code> the filter chain will be cleared when the next
     * graph is loaded.
     */
    private boolean clearFiltersOnNextLoad;

    /**
     * Field defaultProgressMonitor.
     */
    private IProgressMonitor defaultProgressMonitor;

    /**
     * Field displayer.
     */
    private GraphDisplayer displayer;

    /**
     * Field figure.
     */
    private Draw2dGraph figure;

    /**
     * Field filterChain.
     */
    private FilterChainModel filterChain;

    /**
     * Field graph.
     */
    private Graph graph;

    /**
     * Field graphEventManager.
     */
    private EventManager graphEventManager;

    /**
     * Field model.
     */
    private GraphModel model;

    /**
     * Field nodeContentProvider.
     */
    private final GraphNodeContentProvider nodeContentProvider;

    /**
     * Field parameterChangedEvent.
     */
    private Dispatcher parameterChangedEvent;

    /**
     * Field renderer.
     */
    private Draw2dGraphRenderer renderer;

    /**
     * Field selectedNodes.
     */
    private final Set<Draw2dNode> selectedNodes = new HashSet<Draw2dNode>();

    /**
     * Field selectionChangedDispatcher.
     */
    private Dispatcher selectionChangedDispatcher;

    /**
     * Field window.
     */
    private GraphWindow window;

    /**
     * Constructor for GraphController.
     * @param window GraphWindow
     */
    public GraphController(final GraphWindow window) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Creating new controller to " + window);
        }
        this.window = window;
        model = new GraphModel();
        filterChain = new FilterChainModel(model);
        // TODO voir si je peux virer le renderer et laisser le graph faire le
        // boulot tout seul.
        renderer = new Draw2dGraphRenderer();

        nodeContentProvider = new GraphNodeContentProvider();

        graphEventManager = new EventManager("Graph Event");
        try {
            selectionChangedDispatcher = graphEventManager.createDispatcher(GraphListener.class
                    .getDeclaredMethod("selectionChanged", Collection.class));
            parameterChangedEvent = graphEventManager.createDispatcher(GraphListener.class
                    .getDeclaredMethod("parameterChanged", GraphController.class));
        } catch (final SecurityException e) {
            LOG.fatal("Caught exception initializing GraphController", e);
            throw new RuntimeException("Cannot instantiate GraphController", e);
        } catch (final NoSuchMethodException e) {
            LOG.fatal("Caught exception initializing GraphController", e);
            throw new RuntimeException("Cannot instantiate GraphController", e);
        }

        clearFiltersOnNextLoad = true; // Conservative.
        final GrandUiPrefStore preferenceStore = Application.getInstance().getPreferenceStore();
        busRoutingEnabled = preferenceStore.getBoolean(PreferenceKeys.GRAPH_BUS_ENABLED_DEFAULT);
        preferenceStore.addPropertyChangeListener(this);
    }

    /**
     * @param filter GraphFilter
     */
    public final void addFilter(final GraphFilter filter) {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        try {
            ModalContext.run(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    LOG.info("Adding filter " + filter);
                    progressMonitor.beginTask("Adding filter", 4);
                    filterChain.addFilterLast(filter);
                    progressMonitor.worked(1);
                    renderFilteredGraph(progressMonitor);
                }
            }, true, progressMonitor, Display.getCurrent());
        } catch (final InvocationTargetException e) {
            reportError("Cannot add filter", e);
        } catch (final InterruptedException e) {
            reportError("Cannot add filter", e);
        } finally {
            progressMonitor.done();
        }
    }

    /**
     * Method addListener.
     * @param listener GraphListener
     * @see net.ggtools.grand.ui.graph.SelectionManager#addListener(net.ggtools.grand.ui.graph.GraphListener)
     */
    public final void addListener(final GraphListener listener) {
        if (graphEventManager != null) {
            graphEventManager.subscribe(listener);
        }
    }

    /**
     * Method clearFilters.
     */
    public final void clearFilters() {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        try {
            ModalContext.run(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    LOG.info("Clearing filters");
                    progressMonitor.beginTask("Clearing filters", 4);
                    filterChain.clearFilters();
                    progressMonitor.worked(1);
                    renderFilteredGraph(progressMonitor);
                }
            }, true, progressMonitor, Display.getCurrent());
        } catch (final InvocationTargetException e) {
            reportError("Cannot clear filters", e);
        } catch (final InterruptedException e) {
            reportError("Cannot clear filters", e);
        } finally {
            progressMonitor.done();
        }

    }

    /**
     * Method deselectAllNodes.
     * @see net.ggtools.grand.ui.graph.SelectionManager#deselectAllNodes()
     */
    public final void deselectAllNodes() {
        if (!selectedNodes.isEmpty()) {
            for (final Draw2dNode currentNode : selectedNodes) {
                currentNode.setSelected(false);
            }
            selectedNodes.clear();
            displayer.setSourceText("");
            selectionChangedDispatcher.dispatch(selectedNodes);
        }
    }

    /**
     * Method deselectNode.
     * @param node Draw2dNode
     * @see net.ggtools.grand.ui.graph.SelectionManager#deselectNode(net.ggtools.grand.ui.graph.draw2d.Draw2dNode)
     */
    public final void deselectNode(final Draw2dNode node) {
        LOG.debug("Deselect node " + node);
        if (node.isSelected()) {
            selectedNodes.remove(node);
            node.setSelected(false);
            selectionChangedDispatcher.dispatch(selectedNodes);
        }
    }

    /**
     * Enable or disable the use of the bus routing algorithm for graph layout.
     *
     * @param enabled boolean
     */
    public final void enableBusRouting(final boolean enabled) {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        if (busRoutingEnabled != enabled) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Using bus routing set to " + enabled);
            }
            busRoutingEnabled = enabled;
            parameterChangedEvent.dispatch(this);
            progressMonitor.beginTask("Rerouting graph", 3);
            renderFilteredGraph(progressMonitor);
            progressMonitor.done();
        }
    }

    /**
     * Focus on a specific target. The current implementation of focusing means
     * bring the specific target as close as possible from the canvas centre.
     *
     * @param targetName
     *            the target to focus on.
     */
    public final void focusOn(final String targetName) {
        if (displayer != null) {
            displayer.jumpToNode(targetName);
        }
    }

    /**
     * @return Returns the dest.
     */
    public final GraphDisplayer getDisplayer() {
        if (displayer == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Opening graph displayer");
            }
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    displayer = window.newDisplayer(GraphController.this);
                }
            });
        }
        return displayer;
    }

    /**
     * Method getGraphProperties.
     * @return Properties
     */
    public final Properties getGraphProperties() {
        if (model != null) {
            return model.getUserProperties();
        } else {
            return null;
        }
    }

    /**
     * Method getNodeContentProvider.
     * @return IStructuredContentProvider
     */
    public final IStructuredContentProvider getNodeContentProvider() {
        return nodeContentProvider;
    }

    /**
     * Method getNodeLabelProvider.
     * @return ILabelProvider
     */
    public final ILabelProvider getNodeLabelProvider() {
        return nodeContentProvider;
    }

    /**
     * @return Returns the progressMonitor.
     */
    public final IProgressMonitor getProgressMonitor() {
        return defaultProgressMonitor;
    }

    /**
     * Method getSelection.
     * @return Collection&lt;Draw2dNode&gt;
     * @see net.ggtools.grand.ui.graph.SelectionManager#getSelection()
     */
    public final Collection<Draw2dNode> getSelection() {
        return selectedNodes;
    }

    /**
     * Method getWindow.
     * @return GraphWindow
     */
    public final GraphWindow getWindow() {
        return window;
    }

    /**
     * @return Returns the busRoutingEnabled.
     */
    public final boolean isBusRoutingEnabled() {
        return busRoutingEnabled;
    }

    /**
     * Opens a new graph.
     *
     * @param file
     *            the file to open.
     * @param properties
     *            a set of properties to be preset when opening the graph or
     *            <code>null</code> if no properties should be preset.
     */
    public final void openFile(final File file, final Properties properties) {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        if (LOG.isInfoEnabled()) {
            LOG.info("Opening " + file);
        }

        progressMonitor.beginTask("Opening new graph", 5);
        clearFiltersOnNextLoad = true;

        try {
            progressMonitor.subTask("Loading ant file");
            model.openFile(file, properties);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Model loaded graph");
            }
            progressMonitor.worked(1);

            filterAndRenderGraph(progressMonitor);
            if (LOG.isInfoEnabled()) {
                LOG.info("Graph loaded & rendered");
            }
            RecentFilesManager.getInstance().addNewFile(file, properties);
        } catch (final GrandException e) {
            reportError("Cannot open graph", e);
            stopController();
        } catch (final BuildException e) {
            reportError("Cannot open graph", e);
            stopController();
        } finally {
            progressMonitor.done();
        }
    }

    /**
     * Open the build file containing a specific node.
     *
     * @param node Draw2dNode
     */
    public final void openNodeFile(final Draw2dNode node) {
        final AntTargetNode targetNode =
                (AntTargetNode) node.getVertex().getData();
        final String buildFile = targetNode.getBuildFile();
        if ((buildFile != null) && (buildFile.length() > 0)) {
            String targetName = targetNode.getName();
            if (targetName != null) {
                // Remove the surrounding [].
                // FIXME add a method to get the real target name in AntTargetNode.
                targetName = targetName.substring(1, targetName.length() - 1);
            }
            window.openGraphInNewDisplayer(new File(buildFile), targetName, null);
        }
    }

    /**
     * Prints the current graph.
     *
     * @param printer Printer
     */
    public final void print(final Printer printer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Printing graph");
        }
        final PrintFigureOperation printOp =
                new PrintFigureOperation(printer, figure);
        printOp.setPrintMode(printMode);
        printOp.run("Grand:" + graph.getName());
    }

    /**
     * Method propertyChange.
     * @param event PropertyChangeEvent
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public final void propertyChange(final PropertyChangeEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get PropertyChangeEvent " + event.getProperty());
        }
        if (event.getProperty().startsWith(PreferenceKeys.GRAPH_PREFIX)) {
            refreshGraph();
        }
    }

    /**
     * Refreshing (i.e.: rerender) the current graph.
     */
    public final void refreshGraph() {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        // FIXME check that there is a graph.
        if (LOG.isInfoEnabled()) {
            LOG.info("Refreshing current graph");
        }
        progressMonitor.beginTask("Refreshing graph", 3);
        clearFiltersOnNextLoad = false;

        try {
            renderFilteredGraph(progressMonitor);
            if (LOG.isInfoEnabled()) {
                LOG.info("Graph refreshed");
            }
        } catch (final BuildException e) {
            reportError("Cannot open graph", e);
        } finally {
            progressMonitor.done();
        }
    }

    /**
     * Reload the current graph.
     */
    public final void reloadGraph() {
        reloadGraph(null);
    }

    /**
     * Method reloadGraph.
     * @param properties Properties
     */
    public final void reloadGraph(final Properties properties) {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        // FIXME check that there is a model.
        if (LOG.isInfoEnabled()) {
            LOG.info("Reloading current graph");
        }
        progressMonitor.beginTask("Reloading graph", 5);
        clearFiltersOnNextLoad = false;

        try {
            model.reload(properties);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Model reloaded graph");
            }
            progressMonitor.worked(1);

            filterAndRenderGraph(progressMonitor);
            if (LOG.isInfoEnabled()) {
                LOG.info("Graph reloaded");
            }
            RecentFilesManager.getInstance().updatePropertiesFor(model.getLastLoadedFile(),
                    properties);
        } catch (final GrandException e) {
            reportError("Cannot reload graph", e);
        } catch (final BuildException e) {
            reportError("Cannot open graph", e);
        } finally {
            progressMonitor.done();
        }
    }

    /**
     * Method removeSelectionListener.
     * @param listener GraphListener
     * @see net.ggtools.grand.ui.graph.SelectionManager#removeSelectionListener(net.ggtools.grand.ui.graph.GraphListener)
     */
    public final void removeSelectionListener(final GraphListener listener) {
        graphEventManager.unSubscribe(listener);
    }

    /**
     * Method selectNode.
     * @param node Draw2dNode
     * @param addToSelection boolean
     * @see net.ggtools.grand.ui.graph.SelectionManager#selectNode(net.ggtools.grand.ui.graph.draw2d.Draw2dNode, boolean)
     */
    public final void selectNode(final Draw2dNode node, final boolean addToSelection) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Select node " + node);
        }
        if (!node.isSelected()) {
            if (!addToSelection) {
                deselectAllNodes();
            }
            selectedNodes.add(node);
            node.setSelected(true);
            displayer.setRichSource(((AntTargetNode) node.getVertex().getData()).getRichSource());
            selectionChangedDispatcher.dispatch(selectedNodes);
        }
    }

    /**
     * Method selectNodeByName.
     * @param nodeName String
     * @param addToSelection boolean
     * @see net.ggtools.grand.ui.graph.SelectionManager#selectNodeByName(java.lang.String, boolean)
     */
    public final void selectNodeByName(final String nodeName, final boolean addToSelection) {
        figure.selectNodeByName(nodeName, addToSelection);
    }

    /**
     * @param progressMonitor
     *            The progressMonitor to set.
     */
    public final void setProgressMonitor(final IProgressMonitor progressMonitor) {
        defaultProgressMonitor = progressMonitor;
    }

    /**
     * Filter the current graph of the model and render it.
     * @param progressMonitor IProgressMonitor
     */
    private void filterAndRenderGraph(final IProgressMonitor progressMonitor) {
        progressMonitor.subTask("Filtering graph");
        if (clearFiltersOnNextLoad) {
            filterChain.clearFilters();
        }
        filterChain.filterGraph();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Filtering done");
        }
        progressMonitor.worked(1);

        renderFilteredGraph(progressMonitor);
    }

    /**
     * Render the currently load/filtered graph. This method increase the
     * progress monitor by 3.
     * @param progressMonitor IProgressMonitor
     */
    private void renderFilteredGraph(final IProgressMonitor progressMonitor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating dot graph");
        }
        progressMonitor.subTask("Laying out graph");
        graph = filterChain.getGraph();
        nodeContentProvider.setGraph(graph);
        final DotGraphCreator creator =
                new DotGraphCreator(graph, busRoutingEnabled);
        final IDotGraph dotGraph = creator.getGraph();
        progressMonitor.worked(1);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Laying out graph");
        }
        final Dot app = new Dot();
        app.layout(dotGraph, 0, -7);
        progressMonitor.worked(1);

        progressMonitor.subTask("Rendering graph");
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                if (figure == null) {
                    figure = renderer.render(dotGraph);
                } else {
                    renderer.render(figure, dotGraph);
                }

            }
        });
        figure.setSelectionManager(this);
        progressMonitor.worked(1);

        String graphName = graph.getName();
        if (graphName == null) {
            graphName = "Untitled";
        }
        getDisplayer().setGraph(figure, graphName,
                model.getLastLoadedFile().getAbsolutePath());
    }

    /**
     * Reports an error in both log and a dialog.
     *
     * @param message String
     * @param e Throwable
     */
    private void reportError(final String message, final Throwable e) {
        LOG.error(message, e);
        ExceptionDialog.openException(window.getShell(), message, e);
    }

    /**
     * Puts the controller in a pre-mortem state where it does not receive or
     * send event.
     */
    private void stopController() {
        // Stop sending & receiving events.
        graphEventManager.clear();
        Application.getInstance().getPreferenceStore().removePropertyChangeListener(this);

        // Help garbage collector.
        window = null;
        model = null;
        filterChain = null;
        renderer = null;
        graphEventManager = null;
        selectionChangedDispatcher = null;
        parameterChangedEvent = null;
    }

    /**
     * Creates new {@link Image} for the current graph. As new image will be
     * created for each call the caller is responsible for calling
     * <code>dispose()</code> on the returned image.
     *
     * @return a new image or <code>null</code> if no graph is loaded. This
     *         image should be disposed after use.
     */
    public final Image createImageForGraph() {
        if (figure == null) {
            return null;
        }
        final Display display = window.getShell().getDisplay();
        final Rectangle r = figure.getBounds();
        final Image image = new Image(display, r.width, r.height);

        // Fill the image up.
        display.syncExec(new Runnable() {
            public void run() {
                GC gc = null;
                SWTGraphics g = null;
                try {
                    gc = new GC(image);
                    g = new SWTGraphics(gc);
                    g.translate(r.x * -1, r.y * -1);
                    g.setForegroundColor(figure.getForegroundColor());
                    g.setBackgroundColor(figure.getBackgroundColor());
                    g.setFont(figure.getFont());
                    figure.paint(g);
                } finally {
                    if (g != null) {
                        g.dispose();
                    }
                    if (gc != null) {
                        gc.dispose();
                    }
                }
            }
        });

        return image;
    }
}
