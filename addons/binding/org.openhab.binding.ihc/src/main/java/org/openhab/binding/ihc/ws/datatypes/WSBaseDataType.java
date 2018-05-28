/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws.datatypes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openhab.binding.ihc.ws.exeptions.IhcExecption;
import org.xml.sax.InputSource;

/**
 * Base data class for all controllers data values.
 *
 * @author Pauli Anttila - Initial contribution
 */
public abstract class WSBaseDataType {

    static public String parseXMLValue(String xml, String xpathExpression) throws IhcExecption {
        InputStream is;
        try {
            is = new ByteArrayInputStream(xml.getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
            throw new IhcExecption(e);
        }

        XPath xpath = XPathFactory.newInstance().newXPath();
        InputSource inputSource = new InputSource(is);

        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix == null) {
                    throw new IllegalArgumentException("Prefix argument can't be null");
                } else if ("SOAP-ENV".equals(prefix)) {
                    return "http://schemas.xmlsoap.org/soap/envelope/";
                } else if ("ns1".equals(prefix)) {
                    return "utcs";
                } else if ("ns2".equals(prefix)) {
                    return "utcs.values";
                }
                return null;
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
        });

        try {
            return (String) xpath.evaluate(xpathExpression, inputSource, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new IhcExecption(e);
        }
    }

    static public boolean parseValueToBoolean(String xml, String xpathExpression) throws IhcExecption {
        return Boolean.parseBoolean(parseXMLValue(xml, xpathExpression));
    }
}
