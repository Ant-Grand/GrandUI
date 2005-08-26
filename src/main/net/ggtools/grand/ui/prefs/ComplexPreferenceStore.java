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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import org.xml.sax.InputSource;
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
        void addEntry(final String key, final String value);

        void addProperties(final String key, final Element propertiesElement);
    }

    /**
     * An interface used to save properties like structure. It is used to save
     * either a {@link PreferenceStore} or a {@link Properties}.
     * 
     * @author Christophe Labouisse
     */
    private interface PropertySaver {

        String get(final String key);

        Collection getKeys();

        boolean needSaving(final String key);
    }

    private static final int COLLECTION_NO_LIMIT = -1;

    private static final String DATE_ATTRIBUTE = "date";

    private static final String ENTRY_ELEMENT = "entry";

    private static final String KEY_ATTRIBUTE = "key";

    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(ComplexPreferenceStore.class);

    private static final int PREF_FILE_VERSION_MAJOR = 1;

    private static final int PREF_FILE_VERSION_MINOR = 0;

    private static final String PROPERTIES_ELEMENT = "properties";

    private static final String ROOT_ELEMENT = "preferences";

    private static final String VERSION_ATTRIBUTE = "version";

    /**
     * Escapes the "," character in a script to be able to use the "," as
     * separator in lists.
     * 
     * @param item
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

    private final ColorRegistry colorRegistry = new ColorRegistry();

    private final FontRegistry fontRegistry = new FontRegistry();

    private File prefFile;

    private final Map propertiesTable = new HashMap();

    /**
     * Get a collection of {@link String}s.
     * 
     * @param key
     * @return
     */
    public Collection getCollection(final String key) {
        return getCollection(key, COLLECTION_NO_LIMIT);
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
        if (limit == COLLECTION_NO_LIMIT) limit = tokenizer.countTokens();
        for (int i = 0; (i < limit) && tokenizer.hasMoreTokens(); i++) {
            list.addLast(unEscapeString(tokenizer.nextToken()));
        }
        return list;
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

    /**
     * Retrieve a Properties object stored by
     * {@link #setValue(String, Properties)}.
     * 
     * @param key
     * @return
     */
    public Properties getProperties(final String key) {
        Properties properties = null;
        if (propertiesTable.containsKey(key)) {
            properties = new Properties();
            properties.putAll((Properties) propertiesTable.get(key));
        }
        return properties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferenceStore#load()
     */
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
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource inputSource = new InputSource(is);
                doc = db.parse(is);
            } catch (ParserConfigurationException e) {
                log.error("Got exception while parsing preference file", e);
                throw new Error(e);
            } catch (SAXException e) {
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

                public void addEntry(String key, String value) {
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
            if (is != null) is.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPersistentPreferenceStore#save()
     */
    public void save() throws IOException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(prefFile);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                log.error("Cannot create document builder", e);
                throw new Error("Cannot create document builder", e);
            }

            final Document doc = db.newDocument();
            final Element rootElement = (Element) doc.appendChild(doc.createElement(ROOT_ELEMENT));
            rootElement.setAttribute(VERSION_ATTRIBUTE, Integer.toString(PREF_FILE_VERSION_MAJOR)
                    + "." + PREF_FILE_VERSION_MINOR);
            rootElement.setAttribute(DATE_ATTRIBUTE, new Date().toString());

            final PropertySaver prefStoreSaver = new PropertySaver() {

                public String get(String key) {
                    return getString(key);
                }

                public Collection getKeys() {
                    return Arrays.asList(preferenceNames());
                }

                public boolean needSaving(String key) {
                    return !isDefault(key);
                }
            };

            saveProperties(doc, rootElement, prefStoreSaver);

            for (Iterator iter = propertiesTable.entrySet().iterator(); iter.hasNext();) {
                final Map.Entry entry = (Map.Entry) iter.next();
                final String propKey = (String) entry.getKey();
                final Properties props = (Properties) entry.getValue();
                final Element currentElement = (Element) rootElement.appendChild(doc
                        .createElement(PROPERTIES_ELEMENT));
                currentElement.setAttribute(KEY_ATTRIBUTE, propKey);
                final PropertySaver propertySaver = new PropertySaver() {

                    public String get(String key) {
                        return props.getProperty(key);
                    }

                    public Collection getKeys() {
                        return props.keySet();
                    }

                    public boolean needSaving(String key) {
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
            } catch (TransformerConfigurationException e) {
                log.error("Cannot configure Tranformer to save preferences", e);
                throw new RuntimeException("Cannot configure Tranformer to save preferences", e);
            }
            final DOMSource doms = new DOMSource(doc);
            final StreamResult sr = new StreamResult(os);
            try {
                t.transform(doms, sr);
            } catch (TransformerException e) {
                log.error("Cannot save preferences", e);
                IOException ioe = new IOException("Cannot save preferences");
                ioe.initCause(e);
                throw ioe;
            }
        } finally {
            if (os != null) os.close();
        }
    }

    public final void setPrefFile(File prefFile) {
        this.prefFile = prefFile;
    }

    /**
     * Sets a properties to the default value. It removes the key from the
     * properties table and mark it for removal on next save.
     * 
     * @param key
     */
    public void setPropertiesToDefault(final String key) {
        // TODO fire listeners
        if (propertiesTable.containsKey(key)) {
            propertiesTable.remove(key);
        }
    }

    /**
     * Save a string collection into the preference store. The collection is
     * stored by escaping each element and appending them separated by commas.
     * 
     * @param key
     * @param value
     */
    public void setValue(final String key, final Collection value) {
        final StringBuffer buffer = new StringBuffer();
        for (final Iterator iter = value.iterator(); iter.hasNext();) {
            final String item = (String) iter.next();
            buffer.append(escapeString(item));
            if (iter.hasNext()) buffer.append(",");
        }
        setValue(key, buffer.toString());
    }

    /**
     * Save a {@link Properties} into the preference store.
     * 
     * @param key
     * @param props
     */
    public void setValue(final String key, final Properties props) {
        final Properties myProperties = new Properties();
        myProperties.putAll(props);
        propertiesTable.remove(key);
        propertiesTable.put(key, myProperties);
        // TODO fire listeners
    }

    /**
     * @param propElement
     */
    private void loadProperties(final Element propElement, final PropertyLoader loader) {
        NodeList entries = propElement.getChildNodes();
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
     * @param doc
     * @param properties
     * @param saver
     */
    private void saveProperties(Document doc, Element properties, final PropertySaver saver) {
        final Collection keys = saver.getKeys();
        final Iterator i = keys.iterator();
        while (i.hasNext()) {
            final String key = (String) i.next();
            if (saver.needSaving(key)) {
                Element entry = (Element) properties.appendChild(doc.createElement(ENTRY_ELEMENT));
                entry.setAttribute(KEY_ATTRIBUTE, key);
                entry.appendChild(doc.createTextNode(saver.get(key)));
            }
        }
    }

}
