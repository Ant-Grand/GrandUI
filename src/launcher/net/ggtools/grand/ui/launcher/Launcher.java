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
package net.ggtools.grand.ui.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Launcher {
    private static final class JarFilenameFilter implements FilenameFilter {
        public boolean accept(final File dir, final String name) {
            return name.endsWith(".jar");
        }
    }

    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(Launcher.class);

    public Launcher() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        try {
            final ArrayList<URL> urlList = new ArrayList<URL>(20);
            addDirectoryJars("/home/moi/prog/Eclipse/GrandUi/dist/grand-ui-0.8pre/lib", urlList);

            final String osName = System.getProperty("os.name").toLowerCase(Locale.US);
            
            if (osName.equals("linux")) {
                addDirectoryJars("/home/moi/prog/Eclipse/GrandUi/dist/grand-ui-0.8pre/lib/linux-gtk", urlList);
            }
            
            final URLClassLoader cl = new URLClassLoader(urlList.toArray(new URL[0]),Launcher.class.getClassLoader());
            Thread.currentThread().setName("Display thread");
            Thread.currentThread().setContextClassLoader(cl);
            final Class clazz = cl.loadClass("net.ggtools.grand.ui.Application");
            log.info("Classloader: " + clazz.getClassLoader());
            final Runnable application = (Runnable) clazz.newInstance();
            application.run();
        } catch (final Throwable e) {
            log.fatal("Cannot run application", e);
        }
        log.info("Exiting ...");
        System.exit(0);
    }

    /**
     * @param directory
     * @param jarList
     * @throws MalformedURLException
     */
    private static void addDirectoryJars(final String directory, final ArrayList<URL> jarList) throws MalformedURLException {
        final File libDir = new File(directory);
        final File[] jars = libDir.listFiles(new JarFilenameFilter());

	if (jars != null) {
        for (File element : jars) {
            jarList.add(element.toURL());
        }
    }
    }

}

}
