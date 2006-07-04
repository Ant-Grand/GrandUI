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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.ggtools.grand.ant.AntTargetNode;
import net.ggtools.grand.exceptions.GrandException;
import net.ggtools.grand.filters.GraphFilter;
import net.ggtools.grand.graph.Graph;
import net.ggtools.grand.output.DotWriter;
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
import org.eclipse.jface.dialogs.MessageDialog;
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
public class GraphControler implements DotGraphAttributes, SelectionManager,
        IPropertyChangeListener {
    private static final Log log = LogFactory.getLog(GraphControler.class);

    // FIXME: Ok that's bad it'll probably have to go to the forthcoming pref
    // API.
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
        GraphControler.printMode = printMode;
    }

    private boolean busRoutingEnabled;

    /**
     * If <code>true</code> the filter chain will be cleared when the next
     * graph is loaded.
     */
    private boolean clearFiltersOnNextLoad;

    private IProgressMonitor defaultProgressMonitor;

    private GraphDisplayer displayer;

    private Draw2dGraph figure;

    private FilterChainModel filterChain;

    private Graph graph;

    private EventManager graphEventManager;

    private GraphModel model;

    private final GraphNodeContentProvider nodeContentProvider;

    private Dispatcher parameterChangedEvent;

    private Draw2dGraphRenderer renderer;

    private final Set<Draw2dNode> selectedNodes = new HashSet<Draw2dNode>();

    private Dispatcher selectionChangedDispatcher;

    private GraphWindow window;

    public GraphControler(final GraphWindow window) {
        if (log.isInfoEnabled()) {
            log.info("Creating new controler to " + window);
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
                    .getDeclaredMethod("selectionChanged", new Class[]{Collection.class}));
            parameterChangedEvent = graphEventManager.createDispatcher(GraphListener.class
                    .getDeclaredMethod("parameterChanged", new Class[]{GraphControler.class}));
        } catch (final SecurityException e) {
            log.fatal("Caught exception initializing GraphControler", e);
            throw new RuntimeException("Cannot instanciate GraphControler", e);
        } catch (final NoSuchMethodException e) {
            log.fatal("Caught exception initializing GraphControler", e);
            throw new RuntimeException("Cannot instanciate GraphControler", e);
        }

        clearFiltersOnNextLoad = true; // Conservative.
        final GrandUiPrefStore preferenceStore = Application.getInstance().getPreferenceStore();
        busRoutingEnabled = preferenceStore.getBoolean(PreferenceKeys.GRAPH_BUS_ENABLED_DEFAULT);
        preferenceStore.addPropertyChangeListener(this);
    }

    /**
     * @param filter
     */
    public void addFilter(final GraphFilter filter) {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        try {
            ModalContext.run(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException,
                        InterruptedException {
                    log.info("Adding filter " + filter);
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

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#addSelectionListener(net.ggtools.grand.ui.graph.GraphListener)
     */
    public void addListener(final GraphListener listener) {
        if (graphEventManager != null) {
            graphEventManager.subscribe(listener);
        }
    }

    public void clearFilters() {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        try {
            ModalContext.run(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException,
                        InterruptedException {
                    log.info("Clearing filters");
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

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#deselectAllNodes()
     */
    public void deselectAllNodes() {
        if (!selectedNodes.isEmpty()) {
            for (final Iterator<Draw2dNode> iter = selectedNodes.iterator(); iter.hasNext();) {
                final Draw2dNode currentNode = iter.next();
                currentNode.setSelected(false);
            }
            selectedNodes.clear();
            displayer.setSourceText("");
            selectionChangedDispatcher.dispatch(selectedNodes);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#deselectNode(net.ggtools.grand.ui.graph.Draw2dNode)
     */
    public void deselectNode(final Draw2dNode node) {
        log.debug("Deselect node " + node);
        if (node.isSelected()) {
            selectedNodes.remove(node);
            node.setSelected(false);
            selectionChangedDispatcher.dispatch(selectedNodes);
        }
    }

    /**
     * Hack for gtk: print using the dot command.
     */
    public void dotPrint() {
        if (log.isDebugEnabled()) {
            log.debug("Printing graph using dot");
        }
        final Properties props = new Properties();
        props.setProperty("dot.graph.attributes", "rankdir=\"TB\"");
        String dotParameters;
        switch (printMode) {
        case PrintFigureOperation.FIT_WIDTH:
            dotParameters = "-Gpage=8,11 -Gsize=10,65536 -Grotate=90 -Gmargin=0.45";
            break;

        case PrintFigureOperation.FIT_HEIGHT:
            dotParameters = "-Gpage=8,11 -Gsize=65536,7 -Grotate=90 -Gmargin=0.45";
            break;

        case PrintFigureOperation.FIT_PAGE:
            dotParameters = "-Gpage=8,11 -Gsize=10,7 -Grotate=90 -Gmargin=0.45";
            break;

        default:
            dotParameters = "-Gpage=8,11 -Grotate=90";
            break;
        }
        try {
            final DotWriter dotWriter = new DotWriter(props);
            dotWriter.setProducer(filterChain);
            dotWriter.setShowGraphName(true);
            dotWriter.write(new File("GrandDotPrint.dot"));
            final Process proc = Runtime.getRuntime().exec(
                    "dot -Tps " + dotParameters + " -o GrandDotPrint.ps GrandDotPrint.dot");
            proc.waitFor();
            proc.destroy();
            log.info("Graph printed to GrandDotPrint.ps");
            final MessageDialog dialog = new MessageDialog(window.getShell(), "Graph printed",
                    Application.getInstance().getImage(Application.APPLICATION_ICON),
                    "Graph saved as GraphDotPrint.ps", MessageDialog.INFORMATION,
                    new String[]{"OK"}, 0);
            dialog.open();
        } catch (final Exception e) {
            log.error("Got execption printing", e);
        }
    }

    /**
     * Enable or disable the use of the bus routing algorithm for graph layout.
     * 
     * @param enabled
     */
    public void enableBusRouting(final boolean enabled) {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        if (busRoutingEnabled != enabled) {
            if (log.isInfoEnabled()) {
                log.info("Using bus routing set to " + enabled);
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
    public void focusOn(final String targetName) {
        if (displayer != null) {
            displayer.jumpToNode(targetName);
        }
    }

    /**
     * @return Returns the dest.
     */
    public final GraphDisplayer getDisplayer() {
        if (displayer == null) {
            if (log.isInfoEnabled()) {
                log.info("Opening graph displayer");
            }
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    displayer = window.newDisplayer(GraphControler.this);
                }
            });
        }
        return displayer;
    }

    public Map getGraphProperties() {
        if (model != null) {
            return model.getUserProperties();
        }
        else {
            return null;
        }
    }

    public IStructuredContentProvider getNodeContentProvider() {
        return nodeContentProvider;
    }

    public ILabelProvider getNodeLabelProvider() {
        return nodeContentProvider;
    }

    /**
     * @return Returns the progressMonitor.
     */
    public final IProgressMonitor getProgressMonitor() {
        return defaultProgressMonitor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#getSelection()
     */
    public Collection<Draw2dNode> getSelection() {
        return selectedNodes;
    }

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
    public void openFile(final File file, final Properties properties) {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        if (log.isInfoEnabled()) {
            log.info("Opening " + file);
        }

        progressMonitor.beginTask("Opening new graph", 5);
        clearFiltersOnNextLoad = true;

        try {
            progressMonitor.subTask("Loading ant file");
            model.openFile(file, properties);
            if (log.isDebugEnabled()) {
                log.debug("Model loaded graph");
            }
            progressMonitor.worked(1);

            filterAndRenderGraph(progressMonitor);
            if (log.isInfoEnabled()) {
                log.info("Graph loaded & rendered");
            }
            RecentFilesManager.getInstance().addNewFile(file, properties);
        } catch (final GrandException e) {
            reportError("Cannot open graph", e);
            stopControler();
        } catch (final BuildException e) {
            reportError("Cannot open graph", e);
            stopControler();
        } finally {
            progressMonitor.done();
        }
    }

    /**
     * Open the build file containing a specific node.
     * 
     * @param node
     */
    public void openNodeFile(final Draw2dNode node) {
        final AntTargetNode targetNode = (AntTargetNode) node.getVertex().getData();
        final String buildFile = targetNode.getBuildFile();
        if ((buildFile != null) && (buildFile.length() > 0)) {
            String targetName = targetNode.getName();
            if (targetName != null) {
                // Remove the surrounding [].
                // FIXME add a method to get the real target name in
                // AntTargetNode.
                targetName = targetName.substring(1, targetName.length() - 1);
            }
            window.openGraphInNewDisplayer(new File(buildFile), targetName, null);
        }
    }

    /**
     * Prints the current graph.
     * 
     * @param printer
     */
    public void print(final Printer printer) {
        if (log.isDebugEnabled()) {
            log.debug("Printing graph");
        }
        final PrintFigureOperation printOp = new PrintFigureOperation(printer, figure);
        printOp.setPrintMode(printMode);
        printOp.run("Grand:" + graph.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(final PropertyChangeEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Get PropertyChangeEvent " + event.getProperty());
        }
        if (event.getProperty().startsWith(PreferenceKeys.GRAPH_PREFIX)) {
            refreshGraph();
        }
    }

    /**
     * Refreshing (i.e.: rerender) the current graph.
     */
    public void refreshGraph() {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        // FIXME check that there is a graph.
        if (log.isInfoEnabled()) {
            log.info("Refreshing current graph");
        }
        progressMonitor.beginTask("Refreshing graph", 3);
        clearFiltersOnNextLoad = false;

        try {
            renderFilteredGraph(progressMonitor);
            if (log.isInfoEnabled()) {
                log.info("Graph refreshed");
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
    public void reloadGraph() {
        reloadGraph(null);
    }

    public void reloadGraph(final Properties properties) {
        final IProgressMonitor progressMonitor = defaultProgressMonitor;

        // FIXME Check that there is a model.
        if (log.isInfoEnabled()) {
            log.info("Reloading current graph");
        }
        progressMonitor.beginTask("Reloading graph", 5);
        clearFiltersOnNextLoad = false;

        try {
            model.reload(properties);
            if (log.isDebugEnabled()) {
                log.debug("Model reloaded graph");
            }
            progressMonitor.worked(1);

            filterAndRenderGraph(progressMonitor);
            if (log.isInfoEnabled()) {
                log.info("Graph reloaded");
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

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#removeSelectionListener(net.ggtools.grand.ui.graph.GraphListener)
     */
    public void removeSelectionListener(final GraphListener listener) {
        graphEventManager.unSubscribe(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#selectNode(net.ggtools.grand.ui.graph.Draw2dNode,
     *      boolean)
     */
    public void selectNode(final Draw2dNode node, final boolean addToSelection) {
        if (log.isTraceEnabled()) {
            log.trace("Select node " + node);
        }
        if (!node.isSelected()) {
            if (!addToSelection) {
                deselectAllNodes();
            }
            selectedNodes.add(node);
            node.setSelected(true);
            final AntTargetNode antNode = (AntTargetNode) node.getVertex().getData();
            displayer.setRichSource(((AntTargetNode) node.getVertex().getData()).getRichSource());
            selectionChangedDispatcher.dispatch(selectedNodes);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#selectNodeByName(java.lang.String,
     *      boolean)
     */
    public void selectNodeByName(final String nodeName, final boolean addToSelection) {
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
     */
    private void filterAndRenderGraph(final IProgressMonitor progressMonitor) {
        progressMonitor.subTask("Filtering graph");
        if (clearFiltersOnNextLoad) {
            filterChain.clearFilters();
        }
        filterChain.filterGraph();
        if (log.isDebugEnabled()) {
            log.debug("Filtering done");
        }
        progressMonitor.worked(1);

        renderFilteredGraph(progressMonitor);
    }

    /**
     * Render the currently load/filtered graph. This method increase the
     * progress monitor by 3.
     */
    private void renderFilteredGraph(final IProgressMonitor progressMonitor) {
        if (log.isDebugEnabled()) {
            log.debug("Creating dot graph");
        }
        progressMonitor.subTask("Laying out graph");
        graph = filterChain.getGraph();
        nodeContentProvider.setGraph(graph);
        final DotGraphCreator creator = new DotGraphCreator(graph, busRoutingEnabled);
        final IDotGraph dotGraph = creator.getGraph();
        progressMonitor.worked(1);

        if (log.isDebugEnabled()) {
            log.debug("Laying out graph");
        }
        final Dot app = new Dot();
        app.layout(dotGraph, 0, -7);
        progressMonitor.worked(1);

        progressMonitor.subTask("Rendering graph");
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                if (figure == null) {
                    figure = renderer.render(dotGraph);
                }
                else {
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
        getDisplayer().setGraph(figure, graphName, model.getLastLoadedFile().getAbsolutePath());
    }

    /**
     * Reports an error in both log and a dialog.
     * 
     * @param message
     * @param e
     */
    private void reportError(final String message, final Throwable e) {
        log.error(message, e);
        ExceptionDialog.openException(window.getShell(), message, e);
    }

    /**
     * Puts the controler in a pre-mortem state where it does not receive or
     * send event.
     */
    private void stopControler() {
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
    public Image createImageForGraph() {
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
