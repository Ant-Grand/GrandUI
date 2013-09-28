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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;

/**
 * @author Christophe Labouisse
 */
public final class CanvasScroller implements MouseMoveListener {
    /**
     * Field log.
     */
    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(CanvasScroller.class);

    /**
     * Field canvas.
     */
    private final FigureCanvas canvas;

    /**
     * Field gotStartPoint.
     */
    private boolean gotStartPoint;

    /**
     * Field inDragMode.
     */
    private boolean inDragMode;

    /**
     * Field startDragY.
     */
    /**
     * Field startDragX.
     */
    private int startDragX, startDragY;

    /**
     * Field viewport.
     */
    private final Viewport viewport;

    /**
     * Constructor for CanvasScroller.
     * @param c FigureCanvas
     */
    public CanvasScroller(final FigureCanvas c) {
        canvas = c;
        viewport = canvas.getViewport();
        inDragMode = false;
        gotStartPoint = false;
    }

    /**
     * Put the scroller in drag mode.
     */
    public void enterDragMode() {
        if (!inDragMode) {
            canvas.addMouseMoveListener(this);
            canvas.setCursor(Cursors.SIZEALL);
            inDragMode = true;
            gotStartPoint = false;
        }
    }

    /**
     *
     */
    public void leaveDragMode() {
        if (inDragMode) {
            canvas.removeMouseMoveListener(this);
            canvas.setCursor(Cursors.ARROW);
            inDragMode = false;
        }
    }

    /**
     * Method mouseMove.
     * @param e MouseEvent
     * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseMove(final MouseEvent e) {
        if (gotStartPoint) {
            canvas.scrollTo(startDragX - e.x, startDragY - e.y);
        } else {
            final Point vpLocation = viewport.getViewLocation();
            startDragX = vpLocation.x + e.x;
            startDragY = vpLocation.y + e.y;
            gotStartPoint = true;
        }
    }
}
