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
import net.ggtools.grand.ui.graph.GraphControllerProvider;

/**
 * @author Christophe Labouisse
 */
public class UseBusRoutingAction extends GraphListenerAction {

    /**
     * Field DEFAULT_ACTION_NAME.
     * (value is {@value #DEFAULT_ACTION_NAME})
     */
    private static final String DEFAULT_ACTION_NAME = "Bus Routing";

    /**
     * @param parent GraphControllerProvider
     */
    public UseBusRoutingAction(final GraphControllerProvider parent) {
        super(parent, DEFAULT_ACTION_NAME, AS_CHECK_BOX);
    }

    /**
     * Method parameterChanged.
     * @param controller GraphController
     * @see net.ggtools.grand.ui.graph.GraphListener#parameterChanged(GraphController)
     */
    @Override
    public final void parameterChanged(final GraphController controller) {
        final boolean newState = getGraphController().isBusRoutingEnabled();
        if (newState != isChecked()) {
            setChecked(newState);
        }
    }

    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    @Override
    public final void run() {
        getGraphController().enableBusRouting(isChecked());
    }

    /**
     * Method postAddHook.
     * @see GraphControllerAction#postAddHook()
     */
    @Override
    protected final void postAddHook() {
        super.postAddHook();
        setEnabled(true);
        setChecked(getGraphController().isBusRoutingEnabled());
    }

    /**
     * Method postInitHook.
     * @see GraphControllerAction#postInitHook()
     */
    @Override
    protected final void postInitHook() {
        super.postInitHook();
        if (getGraphController() != null) {
            setChecked(getGraphController().isBusRoutingEnabled());
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }
}
