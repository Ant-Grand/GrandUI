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
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import net.ggtools.grand.ui.Application;
import net.ggtools.grand.ui.actions.ClearRecentFilesAction;
import net.ggtools.grand.ui.prefs.PreferenceKeys;
import net.ggtools.grand.ui.widgets.GraphWindow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * 
 * @author Christophe Labouisse
 */
public class RecentFilesMenu extends MenuManager implements IPropertyChangeListener,
        PreferenceKeys {

    /**
     * An action openning a specific file when run.
     * @author Christophe Labouisse
     */
    private class OpenRecentFileAction extends Action {

        private final File file;

        private final GraphWindow window;

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
            window.openGraphInNewDisplayer(file, null);
        }
    }

    private static Collection array;

    private static final Log log = LogFactory.getLog(RecentFilesMenu.class);

    /**
     * The maximum number of files to keep in the recent files menu. A
     * <code>-1</code> should be intepreted as need to load from the
     * preferences store.
     */
    private static int maxFiles = -1;

    //private static final Preferences prefs =
    // Preferences.userNodeForPackage(RecentFilesMenu.class);
    private final static IPersistentPreferenceStore preferenceStore = Application.getInstance()
            .getPreferenceStore();

    private static final String RECENT_FILES_GROUP = "Recent files";

    private final static LinkedList recentFiles = new LinkedList();

    /**
     * Add a new file to the recent files list & the preference store.
     * @param file
     */
    public final static void addNewFile(final File file) {
        if (log.isDebugEnabled()) log.debug("Adding " + file + " to recent files");
        String fileName = file.getAbsolutePath();
        recentFiles.remove(fileName);
        recentFiles.addFirst(fileName);

        while (recentFiles.size() > maxFiles) {
            recentFiles.removeLast();
        }

        final String result = collectionToString(recentFiles);
        preferenceStore.setValue(RECENT_FILES_PREFS_KEY, result);
        try {
            if (log.isDebugEnabled()) log.debug("Saving recent files");
            preferenceStore.save();
        } catch (IOException e) {
            log.warn("Cannot save recent files", e);
        }
    }

    /**
     * @return
     */
    private static String collectionToString(final Collection collection) {
        final StringBuffer buffer = new StringBuffer();
        for (final Iterator iter = collection.iterator(); iter.hasNext();) {
            final String item = (String) iter.next();
            buffer.append(item);
            if (iter.hasNext()) buffer.append(",");
        }
        return buffer.toString();
    }

    private final GraphWindow window;

    /**
     * Creates a new RecentFilesMenu instance loading the recent files from the
     * preference store.
     * @param window
     */
    public RecentFilesMenu(final GraphWindow window) {
        super("Recent files");
        this.window = window;
        add(new ClearRecentFilesAction(this));
        add(new Separator(RECENT_FILES_GROUP));

        loadRecentFiles();
        addRecentFiles();
        preferenceStore.addPropertyChangeListener(this);
    }

    /**
     * Remove recent files from the menu & from the preference store.
     *  
     */
    public final void clear() {
        if (log.isDebugEnabled()) log.debug("Clearing recent files");
        recentFiles.clear();
        preferenceStore.setValue(RECENT_FILES_PREFS_KEY, "");
        try {
            if (log.isDebugEnabled()) log.debug("Saving recent files");
            preferenceStore.save();
        } catch (IOException e) {
            log.warn("Cannot save recent files", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(final PropertyChangeEvent event) {
        final String changedProperty = event.getProperty();
        if (log.isDebugEnabled())
                log.debug("Prefs changed " + changedProperty + " '" + event.getNewValue() + "'");

        if (RECENT_FILES_PREFS_KEY.equals(changedProperty)) {
            removeRecentFiles();
            addRecentFiles();
        }
        else if (MAX_RECENT_FILES_PREFS_KEY.equals(changedProperty)) {
            maxFiles = preferenceStore.getInt(MAX_RECENT_FILES_PREFS_KEY);
            while (recentFiles.size() > maxFiles) {
                recentFiles.removeLast();
            }

            final String result = collectionToString(recentFiles);
            preferenceStore.putValue(RECENT_FILES_PREFS_KEY, result);
        }
    }

    /**
     * @param window
     */
    private final void addRecentFiles() {
        final Runnable runnable = new Runnable() {
            public void run() {
                for (final Iterator iter = recentFiles.iterator(); iter.hasNext();) {
                    final String fileName = (String) iter.next();
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

    /**
     * Load the recent files from the preferences store to the local list.
     */
    private final void loadRecentFiles() {
        // if maxFiles is -1 we need to load the recent files into the local
        // list.
        if (maxFiles == -1) {
            maxFiles = preferenceStore.getInt(MAX_RECENT_FILES_PREFS_KEY);
            StringTokenizer tokenizer = new StringTokenizer(preferenceStore
                    .getString(RECENT_FILES_PREFS_KEY), ",");
            recentFiles.clear();
            for (int i = 0; (i < maxFiles) && tokenizer.hasMoreTokens(); i++) {
                final String fileName = tokenizer.nextToken();
                recentFiles.addLast(fileName);
            }
        }
    }

    /**
     *  
     */
    private final void removeRecentFiles() {
        final Runnable runnable = new Runnable() {
            public void run() {
                final IContributionItem[] items = getItems();

                for (int i = indexOf(RECENT_FILES_GROUP) + 1; i < items.length; i++) {
                    IContributionItem item = items[i];
                    if (item.isGroupMarker()) break;
                    remove(item);
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