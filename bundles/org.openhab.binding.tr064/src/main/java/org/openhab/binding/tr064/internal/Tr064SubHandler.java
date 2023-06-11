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
package org.openhab.binding.tr064.internal;

import static org.openhab.binding.tr064.internal.Tr064BindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tr064.internal.config.Tr064ChannelConfig;
import org.openhab.binding.tr064.internal.config.Tr064SubConfiguration;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDDeviceType;
import org.openhab.binding.tr064.internal.soap.SOAPConnector;
import org.openhab.binding.tr064.internal.util.SCPDUtil;
import org.openhab.binding.tr064.internal.util.Util;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Tr064SubHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Tr064SubHandler extends BaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SUBDEVICE,
            THING_TYPE_SUBDEVICE_LAN);
    private static final int RETRY_INTERVAL = 60;

    private final Logger logger = LoggerFactory.getLogger(Tr064SubHandler.class);

    private Tr064SubConfiguration config = new Tr064SubConfiguration();

    private String deviceType = "";
    private boolean isInitialized = false;

    private final Map<ChannelUID, Tr064ChannelConfig> channels = new HashMap<>();
    // caching is used to prevent excessive calls to the same action
    private final ExpiringCacheMap<ChannelUID, State> stateCache = new ExpiringCacheMap<>(2000);

    private @Nullable SOAPConnector soapConnector;
    private @Nullable ScheduledFuture<?> connectFuture;
    private @Nullable ScheduledFuture<?> pollFuture;

    Tr064SubHandler(Thing thing) {
        super(thing);
    }

    @Override
    @SuppressWarnings("null")
    public void handleCommand(ChannelUID channelUID, Command command) {
        Tr064ChannelConfig channelConfig = channels.get(channelUID);
        if (channelConfig == null) {
            logger.trace("Channel {} not supported.", channelUID);
            return;
        }

        if (command instanceof RefreshType) {
            State state = stateCache.putIfAbsentAndGet(channelUID, () -> soapConnector == null ? UnDefType.UNDEF
                    : soapConnector.getChannelStateFromDevice(channelConfig, channels, stateCache));
            if (state != null) {
                updateState(channelUID, state);
            }
            return;
        }

        if (channelConfig.getChannelTypeDescription().getSetAction() == null) {
            logger.debug("Discarding command {} to {}, read-only channel", command, channelUID);
            return;
        }
        scheduler.execute(() -> {
            if (soapConnector == null) {
                logger.warn("Could not send command because connector not available");
            } else {
                soapConnector.sendChannelCommandToDevice(channelConfig, command);
            }
        });
    }

    @Override
    public void initialize() {
        config = getConfigAs(Tr064SubConfiguration.class);
        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "One or more mandatory configuration fields are empty");
            return;
        }

        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.UNKNOWN);
            connectFuture = scheduler.scheduleWithFixedDelay(this::internalInitialize, 0, 30, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private void internalInitialize() {
        final Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }
        final Tr064RootHandler bridgeHandler = (Tr064RootHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            logger.warn("Bridge-handler is null in thing {}", thing.getUID());
            return;
        }
        final SCPDUtil scpdUtil = bridgeHandler.getSCPDUtil();
        if (scpdUtil == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not get device definitions");
            return;
        }

        if (checkProperties(scpdUtil)) {
            // properties set, check channels
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withoutChannels(thing.getChannels());
            Util.checkAvailableChannels(thing, thingBuilder, scpdUtil, config.uuid, deviceType, channels);
            updateThing(thingBuilder.build());

            // remove connect scheduler
            removeConnectScheduler();
            soapConnector = bridgeHandler.getSOAPConnector();

            isInitialized = true;
            installPolling();
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        }
    }

    private void removeConnectScheduler() {
        final ScheduledFuture<?> connectFuture = this.connectFuture;
        if (connectFuture != null) {
            connectFuture.cancel(true);
            this.connectFuture = null;
        }
    }

    @Override
    public void dispose() {
        removeConnectScheduler();
        uninstallPolling();

        stateCache.clear();
        isInitialized = false;

        super.dispose();
    }

    /**
     * poll remote device for channel values
     */
    private void poll() {
        SOAPConnector soapConnector = this.soapConnector;
        channels.forEach((channelUID, channelConfig) -> {
            if (isLinked(channelUID)) {
                State state = stateCache.putIfAbsentAndGet(channelUID, () -> soapConnector == null ? UnDefType.UNDEF
                        : soapConnector.getChannelStateFromDevice(channelConfig, channels, stateCache));
                if (state != null) {
                    updateState(channelUID, state);
                }
            }
        });
    }

    /**
     * get device properties from remote device
     *
     * @param scpdUtil the SCPD util of this device
     * @return true if successfull
     */
    private boolean checkProperties(SCPDUtil scpdUtil) {
        try {
            SCPDDeviceType device = scpdUtil.getDevice(config.uuid)
                    .orElseThrow(() -> new SCPDException("Could not find device " + config.uuid));
            String deviceType = device.getDeviceType();
            if (deviceType == null) {
                throw new SCPDException("deviceType can't be null ");
            }
            this.deviceType = deviceType;

            Map<String, String> properties = editProperties();
            properties.put("deviceType", deviceType);
            updateProperties(properties);

            return true;
        } catch (SCPDException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Failed to update device properties: " + e.getMessage());

            return false;
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (!bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            removeConnectScheduler();
        } else {
            if (isInitialized) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.UNKNOWN);
                connectFuture = scheduler.scheduleWithFixedDelay(this::internalInitialize, 0, RETRY_INTERVAL,
                        TimeUnit.SECONDS);
            }
        }
    }

    /**
     * uninstall update polling
     */
    private void uninstallPolling() {
        final ScheduledFuture<?> pollFuture = this.pollFuture;
        if (pollFuture != null) {
            pollFuture.cancel(true);
            this.pollFuture = null;
        }
    }

    /**
     * install update polling
     */
    private void installPolling() {
        uninstallPolling();
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, config.refresh, TimeUnit.SECONDS);
    }
}
