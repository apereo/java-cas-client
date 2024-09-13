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
package org.apereo.cas.client.validation;

import org.apereo.cas.client.util.XmlUtils;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Service tickets validation service for the CAS protocol v3.
 *
 * @author Jerome Leleu
 * @since 3.4.0
 */
public class Cas30ServiceTicketValidator extends Cas20ServiceTicketValidator {

    public Cas30ServiceTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    @Override
    protected String getUrlSuffix() {
        return "p3/serviceValidate";
    }

    /**
     * Custom attribute extractor that will account for inlined CAS attributes.  Useful when CAS is acting
     * as SAML 2 IdP and returns SAML attributes with names that contains namespaces.
     *
     * @param xml the XML to parse.
     * @return - Map of attributes
     */
    @Override
    protected Map<String, Object> extractCustomAttributes(final String xml) {
        final var document = XmlUtils.newDocument(xml);

        // Check if attributes are inlined.  If not return default super method results
        final var attributeList = document.getElementsByTagName("cas:attribute");
        if (attributeList.getLength() == 0) {
            return super.extractCustomAttributes(xml);
        }

        final Map<String, Object> attributes = new HashMap<>();

        for (var i = 0; i < attributeList.getLength(); i++) {
            final var casAttributeNode = attributeList.item(i);
            final var nodeAttributes = casAttributeNode.getAttributes();
            final var name = nodeAttributes.getNamedItem("name").getNodeValue();
            final var value = nodeAttributes.getNamedItem("value").getTextContent();
            final var mapValue = attributes.get(name);
            if (mapValue != null) {
                if (mapValue instanceof List) {
                    ((List) mapValue).add(value);
                } else {
                    final Deque<Object> list = new LinkedList<>();
                    list.add(mapValue);
                    list.add(value);
                    attributes.put(name, list);
                }
            } else {
                attributes.put(name, value);
            }
        }
        return attributes;
    }

}
