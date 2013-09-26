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
package net.ggtools.grand.ui.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A {@link org.eclipse.jface.preference.PreferenceStore} featuring higher level
 * functionalities like save properties or list.
 *
 * @author Christophe Labouisse
 */
/**
 * @author Christophe Labouisse
 */
public class ComplexPreferenceStore extends PreferenceStore {
    /**
     * An interface used to load properties like structure. It is used to save
     * either a {@link PreferenceStore} or a {@link Properties}.
     *
     * @author Christophe Labouisse
     */
    private interface PropertyLoader {
        /**
         * Method addEntry.
         * @param key String
         * @param value String
         */
        void addEntry(final String key, final String value);

        /**
         * Method addProperties.
         * @param key String
         * @param propertiesElement Element
         */
        void addProperties(final String key, final Element propertiesElement);
    }

    /**
     * An interface used to save properties like structure. It is used to save
     * either a {@link PreferenceStore} or a {@link Properties}.
     *
     * @author Christophe Labouisse
     */
    private interface PropertySaver {

        /**
         * Method get.
         * @param key String
         * @return String
         */
        String get(final String key);

        /**
         * Method getKeys.
         * @return Collection<?>
         */
        Collection<?> getKeys();

        /**
         * Method needSaving.
         * @param key String
         * @return boolean
         */
        boolean needSaving(final String key);
    }

    /**
     * Field COLLECTION_NO_LIMIT.
     * (value is -1)
     */
    private static final int COLLECTION_NO_LIMIT = -1;

    /**
     * Field DATE_ATTRIBUTE.
     * (value is ""date"")
     */
    private static final String DATE_ATTRIBUTE = "date";

    /**
     * Field ENTRY_ELEMENT.
     * (value is ""entry"")
     */
    private static final String ENTRY_ELEMENT = "entry";

    /**
     * Field KEY_ATTRIBUTE.
     * (value is ""key"")
     */
    private static final String KEY_ATTRIBUTE = "key";

    /**
     * Logger for this class.
     */
    private static final Log log = LogFactory.getLog(ComplexPreferenceStore.class);

    /**
     * Field PREF_FILE_VERSION_MAJOR.
     * (value is 1)
     */
    private static final int PREF_FILE_VERSION_MAJOR = 1;

    /**
     * Field PREF_FILE_VERSION_MINOR.
     * (value is 0)
     */
    private static final int PREF_FILE_VERSION_MINOR = 0;

    /**
     * Field PROPERTIES_ELEMENT.
     * (value is ""properties"")
     */
    private static final String PROPERTIES_ELEMENT = "properties";

    /**
     * Field ROOT_ELEMENT.
     * (value is ""preferences"")
     */
    private static final String ROOT_ELEMENT = "preferences";

    /**
     * Field VERSION_ATTRIBUTE.
     * (value is ""version"")
     */
    private static final String VERSION_ATTRIBUTE = "version";

    /**
     * Escapes the "," character in a script to be able to use the "," as
     * separator in lists.
     *
     * @param item String
     * @return String
     */
    private static String escapeString(final String item) {
        return item.replaceAll("%", "%%").replaceAll(",", "%,");
    }

    /**
     * @param item String
     * @return String
     */
    private static String unEscapeString(final String item) {
        return item.replaceAll("%,", ",").replaceAll("%%", "%");
    }

    /**
     * Field colorRegistry.
     */
    private final ColorRegistry colorRegistry = new ColorRegistry();

    /**
     * Field fontRegistry.
     */
    private final FontRegistry fontRegistry = new FontRegistry();

    /**
     * Field prefFile.
     */
    private File prefFile;

    /**
     * Field propertiesTable.
     */
    private final Map<String, Properties> propertiesTable =
            new HashMap<String, Properties>();

    /**
     * Get a collection of {@link String}s.
     *
     * @param key String
     * @return Collection<String>
     */
    public final Collection<String> getCollection(final String key) {
        return getCollection(key, COLLECTION_NO_LIMIT);
    }

    /**
     * Get a collection of {@link String}s. The number of elements returned by
     * this method can be tuned by the limit parameter. If the limit is less
     * than the number of stored elements, only the <i>limit</i> first ones
     * will be fetched.
     *
     * @param key String
     * @param limit
     *            maximum size of the returned list or -1 for unlimited.
     * @return Collection<String>
     */
    public final Collection<String> getCollection(final String key,
            final int limit) {
        final LinkedList<String> list = new LinkedList<String>();
        final StringTokenizer tokenizer = new StringTokenizer(getString(key), ",");
        int lim = (limit == COLLECTION_NO_LIMIT) ? tokenizer.countTokens() : limit;
        for (int i = 0; (i < lim) && tokenizer.hasMoreTokens(); i++) {
            list.addLast(unEscapeString(tokenizer.nextToken()));
        }
        return list;
    }

    /**
     * Method getColor.
     * @param key String
     * @return Color
     */
    public final Color getColor(final String key) {
        final RGB newRGBColor = PreferenceConverter.getColor(this, key);
        final RGB currentRGBColor = colorRegistry.getRGB(key);
        if (!newRGBColor.equals(currentRGBColor)) {
            colorRegistry.put(key, newRGBColor);
        }
        return colorRegistry.get(key);
    }

    /**
     * Method getFont.
     * @param key String
     * @return Font
     */
    public final Font getFont(final String key) {
        final FontData[] newFontDataArray = PreferenceConverter.getFontDataArray(this, key);
        final FontData[] currentFontDataArray = fontRegistry.getFontData(key);
        if (!newFontDataArray.equals(currentFontDataArray)) {
            fontRegistry.put(key, newFontDataArray);
        }
        return fontRegistry.get(key);
    }

    /**
     * Retrieve a Properties object stored by
     * {@link #setValue(String, Properties)}.
     *
     * @param key String
     * @return Properties
     */
    public final Properties getProperties(final String key) {
        Properties properties = null;
        if (propertiesTable.containsKey(key)) {
            properties = new Properties();
            properties.putAll(propertiesTable.get(key));
        }
        return properties;
    }

    /**
     * Method load.
     * @throws IOException
     * @see org.eclipse.jface.preference.PreferenceStore#load()
     */
    @Override
    public void load() throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(prefFile);
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setValidating(false);
            dbf.setCoalescing(true);
            dbf.setIgnoringComments(true);
            Document doc = null;
            try {
                final DocumentBuilder db = dbf.newDocumentBuilder();
//                final InputSource inputSource = new InputSource(is);
                doc = db.parse(is);
            } catch (final ParserConfigurationException e) {
                log.error("Got exception while parsing preference file", e);
                throw new Error(e);
            } catch (final SAXException e) {
                log.error("Got exception while parsing preference file", e);
                throw new Error(e);
            }

            final Element rootElement = doc.getDocumentElement();
            if (rootElement.hasAttribute(VERSION_ATTRIBUTE)) {
                final String version = rootElement.getAttribute(VERSION_ATTRIBUTE);
                final String[] versionParts = version.split("\\.", 2);
                if (PREF_FILE_VERSION_MAJOR != Integer.parseInt(versionParts[0])) {
                    final String message = "Cannot load preferences file version " + version
                            + " current version is " + PREF_FILE_VERSION_MAJOR + "."
                            + PREF_FILE_VERSION_MINOR;
                    log.error(message);
                    throw new Error(message);
                }
                if (log.isInfoEnabled()) {
                    log.info("Loading from preference file version " + version);
                }
            }
            else {
                log.warn("Root element does not have a version, trying to log anyway");
            }

            final PropertyLoader loader = new PropertyLoader() {

                public void addEntry(final String key, final String value) {
                    putValue(key, value);
                }

                public void addProperties(final String key, final Element propertiesElement) {
                    final Properties properties = new Properties();
                    final PropertyLoader propertiesLoader = new PropertyLoader() {

                        public void addEntry(final String k, final String v) {
                            properties.setProperty(k, v);
                        }

                        public void addProperties(final String k, final Element element) {
                            log.warn("Nested properties not supported, ignoring key " + k);
                        }
                    };

                    loadProperties(propertiesElement, propertiesLoader);
                    propertiesTable.put(key, properties);
                }
            };

            loadProperties(rootElement, loader);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Method save.
     * @throws IOException
     * @see org.eclipse.jface.preference.IPersistentPreferenceStore#save()
     */
    @Override
    public void save() throws IOException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(prefFile);
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (final ParserConfigurationException e) {
                log.error("Cannot create document builder", e);
                throw new Error("Cannot create document builder", e);
            }

            final Document doc = db.newDocument();
            final Element rootElement = (Element) doc.appendChild(doc.createElement(ROOT_ELEMENT));
            rootElement.setAttribute(VERSION_ATTRIBUTE, Integer.toString(PREF_FILE_VERSION_MAJOR)
                    + "." + PREF_FILE_VERSION_MINOR);
            rootElement.setAttribute(DATE_ATTRIBUTE, new Date().toString());

            final PropertySaver prefStoreSaver = new PropertySaver() {

                public String get(final String key) {
                    return getString(key);
                }

                public Collection<String> getKeys() {
                    return Arrays.asList(preferenceNames());
                }

                public boolean needSaving(final String key) {
                    return !isDefault(key);
                }
            };

            saveProperties(doc, rootElement, prefStoreSaver);

            for (final Iterator<Map.Entry<String, Properties>> iter = propertiesTable.entrySet().iterator(); iter.hasNext();) {
                final Map.Entry<String, Properties> entry = iter.next();
                final String propKey = entry.getKey();
                final Properties props = entry.getValue();
                final Element currentElement = (Element) rootElement.appendChild(doc
                        .createElement(PROPERTIES_ELEMENT));
                currentElement.setAttribute(KEY_ATTRIBUTE, propKey);
                final PropertySaver propertySaver = new PropertySaver() {

                    public String get(final String key) {
                        return props.getProperty(key);
                    }

                    public Collection<?> getKeys() {
                        return props.keySet();
                    }

                    public boolean needSaving(final String key) {
                        return true;
                    }
                };
                saveProperties(doc, currentElement, propertySaver);
            }

            final TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = null;
            try {
                t = tf.newTransformer();
                t.setOutputProperty(OutputKeys.INDENT, "yes");
                t.setOutputProperty(OutputKeys.METHOD, "xml");
                t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            } catch (final TransformerConfigurationException e) {
                log.error("Cannot configure Transformer to save preferences", e);
                throw new RuntimeException("Cannot configure Transformer to save preferences", e);
            }
            final DOMSource doms = new DOMSource(doc);
            final StreamResult sr = new StreamResult(os);
            try {
                t.transform(doms, sr);
            } catch (final TransformerException e) {
                log.error("Cannot save preferences", e);
                final IOException ioe = new IOException("Cannot save preferences");
                ioe.initCause(e);
                throw ioe;
            }
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    /**
     * Method setPrefFile.
     * @param prefFile File
     */
    public final void setPrefFile(final File prefFile) {
        this.prefFile = prefFile;
    }

    /**
     * Sets a properties to the default value. It removes the key from the
     * properties table and mark it for removal on next save.
     *
     * @param key String
     */
    public final void setPropertiesToDefault(final String key) {
        // TODO fire listeners
        if (propertiesTable.containsKey(key)) {
            propertiesTable.remove(key);
        }
    }

    /**
     * Save a string collection into the preference store. The collection is
     * stored by escaping each element and appending them separated by commas.
     *
     * @param key String
     * @param value Collection<String>
     */
    public final void setValue(final String key, final Collection<String> value) {
        final StringBuffer buffer = new StringBuffer();
        for (final Iterator<String> iter = value.iterator(); iter.hasNext();) {
            final String item = iter.next();
            buffer.append(escapeString(item));
            if (iter.hasNext()) {
                buffer.append(",");
            }
        }
        setValue(key, buffer.toString());
    }

    /**
     * Save a {@link Properties} into the preference store.
     *
     * @param key String
     * @param props Properties
     */
    public final void setValue(final String key, final Properties props) {
        final Properties myProperties = new Properties();
        myProperties.putAll(props);
        propertiesTable.remove(key);
        propertiesTable.put(key, myProperties);
        // TODO fire listeners
    }

    /**
     * @param propElement Element
     * @param loader PropertyLoader
     */
    private void loadProperties(final Element propElement, final PropertyLoader loader) {
        final NodeList entries = propElement.getChildNodes();
        for (int i = 0; i < entries.getLength(); i++) {
            final Node item = entries.item(i);
            if (ENTRY_ELEMENT.equals(item.getNodeName())) {
                final Element entryElement = (Element) item;
                if (entryElement.hasAttribute(KEY_ATTRIBUTE)) {
                    final Node n = entryElement.getFirstChild();
                    final String val = (n == null) ? "" : n.getNodeValue();
                    loader.addEntry(entryElement.getAttribute(KEY_ATTRIBUTE), val);
                }
            }
            else if (PROPERTIES_ELEMENT.equals(item.getNodeName())) {
                final Element entryElement = (Element) item;
                if (entryElement.hasAttribute(KEY_ATTRIBUTE)) {
                    loader.addProperties(entryElement.getAttribute(KEY_ATTRIBUTE), entryElement);
                }
            }
        }
    }

    /**
     * @param doc Document
     * @param properties Element
     * @param saver PropertySaver
     */
    private void saveProperties(final Document doc, final Element properties, final PropertySaver saver) {
        final Collection<?> keys = saver.getKeys();
        // TODO Solve the properties problem.
        for (Object i : keys) {
            final String key = i instanceof String ? (String) i : i.toString();
            if (saver.needSaving(key)) {
                final Element entry = (Element) properties.appendChild(doc.createElement(ENTRY_ELEMENT));
                entry.setAttribute(KEY_ATTRIBUTE, key);
                entry.appendChild(doc.createTextNode(saver.get(key)));
            }
        }
    }

}
