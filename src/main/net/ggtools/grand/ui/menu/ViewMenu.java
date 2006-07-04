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

import net.ggtools.grand.ui.actions.ShowLogAction;
import net.ggtools.grand.ui.actions.ShowOutlinePanelAction;
import net.ggtools.grand.ui.actions.ShowSourcePanelAction;
import net.ggtools.grand.ui.actions.UseBusRoutingAction;
import net.ggtools.grand.ui.actions.ZoomInAction;
import net.ggtools.grand.ui.actions.ZoomOutAction;
import net.ggtools.grand.ui.actions.ZoomResetAction;
import net.ggtools.grand.ui.widgets.GraphWindow;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

/**
 * 
 * 
 * @author Christophe Labouisse
 */
public class ViewMenu extends MenuManager {

    public ViewMenu(final GraphWindow window) {
        super("View");
        add(new Separator("panels"));
        add(new ShowSourcePanelAction(window));
        add(new ShowOutlinePanelAction(window));
        add(new Separator("display"));
        add(new UseBusRoutingAction(window));
        add(new Separator("zoom"));
        add(new ZoomInAction(window));
        add(new ZoomOutAction(window));
        add(new ZoomResetAction(window));
        // Zoom menu
        add(new Separator("misc"));
        add(new ShowLogAction(window));
    }
}
