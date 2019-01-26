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

import net.ggtools.grand.filters.GraphFilter;
import net.ggtools.grand.filters.PrefixedNodeFilter;
import net.ggtools.grand.ui.graph.GraphControllerProvider;

import org.eclipse.swt.SWT;

/**
 * An action to filter the prefixed nodes.
 *
 * @author Christophe Labouisse
 */
public class FilterPrefixedNodesAction extends GraphControllerAction {

    /**
     * Field DEFAULT_ACTION_NAME.
     * (value is {@value #DEFAULT_ACTION_NAME})
     */
    private static final String DEFAULT_ACTION_NAME =
            "Filter out prefixed nodes";

    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    @Override
    public final void run() {
        final GraphFilter filter = new PrefixedNodeFilter();
        getGraphController().addFilter(filter);
    }

    /**
     * Constructor for FilterPrefixedNodesAction.
     * @param parent GraphControllerProvider
     */
    public FilterPrefixedNodesAction(final GraphControllerProvider parent) {
        super(parent, DEFAULT_ACTION_NAME);
        setAccelerator((SWT.getPlatform().equals("cocoa") ? SWT.MOD1 : SWT.CONTROL) | SWT.ALT | 'P');
    }
}
