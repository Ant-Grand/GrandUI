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

import net.ggtools.grand.ui.Application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.text.BlockFlow;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.InlineFlow;
import org.eclipse.draw2d.text.TextFlow;

import sf.jzgraph.IVertex;

/**
 * A figure for node's tooltips.
 * 
 * @author Christophe Labouisse
 */
public class NodeTooltip extends AbstractGraphTooltip implements DotGraphAttributes {
    private static final Log log = LogFactory.getLog(NodeTooltip.class);

    private final IVertex vertex;

    /**
     * Creates a new tooltip from a Jzgraph node.
     * @param vertex
     */
    public NodeTooltip(IVertex vertex) {
        super();
        this.vertex = vertex;
        createContents();
    }

    protected void createContents() {
        final Label name = new Label(vertex.getName(), Application.getInstance().getImage(
                Application.NODE_ICON));
        name.setFont(Application.getInstance().getBoldFont(Application.TOOLTIP_FONT));
        add(name);

        FlowPage page = null;

        if (vertex.hasAttr(BUILD_FILE_ATTR)) {
            final Label buildFile = new Label(vertex.getAttrAsString(BUILD_FILE_ATTR));
            buildFile.setFont(Application.getInstance().getFont(Application.TOOLTIP_MONOSPACE_FONT));
            buildFile.setBorder(new SectionBorder());
            add(buildFile);

        }

        if (vertex.hasAttr(IF_CONDITION_ATTR)) {
            if (page == null) page = createFlowPage();
            BlockFlow blockFlow = new BlockFlow();
            blockFlow.add(new TextFlow("if: "));
            InlineFlow inline = new InlineFlow();
            final TextFlow textFlow = new TextFlow(vertex.getAttrAsString(IF_CONDITION_ATTR));
            inline.add(textFlow);
            textFlow.setFont(Application.getInstance().getFont(Application.TOOLTIP_MONOSPACE_FONT));
            blockFlow.add(inline);
            blockFlow.setBorder(new SectionBorder());
            page.add(blockFlow);
        }

        if (vertex.hasAttr(UNLESS_CONDITION_ATTR)) {
            if (page == null) page = createFlowPage();
            BlockFlow blockFlow = new BlockFlow();
            blockFlow.add(new TextFlow("unless: "));
            InlineFlow inline = new InlineFlow();
            final TextFlow textFlow = new TextFlow(vertex.getAttrAsString(UNLESS_CONDITION_ATTR));
            inline.add(textFlow);
            textFlow.setFont(Application.getInstance().getFont(Application.TOOLTIP_MONOSPACE_FONT));
            blockFlow.add(inline);
            blockFlow.setBorder(new SectionBorder());
            page.add(blockFlow);
        }

        if (vertex.hasAttr(DESCRIPTION_ATTR)) {
            if (page == null) page = createFlowPage();
            BlockFlow blockFlow = new BlockFlow();
            final TextFlow textFlow = new TextFlow(vertex.getAttrAsString(DESCRIPTION_ATTR));
            textFlow.setFont(Application.getInstance().getItalicFont(Application.TOOLTIP_FONT));
            blockFlow.add(textFlow);
            blockFlow.setBorder(new SectionBorder());
            page.add(blockFlow);
        }
    }
}
