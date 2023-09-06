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
package org.openhab.binding.fsinternetradio.internal.radio;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class hold the result of a request read from the radio. Upon a request the radio returns a XML document like
 * this:
 *
 * <pre>
 * <xmp>
 *   <fsapiResponse> <status>FS_OK</status> <value><u8>1</u8></value> </fsapiResponse>
 * </xmp>
 * </pre>
 *
 * This class parses this XML data and provides functions for reading and casting typical fields.
 *
 * @author Rainer Ostendorf
 * @author Patrick Koenemann
 *
 */
public class FrontierSiliconRadioApiResult {

    /**
     * XML structure holding the parsed response
     */
    final Document xmlDoc;

    private final Logger logger = LoggerFactory.getLogger(FrontierSiliconRadioApiResult.class);

    /**
     * Create result object from XML that was received from the radio.
     *
     * @param requestResultString
     *            The XML string received from the radio.
     * @throws IOException in case the XML returned by the radio is invalid.
     */
    public FrontierSiliconRadioApiResult(String requestResultString) throws IOException {
        Document xml = null;
        try {
            xml = getXmlDocFromString(requestResultString);
        } catch (Exception e) {
            logger.trace("converting to XML failed: '{}' with {}: {}", requestResultString, e.getClass().getName(),
                    e.getMessage());
            logger.debug("converting to XML failed with {}: {}", e.getClass().getName(), e.getMessage());
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
        xmlDoc = xml;
    }

    /**
     * Extract the field "status" from the result and return it
     *
     * @return result field as string.
     */
    private String getStatus() {
        final Element fsApiResult = (Element) xmlDoc.getElementsByTagName("fsapiResponse").item(0);
        final Element statusNode = (Element) fsApiResult.getElementsByTagName("status").item(0);

        final String status = getCharacterDataFromElement(statusNode);
        logger.trace("status is: {}", status);

        return status;
    }

    /**
     * checks if the responses status code was "FS_OK"
     *
     * @return true if status is "FS_OK", false else
     */
    public boolean isStatusOk() {
        return ("FS_OK").equals(getStatus());
    }

    /**
     * read the &lt;value&gt;&lt;u8&gt; field as boolean
     *
     * @return value.u8 field as bool
     */
    public boolean getValueU8AsBoolean() {
        try {
            final Element fsApiResult = (Element) xmlDoc.getElementsByTagName("fsapiResponse").item(0);
            final Element valueNode = (Element) fsApiResult.getElementsByTagName("value").item(0);
            final Element u8Node = (Element) valueNode.getElementsByTagName("u8").item(0);

            final String value = getCharacterDataFromElement(u8Node);
            logger.trace("value is: {}", value);

            return "1".equals(value);
        } catch (Exception e) {
            logger.error("getting Value.U8 failed with {}: {}", e.getClass().getName(), e.getMessage());
            return false;
        }
    }

    /**
     * read the &lt;value&gt;&lt;u8&gt; field as int
     *
     * @return value.u8 field as int
     */
    public int getValueU8AsInt() {
        try {
            final Element fsApiResult = (Element) xmlDoc.getElementsByTagName("fsapiResponse").item(0);
            final Element valueNode = (Element) fsApiResult.getElementsByTagName("value").item(0);
            final Element u8Node = (Element) valueNode.getElementsByTagName("u8").item(0);

            final String value = getCharacterDataFromElement(u8Node);
            logger.trace("value is: {}", value);

            return Integer.parseInt(value);
        } catch (Exception e) {
            logger.error("getting Value.U8 failed with {}: {}", e.getClass().getName(), e.getMessage());
            return 0;
        }
    }

    /**
     * read the &lt;value&gt;&lt;u32&gt; field as int
     *
     * @return value.u32 field as int
     */
    public int getValueU32AsInt() {
        try {
            final Element fsApiResult = (Element) xmlDoc.getElementsByTagName("fsapiResponse").item(0);
            final Element valueNode = (Element) fsApiResult.getElementsByTagName("value").item(0);
            final Element u32Node = (Element) valueNode.getElementsByTagName("u32").item(0);

            final String value = getCharacterDataFromElement(u32Node);
            logger.trace("value is: {}", value);

            return Integer.parseInt(value);
        } catch (Exception e) {
            logger.error("getting Value.U32 failed with {}: {}", e.getClass().getName(), e.getMessage());
            return 0;
        }
    }

    /**
     * read the &lt;value&gt;&lt;c8_array&gt; field as String
     *
     * @return value.c8_array field as String
     */
    public String getValueC8ArrayAsString() {
        try {
            final Element fsApiResult = (Element) xmlDoc.getElementsByTagName("fsapiResponse").item(0);
            final Element valueNode = (Element) fsApiResult.getElementsByTagName("value").item(0);
            final Element c8Array = (Element) valueNode.getElementsByTagName("c8_array").item(0);

            final String value = getCharacterDataFromElement(c8Array);
            logger.trace("value is: {}", value);

            return value;
        } catch (Exception e) {
            logger.error("getting Value.c8array failed with {}: {}", e.getClass().getName(), e.getMessage());
            return "";
        }
    }

    /**
     * read the &lt;sessionId&gt; field as String
     *
     * @return value of sessionId field
     */
    public String getSessionId() {
        final NodeList sessionIdTagList = xmlDoc.getElementsByTagName("sessionId");
        final String givenSessId = getCharacterDataFromElement((Element) sessionIdTagList.item(0));
        return givenSessId;
    }

    /**
     * converts the string we got from the radio to a parsable XML document
     *
     * @param xmlString
     *            the XML string read from the radio
     * @return the parsed XML document
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document getXmlDocFromString(String xmlString)
            throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document xmlDocument = builder.parse(new InputSource(new StringReader(xmlString)));
        return xmlDocument;
    }

    /**
     * convert the value of a given XML element to a string for further processing
     *
     * @param e
     *            XML Element
     * @return the elements value converted to string
     */
    private static String getCharacterDataFromElement(Element e) {
        final Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            final CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }
}
