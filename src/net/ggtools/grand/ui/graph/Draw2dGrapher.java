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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.text.FlowContainerLayout;
import org.eclipse.draw2d.text.InlineFlowLayout;

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
        final net.ggtools.grand.graph.Node startNode = graph.getStartNode();
        final Map vertexLUT = new HashMap();
        for (Iterator iter = graph.getNodes(); iter.hasNext();) {
            net.ggtools.grand.graph.Node node = (net.ggtools.grand.graph.Node) iter.next();
            //TODO Voir si je peux faire la construction en une seule passe.
            if (node.getName().equals("")) {
                continue;
            }

            final IVertex vertex = dotGraph.newVertex(node.getName(), node);
            vertex.setAttr("shape","box");
            vertexLUT.put(node.getName(), vertex);
        }

        for (Iterator iter = graph.getNodes(); iter.hasNext();) {
            net.ggtools.grand.graph.Node node = (net.ggtools.grand.graph.Node) iter.next();
            final Collection deps = node.getLinks();
            int index = 1;
            final int numDeps = deps.size();
            for (Iterator iterator = deps.iterator(); iterator.hasNext();) {
                Link link = (Link) iterator.next();
                dotGraph.newEdge((IVertex) vertexLUT.get(link.getStartNode().getName()),
                        (IVertex) vertexLUT.get(link.getEndNode().getName()), link.getName(),
                        link);
            }
        }

        dotGraph.setAttr("rankdir", "LR");

        final Dot app = new Dot();
        app.layout(dotGraph, 0, 7);
        IVertex v = (IVertex) dotGraph.getVertexSet().toArray()[0];
        for (Iterator iter = v.attrKeySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            log.info("Found key " + key + " -> " + v.getAttr(key) + "("
                    + v.getAttr(key).getClass().getName() + ")");
        }
        
        GraphShape shape = (GraphShape) v.getAttr("-shape");
        final float[] coords = new float[6];
        for (final PathIterator ite = /*new FlatteningPathIterator(*/shape
                .getPathIterator(new AffineTransform())/*, 1.5)*/; !ite.isDone(); ite.next()) {
            final int segType = ite.currentSegment(coords);

            switch (segType) {
            case PathIterator.SEG_MOVETO:
                log.debug("Got SEG_MOVETO: "+coords[0]+","+coords[1]);
                break;

            case PathIterator.SEG_LINETO:
                log.debug("Got SEG_LINETO: "+coords[0]+","+coords[1]);
                break;

            case PathIterator.SEG_CUBICTO:
                log.debug("Got SEG_CUBICTO: "+coords[0]+","+coords[1]+","+coords[2]+","+coords[3]);
                break;

            case PathIterator.SEG_QUADTO:
                log.debug("Got SEG_QUADTO: "+coords[0]+","+coords[1]+","+coords[2]+","+coords[3]+","+coords[4]+","+coords[5]);
                break;

            case PathIterator.SEG_CLOSE:
                log.debug("Got SEG_CLOSE");
                break;

            default:
                log.error("Unexpected segment type " + segType);
                break;
            }
        }

        IFigure contents = new Panel();
        contents.setLayoutManager(new XYLayout());

        for (Iterator iter = dotGraph.getVertexSet().iterator(); iter.hasNext();) {
            final IVertex node = (IVertex) iter.next();
            buildNodeFigure(contents, node);
        }

        for (Iterator iter = dotGraph.getEdgeSet().iterator(); iter.hasNext();) {
            final IEdge edge = (IEdge) iter.next();
            buildEdgeFigure(contents, edge);
        }

        return contents;
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
                .getPathIterator(new AffineTransform()), 1.5); !ite.isDone(); ite.next()) {
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
        conn.setForegroundColor(ColorConstants.gray);
        final PolygonDecoration dec = new PolygonDecoration();
        conn.setTargetDecoration(dec);
        //conn.setSourceAnchor(new XYAnchor(new Point(route.getStartPt().getX(),route.getStartPt().getY())));
        conn.setSourceAnchor(new XYAnchor((Point) bends.remove(0)));
        conn.setTargetAnchor(new XYAnchor(new Point(route.getEndPt().getX(),route.getEndPt().getY())));
        //conn.setSourceAnchor(new XYAnchor((Point) bends.remove(0)));
        //conn.setTargetAnchor(new XYAnchor((Point) bends.remove(bends.size() - 1)));

        if (bends.isEmpty()) {
            conn.setConnectionRouter(null);
        } else {
            conn.setConnectionRouter(new BendpointConnectionRouter());
            conn.setRoutingConstraint(bends);
        }

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
        polygon.setBackgroundColor(ColorConstants.lightGray);
        polygon.setOpaque(true);
        
        final GraphShape shape = (GraphShape) node.getAttr("-shape");
        final float[] coords = new float[6];
        for (final PathIterator ite = new FlatteningPathIterator(shape
                .getPathIterator(new AffineTransform()), 1.5); !ite.isDone(); ite.next()) {
            final int segType = ite.currentSegment(coords);

            switch (segType) {
            case PathIterator.SEG_MOVETO:
                log.debug("Got SEG_MOVETO: "+coords[0]+","+coords[1]);
                polygon.addPoint(new Point(coords[0],coords[1]));
                break;

            case PathIterator.SEG_LINETO:
                log.debug("Got SEG_LINETO: "+coords[0]+","+coords[1]);
                polygon.addPoint(new Point(coords[0],coords[1]));
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
        //label.setBackgroundColor(ColorConstants.lightGray);
        //label.setOpaque(true);
        //label.setBorder(new LineBorder());

        String text = node.getAttrString("label"); // + "(" + node.index +","+node.sortValue+ ")";
        label.setText(text);
        polygon.add(label,polygon.getClientArea());
        polygon.setLayoutManager(new ToolbarLayout()); // TODO à changer
        contents.add(polygon,polygon.getBounds());
        //contents.add(label, new Rectangle(x, y, width, height));
    }
}