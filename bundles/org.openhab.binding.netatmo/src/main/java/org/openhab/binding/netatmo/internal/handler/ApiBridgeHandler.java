/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.openhab.binding.netatmo.internal.api.ApiError;
import org.openhab.binding.netatmo.internal.api.AuthenticationApi;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.RestManager;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.Scope;
import org.openhab.binding.netatmo.internal.config.ApiHandlerConfiguration;
import org.openhab.binding.netatmo.internal.config.ApiHandlerConfiguration.Credentials;
import org.openhab.binding.netatmo.internal.config.BindingConfiguration;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.binding.netatmo.internal.discovery.NetatmoDiscoveryService;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ApiBridgeHandler} is the handler for a Netatmo API and connects it to the framework.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class ApiBridgeHandler extends BaseBridgeHandler {
    private static final int TIMEOUT_S = 20;

    private final Logger logger = LoggerFactory.getLogger(ApiBridgeHandler.class);
    private final BindingConfiguration bindingConf;
    private final HttpService httpService;
    private final AuthenticationApi connectApi;
    private final HttpClient httpClient;
    private final NADeserializer deserializer;

    private Optional<ScheduledFuture<?>> connectJob = Optional.empty();
    private Optional<NetatmoServlet> servlet = Optional.empty();
    private @NonNullByDefault({}) ApiHandlerConfiguration thingConf;

    private Map<Class<? extends RestManager>, RestManager> managers = new HashMap<>();

    public ApiBridgeHandler(Bridge bridge, HttpClient httpClient, HttpService httpService, NADeserializer deserializer,
            BindingConfiguration configuration) {
        super(bridge);
        this.bindingConf = configuration;
        this.httpService = httpService;
        this.connectApi = new AuthenticationApi(this, scheduler);
        this.httpClient = httpClient;
        this.deserializer = deserializer;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Netatmo API bridge handler.");
        thingConf = getConfigAs(ApiHandlerConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            openConnection();
            String webHookUrl = thingConf.webHookUrl;
            if (webHookUrl != null && !webHookUrl.isBlank()) {
                servlet = Optional.of(new NetatmoServlet(httpService, this, webHookUrl));
            }
        });
    }

    private void openConnection() {
        try {
            Credentials credentials = thingConf.getCredentials();
            logger.debug("Connecting to Netatmo API.");
            try {
                connectApi.authenticate(credentials, bindingConf.features);
                updateStatus(ThingStatus.ONLINE);
                getThing().getThings().stream().filter(Thing::isEnabled).map(Thing::getHandler).filter(Objects::nonNull)
                        .map(CommonInterface.class::cast).forEach(CommonInterface::expireData);
            } catch (NetatmoException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                prepareReconnection();
            }
        } catch (NetatmoException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    private void prepareReconnection() {
        connectApi.disconnect();
        freeConnectJob();
        connectJob = Optional
                .of(scheduler.schedule(() -> openConnection(), thingConf.reconnectInterval, TimeUnit.SECONDS));
    }

    private void freeConnectJob() {
        connectJob.ifPresent(j -> j.cancel(true));
        connectJob = Optional.empty();
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Netatmo API bridge handler.");
        servlet.ifPresent(servlet -> servlet.dispose());
        servlet = Optional.empty();
        connectApi.dispose();
        freeConnectJob();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Netatmo Bridge is read-only and does not handle commands");
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(NetatmoDiscoveryService.class);
    }

    @SuppressWarnings("unchecked")
    public <T extends RestManager> @Nullable T getRestManager(Class<T> clazz) {
        if (!managers.containsKey(clazz)) {
            try {
                Constructor<T> constructor = clazz.getConstructor(getClass());
                T instance = constructor.newInstance(this);
                Set<Scope> expected = instance.getRequiredScopes();
                if (connectApi.matchesScopes(expected)) {
                    managers.put(clazz, instance);
                } else {
                    logger.info("Unable to instantiate {}, expected scope {} is not active", clazz, expected);
                }
            } catch (SecurityException | ReflectiveOperationException e) {
                logger.warn("Error invoking RestManager constructor for class {} : {}", clazz, e.getMessage());
            }
        }
        return (T) managers.get(clazz);
    }

    public synchronized <T> T executeUri(URI uri, HttpMethod method, Class<T> clazz, @Nullable String payload,
            @Nullable String contentType, int retryCount) throws NetatmoException {
        try {
            logger.trace("executeUri {}  {} ", method.toString(), uri);

            Request request = httpClient.newRequest(uri).method(method).timeout(TIMEOUT_S, TimeUnit.SECONDS);

            String auth = connectApi.getAuthorization();
            if (auth != null) {
                request.header(HttpHeader.AUTHORIZATION, auth);
            }

            if (payload != null && contentType != null
                    && (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method))) {
                InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
                try (InputStreamContentProvider inputStreamContentProvider = new InputStreamContentProvider(stream)) {
                    request.content(inputStreamContentProvider, contentType);
                }
            }

            ContentResponse response = request.send();

            Code statusCode = HttpStatus.getCode(response.getStatus());
            String responseBody = new String(response.getContent(), StandardCharsets.UTF_8);
            logger.trace("executeUri returned : code {} body {}", statusCode, responseBody);

            if (statusCode != Code.OK) {
                ApiError error = deserializer.deserialize(ApiError.class, responseBody);
                throw new NetatmoException(error);
            }
            return deserializer.deserialize(clazz, responseBody);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw new NetatmoException(String.format("%s: \"%s\"", e.getClass().getName(), e.getMessage()));
        } catch (TimeoutException | ExecutionException e) {
            if (retryCount > 0) {
                logger.debug("Request timedout, retry counter : {}", retryCount);
                return executeUri(uri, method, clazz, payload, contentType, retryCount - 1);
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/request-time-out");
            prepareReconnection();
            throw new NetatmoException(String.format("%s: \"%s\"", e.getClass().getName(), e.getMessage()));
        }
    }

    public BindingConfiguration getConfiguration() {
        return bindingConf;
    }

    public Optional<NetatmoServlet> getServlet() {
        return servlet;
    }

    public NADeserializer getDeserializer() {
        return deserializer;
    }

    public boolean isConnected() {
        return connectApi.isConnected();
    }
}
