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
package org.openhab.binding.eyeonwater.internal.handler;

import static org.openhab.binding.eyeonwater.internal.EyeOnWaterBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.eyeonwater.internal.api.EyeOnWaterClient.EyeOnWaterMeterData;
import org.openhab.binding.eyeonwater.internal.config.EyeOnWaterMeterConfiguration;
import org.openhab.binding.eyeonwater.internal.util.EyeOnWaterUnitConverter;
import org.openhab.binding.eyeonwater.internal.util.EyeOnWaterUnitConverter.NormalizedReading;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EyeOnWaterMeterHandler} represents a physical water meter registered in EyeOnWater.
 * It maps values from the REST API to openHAB channels.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
public class EyeOnWaterMeterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EyeOnWaterMeterHandler.class);

    private final TimeZoneProvider timeZoneProvider;

    private String meterUuid = "";
    private String meterId = "";

    public EyeOnWaterMeterHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing EyeOnWater Meter Handler: {}", getThing().getUID());

        EyeOnWaterMeterConfiguration config = getConfigAs(EyeOnWaterMeterConfiguration.class);
        meterUuid = config.meterUuid.trim();
        meterId = config.meterId.trim();

        if (meterUuid.isEmpty() || meterId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.meter-config-missing");
            return;
        }

        @Nullable
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "@text/offline.bridge-undefined");
            return;
        }

        @Nullable
        ThingHandler bridgeHandler = bridge.getHandler();
        if (bridgeHandler instanceof EyeOnWaterBridgeHandler) {
            updateStatus(ThingStatus.UNKNOWN);
            ((EyeOnWaterBridgeHandler) bridgeHandler).registerMeterHandler(this);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "@text/offline.bridge-not-initialized");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing EyeOnWater Meter Handler: {}", getThing().getUID());

        @Nullable
        Bridge bridge = getBridge();
        if (bridge != null) {
            @Nullable
            ThingHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof EyeOnWaterBridgeHandler) {
                ((EyeOnWaterBridgeHandler) bridgeHandler).unregisterMeterHandler(this);
            }
        }

        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refresh requested for channel: {}", channelUID);
            @Nullable
            Bridge bridge = getBridge();
            if (bridge != null) {
                @Nullable
                ThingHandler bridgeHandler = bridge.getHandler();
                if (bridgeHandler instanceof EyeOnWaterBridgeHandler) {
                    ((EyeOnWaterBridgeHandler) bridgeHandler).registerMeterHandler(this);
                }
            }
        }
    }

    public String getMeterUuid() {
        return meterUuid;
    }

    public String getMeterId() {
        return meterId;
    }

    public void updateStatusOffline(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
    }

    /**
     * Called by the bridge handler to update this meter's states.
     */
    public void updateState(EyeOnWaterMeterData data) {
        @Nullable
        String rawUnit = data.getReadingUnit();
        try {
            NormalizedReading normalized = EyeOnWaterUnitConverter.normalize(data.getReadingValue(), rawUnit);
            updateStatus(ThingStatus.ONLINE);

            // Update Reading Channel
            updateState(CHANNEL_READING, new QuantityType<>(normalized.getValue() + " " + normalized.getUnit()));

            // Update Flow Rate if available
            double leakRate = data.getLeakRate();
            if (leakRate >= 0) {
                String flowUnit;
                if ("gal".equals(normalized.getUnit())) {
                    flowUnit = "gal/min";
                } else if ("ft³".equals(normalized.getUnit())) {
                    flowUnit = "ft³/min";
                } else {
                    flowUnit = "m³/h";
                }
                updateState(CHANNEL_LEAK_FLOW_RATE, new QuantityType<>(leakRate + " " + flowUnit));
            }

            // Update alerts
            updateState(CHANNEL_LEAK_ALERT, data.isLeakAlert() ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_LOW_BATTERY, data.isLowBatteryAlert() ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_REVERSE_FLOW, data.isReverseFlowAlert() ? OnOffType.ON : OnOffType.OFF);

            // Update Read Time
            String readTime = data.getReadTime();
            if (!readTime.isBlank()) {
                try {
                    java.time.ZonedDateTime zdt;
                    if (readTime.contains("Z") || readTime.contains("+") || (readTime.lastIndexOf("-") > 10)) {
                        zdt = java.time.ZonedDateTime.parse(readTime);
                    } else {
                        zdt = java.time.LocalDateTime.parse(readTime).atZone(timeZoneProvider.getTimeZone());
                    }
                    updateState(CHANNEL_LAST_READ_TIME, new DateTimeType(zdt));
                } catch (java.time.format.DateTimeParseException e) {
                    logger.warn("Failed to parse ISO-8601 read time: {}", readTime, e);
                }
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Unsupported unit '{}' received for meter {}: {}", rawUnit, getThing().getUID(),
                    e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.unsupported-unit");
        }
    }
}
