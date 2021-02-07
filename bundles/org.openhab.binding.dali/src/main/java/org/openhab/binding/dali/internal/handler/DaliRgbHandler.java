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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dali.internal.protocol.DaliAddress;
import org.openhab.binding.dali.internal.protocol.DaliDAPCCommand;
import org.openhab.binding.dali.internal.protocol.DaliStandardCommand;
import org.openhab.core.library.types.HSBType;
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
 * The {@link DaliRgbHandler} handles commands for things of type RGB.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliRgbHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(DaliRgbHandler.class);

    static final int Switch100Percent = 254;

    public DaliRgbHandler(Thing thing) {
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

        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
            logger.debug("Initialized device state for rgb {} {}", ThingStatus.OFFLINE,
                    ThingStatusDetail.CONFIGURATION_ERROR);
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Initialized device state for rgb {}", ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            logger.debug("Initialized device state for rgb {} {}", ThingStatus.OFFLINE,
                    ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (CHANNEL_COLOR.equals(channelUID.getId())) {
                List<Integer> outputs = List.of(
                        ((BigDecimal) this.thing.getConfiguration().get(TARGET_ID_R)).intValueExact(),
                        ((BigDecimal) this.thing.getConfiguration().get(TARGET_ID_G)).intValueExact(),
                        ((BigDecimal) this.thing.getConfiguration().get(TARGET_ID_B)).intValueExact());

                if (command instanceof HSBType) {
                    PercentType[] rgb = ((HSBType) command).toRGB();

                    for (int i = 0; i < 3; i++) {
                        byte dimmValue = (byte) ((rgb[i].floatValue() * Switch100Percent) / 100);
                        getBridgeHandler()
                                .sendCommand(new DaliDAPCCommand(DaliAddress.Short(outputs.get(i)), dimmValue));
                    }
                } else if (command instanceof OnOffType) {
                    if ((OnOffType) command == OnOffType.ON) {
                        for (Integer output : outputs) {
                            getBridgeHandler().sendCommand(
                                    new DaliDAPCCommand(DaliAddress.Short(output), (byte) Switch100Percent));
                        }
                    } else {
                        for (Integer output : outputs) {
                            getBridgeHandler().sendCommand(DaliStandardCommand.Off(DaliAddress.Short(output)));
                        }
                    }
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
