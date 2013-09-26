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

package net.ggtools.grand.ui.menu;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import net.ggtools.grand.ui.RecentFilesListener;
import net.ggtools.grand.ui.RecentFilesManager;
import net.ggtools.grand.ui.actions.ClearRecentFilesAction;
import net.ggtools.grand.ui.widgets.GraphWindow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Display;

/**
 *
 *
 * @author Christophe Labouisse
 */
public class RecentFilesMenu extends MenuManager
        implements RecentFilesListener {

    /**
     * An action opening a specific file when run.
     *
     * @author Christophe Labouisse
     */
    private class OpenRecentFileAction extends Action {

        /**
         * Field file.
         */
        private final File file;

        /**
         * Field window.
         */
        private final GraphWindow window;

        /**
         * Constructor for OpenRecentFileAction.
         * @param window GraphWindow
         * @param fileName String
         */
        public OpenRecentFileAction(final GraphWindow window,
                final String fileName) {
            super(fileName);
            this.window = window;
            file = new File(fileName);
        }

        /**
         * Method run.
         * @see org.eclipse.jface.action.IAction#run()
         */
        @Override
        public void run() {
            window.openGraphInNewDisplayer(file,
                    RecentFilesManager.getInstance().getProperties(file));
        }
    }

    /**
     * Field log.
     */
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(RecentFilesMenu.class);

    /**
     * Field RECENT_FILES_GROUP.
     * (value is ""Recent files"")
     */
    private static final String RECENT_FILES_GROUP = "Recent files";

    /**
     * Field window.
     */
    private final GraphWindow window;

    /**
     * Creates a new RecentFilesMenu instance loading the recent files from the
     * preference store.
     *
     * @param window GraphWindow
     */
    public RecentFilesMenu(final GraphWindow window) {
        super("Recent files");
        this.window = window;
        add(new ClearRecentFilesAction());
        add(new Separator(RECENT_FILES_GROUP));
        RecentFilesManager.getInstance().addListener(this);
    }

    /**
     * Update the recent files menu.
     *
     * @param recentFiles
     *            list of files to put in the menu.
     * @see net.ggtools.grand.ui.RecentFilesListener#refreshRecentFiles(Collection)
     */
    public final void refreshRecentFiles(final Collection<String> recentFiles) {
        final Runnable runnable = new Runnable() {
            public void run() {
                // Clear the previous items.
                final IContributionItem[] items = getItems();

                for (int i = indexOf(RECENT_FILES_GROUP) + 1; i < items.length; i++) {
                    IContributionItem item = items[i];
                    if (item.isGroupMarker()) {
                        break;
                    }
                    remove(item);
                }

                // Re-add the contents.
                for (final Iterator<String> iter = recentFiles.iterator(); iter.hasNext();) {
                    final String fileName = iter.next();
                    appendToGroup(RECENT_FILES_GROUP, new OpenRecentFileAction(window, fileName));
                }
            }
        };

        if (Display.getCurrent() == null) {
            Display.getDefault().syncExec(runnable);
        }
        else {
            runnable.run();
        }
    }
}
