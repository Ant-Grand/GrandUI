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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import net.ggtools.grand.ant.AntLink;
import net.ggtools.grand.ant.AntTargetNode;
import net.ggtools.grand.ant.AntTaskLink;
import net.ggtools.grand.graph.Graph;
import net.ggtools.grand.graph.Link;
import net.ggtools.grand.graph.Node;
import net.ggtools.grand.graph.visit.LinkVisitor;
import net.ggtools.grand.graph.visit.NodeVisitor;
import net.ggtools.grand.ui.AppData;
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
public class DotGraphCreator implements NodeVisitor, LinkVisitor, DotGraphAttributes {
    private static final Log log = LogFactory.getLog(GraphControler.class);

    private Graph graph;

    private final Map nameDimensions;

    private final IDotGraph dotGraph;

    private final Map vertexLUT;

    private String currentLinkName;

    private final Node startNode;

    /**
     *  
     */
    public DotGraphCreator(final Graph graph) {
        this.graph = graph;
        nameDimensions = new HashMap();
        dotGraph = new DotGraph(IGraph.GRAPH, graph.getName());
        vertexLUT = new HashMap();
        startNode = graph.getStartNode();
    }

    public IDotGraph getGraph() {
        if (startNode != null) {
            startNode.accept(this);
        }
        
        for (Iterator iter = graph.getNodes(); iter.hasNext();) {
            final Node node = (net.ggtools.grand.graph.Node) iter.next();
            if (node.getName().equals("") || (node == startNode)) {
                continue;
            }
            node.accept(this);
        }

        // Update width and height in nodes.
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final Font systemFont = AppData.getInstance().getFont(AppData.NODE_FONT);
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
            Node node = (Node) iter.next();
            final Collection deps = node.getLinks();
            int index = 1;
            final int numDeps = deps.size();
            for (Iterator iterator = deps.iterator(); iterator.hasNext();) {
                final Link link = (Link) iterator.next();
                currentLinkName = "";
                if (numDeps > 1) {
                    currentLinkName += index++;
                }
                link.accept(this);
            }
        }

        return dotGraph;
    }

    /**
     * Creates a basic link.
     * @param link
     * @param name
     * @return
     */
    private IEdge addLink(final Link link) {
        final IEdge edge = dotGraph.newEdge((IVertex) vertexLUT.get(link.getStartNode().getName()),
                (IVertex) vertexLUT.get(link.getEndNode().getName()), currentLinkName, link);
        if (link.hasAttributes(Link.ATTR_WEAK_LINK)) {
            edge.setAttr(DRAW2DFGCOLOR_ATTR, ColorConstants.lightGray);
        }
        return edge;
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.graph.visit.NodeVisitor#visitNode(net.ggtools.grand.graph.Node)
     */
    public void visitNode(Node node) {
        addNode(node);
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.graph.visit.NodeVisitor#visitNode(net.ggtools.grand.ant.AntTargetNode)
     */
    public void visitNode(AntTargetNode node) {
        IVertex vertex = addNode(node);
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

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.graph.visit.LinkVisitor#visitLink(net.ggtools.grand.graph.Link)
     */
    public void visitLink(Link link) {
        addLink(link);
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.graph.visit.LinkVisitor#visitLink(net.ggtools.grand.ant.AntLink)
     */
    public void visitLink(AntLink link) {
        addLink(link);
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.graph.visit.LinkVisitor#visitLink(net.ggtools.grand.ant.AntTaskLink)
     */
    public void visitLink(AntTaskLink link) {
        final IEdge edge = addLink(link);
        AntTaskLink taskLink = (AntTaskLink) link;

        edge.setAttr(LINK_TASK_ATTR, taskLink.getTaskName());
        edge.setAttr(LINK_PARAMETERS_ATTR, taskLink.getParameterMap());
    }

    /**
     * Creates a <em>basic node</em>
     * @param node
     * @return
     */
    private final IVertex addNode(final Node node) {
        final String name = node.getName();
        final IVertex vertex = dotGraph.newVertex(name, node);

        vertex.setAttr(DRAW2DFGCOLOR_ATTR, ColorConstants.black);
        vertex.setAttr(DRAW2DLINEWIDTH_ATTR, 1);

        if (node.equals(startNode)) {
            vertex.setAttr(SHAPE_ATTR, "octagon");
            vertex.setAttr(DRAW2DFILLCOLOR_ATTR, ColorConstants.yellow);
            vertex.setAttr(DRAW2DLINEWIDTH_ATTR, 2);
        }
        else if (node.hasAttributes(Node.ATTR_MAIN_NODE)) {
            vertex.setAttr(SHAPE_ATTR, "box");
            vertex.setAttr(DRAW2DFILLCOLOR_ATTR, ColorConstants.cyan);
        }
        else {
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

}