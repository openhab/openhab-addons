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
package org.openhab.binding.caddx.internal.handler;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caddx.internal.CaddxBindingConstants;
import org.openhab.binding.caddx.internal.CaddxEvent;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.openhab.binding.caddx.internal.CaddxMessageContext;
import org.openhab.binding.caddx.internal.CaddxMessageType;
import org.openhab.binding.caddx.internal.CaddxProperty;
import org.openhab.binding.caddx.internal.action.CaddxKeypadActions;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for handling a Keypad type Thing.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class ThingHandlerKeypad extends CaddxBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(ThingHandlerKeypad.class);

    public ThingHandlerKeypad(Thing thing) {
        super(thing, CaddxThingType.KEYPAD);
    }

    @Override
    public void initialize() {
        super.initialize();

        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        // Follow the bridge status
        updateStatus(bridgeHandler.getThing().getStatus());
    }

    @Override
    public void updateChannel(ChannelUID channelUID, String data) {
        if (channelUID.getId().equals(CaddxBindingConstants.KEYPAD_KEY_PRESSED)) {
            StringType stringType = new StringType(data);
            updateState(channelUID, stringType);
        }
    }

    @Override
    public void caddxEventReceived(CaddxEvent event, Thing thing) {
        logger.trace("caddxEventReceived(): Event Received - {}.", event);

        if (getThing().equals(thing)) {
            CaddxMessage message = event.getCaddxMessage();
            CaddxMessageType mt = message.getCaddxMessageType();

            // Log event messages have special handling
            if (CaddxMessageType.KEYPAD_MESSAGE_RECEIVED.equals(mt)) {
                for (CaddxProperty p : mt.properties) {
                    if (!("".equals(p.getId()))) {
                        String value = message.getPropertyById(p.getId());
                        ChannelUID channelUID = new ChannelUID(getThing().getUID(), p.getId());
                        updateChannel(channelUID, value);
                    }
                }
            }

            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // Keypad follows the status of the bridge
        updateStatus(bridgeStatusInfo.getStatus());

        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(CaddxKeypadActions.class);
    }

    public void enterTerminalMode() {
        String cmd = CaddxBindingConstants.KEYPAD_TERMINAL_MODE_REQUEST;
        logger.debug("Address: {}, Seconds: {}", getKeypadAddress(), getTerminalModeSeconds());
        String data = String.format("%d,15", getKeypadAddress(), getTerminalModeSeconds());

        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }
        bridgeHandler.sendCommand(CaddxMessageContext.COMMAND, cmd, data);
    }

    public void sendKeypadTextMessage(String displayLocation, String text) {
        if (text.length() != 8) {
            logger.debug("Text to be displayed on the keypad has not the correct length");
            return;
        }
        String cmd = CaddxBindingConstants.KEYPAD_SEND_KEYPAD_TEXT_MESSAGE;
        String data = String.format("%d,0,%d,%d,%d,%d,%d,%d,%d,%d,%d", getKeypadAddress(), displayLocation,
                text.charAt(0), text.charAt(1), text.charAt(2), text.charAt(3), text.charAt(4), text.charAt(5),
                text.charAt(6), text.charAt(7));

        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }
        bridgeHandler.sendCommand(CaddxMessageContext.COMMAND, cmd, data);
    }
}
