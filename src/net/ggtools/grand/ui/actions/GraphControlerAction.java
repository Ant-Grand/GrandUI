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
package net.ggtools.grand.ui.actions;

import net.ggtools.grand.ui.graph.GraphControler;
import net.ggtools.grand.ui.graph.GraphControlerListener;
import net.ggtools.grand.ui.graph.GraphControlerProvider;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An abstract action managing the GraphControler availability events. The
 * behavior of this action is to be enable when a GraphControler is available
 * and disabled when none is available.
 * @author Christophe Labouisse
 */
public abstract class GraphControlerAction extends Action implements GraphControlerListener {

    private GraphControler graphControler;

    private GraphControlerProvider graphControlerProvider;

    /**
     *  
     */
    public GraphControlerAction(final GraphControlerProvider parent) {
        super();
        init(parent);
    }

    /**
     * @param text
     */
    public GraphControlerAction(final GraphControlerProvider parent, final String text) {
        super(text);
        init(parent);
    }

    /**
     * @param text
     * @param image
     */
    public GraphControlerAction(final GraphControlerProvider parent, final String text,
            final ImageDescriptor image) {
        super(text, image);
        init(parent);
    }

    /**
     * @param text
     * @param style
     */
    public GraphControlerAction(final GraphControlerProvider parent, final String text, int style) {
        super(text, style);
        init(parent);
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphControlerListener#controlerAvailable(net.ggtools.grand.ui.graph.GraphControler)
     */
    final public void controlerAvailable(GraphControler controler) {
        if (graphControler != null) {
            removeGraphControler();
        }
        graphControler = controler;
        postAddHook();
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphControlerListener#controlerRemoved(net.ggtools.grand.ui.graph.GraphControler)
     */
    final public void controlerRemoved(GraphControler controler) {
        if (controler == graphControler) {
            removeGraphControler();
        }
    }

    /**
     * @return Returns the graphControler.
     */
    final public GraphControler getGraphControler() {
        return graphControler;
    }

    /**
     * @return Returns the graphControlerProvider.
     */
    final public GraphControlerProvider getGraphControlerProvider() {
        return graphControlerProvider;
    }

    /**
     * Initialize the action.
     *  
     */
    final private void init(final GraphControlerProvider provider) {
        graphControlerProvider = provider;
        provider.addControlerListener(this);
        graphControler = provider.getControler();
        postInitHook();
    }

    /**
     * A method called a the end of the initialization. The default
     * behavior is to enable the action if there is a GraphControler available.
     */
    protected void postInitHook() {
        setEnabled(graphControler != null);
    }

    /**
     *  
     */
    private void removeGraphControler() {
        preRemoveHook();
        graphControler = null;
    }

    /**
     * A method being called just after a new graph controler have been made
     * available. Overriders can assume that graphControler already refs the new
     * available graph.
     * 
     * The default behaviour is to enable the action.
     */
    protected void postAddHook() {
        setEnabled(true);
    }

    /**
     * A method being called just before a the current graph is removed.
     * Overriders may consider that the graphControler still refs the to be
     * removed controler.
     * 
     * The default behaviour is to disable the action.
     */
    protected void preRemoveHook() {
        setEnabled(false);
    }

}
