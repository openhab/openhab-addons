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
import org.openhab.binding.mikrotik.internal.config.DeviceConfig;
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
 * The {@link MikrotikDeviceHandler} Handles any commands for devices that have a MAC address connected to a Mikrotik
 * bridge device.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class MikrotikDeviceHandler extends MikrotikBaseThingHandler<DeviceConfig> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private @Nullable ScheduledFuture<?> connectingJob;
    private DeviceConfig config = new DeviceConfig();
    private List<Map<String, String>> devices = List.of();

    public MikrotikDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(DeviceConfig.class);
        connectingJob = scheduler.scheduleWithFixedDelay(this::connect, 1, 30, TimeUnit.SECONDS);
    }

    private void connect() {
        refreshModels();
        if (getDevicesValue(devices, "mac-address").isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "MAC Address was not found on Mikrotik Bridge, check correct MAC is entered in things config");
        } else {
            cancelConnectingJob();
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public void refresh(List<Map<String, String>> devices) {
        this.devices = devices;
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
        devices = routeros.getDeviceCache();
    }

    private String getDevicesValue(List<Map<String, String>> devices, String key) {
        for (Map<String, String> device : devices) {
            String mac = device.get("mac-address");
            if (mac != null && mac.contentEquals(config.mac)) {
                String value = device.get(key);
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return "";
    }

    private int getDevicesIndex(List<Map<String, String>> devices) {
        int index = 0;
        for (Map<String, String> device : devices) {
            String mac = device.get("mac-address");
            if (mac != null && mac.contentEquals(config.mac)) {
                return index;
            }
            index++;
        }
        return -1;
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
            refreshChannel(channelUID);
            return;
        }
        switch (channelUID.getId()) {
            case CHANNEL_ENABLED:
                if (command == OnOffType.ON) {
                    routeros.setDeviceEnabled(getDevicesIndex(devices), true);
                } else if (command == OnOffType.OFF) {
                    routeros.setDeviceEnabled(getDevicesIndex(devices), false);
                }
                break;
            default:
                logger.debug("Ignoring unsupported command = {} for channel = {}", command, channelUID);
        }
    }

    @Override
    protected void refreshChannel(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_ENABLED:
                String disabled = getDevicesValue(devices, "disabled");
                if ("false".contentEquals(disabled)) {
                    updateState(CHANNEL_ENABLED, OnOffType.ON);
                } else {
                    updateState(CHANNEL_ENABLED, OnOffType.OFF);
                }
                break;
            case CHANNEL_RX_DATA_RATE:
                String rxRate = getDevicesValue(devices, "rate-down");
                updateState(CHANNEL_RX_DATA_RATE, rxRate.isEmpty() ? org.openhab.core.types.UnDefType.UNDEF
                        : new QuantityType<>(new BigInteger(rxRate), Units.BIT_PER_SECOND));
                break;
            case CHANNEL_TX_DATA_RATE:
                BigInteger txTotalBps = new BigInteger(getDevicesValue(devices, "rate-up"));
                updateState(CHANNEL_TX_DATA_RATE, new QuantityType<>(txTotalBps, Units.BIT_PER_SECOND));
                break;
            default:
                logger.debug("Failed when trying to refresh an unsupported kid control channel {}", channelUID);
        }
    }

    @Override
    protected void executeCommand(ChannelUID channelUID, Command command) {
    }

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return MikrotikBindingConstants.THING_TYPE_DEVICE.equals(thingTypeUID);
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
        super.dispose();
    }
}
