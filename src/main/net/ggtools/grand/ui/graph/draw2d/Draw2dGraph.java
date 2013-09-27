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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.ggtools.grand.ui.Application;
import net.ggtools.grand.ui.graph.GraphControler;
import net.ggtools.grand.ui.graph.GraphListener;
import net.ggtools.grand.ui.graph.SelectionManager;
import net.ggtools.grand.ui.widgets.CanvasScroller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.ScaledGraphics;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.swt.SWT;

import sf.jzgraph.IVertex;

/**
 * @author Christophe Labouisse
 */
public class Draw2dGraph extends Panel implements SelectionManager {

    /**
     * @author Christophe Labouisse
     */
    private final class GraphMouseListener extends MouseListener.Stub {
        /**
         * Method mousePressed.
         * @param me MouseEvent
         * @see org.eclipse.draw2d.MouseListener#mousePressed(org.eclipse.draw2d.MouseEvent)
         */
        @Override
        public void mousePressed(final MouseEvent me) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Graph got mousePressed " + me.button);
            }
            switch (me.button) {
            case (1):
                deselectAllNodes();
                me.consume();
            // No break

            case (2):
                if (scroller != null) {
                    scroller.enterDragMode();
                }
                break;

            case (3):
                if (graphControler != null) {
                    graphControler.getDisplayer().getContextMenu().setVisible(true);
                }
                break;
            }
        }

        /**
         * Method mouseReleased.
         * @param me MouseEvent
         * @see org.eclipse.draw2d.MouseListener#mouseReleased(org.eclipse.draw2d.MouseEvent)
         */
        @Override
        public void mouseReleased(final MouseEvent me) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Graph got mouseReleased " + me.button);
            }
            switch (me.button) {
            case (1):
            case (2):
                if (scroller != null) {
                    scroller.leaveDragMode();
                }
                break;
            }
        }
    }

    /**
     * @author Christophe Labouisse
     */
    private final class NodeMouseListener extends MouseListener.Stub {
        /**
         * Field log.
         */
        private final Log log = LogFactory.getLog(NodeMouseListener.class);

        /**
         * Field node.
         */
        private final Draw2dNode node;

        /**
         * Constructor for NodeMouseListener.
         * @param node Draw2dNode
         */
        private NodeMouseListener(final Draw2dNode node) {
            super();
            this.node = node;
        }

        /**
         * Method mouseDoubleClicked.
         * @param me MouseEvent
         * @see org.eclipse.draw2d.MouseListener#mouseDoubleClicked(org.eclipse.draw2d.MouseEvent)
         */
        @Override
        public void mouseDoubleClicked(final MouseEvent me) {
            if (log.isTraceEnabled()) {
                log.trace("Node got mouseDoubleClicked " + me.button);
            }
            switch (me.button) {
            case (1): {
                final boolean addToSelection;
                if ((me.getState() & SWT.CONTROL) == 0) {
                    addToSelection = false;
                } else {
                    addToSelection = true;
                }
                selectNode(node, addToSelection);
                graphControler.openNodeFile(node);
            }
            }
            me.consume();
        }

        /**
         * Method mousePressed.
         * @param me MouseEvent
         * @see org.eclipse.draw2d.MouseListener#mousePressed(org.eclipse.draw2d.MouseEvent)
         */
        @Override
        public void mousePressed(final MouseEvent me) {
            if (log.isTraceEnabled()) {
                log.trace("Node got mousePressed " + me.button);
            }
            switch (me.button) {
            case (1): {
                final boolean addToSelection;
                if ((me.getState() & SWT.CONTROL) == 0) {
                    addToSelection = false;
                } else {
                    addToSelection = true;
                }
                toggleSelection(node, addToSelection);
                me.consume();
                break;
            }
            case (3): {
                if (!node.isSelected()) {
                    selectNode(node, false);
                }
                // TODO rewrite in a clean way
                if (graphControler != null) {
                    (graphControler).getDisplayer().getContextMenu().setVisible(
                            true);
                }
                break;
            }
            }
        }

        /**
         * Method mouseReleased.
         * @param me MouseEvent
         * @see org.eclipse.draw2d.MouseListener#mouseReleased(org.eclipse.draw2d.MouseEvent)
         */
        @Override
        public void mouseReleased(final MouseEvent me) {
            if (log.isTraceEnabled()) {
                log.trace("Node got mouseReleased " + me.button);
            }

            // Hack, the graph do not get this event if the mouse button is
            // released
            // on a node.
            switch (me.button) {
            case (1):
            case (2):
                if (scroller != null) {
                    scroller.leaveDragMode();
                }
                break;
            }
        }

    }

    /**
     * Field log.
     */
    private static final Log LOG = LogFactory.getLog(Draw2dGraph.class);

    /**
     * Field graphControler.
     */
    private GraphControler graphControler;

    /**
     * Field graphMouseListener.
     */
    private GraphMouseListener graphMouseListener;

    /**
     * Field nodeIndex.
     */
    private final Map<String, Draw2dNode> nodeIndex =
            new HashMap<String, Draw2dNode>();

    /**
     * Field scroller.
     */
    private CanvasScroller scroller;

    /**
     * Field zoom.
     */
    private float zoom;

    /**
     * Constructor for Draw2dGraph.
     */
    public Draw2dGraph() {
        super();
        scroller = null;
        setLayoutManager(new XYLayout());
        setZoom(1.0f);
    }

    /**
     * @param listener GraphListener
     * @see net.ggtools.grand.ui.graph.SelectionManager#addListener(GraphListener)
     */
    public final void addListener(final GraphListener listener) {
        if (graphControler != null) {
            graphControler.addListener(listener);
        }
    }

    /**
     * Method addNotify.
     * @see org.eclipse.draw2d.IFigure#addNotify()
     */
    @Override
    public final void addNotify() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Adding listeners");
        }
        super.addNotify();
        graphMouseListener = new GraphMouseListener();
        addMouseListener(graphMouseListener);
        setFocusTraversable(true);
    }

    /**
     * Method createNode.
     * @param vertex IVertex
     * @return Draw2dNode
     */
    public final Draw2dNode createNode(final IVertex vertex) {
        final Draw2dNode node = new Draw2dNode(this, vertex);
        add(node, node.getBounds());
        node.setFont(Application.getInstance().getFont(Application.NODE_FONT));
        node.addMouseListener(new NodeMouseListener(node));
        node.setCursor(Cursors.HAND);
        nodeIndex.put(node.getName(), node);
        return node;
    }

    /**
     *
     * @see net.ggtools.grand.ui.graph.SelectionManager#deselectAllNodes()
     */
    public final void deselectAllNodes() {
        if (graphControler != null) {
            graphControler.deselectAllNodes();
        }
    }

    /**
     * @param node Draw2dNode
     * @see net.ggtools.grand.ui.graph.SelectionManager#deselectNode(Draw2dNode)
     */
    public final void deselectNode(final Draw2dNode node) {
        if (graphControler != null) {
            graphControler.deselectNode(node);
        }
    }

    /**
     * Gets the bounding box for a specific node.
     *
     * @param name
     *            name of the node to look for.
     * @return the bounds for the node or <code>null</code> if no such node
     *         exist.
     */
    public final Rectangle getBoundsForNode(final String name) {
        final Draw2dNode node = nodeIndex.get(name);
        return (node == null) ? null : node.getBounds();
    }

    /**
     * @param rect Rectangle
     * @return Rectangle
     * @see org.eclipse.draw2d.Figure#getClientArea()
     */
    @Override
    public final Rectangle getClientArea(final Rectangle rect) {
        super.getClientArea(rect);
        rect.width /= zoom;
        rect.height /= zoom;
        return rect;
    }

    /**
     * @return Returns the controller.
     */
    public final SelectionManager getControler() {
        return graphControler;
    }

    /**
     * Method getMinimumSize.
     * @param wHint int
     * @param hHint int
     * @return Dimension
     * @see org.eclipse.draw2d.IFigure#getMinimumSize(int, int)
     */
    @Override
    public final Dimension getMinimumSize(final int wHint, final int hHint) {
        final Dimension d = super.getMinimumSize(wHint, hHint);
        int w = getInsets().getWidth();
        int h = getInsets().getHeight();
        return d.getExpanded(-w, -h).scale(zoom).expand(w, h);
    }

    /**
     * Method getPreferredSize.
     * @param wHint int
     * @param hHint int
     * @return Dimension
     * @see org.eclipse.draw2d.IFigure#getPreferredSize(int, int)
     */
    @Override
    public final Dimension getPreferredSize(final int wHint, final int hHint) {
        final Dimension d = super.getPreferredSize(wHint, hHint);
        int w = getInsets().getWidth();
        int h = getInsets().getHeight();
        return d.getExpanded(-w, -h).scale(zoom).expand(w, h);
    }

    /**
     * @return Returns the scroller.
     */
    public final CanvasScroller getScroller() {
        return scroller;
    }

    /**
     * Method getSelection.
     * @return Collection<Draw2dNode>
     * @see net.ggtools.grand.ui.graph.SelectionManager#getSelection()
     */
    public final Collection<Draw2dNode> getSelection() {
        if (graphControler != null) {
            return graphControler.getSelection();
        }
        return null;
    }

    /**
     * @return Returns the zoom.
     */
    public final float getZoom() {
        return zoom;
    }

    /**
     * Method removeNotify.
     * @see org.eclipse.draw2d.IFigure#removeNotify()
     */
    @Override
    public final void removeNotify() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Removing listeners");
        }
        super.removeNotify();
        if (graphMouseListener != null) {
            removeMouseListener(graphMouseListener);
        }
        setFocusTraversable(false);
    }

    /**
     * @param listener GraphListener
     * @see net.ggtools.grand.ui.graph.SelectionManager#removeSelectionListener(GraphListener)
     */
    public final void removeSelectionListener(final GraphListener listener) {
        if (graphControler != null) {
            graphControler.removeSelectionListener(listener);
        }
    }

    /**
     * @param node Draw2dNode
     * @param addToSelection boolean
     * @see net.ggtools.grand.ui.graph.SelectionManager#selectNode(Draw2dNode, boolean)
     */
    public final void selectNode(final Draw2dNode node,
            final boolean addToSelection) {
        if (graphControler != null) {
            graphControler.selectNode(node, addToSelection);
        }
    }

    /**
     * Method selectNodeByName.
     * @param nodeName String
     * @param addToSelection boolean
     * @see net.ggtools.grand.ui.graph.SelectionManager#selectNodeByName(java.lang.String, boolean)
     */
    public final void selectNodeByName(final String nodeName,
            final boolean addToSelection) {
        final Draw2dNode node = nodeIndex.get(nodeName);
        if (node != null) {
            selectNode(node, addToSelection);
        }
    }

    /**
     * @param scroller
     *            The scroller to set.
     */
    public final void setScroller(final CanvasScroller scroller) {
        this.scroller = scroller;
    }

    /**
     * @param graphControler
     *            The controler to set.
     */
    public final void setSelectionManager(final GraphControler graphControler) {
        this.graphControler = graphControler;
    }

    /**
     * Method setZoom.
     * @param zoom float
     */
    public final void setZoom(final float zoom) {
        this.zoom = zoom;
        revalidate();
        repaint();
    }

    /**
     * @param t Translatable
     * @see org.eclipse.draw2d.Figure#translateFromParent(Translatable)
     */
    @Override
    public final void translateFromParent(final Translatable t) {
        super.translateFromParent(t);
        t.performScale(1 / zoom);
    }

    /**
     * @param t Translatable
     * @see org.eclipse.draw2d.Figure#translateToParent(Translatable)
     */
    @Override
    public final void translateToParent(final Translatable t) {
        t.performScale(zoom);
        super.translateToParent(t);
    }

    /**
     * Method toggleSelection.
     * @param node Draw2dNode
     * @param addToSelection boolean
     */
    private void toggleSelection(final Draw2dNode node,
            final boolean addToSelection) {
        if (node.isSelected()) {
            deselectNode(node);
        } else {
            selectNode(node, addToSelection);
        }
    }

    /**
     * @param graphics Graphics
     * @see org.eclipse.draw2d.Figure#paintClientArea(Graphics)
     */
    @Override
    protected final void paintClientArea(final Graphics graphics) {
        if (getChildren().isEmpty()) {
            return;
        }

        boolean optimizeClip = (getBorder() == null) || getBorder().isOpaque();

        final ScaledGraphics g = new ScaledGraphics(graphics);

        if (!optimizeClip) {
            g.clipRect(getBounds().getShrinked(getInsets()));
        }
        g.translate(getBounds().x + getInsets().left,
                getBounds().y + getInsets().top);
        g.scale(zoom);
        g.pushState();
        paintChildren(g);
        g.popState();
        g.dispose();
        graphics.restoreState();
    }

    /**
     * @return boolean
     * @see org.eclipse.draw2d.Figure#useLocalCoordinates()
     */
    @Override
    protected final boolean useLocalCoordinates() {
        return true;
    }
}
