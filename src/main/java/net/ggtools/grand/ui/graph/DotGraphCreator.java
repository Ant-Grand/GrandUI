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
package net.ggtools.grand.ui.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.ggtools.grand.ant.AntLink;
import net.ggtools.grand.ant.AntTargetNode;
import net.ggtools.grand.ant.AntTaskLink;
import net.ggtools.grand.ant.SubantTaskLink;
import net.ggtools.grand.graph.Graph;
import net.ggtools.grand.graph.Link;
import net.ggtools.grand.graph.Node;
import net.ggtools.grand.graph.visit.LinkVisitor;
import net.ggtools.grand.graph.visit.NodeVisitor;
import net.ggtools.grand.ui.Application;
import net.ggtools.grand.ui.GrandUiPrefStore;
import net.ggtools.grand.ui.prefs.PreferenceKeys;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import sf.jzgraph.IDotGraph;
import sf.jzgraph.IEdge;
import sf.jzgraph.IGraph;
import sf.jzgraph.IVertex;
import sf.jzgraph.dot.impl.DotGraph;

/**
 * Factory class creating a JzGraph graph for a Grand graph using Grand's
 * visitor API.
 *
 * @author Christophe Labouisse
 */
public class DotGraphCreator
    implements NodeVisitor, LinkVisitor, DotGraphAttributes {

    /**
     * Field currentLinkName.
     */
    private String currentLinkName;

    /**
     * Field dotGraph.
     */
    private final IDotGraph dotGraph;

    /**
     * Field graph.
     */
    private final Graph graph;

    /**
     * Field nameDimensions.
     */
    private final Map<String, IVertex> nameDimensions;

    /**
     * Field startNode.
     */
    private final Node startNode;

    /**
     * Field useBusRouting.
     */
    private final boolean useBusRouting;

    /**
     * Field vertexLUT.
     */
    private final Map<String, IVertex> vertexLUT;

    /**
     *
     * @param graph Graph
     * @param useBusRouting boolean
     */
    public DotGraphCreator(final Graph graph, final boolean useBusRouting) {
        this.graph = graph;
        this.useBusRouting = useBusRouting;
        nameDimensions = new HashMap<String, IVertex>();
        dotGraph = new DotGraph(IGraph.GRAPH, graph.getName());
        vertexLUT = new HashMap<String, IVertex>();
        startNode = graph.getStartNode();
    }

    /**
     * Method getGraph.
     * @return IDotGraph
     */
    public final IDotGraph getGraph() {
        if (startNode != null) {
            startNode.accept(this);
        }

        for (final Iterator<Node> iter = graph.getNodes(); iter.hasNext();) {
            final Node node = iter.next();
            if ("".equals(node.getName()) || (node == startNode)) {
                continue;
            }
            node.accept(this);
        }

        // Update width and height in nodes.
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final Font systemFont = Application.getInstance().getFont(Application.NODE_FONT);
                for (final Entry<String, IVertex> entry : nameDimensions.entrySet()) {
                    final IVertex vertex = entry.getValue();

                    final Dimension dim = FigureUtilities.getTextExtents(entry.getKey(), systemFont);
                    vertex.setAttr(MINWIDTH_ATTR, Math.max(dim.width, 50));
                    vertex.setAttr(MINHEIGHT_ATTR, Math.max(dim.height, 25));
                }
            }
        });

        for (final Iterator<Node> iter = graph.getNodes(); iter.hasNext();) {
            final Node node = iter.next();
            final Collection<Link> deps = node.getLinks();
            int index = 1;
            final int numDeps = deps.size();
            for (final Link link : deps) {
                currentLinkName = (numDeps == 1) ? "" : String.valueOf(index++);
                link.accept(this);
            }
        }

        return dotGraph;
    }

    /**
     * Method visitLink.
     * @param link AntLink
     * @see net.ggtools.grand.graph.visit.LinkVisitor#visitLink(net.ggtools.grand.ant.AntLink)
     */
    public final void visitLink(final AntLink link) {
        addLink(link);
    }

    /**
     * Method visitLink.
     * @param link AntTaskLink
     * @see net.ggtools.grand.graph.visit.LinkVisitor#visitLink(net.ggtools.grand.ant.AntTaskLink)
     */
    public final void visitLink(final AntTaskLink link) {
        final IEdge edge = addLink(link);
        edge.setAttr(LINK_TASK_ATTR, link.getTaskName());
        edge.setAttr(LINK_PARAMETERS_ATTR, link.getParameterMap());
    }

    /**
     * Method visitLink.
     * @param link Link
     * @see net.ggtools.grand.graph.visit.LinkVisitor#visitLink(net.ggtools.grand.graph.Link)
     */
    public final void visitLink(final Link link) {
        addLink(link);
    }

    /**
     * Method visitLink.
     * @param link SubantTaskLink
     * @see net.ggtools.grand.graph.visit.LinkVisitor#visitLink(net.ggtools.grand.ant.SubantTaskLink)
     */
    public final void visitLink(final SubantTaskLink link) {
        final IEdge edge = addLink(link);
        edge.setAttr(LINK_TASK_ATTR, link.getTaskName());
        edge.setAttr(LINK_PARAMETERS_ATTR, link.getParameterMap());
        edge.setAttr(LINK_SUBANT_DIRECTORIES, link.getDirectories());
        final GrandUiPrefStore preferenceStore =
                Application.getInstance().getPreferenceStore();
        edge.setAttr(DRAW2DFGCOLOR_ATTR, preferenceStore
                .getColor(PreferenceKeys.LINK_SUBANT_COLOR));
        edge.setAttr(DRAW2DLINEWIDTH_ATTR, preferenceStore
                .getInt(PreferenceKeys.LINK_SUBANT_LINEWIDTH));

    }

    /**
     * Method visitNode.
     * @param node AntTargetNode
     * @see net.ggtools.grand.graph.visit.NodeVisitor#visitNode(net.ggtools.grand.ant.AntTargetNode)
     */
    public final void visitNode(final AntTargetNode node) {
        final IVertex vertex = addNode(node);
        final String ifCondition = node.getIfCondition();
        if (ifCondition != null) {
            vertex.setAttr(IF_CONDITION_ATTR, ifCondition);
        }

        final String unlessCondition = node.getUnlessCondition();
        if (unlessCondition != null) {
            vertex.setAttr(UNLESS_CONDITION_ATTR, unlessCondition);
        }

        final String buildFile = node.getBuildFile();
        if (buildFile != null) {
            vertex.setAttr(BUILD_FILE_ATTR, buildFile);
        }
    }

    /**
     * Method visitNode.
     * @param node Node
     * @see net.ggtools.grand.graph.visit.NodeVisitor#visitNode(net.ggtools.grand.graph.Node)
     */
    public final void visitNode(final Node node) {
        addNode(node);
    }

    /**
     * Creates a basic link.
     * @param link Link
     * @return IEdge
     */
    private IEdge addLink(final Link link) {
        final IEdge edge = dotGraph.newEdge(vertexLUT.get(link.getStartNode().getName()),
                vertexLUT.get(link.getEndNode().getName()), currentLinkName, link);
        final GrandUiPrefStore preferenceStore = Application.getInstance().getPreferenceStore();
        if (link.hasAttributes(Link.ATTR_WEAK_LINK)) {
            edge.setAttr(DRAW2DFGCOLOR_ATTR, preferenceStore
                    .getColor(PreferenceKeys.LINK_WEAK_COLOR));
            edge.setAttr(DRAW2DLINEWIDTH_ATTR, preferenceStore
                    .getInt(PreferenceKeys.LINK_WEAK_LINEWIDTH));
        } else {
            edge.setAttr(DRAW2DFGCOLOR_ATTR, preferenceStore
                    .getColor(PreferenceKeys.LINK_DEFAULT_COLOR));
            edge.setAttr(DRAW2DLINEWIDTH_ATTR, preferenceStore
                    .getInt(PreferenceKeys.LINK_DEFAULT_LINEWIDTH));
        }
        return edge;
    }

    /**
     * Creates a <em>basic node</em>.
     * @param node Node
     * @return IVertex
     */
    private IVertex addNode(final Node node) {
        final String name = node.getName();
        final IVertex vertex = dotGraph.newVertex(name, node);

        if (node.equals(startNode)) {
            setVertexPreferences(vertex, "start");
        } else if (node.hasAttributes(Node.ATTR_MAIN_NODE)) {
            setVertexPreferences(vertex, "main");
        } else if (node.hasAttributes(Node.ATTR_MISSING_NODE)) {
            setVertexPreferences(vertex, "missing");
        } else {
            setVertexPreferences(vertex, "default");
        }

        if (node.getDescription() != null) {
            vertex.setAttr(DESCRIPTION_ATTR, node.getDescription());
        }

        if (useBusRouting) {
            final GrandUiPrefStore preferenceStore = Application.getInstance().getPreferenceStore();
            vertex.setAttr("inthreshold", preferenceStore
                    .getInt(PreferenceKeys.GRAPH_BUS_IN_THRESHOLD));
            vertex.setAttr("outthreshold", preferenceStore
                    .getInt(PreferenceKeys.GRAPH_BUS_OUT_THRESHOLD));
        }

        vertexLUT.put(name, vertex);
        nameDimensions.put(name, vertex);
        return vertex;
    }

    /**
     * @param vertex IVertex
     * @param nodeType String
     */
    private void setVertexPreferences(final IVertex vertex,
            final String nodeType) {
        final GrandUiPrefStore preferenceStore =
                Application.getInstance().getPreferenceStore();
        final String keyPrefix = PreferenceKeys.NODE_PREFIX + nodeType;
        vertex.setAttr(SHAPE_ATTR, preferenceStore.getString(keyPrefix + ".shape"));
        vertex.setAttr(DRAW2DFGCOLOR_ATTR, preferenceStore.getColor(keyPrefix + ".fgcolor"));
        vertex.setAttr(DRAW2DFILLCOLOR_ATTR, preferenceStore.getColor(keyPrefix + ".fillcolor"));
        vertex.setAttr(DRAW2DLINEWIDTH_ATTR, preferenceStore.getInt(keyPrefix + ".linewidth"));
    }

}
