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
import org.openhab.binding.enphase.internal.EnvoyConnectionException;
import org.openhab.binding.enphase.internal.EnvoyNoHostnameException;
import org.openhab.binding.enphase.internal.dto.EnvoyEnergyDTO;
import org.openhab.binding.enphase.internal.dto.EnvoyErrorDTO;
import org.openhab.binding.enphase.internal.dto.InventoryJsonDTO;
import org.openhab.binding.enphase.internal.dto.InverterDTO;
import org.openhab.binding.enphase.internal.dto.ProductionJsonDTO;
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
class EnvoyConnector {

    private static final String HTTP = "http://";
    private static final String PRODUCTION_JSON_URL = "/production.json";
    private static final String INVENTORY_JSON_URL = "/inventory.json";
    private static final String PRODUCTION_URL = "/api/v1/production";
    private static final String CONSUMPTION_URL = "/api/v1/consumption";
    private static final String INVERTERS_URL = PRODUCTION_URL + "/inverters";
    private static final long CONNECT_TIMEOUT_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(EnvoyConnector.class);
    private final Gson gson = new GsonBuilder().create();
    private final HttpClient httpClient;
    private String hostname = "";
    private @Nullable DigestAuthentication envoyAuthn;
    private @Nullable URI invertersURI;

    public EnvoyConnector(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sets the Envoy connection configuration.
     *
     * @param configuration the configuration to set
     */
    public void setConfiguration(final EnvoyConfiguration configuration) {
        hostname = configuration.hostname;
        if (hostname.isEmpty()) {
            return;
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
        invertersURI = URI.create(HTTP + hostname + INVERTERS_URL);
        envoyAuthn = new DigestAuthentication(invertersURI, Authentication.ANY_REALM, username, password);
        store.addAuthentication(envoyAuthn);
    }

    /**
     * @return Returns the production data from the Envoy gateway.
     */
    public EnvoyEnergyDTO getProduction() throws EnvoyConnectionException, EnvoyNoHostnameException {
        return retrieveData(PRODUCTION_URL, this::jsonToEnvoyEnergyDTO);
    }

    /**
     * @return Returns the consumption data from the Envoy gateway.
     */
    public EnvoyEnergyDTO getConsumption() throws EnvoyConnectionException, EnvoyNoHostnameException {
        return retrieveData(CONSUMPTION_URL, this::jsonToEnvoyEnergyDTO);
    }

    private @Nullable EnvoyEnergyDTO jsonToEnvoyEnergyDTO(final String json) {
        return gson.fromJson(json, EnvoyEnergyDTO.class);
    }

    /**
     * @return Returns the production/consumption data from the Envoy gateway.
     */
    public ProductionJsonDTO getProductionJson() throws EnvoyConnectionException, EnvoyNoHostnameException {
        return retrieveData(PRODUCTION_JSON_URL, json -> gson.fromJson(json, ProductionJsonDTO.class));
    }

    /**
     * @return Returns the inventory data from the Envoy gateway.
     */
    public List<InventoryJsonDTO> getInventoryJson() throws EnvoyConnectionException, EnvoyNoHostnameException {
        return retrieveData(INVENTORY_JSON_URL, this::jsonToEnvoyInventoryJson);
    }

    private @Nullable List<InventoryJsonDTO> jsonToEnvoyInventoryJson(final String json) {
        final InventoryJsonDTO @Nullable [] list = gson.fromJson(json, InventoryJsonDTO[].class);

        return list == null ? null : Arrays.asList(list);
    }

    /**
     * @return Returns the production data for the inverters.
     */
    public List<InverterDTO> getInverters() throws EnvoyConnectionException, EnvoyNoHostnameException {
        synchronized (this) {
            final AuthenticationStore store = httpClient.getAuthenticationStore();
            final Result invertersResult = store.findAuthenticationResult(invertersURI);

            if (invertersResult != null) {
                store.removeAuthenticationResult(invertersResult);
            }
        }
        return retrieveData(INVERTERS_URL, json -> Arrays.asList(gson.fromJson(json, InverterDTO[].class)));
    }

    private synchronized <T> T retrieveData(final String urlPath, final Function<String, @Nullable T> jsonConverter)
            throws EnvoyConnectionException, EnvoyNoHostnameException {
        try {
            if (hostname.isEmpty()) {
                throw new EnvoyNoHostnameException("No host name/ip address known (yet)");
            }
            final URI uri = URI.create(HTTP + hostname + urlPath);
            logger.trace("Retrieving data from '{}'", uri);
            final Request request = httpClient.newRequest(uri).method(HttpMethod.GET).timeout(CONNECT_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
            final ContentResponse response = request.send();
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
