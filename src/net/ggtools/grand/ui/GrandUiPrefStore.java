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
package net.ggtools.grand.ui;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import net.ggtools.grand.ui.prefs.GeneralPreferencePage;
import net.ggtools.grand.ui.prefs.GraphPreferencePage;
import net.ggtools.grand.ui.prefs.LinksPreferencePage;
import net.ggtools.grand.ui.prefs.NodesPreferencePage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Christophe Labouisse
 */
public class GrandUiPrefStore extends PreferenceStore {

    /**
     * @param unEscapeString(item)
     * @return
     */
    private static String escapeString(final String item) {
        return item.replaceAll("%","%%").replaceAll(",","%,");
    }

    /**
     * @param item
     * @return
     */
    private static String unEscapeString(final String item) {
        return item.replaceAll("%,",",").replaceAll("%%","%");
    }

    final ColorRegistry colorRegistry = new ColorRegistry();

    /**
     * @param unEscapeString(item)
     * @return
     */
    private static String escapeString(final String item) {
        return item.replaceAll("%", "%%").replaceAll(",", "%,");
    }

    /**
     * @param item
     * @return
     */
    private static String unEscapeString(final String item) {
        return item.replaceAll("%,", ",").replaceAll("%%", "%");
    }

    private final File baseDir;

    private final ColorRegistry colorRegistry = new ColorRegistry();

    private final FontRegistry fontRegistry = new FontRegistry();

    private final Set pendingPropertiesRemoval = new HashSet();

    private final Map propertiesTable = new HashMap();

    GrandUiPrefStore() throws IOException {
        super();
        setDefaults();
        baseDir = new File(System.getProperty("user.home"), ".grandui");
        if (!baseDir.isDirectory()) {
            baseDir.mkdirs();
            if (!baseDir.isDirectory()) { throw new FileNotFoundException("Cannot find/create "
                    + baseDir); }
        }
        final File prefFile = new File(baseDir, "ui.prefs");
        setFilename(prefFile.getPath());
        if (prefFile.isFile()) {
            load();
        }
        else {
            migratePreferences();
        }
    }

    /**
     * Get a collection of {@link String}s.
     * @param key
     * @return
     */
    public Collection getCollection(final String key) {
        return getCollection(key, -1);
    }

    /**
     * Get a collection of {@link String}s. The number of elements returned by
     * this method can be tuned by the limit parameter. If the limit is less
     * than the number of stored elements, only the <i>limit</i> first ones
     * will be fetched.
     * 
     * @param key
     * @param limit
     *            maximum size of the returned list or -1 for unlimited.
     * @return
     */
    public Collection getCollection(final String key, int limit) {
        LinkedList list = new LinkedList();
        StringTokenizer tokenizer = new StringTokenizer(getString(key), ",");
        if (limit == -1) limit = tokenizer.countTokens();
        for (int i = 0; (i < limit) && tokenizer.hasMoreTokens(); i++) {
            list.addLast(unEscapeString(tokenizer.nextToken()));
        }
        return list;
    }

   /**
    * 
    */
   private void setDefaults()
   {
      setDefault(GeneralPreferencePage.MAX_RECENT_FILES_PREFS_KEY, 4);
      GraphPreferencePage.setDefaults(this);
      NodesPreferencePage.setDefaults(this);
      LinksPreferencePage.setDefaults(this);
   }

    public Color getColor(final String key) {
        final RGB newRGBColor = PreferenceConverter.getColor(this, key);
        final RGB currentRGBColor = colorRegistry.getRGB(key);
        if (!newRGBColor.equals(currentRGBColor)) {
            colorRegistry.put(key, newRGBColor);
        }
        return colorRegistry.get(key);
    }

    public Font getFont(final String key) {
        final FontData[] newFontDataArray = PreferenceConverter.getFontDataArray(this, key);
        final FontData[] currentFontDataArray = fontRegistry.getFontData(key);
        if (!newFontDataArray.equals(currentFontDataArray)) {
            fontRegistry.put(key, newFontDataArray);
        }
        return fontRegistry.get(key);
    }
}
