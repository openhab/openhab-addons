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

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airgradient.internal.config.AirGradientLocationConfiguration;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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

    private @NonNullByDefault({}) AirGradientLocationConfiguration locationConfig = null;

    public AirGradientLocationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel {}: {}", channelUID, command.toFullString());
        if (command instanceof RefreshType) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                if (bridge.getHandler() instanceof AirGradientAPIHandler handler) {
                    handler.pollingCode();
                }
            }
        } else if (CHANNEL_LEDS_MODE.equals(channelUID.getId())) {
            if (command instanceof StringType stringCommand) {
                Bridge bridge = getBridge();
                if (bridge != null) {
                    if (bridge.getHandler() instanceof AirGradientAPIHandler handler) {
                        handler.getApiController().setLedMode(getSerialNo(), stringCommand.toFullString());
                    }
                }
            } else {
                logger.debug("Received command {} for channel {}, but it needs a string command", command.toString(),
                        channelUID.getId());
            }
        } else if (CHANNEL_CALIBRATION.equals(channelUID.getId())) {
            if (command instanceof StringType stringCommand) {
                if ("co2".equals(stringCommand.toFullString())) {
                    Bridge bridge = getBridge();
                    if (bridge != null) {
                        if (bridge.getHandler() instanceof AirGradientAPIHandler handler) {
                            handler.getApiController().calibrateCo2(getSerialNo());
                        }
                    }
                } else {
                    logger.warn(
                            "Received unknown command {} for calibration on channel {}, which we don't know how to handle",
                            command.toString(), channelUID.getId());
                }
            }
        } else {
            // This is read only
            logger.warn("Received command {} for channel {}, which we don't know how to handle", command.toString(),
                    channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);
        locationConfig = getConfigAs(AirGradientLocationConfiguration.class);

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
        return locationConfig.location;
    }

    public void setMeasurment(String locationId, Measure measure) {
        logger.debug("Updating measure for location {}, with name: {}", locationId, measure.locationName);
        updateProperty(PROPERTY_FIRMWARE_VERSION, measure.firmwareVersion);
        updateProperty(PROPERTY_NAME, measure.locationName);
        updateProperty(PROPERTY_SERIAL_NO, measure.serialno);

        updateMeasurement(CHANNEL_ATMP, toQuantityType(measure.atmp, SIUnits.CELSIUS));
        updateMeasurement(CHANNEL_PM_003_COUNT, toQuantityType(measure.pm003Count, Units.ONE));
        updateMeasurement(CHANNEL_PM_01, toQuantityType(measure.pm01, Units.MICROGRAM_PER_CUBICMETRE));
        updateMeasurement(CHANNEL_PM_02, toQuantityType(measure.pm02, Units.MICROGRAM_PER_CUBICMETRE));
        updateMeasurement(CHANNEL_PM_10, toQuantityType(measure.pm10, Units.MICROGRAM_PER_CUBICMETRE));
        updateMeasurement(CHANNEL_RHUM, toQuantityType(measure.rhum, Units.PERCENT));
        updateMeasurement(CHANNEL_UPLOADS_SINCE_BOOT, toQuantityType(measure.boot, Units.ONE));

        Double rco2 = measure.rco2;
        if (rco2 != null) {
            updateMeasurement(CHANNEL_RCO2, toQuantityType(rco2.longValue(), Units.PARTS_PER_MILLION));
        }

        Double tvoc = measure.tvoc;
        if (tvoc != null) {
            updateMeasurement(CHANNEL_TVOC, toQuantityType(tvoc.longValue(), Units.PARTS_PER_BILLION));
        }

        updateMeasurement(CHANNEL_WIFI, toQuantityType(measure.wifi, Units.DECIBEL_MILLIWATTS));
        updateMeasurement(CHANNEL_LEDS_MODE, toStringType(measure.ledMode));
    }

    public void setLedMode(String ledMode) {
        ChannelUID ledsChannel = new ChannelUID(thing.getUID(), CHANNEL_LEDS_MODE);
        updateState(ledsChannel, StringType.valueOf(ledMode));
    }

    private void updateMeasurement(String channelName, State state) {
        ChannelUID channelUid = new ChannelUID(thing.getUID(), channelName);
        if (isLinked(channelUid)) {
            updateState(channelName, state);
        }
    }

    public static State toQuantityType(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, unit);
    }

    public static State toStringType(@Nullable String value) {
        return value == null ? UnDefType.NULL : StringType.valueOf(value);
    }

    /**
     * Returns the serial number of this sensor.
     *
     * @return serial number of this sensor.
     */
    public String getSerialNo() {
        String serialNo = thing.getProperties().get(PROPERTY_SERIAL_NO);
        if (serialNo == null) {
            serialNo = "";
        }

        return serialNo;
    }
}
