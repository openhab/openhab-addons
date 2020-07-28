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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
 * This is a class for handling a Partition type Thing.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class ThingHandlerPartition extends CaddxBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ThingHandlerPartition.class);

    /**
     * Constructor.
     *
     * @param thing
     */
    public ThingHandlerPartition(Thing thing) {
        super(thing, CaddxThingType.PARTITION);
    }

    @Override
    public void updateChannel(ChannelUID channelUID, String data) {
        if (CaddxBindingConstants.PARTITION_SECONDARY_COMMAND.equals(channelUID.getId())) {
            updateState(channelUID, new DecimalType(data));
        } else {
            OnOffType onOffType = ("true".equals(data)) ? OnOffType.ON : OnOffType.OFF;
            updateState(channelUID, onOffType);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand(): Command Received - {} {}.", channelUID, command);

        String cmd = null;
        String data = null;
        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        if (command instanceof RefreshType) {
            if (channelUID.getId().equals(CaddxBindingConstants.PARTITION_ARMED)) {
                cmd = CaddxBindingConstants.PARTITION_STATUS_REQUEST;
                data = String.format("%d", getPartitionNumber() - 1);
            } else {
                return;
            }
        } else if (channelUID.getId().equals(CaddxBindingConstants.PARTITION_SECONDARY_COMMAND)) {
            cmd = channelUID.getId();
            data = String.format("%s,%d", command.toString(), (1 << getPartitionNumber() - 1));
        } else {
            logger.debug("Unknown command {}", command);
            return;
        }

        if (!data.startsWith("-")) {
            bridgeHandler.sendCommand(cmd, data);
        }
    }

    @Override
    public void caddxEventReceived(CaddxEvent event, Thing thing) {
        logger.trace("caddxEventReceived(): Event Received - {}", event);

        if (getThing().equals(thing)) {
            CaddxMessage message = event.getCaddxMessage();
            CaddxMessageType mt = message.getCaddxMessageType();
            ChannelUID channelUID = null;

            for (CaddxProperty p : mt.properties) {
                if (!p.getId().isEmpty()) {
                    String value = message.getPropertyById(p.getId());
                    channelUID = new ChannelUID(getThing().getUID(), p.getId());
                    updateChannel(channelUID, value);
                }
            }

            // Reset the command
            String value = "-1";
            channelUID = new ChannelUID(getThing().getUID(), CaddxBindingConstants.PARTITION_SECONDARY_COMMAND);
            updateChannel(channelUID, value);

            updateStatus(ThingStatus.ONLINE);
        }
    }
}
