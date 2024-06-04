/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.communicator.message;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Decodes a XML-RPC message from the Homematic server.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class XmlRpcResponse implements RpcResponse {
    private String methodName;
    private Object[] responseData;

    /**
     * Decodes a XML-RPC message from the given InputStream.
     */
    public XmlRpcResponse(InputStream is, Charset encoding)
            throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        saxParser.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        InputSource inputSource = new InputSource(is);
        inputSource.setEncoding(encoding.name());
        saxParser.parse(inputSource, new XmlRpcHandler());
    }

    @Override
    public Object[] getResponseData() {
        return responseData;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        return RpcUtils.dumpRpcMessage(methodName, responseData);
    }

    /**
     * SAX parser implementation to decode XML-RPC.
     *
     * @author Gerhard Riegler
     */
    private class XmlRpcHandler extends DefaultHandler {
        private List<Object> result = new ArrayList<>();
        private LinkedList<List<Object>> currentDataObject = new LinkedList<>();
        private StringBuilder tagValue;
        private boolean isValueTag;

        @Override
        public void startDocument() throws SAXException {
            currentDataObject.addLast(new ArrayList<>());
        }

        @Override
        public void endDocument() throws SAXException {
            result.addAll(currentDataObject.removeLast());
            responseData = result.toArray();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            String tag = qName.toLowerCase();
            if ("array".equals(tag) || "struct".equals(tag)) {
                currentDataObject.addLast(new ArrayList<>());
            }
            isValueTag = "value".equals(tag);
            tagValue = new StringBuilder();
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String currentTag = qName.toLowerCase();
            String currentValue = tagValue.toString();
            List<Object> data = currentDataObject.peekLast();

            switch (currentTag) {
                case "boolean":
                    data.add("1".equals(currentValue) ? Boolean.TRUE : Boolean.FALSE);
                    break;
                case "int":
                case "i4":
                    data.add(Integer.valueOf(currentValue));
                    break;
                case "double":
                    data.add(Double.valueOf(currentValue));
                    break;
                case "string":
                case "name":
                    data.add(currentValue);
                    break;
                case "value":
                    if (isValueTag) {
                        data.add(currentValue);
                        isValueTag = false;
                    }
                    break;
                case "array":
                    List<Object> arrayData = currentDataObject.removeLast();
                    currentDataObject.peekLast().add(arrayData.toArray());
                    break;
                case "struct":
                    List<Object> mapData = currentDataObject.removeLast();
                    Map<Object, Object> resultMap = new HashMap<>();

                    for (int i = 0; i < mapData.size(); i += 2) {
                        resultMap.put(mapData.get(i), mapData.get(i + 1));
                    }
                    currentDataObject.peekLast().add(resultMap);
                    break;
                case "base64":
                    data.add(Base64.getDecoder().decode(currentValue));
                    break;
                case "datetime.iso8601":
                    try {
                        data.add(XmlRpcRequest.XML_RPC_DATEFORMAT.parse(currentValue));
                    } catch (ParseException ex) {
                        throw new SAXException(ex.getMessage(), ex);
                    }
                    break;
                case "methodname":
                    methodName = currentValue;
                    break;
                case "params":
                case "param":
                case "methodcall":
                case "methodresponse":
                case "member":
                case "data":
                case "fault":
                    break;
                default:
                    throw new SAXException("Unknown XML-RPC tag: " + currentTag);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            tagValue.append(new String(ch, start, length));
        }
    }
}
