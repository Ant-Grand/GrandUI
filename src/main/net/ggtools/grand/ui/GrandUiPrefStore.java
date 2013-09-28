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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.ggtools.grand.ui.prefs.ComplexPreferenceStore;
import net.ggtools.grand.ui.prefs.GraphPreferencePage;
import net.ggtools.grand.ui.prefs.LinksPreferencePage;
import net.ggtools.grand.ui.prefs.NodesPreferencePage;
import net.ggtools.grand.ui.prefs.PreferenceKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Christophe Labouisse
 */
public class GrandUiPrefStore extends ComplexPreferenceStore {
    /**
     * Logger for this class.
     */
    private static final Log LOG = LogFactory.getLog(GrandUiPrefStore.class);

    /**
     * Field baseDir.
     */
    private final File baseDir;

    /**
     * Constructor for GrandUiPrefStore.
     * @throws IOException
     */
    GrandUiPrefStore() throws IOException {
        super();
        baseDir = new File(System.getProperty("user.home"), ".grandui");
        final File destFile = new File(baseDir, "ui.prefs");
        setPrefFile(destFile);
        setDefaults();
        if (destFile.isFile()) {
            load();
        } else {
            migratePreferences();
        }
    }

    /**
     * Method save.
     * @throws IOException
     * @see org.eclipse.jface.preference.IPersistentPreferenceStore#save()
     */
    @Override
    public final void save() throws IOException {
        if (!baseDir.isDirectory()) {
            baseDir.mkdirs();
            if (!baseDir.isDirectory()) {
                throw new FileNotFoundException("Cannot find/create " + baseDir);
            }
        }

        super.save();
    }

    /**
     * Method migratePreferences.
     */
    private void migratePreferences() {
        // Try to get data from the old preference store.
        final Preferences node = Preferences.userNodeForPackage(GrandUiPrefStore.class);
        final String[] keys;
        try {
            keys = node.keys();
            for (final String key : keys) {
                putValue(key, node.get(key, "SHOULD NOT APPEAR"));
            }
            save();
            node.removeNode();
        } catch (final BackingStoreException e) {
            LOG.warn("Cannot retrieve previous preferences", e);
        } catch (final IOException e) {
            LOG.error("Cannot save preferences after migration", e);
        }
    }

    /**
     * Method setDefaults.
     */
    private void setDefaults() {
        setDefault(PreferenceKeys.MAX_RECENT_FILES_PREFS_KEY, 4);
        GraphPreferencePage.setDefaults(this);
        NodesPreferencePage.setDefaults(this);
        LinksPreferencePage.setDefaults(this);
    }
}
