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

import net.ggtools.grand.ui.Application;
import net.ggtools.grand.ui.widgets.CanvasScroller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.InputEvent;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.XYLayout;

import sf.jzgraph.IVertex;

/**
 * @author Christophe Labouisse
 */
public class Draw2dGraph extends Panel implements SelectionManager {
    private final class NodeMouseListener extends MouseListener.Stub {
        private final Log log = LogFactory.getLog(NodeMouseListener.class);

        private final Draw2dNode node;

        private NodeMouseListener(Draw2dNode node) {
            super();
            this.node = node;
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.draw2d.MouseListener#mouseDoubleClicked(org.eclipse.draw2d.MouseEvent)
         */
        public void mouseDoubleClicked(MouseEvent me) {
            log.trace("Double click on " + me.button);
            switch (me.button) {
            case (1): {
                final boolean addToSelection;
                if ((me.getState() & InputEvent.CONTROL) == 0) {
                    addToSelection = false;
                }
                else {
                    addToSelection = true;
                }
                selectNode(node, addToSelection);
                graphControler.openNodeFile(node);
            }
            }
            me.consume();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.draw2d.MouseListener.Stub#mousePressed(org.eclipse.draw2d.MouseEvent)
         */
        public void mousePressed(MouseEvent me) {
            log.trace("Got mouse down");
            switch (me.button) {
            case (1): {
                final boolean addToSelection;
                if ((me.getState() & InputEvent.CONTROL) == 0) {
                    addToSelection = false;
                }
                else {
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
                    ((GraphControler) graphControler).getDest().getContextMenu().setVisible(true);
                }
                break;
            }
            }
        }

    }

    private static final Log log = LogFactory.getLog(Draw2dGraph.class);

    private GraphControler graphControler;

    private CanvasScroller scroller;

    public Draw2dGraph() {
        super();
        scroller = null;
        setLayoutManager(new XYLayout());
        addMouseListener(new MouseListener.Stub() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.draw2d.MouseListener.Stub#mousePressed(org.eclipse.draw2d.MouseEvent)
             */
            public void mousePressed(MouseEvent me) {
                switch (me.button) {
                case (1):
                    deselectAllNodes();
                    me.consume();

                case (2):
                    if (scroller != null) {
                        scroller.enterDragMode();
                    }
                    break;

                case (3):
                    if (graphControler != null) {
                        graphControler.getDest().getContextMenu().setVisible(true);
                    }
                    break;
                }
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.draw2d.MouseListener.Stub#mouseReleased(org.eclipse.draw2d.MouseEvent)
             */
            public void mouseReleased(MouseEvent me) {
                switch (me.button) {
                case (1):
                case (2):
                    if (scroller != null) {
                        scroller.leaveDragMode();
                    }
                    break;
                }
            }
        });
    }

    /**
     * @param listener
     */
    public void addListener(GraphListener listener) {
        if (graphControler != null) graphControler.addListener(listener);
    }

    public Draw2dNode createNode(IVertex vertex) {
        final Draw2dNode node = new Draw2dNode(this, vertex);
        add(node, node.getBounds());
        node.setFont(Application.getInstance().getFont(Application.NODE_FONT));
        node.addMouseListener(new NodeMouseListener(node));
        node.setCursor(Cursors.HAND);
        return node;
    }

    /**
     *  
     */
    public void deselectAllNodes() {
        if (graphControler != null) graphControler.deselectAllNodes();
    }

    /**
     * @param node
     */
    public void deselectNode(Draw2dNode node) {
        if (graphControler != null) graphControler.deselectNode(node);
    }

    /**
     * @return Returns the controler.
     */
    public final SelectionManager getControler() {
        return graphControler;
    }

    /**
     * @param listener
     */
    public void removeSelectionListener(GraphListener listener) {
        if (graphControler != null) graphControler.removeSelectionListener(listener);
    }

    /**
     * @param node
     * @param addToSelection
     */
    public void selectNode(Draw2dNode node, boolean addToSelection) {
        if (graphControler != null) graphControler.selectNode(node, addToSelection);
    }

    /**
     * @param graphControler
     *            The controler to set.
     */
    public final void setSelectionManager(GraphControler graphControler) {
        this.graphControler = graphControler;
    }

    private void toggleSelection(final Draw2dNode node, final boolean addToSelection) {
        if (node.isSelected()) {
            deselectNode(node);
        }
        else {
            selectNode(node, addToSelection);
        }
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.SelectionManager#getSelection()
     */
    public Collection getSelection() {
        if (graphControler != null) return graphControler.getSelection();
        return null;
    }
    /**
     * @return Returns the scroller.
     */
    public final CanvasScroller getScroller() {
        return scroller;
    }
    /**
     * @param scroller The scroller to set.
     */
    public final void setScroller(CanvasScroller scroller) {
        this.scroller = scroller;
    }
}
