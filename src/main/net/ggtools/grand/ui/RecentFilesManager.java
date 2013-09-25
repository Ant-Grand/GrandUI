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
package net.ggtools.grand.ui;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.ggtools.grand.ui.prefs.PreferenceKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Singleton class managing the recent files data.
 * 
 * @author Christophe Labouisse
 */
public class RecentFilesManager implements IPropertyChangeListener, PreferenceKeys {
    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(RecentFilesManager.class);

    private static RecentFilesManager instance;

    /**
     * The maximum number of files to keep in the recent files menu. A
     * <code>-1</code> should be intepreted as need to load from the
     * preferences store.
     */
    private int maxFiles = -1;

    private final LinkedList<String> recentFiles = new LinkedList<String>();

    private final GrandUiPrefStore preferenceStore;

    private final Collection<RecentFilesListener> subscribers;

    private final List<String> readOnlyRecentFiles;

    /**
     * Private constructor.
     */
    private RecentFilesManager() {
        subscribers = new HashSet<RecentFilesListener>();
        readOnlyRecentFiles = Collections.unmodifiableList(recentFiles);
        preferenceStore = Application.getInstance().getPreferenceStore();
        loadRecentFiles();
    }

    public void addListener(final RecentFilesListener listener) {
        if (!subscribers.contains(listener)) {
            subscribers.add(listener);
            listener.refreshRecentFiles(getRecentFiles());
        }
    }

    public void removeListener(final RecentFilesListener listener) {
        subscribers.remove(listener);
    }

    private void notifyListeners() {
        for (final Iterator<RecentFilesListener> iter = subscribers.iterator(); iter.hasNext();) {
            final RecentFilesListener listener = iter.next();
            listener.refreshRecentFiles(getRecentFiles());
        }
    }

    /**
     * Get the singleton instance.
     * 
     * @return
     */
    public static RecentFilesManager getInstance() {
        if (instance == null) {
            instance = new RecentFilesManager();
        }

        return instance;
    }

    /**
     * Add a new file to the recent file list.
     * 
     * @param fileName
     */
    public void addNewFile(final File file) {
        addNewFile(file, null);
    }

    /**
     * Add a new file to the recent file list specifying the properties used
     * when it was opened.
     * 
     * @param fileName
     * @param properties
     */
    public void addNewFile(final File file, final Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Adding " + file + " to recent files");
        }
        final String fileName = file.getAbsolutePath();
        recentFiles.remove(fileName);
        recentFiles.addFirst(fileName);
        removeExcessFiles();
        preferenceStore.setValue(RECENT_FILES_PREFS_KEY, recentFiles);
        if (properties == null) {
            preferenceStore.setPropertiesToDefault(getKeyForProperties(fileName));
        }
        else {
            preferenceStore.setValue(getKeyForProperties(fileName), properties);
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Saving recent files");
            }
            preferenceStore.save();
        } catch (final IOException e) {
            log.error("Cannot save recent files", e);
        }
        notifyListeners();
    }

    /**
     * Update the properties for a specific file.
     * 
     * @param file
     * @param properties
     */
    public void updatePropertiesFor(final File file, final Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Updating properties for " + file);
        }
        final String fileName = file.getAbsolutePath();
        if (recentFiles.contains(fileName)) {
            if (properties == null) {
                preferenceStore.setPropertiesToDefault(getKeyForProperties(fileName));
            }
            else {
                preferenceStore.setValue(getKeyForProperties(fileName), properties);
            }
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Saving recent files");
                }
                preferenceStore.save();
            } catch (final IOException e) {
                log.error("Cannot save recent files", e);
            }
        }
    }

    /**
     * @param fileName
     * @return
     */
    private String getKeyForProperties(final String fileName) {
        return RECENT_FILES_PREFS_KEY + ".properties." + fileName;
    }

    /**
     * Clear the recent files list.
     * 
     */
    public void clear() {
        recentFiles.clear();
        preferenceStore.setToDefault(RECENT_FILES_PREFS_KEY);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Saving recent files");
            }
            preferenceStore.save();
        } catch (final IOException e) {
            log.warn("Cannot save recent files", e);
        }
        notifyListeners();
    }

    /**
     * Load the recent files from the persistent storage to the local list.
     */
    private final void loadRecentFiles() {
        maxFiles = preferenceStore.getInt(MAX_RECENT_FILES_PREFS_KEY);
        recentFiles.clear();
        recentFiles.addAll(preferenceStore.getCollection(RECENT_FILES_PREFS_KEY, maxFiles));
        notifyListeners();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(final PropertyChangeEvent event) {
        final String changedProperty = event.getProperty();
        if (log.isDebugEnabled()) {
            log.debug("Prefs changed " + changedProperty + " '" + event.getNewValue() + "'");
        }

        if (MAX_RECENT_FILES_PREFS_KEY.equals(changedProperty)) {
            maxFiles = preferenceStore.getInt(MAX_RECENT_FILES_PREFS_KEY);
            removeExcessFiles();
            preferenceStore.setValue(RECENT_FILES_PREFS_KEY, recentFiles);
        }
    }

    /**
     * 
     */
    private void removeExcessFiles() {
        while (recentFiles.size() > maxFiles) {
            recentFiles.removeLast();
        }
    }

    /**
     * Get the recent files.
     * 
     * @return
     */
    public final Collection<String> getRecentFiles() {
        return readOnlyRecentFiles;
    }

    public Properties getProperties(final File file) {
        if (file == null) {
            return null;
        }
        else {
            return getProperties(file.getAbsolutePath());
        }
    }

    public Properties getProperties(final String fileName) {
        return preferenceStore.getProperties(getKeyForProperties(fileName));
    }
}
