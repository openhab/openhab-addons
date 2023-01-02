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

import static java.util.Comparator.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import javax.ws.rs.core.UriBuilder;

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
import org.openhab.binding.netatmo.internal.api.AircareApi;
import org.openhab.binding.netatmo.internal.api.ApiError;
import org.openhab.binding.netatmo.internal.api.AuthenticationApi;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.ListBodyResponse;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.RestManager;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.Scope;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.ServiceError;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.api.dto.NAModule;
import org.openhab.binding.netatmo.internal.config.ApiHandlerConfiguration;
import org.openhab.binding.netatmo.internal.config.BindingConfiguration;
import org.openhab.binding.netatmo.internal.config.ConfigurationLevel;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.binding.netatmo.internal.discovery.NetatmoDiscoveryService;
import org.openhab.binding.netatmo.internal.servlet.GrantServlet;
import org.openhab.binding.netatmo.internal.servlet.WebhookServlet;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
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
    private final AuthenticationApi connectApi;
    private final HttpClient httpClient;
    private final NADeserializer deserializer;
    private final HttpService httpService;

    private Optional<ScheduledFuture<?>> connectJob = Optional.empty();
    private Map<Class<? extends RestManager>, RestManager> managers = new HashMap<>();
    private @Nullable WebhookServlet webHookServlet;
    private @Nullable GrantServlet grantServlet;
    private Deque<LocalDateTime> requestsTimestamps;
    private final ChannelUID requestCountChannelUID;

    public ApiBridgeHandler(Bridge bridge, HttpClient httpClient, NADeserializer deserializer,
            BindingConfiguration configuration, HttpService httpService) {
        super(bridge);
        this.bindingConf = configuration;
        this.connectApi = new AuthenticationApi(this, scheduler);
        this.httpClient = httpClient;
        this.deserializer = deserializer;
        this.httpService = httpService;
        this.requestsTimestamps = new ArrayDeque<>(200);
        this.requestCountChannelUID = new ChannelUID(getThing().getUID(), GROUP_MONITORING, CHANNEL_REQUEST_COUNT);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Netatmo API bridge handler.");
        updateStatus(ThingStatus.UNKNOWN);
        GrantServlet servlet = new GrantServlet(this, httpService);
        servlet.startListening();
        grantServlet = servlet;
        scheduler.execute(() -> openConnection(null, null));
    }

    public void openConnection(@Nullable String code, @Nullable String redirectUri) {
        ApiHandlerConfiguration configuration = getConfiguration();
        ConfigurationLevel level = configuration.check();
        switch (level) {
            case EMPTY_CLIENT_ID:
            case EMPTY_CLIENT_SECRET:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, level.message);
                break;
            case REFRESH_TOKEN_NEEDED:
                if (code == null || redirectUri == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, level.message);
                    break;
                } // else we can proceed to get the token refresh
            case COMPLETED:
                try {
                    logger.debug("Connecting to Netatmo API.");

                    String refreshToken = connectApi.authorize(configuration, code, redirectUri);

                    if (configuration.refreshToken.isBlank()) {
                        Configuration thingConfig = editConfiguration();
                        thingConfig.put(ApiHandlerConfiguration.REFRESH_TOKEN, refreshToken);
                        updateConfiguration(thingConfig);
                        configuration = getConfiguration();
                    }

                    if (!configuration.webHookUrl.isBlank()) {
                        SecurityApi securityApi = getRestManager(SecurityApi.class);
                        if (securityApi != null) {
                            WebhookServlet servlet = new WebhookServlet(this, httpService, deserializer, securityApi,
                                    configuration.webHookUrl, configuration.webHookPostfix);
                            servlet.startListening();
                            this.webHookServlet = servlet;
                        }
                    }

                    updateStatus(ThingStatus.ONLINE);

                    getThing().getThings().stream().filter(Thing::isEnabled).map(Thing::getHandler)
                            .filter(Objects::nonNull).map(CommonInterface.class::cast)
                            .forEach(CommonInterface::expireData);

                } catch (NetatmoException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    prepareReconnection(code, redirectUri);
                }
                break;
        }
    }

    public ApiHandlerConfiguration getConfiguration() {
        return getConfigAs(ApiHandlerConfiguration.class);
    }

    private void prepareReconnection(@Nullable String code, @Nullable String redirectUri) {
        connectApi.disconnect();
        freeConnectJob();
        connectJob = Optional.of(scheduler.schedule(() -> openConnection(code, redirectUri),
                getConfiguration().reconnectInterval, TimeUnit.SECONDS));
    }

    private void freeConnectJob() {
        connectJob.ifPresent(j -> j.cancel(true));
        connectJob = Optional.empty();
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Netatmo API bridge handler.");
        WebhookServlet localWebHook = this.webHookServlet;
        if (localWebHook != null) {
            localWebHook.dispose();
        }
        GrantServlet localGrant = this.grantServlet;
        if (localGrant != null) {
            localGrant.dispose();
        }
        connectApi.dispose();
        freeConnectJob();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Netatmo Bridge is read-only and does not handle commands");
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

            if (isLinked(requestCountChannelUID)) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime oneHourAgo = now.minusHours(1);
                requestsTimestamps.addLast(now);
                while (requestsTimestamps.getFirst().isBefore(oneHourAgo)) {
                    requestsTimestamps.removeFirst();
                }
                updateState(requestCountChannelUID, new DecimalType(requestsTimestamps.size()));
            }
            ContentResponse response = request.send();

            Code statusCode = HttpStatus.getCode(response.getStatus());
            String responseBody = new String(response.getContent(), StandardCharsets.UTF_8);
            logger.trace("executeUri returned : code {} body {}", statusCode, responseBody);

            if (statusCode != Code.OK) {
                try {
                    ApiError error = deserializer.deserialize(ApiError.class, responseBody);
                    throw new NetatmoException(error);
                } catch (NetatmoException e) {
                    logger.debug("Error deserializing payload from error response", e);
                    throw new NetatmoException(statusCode.getMessage());
                }
            }
            return deserializer.deserialize(clazz, responseBody);
        } catch (NetatmoException e) {
            if (e.getStatusCode() == ServiceError.MAXIMUM_USAGE_REACHED) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                prepareReconnection(null, null);
            }
            throw e;
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
            prepareReconnection(null, null);
            throw new NetatmoException(String.format("%s: \"%s\"", e.getClass().getName(), e.getMessage()));
        }
    }

    public void identifyAllModulesAndApplyAction(BiFunction<NAModule, ThingUID, Optional<ThingUID>> action) {
        ThingUID accountUID = getThing().getUID();
        try {
            AircareApi airCareApi = getRestManager(AircareApi.class);
            if (airCareApi != null) { // Search Healthy Home Coaches
                ListBodyResponse<NAMain> body = airCareApi.getHomeCoachData(null).getBody();
                if (body != null) {
                    body.getElements().stream().forEach(homeCoach -> action.apply(homeCoach, accountUID));
                }
            }
            WeatherApi weatherApi = getRestManager(WeatherApi.class);
            if (weatherApi != null) { // Search owned or favorite stations
                weatherApi.getFavoriteAndGuestStationsData().stream().forEach(station -> {
                    if (!station.isReadOnly() || getReadFriends()) {
                        action.apply(station, accountUID).ifPresent(stationUID -> station.getModules().values().stream()
                                .forEach(module -> action.apply(module, stationUID)));
                    }
                });
            }
            HomeApi homeApi = getRestManager(HomeApi.class);
            if (homeApi != null) { // Search those depending from a home that has modules + not only weather modules
                homeApi.getHomesData(null, null).stream()
                        .filter(h -> !(h.getFeatures().isEmpty()
                                || h.getFeatures().contains(FeatureArea.WEATHER) && h.getFeatures().size() == 1))
                        .forEach(home -> {
                            action.apply(home, accountUID).ifPresent(homeUID -> {
                                home.getKnownPersons().forEach(person -> action.apply(person, homeUID));

                                Map<String, ThingUID> bridgesUids = new HashMap<>();

                                home.getRooms().values().stream().forEach(room -> {
                                    room.getModuleIds().stream().map(id -> home.getModules().get(id))
                                            .map(m -> m != null ? m.getType().feature : FeatureArea.NONE)
                                            .filter(f -> FeatureArea.ENERGY.equals(f)).findAny().ifPresent(f -> {
                                                action.apply(room, homeUID)
                                                        .ifPresent(roomUID -> bridgesUids.put(room.getId(), roomUID));
                                            });
                                });

                                // Creating modules that have no bridge first, avoiding weather station itself
                                home.getModules().values().stream()
                                        .filter(module -> module.getType().feature != FeatureArea.WEATHER)
                                        .sorted(comparing(HomeDataModule::getBridge, nullsFirst(naturalOrder())))
                                        .forEach(module -> {
                                            String bridgeId = module.getBridge();
                                            if (bridgeId == null) {
                                                action.apply(module, homeUID).ifPresent(
                                                        moduleUID -> bridgesUids.put(module.getId(), moduleUID));
                                            } else {
                                                action.apply(module, bridgesUids.getOrDefault(bridgeId, homeUID));
                                            }
                                        });
                            });
                        });
            }
        } catch (NetatmoException e) {
            logger.warn("Error while identifying all modules : {}", e.getMessage());
        }
    }

    public boolean getReadFriends() {
        return bindingConf.readFriends;
    }

    public boolean isConnected() {
        return connectApi.isConnected();
    }

    public String getId() {
        return (String) getThing().getConfiguration().get(ApiHandlerConfiguration.CLIENT_ID);
    }

    public UriBuilder formatAuthorizationUrl() {
        return AuthenticationApi.getAuthorizationBuilder(getId());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(NetatmoDiscoveryService.class);
    }

    public Optional<WebhookServlet> getWebHookServlet() {
        return Optional.ofNullable(webHookServlet);
    }
}
