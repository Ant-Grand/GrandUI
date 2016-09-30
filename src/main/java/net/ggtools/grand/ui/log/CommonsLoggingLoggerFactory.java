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

import net.ggtools.grand.log.LoggerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Logger factory used to make Grand's core log using the same mechanism than
 * the UI.
 *
 * @author Christophe Labouisse
 */
public class CommonsLoggingLoggerFactory implements LoggerFactory {

    /**
     * Package instanciation only.
     */
    public CommonsLoggingLoggerFactory() {
    }

    /**
     * Method getLog.
     * @param clazz Class&lt;?&gt;
     * @return Log
     * @see net.ggtools.grand.log.LoggerFactory#getLog(java.lang.Class)
     */
    public final Log getLog(final Class<?> clazz) {
        return LogFactory.getLog(clazz);
    }

    /**
     * Method getLog.
     * @param name String
     * @return Log
     * @see net.ggtools.grand.log.LoggerFactory#getLog(java.lang.String)
     */
    public final Log getLog(final String name) {
        return LogFactory.getLog(name);
    }

}
