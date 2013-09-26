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
import java.awt.geom.Rectangle2D;

import net.ggtools.grand.graph.Node;
import net.ggtools.grand.ui.graph.DotGraphAttributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;

import sf.jzgraph.IVertex;
import sf.jzgraph.impl.GraphShape;

/**
 * The graphical representation of a node.
 *
 * @author Christophe Labouisse
 */
public class Draw2dNode extends Polygon implements DotGraphAttributes {
    /**
     * Field log.
     */
    private static final Log log = LogFactory.getLog(Draw2dNode.class);

    /**
     * Field graph.
     */
    @SuppressWarnings("unused")
    private Draw2dGraph graph;

    /**
     * Field label.
     */
    private Label label;

    /**
     * Field name.
     */
    private String name;

    /**
     * Field nodeBgColor.
     */
    private Color nodeBgColor;

    /**
     * Field nodeFgColor.
     */
    private Color nodeFgColor;

    /**
     * Field selected.
     */
    private boolean selected;

    /**
     * Field selectedBgColor.
     */
    private Color selectedBgColor;

    /**
     * Field vertex.
     */
    private IVertex vertex;

    /**
     * Constructor for Draw2dNode.
     * @param graph Draw2dGraph
     * @param vertex IVertex
     */
    public Draw2dNode(final Draw2dGraph graph, final IVertex vertex) {
        super();

        this.vertex = vertex;
        selected = false;
        this.graph = graph;

        nodeFgColor = (Color) vertex.getAttr(DRAW2DFGCOLOR_ATTR);
        nodeBgColor = (Color) vertex.getAttr(DRAW2DFILLCOLOR_ATTR);
        selectedBgColor = FigureUtilities.darker(nodeBgColor);

        @SuppressWarnings("unused")
        int x, y, width, height;
        final Rectangle2D rect = (Rectangle2D) vertex.getAttr(_BOUNDS_ATTR);
        x = (int) rect.getX();
        y = (int) rect.getY();
        width = (int) rect.getWidth();
        height = (int) rect.getHeight();

        setForegroundColor(nodeFgColor);
        setBackgroundColor(nodeBgColor);
        setLineWidth(vertex.getAttrInt(DRAW2DLINEWIDTH_ATTR));
        setOpaque(true);

        final GraphShape shape = (GraphShape) vertex.getAttr(_SHAPE_ATTR);
        final float[] coords = new float[6];
        for (final PathIterator ite =
                new FlatteningPathIterator(shape.getPathIterator(new AffineTransform()), PATH_ITERATOR_FLATNESS);
                !ite.isDone(); ite.next()) {
            final int segType = ite.currentSegment(coords);

            switch (segType) {
            case PathIterator.SEG_MOVETO:
                addPoint(new Point(coords[0], coords[1]));
                break;

            case PathIterator.SEG_LINETO:
                addPoint(new Point(coords[0], coords[1]));
                break;

            case PathIterator.SEG_CLOSE:
                // Do nothing but no error
                break;

            default:
                log.error("Unexpected segment type " + segType);
                break;
            }
        }

        label = new Label();

        name = vertex.getAttrString(LABEL_ATTR);
        label.setText(name);
        label.setForegroundColor(nodeFgColor);
        setLayoutManager(new BorderLayout());
        add(label, BorderLayout.CENTER);
    }

    /**
     * @return Returns the name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Return the underlying node.
     *
     * @return Node
     */
    public final Node getNode() {
        return (Node) vertex.getData();
    }

    /**
     * @return Returns the vertex.
     */
    public final IVertex getVertex() {
        return vertex;
    }

    /**
     * @return Returns the selected.
     */
    public final boolean isSelected() {
        return selected;
    }

    /**
     * @param selected
     *            The selected to set.
     */
    public final void setSelected(final boolean selected) {
        if (selected != this.selected) {
            this.selected = selected;
            if (selected) {
                setBackgroundColor(selectedBgColor);
            }
            else {
                setBackgroundColor(nodeBgColor);
            }
            repaint();
        }
    }

    /**
     * Method toString.
     * @return String
     */
    @Override
    public final String toString() {
        return this.getClass().getName() + "@" + vertex.getName();
    }

}
