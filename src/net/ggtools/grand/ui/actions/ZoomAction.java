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

package net.ggtools.grand.ui.actions;

import java.util.Arrays;

import net.ggtools.grand.ui.graph.GraphControlerProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract action to change the zoom factor of the currently displayed graph.
 * 
 * @author Christophe Labouisse
 */
public abstract class ZoomAction extends GraphControlerAction {
    private static final Log log = LogFactory.getLog(ZoomAction.class);

    protected final float[] ZOOM_STEPS = {0.134217728f, 0.16777216f, 0.2097152f, 0.262144f,
            0.32768f, 0.4096f, 0.512f, 0.64f, 0.8f, 1.0f, 1.25f, 1.5625f, 1.953125f, 2.44140625f};
    
    protected final float ZOOM_MIN = ZOOM_STEPS[0];
    
    protected final float ZOOM_MAX = ZOOM_STEPS[ZOOM_STEPS.length-1];

    public ZoomAction(final GraphControlerProvider provider, final String text) {
        super(provider,text);
    }

    /**
     * Find the step for a given zoom factor or the closed step.
     * @param zoom
     * @return
     */
    protected int getZoomStep(float zoom) {
        int index = Arrays.binarySearch(ZOOM_STEPS, zoom);

        if (index < 0) {
            index = -(index + 1);
            if (index >= ZOOM_STEPS.length) index = ZOOM_STEPS.length - 1;
        }
        return index;
    }
    
    protected void doZoomIn() {
        final float zoom = getGraphControler().getDisplayer().getZoom();
        if (zoom < ZOOM_MAX) {
            final int index = getZoomStep(zoom);
            getGraphControler().getDisplayer().setZoom(ZOOM_STEPS[index+1]);
        }
    }
    
    protected void doZoomOut() {
        final float zoom = getGraphControler().getDisplayer().getZoom();
        if (zoom > ZOOM_MIN) {
            final int index = getZoomStep(zoom);
            getGraphControler().getDisplayer().setZoom(ZOOM_STEPS[index-1]);
        }
    }
        
    protected void setZoom(final float zoom) {
        getGraphControler().getDisplayer().setZoom(zoom);
    }
}
