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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
public class Draw2dGraph extends Panel {
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
            if (me.button == 1) {
                log.trace("Button 1 pressed on " + node);
                boolean addToSelection;
                if ((me.getState() & InputEvent.CONTROL) == 0) {
                    addToSelection = false;
                } else {
                    addToSelection = true;
                }
                toggleSelection(node, addToSelection);
                me.consume();
            }
        }
    }

    private static final Log log = LogFactory.getLog(Draw2dGraph.class);

    Set selectedNodes = new HashSet();

    public Draw2dGraph() {
        super();
        setLayoutManager(new XYLayout());
    }

    public Draw2dNode createNode(IVertex vertex) {
        final Draw2dNode node = new Draw2dNode(this, vertex);
        add(node, node.getBounds());
        node.addMouseListener(new NodeMouseListener(node));

        return node;
    }

    public void deselectNode(Draw2dNode node) {
        log.debug("Deselect node " + node);
        if (node.isSelected()) {
            selectedNodes.remove(node);
            node.setSelected(false);
        }
    }

    public void selectNode(final Draw2dNode node, final boolean addToSelection) {
        log.debug("Select node " + node);
        if (!node.isSelected()) {
            if (!addToSelection) {
                for (final Iterator iter = selectedNodes.iterator(); iter.hasNext();) {
                    Draw2dNode currentNode = (Draw2dNode) iter.next();
                    currentNode.setSelected(false);
                    iter.remove();
                }
            }
            selectedNodes.add(node);
            node.setSelected(true);
        }
    }

    public void toggleSelection(final Draw2dNode node, final boolean addToSelection) {
        if (node.isSelected()) {
            deselectNode(node);
        } else {
            selectNode(node, addToSelection);
        }
    }
}