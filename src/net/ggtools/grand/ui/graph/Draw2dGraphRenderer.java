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

import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MidpointLocator;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.geometry.Point;
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
     * Builds a figure for the given edge and adds it to contents
     * 
     * @param contents
     *            the parent figure to add the edge to
     * @param edge
     *            the edge
     */
    private void buildEdgeFigure(final IFigure contents, final IEdge edge) {
        final DotRoute route = (DotRoute) edge.getAttr(POSITION_ATTR);
        final float[] coords = new float[6];
        final ArrayList bends = new ArrayList();
        boolean isFirstPoint = true;
        for (final PathIterator ite = new FlatteningPathIterator(route.getPath().getPathIterator(
                new AffineTransform()), PATH_ITERATOR_FLATNESS); !ite.isDone(); ite.next()) {
            final int segType = ite.currentSegment(coords);

            switch (segType) {
            case PathIterator.SEG_MOVETO:
                if (isFirstPoint) {
                    bends.add(new AbsoluteBendpoint((int) coords[0], (int) coords[1]));
                } else {
                    log.error("Got SEG_MOVETO after first point");
                }
                break;

            case PathIterator.SEG_LINETO:
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
        if (edge.getAttr(DRAW2DFGCOLOR_ATTR) != null) {
            conn.setForegroundColor((Color) edge.getAttr(DRAW2DFGCOLOR_ATTR));
        }

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
            final ConnectionLocator locator = new MidpointLocator(conn, bends.size() / 2);
            locator.setRelativePosition(PositionConstants.CENTER);
            conn.add(label, locator);
        }

        // TODO better tooltip
        addTooltip(conn, "From: " + edge.getTail().getName() + "\nTo: " + edge.getHead().getName());

        contents.add(conn,conn.getBounds());
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
        final Draw2dNode polygon = contents.createNode(node);
        
        if (node.hasAttr(DESCRIPTION_ATTR)) {
            addTooltip(polygon, node.getAttrAsString(DESCRIPTION_ATTR));
        }
    }

    /**
     * @param figure
     * @param toolTipContent
     */
    private void addTooltip(final IFigure figure, final String toolTipContent) {
        final Label toolTip = new Label(toolTipContent);
        toolTip.setForegroundColor(ColorConstants.tooltipForeground);
        toolTip.setBackgroundColor(ColorConstants.tooltipBackground);
        toolTip.setOpaque(true);
        figure.setToolTip(toolTip);
    }

}