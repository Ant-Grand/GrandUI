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
package net.ggtools.grand.ui.widgets;

import net.ggtools.grand.ui.graph.GraphControler;
import net.ggtools.grand.ui.graph.GraphControlerListener;
import net.ggtools.grand.ui.graph.GraphDisplayer;
import net.ggtools.grand.ui.menu.GraphMenu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

/**
 * A CTabItem specialized in displaying graph. Although it implements
 * {@link net.ggtools.grand.ui.graph.GraphControlerProvider}this class only
 * manage a dummy notification system as there won't be any change of the
 * controler during the instances lifetime.
 * 
 * @author Christophe Labouisse
 */
public class GraphTabItem extends CTabItem implements GraphDisplayer {
    private final class CanvasScroller extends MouseAdapter implements MouseMoveListener {
        final private FigureCanvas canvas;

        private int startDragX, startDragY;

        private final Viewport viewport;
        
        private boolean inDragMode;

        public CanvasScroller(FigureCanvas c) {
            canvas = c;
            viewport = canvas.getViewport();
            inDragMode = false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseDown(MouseEvent e) {
            log.trace("Got mouse down");
            if (!inDragMode && (e.button == 2 || e.button == 1)) {
                final Point vpLocation = viewport.getViewLocation();
                startDragX = vpLocation.x + e.x;
                startDragY = vpLocation.y + e.y;
                canvas.addMouseMoveListener(this);
                getControl().setCursor(Cursors.SIZEALL);
                inDragMode = true;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseMove(MouseEvent e) {
            canvas.scrollTo(startDragX - e.x, startDragY - e.y);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseUp(MouseEvent e) {
            log.trace("Got mouse up");
            if (inDragMode && (e.button == 2 || e.button == 1)) {
                canvas.removeMouseMoveListener(this);
                getControl().setCursor(Cursors.ARROW);
                inDragMode = false;
            }
        }
    }

    private static final Log log = LogFactory.getLog(GraphTabItem.class);

    private FigureCanvas canvas;

    private final Menu contextMenu;

    private final MenuManager contextMenuManager;

    private final GraphControler controler;

    /**
     * Creates a tab to display a new graph.
     * @param parent parent CTabFolder.
     * @param style
     */
    public GraphTabItem(CTabFolder parent, int style, GraphControler controler) {
        super(parent, style);
        this.controler = controler;
        canvas = new FigureCanvas(parent);
        setControl(canvas);
        canvas.getViewport().setContentsTracksHeight(true);
        canvas.getViewport().setContentsTracksWidth(true);
        canvas.setBackground(ColorConstants.white);
        canvas.setScrollBarVisibility(FigureCanvas.AUTOMATIC);
        final CanvasScroller synchronizer = new CanvasScroller(canvas);
        canvas.addMouseListener(synchronizer);
        contextMenuManager = new GraphMenu(this);
        contextMenu = contextMenuManager.createContextMenu(canvas);
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphControlerProvider#addControlerListener(net.ggtools.grand.ui.graph.GraphControlerListener)
     */
    public void addControlerListener(GraphControlerListener listener) {
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#getContextMenu()
     */
    public Menu getContextMenu() {
        return contextMenu;
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#getControler()
     */
    public GraphControler getControler() {
        return controler;
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphControlerProvider#removeControlerListener(net.ggtools.grand.ui.graph.GraphControlerListener)
     */
    public void removeControlerListener(GraphControlerListener listener) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#setGraph(net.ggtools.grand.ui.graph.Graph)
     */
    public void setGraph(final IFigure figure, final String name, final String toolTip) {
        Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                canvas.setContents(figure);
                setText(name);
                setToolTipText(toolTip);
            }
        });
    }
}
