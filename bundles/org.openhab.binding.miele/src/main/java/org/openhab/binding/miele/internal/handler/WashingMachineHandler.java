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
package org.openhab.binding.miele.internal.handler;

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.miele.internal.api.dto.DeviceProperty;
import org.openhab.binding.miele.internal.exceptions.MieleRpcException;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link WashingMachineHandler} is responsible for handling commands,
 * which are sent to one of the channels
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - fixed handling of REFRESH commands
 * @author Martin Lepsy - fixed handling of empty JSON results
 * @author Jacob Laursen - Fixed multicast and protocol support (ZigBee/LAN), added power/water consumption channels
 **/
@NonNullByDefault
public class WashingMachineHandler extends MieleApplianceHandler<WashingMachineChannelSelector>
        implements ExtendedDeviceStateListener {

    private static final int LAUNDRY_WEIGHT_BYTE_POSITION = 44;
    private static final int ENERGY_CONSUMPTION_BYTE_POSITION = 51;
    private static final int WATER_CONSUMPTION_BYTE_POSITION = 53;
    private static final int EXTENDED_STATE_MIN_SIZE_BYTES = 54;

    private final Logger logger = LoggerFactory.getLogger(WashingMachineHandler.class);

    public WashingMachineHandler(Thing thing, TranslationProvider i18nProvider, LocaleProvider localeProvider,
            TimeZoneProvider timeZoneProvider) {
        super(thing, i18nProvider, localeProvider, timeZoneProvider, WashingMachineChannelSelector.class,
                MIELE_DEVICE_CLASS_WASHING_MACHINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        String channelID = channelUID.getId();
        String applianceId = this.applianceId;
        if (applianceId == null) {
            logger.warn("Command '{}' failed, appliance id is unknown", command);
            return;
        }

        WashingMachineChannelSelector selector = (WashingMachineChannelSelector) getValueSelectorFromChannelID(
                channelID);
        JsonElement result = null;

        try {
            switch (selector) {
                case SWITCH: {
                    MieleBridgeHandler bridgeHandler = getMieleBridgeHandler();
                    if (bridgeHandler == null) {
                        logger.warn("Command '{}' failed, missing bridge handler", command);
                        return;
                    }
                    if (command.equals(OnOffType.ON)) {
                        result = bridgeHandler.invokeOperation(applianceId, modelID, "start");
                    } else if (command.equals(OnOffType.OFF)) {
                        result = bridgeHandler.invokeOperation(applianceId, modelID, "stop");
                    }
                    break;
                }
                default: {
                    if (!(command instanceof RefreshType)) {
                        logger.debug("{} is a read-only channel that does not accept commands",
                                selector.getChannelID());
                    }
                }
            }
            // process result
            if (result != null && isResultProcessable(result)) {
                logger.debug("Result of operation is {}", result.getAsString());
            }
        } catch (IllegalArgumentException e) {
            logger.warn(
                    "An error occurred while trying to set the read-only variable associated with channel '{}' to '{}'",
                    channelID, command.toString());
        } catch (MieleRpcException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                logger.warn("An error occurred while trying to invoke operation: {}", e.getMessage());
            } else {
                logger.warn("An error occurred while trying to invoke operation: {} -> {}", e.getMessage(),
                        cause.getMessage());
            }
        }
    }

    @Override
    public void onAppliancePropertyChanged(DeviceProperty dp) {
        super.onAppliancePropertyChanged(dp);
        updateSwitchStartStopFromState(dp);
    }

    @Override
    public void onApplianceExtendedStateChanged(byte[] extendedDeviceState) {
        if (extendedDeviceState.length < EXTENDED_STATE_MIN_SIZE_BYTES) {
            logger.debug("Insufficient extended state data to extract consumption values: {}", extendedDeviceState);
            return;
        }

        BigDecimal kiloWattHoursTenths = BigDecimal
                .valueOf(extendedDeviceState[ENERGY_CONSUMPTION_BYTE_POSITION] & 0xff);
        var kiloWattHours = new QuantityType<>(kiloWattHoursTenths.divide(BigDecimal.valueOf(10)), Units.KILOWATT_HOUR);
        updateExtendedState(ENERGY_CONSUMPTION_CHANNEL_ID, kiloWattHours);

        var litres = new QuantityType<>(BigDecimal.valueOf(extendedDeviceState[WATER_CONSUMPTION_BYTE_POSITION] & 0xff),
                Units.LITRE);
        updateExtendedState(WATER_CONSUMPTION_CHANNEL_ID, litres);

        var weight = new QuantityType<>(
                BigDecimal.valueOf(256 * (extendedDeviceState[LAUNDRY_WEIGHT_BYTE_POSITION] & 0xff)
                        + (extendedDeviceState[LAUNDRY_WEIGHT_BYTE_POSITION + 1] & 0xff)),
                SIUnits.GRAM);
        updateExtendedState(LAUNDRY_WEIGHT_CHANNEL_ID, weight);
    }
}
