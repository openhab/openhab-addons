/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.ecovacs.internal.api.util;

import java.io.StringReader;
import java.util.Optional;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class XPathUtils {
    private static @Nullable XPathFactory factory;

    public static Node getFirstXPathMatch(String xml, String xpathExpression) throws DataParsingException {
        NodeList nodes = getXPathMatches(xml, xpathExpression);
        if (nodes.getLength() == 0) {
            throw new DataParsingException("No nodes matching expression " + xpathExpression + " in XML " + xml);
        }
        return nodes.item(0);
    }

    public static Optional<Node> getFirstXPathMatchOpt(String xml, String xpathExpression) throws DataParsingException {
        NodeList nodes = getXPathMatches(xml, xpathExpression);
        return nodes.getLength() == 0 ? Optional.empty() : Optional.of(nodes.item(0));
    }

    public static NodeList getXPathMatches(String xml, String xpathExpression) throws DataParsingException {
        try {
            InputSource source = new InputSource(new StringReader(xml));
            return (NodeList) newXPath().evaluate(xpathExpression, source, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new DataParsingException(e);
        }
    }

    @SuppressWarnings("null") // null annotations don't recognize FACTORY can not be null in return statement
    private static XPath newXPath() {
        synchronized (XPathUtils.class) {
            if (factory == null) {
                factory = XPathFactory.newInstance();
            }
            return factory.newXPath();
        }
    }
}
