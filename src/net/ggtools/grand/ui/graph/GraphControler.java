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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.ggtools.grand.filters.GraphFilter;
import net.ggtools.grand.graph.Graph;
import net.ggtools.grand.graph.Link;
import net.ggtools.grand.graph.Node;
import net.ggtools.grand.ui.event.Dispatcher;
import net.ggtools.grand.ui.event.EventManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import sf.jzgraph.IDotGraph;
import sf.jzgraph.IEdge;
import sf.jzgraph.IGraph;
import sf.jzgraph.IVertex;
import sf.jzgraph.dot.impl.Dot;
import sf.jzgraph.dot.impl.DotGraph;

/**
 * A class responsible of interfacing the Grand graph objects to the GrandUi
 * display.
 * 
 * @author Christophe Labouisse
 */
public class GraphControler implements GraphModelListener, DotGraphAttributes, SelectionManager,
        FilterChainModelListener {
    private static final Log log = LogFactory.getLog(GraphControler.class);

    private final GraphDisplayer dest;

    private final GraphModel model;

    private final FilterChainModel filterChain;

    private final Draw2dGraphRenderer renderer;

    private final Set selectedNodes = new HashSet();

    private final EventManager selectionEventManager;

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
        selectionEventManager = new EventManager("Selection Event");
        try {
            selectionChangedDispatcher = selectionEventManager
                    .createDispatcher(GraphSelectionListener.class.getDeclaredMethod(
                            "selectionChanged", new Class[]{Collection.class}));
        } catch (SecurityException e) {
            log.fatal("Caught exception initializing GraphControler", e);
            throw new RuntimeException("Cannot instanciate GraphControler", e);
        } catch (NoSuchMethodException e) {
            log.fatal("Caught exception initializing GraphControler", e);
            throw new RuntimeException("Cannot instanciate GraphControler", e);
        }
    }

    /**
     * @param filter
     */
    public void addFilter(GraphFilter filter) {
        log.info("Adding filter " + filter);
        dest.beginTask("Adding filter",4);
        filterChain.addFilterLast(filter);
    }

    public void clearFilters() {
        log.info("Clearing filters");
        dest.beginTask("Clearing filters",4);
        filterChain.clearFilters();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#addSelectionListener(net.ggtools.grand.ui.graph.GraphSelectionListener)
     */
    public void addSelectionListener(GraphSelectionListener listener) {
        selectionEventManager.subscribe(listener);
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
     * @return Returns the dest.
     */
    public final GraphDisplayer getDest() {
        return dest;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.GraphModelListener#newGraphLoaded(net.ggtools.grand.ui.graph.GraphEvent)
     */
    public void newGraphLoaded(GraphEvent event) {
        if (log.isDebugEnabled()) log.debug("Received GraphLoaded event");
        dest.worked(1);
        dest.subTask("Filtering graph");
    }

    public void openFile(final String fileName) {
        if (log.isInfoEnabled()) log.info("Opening " + fileName);
        dest.beginTask("Opening new graph",5);
        model.openFile(fileName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.SelectionManager#removeSelectionListener(net.ggtools.grand.ui.graph.GraphSelectionListener)
     */
    public void removeSelectionListener(GraphSelectionListener listener) {
        selectionEventManager.unSubscribe(listener);
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

    private final IVertex addNode(final IDotGraph dotGraph, final Map vertexLUT,
            final Map nameDimensions, final Node node, boolean isStartNode) {
        final String name = node.getName();
        final IVertex vertex = dotGraph.newVertex(name, node);

        vertex.setAttr(DRAW2DFGCOLOR_ATTR, ColorConstants.black);
        vertex.setAttr(DRAW2DLINEWIDTH_ATTR, 1);

        if (isStartNode) {
            vertex.setAttr(SHAPE_ATTR, "octagon");
            vertex.setAttr(DRAW2DFILLCOLOR_ATTR, ColorConstants.yellow);
            vertex.setAttr(DRAW2DLINEWIDTH_ATTR, 2);
        } else if (node.hasAttributes(Node.ATTR_MAIN_NODE)) {
            vertex.setAttr(SHAPE_ATTR, "box");
            vertex.setAttr(DRAW2DFILLCOLOR_ATTR, ColorConstants.cyan);
        } else {
            vertex.setAttr(SHAPE_ATTR, "oval");
            vertex.setAttr(DRAW2DFILLCOLOR_ATTR, ColorConstants.white);
        }

        if (node.hasAttributes(Node.ATTR_MISSING_NODE)) {
            vertex.setAttr(DRAW2DFGCOLOR_ATTR, ColorConstants.gray);
            vertex.setAttr(DRAW2DFILLCOLOR_ATTR, ColorConstants.lightGray);
        }

        if (node.getDescription() != null) {
            vertex.setAttr(DESCRIPTION_ATTR, node.getDescription());
        }

        vertexLUT.put(name, vertex);
        nameDimensions.put(name, vertex);
        return vertex;
    }

    private final IDotGraph createDotGraph(Graph graph) {
        if (log.isDebugEnabled()) log.debug("Creating DotGraph");
        final IDotGraph dotGraph = new DotGraph(IGraph.GRAPH, graph.getName());

        final Map vertexLUT = new HashMap();
        final Node startNode = graph.getStartNode();

        final Map nameDimensions = new HashMap();

        if (startNode != null) {
            final IVertex vertex = addNode(dotGraph, vertexLUT, nameDimensions, startNode, true);
        }

        for (Iterator iter = graph.getNodes(); iter.hasNext();) {
            final Node node = (net.ggtools.grand.graph.Node) iter.next();
            if (node.getName().equals("") || node == startNode) {
                continue;
            }

            final IVertex vertex = addNode(dotGraph, vertexLUT, nameDimensions, node, false);
        }

        // Update width and height in nodes.
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                // TODO add a font registry.
                final Font systemFont = Display.getDefault().getSystemFont();
                for (Iterator iter = nameDimensions.entrySet().iterator(); iter.hasNext();) {
                    final Map.Entry entry = (Map.Entry) iter.next();
                    final String name = (String) entry.getKey();
                    final IVertex vertex = (IVertex) entry.getValue();

                    final Dimension dim = FigureUtilities.getTextExtents(name, systemFont);
                    vertex.setAttr(MINWIDTH_ATTR, Math.max(dim.width, 50));
                    vertex.setAttr(MINHEIGHT_ATTR, Math.max(dim.height, 25));
                }
            }
        });

        for (Iterator iter = graph.getNodes(); iter.hasNext();) {
            net.ggtools.grand.graph.Node node = (net.ggtools.grand.graph.Node) iter.next();
            final Collection deps = node.getLinks();
            int index = 1;
            final int numDeps = deps.size();
            for (Iterator iterator = deps.iterator(); iterator.hasNext();) {
                final Link link = (Link) iterator.next();
                String name = "";
                if (numDeps > 1) {
                    name += index++;
                }
                final IEdge edge = dotGraph.newEdge((IVertex) vertexLUT.get(link.getStartNode()
                        .getName()), (IVertex) vertexLUT.get(link.getEndNode().getName()), name,
                        link);
                if (link.hasAttributes(Link.ATTR_WEAK_LINK)) {
                    edge.setAttr(DRAW2DFGCOLOR_ATTR, ColorConstants.lightGray);
                }

            }
        }

        return dotGraph;
    }

    /* (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.FilterChainModelListener#filteredGraphAvailable(net.ggtools.grand.ui.graph.FilterChainModel)
     */
    public void filteredGraphAvailable(final Graph filteredGraph) {
        if (log.isDebugEnabled()) log.debug("New filtered graph available");
        final Graph graph = filteredGraph;
        dest.worked(1);

        // TODO Creation d'un type de IDotGraph pour moi dans lequel les Vertex
        // & les Edges
        // soient aussi des objets draw2d.
        if (log.isDebugEnabled()) log.debug("Creating dot graph");
        dest.subTask("Laying out graph");
        final IDotGraph dotGraph = createDotGraph(graph);
        dest.worked(1);

        if (log.isDebugEnabled()) log.debug("Laying out graph");
        final Dot app = new Dot();
        app.layout(dotGraph, 0, -7);
        dest.worked(1);

        dest.subTask("Rendering graph");
        final Draw2dGraph figure = renderer.render(dotGraph);
        figure.setSelectionManager(this);
        dest.worked(1);

        if (log.isDebugEnabled()) log.debug("Done");
        dest.done();
        dest.setGraph(figure);
    }

    /**
     * Reload the current graph.
     */
    public void reloadGraph() {
        if (log.isInfoEnabled()) log.info("Reloading current graph");
        dest.beginTask("Reloading graph",5);
        model.reload();
    }

}