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
public class ComplexPreferenceStore extends PreferenceStore {

    /**
     * An interface used to save properties like structure. It is used to save
     * either a PreferenceStore or a Properties.
     * 
     * @author Christophe Labouisse
     */
    private interface PropertySaver {

        String get(final String key);

        Collection getKeys();

        boolean needSaving(final String key);
    }

    private static final int COLLECTION_NO_LIMIT = -1;

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
                throw new Error(e);
            } catch (SAXException e) {
                throw new Error(e);
            }

            final Element rootElement = doc.getDocumentElement();
            // TODO check version

            NodeList entries = rootElement.getChildNodes();
            for (int i = 0; i < entries.getLength(); i++) {
                final Node item = entries.item(i);
                if ("entry".equals(item.getNodeName())) {
                    final Element entryElement = (Element) item;
                    if (entryElement.hasAttribute("key")) {
                        final Node n = entryElement.getFirstChild();
                        final String val = (n == null) ? "" : n.getNodeValue();
                        putValue(entryElement.getAttribute("key"), val);
                    }
                }
            }

            // TODO load properties
        } finally {
            if (is != null) is.close();
        }
    }

    public void save() throws IOException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(prefFile);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException pce) {
                assert (false);
            }

            final Document doc = db.newDocument();
            final Element rootElement = (Element) doc.appendChild(doc.createElement("preferences"));
            rootElement.setAttribute("version", "1.0");
            rootElement.setAttribute("date", new Date().toString());

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
                Element currentElement = (Element) rootElement.appendChild(doc
                        .createElement("properties"));
                currentElement.setAttribute("key", propKey);
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
            } catch (TransformerConfigurationException tce) {
                throw new RuntimeException("Cannot configure Tranformer to save preferences", tce);
            }
            final DOMSource doms = new DOMSource(doc);
            final StreamResult sr = new StreamResult(os);
            try {
                t.transform(doms, sr);
            } catch (TransformerException te) {
                IOException ioe = new IOException("Cannot save preferences");
                ioe.initCause(te);
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
                Element entry = (Element) properties.appendChild(doc.createElement("entry"));
                entry.setAttribute("key", key);
                entry.appendChild(doc.createTextNode(saver.get(key)));
            }
        }
    }

}
