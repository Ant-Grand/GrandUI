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
package net.ggtools.grand.ui.log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.ggtools.grand.ui.log.LogEvent.Level;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A singleton class used to store the log events generated from the start of
 * the application.
 * 
 * @author Christophe Labouisse
 */
public class LogEventBufferImpl implements LogEventBuffer {

    private static final long serialVersionUID = 4050761593883343159L;

    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(LogEventBufferImpl.class);

    private final LinkedList eventList = new LinkedList();

    private int eventCounter = 0;

    private static LogEventBufferImpl instance;

    private transient LogEventListener listener;

    /**
     * Creates a new buffer.
     */
    private LogEventBufferImpl() {
        super();
    }

    /**
     * Get the singleton instance.
     * 
     * @return the instance.
     */
    public static LogEventBufferImpl getInstance() {
        if (instance == null) {
            instance = new LogEventBufferImpl();
        }
        return instance;
    }

    /**
     * Return an unmodifiable list.
     * 
     * @return
     */
    public List getEventList() {
        return Collections.unmodifiableList(eventList);
    }

    void addLogEvent(final Level level, final String originator, final Object message) {
        addLogEvent(level, originator, message, null);
    }

    synchronized void addLogEvent(final Level level, final String originator, final Object message,
            final Throwable exception) {
        final LogEvent logEvent = new LogEvent(eventCounter++, level, originator, message,
                exception);
        eventList.addLast(logEvent);
        if (listener != null) listener.logEventReceived(logEvent);
    }

    public void addListener(final LogEventListener newListener) {
        listener = newListener;
    }

    public void removeListener(final LogEventListener toRemove) {
        if (listener == toRemove) listener = null;
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.log.LogEventBuffer#clearLogEvents()
     */
    synchronized public void clearLogEvents() {
        eventList.clear();
    }
}
