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

package net.ggtools.grand.ui.graph.draw2d;

import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Iterator;

import net.ggtools.grand.ui.Application;
import net.ggtools.grand.ui.graph.DotGraphAttributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MidpointLocator;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import sf.jzgraph.IDotGraph;
import sf.jzgraph.IEdge;
import sf.jzgraph.IVertex;
import sf.jzgraph.dot.impl.DotRoute;

/**
 * Renders a IDotGraph into draw2d objects.
 * 
 * @author Christophe Labouisse
 */
public class Draw2dGraphRenderer implements DotGraphAttributes {
    private static final Log log = LogFactory.getLog(Draw2dGraphRenderer.class);

    public Draw2dGraph render(IDotGraph dotGraph) {
        if (log.isDebugEnabled()) log.debug("Rendering Draw2d Graph");
        final Draw2dGraph contents = new Draw2dGraph();
        // Add a margin on the right & bottom edge.
        // The 24 is a magic number which seems to be the same margin JzGraph is
        // applying on the top & left edges.
        // FIXME find out the real dot margin.
        contents.setBorder(new MarginBorder(0,0,24,24));

        return createGraph(dotGraph, contents);
    }
    
    /**
     * Fill an existing graph with a IDotGraph.
     * 
     * @param contents
     * @param dotGraph
     * @return
     */
    public Draw2dGraph render(Draw2dGraph contents, IDotGraph dotGraph) {
        contents.removeAll();
        return createGraph(dotGraph, contents);
    }

    /**
     * @param dotGraph
     * @param contents
     * @return
     */
    private Draw2dGraph createGraph(IDotGraph dotGraph, final Draw2dGraph contents) {
        for (Iterator iter = dotGraph.allVertices().iterator(); iter.hasNext();) {
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
     * @param coords
     * @param bends
     * @param min
     * @param max
     */
    private final void addBendPoint(final float[] coords, final ArrayList bends, final Point min,
            final Point max) {
        final int x = (int) coords[0];
        final int y = (int) coords[1];
        bends.add(new AbsoluteBendpoint(x, y));
        if (x < min.x) min.x = x;
        if (x > max.x) max.x = x;
        if (y < min.y) min.y = y;
        if (y > max.y) max.y = y;
    }

    /**
     * @param contents
     * @param edge
     * @param route
     * @return
     */
    private PolylineConnection addConnectionFromRoute(final IFigure contents, final String name,
            final DotRoute route) {
        final float[] coords = new float[6];
        final ArrayList bends = new ArrayList();
        boolean isFirstPoint = true;
        final Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        final Point max = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

        for (final PathIterator ite = new FlatteningPathIterator(route.getPath().getPathIterator(
                new AffineTransform()), PATH_ITERATOR_FLATNESS); !ite.isDone(); ite.next()) {
            final int segType = ite.currentSegment(coords);

            switch (segType) {
            case PathIterator.SEG_MOVETO:
                if (isFirstPoint) {
                    addBendPoint(coords, bends, min, max);
                }
                else {
                    log.error("Got SEG_MOVETO after first point");
                }
                break;

            case PathIterator.SEG_LINETO:
                addBendPoint(coords, bends, min, max);
                break;

            default:
                log.error("Unexpected segment type " + segType);
                break;
            }

            isFirstPoint = false;
        }

        Rectangle bounds = new Rectangle(min, max);
        final PolylineConnection conn = new PolylineConnection();

        final Point sourcePoint = (Point) bends.remove(0);
        final Point targetPoint;
        if (route.getEndPt() != null) {
            targetPoint = new Point(route.getEndPt().getX(), route.getEndPt().getY());
        }
        else {
            targetPoint = (Point) bends.remove(bends.size() - 1);
        }
        conn.setSourceAnchor(new XYRelativeAnchor(conn, sourcePoint));
        conn.setTargetAnchor(new XYRelativeAnchor(conn, targetPoint));

        if (bends.isEmpty()) {
            conn.setConnectionRouter(null);
        }
        else {
            conn.setConnectionRouter(new BendpointConnectionRouter());
            conn.setRoutingConstraint(bends);
        }

        if (name != null) {
            Label label = new Label(name);
            label.setOpaque(true);
            label.setBackgroundColor(ColorConstants.buttonLightest);
            label.setBorder(new LineBorder());
            label.setFont(Application.getInstance().getFont(Application.LINK_FONT));
            final ConnectionLocator locator = new MidpointLocator(conn, bends.size() / 2);
            locator.setRelativePosition(PositionConstants.CENTER);
            // Includes the label in the connnection bounds.
            // Worth cas scenario, the label is on the connection edge.
            final Dimension labelSize = label.getPreferredSize();
            bounds.expand(labelSize.width, labelSize.height);
            conn.add(label, locator);
        }

        contents.add(conn, bounds);

        return conn;
    }

    /**
     * Builds a figure for the given edge and adds it to contents
     * 
     * @param contents
     *            the parent figure to add the edge to
     * @param edge
     *            the edge
     */
    private void buildEdgeFigure(final IFigure contents, final IEdge edge) {
        if (log.isTraceEnabled())
                log.trace("Building edge from " + edge.getTail().getName() + " to "
                        + edge.getHead().getName());
        final DotRoute route = (DotRoute) edge.getAttr(POSITION_ATTR);

        String name = edge.getName();
        if ("".equals(name)) name = null;

        final PolylineConnection conn = addConnectionFromRoute(contents, name, route);

        if (edge.getAttr(DRAW2DFGCOLOR_ATTR) != null) {
            conn.setForegroundColor((Color) edge.getAttr(DRAW2DFGCOLOR_ATTR));
        }
        
        if (edge.getAttr(DRAW2DLINEWIDTH_ATTR) != null) {
            conn.setLineWidth(edge.getAttrInt(DRAW2DLINEWIDTH_ATTR));
        }

        final PolygonDecoration dec = new PolygonDecoration();
        conn.setTargetDecoration(dec);

        conn.setToolTip(new LinkTooltip(edge));
        conn.setCursor(Cursors.HAND);
    }

    /**
     * Builds a Figure for the given node and adds it to contents
     * 
     * @param contents
     *            the parent Figure to add the node to
     * @param node
     *            the node to add
     */
    private void buildNodeFigure(Draw2dGraph contents, IVertex node) {
        if (log.isDebugEnabled()) log.debug("Building node " + node.getName());
        final Draw2dNode polygon = contents.createNode(node);
        polygon.setToolTip(new NodeTooltip(node));

        if (node.hasAttr("inbus")) {
            final PolylineConnection conn = createBusConnexion(contents, node, ColorConstants.red,
                    "inbus", "bus to");
            conn.setLineWidth(2);
        }
        if (node.hasAttr("outbus")) {
            final PolylineConnection conn = createBusConnexion(contents, node, ColorConstants.blue,
                    "outbus", "bus from");
            conn.setLineWidth(2);
        }
        if (node.hasAttr("tobus")) {
            final PolylineConnection conn = createBusConnexion(contents, node, ColorConstants.blue,
                    "tobus", "bus from");
        }
        if (node.hasAttr("frombus")) {
            final PolylineConnection conn = createBusConnexion(contents, node, ColorConstants.red,
                    "frombus", "bus to");
            final PolygonDecoration dec = new PolygonDecoration();
            conn.setTargetDecoration(dec);
        }
    }

    /**
     * @param contents
     * @param node
     * @param color
     * @param busLabel
     * @return
     */
    private PolylineConnection createBusConnexion(Draw2dGraph contents, IVertex node, Color color,
            String busId, String busLabel) {
        final PolylineConnection conn = addConnectionFromRoute(contents, null, (DotRoute) node
                .getAttr(busId));
        conn.setForegroundColor(color);
        contents.add(conn, conn.getBounds());
        final Label label = new Label(busLabel + " " + node.getName(), Application.getInstance()
                .getImage(Application.LINK_ICON));
        label.setFont(Application.getInstance().getBoldFont(Application.TOOLTIP_FONT));
        conn.setToolTip(label);
        return conn;
    }

}