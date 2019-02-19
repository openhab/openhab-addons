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
package org.openhab.binding.bluetooth.ruuvitag.handler;

import static org.openhab.binding.bluetooth.ruuvitag.RuuviTagBindingConstants.*;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.tkgwf.ruuvi.common.bean.RuuviMeasurement;
import fi.tkgwf.ruuvi.common.parser.impl.AnyDataFormatParser;
import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.function.LogConverter;
import tec.uom.se.function.MultiplyConverter;
import tec.uom.se.unit.TransformedUnit;

/**
 * The {@link RuuviTagHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class RuuviTagHandler extends BeaconBluetoothHandler {
    private static final double METRE_PER_SQUARE_SECOND_TO_STANDARD_GRAVITY = 9.80665;
    private static final UnitConverter DIVIDE_BY_TEN = new MultiplyConverter(10).inverse();
    private static final UnitConverter EXP_BASE_TEN = new LogConverter(10).inverse();

    private static final Unit<Acceleration> STANDARD_GRAVITY = new TransformedUnit<>("g",
            SmartHomeUnits.METRE_PER_SQUARE_SECOND, new MultiplyConverter(METRE_PER_SQUARE_SECOND_TO_STANDARD_GRAVITY));
    private static final Unit<Power> DECIBEL_MILLIWATTS = new TransformedUnit<>("dBm",
            MetricPrefix.MILLI(SmartHomeUnits.WATT), EXP_BASE_TEN.concatenate(DIVIDE_BY_TEN));

    static {
        // Register the custom units labels
        SimpleUnitFormat.getInstance().label(STANDARD_GRAVITY, "g");
        SimpleUnitFormat.getInstance().label(DECIBEL_MILLIWATTS, "dBm");
    }

    private final Logger logger = LoggerFactory.getLogger(RuuviTagHandler.class);
    private final AnyDataFormatParser parser = new AnyDataFormatParser();

    public RuuviTagHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        super.onScanRecordReceived(scanNotification);
        final byte[] manufacturerData = scanNotification.getManufacturerData();
        if (manufacturerData != null && manufacturerData.length > 0) {
            final RuuviMeasurement ruuvitagData = parser.parse(manufacturerData);
            logger.trace("Ruuvi received new scan notification for {}: {}", scanNotification.getAddress(),
                    ruuvitagData);
            if (ruuvitagData != null) {
                boolean atLeastOneRuuviFieldPresent = false;
                for (Channel channel : getThing().getChannels()) {
                    ChannelUID channelUID = channel.getUID();
                    switch (channelUID.getId()) {
                        case CHANNEL_ID_ACCELERATIONX:
                            atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                    ruuvitagData.getAccelerationX(), STANDARD_GRAVITY);
                            break;
                        case CHANNEL_ID_ACCELERATIONY:
                            atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                    ruuvitagData.getAccelerationY(), STANDARD_GRAVITY);
                            break;
                        case CHANNEL_ID_ACCELERATIONZ:
                            atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                    ruuvitagData.getAccelerationZ(), STANDARD_GRAVITY);
                            break;
                        case CHANNEL_ID_BATTERY:
                            atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                    ruuvitagData.getBatteryVoltage(), SmartHomeUnits.VOLT);
                            break;
                        case CHANNEL_ID_DATA_FORMAT:
                            atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getDataFormat(),
                                    null);
                            break;
                        case CHANNEL_ID_HUMIDITY:
                            atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getHumidity(),
                                    SmartHomeUnits.PERCENT);
                            break;
                        case CHANNEL_ID_MEASUREMENT_SEQUENCE_NUMBER:
                            atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                    ruuvitagData.getMeasurementSequenceNumber(), SmartHomeUnits.ONE);
                            break;
                        case CHANNEL_ID_MOVEMENT_COUNTER:
                            atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                    ruuvitagData.getMovementCounter(), SmartHomeUnits.ONE);
                            break;
                        case CHANNEL_ID_PRESSURE:
                            atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getPressure(),
                                    SIUnits.PASCAL);
                            break;
                        case CHANNEL_ID_TEMPERATURE:
                            atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                    ruuvitagData.getTemperature(), SIUnits.CELSIUS);
                            break;
                        case CHANNEL_ID_TX_POWER:
                            atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getTxPower(),
                                    DECIBEL_MILLIWATTS);
                            break;
                    }
                }
                if (atLeastOneRuuviFieldPresent) {
                    // In practice, updated to ONLINE by super.onScanRecordReceived already, based on RSSI value
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Received Ruuvi Tag data but no fields could be parsed");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Received bluetooth data which could not be parsed to any known Ruuvi Tag data formats");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Received Bluetooth scan with no manufacturer data");
        }
    }

    /**
     * Update channel state
     *
     * Update is not done when value is null.
     *
     * @param channelUID channel UID
     * @param value      value to update
     * @param unit       unit associated with value, or null when we are updating DecimalType
     * @return whether the value was present
     */
    private <T extends Quantity<T>> boolean updateStateIfLinked(ChannelUID channelUID, @Nullable Number value,
            @Nullable Unit<T> unit) {
        if (value == null) {
            return false;
        }
        if (!isLinked(channelUID)) {
            return true;
        }
        if (unit == null) {
            updateState(channelUID, new DecimalType(value.doubleValue()));
        } else {
            updateState(channelUID, new QuantityType<>(value, unit));
        }
        return true;
    }

}
