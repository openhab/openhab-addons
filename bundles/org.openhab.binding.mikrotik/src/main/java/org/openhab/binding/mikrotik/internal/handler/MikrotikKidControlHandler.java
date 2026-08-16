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
package org.openhab.binding.mikrotik.internal.handler;

import static org.openhab.binding.mikrotik.internal.MikrotikBindingConstants.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.MikrotikBindingConstants;
import org.openhab.binding.mikrotik.internal.config.KidControlConfig;
import org.openhab.binding.mikrotik.internal.model.RouterosDevice;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MikrotikKidControlHandler} Handles any commands for Kid Controls that are on the bridge
 * device.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class MikrotikKidControlHandler extends MikrotikBaseThingHandler<KidControlConfig> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private @Nullable ScheduledFuture<?> connectingJob;
    private KidControlConfig config = new KidControlConfig();
    private Map<String, String> kid = Map.of();
    private List<Map<String, String>> devices = List.of();

    public MikrotikKidControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(KidControlConfig.class);
        connectingJob = scheduler.scheduleWithFixedDelay(this::connect, 1, 30, TimeUnit.SECONDS);
    }

    private void connect() {
        refreshModels();
        String name = kid.get("name");
        if (name != null && config.name.contentEquals(name)) {
            cancelConnectingJob();
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Name cannot be found on Mikrotik device, check the correct kids name is entered in the things config");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }
        MikrotikRouterosBridgeHandler bridgeHandler = (MikrotikRouterosBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }
        RouterosDevice routeros = bridgeHandler.getRouteros();
        if (routeros == null) {
            return;
        }
        if (command instanceof RefreshType) {
            this.devices = routeros.updateKidControlCache();
            refreshChannel(channelUID);
            return;
        }
        switch (channelUID.getId()) {
            case CHANNEL_ENABLED:
                if (command == OnOffType.ON) {
                    routeros.setKidControlEnabledState(config.name, "no");
                } else if (command == OnOffType.OFF) {
                    routeros.setKidControlEnabledState(config.name, "yes");
                }
                break;
            case CHANNEL_PAUSED:
                if (command == OnOffType.ON) {
                    routeros.setKidControlPausedState(config.name, true);
                } else if (command == OnOffType.OFF) {
                    routeros.setKidControlPausedState(config.name, false);
                }
                break;
            default:
                logger.debug("Ignoring unsupported command = {} for channel = {}", command, channelUID);
        }
    }

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return MikrotikBindingConstants.THING_TYPE_KID_CONTROL.equals(thingTypeUID);
    }

    @Override
    protected void refreshModels() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }
        MikrotikRouterosBridgeHandler bridgeHandler = (MikrotikRouterosBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }
        RouterosDevice routeros = bridgeHandler.getRouteros();
        if (routeros == null) {
            return;
        }
        refresh(routeros.getKidControlCache(), routeros.getDeviceCache());
    }

    private BigInteger calculateTotalRate(List<Map<String, String>> devices, String key) {
        BigInteger totalRate = BigInteger.ZERO;
        for (Map<String, String> device : devices) {
            String user = device.get("user");
            if (user != null && !user.isEmpty()) {
                String rate = device.get(key);
                if (rate != null && !rate.isEmpty()) {
                    totalRate = totalRate.add(new BigInteger(rate));
                }
            }
        }
        return totalRate;
    }

    @Override
    protected void refreshChannel(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_ENABLED:
                String disabled = kid.get("disabled");
                if (disabled != null && "false".contentEquals(disabled)) {
                    updateState(CHANNEL_ENABLED, OnOffType.ON);
                } else {
                    updateState(CHANNEL_ENABLED, OnOffType.OFF);
                }
                break;
            case CHANNEL_PAUSED:
                String paused = kid.get("paused");
                if (paused != null && "true".contentEquals(paused)) {
                    updateState(CHANNEL_PAUSED, OnOffType.ON);
                } else {
                    updateState(CHANNEL_PAUSED, OnOffType.OFF);
                }
                break;
            case CHANNEL_RX_DATA_RATE:
                BigInteger rxTotalBps = calculateTotalRate(devices, "rate-down");
                updateState(CHANNEL_RX_DATA_RATE, new QuantityType<>(rxTotalBps, Units.BIT_PER_SECOND));
                break;
            case CHANNEL_TX_DATA_RATE:
                BigInteger txTotalBps = calculateTotalRate(devices, "rate-up");
                updateState(CHANNEL_TX_DATA_RATE, new QuantityType<>(txTotalBps, Units.BIT_PER_SECOND));
                break;
            default:
                logger.debug("Failed when trying to refresh an unsupported kid control channel {}", channelUID);
        }
    }

    @Override
    protected void executeCommand(ChannelUID channelUID, Command command) {
    }

    public void refresh(List<Map<String, String>> kids, List<Map<String, String>> devices) {
        this.devices = devices;
        for (Map<String, String> kid : kids) {
            if (kid.containsKey("name")) {
                String name = kid.get("name");
                if (name != null && !name.isEmpty()) {
                    this.kid = kid;
                }
            }
        }
    }

    private void cancelConnectingJob() {
        Future<?> future = connectingJob;
        if (future != null) {
            future.cancel(true);
            connectingJob = null;
        }
    }

    @Override
    public void dispose() {
        cancelConnectingJob();
    }
}
