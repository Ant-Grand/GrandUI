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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import net.ggtools.grand.filters.GraphFilter;
import net.ggtools.grand.graph.Graph;
import net.ggtools.grand.output.DotWriter;
import net.ggtools.grand.ui.event.Dispatcher;
import net.ggtools.grand.ui.event.EventManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.PrintFigureOperation;
import org.eclipse.swt.printing.Printer;

import sf.jzgraph.IDotGraph;
import sf.jzgraph.dot.impl.Dot;

/**
 * A class responsible of interfacing the Grand graph objects to the GrandUi
 * display.
 * 
 * @author Christophe Labouisse
 */
public class GraphControler implements GraphModelListener, DotGraphAttributes, SelectionManager,
        FilterChainModelListener {
    private static final Log log = LogFactory.getLog(GraphControler.class);

    private boolean busRoutingEnabled;

    /**
     * If <code>true</code> the filter chain will be cleared when the next
     * graph is loaded.
     */
    private boolean clearFiltersOnNextLoad;

    private final GraphDisplayer dest;

    private Draw2dGraph figure;

    private final FilterChainModel filterChain;

    private Graph graph;

    private final EventManager graphEventManager;

    private final GraphModel model;

    private final Dispatcher parameterChangedEvent;

    private final Draw2dGraphRenderer renderer;

    private final Set selectedNodes = new HashSet();

    private final Dispatcher selectionChangedDispatcher;

    public GraphControler(final GraphDisplayer dest) {
        if (log.isDebugEnabled()) log.debug("Creating new controler to " + dest);
        this.dest = dest;
        model = new GraphModel();
        model.addListener(this);
        filterChain = new FilterChainModel(model);
        filterChain.addListener(this);
        // TODO voir si je peux virer le renderer et laisser le graph faire le
        // boulot tout seul.
        renderer = new Draw2dGraphRenderer();
        graphEventManager = new EventManager("Graph Event");
        try {
            selectionChangedDispatcher = graphEventManager
                    .createDispatcher(GraphSelectionListener.class.getDeclaredMethod(
                            "selectionChanged", new Class[]{Collection.class}));
            parameterChangedEvent = graphEventManager.createDispatcher(GraphSelectionListener.class
                    .getDeclaredMethod("parameterChanged", new Class[]{GraphControler.class}));
            ;
        } catch (SecurityException e) {
            log.fatal("Caught exception initializing GraphControler", e);
            throw new RuntimeException("Cannot instanciate GraphControler", e);
        } catch (NoSuchMethodException e) {
            log.fatal("Caught exception initializing GraphControler", e);
            throw new RuntimeException("Cannot instanciate GraphControler", e);
        }
        clearFiltersOnNextLoad = true; // Conservative.
        busRoutingEnabled = false;
    }

    /**
     * @param filter
     */
    public void addFilter(GraphFilter filter) {
        log.info("Adding filter " + filter);
        dest.beginTask("Adding filter", 4);
        filterChain.addFilterLast(filter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#addSelectionListener(net.ggtools.grand.ui.graph.GraphSelectionListener)
     */
    public void addSelectionListener(GraphSelectionListener listener) {
        graphEventManager.subscribe(listener);
    }

    public void clearFilters() {
        log.info("Clearing filters");
        dest.beginTask("Clearing filters", 4);
        filterChain.clearFilters();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#deselectAllNodes()
     */
    public void deselectAllNodes() {
        for (final Iterator iter = selectedNodes.iterator(); iter.hasNext();) {
            final Draw2dNode currentNode = (Draw2dNode) iter.next();
            currentNode.setSelected(false);
        }
        selectedNodes.clear();
        selectionChangedDispatcher.dispatch(selectedNodes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#deselectNode(net.ggtools.grand.ui.graph.Draw2dNode)
     */
    public void deselectNode(Draw2dNode node) {
        log.debug("Deselect node " + node);
        if (node.isSelected()) {
            selectedNodes.remove(node);
            node.setSelected(false);
        }
        selectionChangedDispatcher.dispatch(selectedNodes);
    }

    /**
     * Hack for gtk: print using the dot command.
     */
    public void dotPrint() {
        if (log.isDebugEnabled()) log.debug("Printing graph using dot");
        final Properties props = new Properties();
        props.setProperty("dot.graph.attributes", "");
        try {
            final DotWriter dotWriter = new DotWriter(props);
            dotWriter.setProducer(filterChain);
            dotWriter.setShowGraphName(true);
            dotWriter.write(new File("GrandDotPrint.dot"));
            Process proc = Runtime.getRuntime().exec(
                    "dot -Tps -Gpage=8,10 -o GrandDotPrint.ps GrandDotPrint.dot");
            proc.waitFor();
            proc.destroy();
            log.info("Graph printed to GrandDotPrint.ps");
        } catch (Exception e) {
            log.error("Got execption printing", e);
        }
    }

    /**
     * Enable or disable the use of the bus routing algorithm for graph layout.
     * @param enabled
     */
    public void enableBusRouting(final boolean enabled) {
        if (busRoutingEnabled != enabled) {
            busRoutingEnabled = enabled;
            parameterChangedEvent.dispatch(this);
            final Thread thread = new Thread(new Runnable() {
                public void run() {
                    dest.beginTask("Rerouting graph", 3);
                    renderFilteredGraph();
                }
            }, "Graph Layout");
            thread.start();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.FilterChainModelListener#filteredGraphAvailable(net.ggtools.grand.ui.graph.FilterChainModel)
     */
    public void filteredGraphAvailable(final Graph filteredGraph) {
        if (log.isDebugEnabled()) log.debug("New filtered graph available");
        graph = filteredGraph;
        dest.worked(1);

        renderFilteredGraph();
    }

    /**
     * @return Returns the dest.
     */
    public final GraphDisplayer getDest() {
        return dest;
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.SelectionManager#getSelection()
     */
    public Collection getSelection() {
        return selectedNodes;
    }

    /**
     * @return Returns the busRoutingEnabled.
     */
    public final boolean isBusRoutingEnabled() {
        return busRoutingEnabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.GraphModelListener#newGraphLoaded(net.ggtools.grand.ui.graph.GraphEvent)
     */
    public void newGraphLoaded(GraphEvent event) {
        if (log.isDebugEnabled()) log.debug("Received GraphLoaded event");
        dest.worked(1);
        if (clearFiltersOnNextLoad) filterChain.clearFilters();
        dest.subTask("Filtering graph");
    }

    public void openFile(final File file) {
        if (log.isInfoEnabled()) log.info("Opening " + file);
        dest.beginTask("Opening new graph", 5);
        clearFiltersOnNextLoad = true;
        model.openFile(file);
    }

    /**
     * Prints the current graph.
     * @param printer
     */
    public void print(Printer printer) {
        if (log.isDebugEnabled()) log.debug("Printing graph");
        PrintFigureOperation printOp = new PrintFigureOperation(printer, figure);
        printOp.setPrintMode(PrintFigureOperation.FIT_PAGE);
        printOp.run("Grand-Printing");
    }

    /**
     * Reload the current graph.
     */
    public void reloadGraph() {
        if (log.isInfoEnabled()) log.info("Reloading current graph");
        dest.beginTask("Reloading graph", 5);
        clearFiltersOnNextLoad = false;
        model.reload();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#removeSelectionListener(net.ggtools.grand.ui.graph.GraphSelectionListener)
     */
    public void removeSelectionListener(GraphSelectionListener listener) {
        graphEventManager.unSubscribe(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#selectNode(net.ggtools.grand.ui.graph.Draw2dNode,
     *      boolean)
     */
    public void selectNode(final Draw2dNode node, final boolean addToSelection) {
        log.debug("Select node " + node);
        if (!node.isSelected()) {
            if (!addToSelection) {
                deselectAllNodes();
            }
            selectedNodes.add(node);
            node.setSelected(true);
        }
        selectionChangedDispatcher.dispatch(selectedNodes);
    }

    /**
     * Creates a Draw2d graph from a Grand graph. This method will work 3 units
     * and call <code>dest.done()</code> at the end.
     */
    private void renderFilteredGraph() {
        if (log.isDebugEnabled()) log.debug("Creating dot graph");
        dest.subTask("Laying out graph");
        final DotGraphCreator creator = new DotGraphCreator(graph, busRoutingEnabled);
        final IDotGraph dotGraph = creator.getGraph();
        dest.worked(1);

        if (log.isDebugEnabled()) log.debug("Laying out graph");
        final Dot app = new Dot();
        app.layout(dotGraph, 0, -7);
        dest.worked(1);

        dest.subTask("Rendering graph");
        figure = renderer.render(dotGraph);
        figure.setSelectionManager(this);
        dest.worked(1);

        if (log.isDebugEnabled()) log.debug("Done");
        dest.done();
        dest.setGraph(figure, graph.getName());
    }

}