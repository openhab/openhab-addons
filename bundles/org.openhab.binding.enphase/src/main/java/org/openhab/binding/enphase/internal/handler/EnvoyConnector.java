/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.Authentication.Result;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.enphase.internal.EnphaseBindingConstants;
import org.openhab.binding.enphase.internal.EnvoyConfiguration;
import org.openhab.binding.enphase.internal.dto.EnvoyEnergyDTO;
import org.openhab.binding.enphase.internal.dto.EnvoyErrorDTO;
import org.openhab.binding.enphase.internal.dto.InventoryJsonDTO;
import org.openhab.binding.enphase.internal.dto.InverterDTO;
import org.openhab.binding.enphase.internal.dto.ProductionJsonDTO;
import org.openhab.binding.enphase.internal.exception.EnphaseException;
import org.openhab.binding.enphase.internal.exception.EnvoyConnectionException;
import org.openhab.binding.enphase.internal.exception.EnvoyNoHostnameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Methods to make API calls to the Envoy gateway.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class EnvoyConnector {

    protected static final long CONNECT_TIMEOUT_SECONDS = 10;

    private static final String HTTP = "http://";
    private static final String PRODUCTION_JSON_URL = "/production.json";
    private static final String INVENTORY_JSON_URL = "/inventory.json";
    private static final String PRODUCTION_URL = "/api/v1/production";
    private static final String CONSUMPTION_URL = "/api/v1/consumption";
    private static final String INVERTERS_URL = PRODUCTION_URL + "/inverters";
    private static final String INFO_XML = "/info.xml";

    private static final String INFO_SOFTWARE_BEGIN = "<software>";
    private static final String INFO_SOFTWARE_END = "</software>";

    protected final HttpClient httpClient;
    protected final Gson gson = new GsonBuilder().create();

    private final Logger logger = LoggerFactory.getLogger(EnvoyConnector.class);
    private final String schema;

    private @Nullable DigestAuthentication envoyAuthn;
    private @Nullable URI invertersURI;

    protected @NonNullByDefault({}) EnvoyConfiguration configuration;

    public EnvoyConnector(final HttpClient httpClient) {
        this(httpClient, HTTP);
    }

    protected EnvoyConnector(final HttpClient httpClient, final String schema) {
        this.httpClient = httpClient;
        this.schema = schema;
    }

    /**
     * Sets the Envoy connection configuration.
     *
     * @param configuration the configuration to set
     * @return configuration error message or empty string if no configuration errors present
     */
    public String setConfiguration(final EnvoyConfiguration configuration) {
        this.configuration = configuration;

        if (configuration.hostname.isEmpty()) {
            return "";
        }
        final String password = configuration.password.isEmpty()
                ? EnphaseBindingConstants.defaultPassword(configuration.serialNumber)
                : configuration.password;
        final String username = configuration.username.isEmpty() ? EnvoyConfiguration.DEFAULT_USERNAME
                : configuration.username;

        final AuthenticationStore store = httpClient.getAuthenticationStore();

        if (envoyAuthn != null) {
            store.removeAuthentication(envoyAuthn);
        }
        invertersURI = URI.create(schema + configuration.hostname + INVERTERS_URL);
        envoyAuthn = new DigestAuthentication(invertersURI, Authentication.ANY_REALM, username, password);
        store.addAuthentication(envoyAuthn);
        return "";
    }

    /**
     * Checks if data can be read from the Envoy, and to determine the software version returned by the Envoy.
     *
     * @param hostname hostname of the Envoy.
     * @return software version number as reported by the the Envoy or null if connection could be made or software
     *         version not detected.
     */
    protected @Nullable String checkConnection(final String hostname) {
        try {
            final String url = hostname + INFO_XML;
            logger.debug("Check connection to '{}'", url);
            final Request createRequest = createRequest(url);
            final ContentResponse response = send(createRequest);

            logger.debug("Checkconnection status from request is: {}", response.getStatus());
            if (response.getStatus() == HttpStatus.OK_200) {
                final String content = response.getContentAsString();
                final int begin = content.indexOf(INFO_SOFTWARE_BEGIN);
                final int end = content.lastIndexOf(INFO_SOFTWARE_END);

                if (begin > 0 && end > 0) {
                    final String version = content.substring(begin + INFO_SOFTWARE_BEGIN.length(), end);

                    logger.debug("Found Envoy version number '{}' in info.xml", version);
                    return Character.isDigit(version.charAt(0)) ? version : version.substring(1);
                }
            }
        } catch (EnphaseException | HttpResponseException e) {
            logger.debug("Exception trying to check the connection.", e);
        }
        return null;
    }

    /**
     * @return Returns the production data from the Envoy gateway.
     */
    public EnvoyEnergyDTO getProduction() throws EnphaseException {
        return retrieveData(PRODUCTION_URL, this::jsonToEnvoyEnergyDTO);
    }

    /**
     * @return Returns the consumption data from the Envoy gateway.
     */
    public EnvoyEnergyDTO getConsumption() throws EnphaseException {
        return retrieveData(CONSUMPTION_URL, this::jsonToEnvoyEnergyDTO);
    }

    private @Nullable EnvoyEnergyDTO jsonToEnvoyEnergyDTO(final String json) {
        return gson.fromJson(json, EnvoyEnergyDTO.class);
    }

    /**
     * @return Returns the production/consumption data from the Envoy gateway.
     */
    public ProductionJsonDTO getProductionJson() throws EnphaseException {
        return retrieveData(PRODUCTION_JSON_URL, json -> gson.fromJson(json, ProductionJsonDTO.class));
    }

    /**
     * @return Returns the inventory data from the Envoy gateway.
     */
    public List<InventoryJsonDTO> getInventoryJson() throws EnphaseException {
        return retrieveData(INVENTORY_JSON_URL, this::jsonToEnvoyInventoryJson);
    }

    private @Nullable List<InventoryJsonDTO> jsonToEnvoyInventoryJson(final String json) {
        final InventoryJsonDTO @Nullable [] list = gson.fromJson(json, InventoryJsonDTO[].class);

        return list == null ? null : Arrays.asList(list);
    }

    /**
     * @return Returns the production data for the inverters.
     */
    public List<InverterDTO> getInverters() throws EnphaseException {
        synchronized (this) {
            final AuthenticationStore store = httpClient.getAuthenticationStore();
            final Result invertersResult = store.findAuthenticationResult(invertersURI);

            if (invertersResult != null) {
                store.removeAuthenticationResult(invertersResult);
            }
        }
        return retrieveData(INVERTERS_URL, json -> Arrays.asList(gson.fromJson(json, InverterDTO[].class)));
    }

    protected synchronized <T> T retrieveData(final String urlPath, final Function<String, @Nullable T> jsonConverter)
            throws EnphaseException {
        final Request request = createRequest(configuration.hostname + urlPath);

        constructRequest(request);
        final ContentResponse response = send(request);
        final String content = response.getContentAsString();

        logger.trace("Envoy returned data for '{}' with status {}: {}", urlPath, response.getStatus(), content);
        try {
            if (response.getStatus() == HttpStatus.OK_200) {
                final T result = jsonConverter.apply(content);
                if (result == null) {
                    throw new EnvoyConnectionException("No data received");
                }
                return result;
            } else {
                final @Nullable EnvoyErrorDTO error = gson.fromJson(content, EnvoyErrorDTO.class);

                logger.debug("Envoy returned an error: {}", error);
                throw new EnvoyConnectionException(error == null ? response.getReason() : error.info);
            }
        } catch (final JsonSyntaxException e) {
            logger.debug("Error parsing json: {}", content, e);
            throw new EnvoyConnectionException("Error parsing data: ", e);
        }
    }

    private Request createRequest(final String urlPath) throws EnvoyNoHostnameException {
        return httpClient.newRequest(URI.create(schema + urlPath)).method(HttpMethod.GET)
                .timeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    protected void constructRequest(final Request request) throws EnphaseException {
        logger.trace("Retrieving data from '{}' ", request.getURI());
    }

    protected ContentResponse send(final Request request) throws EnvoyConnectionException {
        try {
            return request.send();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EnvoyConnectionException("Interrupted");
        } catch (final TimeoutException e) {
            logger.debug("TimeoutException: {}", e.getMessage());
            throw new EnvoyConnectionException("Connection timeout: ", e);
        } catch (final ExecutionException e) {
            logger.debug("ExecutionException: {}", e.getMessage(), e);
            throw new EnvoyConnectionException("Could not retrieve data: ", e.getCause());
        }
    }
}
