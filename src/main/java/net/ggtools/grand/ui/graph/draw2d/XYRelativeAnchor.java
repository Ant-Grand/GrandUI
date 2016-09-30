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

package net.ggtools.grand.ui.graph.draw2d;

import org.eclipse.draw2d.ConnectionAnchorBase;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;

/**
 * An anchor positioned to a XY position relatively to a
 * {@link org.eclipse.draw2d.IFigure}.
 *
 * @author Christophe Labouisse
 */
public class XYRelativeAnchor extends ConnectionAnchorBase {
    /**
     * Field owner.
     */
    private final IFigure owner;

    /**
     * Field location.
     */
    private final Point location;

    /**
     *
     * @param owner IFigure
     * @param location Point
     */
    public XYRelativeAnchor(final IFigure owner, final Point location) {
        this.owner = owner;
        this.location = location;
    }

    /**
     * Method getLocation.
     * @param reference Point
     * @return Point
     * @see org.eclipse.draw2d.ConnectionAnchor#getLocation(org.eclipse.draw2d.geometry.Point)
     */
    public final Point getLocation(final Point reference) {
        final Point result = location.getCopy();
        getOwner().translateToAbsolute(result);
        return result;
    }

    /**
     * Method getOwner.
     * @return IFigure
     * @see org.eclipse.draw2d.ConnectionAnchor#getOwner()
     */
    public final IFigure getOwner() {
        return owner;
    }

    /**
     * Method getReferencePoint.
     * @return Point
     * @see org.eclipse.draw2d.ConnectionAnchor#getReferencePoint()
     */
    public final Point getReferencePoint() {
        return location;
    }

    /**
     * Method setLocation.
     * @param p Point
     */
    public final void setLocation(final Point p) {
        location.setLocation(p);
        fireAnchorMoved();
    }
}
