/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.SimpleBeanInfo;

import org.springframework.beans.propertyeditors.CustomBooleanEditor;

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
            final PropertyDescriptor descriptor = new PropertyDescriptor("allowedProxyChains", Cas20ProxyTicketValidator.class, null, "setAllowedProxyChains") {
                public PropertyEditor createPropertyEditor(final Object bean) {
                    return new ProxyListPropertyEditor();
                }
            };
            
            final PropertyDescriptor acceptAnyProxy = new PropertyDescriptor("acceptAnyProxy", Cas20ProxyTicketValidator.class, null, "setAcceptAnyProxy") {
                public PropertyEditor createPropertyEditor(final Object bean) {
                    return new CustomBooleanEditor(true);
                }
            };
            
            return new PropertyDescriptor[] {descriptor, acceptAnyProxy};
        } catch (final IntrospectionException e) {
              throw new RuntimeException(e);
        }
    }
}
