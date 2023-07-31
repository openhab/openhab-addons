/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.vesync.internal.handlers;

import static org.openhab.binding.vesync.internal.VeSyncConstants.DEVICE_CHANNEL_CURRENT;
import static org.openhab.binding.vesync.internal.VeSyncConstants.DEVICE_CHANNEL_ENABLED;
import static org.openhab.binding.vesync.internal.VeSyncConstants.DEVICE_CHANNEL_ENERGY;
import static org.openhab.binding.vesync.internal.VeSyncConstants.DEVICE_CHANNEL_HIGHEST_VOLTAGE;
import static org.openhab.binding.vesync.internal.VeSyncConstants.DEVICE_CHANNEL_POWER;
import static org.openhab.binding.vesync.internal.VeSyncConstants.DEVICE_CHANNEL_VOLTAGE;
import static org.openhab.binding.vesync.internal.VeSyncConstants.DEVICE_CHANNEL_VOLTAGE_PT_STATUS;
import static org.openhab.binding.vesync.internal.VeSyncConstants.DEVICE_PROP_DEVICE_FAMILY;
import static org.openhab.binding.vesync.internal.VeSyncConstants.EMPTY_STRING;
import static org.openhab.binding.vesync.internal.VeSyncConstants.THING_TYPE_OUTLET;
import static org.openhab.binding.vesync.internal.dto.requests.VeSyncProtocolConstants.DEVICE_GET_OUTLET_STATUS;
import static org.openhab.binding.vesync.internal.dto.requests.VeSyncProtocolConstants.DEVICE_SET_SWITCH;
import static org.openhab.binding.vesync.internal.dto.requests.VeSyncProtocolConstants.MODE_ON;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vesync.internal.VeSyncBridgeConfiguration;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestManagedDeviceBypassV2;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncV2BypassOutletStatus;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeSyncDeviceOutletHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Goerentz - Add outlets to the supported devices
 */
@NonNullByDefault
public class VeSyncDeviceOutletHandler extends VeSyncBaseDeviceHandler {

    public static final String DEV_TYPE_FAMILY_OUTLET = "OUT";

    public static final int DEFAULT_OUTLET_POLL_RATE = 60;

    public static final String DEV_FAMILY_CORE_WHOG_PLUG = "WHOG";
    public static final String DEV_FAMILY_CORE_ESW = "ESW";
    public static final String DEV_FAMILY_CORE_ESWL = "ESWL";

    public static final VeSyncDeviceMetadata COREESW = new VeSyncDeviceMetadata(DEV_FAMILY_CORE_ESW,
            Collections.emptyList(), Arrays.asList("ESW03-USA", "ESW01-EU", "ESW15-USA"));

    public static final VeSyncDeviceMetadata COREESWL = new VeSyncDeviceMetadata(DEV_FAMILY_CORE_ESWL,
            Collections.emptyList(), Arrays.asList("ESWL01", "ESWL03"));

    public static final VeSyncDeviceMetadata COREWHOPGPLUG = new VeSyncDeviceMetadata(DEV_FAMILY_CORE_WHOG_PLUG,
            Arrays.asList("WHOG"), List.of("WHOGPLUG"));

    public static final List<VeSyncDeviceMetadata> SUPPORTED_MODEL_FAMILIES = Arrays.asList(COREESW, COREESWL,
            COREWHOPGPLUG);

    private final Logger logger = LoggerFactory.getLogger(VeSyncDeviceOutletHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_OUTLET);

    private final Object pollLock = new Object();

    public VeSyncDeviceOutletHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        customiseChannels();
    }

    @Override
    public String getDeviceFamilyProtocolPrefix() {
        return DEV_TYPE_FAMILY_OUTLET;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final String deviceFamily = getThing().getProperties().get(DEVICE_PROP_DEVICE_FAMILY);
        if (deviceFamily == null) {
            return;
        }

        scheduler.submit(() -> {
            if (command instanceof OnOffType) {
                switch (channelUID.getId()) {
                    case DEVICE_CHANNEL_ENABLED:
                        sendV2BypassControlCommand(DEVICE_SET_SWITCH,
                                new VeSyncRequestManagedDeviceBypassV2.SetSwitchPayload(command.equals(OnOffType.ON),
                                        0));
                        break;
                }
            } else if (command instanceof RefreshType) {
                pollForUpdate();
            } else {
                logger.trace("UNKNOWN COMMAND: {} {}", command.getClass().toString(), channelUID);
            }
        });
    }

    @Override
    public void updateBridgeBasedPolls(final VeSyncBridgeConfiguration config) {
        Integer pollRate = config.outletPollInterval;
        if (pollRate == null) {
            pollRate = DEFAULT_OUTLET_POLL_RATE;
        }
        if (ThingStatus.OFFLINE.equals(getThing().getStatus())) {
            setBackgroundPollInterval(-1);
        } else {
            setBackgroundPollInterval(pollRate);
        }
    }

    @Override
    protected void pollForDeviceData(ExpiringCache<String> cachedResponse) {
        processV2BypassPoll(cachedResponse);
    }

    private void processV2BypassPoll(final ExpiringCache<String> cachedResponse) {
        String response;
        VeSyncV2BypassOutletStatus outletStatus;
        synchronized (pollLock) {
            response = cachedResponse.getValue();
            boolean cachedDataUsed = response != null;
            if (response == null) {
                logger.trace("Requesting fresh response");
                response = sendV2BypassCommand(DEVICE_GET_OUTLET_STATUS,
                        new VeSyncRequestManagedDeviceBypassV2.EmptyPayload());
            } else {
                logger.trace("Using cached response {}", response);
            }

            if (response.equals(EMPTY_STRING)) {
                return;
            }

            outletStatus = VeSyncConstants.GSON.fromJson(response, VeSyncV2BypassOutletStatus.class);

            if (outletStatus == null) {
                return;
            }

            if (!cachedDataUsed) {
                cachedResponse.putValue(response);
            }
        }

        // Bail and update the status of the thing - it will be updated to online by the next search
        // that detects it is online.
        if (outletStatus.isMsgDeviceOffline()) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        } else if (outletStatus.isMsgSuccess()) {
            updateStatus(ThingStatus.ONLINE);
        }

        if (!"0".equals(outletStatus.getCode())) {
            logger.warn("Check Thing type has been set - API gave a unexpected response for an Air Purifier");
            return;
        }

        updateState(DEVICE_CHANNEL_ENABLED,
                OnOffType.from(MODE_ON.equals(outletStatus.outletResult.result.getDeviceStatus())));
        updateState(DEVICE_CHANNEL_CURRENT, new QuantityType<>(outletStatus.outletResult.result.current, Units.AMPERE));
        updateState(DEVICE_CHANNEL_VOLTAGE, new QuantityType<>(outletStatus.outletResult.result.voltage, Units.VOLT));
        updateState(DEVICE_CHANNEL_ENERGY, new QuantityType<>(outletStatus.outletResult.result.energy, Units.WATT));
        updateState(DEVICE_CHANNEL_POWER,
                new QuantityType<>(outletStatus.outletResult.result.power, MetricPrefix.KILO(Units.WATT_HOUR)));
        updateState(DEVICE_CHANNEL_HIGHEST_VOLTAGE,
                new QuantityType<>(outletStatus.outletResult.result.highestVoltage, Units.VOLT));
        updateState(DEVICE_CHANNEL_VOLTAGE_PT_STATUS, OnOffType.from(outletStatus.outletResult.result.voltagePTStatus));
    }

    @Override
    public List<VeSyncDeviceMetadata> getSupportedDeviceMetadata() {
        return SUPPORTED_MODEL_FAMILIES;
    }
}
