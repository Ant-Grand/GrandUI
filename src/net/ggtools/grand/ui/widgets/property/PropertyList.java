//$Id$
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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

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
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(PropertyList.class);
    
    final LinkedHashSet pairList = new LinkedHashSet();
    
    EventManager eventManager;

    private Dispatcher propertyChangedDispatcher;

    private Dispatcher propertyAddedDispatcher;

    private Dispatcher propertyRemovedDispatcher;

    public PropertyList() {
        eventManager = new EventManager("PropertyList event manager");
        try {
            propertyChangedDispatcher = eventManager.createDispatcher(PropertyChangedListener.class
                    .getDeclaredMethod("propertyChanged", new Class[]{PropertyPair.class}));
            propertyAddedDispatcher = eventManager.createDispatcher(PropertyChangedListener.class
                    .getDeclaredMethod("propertyAdded", new Class[]{PropertyPair.class}));
            propertyRemovedDispatcher = eventManager.createDispatcher(PropertyChangedListener.class
                    .getDeclaredMethod("propertyRemoved", new Class[]{PropertyPair.class}));
        } catch (SecurityException e) {
            log.fatal("Caught exception initializing PropertyList", e);
            throw new RuntimeException("Cannot instanciate PropertyList", e);
        } catch (NoSuchMethodException e) {
            log.fatal("Caught exception initializing PropertyList", e);
            throw new RuntimeException("Cannot instanciate PropertyList", e);
        }
    }

    public PropertyPair[] toArray() {
        return (PropertyPair[]) pairList.toArray(new PropertyPair[pairList.size()]);
    }
    
    public void addProperty() {
        add(new PropertyPair("",""));
    }

    /**
     * @param pair
     */
    private void add(final PropertyPair pair) {
        pairList.add(pair);
        propertyAddedDispatcher.dispatch(pair);
    }
    
    public void addPropertyChangedListener(final PropertyChangedListener listener) {
        eventManager.subscribe(listener);
    }
    
    /**
     * @param pair
     */
    public void update(final PropertyPair pair) {
        propertyChangedDispatcher.dispatch(pair);
    }
    
    public String toString() {
        final StringBuffer strBuff = new StringBuffer();
        for (Iterator iter = pairList.iterator(); iter.hasNext();) {
            PropertyPair pair = (PropertyPair) iter.next();
            strBuff.append(pair.getName()).append(" => '").append(pair.getValue()).append("'\n");
        }
        return strBuff.toString();
    }

    public void remove(PropertyPair pair) {
        pairList.remove(pair);
        propertyRemovedDispatcher.dispatch(pair);
    }

    public void clear() {
        pairList.clear();
    }

    public void addAll(Properties properties) {
        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            add(new PropertyPair(entry));
        }
    }

    public Properties getAsProperties() {
        final Properties props = new Properties();
        for (Iterator iter = pairList.iterator(); iter.hasNext();) {
            PropertyPair pair = (PropertyPair) iter.next();
            props.setProperty(pair.getName(),pair.getValue());
        }
        return props;
    }
}
