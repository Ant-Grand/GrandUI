// $Id$
/* ====================================================================
 * Copyright (c) 2002-2003, Christophe Labouisse
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.ggtools.grand.ui.graph;

import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.ggtools.grand.exceptions.GrandException;
import net.ggtools.grand.graph.Graph;
import net.ggtools.grand.graph.GraphConsumer;
import net.ggtools.grand.graph.GraphProducer;
import net.ggtools.grand.graph.Link;
import net.ggtools.grand.graph.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import sf.jzgraph.IDotGraph;
import sf.jzgraph.IEdge;
import sf.jzgraph.IGraph;
import sf.jzgraph.IVertex;
import sf.jzgraph.dot.impl.Dot;
import sf.jzgraph.dot.impl.DotGraph;
import sf.jzgraph.dot.impl.DotRoute;
import sf.jzgraph.impl.GraphShape;

/**
 * 
 * 
 * @author Christophe Labouisse
 */
public class Draw2dGrapher implements GraphConsumer {
    private static final double PATH_ITERATOR_FLATNESS = 1.0;

    private static Log log = LogFactory.getLog(Draw2dGrapher.class);

    private GraphProducer graphProducer;

    /* (non-Javadoc)
     * @see net.ggtools.grand.graph.GraphConsumer#setProducer(net.ggtools.grand.graph.GraphProducer)
     */
    public void setProducer(GraphProducer producer) {
        graphProducer = producer;
    }

    public IFigure drawGraph() throws GrandException {
        final Graph graph = graphProducer.getGraph();
        final IDotGraph dotGraph = new DotGraph(IGraph.GRAPH, graph.getName());
        dotGraph.setAttr("rankdir", "LR");

        final Map vertexLUT = new HashMap();
        final Node startNode = graph.getStartNode();

        if (startNode != null) {
            addNode(dotGraph, vertexLUT, startNode, true);
        }

        for (Iterator iter = graph.getNodes(); iter.hasNext();) {
            final Node node = (net.ggtools.grand.graph.Node) iter.next();
            //TODO Voir si je peux faire la construction en une seule passe.
            if (node.getName().equals("") || node == startNode) {
                continue;
            }

            addNode(dotGraph, vertexLUT, node, false);
        }

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
                        .getName()), (IVertex) vertexLUT.get(link.getEndNode().getName()),
                        name, link);
            }
        }

        final Dot app = new Dot();
        app.layout(dotGraph, 0, 7);

        final IVertex v = (IVertex) dotGraph.getVertexSet().toArray()[0];
        for (Iterator iter = v.attrKeySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            log.info("Found key " + key + " -> " + v.getAttr(key) + "("
                    + v.getAttr(key).getClass().getName() + ")");
        }

        final IFigure contents = new Panel();
        contents.setLayoutManager(new XYLayout());

        for (Iterator iter = dotGraph.vertexIterator(); iter.hasNext();) {
            final IVertex node = (IVertex) iter.next();
            buildNodeFigure(contents, node);
        }

        for (Iterator iter = dotGraph.edgeIterator(); iter.hasNext();) {
            final IEdge edge = (IEdge) iter.next();
            buildEdgeFigure(contents, edge);
        }

        return contents;
    }

    /**
     * @param dotGraph
     * @param vertexLUT
     * @param node
     * @param isStartNode
     */
    private void addNode(final IDotGraph dotGraph, final Map vertexLUT, final Node node,
            boolean isStartNode) {
        final IVertex vertex = dotGraph.newVertex(node.getName(), node);

        vertex.setAttr("draw2dfgcolor", ColorConstants.black);
        vertex.setAttr("draw2dlinewidth",1);

        if (isStartNode) {
            vertex.setAttr("shape", "triangle");
            vertex.setAttr("draw2dfillcolor", ColorConstants.yellow);
            vertex.setAttr("draw2dlinewidth",2);
        } else if (node.hasAttributes(Node.ATTR_MAIN_NODE)) {
            vertex.setAttr("shape", "box");
            vertex.setAttr("draw2dfillcolor", ColorConstants.cyan);
        } else {
            vertex.setAttr("shape", "oval");
            vertex.setAttr("draw2dfillcolor", ColorConstants.white);
        }

        if (node.hasAttributes(Node.ATTR_MISSING_NODE)) {
            vertex.setAttr("draw2dfgcolor", ColorConstants.gray);
            vertex.setAttr("draw2dfillcolor", ColorConstants.lightGray);
        }
        
        if (node.getDescription() != null) {
            vertex.setAttr("description",node.getDescription());
        }

        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final Dimension dim = FigureUtilities.getTextExtents(node.getName(), Display
                        .getDefault().getSystemFont());
                vertex.setAttr("minwidth", Math.max(dim.width, 50));
                vertex.setAttr("minheight", Math.max(dim.height, 25));
            }
        });
        vertexLUT.put(node.getName(), vertex);
    }

    /**
     * Builds a figure for the given edge and adds it to contents
     * @param contents the parent figure to add the edge to
     * @param edge the edge
     */
    static void buildEdgeFigure(final IFigure contents, final IEdge edge) {
        final DotRoute route = (DotRoute) edge.getAttr("pos");
        final float[] coords = new float[6];
        final ArrayList bends = new ArrayList();
        boolean isFirstPoint = true;
        for (final PathIterator ite = new FlatteningPathIterator(route.getPath()
                .getPathIterator(new AffineTransform()), PATH_ITERATOR_FLATNESS); !ite.isDone(); ite
                .next()) {
            final int segType = ite.currentSegment(coords);

            switch (segType) {
            case PathIterator.SEG_MOVETO:
                log.debug("Got SEG_MOVETO");
                if (isFirstPoint) {
                    bends.add(new AbsoluteBendpoint((int) coords[0], (int) coords[1]));
                } else {
                    log.error("Got SEG_MOVETO after first point");
                }
                break;

            case PathIterator.SEG_LINETO:
                log.debug("Got SEG_LINETO");
                bends.add(new AbsoluteBendpoint((int) coords[0], (int) coords[1]));
                break;

            default:
                log.error("Unexpected segment type " + segType);
                break;
            }

            isFirstPoint = false;
        }

        final PolylineConnection conn = new PolylineConnection();
        final PolygonDecoration dec = new PolygonDecoration();
        conn.setTargetDecoration(dec);

        final Point sourcePoint = (Point) bends.remove(0);
        final Point targetPoint = new Point(route.getEndPt().getX(), route.getEndPt().getY());
        conn.setSourceAnchor(new XYAnchor(sourcePoint));
        conn.setTargetAnchor(new XYAnchor(targetPoint));

        if (bends.isEmpty()) {
            conn.setConnectionRouter(null);
        } else {
            conn.setConnectionRouter(new BendpointConnectionRouter());
            conn.setRoutingConstraint(bends);
        }

        final String name = edge.getName();
        if (!"".equals(name)) {
            Label label = new Label(name);
            label.setOpaque(true);
            label.setBackgroundColor(ColorConstants.buttonLightest);
            label.setBorder(new LineBorder());
            //final ConnectionLocator locator = new ConnectionLocator(conn, ConnectionLocator.SOURCE);
            final ConnectionLocator locator = new MidpointLocator(conn, bends.size()/2);
            locator.setRelativePosition(PositionConstants.CENTER);
            conn.add(label, locator);
        }
        // TODO Tooltip sur les edges ?
        contents.add(conn);
    }

    /**
     * Builds a Figure for the given node and adds it to contents
     * @param contents the parent Figure to add the node to
     * @param node the node to add
     */
    static void buildNodeFigure(IFigure contents, IVertex node) {
        int x, y, width, height;
        Rectangle2D rect = (Rectangle2D) node.getAttr("-bounds");
        x = (int) rect.getX();
        y = (int) rect.getY();
        width = (int) rect.getWidth();
        height = (int) rect.getHeight();

        final Polygon polygon = new Polygon();
        polygon.setForegroundColor((Color) node.getAttr("draw2dfgcolor"));
        polygon.setBackgroundColor((Color) node.getAttr("draw2dfillcolor"));
        polygon.setLineWidth(node.getAttrInt("draw2dlinewidth"));
        polygon.setOpaque(true);

        final GraphShape shape = (GraphShape) node.getAttr("-shape");
        final float[] coords = new float[6];
        for (final PathIterator ite = new FlatteningPathIterator(shape
                .getPathIterator(new AffineTransform()), PATH_ITERATOR_FLATNESS); !ite.isDone(); ite
                .next()) {
            final int segType = ite.currentSegment(coords);

            switch (segType) {
            case PathIterator.SEG_MOVETO:
                log.debug("Got SEG_MOVETO: " + coords[0] + "," + coords[1]);
                polygon.addPoint(new Point(coords[0], coords[1]));
                break;

            case PathIterator.SEG_LINETO:
                log.debug("Got SEG_LINETO: " + coords[0] + "," + coords[1]);
                polygon.addPoint(new Point(coords[0], coords[1]));
                break;

            case PathIterator.SEG_CLOSE:
                log.debug("Got SEG_CLOSE");
                break;

            default:
                log.error("Unexpected segment type " + segType);
                break;
            }
        }

        log.debug("Creating label for node " + node.toString() + " (" + x + "," + y + ")");
        final Label label;
        label = new Label();

        String text = node.getAttrString("label");
        label.setText(text);
        label.setForegroundColor((Color) node.getAttr("draw2dfgcolor"));
        polygon.setLayoutManager(new BorderLayout());
        polygon.add(label, BorderLayout.CENTER);
        
        if (node.hasAttr("description")) {
            final Label toolTip = new Label(node.getAttrAsString("description"));
            toolTip.setForegroundColor(ColorConstants.tooltipForeground);
            toolTip.setBackgroundColor(ColorConstants.tooltipBackground);
            toolTip.setOpaque(true);
            polygon.setToolTip(toolTip);
        }

        contents.add(polygon, polygon.getBounds());
    }
}