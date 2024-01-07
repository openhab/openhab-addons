/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.airgradient.internal.handler;

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airgradient.internal.config.AirGradientLocationConfiguration;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirGradientAPIHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class AirGradientLocationHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AirGradientLocationHandler.class);

    public AirGradientLocationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // This is read only
        logger.debug("Received command {} for channel {}, but air gradient locations are read only", command.toString(),
                channelUID.getId());
    }

    @Override
    public void initialize() {
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        Bridge controller = getBridge();
        if (controller == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        } else if (ThingStatus.OFFLINE.equals(controller.getStatus())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public String getLocationId() {
        AirGradientLocationConfiguration config = getConfigAs(AirGradientLocationConfiguration.class);
        return config.location;
    }

    public void setMeasurment(String locationId, Measure measure) {
        logger.debug("Updating measure for location {}, with name: {}", locationId, measure.locationName);
        updateProperty(PROPERTY_FIRMWARE_VERSION, measure.firmwareVersion);
        updateProperty(PROPERTY_NAME, measure.locationName);
        updateProperty(PROPERTY_SERIAL_NO, measure.serialno);

        updateStateNullSafe(CHANNEL_ATMP, measure.atmp, (d) -> new QuantityType<>(d, SIUnits.CELSIUS));
        updateStateNullSafe(CHANNEL_PM_003_COUNT, measure.pm003Count, (d) -> new QuantityType<>(d, Units.ONE));
        updateStateNullSafe(CHANNEL_PM_01, measure.pm01, (d) -> new QuantityType<>(d, Units.MICROGRAM_PER_CUBICMETRE));
        updateStateNullSafe(CHANNEL_PM_02, measure.pm02, (d) -> new QuantityType<>(d, Units.MICROGRAM_PER_CUBICMETRE));
        updateStateNullSafe(CHANNEL_PM_10, measure.pm10, (d) -> new QuantityType<>(d, Units.MICROGRAM_PER_CUBICMETRE));
        updateStateNullSafe(CHANNEL_RCO2, measure.rco2, (d) -> new QuantityType<>(d, Units.PARTS_PER_MILLION));
        updateStateNullSafe(CHANNEL_RHUM, measure.rhum, (d) -> new QuantityType<>(d, Units.PERCENT));
        updateStateNullSafe(CHANNEL_TVOC, measure.tvoc, (d) -> new QuantityType<>(d, Units.PARTS_PER_BILLION));
        updateStateNullSafe(CHANNEL_WIFI, measure.wifi, (d) -> new QuantityType<>(d, Units.DECIBEL_MILLIWATTS));
    }

    private void updateStateNullSafe(String channelName, @Nullable Double measure,
            Function<Double, QuantityType<?>> value) {
        if (measure == null) {
            updateState(channelName, UnDefType.NULL);
        } else {
            updateState(channelName, value.apply(measure));
        }
    }
}
