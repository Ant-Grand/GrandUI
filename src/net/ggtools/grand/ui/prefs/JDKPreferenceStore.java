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
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A preference store usign JDK 1.4 preferences API as a storage.
 * 
 * @author Christophe Labouisse
 */
public class JDKPreferenceStore implements IPersistentPreferenceStore, PreferenceChangeListener {

    /**
     * @author Christophe Labouisse
     */
    private static class DefaultValue {
        public final static DefaultValue DEFAULT_DEFAULT_VALUE = new DefaultValue();

        private boolean booleanValue = BOOLEAN_DEFAULT_DEFAULT;

        private double doubleValue = DOUBLE_DEFAULT_DEFAULT;

        private long longValue = LONG_DEFAULT_DEFAULT;

        private String stringValue = STRING_DEFAULT_DEFAULT;

        /**
         * @return Returns the booleanValue.
         */
        public final boolean getBooleanValue() {
            return booleanValue;
        }

        /**
         * @return Returns the doubleValue.
         */
        public final double getDoubleValue() {
            return doubleValue;
        }

        /**
         * @return Returns the longValue.
         */
        public final long getLongValue() {
            return longValue;
        }

        /**
         * @return Returns the stringValue.
         */
        public final String getStringValue() {
            return stringValue;
        }

        /**
         * @param booleanValue
         *            The booleanValue to set.
         */
        public final void setValue(boolean booleanValue) {
            clear();
            this.booleanValue = booleanValue;
            stringValue = Boolean.toString(booleanValue);
        }

        /**
         * @param doubleValue
         *            The doubleValue to set.
         */
        public final void setValue(double doubleValue) {
            clear();
            this.doubleValue = doubleValue;
            stringValue = Double.toString(doubleValue);
        }

        /**
         * @param longValue
         *            The longValue to set.
         */
        public final void setValue(long longValue) {
            clear();
            this.longValue = longValue;
            doubleValue = longValue;
            stringValue = Long.toString(longValue);
        }

        /**
         * @param stringValue
         *            The stringValue to set.
         */
        public final void setValue(String stringValue) {
            clear();
            this.stringValue = stringValue;
            try {
                doubleValue = Double.parseDouble(stringValue);
            } catch (final NumberFormatException e) {
            }
            try {
                longValue = Long.parseLong(stringValue);
            } catch (final NumberFormatException e) {
            }
            booleanValue = Boolean.getBoolean(stringValue);
        }

        private final void clear() {
            longValue = LONG_DEFAULT_DEFAULT;
            booleanValue = BOOLEAN_DEFAULT_DEFAULT;
            doubleValue = DOUBLE_DEFAULT_DEFAULT;
            stringValue = STRING_DEFAULT_DEFAULT;
        }
    }

    private static final Log LOG = LogFactory.getLog(JDKPreferenceStore.class);

    private final Map defaultValues = new HashMap();

    /**
     * List of registered listeners (element type:
     * <code>IPropertyChangeListener</code>). These listeners are to be
     * informed when the current value of a preference changes.
     */
    private ListenerList listeners = new ListenerList();

    private final Preferences prefNode;

    /**
     *  
     */
    public JDKPreferenceStore(final Preferences prefNode) {
        super();
        this.prefNode = prefNode;
        prefNode.addPreferenceChangeListener(this);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        listeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
     */
    public boolean contains(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String,
     *      java.lang.Object, java.lang.Object)
     */
    public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
        final Object[] finalListeners = this.listeners.getListeners();
        // Do we need to fire an event.
        if (finalListeners.length > 0 && (oldValue == null || !oldValue.equals(newValue))) {
            final PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
            for (int i = 0; i < finalListeners.length; ++i) {
                IPropertyChangeListener l = (IPropertyChangeListener) finalListeners[i];
                l.propertyChange(pe);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
     */
    public boolean getBoolean(final String name) {
        return prefNode.getBoolean(name, getDefaultValue(name).getBooleanValue());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
     */
    public boolean getDefaultBoolean(final String name) {
        return getDefaultValue(name).getBooleanValue();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
     */
    public double getDefaultDouble(final String name) {
        return getDefaultValue(name).getDoubleValue();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
     */
    public float getDefaultFloat(final String name) {
        return (float) getDefaultValue(name).getDoubleValue();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
     */
    public int getDefaultInt(final String name) {
        return (int) getDefaultValue(name).getLongValue();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
     */
    public long getDefaultLong(String name) {
        return getDefaultValue(name).getLongValue();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
     */
    public String getDefaultString(final String name) {
        return getDefaultValue(name).getStringValue();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
     */
    public double getDouble(final String name) {
        return prefNode.getDouble(name, getDefaultValue(name).getDoubleValue());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
     */
    public float getFloat(final String name) {
        return prefNode.getFloat(name, (float) getDefaultValue(name).getDoubleValue());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
     */
    public int getInt(final String name) {
        return prefNode.getInt(name, (int) getDefaultValue(name).getLongValue());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
     */
    public long getLong(final String name) {
        return prefNode.getLong(name, getDefaultValue(name).getLongValue());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
     */
    public String getString(final String name) {
        return prefNode.get(name, getDefaultValue(name).getStringValue());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
     */
    public boolean isDefault(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
     */
    public boolean needsSaving() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String,
     *      java.lang.String)
     */
    public void putValue(String name, String value) {
        prefNode.put(name, value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        listeners.remove(listener);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPersistentPreferenceStore#save()
     */
    public void save() throws IOException {
        try {
            prefNode.flush();
        } catch (BackingStoreException e) {
            final String message = "Cannot flush preference to disk";
            LOG.error(message,e);
            throw new RuntimeException(message,e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
     *      boolean)
     */
    public void setDefault(String name, boolean value) {
        getOrCreateDefaultValue(name).setValue(value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
     *      double)
     */
    public void setDefault(String name, double value) {
        getOrCreateDefaultValue(name).setValue(value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
     *      float)
     */
    public void setDefault(String name, float value) {
        getOrCreateDefaultValue(name).setValue(value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
     *      int)
     */
    public void setDefault(String name, int value) {
        getOrCreateDefaultValue(name).setValue(value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
     *      long)
     */
    public void setDefault(String name, long value) {
        getOrCreateDefaultValue(name).setValue(value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
     *      java.lang.String)
     */
    public void setDefault(String name, String defaultObject) {
        getOrCreateDefaultValue(name).setValue(defaultObject);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
     */
    public void setToDefault(String name) {
        prefNode.put(name, getDefaultValue(name).getStringValue());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String,
     *      boolean)
     */
    public void setValue(String name, boolean value) {
        prefNode.putBoolean(name, value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String,
     *      double)
     */
    public void setValue(String name, double value) {
        prefNode.putDouble(name, value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String,
     *      float)
     */
    public void setValue(String name, float value) {
        prefNode.putFloat(name, value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String,
     *      int)
     */
    public void setValue(String name, int value) {
        prefNode.putInt(name, value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String,
     *      long)
     */
    public void setValue(String name, long value) {
        prefNode.putLong(name, value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String,
     *      java.lang.String)
     */
    public void setValue(String name, String value) {
        prefNode.put(name, value);
    }

    /**
     * Gets the default value for a key or a <em>default default</em> value.
     * @param key
     *            to search
     * @return the DefaultValue object for the key or
     *         DefaultValue.DEFAULT_DEFAULT_VALUE.
     */
    protected DefaultValue getDefaultValue(final String key) {
        DefaultValue result = (DefaultValue) defaultValues.get(key);
        if (result == null) result = DefaultValue.DEFAULT_DEFAULT_VALUE;
        return result;
    }

    /**
     * Gets the default value for a key or creates a new one.
     * @param key
     * @return
     */
    protected DefaultValue getOrCreateDefaultValue(final String key) {
        DefaultValue result = (DefaultValue) defaultValues.get(key);
        if (result == null) {
            result = new DefaultValue();
            defaultValues.put(key, result);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.util.prefs.PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
     */
    public void preferenceChange(final PreferenceChangeEvent evt) {
        LOG.trace("PreferenceChangeEvent '" + evt.getKey() + "' '" + evt.getNewValue() + "' '"
                + evt.getNode().get(evt.getKey(), "not set") + "'");
        firePropertyChangeEvent(evt.getKey(),null,evt.getNewValue());
    }
}