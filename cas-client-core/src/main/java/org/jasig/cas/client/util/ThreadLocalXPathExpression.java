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

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.*;

/**
 * Thread local XPath expression.
 *
 * @author Marvin S. Addison
 * @since 3.4
 */
public class ThreadLocalXPathExpression extends ThreadLocal<XPathExpression> implements XPathExpression {

    /** XPath expression */
    private final String expression;

    /** Namespace context. */
    private final NamespaceContext context;

    /**
     * Creates a new instance from an XPath expression and namespace context.
     *
     * @param xPath XPath expression.
     * @param context Namespace context for handling namespace prefix to URI mappings.
     */
    public ThreadLocalXPathExpression(final String xPath, final NamespaceContext context) {
        this.expression = xPath;
        this.context = context;
    }

    public Object evaluate(final Object o, final QName qName) throws XPathExpressionException {
        return get().evaluate(o, qName);
    }

    public String evaluate(final Object o) throws XPathExpressionException {
        return get().evaluate(o);
    }

    public Object evaluate(final InputSource inputSource, final QName qName) throws XPathExpressionException {
        return get().evaluate(inputSource, qName);
    }

    public String evaluate(final InputSource inputSource) throws XPathExpressionException {
        return get().evaluate(inputSource);
    }

    /**
     * Evaluates the XPath expression and returns the result coerced to a string.
     *
     * @param o Object on which to evaluate the expression; typically a DOM node.
     *
     * @return Evaluation result as a string.
     *
     * @throws XPathExpressionException On XPath evaluation errors.
     */
    public String evaluateAsString(final Object o) throws XPathExpressionException {
        return (String) evaluate(o, XPathConstants.STRING);
    }

    /**
     * Evaluates the XPath expression and returns the result coerced to a node list.
     *
     * @param o Object on which to evaluate the expression; typically a DOM node.
     *
     * @return Evaluation result as a node list.
     *
     * @throws XPathExpressionException On XPath evaluation errors.
     */
    public NodeList evaluateAsNodeList(final Object o) throws XPathExpressionException {
        return (NodeList) evaluate(o, XPathConstants.NODESET);
    }

    @Override
    protected XPathExpression initialValue() {
        try {
            final XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(context);
            return xPath.compile(expression);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Invalid XPath expression");
        }
    }
}
