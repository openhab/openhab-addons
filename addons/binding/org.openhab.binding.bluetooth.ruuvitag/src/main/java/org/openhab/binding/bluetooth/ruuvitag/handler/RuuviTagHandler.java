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

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.binding.bluetooth.ruuvitag.RuuviTagBindingConstants;
import org.slf4j.LoggerFactory;

import fi.tkgwf.ruuvi.common.bean.RuuviMeasurement;
import fi.tkgwf.ruuvi.common.parser.impl.AnyDataFormatParser;
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
public class RuuviTagHandler extends BeaconBluetoothHandler implements BluetoothDeviceListener {

    private static final double METRE_PER_SQUARE_SECOND_TO_STANDARD_GRAVITY = 9.80665;
    private static final UnitConverter DIVIDE_BY_TEN = new MultiplyConverter(10).inverse();
    private static final UnitConverter EXP_BASE_TEN = new LogConverter(10).inverse();

    private static final Unit<Acceleration> STANDARD_GRAVITY = new TransformedUnit<>("g",
            SmartHomeUnits.METRE_PER_SQUARE_SECOND, new MultiplyConverter(METRE_PER_SQUARE_SECOND_TO_STANDARD_GRAVITY));
    private static final Unit<Power> DECIBEL_MILLIWATTS = new TransformedUnit<>("dBm",
            MetricPrefix.MILLI(SmartHomeUnits.WATT), EXP_BASE_TEN.concatenate(DIVIDE_BY_TEN));

    private final AnyDataFormatParser parser = new AnyDataFormatParser();
    private volatile Map<String, ChannelUID> channelCache = new HashMap<>();

    public RuuviTagHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        channelCache.clear();
        super.initialize();
    }

    @Override
    public void dispose() {
        channelCache.clear();
        super.dispose();
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        LoggerFactory.getLogger(this.getClass()).info("onScanRecordReceived");
        final byte[] manufacturerData = scanNotification.getManufacturerData();
        boolean fieldPresent = false;
        if (manufacturerData != null) {
            final RuuviMeasurement ruuvitagData = parser.parse(manufacturerData);
            LoggerFactory.getLogger(this.getClass()).info("onScanRecordReceived-> {}", ruuvitagData);
            if (ruuvitagData != null) {
                fieldPresent |= updateStateIfLinked(RuuviTagBindingConstants.CHANNEL_ID_ACCELERATIONX,
                        ruuvitagData.getAccelerationX(), STANDARD_GRAVITY);
                fieldPresent |= updateStateIfLinked(RuuviTagBindingConstants.CHANNEL_ID_ACCELERATIONY,
                        ruuvitagData.getAccelerationY(), STANDARD_GRAVITY);
                fieldPresent |= updateStateIfLinked(RuuviTagBindingConstants.CHANNEL_ID_ACCELERATIONZ,
                        ruuvitagData.getAccelerationZ(), STANDARD_GRAVITY);
                fieldPresent |= updateStateIfLinked(RuuviTagBindingConstants.CHANNEL_ID_BATTERY,
                        ruuvitagData.getBatteryVoltage(), SmartHomeUnits.VOLT);
                fieldPresent |= updateStateIfLinked(RuuviTagBindingConstants.CHANNEL_ID_DATA_FORMAT,
                        ruuvitagData.getDataFormat(), null);
                fieldPresent |= updateStateIfLinked(RuuviTagBindingConstants.CHANNEL_ID_HUMIDITY,
                        ruuvitagData.getHumidity(), SmartHomeUnits.PERCENT);
                fieldPresent |= updateStateIfLinked(RuuviTagBindingConstants.CHANNEL_ID_MEASUREMENT_SEQUENCE_NUMBER,
                        ruuvitagData.getMeasurementSequenceNumber(), SmartHomeUnits.ONE);
                fieldPresent |= updateStateIfLinked(RuuviTagBindingConstants.CHANNEL_ID_MOVEMENT_COUNTER,
                        ruuvitagData.getMovementCounter(), SmartHomeUnits.ONE);
                fieldPresent |= updateStateIfLinked(RuuviTagBindingConstants.CHANNEL_ID_PRESSURE,
                        ruuvitagData.getPressure(), SIUnits.PASCAL);
                fieldPresent |= updateStateIfLinked(RuuviTagBindingConstants.CHANNEL_ID_TEMPERATURE,
                        ruuvitagData.getTemperature(), SIUnits.CELSIUS);
                fieldPresent |= updateStateIfLinked(RuuviTagBindingConstants.CHANNEL_ID_TX_POWER,
                        ruuvitagData.getTxPower(), DECIBEL_MILLIWATTS);
                if (fieldPresent) {
                    updateStatus(ThingStatus.ONLINE);
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
                    "Received bluetooth data with empty ");
        }
        super.onScanRecordReceived(scanNotification);
    }

    /**
     * Update channel state
     *
     * Update is not done when value is null.
     *
     * @param channel channel id
     * @param value   value to update
     * @param unit    unit associated with value, or null when we are updating DecimalType
     * @return whether the value was present
     */
    private <T extends Quantity<T>> boolean updateStateIfLinked(String channel, @Nullable Number value,
            @Nullable Unit<T> unit) {
        if (value == null) {
            return false;
        }
        ChannelUID channelUID = getChannelUID(channel);
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

    private ChannelUID getChannelUID(String channelID) {
        return channelCache.computeIfAbsent(channelID, id -> new ChannelUID(getThing().getUID(), id));
    }

}
