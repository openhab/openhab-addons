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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.scalarweb.gson.GsonUtilities;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebRequest;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.SonyTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class represents a factory to create {@link ScalarWebClient}
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebClientFactory {
    /** The likely base URL path to the sony services */
    private static final String LIKELY_PATH = "/sony";

    /** The likely port for the base URL (ie use default port for protocol [80=http,443=https]) */
    private static final int LIKELY_PORT = -1;

    /** Default audio port for soundbars/receiver (websocket) */
    private static final int HOME_AUDIO_PORT = 10000;

    /** Default audio port for wireless speakers (websocket) */
    private static final int PERSONAL_AUDIO_PORT = 54480;

    /** Websocket guide path */
    private static final String LIKELY_GUIDE_PATH = LIKELY_PATH + "/guide";

    /**
     * Gets a {@link ScalarWebClient} for the given URL and context
     * 
     * @param scalarWebUrl a non-null, non-empty URL
     * @param context a non-null context
     * @return a {@link ScalarWebClient}
     * @throws IOException if an IO Exception occurs
     * @throws ParserConfigurationException if a parser configuration exception occurrs
     * @throws SAXException if a SAX exception occurs readonly the documents
     * @throws URISyntaxException if a URI syntax exception occurs
     */
    public static ScalarWebClient get(final String scalarWebUrl, final ScalarWebContext context)
            throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
        Validate.notEmpty(scalarWebUrl, "scalarWebUrl cannot be empty");
        Objects.requireNonNull(context, "context cannot be null");

        return get(new URL(scalarWebUrl), context);
    }

    /**
     * Gets a {@link ScalarWebClient} for the given URL and context
     * 
     * @param scalarWebUrl a non-null URL
     * @param context a non-null context
     * @return a {@link ScalarWebClient}
     * @throws IOException if an IO Exception occurs
     * @throws ParserConfigurationException if a parser configuration exception occurrs
     * @throws SAXException if a SAX exception occurs readonly the documents
     * @throws URISyntaxException if a URI syntax exception occurs
     */
    public static ScalarWebClient get(final URL scalarWebUrl, final ScalarWebContext context)
            throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
        Objects.requireNonNull(scalarWebUrl, "scalarWebUrl cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        final Logger logger = LoggerFactory.getLogger(ScalarWebClientFactory.class);
        if (StringUtils.isEmpty(scalarWebUrl.getPath())) {
            return getDefaultClient(scalarWebUrl, context, logger);
        } else {
            return queryScalarWebSclient(scalarWebUrl, context, logger);
        }
    }

    /**
     * Helper method to attempt to get a 'default' client. Basically if we have just an ip address, try to get a client
     * base on querying it
     * 
     * @param scalarWebUrl a non-null URL
     * @param context a non-null context
     * @param logger a non-null logger
     * @return a {@link ScalarWebClient}
     * @throws IOException if an IO Exception occurs
     * @throws DOMException if a DOM exception occurs readonly the documents
     * @throws URISyntaxException if a URI syntax exception occurs
     */
    private static ScalarWebClient getDefaultClient(final URL scalarWebUrl, final ScalarWebContext context,
            final Logger logger) throws DOMException, IOException, URISyntaxException {
        Objects.requireNonNull(scalarWebUrl, "scalarWebUrl cannot be null");
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(logger, "logger cannot be null");

        final ScalarWebClient homeClient = tryDefaultClientUrl(scalarWebUrl, HOME_AUDIO_PORT, context, logger);
        if (homeClient != null) {
            return homeClient;
        }

        final ScalarWebClient personalClient = tryDefaultClientUrl(scalarWebUrl, PERSONAL_AUDIO_PORT, context, logger);
        if (personalClient != null) {
            return personalClient;
        }

        final URL baseUrl = new URL(scalarWebUrl.getProtocol(), scalarWebUrl.getHost(), LIKELY_PORT, LIKELY_PATH);
        return new ScalarWebDeviceManager(baseUrl, context);
    }

    /**
     * Helper method to try a specific port for our scalar url
     * 
     * @param scalarWebUrl a non-null scalar url
     * @param port a > 0 port number
     * @param context a non-null context
     * @param logger a non-null logger
     * @return the scalar web client (if found) or null if not
     * @throws URISyntaxException if a uri syntax is invalid
     * @throws DOMException if a dom exception occurs
     * @throws IOException if an IO exception occurs
     */
    private static @Nullable ScalarWebClient tryDefaultClientUrl(final URL scalarWebUrl, final int port,
            final ScalarWebContext context, final Logger logger) throws URISyntaxException, DOMException, IOException {
        Objects.requireNonNull(scalarWebUrl, "scalarWebUrl cannot be null");
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(logger, "logger cannot be null");
        if (port <= 0) {
            throw new IllegalArgumentException("port cannot be <= 0: " + port);
        }

        final URL likelyUrl = new URL(scalarWebUrl.getProtocol(), scalarWebUrl.getHost(), port, LIKELY_GUIDE_PATH);
        logger.debug("Testing Default Scalar Web client: {}", likelyUrl);
        try (SonyTransport transport = SonyTransportFactory.createHttpTransport(likelyUrl,
                GsonUtilities.getApiGson())) {
            // see ScalarWebRequest id field for explanation of why I used 1
            final ScalarWebResult res = transport
                    .execute(new ScalarWebRequest(ScalarWebMethod.GETVERSIONS, ScalarWebMethod.V1_0));
            if (res.getHttpResponse().getHttpCode() == HttpStatus.OK_200) {
                final URL baseUrl = new URL(scalarWebUrl.getProtocol(), scalarWebUrl.getHost(), port, LIKELY_PATH);
                return new ScalarWebDeviceManager(baseUrl, context);
            }
        }
        return null;
    }

    /**
     * Helper method to get the client to UPNP documents describing the device
     * 
     * @param scalarWebUrl a non-null URL
     * @param context a non-null context
     * @param logger a non-null logger
     * @return a {@link ScalarWebClient}
     * @throws IOException if an IO Exception occurs
     * @throws ParserConfigurationException if a parser configuration exception occurrs
     * @throws SAXException if a SAX exception occurs readonly the documents
     * @throws URISyntaxException if a URI syntax exception occurs
     */
    public static ScalarWebClient queryScalarWebSclient(final URL scalarWebUrl, final ScalarWebContext context,
            final Logger logger) throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
        Objects.requireNonNull(scalarWebUrl, "scalarWebUrl cannot be null");
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(logger, "logger cannot be null");

        try (SonyHttpTransport transport = SonyTransportFactory.createHttpTransport(scalarWebUrl.toExternalForm())) {
            final HttpResponse resp = transport.executeGet(scalarWebUrl.toExternalForm());

            if (resp.getHttpCode() == HttpStatus.OK_200) {
                final Document scalarWebDocument = resp.getContentAsXml();

                final NodeList deviceInfos = scalarWebDocument.getElementsByTagNameNS(ScalarWebDeviceManager.SONY_AV_NS,
                        "X_ScalarWebAPI_DeviceInfo");
                if (deviceInfos.getLength() > 1) {
                    logger.debug("More than one X_ScalarWebAPI_DeviceInfo found - using the first valid one");
                }

                // Use the first valid one
                ScalarWebDeviceManager myDevice = null;
                for (int i = deviceInfos.getLength() - 1; i >= 0; i--) {
                    final Node deviceInfo = deviceInfos.item(i);

                    try {
                        myDevice = ScalarWebDeviceManager.create(deviceInfo, context);
                        break;
                    } catch (IOException | DOMException e) {
                        logger.debug("Exception getting creating scalarwebapi device for {}[{}]: {}",
                                deviceInfo.getNodeName(), i, e.getMessage(), e);
                    }
                }
                if (myDevice == null) {
                    throw new IOException("No valid scalar web devices found");
                }

                return myDevice;
            } else {
                // If can't connect - try to connect to the likely websocket server directly using
                // the host name and default path
                return getDefaultClient(scalarWebUrl, context, logger);
            }
        }
    }
}
