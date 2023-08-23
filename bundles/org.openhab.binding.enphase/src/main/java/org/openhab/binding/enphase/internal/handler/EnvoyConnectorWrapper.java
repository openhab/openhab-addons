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
package org.openhab.binding.enphase.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.enphase.internal.EnvoyConfiguration;
import org.openhab.binding.enphase.internal.exception.EnphaseException;
import org.openhab.binding.enphase.internal.exception.EnvoyConnectionException;
import org.openhab.binding.enphase.internal.exception.EnvoyNoHostnameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps around the specific Envoy connector and provides methods to determine which connector to use.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class EnvoyConnectorWrapper {

    private final Logger logger = LoggerFactory.getLogger(EnvoyConnectorWrapper.class);
    private final HttpClient httpClient;
    private @Nullable EnvoyConnector connector;
    private @Nullable String version;

    public EnvoyConnectorWrapper(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public @Nullable String getVersion() {
        return version;
    }

    /**
     * Sets the Envoy software version as retreived from the Envoy data.
     *
     * @param version Envoy software version.
     */
    public void setVersion(final @Nullable String version) {
        if (version == null) {
            return;
        }
        logger.debug("Set Envoy version found in the Envoy data: {}", version);
        this.version = version;
    }

    /**
     * Set the connector given the configuration.
     *
     * @param configuration configuration to use to set the connector.
     * @return Returns configuration error message or empty string if no configuration errors present
     * @throws EnphaseException
     */
    public synchronized String setConnector(final EnvoyConfiguration configuration) throws EnphaseException {
        final EnvoyConnector connector = determineConnector(configuration.hostname);
        final String message = connector.setConfiguration(configuration);

        // Only set connector if no error messages.
        if (message.isEmpty()) {
            this.connector = connector;
        }
        return message;
    }

    /**
     * @return Returns true if a connection with the Envoy has been established.
     */
    public boolean hasConnection() {
        return connector != null;
    }

    /**
     * @return Returns the connector when present. This method should only be called when a connector is present as
     *         returned by the {@link #hasConnection()} method.
     */
    public EnvoyConnector getConnector() throws EnvoyConnectionException {
        final EnvoyConnector connector = this.connector;

        if (connector == null) {
            throw new EnvoyConnectionException("No connection to the Envoy. Check your configuration.");
        }
        return connector;
    }

    private EnvoyConnector determineConnector(final String hostname) throws EnphaseException {
        final EnvoyConnector connectorByVersion = determineConnectorOnVersion();

        if (connectorByVersion != null) {
            return connectorByVersion;
        }

        if (hostname.isBlank()) {
            throw new EnvoyNoHostnameException("No hostname available.");
        }
        final EnvoyConnector envoyConnector = new EnvoyConnector(httpClient);
        final String version = envoyConnector.checkConnection(hostname);

        if (version != null) {
            this.version = version;
            final int majorVersionNumber = determineMajorVersionNumber();

            if (majorVersionNumber > 0 && majorVersionNumber < 7) {
                logger.debug(
                        "Connection to Envoy determined by getting a reply from the Envoy using the prior to version 7 method.");
                return envoyConnector;
            }
            if (majorVersionNumber >= 7) {
                logger.debug(
                        "Connection to Envoy determined by getting a reply from the Envoy using version 7 connection method.");
                return new EnvoyEntrezConnector(httpClient);
            }
        }
        final EnvoyEntrezConnector envoyEntrezConnector = new EnvoyEntrezConnector(httpClient);
        final String entrezVersion = envoyEntrezConnector.checkConnection(hostname);

        if (entrezVersion != null) {
            this.version = entrezVersion;
            final int majorVersionNumber = determineMajorVersionNumber();

            if (majorVersionNumber >= 7) {
                logger.info(
                        "Connection to Envoy determined by getting a reply from the Envoy using version 7 connection method.");
                return envoyEntrezConnector;
            }
        }
        throw new EnphaseException("No connection could be made with the Envoy. Check your connection/hostname.");
    }

    private @Nullable EnvoyConnector determineConnectorOnVersion() {
        final int majorVersionNumber = determineMajorVersionNumber();

        if (majorVersionNumber < 0) {
            return null;
        } else if (majorVersionNumber < 7) {
            logger.debug("Connect to Envoy based on version number {} using standard connector", version);
            return new EnvoyConnector(httpClient);
        } else {
            logger.debug("Connect to Envoy based on version number {} using entrez connector", version);
            return new EnvoyEntrezConnector(httpClient);
        }
    }

    private int determineMajorVersionNumber() {
        final String version = this.version;

        if (version == null) {
            return -1;
        }
        logger.debug("Envoy version information used to determine actual version: {}", version);
        final int marjorVersionIndex = version.indexOf('.');

        if (marjorVersionIndex < 0) {
            return -1;
        }
        try {
            return Integer.parseInt(version.substring(0, marjorVersionIndex));
        } catch (final NumberFormatException e) {
            logger.trace("Could not parse major version number in {}, error message: {}", version, e.getMessage());
            return -1;
        }
    }
}
