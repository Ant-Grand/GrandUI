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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import net.ggtools.grand.ui.actions.ClearRecentFilesAction;
import net.ggtools.grand.ui.graph.GraphControler;
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
public class RecentFilesMenu extends MenuManager implements PreferenceChangeListener {
    private static final String MAX_RECENT_FILES_PREFS_KEY = "max recent files";

    private static final String RECENT_FILES_PREFS_KEY = "recent files";

    private static final Log log = LogFactory.getLog(GraphControler.class);

    /**
     * An action openning a specific file when run.
     * @author Christophe Labouisse
     */
    private class OpenRecentFileAction extends Action {
        private final GraphWindow window;

        private final File file;

        public OpenRecentFileAction(final GraphWindow window, final String fileName) {
            super(fileName);
            this.window = window;
            this.file = new File(fileName);

        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            window.openGraphInNewDisplayer(file);
        }
    }

    private static final Preferences prefs = Preferences.userNodeForPackage(RecentFilesMenu.class);

    private final GraphWindow window;

    private final static LinkedList recentFiles = new LinkedList();

    private static int maxFiles = prefs.getInt(MAX_RECENT_FILES_PREFS_KEY, 4);

    public RecentFilesMenu(final GraphWindow window) {
        super("Recent files");
        this.window = window;
        add(new ClearRecentFilesAction(this));
        add(new Separator(RECENT_FILES_PREFS_KEY));

        loadRecentFiles();
        addRecentFiles();
        prefs.addPreferenceChangeListener(this);
    }

    /**
     * @param window
     */
    private void addRecentFiles() {
        final Runnable runnable = new Runnable() {
            public void run() {
                for (final Iterator iter = recentFiles.iterator(); iter.hasNext();) {
                    final String fileName = (String) iter.next();
                    appendToGroup(RECENT_FILES_PREFS_KEY,
                            new OpenRecentFileAction(window, fileName));
                }
            }
        };

        if (Display.getCurrent() == null) {
            Display.getDefault().asyncExec(runnable);
        }
        else {
            runnable.run();
        }
    }

    /**
     *  
     */
    private final static void loadRecentFiles() {
        StringTokenizer tokenizer = new StringTokenizer(prefs.get(RECENT_FILES_PREFS_KEY, ""), ",");
        recentFiles.clear();
        for (int i = 0; (i < maxFiles) && tokenizer.hasMoreTokens(); i++) {
            final String fileName = tokenizer.nextToken();
            recentFiles.addFirst(fileName);
        }
    }

    public final static void addNewFile(final File file) {
        String fileName = file.getAbsolutePath();
        recentFiles.remove(fileName);
        recentFiles.addFirst(fileName);
        if (recentFiles.size() > maxFiles) {
            recentFiles.removeLast();
        }

        StringBuffer buffer = new StringBuffer();
        for (final Iterator iter = recentFiles.iterator(); iter.hasNext();) {
            final String item = (String) iter.next();
            buffer.append(item);
            if (iter.hasNext()) buffer.append(",");
        }
        prefs.put(RECENT_FILES_PREFS_KEY, buffer.toString());
    }

    public final void clear() {
        if (log.isDebugEnabled()) log.debug("Clearing recent files");
        removeRecentFiles();
        prefs.put(RECENT_FILES_PREFS_KEY, "");
        recentFiles.clear();
    }

    /**
     *  
     */
    private final void removeRecentFiles() {
        final Runnable runnable = new Runnable() {
            public void run() {
                final IContributionItem[] items = getItems();

                for (int i = indexOf(RECENT_FILES_PREFS_KEY) + 1; i < items.length; i++) {
                    IContributionItem item = items[i];
                    if (item.isGroupMarker()) break;
                    remove(item);
                }
            }
        };

        if (Display.getCurrent() == null) {
            Display.getDefault().asyncExec(runnable);
        }
        else {
            runnable.run();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.util.prefs.PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
     */
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (log.isDebugEnabled()) log.debug("Prefs changed " + evt.getKey()+ " '"+evt.getNewValue()+"'");

        if (RECENT_FILES_PREFS_KEY.equals(evt.getKey())) {
            removeRecentFiles();
            addRecentFiles();
        }
    }
}