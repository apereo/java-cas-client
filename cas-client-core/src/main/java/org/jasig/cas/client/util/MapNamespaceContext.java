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
 * @since 3.3.1
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
