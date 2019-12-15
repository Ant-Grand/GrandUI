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
package net.ggtools.grand.ui.widgets.property;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import net.ggtools.grand.ui.event.Dispatcher;
import net.ggtools.grand.ui.event.EventManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Specialized list for property pairs.
 *
 * @author Christophe Labouisse
 */
class PropertyList {
    /**
     * Logger for this class.
     */
    private static final Log LOG = LogFactory.getLog(PropertyList.class);

    /**
     * Field allPropertiesChangedDispatcher.
     */
    @SuppressWarnings("unused")
    private final Dispatcher allPropertiesChangedDispatcher;

    /**
     * Field clearedPropertiesDispatcher.
     */
    private final Dispatcher clearedPropertiesDispatcher;

    /**
     * Field propertyAddedDispatcher.
     */
    private final Dispatcher propertyAddedDispatcher;

    /**
     * Field propertyChangedDispatcher.
     */
    private final Dispatcher propertyChangedDispatcher;

    /**
     * Field propertyRemovedDispatcher.
     */
    private final Dispatcher propertyRemovedDispatcher;

    /**
     * Field eventManager.
     */
    private final EventManager eventManager;

    /**
     * Field pairList.
     */
    private final Set<PropertyPair> pairList = new HashSet<>();

    /**
     * Constructor for PropertyList.
     */
    public PropertyList() {
        eventManager = new EventManager("PropertyList event manager");
        try {
            propertyChangedDispatcher = eventManager.createDispatcher(PropertyChangedListener.class
                    .getDeclaredMethod("propertyChanged", PropertyPair.class));
            propertyAddedDispatcher = eventManager.createDispatcher(PropertyChangedListener.class
                    .getDeclaredMethod("propertyAdded", PropertyPair.class));
            propertyRemovedDispatcher = eventManager.createDispatcher(PropertyChangedListener.class
                    .getDeclaredMethod("propertyRemoved", PropertyPair.class));
            clearedPropertiesDispatcher = eventManager
                    .createDispatcher(PropertyChangedListener.class.getDeclaredMethod(
                            "clearedProperties", Object.class));
            allPropertiesChangedDispatcher = eventManager
                    .createDispatcher(PropertyChangedListener.class.getDeclaredMethod(
                            "allPropertiesChanged", Object.class));
        } catch (final SecurityException e) {
            LOG.fatal("Caught exception initializing PropertyList", e);
            throw new RuntimeException("Cannot instantiate PropertyList", e);
        } catch (final NoSuchMethodException e) {
            LOG.fatal("Caught exception initializing PropertyList", e);
            throw new RuntimeException("Cannot instantiate PropertyList", e);
        }
    }

    /**
     * Method addAll.
     * @param properties Properties
     */
    public void addAll(final Properties properties) {
        for (final Entry<Object, Object> entry : properties.entrySet()) {
            pairList.add(new PropertyPair(entry));
        }

    }

    /**
     * Method addProperty.
     */
    public void addProperty() {
        add(new PropertyPair("new property", ""));
    }

    /**
     * Method addPropertyChangedListener.
     * @param listener PropertyChangedListener
     */
    public void addPropertyChangedListener(final PropertyChangedListener listener) {
        eventManager.subscribe(listener);
    }

    /**
     * Method clear.
     */
    public void clear() {
        pairList.clear();
        clearedPropertiesDispatcher.dispatch(null);
    }

    /**
     * Method getAsProperties.
     * @return Properties
     */
    public Properties getAsProperties() {
        final Properties props = new Properties();
        for (final PropertyPair pair : pairList) {
            props.setProperty(pair.getName(), pair.getValue());
        }
        return props;
    }

    /**
     * Method remove.
     * @param pair PropertyPair
     */
    public void remove(final PropertyPair pair) {
        pairList.remove(pair);
        propertyRemovedDispatcher.dispatch(pair);
    }

    /**
     * Method removePropertyChangedListener.
     * @param listener PropertyChangedListener
     */
    public void removePropertyChangedListener(final PropertyChangedListener listener) {
        eventManager.unSubscribe(listener);
    }

    /**
     * Method toArray.
     * @return PropertyPair[]
     */
    public PropertyPair[] toArray() {
        return pairList.toArray(new PropertyPair[pairList.size()]);
    }

    /**
     * Method toString.
     * @return String
     */
    @Override
    public String toString() {
        final StringBuilder strBuff = new StringBuilder();
        for (final PropertyPair pair : pairList) {
            strBuff.append(pair.getName()).append(" => '").append(pair.getValue()).append("'\n");
        }
        return strBuff.toString();
    }

    /**
     * @param pair PropertyPair
     */
    public void update(final PropertyPair pair) {
        propertyChangedDispatcher.dispatch(pair);
    }

    /**
     * @param pair PropertyPair
     */
    private void add(final PropertyPair pair) {
        pairList.add(pair);
        propertyAddedDispatcher.dispatch(pair);
    }
}
