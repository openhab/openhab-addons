/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.upnp.models;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpStatus;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class UpnpService.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class UpnpService {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(UpnpService.class);

    /** The service id. */
    private String serviceId;

    /** The service type. */
    private String serviceType;

    /** The scpd url. */
    private String scpdUrl;

    /** The control url. */
    private String controlUrl;

    /** The service descriptor. */
    private UpnpServiceDescriptor serviceDescriptor;

    /**
     * Instantiates a new upnp service.
     *
     * @param request the request
     * @param baseUrl the base url
     * @param service the service
     * @throws DOMException the DOM exception
     * @throws URISyntaxException the URI syntax exception
     */
    public UpnpService(HttpRequest request, URI baseUrl, Node service) throws DOMException, URISyntaxException {
        final NodeList nodes = service.getChildNodes();
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            final Node node = nodes.item(i);
            final String nodeName = node.getNodeName();

            if ("serviceid".equalsIgnoreCase(nodeName)) {
                serviceId = node.getTextContent();
            } else if ("servicetype".equalsIgnoreCase(nodeName)) {
                serviceType = node.getTextContent();
            } else if ("scpdurl".equalsIgnoreCase(nodeName)) {
                scpdUrl = NetUtilities.getUri(baseUrl, node.getTextContent()).toString();
                final HttpResponse resp = request.sendGetCommand(scpdUrl);
                if (resp.getHttpCode() == HttpStatus.SC_OK) {
                    try {
                        serviceDescriptor = new UpnpServiceDescriptor(this, resp.getContentAsXml());
                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        logger.warn("Unknown response from {}: {}", scpdUrl, resp);
                    }
                } else {
                    logger.warn("Unknown response from {}: {}", scpdUrl, resp);
                }
            } else if ("controlurl".equalsIgnoreCase(nodeName)) {
                controlUrl = NetUtilities.getUri(baseUrl, node.getTextContent()).toString();
            }
        }
    }

    /**
     * Gets the service descriptor.
     *
     * @return the service descriptor
     */
    public UpnpServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    /**
     * Gets the control url.
     *
     * @return the control url
     */
    public String getControlUrl() {
        return controlUrl;
    }

    /**
     * Gets the service type.
     *
     * @return the service type
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Gets the service id.
     *
     * @return the service id
     */
    public String getServiceId() {
        return serviceId;
    }
}