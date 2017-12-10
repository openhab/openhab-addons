/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku.internal.protocol;

import static org.openhab.binding.roku.RokuBindingConstants.*;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.roku.internal.RokuState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link RokuCommunication} class is what communicates directly with a Roku device (sending commands to it,
 * updating state, etc).
 *
 * @author Jarod Peters - Initial contribution
 */
public class RokuCommunication {
    private final Logger logger = LoggerFactory.getLogger(RokuCommunication.class);
    private final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private final String host;
    private final Number port;

    public RokuCommunication(String host, Number port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public Number getPort() {
        return port;
    }

    private StringType getTagName(String tag, Element eElement) {
        return new StringType(eElement.getElementsByTagName(tag).item(0).getTextContent());
    }

    private String getSubTagName(String tag, Element eElement) {
        return eElement.getElementsByTagName(tag).item(0).getAttributes().getNamedItem("id").toString()
                .replace("id=", "").replace("\"", "");
    }

    private String getRokuStateFieldForXmlName(String name) {
        String[] parts = name.split("-");
        StringBuilder sb = new StringBuilder(name.length() - parts.length + 1);
        sb.append(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
        }
        return sb.toString();
    }

    public void updateState(RokuState state) throws IOException {
        Document doc = getRequest(ROKU_DEVICE_INFO);
        if (doc == null) {
            return;
        }
        String[] methodStringArray = { "udn", "serial-number", "device-id", "vendor-name", "model-name", "model-number",
                "model-region", "wifi-mac", "ethernet-mac", "network-type", "user-device-name", "software-version",
                "software-build", "power-mode", "headphones-connected" };
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("device-info");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                for (int ii = 0; ii < methodStringArray.length; ii++) {
                    String fieldName = getRokuStateFieldForXmlName(methodStringArray[ii]);
                    Element eElement = (Element) nNode;
                    Class<RokuState> aClass = RokuState.class;
                    Field field = null;

                    try {
                        field = aClass.getField(fieldName);
                        field.set(state, getTagName(methodStringArray[ii], eElement));
                    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                            | IllegalAccessException e) {
                        logger.debug("Could not set field '{}': {}", fieldName, e.getMessage());
                    }
                }
            }
        }

        doc = getRequest(ROKU_ACTIVE_APP);
        if (doc == null) {
            return;
        }
        doc.getDocumentElement().normalize();
        nList = doc.getElementsByTagName("active-app");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            Element eElement = (Element) nNode;
            logger.debug("Current Element: " + nNode.getNodeName());
            try {
                state.activeApp = getTagName("screensaver", eElement);
                String app_value = getSubTagName("screensaver", eElement);
                try {
                    state.activeAppImg = HttpUtil
                            .downloadImage("http://" + host + ":" + port + "/query/icon/" + app_value);
                } catch (Exception e) {
                    logger.debug("Failed to get channel artwork for: {}", e);
                }
            } catch (NullPointerException e) {
                state.activeApp = getTagName("app", eElement);
                String app_value = getSubTagName("app", eElement);
                try {
                    state.activeAppImg = HttpUtil
                            .downloadImage("http://" + host + ":" + port + "/query/icon/" + app_value);
                } catch (Exception e1) {
                    logger.debug("Failed to get channel artwork for: {}", e1);
                }
            }
        }
    }

    private Document getRequest(String context) throws IOException {
        String response = HttpUtil.executeUrl("GET", "http://" + host + ":" + port + context, 5000);
        if (response == null) {
            logger.debug("Roku at {}:{} failed to respond", host, port);
        }
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(response)));
            if (doc.getFirstChild().hasChildNodes() == false) {
                logger.debug("Invalid response returned from {}:{}:\n{}", host, port, response);
                return null;
            }
            return doc;
        } catch (ParserConfigurationException | SAXException e) {
            logger.debug("Unable to parse response from {}:{}:\n{}", host, port, response);
            return null;
        }
    }
}
