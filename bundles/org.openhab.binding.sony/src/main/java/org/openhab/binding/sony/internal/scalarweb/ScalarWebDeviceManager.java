/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.scalarweb.gson.GsonUtilities;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.api.ServiceProtocol;
import org.openhab.binding.sony.internal.scalarweb.models.api.ServiceProtocols;
import org.openhab.binding.sony.internal.scalarweb.models.api.SupportedApi;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;

/**
 * This class represents device manager
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebDeviceManager implements ScalarWebClient {
    /** The Constant for the sony upnp identifier */
    public static final String SONY_AV_NS = "urn:schemas-sony-com:av";

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ScalarWebDeviceManager.class);

    /** The device version */
    private String version;

    /** The URL of the device */
    private URL baseUrl;

    /** The services offered by the device */
    private final Map<String, ScalarWebService> services;

    /**
     * Constructs a device manager from the base URL
     *
     * @param baseUrl a non-null base url
     * @param context a non-null context to use
     * @throws IOException if an IOException occurs contacting the device
     * @throws DOMException if a DOMException occurs processing the XML from the device
     * @throws MalformedURLException if there is a malformed URL (in the device XML)
     * @throws URISyntaxException if a URI syntax exception occurs
     */
    public ScalarWebDeviceManager(final URL baseUrl, final ScalarWebContext context)
            throws IOException, DOMException, MalformedURLException, URISyntaxException {
        this(baseUrl, "1.0", new HashSet<>(), context);
    }

    /**
     * Private contructor to create a device manager from the parameters
     *
     * @param baseUrl a non-null base URL
     * @param version a non-null, non-empty API version
     * @param serviceProtocols a non-null, possibly empty list of protocols
     * @param context a non-null context
     * @throws IOException if an IOException occurs contacting the device
     * @throws DOMException if a DOMException occurs processing the XML from the device
     * @throws MalformedURLException if there is a malformed URL (in the device XML)
     * @throws URISyntaxException if an URI exception occurs
     */
    private ScalarWebDeviceManager(final URL baseUrl, final String version, final Set<ServiceProtocol> serviceProtocols,
            final ScalarWebContext context)
            throws IOException, DOMException, MalformedURLException, URISyntaxException {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        Validate.notEmpty(version, "version cannot be empty");
        Objects.requireNonNull(serviceProtocols, "serviceProtocols cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        this.version = version;
        this.baseUrl = baseUrl;

        final Gson gson = GsonUtilities.getApiGson();

        final SonyTransportFactory transportFactory = new SonyTransportFactory(baseUrl, gson,
                context.getWebSocketClient(), context.getScheduler());

        final Set<ServiceProtocol> myServiceProtocols = new HashSet<>(serviceProtocols);

        try (final SonyHttpTransport httpTransport = SonyTransportFactory.createHttpTransport(baseUrl,
                ScalarWebService.GUIDE)) {
            // Manually create the guide as it's used to get service protocols and supported methods
            // Must use alternative supported api since SupportedApi requires a guide!
            final SupportedApi guideApi = SupportedApi.getSupportApiAlternate(ScalarWebService.GUIDE, httpTransport,
                    logger);
            final ScalarWebService guide = new ScalarWebService(transportFactory,
                    new ServiceProtocol(ScalarWebService.GUIDE, Collections.singleton(SonyTransportFactory.HTTP)),
                    version, guideApi);

            final ServiceProtocols sps = guide.execute(ScalarWebMethod.GETSERVICEPROTOCOLS).as(ServiceProtocols.class);
            for (final ServiceProtocol serviceProtocol : sps.getServiceProtocols()) {
                // remove the one from the device descriptor above (keyed by name)
                // the add this one (which has protocol information)
                myServiceProtocols.remove(serviceProtocol);
                myServiceProtocols.add(serviceProtocol);
            }

            final Map<String, ScalarWebService> myServices = new HashMap<String, ScalarWebService>();
            myServices.put(ScalarWebService.GUIDE, guide);

            for (ServiceProtocol serviceProtocol : myServiceProtocols) {
                // Ignore the guid - we already added it above
                if (StringUtils.equalsIgnoreCase(ScalarWebService.GUIDE, serviceProtocol.getServiceName())) {
                    continue;
                }

                // Must create a new http transport specific to the service name in case
                // getSupportedApi doesn't exist on the guide service and we fallback to using
                // an http getversions/getmethodtypes alternative for the service
                try (final SonyHttpTransport srvHttpTransport = SonyTransportFactory.createHttpTransport(baseUrl,
                        serviceProtocol.getServiceName())) {
                    final SupportedApi srvApi = SupportedApi.getSupportedApi(guide, serviceProtocol.getServiceName(),
                            srvHttpTransport, logger);
                    final ScalarWebService sws = new ScalarWebService(transportFactory, serviceProtocol, version,
                            srvApi);
                    myServices.put(sws.getServiceName(), sws);
                }
            }
            services = Collections.unmodifiableMap(myServices);
        }
    }

    /**
     * Creates a scalar web device manager from the provided information
     *
     * @param deviceInfo a non-null device info node describing the device
     * @param context a non-null context to use
     * @return a non-null scalar web device manager
     * @throws IOException if an IOException occurs contacting the device
     * @throws DOMException if a DOMException occurs processing the XML from the device
     * @throws MalformedURLException if there is a malformed URL (in the device XML)
     * @throws URISyntaxException if a URI syntax exception occurs
     */
    public static ScalarWebDeviceManager create(final Node deviceInfo, final ScalarWebContext context)
            throws IOException, DOMException, MalformedURLException, URISyntaxException {
        Objects.requireNonNull(deviceInfo, "service cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        String version = "1.0"; // default version
        URL baseUrl = null;

        final Set<ServiceProtocol> serviceProtocols = new HashSet<ServiceProtocol>();

        final NodeList nodes = deviceInfo.getChildNodes();
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            final Node node = nodes.item(i);
            final String nodeName = node.getLocalName();

            if ("X_ScalarWebAPI_Version".equalsIgnoreCase(nodeName)) {
                version = node.getTextContent();
            } else if ("X_ScalarWebAPI_BaseURL".equalsIgnoreCase(nodeName)) {
                baseUrl = new URL(node.getTextContent());
            } else if ("X_ScalarWebAPI_ServiceList".equalsIgnoreCase(nodeName)) {
                final NodeList sts = ((Element) node).getElementsByTagNameNS(SONY_AV_NS, "X_ScalarWebAPI_ServiceType");

                for (int j = sts.getLength() - 1; j >= 0; j--) {
                    // assume auto transport since we don't know
                    serviceProtocols.add(new ServiceProtocol(sts.item(j).getTextContent(),
                            Collections.singleton(SonyTransportFactory.AUTO)));
                }
            }
        }

        if (baseUrl == null) {
            throw new IOException("X_ScalarWebAPI_BaseURL was not found");
        }

        return new ScalarWebDeviceManager(baseUrl, version, serviceProtocols, context);
    }

    /**
     * Gets the device version
     *
     * @return the device version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the base url of the device
     *
     * @return the base url of the device
     */
    public URL getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the services offered by the device
     *
     * @return the non-null, possibly empty immutable collection of services
     */
    public Collection<ScalarWebService> getServices() {
        return services.values();
    }

    /**
     * Gets the service for the service name
     *
     * @param serviceName the service name to try
     * @return the service or null if not found
     */
    public @Nullable ScalarWebService getService(final String serviceName) {
        return services.get(serviceName);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final String newLine = System.lineSeparator();

        final Set<String> serviceNames = new TreeSet<String>(services.keySet());

        for (final String serviceName : serviceNames) {
            final ScalarWebService service = services.get(serviceName);
            if (service != null) {
                sb.append(service);
                sb.append(newLine);
            }
        }

        return sb.toString();
    }

    @Override
    public ScalarWebDeviceManager getDevice() {
        return this;
    }

    @Override
    public void close() {
        for (final Entry<String, ScalarWebService> srv : services.entrySet()) {
            srv.getValue().close();
        }
    }
}
