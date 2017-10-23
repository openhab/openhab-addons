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

    /**
     * Future Functionality
     * private HashMap<String, String> getAllSubTags(String tag, Element eElement) {
     * HashMap<String, String> map = new HashMap<String, String>();
     * for (int i = 0; i < eElement.getElementsByTagName(tag).getLength(); i++) {
     * String id = eElement.getElementsByTagName(tag).item(i).getAttributes().getNamedItem("id").toString()
     * .replace("id=", "").replace("\"", "");
     * String app = eElement.getElementsByTagName(tag).item(i).getTextContent();
     * map.put(app, id);
     * }
     * return map;
     * }
     */

    public void updateState(RokuState state) throws IOException {
        Document doc = getRequest(ROKU_DEVICE_INFO);
        String[] methodStringArray = { "udn", "serial-number", "device-id", "vendor-name", "model-name", "model-number",
                "model-region", "wifi-mac", "ethernet-mac", "network-type", "user-device-name", "software-version",
                "software-build", "power-mode", "headphones-connected" };
        doc.getDocumentElement().normalize();
        if (doc != null) {
            NodeList nList = doc.getElementsByTagName("device-info");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                logger.debug("Current Element: " + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    for (int ii = 0; ii < methodStringArray.length; ii++) {
                        Element eElement = (Element) nNode;
                        Class<RokuState> aClass = RokuState.class;
                        Field field = null;
                        try {
                            field = aClass.getField(methodStringArray[ii].replace("-", "_"));
                            field.set(state, getTagName(methodStringArray[ii], eElement));
                        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                                | IllegalAccessException e) {
                            logger.error("Method not found {}", methodStringArray[ii].replace("-", "_"));
                        }
                    }
                }
            }
        }
        doc = getRequest(ROKU_ACTIVE_APP);
        doc.getDocumentElement().normalize();
        if (doc != null) {
            NodeList nList = doc.getElementsByTagName("active-app");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                Element eElement = (Element) nNode;
                logger.debug("Current Element: " + nNode.getNodeName());
                try {
                    state.active_app = getTagName("screensaver", eElement);
                    String app_value = getSubTagName("screensaver", eElement);
                    try {
                        state.active_app_img = HttpUtil
                                .downloadImage("http://" + host + ":" + port + "/query/icon/" + app_value);
                    } catch (Exception e) {
                        logger.debug("Failed to get channel artwork for: {}", e);
                    }
                } catch (NullPointerException e) {
                    state.active_app = getTagName("app", eElement);
                    String app_value = getSubTagName("app", eElement);
                    try {
                        state.active_app_img = HttpUtil
                                .downloadImage("http://" + host + ":" + port + "/query/icon/" + app_value);
                    } catch (Exception e1) {
                        logger.debug("Failed to get channel artwork for: {}", e1);
                    }
                }
            }
        }
        /**
         * Future Functionality
         * doc = getRequest(ROKU_QUERY_APPS);
         * doc.getDocumentElement().normalize();
         * if (doc != null) {
         * NodeList nList = doc.getElementsByTagName("apps");
         * for (int i = 0; i < nList.getLength(); i++) {
         * Node nNode = nList.item(i);
         * Element eElement = (Element) nNode;
         * logger.debug("Current Element: " + nNode.getNodeName());
         * HashMap<String, String> map = getAllSubTags("app", eElement);
         * StringBuffer sb = new StringBuffer();
         * for (String key : map.keySet()) {
         * sb.append(key + " => " + map.get(key) + "\n");
         * }
         * state.application_menu = new StringType(sb.toString());
         * }
         * }
         */
    }

    private Document getRequest(String context) throws IOException {
        String response = HttpUtil.executeUrl("GET", "http://" + host + ":" + port + context, 5000);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(response)));
            if (doc.getFirstChild().hasChildNodes() == false) {
                throw new IOException("Could not handle response");
            }
            return doc;
        } catch (Exception e) {
            throw new IOException("Could not handle response", e);
        }
    }
}
