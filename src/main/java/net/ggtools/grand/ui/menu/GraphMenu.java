// $Id$
/* ====================================================================
 * Copyright (c) 2002-2003, Christophe Labouisse
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.ggtools.grand.ui.menu;

import net.ggtools.grand.ui.actions.ClearFiltersAction;
import net.ggtools.grand.ui.actions.EditGraphPropertiesAction;
import net.ggtools.grand.ui.actions.ExportGraphAction;
import net.ggtools.grand.ui.actions.FilterConnectedToNodeAction;
import net.ggtools.grand.ui.actions.FilterFromNodeAction;
import net.ggtools.grand.ui.actions.FilterIsolatedNodesAction;
import net.ggtools.grand.ui.actions.FilterMissingNodesAction;
import net.ggtools.grand.ui.actions.FilterPrefixedNodesAction;
import net.ggtools.grand.ui.actions.FilterSelectedNodesAction;
import net.ggtools.grand.ui.actions.FilterToNodeAction;
import net.ggtools.grand.ui.actions.ReloadGraphAction;
import net.ggtools.grand.ui.graph.GraphControllerProvider;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

/**
 *
 *
 * @author Christophe Labouisse
 */
public class GraphMenu extends MenuManager {

    /**
     * Constructor for GraphMenu.
     * @param controllerProvider GraphControllerProvider
     */
    public GraphMenu(final GraphControllerProvider controllerProvider) {
        super("Graph");
        add(new ReloadGraphAction(controllerProvider));
        add(new EditGraphPropertiesAction(controllerProvider));
        add(new ExportGraphAction(controllerProvider));
        add(new Separator("general filters"));
        add(new FilterIsolatedNodesAction(controllerProvider));
        add(new FilterMissingNodesAction(controllerProvider));
        add(new FilterPrefixedNodesAction(controllerProvider));
        add(new Separator("selected node filters"));
        add(new FilterSelectedNodesAction(controllerProvider));
        add(new FilterConnectedToNodeAction(controllerProvider));
        add(new FilterFromNodeAction(controllerProvider));
        add(new FilterToNodeAction(controllerProvider));
        add(new Separator("clear filters"));
        add(new ClearFiltersAction(controllerProvider));
    }
}
