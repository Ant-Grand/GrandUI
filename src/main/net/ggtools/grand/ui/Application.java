// $Id$
/*
 * ====================================================================
 * Copyright (c) 2002-2003, Christophe Labouisse All rights reserved.
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

package net.ggtools.grand.ui;

import java.io.IOException;
import java.util.Properties;

import net.ggtools.grand.Configuration;
import net.ggtools.grand.log.LoggerManager;
import net.ggtools.grand.ui.log.CommonsLoggingLoggerFactory;
import net.ggtools.grand.ui.widgets.ExceptionDialog;
import net.ggtools.grand.ui.widgets.GraphWindow;
import net.ggtools.grand.ui.widgets.Splash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Singleton holding the application data.
 *
 * @author Christophe Labouisse
 */
public class Application implements Runnable {

    /**
     * Field ABOUT_DIALOG_IMAGE.
     * (value is ""net.ggtools.grand.ui.aboutimage"")
     */
    public static final String ABOUT_DIALOG_IMAGE =
            "net.ggtools.grand.ui.aboutimage";

    /**
     * Field APPLICATION_ICON.
     * (value is ""net.ggtools.grand.ui.appicon"")
     */
    public static final String APPLICATION_ICON =
            "net.ggtools.grand.ui.appicon";

    /**
     * Field GRAPH_FONT.
     * (value is ""net.ggtools.grand.ui.graphfont"")
     */
    public static final String GRAPH_FONT =
            "net.ggtools.grand.ui.graphfont";

    /**
     * Field LINK_FONT.
     * (value is ""net.ggtools.grand.ui.linkfont"")
     */
    public static final String LINK_FONT =
            "net.ggtools.grand.ui.linkfont";

    /**
     * Field LINK_ICON.
     * (value is ""net.ggtools.grand.ui.linkicon"")
     */
    public static final String LINK_ICON =
            "net.ggtools.grand.ui.linkicon";

    /**
     * Field MONOSPACE_FONT.
     * (value is ""net.ggtools.grand.ui.monospacefont"")
     */
    public static final String MONOSPACE_FONT =
            "net.ggtools.grand.ui.monospacefont";

    /**
     * Field NODE_FONT.
     * (value is ""net.ggtools.grand.ui.nodefont"")
     */
    public static final String NODE_FONT =
            "net.ggtools.grand.ui.nodefont";

    /**
     * Field NODE_ICON.
     * (value is ""net.ggtools.grand.ui.nodeicon"")
     */
    public static final String NODE_ICON =
            "net.ggtools.grand.ui.nodeicon";

    /**
     * Field TOOLTIP_FONT.
     * (value is ""net.ggtools.grand.ui.tooltipfont"")
     */
    public static final String TOOLTIP_FONT =
            "net.ggtools.grand.ui.tooltipfont";

    /**
     * Field TOOLTIP_MONOSPACE_FONT.
     * (value is ""net.ggtools.grand.ui.tooltipmonospacefont"")
     */
    public static final String TOOLTIP_MONOSPACE_FONT =
            "net.ggtools.grand.ui.tooltipmonospacefont";

    /**
     * Field log.
     */
    private static final Log LOG = LogFactory.getLog(Application.class);

    /**
     * Field singleton.
     */
    private static Application singleton;

    /**
     * Method getInstance.
     * @return Application
     */
    public static Application getInstance() {
        return singleton;
    }

    /**
     * Method main.
     * @param args String[]
     */
    public static void main(final String[] args) {
        try {
            Thread.currentThread().setName("Display thread");
            final Application application = new Application();
            application.run();
        } catch (final Throwable e) {
            LOG.fatal("Cannot run application", e);
        }
        LOG.info("Exiting ...");
        System.exit(0);
    }

    /**
     * Field buildProperties.
     */
    private final Properties buildProperties;

    /**
     * Field fontRegistry.
     */
    private FontRegistry fontRegistry;

    /**
     * Field imageRegistry.
     */
    private ImageRegistry imageRegistry;

    /**
     * Field preferenceStore.
     */
    private GrandUiPrefStore preferenceStore;

    /**
     * Field versionString.
     */
    private final String versionString;

    /**
     * Constructor for Application.
     * @throws IOException when error occurs in load()
     */
    public Application() throws IOException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Creating new application");
        }
        singleton = this;
        buildProperties = new Properties();
        buildProperties.load(getClass().getResourceAsStream("buildnum.properties"));
        versionString = "v" + buildProperties.getProperty("build.version.string")
                + " (build " + buildProperties.getProperty("build.number") + " "
                + buildProperties.getProperty("build.date") + ")";
    }

    /**
     * @param symbolicName String
     * @return Font
     */
    public final Font getBoldFont(final String symbolicName) {
        return fontRegistry.getBold(symbolicName);
    }

    /**
     * @return Returns the buildProperties.
     */
    public final Properties getBuildProperties() {
        return buildProperties;
    }

    /**
     * @param symbolicName String
     * @return Font
     */
    public final Font getFont(final String symbolicName) {
        return fontRegistry.get(symbolicName);
    }

    /**
     * @return Returns the fontRegistry.
     */
    public final FontRegistry getFontRegistry() {
        return fontRegistry;
    }

    /**
     * @param key String
     * @return Image
     */
    public final Image getImage(final String key) {
        return imageRegistry.get(key);
    }

    /**
     * @return Returns the imageRegistry.
     */
    public final ImageRegistry getImageRegistry() {
        return imageRegistry;
    }

    /**
     * @param symbolicName String
     * @return Font
     */
    public final Font getItalicFont(final String symbolicName) {
        return fontRegistry.getItalic(symbolicName);
    }

    /**
     * @return Returns the preferenceStore.
     */
    public final GrandUiPrefStore getPreferenceStore() {
        return preferenceStore;
    }

    /**
     * @return Returns the versionString.
     */
    public final String getVersionString() {
        return versionString;
    }

    /**
     * Initializes the application resources. This method must be called from an
     * active display thread.
     *
     * @throws IOException when error occurs in GrandUIPrefStore
     */
    private void initResources() throws IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Initializing application resources");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing preferences");
        }
        preferenceStore = new GrandUiPrefStore();
        // TODO init with default values.

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing font registry");
        }
        fontRegistry = new FontRegistry("net.ggtools.grand.ui.resource.fonts");
        for (final Object key : fontRegistry.getKeySet()) {
            fontRegistry.get((String) key);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing image registry");
        }
        imageRegistry = new ImageRegistry();

        imageRegistry.put(ABOUT_DIALOG_IMAGE, ImageDescriptor.createFromFile(Application.class,
                "resource/about.png"));
        imageRegistry.put(APPLICATION_ICON, ImageDescriptor.createFromFile(Application.class,
                "resource/application.png"));
        imageRegistry.put(LINK_ICON, ImageDescriptor.createFromFile(Application.class,
                "resource/link-icon.png"));
        imageRegistry.put(NODE_ICON, ImageDescriptor.createFromFile(Application.class,
                "resource/node-icon.png"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing colors");
        }
        // Put the same icons for all windows & dialogs.
        Window.setDefaultImage(getImage(APPLICATION_ICON));

        LoggerManager.setFactory(new CommonsLoggingLoggerFactory());
    }

    /**
     *
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Starting application");
            LOG.info("Version: " + versionString);
            LOG.info("SWT: " + SWT.getPlatform() + " v" + SWT.getVersion());
            Configuration coreConfiguration = null;
            try {
                coreConfiguration = Configuration.getConfiguration();
                LOG.info("Core: " + coreConfiguration.getVersionString());
                LOG.info("Ant: " + coreConfiguration.getAntVersionString());
            } catch (final IOException e) {
                LOG.error("Error getting core configuration", e);
            }
            LOG.info("JRE: " + System.getProperty("java.vm.name") + " "
                    + System.getProperty("java.vm.version"));
        }
        final Display display = Display.getDefault();
        final Splash splash = new Splash(display, versionString);
        splash.open();
        try {
            initResources();
        } catch (final IOException e) {
            splash.close();
            splash.dispose();
            LOG.error("Caught exception initializing resources", e);
            ExceptionDialog.openException(null, "Cannot load preferences", e);
            throw new RuntimeException("Cannot initialize resources", e);
        }
        final ApplicationWindow mainWindow = new GraphWindow();
        mainWindow.setBlockOnOpen(true);
        splash.close();
        splash.dispose();
        mainWindow.open();
    }
}
