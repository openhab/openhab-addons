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
package org.openhab.binding.miele.internal.handler;

import static org.openhab.binding.miele.internal.MieleBindingConstants.MIELE_DEVICE_CLASS_DISHWASHER;
import static org.openhab.binding.miele.internal.MieleBindingConstants.POWER_CONSUMPTION_CHANNEL_ID;
import static org.openhab.binding.miele.internal.MieleBindingConstants.WATER_CONSUMPTION_CHANNEL_ID;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.miele.internal.api.dto.DeviceProperty;
import org.openhab.binding.miele.internal.exceptions.MieleRpcException;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link DishwasherHandler} is responsible for handling commands,
 * which are sent to one of the channels
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - fixed handling of REFRESH commands
 * @author Martin Lepsy - fixed handling of empty JSON results
 * @author Jacob Laursen - Fixed multicast and protocol support (ZigBee/LAN), added power/water consumption channels
 */
@NonNullByDefault
public class DishwasherHandler extends MieleApplianceHandler<DishwasherChannelSelector>
        implements ExtendedDeviceStateListener {

    private static final int POWER_CONSUMPTION_BYTE_POSITION = 16;
    private static final int WATER_CONSUMPTION_BYTE_POSITION = 18;
    private static final int EXTENDED_STATE_MIN_SIZE_BYTES = 19;

    private final Logger logger = LoggerFactory.getLogger(DishwasherHandler.class);

    public DishwasherHandler(Thing thing, TranslationProvider i18nProvider, LocaleProvider localeProvider,
            TimeZoneProvider timeZoneProvider) {
        super(thing, i18nProvider, localeProvider, timeZoneProvider, DishwasherChannelSelector.class,
                MIELE_DEVICE_CLASS_DISHWASHER);
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

        DishwasherChannelSelector selector = (DishwasherChannelSelector) getValueSelectorFromChannelID(channelID);
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
                .valueOf(extendedDeviceState[POWER_CONSUMPTION_BYTE_POSITION] & 0xff);
        var kiloWattHours = new QuantityType<>(kiloWattHoursTenths.divide(BigDecimal.valueOf(10)), Units.KILOWATT_HOUR);
        updateExtendedState(POWER_CONSUMPTION_CHANNEL_ID, kiloWattHours);

        BigDecimal decilitres = BigDecimal.valueOf(extendedDeviceState[WATER_CONSUMPTION_BYTE_POSITION] & 0xff);
        var litres = new QuantityType<>(decilitres.divide(BigDecimal.valueOf(10)), Units.LITRE);
        updateExtendedState(WATER_CONSUMPTION_CHANNEL_ID, litres);
    }
}
