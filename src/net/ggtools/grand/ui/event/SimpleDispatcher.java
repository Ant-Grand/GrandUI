// $Id$
/* ====================================================================
 * Copyright (c) 2002-2004, Christophe Labouisse
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.ggtools.grand.ui.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.ggtools.grand.ui.event.EventManager.InternalDispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A simple dispatcher implementation of InternalDispatcher using
 * <code>invoke</code> to actually dispatch the events.
 * 
 * @author Christophe Labouisse
 */
class SimpleDispatcher extends DispatcherAdapter implements Dispatcher,
        InternalDispatcher {
    private final static Log log = LogFactory.getLog(SimpleDispatcher.class);

    private final Method method;

    SimpleDispatcher(EventManager manager, Method method) {
        super(manager);
        this.method = method;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.event.EventManager.Dispatcher#sendEventToSubscriber(java.lang.Object,
     *      java.lang.Object)
     */
    public void sendEventToSubscriber(Object subscriber, Object eventData) {
        try {
            method.invoke(subscriber, new Object[]{eventData});
        } catch (IllegalAccessException e) {
            log.fatal(getEventManager().getName() + " dispatchOneEvent", e);
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            log.error(getEventManager().getName() + " dispatchOneEvent", e);
            throw new RuntimeException(e.getCause());
        }
    }
}
