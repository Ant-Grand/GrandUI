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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ggtools.grand.ui.graph.GraphControler;
import net.ggtools.grand.ui.graph.GraphControlerProvider;

/**
 * @author Christophe Labouisse
 */
public class UseBusRoutingAction extends GraphListenerAction {

    private static final String DEFAULT_ACTION_NAME = "Bus Routing";

    private static final Log log = LogFactory.getLog(UseBusRoutingAction.class);

    /**
     * @param parent
     */
    public UseBusRoutingAction(GraphControlerProvider parent) {
        super(parent, DEFAULT_ACTION_NAME, AS_CHECK_BOX);
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphListener#parameterChanged(net.ggtools.grand.ui.graph.GraphControler)
     */
    public void parameterChanged(GraphControler controler) {
        boolean newState = getGraphControler().isBusRoutingEnabled();
        if (newState != isChecked()) {
            setChecked(newState);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        getGraphControler().enableBusRouting(isChecked());
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.actions.GraphControlerAction#postAddHook()
     */
    protected void postAddHook() {
        super.postAddHook();
        setEnabled(true);
        setChecked(getGraphControler().isBusRoutingEnabled());
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.actions.GraphControlerAction#postInitHook()
     */
    protected void postInitHook() {
        super.postInitHook();
        if (getGraphControler() != null) {
            setChecked(getGraphControler().isBusRoutingEnabled());
            setEnabled(true);
        }
        else {
            setEnabled(false);
        }
    }
}