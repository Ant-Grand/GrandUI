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

import net.ggtools.grand.ui.actions.OpenFileAction;
import net.ggtools.grand.ui.actions.PageSetupAction;
import net.ggtools.grand.ui.actions.PreferenceAction;
import net.ggtools.grand.ui.actions.PrintAction;
import net.ggtools.grand.ui.actions.QuickOpenFileAction;
import net.ggtools.grand.ui.actions.QuitAction;
import net.ggtools.grand.ui.widgets.GraphWindow;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;

/**
 *
 *
 * @author Christophe Labouisse
 */
public class FileMenuManager extends MenuManager {
    /**
     * Constructor for FileMenuManager.
     * @param window GraphWindow
     */
    public FileMenuManager(final GraphWindow window) {
        super("File");
        add(new QuickOpenFileAction(window));
        add(new OpenFileAction(window));
        add(new Separator("print"));
        add(new PageSetupAction(window));
        add(new PrintAction(window));
        add(new PreferenceAction(window));
        add(new Separator("recent files"));
        add(new RecentFilesMenu(window));
        if (!SWT.getPlatform().equals("cocoa")) {
            add(new Separator("quit"));
            add(new QuitAction());
        }
    }
}
