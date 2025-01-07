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
package org.openhab.binding.vesync.internal.handlers;

import static org.openhab.binding.vesync.internal.VeSyncConstants.*;
import static org.openhab.binding.vesync.internal.dto.requests.VeSyncProtocolConstants.*;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vesync.internal.VeSyncBridgeConfiguration;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestManagedDeviceBypassV2;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncV2BypassEnergyHistory;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncV2BypassEnergyHistory.EnergyHistory.Result.EnergyInfo;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncV2BypassOutletStatus;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
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
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeSyncDeviceOutletHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class VeSyncDeviceOutletHandler extends VeSyncBaseDeviceHandler {

    public static final String DEV_TYPE_FAMILY_OUTLET = "OUT";
    public static final int DEFAULT_OUTLET_POLL_RATE = 60;
    public static final String DEV_FAMILY_CORE_WHOG_PLUG = "WHOG";
    public static final VeSyncDeviceMetadata COREWHOPGPLUG = new VeSyncDeviceMetadata(DEV_FAMILY_CORE_WHOG_PLUG,
            Arrays.asList("WHOG"), List.of("WHOGPLUG"));
    public static final List<VeSyncDeviceMetadata> SUPPORTED_MODEL_FAMILIES = Arrays.asList(COREWHOPGPLUG);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_OUTLET);
    private final Logger logger = LoggerFactory.getLogger(VeSyncDeviceOutletHandler.class);
    private final Object pollLock = new Object();

    public VeSyncDeviceOutletHandler(Thing thing, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
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
                if (channelUID.getId().equals(DEVICE_CHANNEL_ENABLED)) {
                    sendV2BypassControlCommand(DEVICE_SET_SWITCH,
                            new VeSyncRequestManagedDeviceBypassV2.SetSwitchPayload(command.equals(OnOffType.ON), 0));
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
            pollRate = (Integer) DEFAULT_OUTLET_POLL_RATE;
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
        String responseStatus = EMPTY_STRING;
        String responseEnergyHistory = EMPTY_STRING;
        String responses;
        VeSyncV2BypassOutletStatus outletStatus;
        VeSyncV2BypassEnergyHistory energyHistory;
        synchronized (pollLock) {
            responses = cachedResponse.getValue();
            boolean cachedDataUsed = responses != null;
            if (responses == null) {
                logger.trace("Requesting fresh response");
                responseStatus = sendV2BypassCommand(DEVICE_GET_OUTLET_STATUS,
                        new VeSyncRequestManagedDeviceBypassV2.EmptyPayload());

                try {
                    long end = getTimestampForToday();
                    long start = end - SECONDS_IN_MONTH; // 30 days
                    responseEnergyHistory = sendV2BypassCommand(DEVICE_GET_ENEGERGY_HISTORY,
                            new VeSyncRequestManagedDeviceBypassV2.GetEnergyHistory(start, end));
                } catch (ParseException e) {
                    logger.error("Could not parse timestamp: {}", e.getMessage());
                }
            } else {
                logger.trace("Using cached response {}", responses);
                if (responses.contains("?")) {
                    String[] responseStrings = responses.split("/?");
                    responseStatus = responseStrings[0];
                    responseEnergyHistory = responseStrings[1];
                }
            }

            if (responseStatus.equals(EMPTY_STRING) || responseEnergyHistory.equals(EMPTY_STRING)) {
                return;
            }

            outletStatus = VeSyncConstants.GSON.fromJson(responseStatus, VeSyncV2BypassOutletStatus.class);
            energyHistory = VeSyncConstants.GSON.fromJson(responseEnergyHistory, VeSyncV2BypassEnergyHistory.class);

            if (outletStatus == null || energyHistory == null) {
                return;
            }
            if (!cachedDataUsed) {
                cachedResponse.putValue(responseStatus + "?" + responseEnergyHistory);
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
            logger.warn("{}", getLocalizedText("warning.device.unexpected-resp-for-wifi-outlet"));
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
        updateState(DEVICE_CHANNEL_ENERGY_WEEK,
                new QuantityType<>(getEnergy(energyHistory, 7), MetricPrefix.KILO(Units.WATT_HOUR)));
        updateState(DEVICE_CHANNEL_ENERGY_MONTH,
                new QuantityType<>(getEnergy(energyHistory, 30), MetricPrefix.KILO(Units.WATT_HOUR)));
    }

    private static long getTimestampForToday() throws ParseException {
        Instant instant = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return instant.toEpochMilli() / 1000;
    }

    private static double getEnergy(VeSyncV2BypassEnergyHistory energyHistory, int days) {
        List<EnergyInfo> energyList = energyHistory.result.result.energyInfos;
        double energy = 0;
        for (byte i = 0; i < days; i++) {
            energy += energyList.get(i).energy;
        }
        return energy;
    }

    @Override
    public List<VeSyncDeviceMetadata> getSupportedDeviceMetadata() {
        return SUPPORTED_MODEL_FAMILIES;
    }
}
