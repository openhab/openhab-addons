/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.openhab.binding.miele.internal.handler.MieleBridgeHandler.DeviceProperty;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link FridgeFreezerHandler} is responsible for handling commands,
 * which are sent to one of the channels
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - fixed handling of REFRESH commands
 * @author Martin Lepsy - fixed handling of empty JSON results
 * @author Jacob Laursen - Fixed multicast and protocol support (ZigBee/LAN)
 */
public class FridgeFreezerHandler extends MieleApplianceHandler<FridgeFreezerChannelSelector> {

    private final Logger logger = LoggerFactory.getLogger(FridgeFreezerHandler.class);

    public FridgeFreezerHandler(Thing thing, TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        super(thing, i18nProvider, localeProvider, FridgeFreezerChannelSelector.class,
                MIELE_DEVICE_CLASS_FRIDGE_FREEZER);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        String channelID = channelUID.getId();
        String applianceId = (String) getThing().getConfiguration().getProperties().get(APPLIANCE_ID);

        FridgeFreezerChannelSelector selector = (FridgeFreezerChannelSelector) getValueSelectorFromChannelID(channelID);
        JsonElement result = null;

        try {
            if (selector != null) {
                switch (selector) {
                    case SUPERCOOL: {
                        if (command.equals(OnOffType.ON)) {
                            result = bridgeHandler.invokeOperation(applianceId, modelID, "startSuperCooling");
                        } else if (command.equals(OnOffType.OFF)) {
                            result = bridgeHandler.invokeOperation(applianceId, modelID, "stopSuperCooling");
                        }
                        break;
                    }
                    case SUPERFREEZE: {
                        if (command.equals(OnOffType.ON)) {
                            result = bridgeHandler.invokeOperation(applianceId, modelID, "startSuperFreezing");
                        } else if (command.equals(OnOffType.OFF)) {
                            result = bridgeHandler.invokeOperation(applianceId, modelID, "stopSuperFreezing");
                        }
                        break;
                    }
                    default: {
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
        }
    }

    @Override
    protected void onAppliancePropertyChanged(DeviceProperty dp) {
        super.onAppliancePropertyChanged(dp);

        if (!STATE_PROPERTY_NAME.equals(dp.Name)) {
            return;
        }

        // Supercool/superfreeze is not exposed directly as property, but can be deduced from state.
        OnOffType superCoolState, superFreezeState;
        if (dp.Value.equals(String.valueOf(STATE_SUPER_COOLING))) {
            superCoolState = OnOffType.ON;
            superFreezeState = OnOffType.OFF;
        } else if (dp.Value.equals(String.valueOf(STATE_SUPER_FREEZING))) {
            superCoolState = OnOffType.OFF;
            superFreezeState = OnOffType.ON;
        } else {
            superCoolState = OnOffType.OFF;
            superFreezeState = OnOffType.OFF;
        }

        ChannelUID superCoolChannelUid = new ChannelUID(getThing().getUID(), SUPERCOOL_CHANNEL_ID);
        logger.trace("Update state of {} to {} through '{}'", superCoolChannelUid, superCoolState, dp.Name);
        updateState(superCoolChannelUid, superCoolState);

        ChannelUID superFreezeChannelUid = new ChannelUID(getThing().getUID(), SUPERFREEZE_CHANNEL_ID);
        logger.trace("Update state of {} to {} through '{}'", superFreezeChannelUid, superFreezeState, dp.Name);
        updateState(superFreezeChannelUid, superFreezeState);
    }
}
