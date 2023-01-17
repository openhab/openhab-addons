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
package org.openhab.binding.bluetooth.ruuvitag.internal;

import static org.openhab.binding.bluetooth.ruuvitag.internal.RuuviTagBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.tkgwf.ruuvi.common.bean.RuuviMeasurement;
import fi.tkgwf.ruuvi.common.parser.impl.AnyDataFormatParser;

/**
 * The {@link RuuviTagHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class RuuviTagHandler extends BeaconBluetoothHandler {

    // Ruuvitag sends an update every 10 seconds. So we keep a heartbeat to give it some slack
    private static final int HEARTBEAT_TIMEOUT_MINUTES = 1;
    private final Logger logger = LoggerFactory.getLogger(RuuviTagHandler.class);
    private final AnyDataFormatParser parser = new AnyDataFormatParser();
    private final AtomicBoolean receivedStatus = new AtomicBoolean();

    private @NonNullByDefault({}) ScheduledFuture<?> heartbeatFuture;

    public RuuviTagHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (getThing().getStatus() != ThingStatus.OFFLINE) {
            heartbeatFuture = scheduler.scheduleWithFixedDelay(this::heartbeat, 0, HEARTBEAT_TIMEOUT_MINUTES,
                    TimeUnit.MINUTES);
        }
    }

    private void heartbeat() {
        synchronized (receivedStatus) {
            if (!receivedStatus.getAndSet(false) && getThing().getStatus() == ThingStatus.ONLINE) {
                getThing().getChannels().stream().map(Channel::getUID).filter(this::isLinked)
                        .forEach(c -> updateState(c, UnDefType.UNDEF));
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "No data received for some time");
            }
        }
    }

    @Override
    public void dispose() {
        try {
            super.dispose();
        } finally {
            if (heartbeatFuture != null) {
                heartbeatFuture.cancel(true);
                heartbeatFuture = null;
            }
        }
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        synchronized (receivedStatus) {
            receivedStatus.set(true);
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
                                        ruuvitagData.getAccelerationX(), Units.STANDARD_GRAVITY);
                                break;
                            case CHANNEL_ID_ACCELERATIONY:
                                atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                        ruuvitagData.getAccelerationY(), Units.STANDARD_GRAVITY);
                                break;
                            case CHANNEL_ID_ACCELERATIONZ:
                                atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                        ruuvitagData.getAccelerationZ(), Units.STANDARD_GRAVITY);
                                break;
                            case CHANNEL_ID_BATTERY:
                                atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                        ruuvitagData.getBatteryVoltage(), Units.VOLT);
                                break;
                            case CHANNEL_ID_DATA_FORMAT:
                                atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                        ruuvitagData.getDataFormat());
                                break;
                            case CHANNEL_ID_HUMIDITY:
                                atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                        ruuvitagData.getHumidity(), Units.PERCENT);
                                break;
                            case CHANNEL_ID_MEASUREMENT_SEQUENCE_NUMBER:
                                atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                        ruuvitagData.getMeasurementSequenceNumber(), Units.ONE);
                                break;
                            case CHANNEL_ID_MOVEMENT_COUNTER:
                                atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                        ruuvitagData.getMovementCounter(), Units.ONE);
                                break;
                            case CHANNEL_ID_PRESSURE:
                                atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                        ruuvitagData.getPressure(), SIUnits.PASCAL);
                                break;
                            case CHANNEL_ID_TEMPERATURE:
                                atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                        ruuvitagData.getTemperature(), SIUnits.CELSIUS);
                                break;
                            case CHANNEL_ID_TX_POWER:
                                atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                                        ruuvitagData.getTxPower(), Units.DECIBEL_MILLIWATTS);
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
                // Received Bluetooth scan with no manufacturer data
                // This happens -- we ignore this silently.
            }
        }
    }

    /**
     * Update QuantityType channel state
     *
     * Update is not done when value is null.
     *
     * @param channelUID channel UID
     * @param value value to update
     * @param unit unit associated with the value
     * @return whether the value was present
     */
    private <T extends Quantity<T>> boolean updateStateIfLinked(ChannelUID channelUID, @Nullable Number value,
            Unit<T> unit) {
        if (value == null) {
            return false;
        }
        if (isLinked(channelUID)) {
            updateState(channelUID, new QuantityType<>(value, unit));
        }
        return true;
    }

    /**
     * Update DecimalType channel state
     *
     * Update is not done when value is null.
     *
     * @param channelUID channel UID
     * @param value value to update
     * @return whether the value was present
     */
    private <T extends Quantity<T>> boolean updateStateIfLinked(ChannelUID channelUID, @Nullable Integer value) {
        if (value == null) {
            return false;
        }
        if (isLinked(channelUID)) {
            updateState(channelUID, new DecimalType(value));
        }
        return true;
    }
}
