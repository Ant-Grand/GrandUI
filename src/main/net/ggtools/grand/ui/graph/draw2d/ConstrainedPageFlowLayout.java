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

//import org.eclipse.draw2d.text.BlockFlowLayout;
import org.eclipse.draw2d.text.FlowPage;
//import org.eclipse.draw2d.text.LineBox;
import org.eclipse.draw2d.text.PageFlowLayout;

/**
 * A layout for {@link org.eclipse.draw2d.text.FlowPage}object with a maximum
 * width.
 *
 * @author Christophe Labouisse
 */
public class ConstrainedPageFlowLayout extends PageFlowLayout {

    /**
     * Field maxFlowWidth.
     */
    private int maxFlowWidth = -1; // Default is not to bound lines.

    /**
     * Creates a new instance without any constrained width.
     * @param page FlowPage
     */
    public ConstrainedPageFlowLayout(final FlowPage page) {
        super(page);
    }

    /**
     * @return Returns the maximum flow width or <code>-1</code> if not set.
     */
    public final int getMaxFlowWidth() {
        return maxFlowWidth;
    }

    /**
     * Sets the maximum with of the flow. When set to a positive value, the
     * layout will ensure that the flow lines won't be wider than this value.
     *
     * @param maxFlowWidth
     *            The maxFlowWidth to set or <code>-1</code> not to bound the
     *            flow width.
     */
    public final void setMaxFlowWidth(final int maxFlowWidth) {
        this.maxFlowWidth = maxFlowWidth;
        invalidate();
    }

    /*
     * Override to setup the line's x, remaining, and available width.
     * @param line
     *            the LineBox to set up
     */
    /*
    @Override
    protected void setupLine(final LineBox line) {
        super.setupLine(line);
        final int lineWidth = line.getRecommendedWidth();
        if ((maxFlowWidth > 0) && ((lineWidth > maxFlowWidth) || (lineWidth == -1))) {
            line.setRecommendedWidth(maxFlowWidth);
        }
    }
    */
}
