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

import net.ggtools.grand.ui.graph.GraphController;
import net.ggtools.grand.ui.graph.GraphControllerListener;

import net.ggtools.grand.ui.graph.GraphControllerProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An abstract action managing the GraphController availability events. The
 * behavior of this action is to be enable when a GraphController is available
 * and disabled when none is available.
 * @author Christophe Labouisse
 */
public abstract class GraphControllerAction extends Action
        implements GraphControllerListener {

    /**
     * Field graphController.
     */
    private GraphController graphController;

    /**
     * Field graphControllerProvider.
     */
    private GraphControllerProvider graphControllerProvider;

    /**
     *
     * @param parent GraphControllerProvider
     */
    protected GraphControllerAction(final GraphControllerProvider parent) {
        super();
        init(parent);
    }

    /**
     * @param parent GraphControllerProvider
     * @param text String
     */
    protected GraphControllerAction(final GraphControllerProvider parent,
                                    final String text) {
        super(text);
        init(parent);
    }

    /**
     * @param parent GraphControllerProvider
     * @param text String
     * @param image ImageDescriptor
     */
    protected GraphControllerAction(final GraphControllerProvider parent,
                                    final String text, final ImageDescriptor image) {
        super(text, image);
        init(parent);
    }

    /**
     * @param parent GraphControllerProvider
     * @param text String
     * @param style int
     */
    protected GraphControllerAction(final GraphControllerProvider parent,
                                    final String text, final int style) {
        super(text, style);
        init(parent);
    }

    /**
     * Method controllerAvailable.
     * @param controller GraphController
     * @see GraphControllerListener#controllerAvailable(GraphController)
     */
    public final void controllerAvailable(final GraphController controller) {
        if (graphController != null) {
            removeGraphController();
        }
        graphController = controller;
        postAddHook();
    }

    /**
     * Method controllerRemoved.
     * @param controller GraphController
     * @see GraphControllerListener#controllerRemoved(GraphController)
     */
    public final void controllerRemoved(final GraphController controller) {
        if (controller == graphController) {
            removeGraphController();
        }
    }

    /**
     * @return Returns the graphController.
     */
    public final GraphController getGraphController() {
        return graphController;
    }

    /**
     * @return Returns the graphControllerProvider.
     */
    public final GraphControllerProvider getGraphControllerProvider() {
        return graphControllerProvider;
    }

    /**
     * Initialize the action.
     *
     * @param provider GraphControllerProvider
     */
    private void init(final GraphControllerProvider provider) {
        graphControllerProvider = provider;
        provider.addControllerListener(this);
        graphController = provider.getController();
        postInitHook();
    }

    /**
     * A method called a the end of the initialization. The default
     * behavior is to enable the action if there is a GraphController available.
     */
    protected void postInitHook() {
        setEnabled(graphController != null);
    }

    /**
     *
     */
    private void removeGraphController() {
        preRemoveHook();
        graphController = null;
    }

    /**
     * A method being called just after a new graph controller have been made
     * available. Overriders can assume that graphController already refs the new
     * available graph.
     *
     * The default behaviour is to enable the action.
     */
    protected void postAddHook() {
        setEnabled(true);
    }

    /**
     * A method being called just before a the current graph is removed.
     * Overriders may consider that the graphController still refs the to be
     * removed controller.
     *
     * The default behaviour is to disable the action.
     */
    protected void preRemoveHook() {
        setEnabled(false);
    }

}
