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
package org.openhab.binding.dali.internal.handler;

import static org.openhab.binding.dali.internal.DaliBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dali.internal.protocol.DaliAddress;
import org.openhab.binding.dali.internal.protocol.DaliDAPCCommand;
import org.openhab.binding.dali.internal.protocol.DaliResponse;
import org.openhab.binding.dali.internal.protocol.DaliStandardCommand;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DaliDeviceHandler} handles commands for things of type Device and Group.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(DaliDeviceHandler.class);
    protected @Nullable Integer targetId;
    protected @Nullable Integer readDeviceTargetId;

    public DaliDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();

        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else {
            updateStatus(ThingStatus.ONLINE);
        }

        final Configuration conf = this.thing.getConfiguration();
        targetId = ((BigDecimal) conf.get(TARGET_ID)).intValueExact();
        // Reading from group addresses does not work generally, so if a fallback device id is
        // defined, use that instead when reading the current state
        if (conf.get(READ_DEVICE_TARGET_ID) != null) {
            readDeviceTargetId = ((BigDecimal) this.thing.getConfiguration().get(READ_DEVICE_TARGET_ID))
                    .intValueExact();
        } else {
            readDeviceTargetId = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (CHANNEL_DIM_AT_FADE_RATE.equals(channelUID.getId())
                    || CHANNEL_DIM_IMMEDIATELY.equals(channelUID.getId())) {
                DaliAddress address;
                if (THING_TYPE_DEVICE.equals(this.thing.getThingTypeUID())
                        || THING_TYPE_DEVICE_DT8.equals(this.thing.getThingTypeUID())) {
                    address = DaliAddress.createShortAddress(targetId);
                } else if (THING_TYPE_GROUP.equals(this.thing.getThingTypeUID())
                        || THING_TYPE_GROUP_DT8.equals(this.thing.getThingTypeUID())) {
                    address = DaliAddress.createGroupAddress(targetId);
                } else {
                    throw new DaliException("unknown device type");
                }

                boolean queryDeviceState = false;

                if (command instanceof PercentType percentCommand) {
                    byte dimmValue = (byte) ((percentCommand.floatValue() * DALI_SWITCH_100_PERCENT) / 100);
                    // A dimm value of zero is handled correctly by DALI devices, i.e. they are turned off
                    getBridgeHandler().sendCommand(new DaliDAPCCommand(address, dimmValue));
                } else if (command instanceof OnOffType onOffCommand) {
                    if (onOffCommand == OnOffType.ON) {
                        getBridgeHandler().sendCommand(new DaliDAPCCommand(address, (byte) DALI_SWITCH_100_PERCENT));
                    } else {
                        getBridgeHandler().sendCommand(DaliStandardCommand.createOffCommand(address));
                    }
                } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
                    if (CHANNEL_DIM_AT_FADE_RATE.equals(channelUID.getId())) {
                        if (increaseDecreaseCommand == IncreaseDecreaseType.INCREASE) {
                            getBridgeHandler().sendCommand(DaliStandardCommand.createUpCommand(address));
                        } else {
                            getBridgeHandler().sendCommand(DaliStandardCommand.createDownCommand(address));
                        }
                    } else {
                        if (increaseDecreaseCommand == IncreaseDecreaseType.INCREASE) {
                            getBridgeHandler().sendCommand(DaliStandardCommand.createStepUpCommand(address));
                        } else {
                            getBridgeHandler().sendCommand(DaliStandardCommand.createStepDownCommand(address));
                        }
                    }
                    queryDeviceState = true;
                } else if (command instanceof RefreshType) {
                    queryDeviceState = true;
                }

                if (queryDeviceState) {
                    DaliAddress readAddress = address;
                    if (readDeviceTargetId != null) {
                        readAddress = DaliAddress.createShortAddress(readDeviceTargetId);
                    }
                    getBridgeHandler()
                            .sendCommandWithResponse(DaliStandardCommand.createQueryActualLevelCommand(readAddress),
                                    DaliResponse.NumericMask.class)
                            .thenAccept(response -> {
                                if (response != null && !response.mask) {
                                    Integer value = response.value != null ? response.value : 0;
                                    int percentValue = (int) (value.floatValue() * 100 / DALI_SWITCH_100_PERCENT);
                                    updateState(channelUID, new PercentType(percentValue));
                                }
                            }).exceptionally(e -> {
                                logger.warn("Error querying device status: {}", e.getMessage());
                                return null;
                            });
                }
            }
        } catch (DaliException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected DaliserverBridgeHandler getBridgeHandler() throws DaliException {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            throw new DaliException("No bridge was found");
        }

        BridgeHandler handler = bridge.getHandler();
        if (handler == null) {
            throw new DaliException("No handler was found");
        }

        return (DaliserverBridgeHandler) handler;
    }
}
