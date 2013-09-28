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

import org.apache.commons.logging.Log;

/**
 * @author Christophe Labouisse
 */
final class UILogger implements Log {

    /**
     * Field underlying.
     */
    private final Log underlying;

    /**
     * Field name.
     */
    private final String name;

    /**
     * Field logBuffer.
     */
    private LogEventBufferImpl logBuffer;

    /**
     *
     * @param name String
     * @param logger Log
     */
    UILogger(final String name, final Log logger) {
        this.name = name;
        underlying = logger;
        logBuffer = LogEventBufferImpl.getInstance();
    }

    /**
     * @param message Object
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    public void debug(final Object message) {
        underlying.debug(message);
        logBuffer.addLogEvent(LogEvent.DEBUG, name, message);
    }

    /**
     * @param message Object
     * @param t Throwable
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    public void debug(final Object message, final Throwable t) {
        underlying.debug(message, t);
        logBuffer.addLogEvent(LogEvent.DEBUG, name, message, t);
    }

    /**
     * @param message Object
     * @see org.apache.commons.logging.Log#error(Object)
     */
    public void error(final Object message) {
        underlying.error(message);
        logBuffer.addLogEvent(LogEvent.ERROR, name, message);
    }

    /**
     * @param message Object
     * @param t Throwable
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    public void error(final Object message, final Throwable t) {
        underlying.error(message, t);
        logBuffer.addLogEvent(LogEvent.ERROR, name, message, t);
    }

    /**
     * @param message Object
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    public void fatal(final Object message) {
        underlying.fatal(message);
        logBuffer.addLogEvent(LogEvent.FATAL, name, message);
    }

    /**
     * @param message Object
     * @param t Throwable
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    public void fatal(final Object message, final Throwable t) {
        underlying.fatal(message, t);
        logBuffer.addLogEvent(LogEvent.FATAL, name, message, t);
    }

    /**
     * @param message Object
     * @see org.apache.commons.logging.Log#info(Object)
     */
    public void info(final Object message) {
        underlying.info(message);
        logBuffer.addLogEvent(LogEvent.INFO, name, message);
    }

    /**
     * @param message Object
     * @param t Throwable
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    public void info(final Object message, final Throwable t) {
        underlying.info(message, t);
        logBuffer.addLogEvent(LogEvent.INFO, name, message, t);
    }

    /**
     * @return boolean
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return underlying.isDebugEnabled();
    }

    /**
     * @return boolean
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return underlying.isErrorEnabled();
    }

    /**
     * @return boolean
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    public boolean isFatalEnabled() {
        return underlying.isFatalEnabled();
    }

    /**
     * @return boolean
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return underlying.isInfoEnabled();
    }

    /**
     * @return boolean
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return underlying.isTraceEnabled();
    }

    /**
     * @return boolean
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return underlying.isWarnEnabled();
    }

    /**
     * @param message Object
     * @see org.apache.commons.logging.Log#trace(Object)
     */
    public void trace(final Object message) {
        underlying.trace(message);
        logBuffer.addLogEvent(LogEvent.TRACE, name, message);
    }

    /**
     * @param message Object
     * @param t Throwable
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    public void trace(final Object message, final Throwable t) {
        underlying.trace(message, t);
        logBuffer.addLogEvent(LogEvent.TRACE, name, message, t);
    }

    /**
     * @param message Object
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    public void warn(final Object message) {
        underlying.warn(message);
        logBuffer.addLogEvent(LogEvent.WARNING, name, message);
    }

    /**
     * @param message Object
     * @param t Throwable
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    public void warn(final Object message, final Throwable t) {
        underlying.warn(message, t);
        logBuffer.addLogEvent(LogEvent.WARNING, name, message, t);
    }
}
