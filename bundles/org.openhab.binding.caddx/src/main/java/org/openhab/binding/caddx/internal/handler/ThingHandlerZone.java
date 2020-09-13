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
package org.openhab.binding.caddx.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.caddx.internal.CaddxBindingConstants;
import org.openhab.binding.caddx.internal.CaddxEvent;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.openhab.binding.caddx.internal.CaddxMessageType;
import org.openhab.binding.caddx.internal.CaddxProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for handling a Zone type Thing.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class ThingHandlerZone extends CaddxBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ThingHandlerZone.class);

    /**
     * Constructor.
     *
     * @param thing
     */
    public ThingHandlerZone(Thing thing) {
        super(thing, CaddxThingType.ZONE);
    }

    @Override
    public void updateChannel(ChannelUID channelUID, String data) {
        if (channelUID.getId().equals(CaddxBindingConstants.ZONE_NAME)) {
            getThing().setLabel(data);
            updateState(channelUID, new StringType(data));

            logger.trace("  updateChannel: {} = {}", channelUID, data);
        } else if (channelUID.getId().equals(CaddxBindingConstants.ZONE_FAULTED)) {
            OpenClosedType openClosedType = ("true".equals(data)) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            updateState(channelUID, openClosedType);

            logger.trace("  updateChannel: {} = {}", channelUID, data);
        } else {
            OnOffType onOffType = ("true".equals(data)) ? OnOffType.ON : OnOffType.OFF;
            updateState(channelUID, onOffType);

            logger.trace("  updateChannel: {} = {}", channelUID, onOffType);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand(): Command Received - {} {}.", channelUID, command);

        String cmd1 = null;
        String cmd2 = null;
        String data = null;

        if (command instanceof RefreshType) {
            if (channelUID.getId().equals(CaddxBindingConstants.ZONE_FAULTED)) {
                cmd1 = CaddxBindingConstants.ZONE_STATUS_REQUEST;
                cmd2 = CaddxBindingConstants.ZONE_NAME_REQUEST;
                data = String.format("%d", getZoneNumber() - 1);
            } else {
                return;
            }
        } else if (channelUID.getId().equals(CaddxBindingConstants.ZONE_BYPASSED)) {
            cmd1 = channelUID.getId();
            cmd2 = CaddxBindingConstants.ZONE_STATUS_REQUEST;
            data = String.format("%d", getZoneNumber() - 1);
        } else {
            logger.debug("Unknown command {}", command);
            return;
        }

        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }
        bridgeHandler.sendCommand(cmd1, data);
        bridgeHandler.sendCommand(cmd2, data);
    }

    @Override
    public void caddxEventReceived(CaddxEvent event, Thing thing) {
        logger.trace("caddxEventReceived(): Event Received - {}", event);

        if (getThing().equals(thing)) {
            CaddxMessage message = event.getCaddxMessage();
            CaddxMessageType mt = message.getCaddxMessageType();
            ChannelUID channelUID = null;

            for (CaddxProperty p : mt.properties) {
                logger.trace("  Checking property: {}", p.getName());

                if (!p.getId().isEmpty()) {
                    String value = message.getPropertyById(p.getId());
                    channelUID = new ChannelUID(getThing().getUID(), p.getId());
                    updateChannel(channelUID, value);

                    logger.trace("  updateChannel: {} = {}", channelUID, value);
                }
            }

            updateStatus(ThingStatus.ONLINE);
        }
    }
}
