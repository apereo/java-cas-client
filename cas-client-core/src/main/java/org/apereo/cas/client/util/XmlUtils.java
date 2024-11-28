/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.cas.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common utilities for easily parsing XML without duplicating logic.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class XmlUtils {

    /**
     * Static instance of Commons Logging.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtils.class);

    private static final SAXParserFactory saxParserFactory;

    static {
        try {
            saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            saxParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create SAXParserFactory", e);
        }
    }

    private static final DocumentBuilderFactory documentBuilderFactory;

    static {
        try {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final Map<String, Boolean> features = new HashMap<>();
            features.put(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            features.put("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            features.put("http://apache.org/xml/features/disallow-doctype-decl", true);
            for (final var entry : features.entrySet()) {
                try {
                    documentBuilderFactory.setFeature(entry.getKey(), entry.getValue());
                } catch (final ParserConfigurationException e) {
                    LOGGER.warn("Failed setting XML feature {}", entry.getKey(), e);
                }
            }
            documentBuilderFactory.setExpandEntityReferences(false);
            documentBuilderFactory.setNamespaceAware(true);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create DocumentBuilderFactory", e);
        }
    }

    /**
     * Creates a new namespace-aware DOM document object by parsing the given XML.
     *
     * @param xml XML content.
     *
     * @return DOM document.
     */
    public static Document newDocument(final String xml) {
        try {
            return documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (final Exception e) {
            throw new RuntimeException("XML parsing error: " + e);
        }
    }

    /**
     * Get an instance of an XML reader from the XMLReaderFactory.
     *
     * @return the XMLReader.
     */
    public static XMLReader getXmlReader() {
        try {
            return saxParserFactory.newSAXParser().getXMLReader();
        } catch (final Exception e) {
            throw new RuntimeException("Unable to create XMLReader", e);
        }
    }

    /**
     * Retrieve the text for a group of elements. Each text element is an entry
     * in a list.
     * <p>This method is currently optimized for the use case of two elements in a list.
     *
     * @param xmlAsString the xml response
     * @param element     the element to look for
     * @return the list of text from the elements.
     */
    public static List<String> getTextForElements(final String xmlAsString, final String element) {
        final List<String> elements = new ArrayList<>(2);
        final var reader = getXmlReader();

        final var handler = new DefaultHandler() {

            private boolean foundElement = false;

            private StringBuilder buffer = new StringBuilder();

            @Override
            public void startElement(final String uri, final String localName, final String qName,
                                     final Attributes attributes) throws SAXException {
                if (localName.equals(element)) {
                    this.foundElement = true;
                }
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) {
                if (localName.equals(element)) {
                    this.foundElement = false;
                    elements.add(this.buffer.toString());
                    this.buffer = new StringBuilder();
                }
            }

            @Override
            public void characters(final char[] ch, final int start, final int length) {
                if (this.foundElement) {
                    this.buffer.append(ch, start, length);
                }
            }
        };

        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);

        try {
            reader.parse(new InputSource(new StringReader(xmlAsString)));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        return elements;
    }

    /**
     * Retrieve the text for a specific element (when we know there is only
     * one).
     *
     * @param xmlAsString the xml response
     * @param element     the element to look for
     * @return the text value of the element.
     */
    public static String getTextForElement(final String xmlAsString, final String element) {
        final var reader = getXmlReader();
        final var builder = new StringBuilder();

        final var handler = new DefaultHandler() {

            private boolean foundElement = false;

            @Override
            public void startElement(final String uri, final String localName, final String qName,
                                     final Attributes attributes) throws SAXException {
                if (localName.equals(element)) {
                    this.foundElement = true;
                }
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) throws SAXException {
                if (localName.equals(element)) {
                    this.foundElement = false;
                }
            }

            @Override
            public void characters(final char[] ch, final int start, final int length) throws SAXException {
                if (this.foundElement) {
                    builder.append(ch, start, length);
                }
            }
        };

        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);

        try {
            reader.parse(new InputSource(new StringReader(xmlAsString)));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        return builder.toString();
    }
}
