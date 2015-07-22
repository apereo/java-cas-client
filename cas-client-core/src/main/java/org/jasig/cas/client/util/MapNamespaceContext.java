/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.util;

import javax.xml.namespace.NamespaceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Namespace context implementation backed by a map of XML prefixes to namespace URIs.
 *
 * @author Marvin S. Addison
 * @since 3.4
 */
public class MapNamespaceContext implements NamespaceContext {

    private final Map<String, String> namespaceMap;

    /**
     * Creates a new instance from an array of namespace delcarations.
     *
     * @param namespaceDeclarations An array of namespace declarations of the form prefix->uri.
     */
    public MapNamespaceContext(final String ... namespaceDeclarations) {
        namespaceMap = new HashMap<String, String>();
        int index;
        String key;
        String value;
        for (final String decl : namespaceDeclarations) {
            index = decl.indexOf('-');
            key = decl.substring(0, index);
            value = decl.substring(index + 2);
            namespaceMap.put(key, value);
        }
    }

    /**
     * Creates a new instance from a map.
     *
     * @param namespaceMap Map of XML namespace prefixes (keys) to URIs (values).
     */
    public MapNamespaceContext(final Map<String, String> namespaceMap) {
        this.namespaceMap = namespaceMap;
    }

    public String getNamespaceURI(final String prefix) {
        return namespaceMap.get(prefix);
    }

    public String getPrefix(final String namespaceURI) {
        for (final Map.Entry<String, String> entry : namespaceMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(namespaceURI)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Iterator getPrefixes(final String namespaceURI) {
        return Collections.singleton(getPrefix(namespaceURI)).iterator();
    }
}
