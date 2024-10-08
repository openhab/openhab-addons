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
package org.openhab.binding.homewizard.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.dto.DataPayload;
import org.openhab.binding.homewizard.internal.dto.StatePayload;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link HomeWizardEnergySocketHandler} implements functionality to handle a HomeWizard EnergySocket.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class HomeWizardEnergySocketHandler extends HomeWizardStatefulDeviceHandler {

    /**
     * Constructor
     *
     * @param thing The thing to handle
     * @param timeZoneProvider The TimeZoneProvider
     */
    public HomeWizardEnergySocketHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    /**
     * Converts a brightness value (0..255) to a percentage.
     *
     * @param brightness The brightness to convert.
     * @return brightness percentage
     */
    private int brightnessToPercentage(int brightness) {
        return (int) (100.0 * brightness / 255.0 + 0.5);
    }

    /**
     * Converts a percentage to a brightness value (0..255)
     *
     * @param percentage The percentage to convert.
     * @return brightness value
     */
    private int percentageToBrightness(String percentage) {
        return (int) (Double.valueOf(percentage) * 255.0 / 100.0 + 0.5);
    }

    /**
     * Handle incoming commands.
     *
     * Power on/off, Power lock/unlock and Ring brightness are supported.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // For now I prefer not updating immediately above firing a full update request for each channel
            return;
        }

        StatePayload result = null;

        /*
         * The returned payloads below only contain the modified value, so each has it's own
         * call to updateState instead of just calling handleStatePayload() with the returned
         * payload.
         */

        switch (channelUID.getId()) {
            case HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS: {
                result = sendStateCommand(
                        String.format("{\"brightness\": %d}", percentageToBrightness(command.toFullString())));
                if (result != null) {
                    updateState(HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS,
                            new PercentType(brightnessToPercentage(result.getBrightness())));
                }
                break;
            }
            case HomeWizardBindingConstants.CHANNEL_POWER_SWITCH: {
                boolean onOff = command.equals(OnOffType.ON);
                result = sendStateCommand(String.format("{\"power_on\": %b}", onOff));
                if (result != null) {
                    updateState(HomeWizardBindingConstants.CHANNEL_POWER_SWITCH, OnOffType.from(result.getPowerOn()));
                }
                break;
            }
            case HomeWizardBindingConstants.CHANNEL_POWER_LOCK: {
                boolean onOff = command.equals(OnOffType.ON);
                result = sendStateCommand(String.format("{\"switch_lock\": %b}", onOff));
                if (result != null) {
                    updateState(HomeWizardBindingConstants.CHANNEL_POWER_LOCK, OnOffType.from(result.getSwitchLock()));
                }
                break;
            }
            default:
                logger.warn("Should handle {} {}", channelUID.getIdWithoutGroup(), command);
                break;
        }
    }

    /**
     * Device specific handling of the returned payload.
     *
     * @param payload The data parsed from the Json file
     */
    @Override
    protected void handleDataPayload(DataPayload payload) {
        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1,
                new QuantityType<>(payload.getTotalEnergyImportT1Kwh(), Units.KILOWATT_HOUR));
        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1,
                new QuantityType<>(payload.getTotalEnergyExportT1Kwh(), Units.KILOWATT_HOUR));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER,
                new QuantityType<>(payload.getActivePowerW(), Units.WATT));
    }

    @Override
    protected void handleStatePayload(StatePayload payload) {
        updateState(HomeWizardBindingConstants.CHANNEL_POWER_SWITCH, OnOffType.from(payload.getPowerOn()));
        updateState(HomeWizardBindingConstants.CHANNEL_POWER_LOCK, OnOffType.from(payload.getSwitchLock()));
        updateState(HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS,
                new PercentType(brightnessToPercentage(payload.getBrightness())));
    }
}
