/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atmofrance.internal.handler;

import static org.openhab.binding.atmofrance.internal.api.AtmoFranceApi.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.atmofrance.internal.AtmoFranceException;
import org.openhab.binding.atmofrance.internal.api.AtmoFranceApi;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.AtmoResponse;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.ErrorResponse;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.Feature;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.IndexProperties;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.LoginResponse;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.PollensProperties;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.PollensResponse;
import org.openhab.binding.atmofrance.internal.configuration.AtmoFranceConfiguration;
import org.openhab.binding.atmofrance.internal.configuration.ConfigurationLevel;
import org.openhab.binding.atmofrance.internal.deserialization.AtmoFranceDeserializer;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AtmoFranceApiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AtmoFranceApiHandler extends BaseBridgeHandler implements HandlerUtils {
    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final Logger logger = LoggerFactory.getLogger(AtmoFranceApiHandler.class);
    private final Map<String, ScheduledFuture<?>> jobs = new HashMap<>();
    private final AtmoFranceDeserializer deserializer;
    private final HttpClient httpClient;

    private @Nullable AtmoFranceConfiguration config;
    private @Nullable String bearer;

    public AtmoFranceApiHandler(Bridge bridge, HttpClient httpClient, AtmoFranceDeserializer deserializer) {
        super(bridge);
        this.deserializer = deserializer;
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Atmo France bridge handler.");
        var localConfig = getConfigAs(AtmoFranceConfiguration.class);
        ConfigurationLevel configLevel = localConfig.check();

        if (configLevel != ConfigurationLevel.COMPLETED) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configLevel.message);
            return;
        }
        this.config = localConfig;
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::initiateConnexion);
    }

    private void initiateConnexion() {
        try {
            LoginResponse login = executeUri(LOGIN_URI, LoginResponse.class,
                    deserializer.serialize(Objects.requireNonNull(config)));
            bearer = login.token();
            updateStatus(ThingStatus.ONLINE);
            schedule("token refresh", this::initiateConnexion, TOKEN_VALIDITY);
        } catch (AtmoFranceException e) {
            if (e.getStatusDetail() instanceof ThingStatusDetail statusDetail) {
                updateStatus(ThingStatus.OFFLINE, statusDetail, e.getMessage());
            } else {
                logger.warn("{} ", e.getMessage());
            }
        }
    }

    private synchronized <T> T executeUri(URI uri, Class<T> clazz) throws AtmoFranceException {
        String content = executeUri(uri, HttpMethod.GET, null);
        return deserializer.deserialize(clazz, content);
    }

    private synchronized <T> T executeUri(URI uri, Class<T> clazz, String payload) throws AtmoFranceException {
        String content = executeUri(uri, HttpMethod.POST, payload);
        return deserializer.deserialize(clazz, content);
    }

    private synchronized String executeUri(URI uri, HttpMethod method, @Nullable String payload)
            throws AtmoFranceException {
        logger.debug("executeUrl: {} ", uri);

        Request request = httpClient.newRequest(uri).method(method).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .header(HttpHeader.ACCEPT, MediaType.APPLICATION_JSON);

        handleBearer(request, bearer);
        handlePayload(request, payload);

        try {
            ContentResponse response = request.send();

            Code statusCode = HttpStatus.getCode(response.getStatus());
            String content = new String(response.getContent(), DEFAULT_CHARSET);
            if (statusCode == Code.OK) {
                logger.trace("executeUrl: {} returned {}", uri, content);
                return content;
            } else if (statusCode == Code.UNAUTHORIZED) {
                ErrorResponse error = deserializer.deserialize(ErrorResponse.class, content);
                throw new AtmoFranceException(ThingStatusDetail.CONFIGURATION_ERROR,
                        "com-error-unauthorized [\"%s\"]".formatted(error.message()));
            }
            throw new AtmoFranceException("Error '%s' requesting: %s", statusCode.getMessage(), uri.toString());
        } catch (TimeoutException | ExecutionException e) {
            throw new AtmoFranceException(e, "Exception while calling %s: %s", request.getURI(), e.getMessage());
        } catch (InterruptedException e) {
            throw new AtmoFranceException(e, "Execution interrupted: %s", e.getMessage());
        }
    }

    private void handlePayload(Request request, @Nullable String payload) {
        if (payload == null) {
            return;
        }

        InputStream stream = new ByteArrayInputStream(payload.getBytes(DEFAULT_CHARSET));
        try (InputStreamContentProvider inputStreamContentProvider = new InputStreamContentProvider(stream)) {
            request.content(inputStreamContentProvider, MediaType.APPLICATION_JSON);
        }
        logger.trace(" -with payload : {} ", payload);
    }

    private void handleBearer(Request request, @Nullable String bearer) {
        if (bearer == null) {
            return;
        }
        request.header(HttpHeader.AUTHORIZATION, "Bearer %s".formatted(bearer));
    }

    @Override
    public @Nullable Bridge getBridge() {
        return super.getBridge();
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Map<String, ScheduledFuture<?>> getJobs() {
        return jobs;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("This thing does not handle commands");
    }

    public @Nullable IndexProperties getAtmoIndex(String codeInsee) {
        URI atmoUri = AtmoFranceApi.getAtmoUri(LocalDate.now(), codeInsee);
        try {
            AtmoResponse response = executeUri(atmoUri, AtmoResponse.class);
            List<Feature<IndexProperties>> features = response.features();
            if (!features.isEmpty()) {
                return features.getFirst().properties();
            }
        } catch (AtmoFranceException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        return null;
    }

    public @Nullable PollensProperties getPollens(String codeInsee) {
        URI atmoUri = AtmoFranceApi.getPollensUri(LocalDate.now(), codeInsee);
        try {
            PollensResponse response = executeUri(atmoUri, PollensResponse.class);
            List<Feature<PollensProperties>> features = response.features();
            if (!features.isEmpty()) {
                return features.getFirst().properties();
            }
        } catch (AtmoFranceException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        return null;
    }
}
