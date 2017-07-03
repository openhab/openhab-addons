/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.scalarweb.models.api.GetServiceProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebDeviceInfo.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebDeviceInfo implements AutoCloseable {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebDeviceInfo.class);

    /** The requestor. */
    private final HttpRequest _requestor;

    /** The version. */
    private String _version;

    /** The base url. */
    private URI _baseUrl;

    /** The services. */
    private final Map<String, ScalarWebService> _services;

    /**
     * Instantiates a new scalar web device info.
     *
     * @param service the service
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DOMException the DOM exception
     * @throws URISyntaxException the URI syntax exception
     */
    public ScalarWebDeviceInfo(Node service) throws IOException, DOMException, URISyntaxException {
        String version = "1.0"; // default version
        URI baseUrl = null;

        final Set<String> servicesNames = new HashSet<String>();
        final NodeList nodes = service.getChildNodes();
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            final Node node = nodes.item(i);
            final String nodeName = node.getLocalName();

            if ("X_ScalarWebAPI_Version".equalsIgnoreCase(nodeName)) {
                version = node.getTextContent();
            } else if ("X_ScalarWebAPI_BaseURL".equalsIgnoreCase(nodeName)) {
                baseUrl = new URI(node.getTextContent());
            } else if ("X_ScalarWebAPI_ServiceList".equalsIgnoreCase(nodeName)) {

                final NodeList sts = ((Element) node).getElementsByTagNameNS(ScalarWebState.SONY_AV_NS,
                        "X_ScalarWebAPI_ServiceType");

                for (int j = sts.getLength() - 1; j >= 0; j--) {
                    servicesNames.add(sts.item(j).getTextContent());
                }
            }
        }

        if (baseUrl == null) {
            throw new IOException("X_ScalarWebAPI_BaseURL was not found");
        }

        _version = version;
        _baseUrl = baseUrl;

        _requestor = new HttpRequest();
        // _client.register(new LoggingFilter());
        _requestor.register(new ScalarAuthFilter(this));

        // MERGE services from 'sony/guide'->getServiceprotocols
        try {
            final ScalarWebService guide = new ScalarWebService(_requestor, baseUrl, ScalarWebService.Guide, version);
            final GetServiceProtocols serviceProtocols = guide.execute(ScalarWebMethod.GetServiceProtocols)
                    .as(GetServiceProtocols.class);
            for (String serviceName : serviceProtocols.getServiceNames()) {
                servicesNames.add(serviceName);
            }
        } catch (IOException e) {
            // ignore
        }

        Map<String, ScalarWebService> myServices = new HashMap<String, ScalarWebService>();
        for (String serviceName : servicesNames) {
            try {
                final ScalarWebService sv = new ScalarWebService(_requestor, baseUrl, serviceName, version);
                myServices.put(serviceName, sv);
            } catch (IOException e) {
                logger.debug("Service '{}' is unavailable: {} ", serviceName, e.getMessage(), e);
            }
        }
        _services = Collections.unmodifiableMap(myServices);
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return _version;
    }

    /**
     * Gets the base url.
     *
     * @return the base url
     */
    public URI getBaseUrl() {
        return _baseUrl;
    }

    /**
     * Gets the services.
     *
     * @return the services
     */
    public Collection<ScalarWebService> getServices() {
        return _services.values();
    }

    /**
     * Gets the service.
     *
     * @param serviceName the service name
     * @return the service
     */
    public ScalarWebService getService(String serviceName) {
        return _services.get(serviceName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final String newLine = System.lineSeparator();

        final Set<String> serviceNames = new TreeSet<String>(_services.keySet());

        for (String serviceName : serviceNames) {
            final ScalarWebService service = _services.get(serviceName);
            if (service != null) {
                sb.append(service);
                sb.append(newLine);
            }
        }

        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        _requestor.close();
    }
}