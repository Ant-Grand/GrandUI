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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.ggtools.grand.ui.prefs.ComplexPreferenceStore;
import net.ggtools.grand.ui.prefs.GeneralPreferencePage;
import net.ggtools.grand.ui.prefs.GraphPreferencePage;
import net.ggtools.grand.ui.prefs.LinksPreferencePage;
import net.ggtools.grand.ui.prefs.NodesPreferencePage;

/**
 * @author Christophe Labouisse
 */
public class GrandUiPrefStore extends ComplexPreferenceStore {
    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(GrandUiPrefStore.class);

    /**
     * @param unEscapeString(item)
     * @return
     */
    private static String escapeString(final String item) {
        return item.replaceAll("%","%%").replaceAll(",","%,");
    }

    GrandUiPrefStore() throws IOException {
        super("grandui","ui");
        setDefaults();
        if (prefFile.isFile()) {
            load();
        }
        else {
            migratePreferences();
        }
    }

    /**
     * 
     */
    private void migratePreferences() {
        // Try to get data from the old preference store.
        final Preferences node = Preferences.userNodeForPackage(GrandUiPrefStore.class);
        final String[] keys;
        try {
            keys = node.keys();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                putValue(key, node.get(key, "SHOULD NOT APPEAR"));
            }
            save();
            node.removeNode();
        } catch (BackingStoreException e) {
            log.warn("Cannot retrieve previous preferences", e);
        } catch (IOException e) {
            log.error("Cannot save preferences after migration", e);
        }
    }

    /**
     * 
     */
    private void setDefaults() {
        setDefault(GeneralPreferencePage.MAX_RECENT_FILES_PREFS_KEY, 4);
        GraphPreferencePage.setDefaults(this);
        NodesPreferencePage.setDefaults(this);
        LinksPreferencePage.setDefaults(this);
    }
}
