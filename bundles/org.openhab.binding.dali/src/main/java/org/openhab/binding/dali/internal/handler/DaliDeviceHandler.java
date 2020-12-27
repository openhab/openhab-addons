/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.openhab.binding.dali.internal.protocol.DaliAddress;
import org.openhab.binding.dali.internal.protocol.DaliDAPCCommand;
import org.openhab.binding.dali.internal.protocol.DaliResponse;
import org.openhab.binding.dali.internal.protocol.DaliStandardCommand;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
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

    static final int Switch100Percent = 254;

    public DaliDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        initDeviceState();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.info("Bridge status changed to {} updating {}", bridgeStatusInfo.getStatus(), getThing().getLabel());

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            initDeviceState();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    public void initDeviceState() {
        Bridge bridge = getBridge();

        String type = "unknown";
        if (THING_TYPE_DEVICE.equals(this.thing.getThingTypeUID())) {
            type = "device";
        } else if (THING_TYPE_GROUP.equals(this.thing.getThingTypeUID())) {
            type = "group";
        }

        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
            logger.debug("Initialized device state for {} {} {}", type, ThingStatus.OFFLINE,
                    ThingStatusDetail.CONFIGURATION_ERROR);
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Initialized device state for {} {}", type, ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            logger.debug("Initialized device state for {} {} {}", type, ThingStatus.OFFLINE,
                    ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (CHANNEL_DIM_AT_FADE_RATE.equals(channelUID.getId())
                    || CHANNEL_DIM_IMMEDIATELY.equals(channelUID.getId())) {
                Integer targetId = ((BigDecimal) this.thing.getConfiguration().get(TARGET_ID)).intValueExact();

                DaliAddress address;
                if (THING_TYPE_DEVICE.equals(this.thing.getThingTypeUID())) {
                    address = DaliAddress.Short(targetId);
                } else if (THING_TYPE_GROUP.equals(this.thing.getThingTypeUID())) {
                    address = DaliAddress.Group(targetId);
                } else {
                    throw new DaliException("unknown device type");
                }

                boolean queryDeviceState = false;

                if (command instanceof PercentType) {
                    byte dimmValue = (byte) ((((PercentType) command).floatValue() * Switch100Percent) / 100);
                    getBridgeHandler().sendCommand(new DaliDAPCCommand(address, dimmValue));
                } else if (command instanceof OnOffType) {
                    if ((OnOffType) command == OnOffType.ON) {
                        getBridgeHandler().sendCommand(new DaliDAPCCommand(address, (byte) Switch100Percent));
                    } else {
                        getBridgeHandler().sendCommand(DaliStandardCommand.Off(address));
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    if (CHANNEL_DIM_AT_FADE_RATE.equals(channelUID.getId())) {
                        if ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE) {
                            getBridgeHandler().sendCommand(DaliStandardCommand.Up(address));
                        } else {
                            getBridgeHandler().sendCommand(DaliStandardCommand.Down(address));
                        }
                    } else {
                        if ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE) {
                            getBridgeHandler().sendCommand(DaliStandardCommand.StepUp(address));
                        } else {
                            getBridgeHandler().sendCommand(DaliStandardCommand.StepDown(address));
                        }
                    }
                    queryDeviceState = true;
                }

                if (queryDeviceState) {
                    getBridgeHandler().sendCommandWithResponse(DaliStandardCommand.QueryActualLevel(address),
                            DaliResponse.NumericMask.class).thenAccept(r -> {
                                if (r != null && r.mask == false) {
                                    Integer value = r.value != null ? r.value : 0;
                                    int percentValue = (int) (value.floatValue() * 100 / Switch100Percent);
                                    updateState(channelUID, new PercentType(percentValue));
                                }
                            });
                }
            }
        } catch (DaliException e) {
            logger.warn("Error handling command: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected DaliserverBridgeHandler getBridgeHandler() {
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
