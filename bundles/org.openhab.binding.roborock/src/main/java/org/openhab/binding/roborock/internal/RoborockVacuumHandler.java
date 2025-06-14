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

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.Login.Rriot;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link RoborockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class RoborockVacuumHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RoborockVacuumHandler.class);

    @Nullable
    RoborockAccountHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> pollFuture;

    private final Gson gson = new Gson();

    private String token = "";
    private String email = "";
    private @Nullable Rriot rriot;
    private String rrHomeId = "";

    public RoborockVacuumHandler(Thing thing) {
        super(thing);
    }

    protected String getToken() {
        RoborockAccountHandler localBridge = bridgeHandler;
        if (localBridge == null) {
            return "";
        }
        try {
            return localBridge.getToken();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            return "";
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // we do not have any channels -> nothing to do here
    }

    @Override
    public void initialize() {
        if (!(getBridge() instanceof Bridge bridge
                && bridge.getHandler() instanceof RoborockAccountHandler accountHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "No Roborock Bridge selected");
            return;
        }
        bridgeHandler = accountHandler;
        updateStatus(ThingStatus.UNKNOWN);
        token = getToken();
        if (!token.isEmpty()) {
            rriot = bridgeHandler.getRriot();
            Home home;
            home = bridgeHandler.getHomeDetail();
            if (home != null) {
                rrHomeId = Integer.toString(home.data.rrHomeId);
            }
            updateStatus(ThingStatus.ONLINE);
            schedulePoll();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Token empty, can't login");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private void schedulePoll() {
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::pollStatus, 0, 300, TimeUnit.SECONDS);
    }

    private void stopPoll() {
        final Future<?> future = pollFuture;
        if (future != null) {
            future.cancel(true);
            pollFuture = null;
        }
    }

    private void pollStatus() {
        HomeData homeData;
        homeData = bridgeHandler.getHomeData(rrHomeId, rriot);
        if (homeData != null) {
            for (int i = 0; i < homeData.result.devices.length; i++) {
                if (getThing().getUID().getId().equals(homeData.result.devices[i].duid)) {
                    logger.info("Update channels");
                    updateState(RoborockBindingConstants.CHANNEL_ERROR_ID,
                            new DecimalType(homeData.result.devices[i].deviceStatus.errorCode));
                    updateState(RoborockBindingConstants.CHANNEL_STATE,
                            new DecimalType(homeData.result.devices[i].deviceStatus.vacuumState));
                    updateState(RoborockBindingConstants.CHANNEL_BATTERY,
                            new DecimalType(homeData.result.devices[i].deviceStatus.battery));
                    updateState(RoborockBindingConstants.CHANNEL_FAN_POWER,
                            new DecimalType(homeData.result.devices[i].deviceStatus.fanPower));
                    updateState(RoborockBindingConstants.CHANNEL_CONSUMABLE_MAIN_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.mainBrushWorkTime));
                    updateState(RoborockBindingConstants.CHANNEL_CONSUMABLE_SIDE_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.sideBrushWorkTime));
                    updateState(RoborockBindingConstants.CHANNEL_CONSUMABLE_FILTER_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.filterWorkTime));
                }
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }
}
