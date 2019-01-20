/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws.datatypes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Class for XPath utils.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class XPathUtils {
    private static NamespaceContext ihcNamespaceContext = new NamespaceContext() {
        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException("Prefix argument can't be null");
            } else if ("SOAP-ENV".equals(prefix)) {
                return "http://schemas.xmlsoap.org/soap/envelope/";
            } else if ("ns1".equals(prefix)) {
                return "utcs";
            }
            return "utcs.values";
        }

        @Override
        public String getPrefix(String uri) {
            return null;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Iterator getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }
    };

    public static String parseXMLValue(String xml, String xpathExpression)
            throws IOException, XPathExpressionException {
        try (InputStream is = new ByteArrayInputStream(xml.getBytes("UTF8"))) {
            XPath xpath = XPathFactory.newInstance().newXPath();
            InputSource inputSource = new InputSource(is);

            xpath.setNamespaceContext(ihcNamespaceContext);
            return (String) xpath.evaluate(xpathExpression, inputSource, XPathConstants.STRING);
        }
    }

    public static boolean parseValueToBoolean(String xml, String xpathExpression)
            throws IOException, XPathExpressionException {
        return Boolean.parseBoolean(parseXMLValue(xml, xpathExpression));
    }

    public static String createIgnoreNameSpaceSyntaxExpr(String name) {
        return "*[local-name() = '" + name + "']";
    }

    public static String getSpeficValueFromNode(Node n, String xpathExpr) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(ihcNamespaceContext);
        XPathExpression pathExpr = xpath.compile(xpathExpr);
        return (String) pathExpr.evaluate(n, XPathConstants.STRING);
    }

    public static NodeList parseList(String xml, String xpathExpression) throws XPathExpressionException, IOException {
        try (InputStream is = new ByteArrayInputStream(xml.getBytes("UTF8"))) {
            XPath xpath = XPathFactory.newInstance().newXPath();
            InputSource inputSource = new InputSource(is);
            xpath.setNamespaceContext(ihcNamespaceContext);
            return (NodeList) xpath.evaluate(xpathExpression, inputSource, XPathConstants.NODESET);
        }
    }

    public static String getValueFromNode(Node n, String value) throws XPathExpressionException {
        return getSpeficValueFromNode(n, XPathUtils.createIgnoreNameSpaceSyntaxExpr(value));
    }
}
