package org.jasig.cas.client.validation;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.SimpleBeanInfo;
import java.util.List;

/**
 * BeanInfo support for using this class with Spring.  Configures a ProxyListPropertyEditor to be used with the
 * Cas20ProxyTicketValidator when Spring is used to configure the CAS client.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class Cas20ProxyTicketValidatorBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            final PropertyEditor propertyEditor = new ProxyListPropertyEditor();
            final PropertyDescriptor descriptor = new PropertyDescriptor("allowedProxyChains", List.class) {
                public PropertyEditor createPropertyEditor(final Object bean) {
                    return propertyEditor;
                }
            };
            return new PropertyDescriptor[] {descriptor};
        } catch (final IntrospectionException e) {
              throw new RuntimeException(e);
        }
    }
}
