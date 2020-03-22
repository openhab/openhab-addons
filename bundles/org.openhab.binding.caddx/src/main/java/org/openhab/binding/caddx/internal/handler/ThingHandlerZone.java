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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
            StringBuilder s = new StringBuilder(data.length());

            CharacterIterator it = new StringCharacterIterator(data);
            for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
                switch (ch) {
                    case 0xb7:
                        s.append('Γ');
                        break;
                    case 0x10:
                        s.append('Δ');
                        break;
                    case 0x13:
                        s.append('Θ');
                        break;
                    case 0x14:
                        s.append('Λ');
                        break;
                    case 0x12:
                        s.append('Ξ');
                        break;
                    case 0xc8:
                        s.append('Π');
                        break;
                    case 0x16:
                        s.append('Σ');
                        break;
                    case 0xcc:
                        s.append('Φ');
                        break;
                    case 0x17:
                        s.append('Ψ');
                        break;
                    case 0x15:
                        s.append('Ω');
                        break;
                    default:
                        s.append(ch);
                        break;
                }
            }

            String value = s.toString();
            getThing().setLabel(value);
            updateState(channelUID, new StringType(value));

            logger.trace("  updateChannel: {} = {}", channelUID, value);
        } else {
            // All Zone channels are OnOffType
            OnOffType onOffType;

            onOffType = ("true".equals(data)) ? OnOffType.ON : OnOffType.OFF;
            updateState(channelUID, onOffType);

            logger.trace("  updateChannel: {} = {}", channelUID, onOffType);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand(): Command Received - {} {}.", channelUID, command);

        String cmd = null;
        String data = null;
        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        if (command instanceof RefreshType) {
            if (channelUID.getId().equals(CaddxBindingConstants.ZONE_FAULTED)) {
                cmd = CaddxBindingConstants.ZONE_STATUS_REQUEST;
                data = String.format("%d", getZoneNumber() - 1);
            } else if (channelUID.getId().equals(CaddxBindingConstants.ZONE_NAME)) {
                cmd = CaddxBindingConstants.ZONE_NAME_REQUEST;
                data = String.format("%d", getZoneNumber() - 1);
            } else {
                return;
            }
        } else if (channelUID.getId().equals(CaddxBindingConstants.ZONE_BYPASS_TOGGLE)) {
            cmd = channelUID.getId();
            data = String.format("%d", getZoneNumber() - 1);
        } else {
            logger.debug("Unknown command");
            return;
        }

        bridgeHandler.sendCommand(cmd, data);
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

                if (!("".equals(p.getId()))) {
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
