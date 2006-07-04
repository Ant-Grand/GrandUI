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

import java.io.Serializable;

/**
 * A class storing all data related to a log event.
 * 
 * @author Christophe Labouisse
 */
public class LogEvent implements Serializable {

    /**
     * A log level.
     */
    public static final class Level implements Serializable {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3257003276267435833L;

        public final String name;

        public final int value;

        /**
         * Creates a new log level.
         * @param value
         * @param name
         */
        private Level(final int value, final String name) {
            this.value = value;
            this.name = name;
        }
    }

    public static final Level DEBUG = new Level(2, "DEBUG");

    public static final Level ERROR = new Level(5, "ERROR");

    public static final Level FATAL = new Level(6, "FATAL");

    public static final Level INFO = new Level(3, "INFO");

    public static final Level TRACE = new Level(1, "TRACE");

    public static final Level WARNING = new Level(4, "WARNING");

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3545794399121453874L;

    private Throwable exception;

    private Level level;

    private Object message;

    private String originator;

    private long time;

    /**
     * 
     */
    public LogEvent(final Level level, final String originator, final Object message,
            final Throwable exception) {
        this.level = level;
        this.originator = originator;
        this.message = message;
        this.exception = exception;
        time = System.currentTimeMillis();
    }

    /**
     * @return Returns the exception.
     */
    public final Throwable getException() {
        return exception;
    }

    /**
     * @return Returns the level.
     */
    public final Level getLevel() {
        return level;
    }

    /**
     * @return Returns the message.
     */
    public final Object getMessage() {
        return message;
    }

    /**
     * @return Returns the originator.
     */
    public final String getOriginator() {
        return originator;
    }

    /**
     * @return Returns the time.
     */
    public final long getTime() {
        return time;
    }

    /**
     * @param exception
     *            The exception to set.
     */
    final void setException(final Throwable exception) {
        this.exception = exception;
    }

    /**
     * @param level
     *            The level to set.
     */
    final void setLevel(final Level level) {
        this.level = level;
    }

    /**
     * @param message
     *            The message to set.
     */
    final void setMessage(final String message) {
        this.message = message;
    }

    /**
     * @param originator
     *            The originator to set.
     */
    final void setOriginator(final String originator) {
        this.originator = originator;
    }

    /**
     * @param time
     *            The time to set.
     */
    final void setTime(final long time) {
        this.time = time;
    }
}
