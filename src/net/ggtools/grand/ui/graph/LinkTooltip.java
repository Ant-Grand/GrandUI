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
package net.ggtools.grand.ui.graph;

import java.util.Iterator;
import java.util.Map;

import net.ggtools.grand.ui.AppData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.BlockFlow;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.InlineFlow;
import org.eclipse.draw2d.text.TextFlow;

import sf.jzgraph.IEdge;

/**
 * A figure for node's tooltips.
 * 
 * @author Christophe Labouisse
 */
public class LinkTooltip extends Figure implements DotGraphAttributes {
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

    private static final int TOOLTIP_WIDTH = 400;

    private static final Log log = LogFactory.getLog(LinkTooltip.class);

    private final IEdge edge;

    /**
     * Creates a new tooltip from a Jzgraph node.
     * @param vertex
     */
    public LinkTooltip(IEdge edge) {
        this.edge = edge;
        setForegroundColor(ColorConstants.tooltipForeground);
        setBackgroundColor(ColorConstants.tooltipBackground);
        setOpaque(true);

        final ToolbarLayout layout = new ToolbarLayout();
        setLayoutManager(layout);
        createContents();
        setBorder(new MarginBorder(5));
    }

    protected void createContents() {
        final Label type;
        if (edge.hasAttr(LINK_TASK_ATTR)) {
            type = new Label(edge.getAttrAsString(LINK_TASK_ATTR), AppData.getInstance().getImage(
                    AppData.LINK_ICON));
        }
        else {
            type = new Label("depency", AppData.getInstance().getImage(AppData.LINK_ICON));
        }
        type.setFont(AppData.getInstance().getBoldFont(AppData.TOOLTIP_FONT));
        add(type);

        final FlowPage page = createFlowPage();
        BlockFlow blockFlow = new BlockFlow();
        TextFlow textFlow = new TextFlow("From: ");
        blockFlow.add(textFlow);
        InlineFlow inline = new InlineFlow();
        textFlow = new TextFlow(edge.getTail().getName());
        textFlow.setFont(AppData.getInstance().getItalicFont(AppData.TOOLTIP_MONOSPACE_FONT));
        inline.add(textFlow);
        blockFlow.add(inline);
        blockFlow.setBorder(new SectionBorder());
        page.add(blockFlow);

        blockFlow = new BlockFlow();
        textFlow = new TextFlow("To: ");
        blockFlow.add(textFlow);
        inline = new InlineFlow();
        textFlow = new TextFlow(edge.getHead().getName());
        textFlow.setFont(AppData.getInstance().getItalicFont(AppData.TOOLTIP_MONOSPACE_FONT));
        inline.add(textFlow);
        blockFlow.add(inline);
        page.add(blockFlow);

        if (edge.hasAttr(LINK_PARAMETERS_ATTR)) {
            BlockFlow outterBlock = new BlockFlow();
            final Map parameters = (Map) edge.getAttr(LINK_PARAMETERS_ATTR);
            for (Iterator iter = parameters.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                BlockFlow innerBlock = new BlockFlow();
                textFlow = new TextFlow(((String) entry.getKey()) + ": ");
                textFlow.setFont(AppData.getInstance().getFont(AppData.TOOLTIP_MONOSPACE_FONT));
                innerBlock.add(textFlow);
                inline = new InlineFlow();
                textFlow = new TextFlow((String) entry.getValue());
                textFlow.setFont(AppData.getInstance()
                        .getItalicFont(AppData.TOOLTIP_MONOSPACE_FONT));
                inline.add(textFlow);
                innerBlock.add(inline);
                outterBlock.add(innerBlock);
            }
            outterBlock.setBorder(new SectionBorder());
            page.add(outterBlock);
        }
    }

    /**
     * @return
     */
    private FlowPage createFlowPage() {
        FlowPage page;
        page = new FlowPage();
        final ConstrainedPageFlowLayout pageLayout = new ConstrainedPageFlowLayout(page);
        page.setLayoutManager(pageLayout);
        pageLayout.setMaxFlowWidth(TOOLTIP_WIDTH);
        page.setBorder(new SectionBorder());
        page.setFont(AppData.getInstance().getFont(AppData.TOOLTIP_FONT));
        add(page);
        return page;
    }
}