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
package net.ggtools.grand.ui.prefs;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

/**
 * A preference store usign JDK 1.4 preferences API as a storage.
 * 
 * @author Christophe Labouisse
 */
public class JDKPreferenceStore extends PreferenceStore implements IPersistentPreferenceStore,
        PreferenceChangeListener {

    private static final Log LOG = LogFactory.getLog(JDKPreferenceStore.class);

    private final Preferences prefNode;

    /**
     *  
     */
    public JDKPreferenceStore(final Preferences prefNode) {
        super();
        this.prefNode = prefNode;
        //prefNode.addPreferenceChangeListener(this);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceStore#load()
     */
    public void load() throws IOException {
        if (LOG.isDebugEnabled()) LOG.debug("Loading preferences");
        try {
            prefNode.sync();
            final String[] keys = prefNode.keys();
            final HashSet keyHash = new HashSet();
            keyHash.addAll(Arrays.asList(preferenceNames()));
            for (int i = 0; i < keys.length; i++) {
                final String key = keys[i];
                putValue(key, prefNode.get(key, ""));
                keyHash.remove(key);
            }

            for (Iterator iter = keyHash.iterator(); iter.hasNext();) {
                setToDefault((String) iter.next());
            }
        } catch (BackingStoreException e) {
            final String message = "Cannot load preferences";
            LOG.error(message, e);
            throw new RuntimeException(message, e);
        }

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPersistentPreferenceStore#save()
     */
    public void save() throws IOException {
        if (LOG.isDebugEnabled()) LOG.debug("Saving preferences");
        try {
            final String[] keys = preferenceNames();
            final HashSet keyHash = new HashSet();
            keyHash.addAll(Arrays.asList(prefNode.keys()));
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (LOG.isTraceEnabled()) LOG.trace("Considering "+key);
                if (!isDefault(key)) {
                    if (LOG.isTraceEnabled()) LOG.trace("Saving "+key);
                    prefNode.put(key, getString(key));
                    keyHash.remove(key);
                }
            }

            for (Iterator iter = keyHash.iterator(); iter.hasNext();) {
                final String key = (String) iter.next();
                if (LOG.isTraceEnabled()) LOG.trace("Removing "+key);
                prefNode.remove(key);
            }
            prefNode.flush();
        } catch (BackingStoreException e) {
            final String message = "Cannot save preferences";
            LOG.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.util.prefs.PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
     */
    public void preferenceChange(final PreferenceChangeEvent evt) {
        LOG.trace("PreferenceChangeEvent '" + evt.getKey() + "' '" + evt.getNewValue() + "' '"
                + evt.getNode().get(evt.getKey(), "not set") + "'");
        firePropertyChangeEvent(evt.getKey(), null, evt.getNewValue());
    }
}