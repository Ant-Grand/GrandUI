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

import net.ggtools.grand.ui.AppData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
         * 
         * @see org.eclipse.draw2d.MouseListener.Stub#mousePressed(org.eclipse.draw2d.MouseEvent)
         */
        public void mousePressed(MouseEvent me) {
            switch (me.button) {
            case (1):
                {
                    log.trace("Button 1 pressed on " + node);
                    boolean addToSelection;
                    if ((me.getState() & InputEvent.CONTROL) == 0) {
                        addToSelection = false;
                    } else {
                        addToSelection = true;
                    }
                    toggleSelection(node, addToSelection);
                    me.consume();
                    break;
                }
            case (3):
                {
                    log.trace("Button 3 pressed on " + node);
                    if (!node.isSelected()) {
                        selectNode(node, false);
                    }
                    // TODO rewrite in a clean way
                    if (selectionManager != null) {
                        ((GraphControler) selectionManager).getDest().getContextMenu()
                                .setVisible(true);
                    }
                    break;
                }
            }
        }

    }

    private static final Log log = LogFactory.getLog(Draw2dGraph.class);

    private SelectionManager selectionManager;

    public Draw2dGraph() {
        super();
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
                    {
                        log.trace("Button 1 pressed on graph");
                        deselectAllNodes();
                        me.consume();
                        break;
                    }
                case (3):
                    {
                        log.trace("Button 3 pressed on graph");
                        // TODO rewrite in a clean way
                        if (selectionManager != null) {
                            ((GraphControler) selectionManager).getDest().getContextMenu()
                                    .setVisible(true);
                        }
                    }
                }
            }
        });
    }

    /**
     * @param listener
     */
    public void addSelectionListener(GraphSelectionListener listener) {
        if (selectionManager != null) selectionManager.addSelectionListener(listener);
    }

    public Draw2dNode createNode(IVertex vertex) {
        final Draw2dNode node = new Draw2dNode(this, vertex);
        add(node, node.getBounds());
        node.setFont(AppData.getInstance().getFont(AppData.NODE_FONT));
        node.addMouseListener(new NodeMouseListener(node));

        return node;
    }

    /**
     *  
     */
    public void deselectAllNodes() {
        if (selectionManager != null) selectionManager.deselectAllNodes();
    }

    /**
     * @param node
     */
    public void deselectNode(Draw2dNode node) {
        if (selectionManager != null) selectionManager.deselectNode(node);
    }

    /**
     * @return Returns the controler.
     */
    public final SelectionManager getControler() {
        return selectionManager;
    }

    /**
     * @param listener
     */
    public void removeSelectionListener(GraphSelectionListener listener) {
        if (selectionManager != null) selectionManager.removeSelectionListener(listener);
    }

    /**
     * @param node
     * @param addToSelection
     */
    public void selectNode(Draw2dNode node, boolean addToSelection) {
        if (selectionManager != null) selectionManager.selectNode(node, addToSelection);
    }

    /**
     * @param selectionManager
     *            The controler to set.
     */
    public final void setSelectionManager(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    private void toggleSelection(final Draw2dNode node, final boolean addToSelection) {
        if (node.isSelected()) {
            deselectNode(node);
        } else {
            selectNode(node, addToSelection);
        }
    }
}