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

import net.ggtools.grand.ui.Application;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;

/**
 * Basic tooltip.
 * 
 * @author Christophe Labouisse
 */
abstract class AbstractGraphTooltip extends Figure {
    /**
     * A border for the <em>optional</em> sections of the tooltip.
     * @author Christophe Labouisse
     */
    public class SectionBorder extends AbstractBorder {

        /*
         * (non-Javadoc)
         * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
         */
        public Insets getInsets(IFigure figure) {
            return new Insets(1, 0, 0, 0);
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure,
         *      org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
         */
        public void paint(IFigure figure, Graphics graphics, Insets insets) {
            final Rectangle paintRectangle = getPaintRectangle(figure, insets);
            graphics.drawLine(paintRectangle.getTopLeft(), paintRectangle.getTopRight());
        }

    }

    static final int TOOLTIP_WIDTH = 400;
    
    /**
     * Default constructor. 
     */
    public AbstractGraphTooltip() {
        setForegroundColor(ColorConstants.tooltipForeground);
        setBackgroundColor(ColorConstants.tooltipBackground);
        setOpaque(true);

        final ToolbarLayout layout = new ToolbarLayout();
        setLayoutManager(layout);
        setBorder(new MarginBorder(5));
    }

    /**
     * Create the tooltip's contents. Should be called explicitly by implementing classes.
     * TODO: Make this part automatic (ticket #37).
     */
    abstract protected void createContents();

    /**
     * @return
     */
    protected FlowPage createFlowPage() {
        FlowPage page;
        page = new FlowPage();
        final ConstrainedPageFlowLayout pageLayout = new ConstrainedPageFlowLayout(page);
        page.setLayoutManager(pageLayout);
        pageLayout.setMaxFlowWidth(TOOLTIP_WIDTH);
        page.setBorder(new SectionBorder());
        page.setFont(Application.getInstance().getFont(Application.TOOLTIP_FONT));
        add(page);
        return page;
    }
}
