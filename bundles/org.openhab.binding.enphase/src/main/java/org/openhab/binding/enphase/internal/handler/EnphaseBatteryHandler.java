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
package org.openhab.binding.enphase.internal.handler;

import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.*;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enphase.internal.MessageTranslator;
import org.openhab.binding.enphase.internal.dto.InventoryJsonDTO.DeviceDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link EnphaseBatteryHandler} handles the IQ Battery (Encharge) things.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class EnphaseBatteryHandler extends EnphaseDeviceHandler {

    public EnphaseBatteryHandler(final Thing thing, final MessageTranslator messageTranslator) {
        super(thing, messageTranslator);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            final String channelId = channelUID.getId();

            switch (channelId) {
                case BATTERY_CHANNEL_STATE_OF_CHARGE:
                    refreshStateOfCharge(lastKnownDeviceState);
                    break;
                case BATTERY_CHANNEL_TEMPERATURE:
                    refreshTemperature(lastKnownDeviceState);
                    break;
                case BATTERY_CHANNEL_MAX_CELL_TEMPERATURE:
                    refreshMaxCellTemperature(lastKnownDeviceState);
                    break;
                case BATTERY_CHANNEL_CAPACITY:
                    refreshCapacity(lastKnownDeviceState);
                    break;
                case BATTERY_CHANNEL_SLEEP_ENABLED:
                    refreshSleepEnabled(lastKnownDeviceState);
                    break;
                default:
                    super.handleCommandRefresh(channelId);
                    break;
            }
        }
    }

    private void refreshStateOfCharge(final @Nullable DeviceDTO deviceDTO) {
        updateState(BATTERY_CHANNEL_STATE_OF_CHARGE,
                toState(deviceDTO == null ? null : deviceDTO.percentFull, Units.PERCENT));
    }

    private void refreshTemperature(final @Nullable DeviceDTO deviceDTO) {
        updateState(BATTERY_CHANNEL_TEMPERATURE,
                toState(deviceDTO == null ? null : deviceDTO.temperature, SIUnits.CELSIUS));
    }

    private void refreshMaxCellTemperature(final @Nullable DeviceDTO deviceDTO) {
        updateState(BATTERY_CHANNEL_MAX_CELL_TEMPERATURE,
                toState(deviceDTO == null ? null : deviceDTO.maxCellTemp, SIUnits.CELSIUS));
    }

    private void refreshCapacity(final @Nullable DeviceDTO deviceDTO) {
        updateState(BATTERY_CHANNEL_CAPACITY,
                toState(deviceDTO == null ? null : deviceDTO.enchargeCapacity, Units.WATT_HOUR));
    }

    private void refreshSleepEnabled(final @Nullable DeviceDTO deviceDTO) {
        final Boolean sleepEnabled = deviceDTO == null ? null : deviceDTO.sleepEnabled;

        updateState(BATTERY_CHANNEL_SLEEP_ENABLED,
                sleepEnabled == null ? UnDefType.UNDEF : OnOffType.from(sleepEnabled));
    }

    private <Q extends Quantity<Q>> State toState(final @Nullable Integer value, final Unit<Q> unit) {
        return value == null ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    @Override
    public void refreshDeviceState(final @Nullable DeviceDTO deviceDTO) {
        refreshStateOfCharge(deviceDTO);
        refreshTemperature(deviceDTO);
        refreshMaxCellTemperature(deviceDTO);
        refreshCapacity(deviceDTO);
        refreshSleepEnabled(deviceDTO);
        super.refreshDeviceState(deviceDTO);
    }
}
