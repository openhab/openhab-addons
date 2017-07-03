/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.dial.models;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class DialDeviceInfo.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class DialDeviceInfo {

    /** The apps list url. */
    private String appsListUrl;

    /** The device id. */
    private String deviceId;

    /** The device type. */
    private String deviceType;

    /** The apps. */
    private DialApps apps;

    /**
     * Instantiates a new dial device info.
     *
     * @param requestor the requestor
     * @param service the service
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public DialDeviceInfo(HttpRequest requestor, Node service)
            throws ParserConfigurationException, SAXException, IOException {
        final NodeList nodes = service.getChildNodes();
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            final Node node = nodes.item(i);
            final String nodeName = node.getLocalName();

            if ("X_DIALEX_AppsListURL".equalsIgnoreCase(nodeName)) {
                appsListUrl = node.getTextContent();

                if (StringUtils.isNotEmpty(appsListUrl)) {
                    final HttpResponse resp = requestor.sendGetCommand(appsListUrl);
                    if (resp.getHttpCode() == HttpStatus.SC_OK) {
                        apps = new DialApps(resp.getContentAsXml());
                    } else {
                        throw resp.createException();
                    }
                }
            } else if ("X_DIALEX_DeviceID".equalsIgnoreCase(nodeName)) {
                deviceId = node.getTextContent();
            } else if ("X_DIALEX_DeviceType".equalsIgnoreCase(nodeName)) {
                deviceType = node.getTextContent();
            }
        }
    }

    /**
     * Gets the device id.
     *
     * @return the device id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the device type.
     *
     * @return the device type
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Gets the dial apps.
     *
     * @return the dial apps
     */
    public DialApps getDialApps() {
        return apps;
    }
}