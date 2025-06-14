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
package org.openhab.binding.roborock.internal;

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.Login;
import org.openhab.binding.roborock.internal.api.Login.Rriot;
import org.openhab.binding.roborock.internal.discovery.RoborockVacuumDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link RoborockAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class RoborockAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(RoborockAccountHandler.class);

    private @Nullable RoborockAccountConfiguration config;
    protected ScheduledExecutorService executorService = this.scheduler;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @NonNullByDefault({}) RoborockWebTargets webTargets;
    private String token = "";
    private @Nullable Rriot rriot;

    private final Gson gson = new Gson();

    public RoborockAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        config = getConfigAs(RoborockAccountConfiguration.class);
        webTargets = new RoborockWebTargets(httpClient);
    }

    public String getToken() {
        return token;
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    @Nullable
    public Rriot getRriot() {
        return rriot;
    }

    @Nullable
    public Login doLogin(String email, String password) {
        try {
            return webTargets.doLogin(email, password);
        } catch (RoborockAuthenticationException e) {
            logger.debug("Unexpected authentication error connecting to Roborock API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return new Login();
        } catch (NoSuchAlgorithmException e) {
            logger.debug("Unexpected NoSuchAlgorithmException error connecting to Roborock API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return new Login();
        } catch (RoborockCommunicationException e) {
            logger.debug("Unexpected error connecting to Roborock API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return new Login();
        }
    }

    @Nullable
    public Home getHomeDetail() {
        try {
            return webTargets.getHomeDetail(token);
        } catch (RoborockAuthenticationException e) {
            logger.debug("Unexpected authentication error connecting to Roborock API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return new Home();
        } catch (RoborockCommunicationException e) {
            logger.debug("Unexpected error connecting to Roborock API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return new Home();
        }
    }

    @Nullable
    public HomeData getHomeData(String rrHomeID, @Nullable Rriot rriot) {
        try {
            return webTargets.getHomeData(rrHomeID, rriot);
        } catch (RoborockAuthenticationException | NoSuchAlgorithmException | InvalidKeyException e) {
            logger.debug("Unexpected authentication error connecting to Roborock API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return new HomeData();
        } catch (RoborockCommunicationException e) {
            logger.debug("Unexpected error connecting to Roborock API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return new HomeData();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // we do not have any channels -> nothing to do here
    }

    @Override
    public void initialize() {
        config = getConfigAs(RoborockAccountConfiguration.class);
        if (config.email.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing email address configuration");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        Login loginResponse;
        loginResponse = doLogin(config.email, config.password);
        if (loginResponse != null) {
            token = loginResponse.data.token;
            rriot = loginResponse.data.rriot;
        }
        /*
         * Home home;
         * home = getHomeDetail();
         * if (home != null) {
         * HomeData homeData;
         * homeData = getHomeData(Integer.toString(home.data.rrHomeId), loginResponse.data.rriot);
         * }
         */
        /*
         * String responseVehicleList = getVehicleList();
         * JsonArray jsonArrayVehicleList = JsonParser.parseString(responseVehicleList).getAsJsonArray();
         * VehicleList vehicleList = gson.fromJson(jsonArrayVehicleList.get(0), VehicleList.class);
         * if (vehicleList == null) {
         * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to retrieve Vehicle List");
         * return;
         * }
         */
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(RoborockVacuumDiscoveryService.class);
    }
}
