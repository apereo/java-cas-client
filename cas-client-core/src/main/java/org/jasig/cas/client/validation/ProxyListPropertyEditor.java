package org.jasig.cas.client.validation;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Convert a String-formatted list of acceptable proxies to an array.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public final class ProxyListPropertyEditor extends PropertyEditorSupport {

    /**
     * The new list of proxies to create.  Its a list of String arrays.
     */
    private final List proxyChains = new ArrayList();

    public Object getValue() {
        return this.proxyChains;
    }

    /** Converts the List of Strings into a list of arrays. */
    public void setValue(final Object o) {
        final List values = (List) o;

        for (final Iterator iter = values.iterator(); iter.hasNext();) {
            proxyChains.add(((String) iter.next()).split(" "));
        }
    }
}
