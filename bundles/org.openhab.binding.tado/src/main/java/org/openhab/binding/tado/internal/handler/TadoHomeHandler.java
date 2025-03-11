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
package org.openhab.binding.tado.internal.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.TadoBindingConstants;
import org.openhab.binding.tado.internal.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.internal.api.HomeApiFactory;
import org.openhab.binding.tado.internal.config.TadoHomeConfig;
import org.openhab.binding.tado.internal.servlet.TadoAuthenticationServlet;
import org.openhab.binding.tado.swagger.codegen.api.ApiException;
import org.openhab.binding.tado.swagger.codegen.api.client.HomeApi;
import org.openhab.binding.tado.swagger.codegen.api.model.HomeInfo;
import org.openhab.binding.tado.swagger.codegen.api.model.HomePresence;
import org.openhab.binding.tado.swagger.codegen.api.model.HomeState;
import org.openhab.binding.tado.swagger.codegen.api.model.PresenceState;
import org.openhab.binding.tado.swagger.codegen.api.model.User;
import org.openhab.binding.tado.swagger.codegen.api.model.UserHomes;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TadoHomeHandler} is the bridge of all home-based things.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
public class TadoHomeHandler extends BaseBridgeHandler implements AccessTokenRefreshListener {

    private static final ZonedDateTime AUTH_V2_FROM_DATE = ZonedDateTime.parse("2025-03-15T00:00:00Z");

    private static final String DEVICE_URL = "https://login.tado.com/oauth2/device_authorize";
    private static final String TOKEN_URL = "https://login.tado.com/oauth2/token";
    private static final String CLIENT_ID = "1bb50063-6b0c-4d11-bd99-387f4a91cc46";
    private static final String SCOPE = "offline_access";

    private final Logger logger = LoggerFactory.getLogger(TadoHomeHandler.class);

    private final TadoBatteryChecker batteryChecker;
    private final HttpService httpService;
    private final TadoAuthenticationServlet httpServlet;
    private final OAuthFactory oAuthFactory;

    private @NonNullByDefault({}) TadoHomeConfig configuration;
    private @NonNullByDefault({}) String offlineMessage;
    private @NonNullByDefault({}) HomeApi api;

    private @Nullable Long homeId;
    private @Nullable ScheduledFuture<?> initializationFuture;
    private @Nullable OAuthClientService oAuthClientService;

    public TadoHomeHandler(Bridge bridge, HttpService httpService, OAuthFactory oAuthFactory) {
        super(bridge);
        this.batteryChecker = new TadoBatteryChecker(this);
        this.configuration = getConfigAs(TadoHomeConfig.class);
        this.httpService = httpService;
        this.httpServlet = new TadoAuthenticationServlet(this);
        this.oAuthFactory = oAuthFactory;
    }

    public TemperatureUnit getTemperatureUnit() {
        String temperatureUnitStr = this.thing.getProperties()
                .getOrDefault(TadoBindingConstants.PROPERTY_HOME_TEMPERATURE_UNIT, "CELSIUS");
        return TemperatureUnit.valueOf(temperatureUnitStr);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        configuration = getConfigAs(TadoHomeConfig.class);

        String userName = configuration.username;
        String password = configuration.password;
        boolean v1CredentialsOk = userName != null && !userName.isBlank() && password != null && !password.isBlank();

        boolean suggestRfc8628 = false;
        suggestRfc8628 |= Boolean.TRUE.equals(configuration.useRfc8628);
        suggestRfc8628 |= !v1CredentialsOk;
        suggestRfc8628 |= ZonedDateTime.now().isAfter(AUTH_V2_FROM_DATE);

        if (suggestRfc8628) {
            String ipAddress;
            try {
                ipAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                ipAddress = "[ip-address]";
            }
            offlineMessage = String.format("Try authenticating at http://%s:8080%s", ipAddress,
                    TadoAuthenticationServlet.PATH);

            OAuthClientService service = oAuthFactory.getOAuthClientService(thing.getUID().toString());
            if (service == null) {
                service = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(), TOKEN_URL, DEVICE_URL,
                        CLIENT_ID, null, SCOPE, false);
            }
            service.addAccessTokenRefreshListener(this);
            oAuthClientService = service;

            api = new HomeApiFactory().create(service);

            logger.trace("initialize() api v2 created");
        } else {
            offlineMessage = "Username and/or password might be invalid";
            api = new HomeApiFactory().create(Objects.requireNonNull(userName), Objects.requireNonNull(password));
            logger.trace("initialize() api v1 created");
        }

        try {
            httpService.registerServlet(TadoAuthenticationServlet.PATH, httpServlet, null, null);
        } catch (ServletException | NamespaceException e) {
            logger.warn("initialize() failed to register servlet", e);
        }

        ScheduledFuture<?> initializationFuture = this.initializationFuture;
        if (initializationFuture == null || initializationFuture.isDone()) {
            this.initializationFuture = scheduler.scheduleWithFixedDelay(
                    this::initializeBridgeStatusAndPropertiesIfOffline, 0, 300, TimeUnit.SECONDS);
        }
    }

    private synchronized void initializeBridgeStatusAndPropertiesIfOffline() {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if ((handler instanceof BaseHomeThingHandler) && (thing.getStatus() == ThingStatus.OFFLINE)
                        && (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR)) {
                    scheduler.submit(() -> handler.bridgeStatusChanged(getThing().getStatusInfo()));
                }
            }
        }

        try {
            // if we are already online, don't make unnecessary calls on the server
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                // Get user info to verify successful authentication and connection to server
                User user = api.showUser();
                if (user == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, offlineMessage);
                    return;
                }

                List<UserHomes> homes = user.getHomes();
                if (homes == null || homes.isEmpty()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "User does not have access to any home");
                    return;
                }

                Integer firstHomeId = homes.get(0).getId();
                if (firstHomeId == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Missing Home Id");
                    return;
                }

                homeId = firstHomeId.longValue();
            }

            // but always make one server call as a 'ping' to confirm we are really still online
            HomeInfo homeInfo = api.showHome(homeId);
            TemperatureUnit temperatureUnit = org.openhab.binding.tado.swagger.codegen.api.model.TemperatureUnit.FAHRENHEIT == homeInfo
                    .getTemperatureUnit() ? TemperatureUnit.FAHRENHEIT : TemperatureUnit.CELSIUS;
            updateProperty(TadoBindingConstants.PROPERTY_HOME_TEMPERATURE_UNIT, temperatureUnit.name());
        } catch (IOException | ApiException e) {
            logger.debug("Error accessing tado server: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, offlineMessage);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        super.dispose();
        OAuthClientService service = oAuthClientService;
        if (service != null) {
            service.removeAccessTokenRefreshListener(this);
        }
        ScheduledFuture<?> initializationFuture = this.initializationFuture;
        if (initializationFuture != null && !initializationFuture.isCancelled()) {
            initializationFuture.cancel(true);
        }
    }

    public HomeApi getApi() {
        return api;
    }

    public @Nullable Long getHomeId() {
        return homeId;
    }

    public HomeState getHomeState() throws IOException, ApiException {
        return api.homeState(getHomeId());
    }

    public void updateHomeState() {
        try {
            updateState(TadoBindingConstants.CHANNEL_HOME_PRESENCE_MODE,
                    OnOffType.from(getHomeState().getPresence() == PresenceState.HOME));
        } catch (IOException | ApiException e) {
            logger.debug("Error accessing tado server: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();

        if (command == RefreshType.REFRESH) {
            updateHomeState();
            return;
        }

        switch (id) {
            case TadoBindingConstants.CHANNEL_HOME_PRESENCE_MODE:
                HomePresence presence = new HomePresence();
                presence.setHomePresence("ON".equals(command.toFullString().toUpperCase())
                        || "HOME".equals(command.toFullString().toUpperCase()) ? PresenceState.HOME
                                : PresenceState.AWAY);
                try {
                    api.updatePresenceLock(homeId, presence);
                } catch (IOException | ApiException e) {
                    logger.warn("Error setting home presence: {}", e.getMessage(), e);
                }

                break;
        }
    }

    public TadoBatteryChecker getBatteryChecker() {
        return this.batteryChecker;
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse atr) {
        initializeBridgeStatusAndPropertiesIfOffline();
    }
}
