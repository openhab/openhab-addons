/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.touchwand.internal;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TouchWandSwitchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Roie Geron - Initial contribution
 *
 */
@NonNullByDefault
public class TouchWandSwitchHandler extends BaseThingHandler {

    public TouchWandSwitchHandler(Thing thing) {
        super(thing);
    }

    @NonNullByDefault({})
    private String unitId;

    private final Logger logger = LoggerFactory.getLogger(TouchWandSwitchHandler.class);

    private final static int INITIAL_UPDATE_TIME = 10;

    private @Nullable ScheduledFuture<?> pollingJob;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_SWITCH));

    @NonNullByDefault({})
    private TouchWandBridgeHandler bridgeHandler;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        String channelId = channelUID.getId();
        if (CHANNEL_SWITCH.equals(channelId)) {
            if (command instanceof RefreshType) {
                updateState(CHANNEL_SWITCH, getUnitState(unitId));
            } else if (command instanceof OnOffType) {
                bridgeHandler.touchWandClient.cmdSwitchOnOff(unitId, (OnOffType) command);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
    }

    @Override
    public void initialize() {

        ThingStatus bridgeStatus;

        logger.debug("Initializing TocuhWand Switch handler");

        Bridge bridge = getBridge();

        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.warn("Trying to initialize {} without a bridge", getThing().getUID());
            return;
        } else {
            bridgeHandler = (TouchWandBridgeHandler) bridge.getHandler();
            bridgeStatus = bridge.getStatus();
        }

        updateStatus(ThingStatus.UNKNOWN);

        if (!bridgeStatus.equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }

        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);

        Thing thing = getThing();
        Map<String, String> properties = thing.getProperties();
        unitId = properties.get("id");

        scheduler.execute(() -> {
            boolean thingReachable = false;
            String response = bridgeHandler.touchWandClient.cmdGetUnitById(unitId);
            thingReachable = !(response == null);
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
                int statusRefreshRate = bridgeHandler.getStatusRefreshTime();
                pollingJob = scheduler.scheduleWithFixedDelay(runnable, INITIAL_UPDATE_TIME, statusRefreshRate,
                        TimeUnit.SECONDS);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        });

        logger.debug("Finished initializing!");
    }

    private OnOffType getUnitState(String unitId) {

        OnOffType state = OnOffType.OFF;

        if (bridgeHandler == null) {
            return state;
        }

        String response = bridgeHandler.touchWandClient.cmdGetUnitById(unitId);
        if (response == null) {
            return state;
        }

        JsonParser jsonParser = new JsonParser();
        try {
            JsonObject unitObj = jsonParser.parse(response).getAsJsonObject();
            String status = unitObj.get("currStatus").getAsString();
            if (status.equals(SWITCH_STATUS_ON)) {
                state = OnOffType.ON;
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.warn("Could not parse cmdGetUnitById response {}", getThing().getLabel());
        }

        return state;

    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateState(CHANNEL_SWITCH, getUnitState(unitId));
        }
    };

}
