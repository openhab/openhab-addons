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

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * This is a class for handling a Panel type Thing.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class ThingHandlerPanel extends CaddxBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(ThingHandlerPanel.class);
    private @Nullable HashMap<String, String> panelLogMessagesMap = null;
    private @Nullable String communicatorStackPointer = null;

    /**
     * Constructor.
     *
     * @param thing
     */
    public ThingHandlerPanel(Thing thing) {
        super(thing, CaddxThingType.PANEL);
    }

    @Override
    public void updateChannel(ChannelUID channelUID, String data) {
        if (channelUID.getId().equals(CaddxBindingConstants.PANEL_FIRMWARE_VERSION)
                || channelUID.getId().startsWith("panel_log_message_")) {
            updateState(channelUID, new StringType(data));
        } else {
            // All Panel channels are OnOffType
            OnOffType onOffType;

            onOffType = ("true".equals(data)) ? OnOffType.ON : OnOffType.OFF;
            updateState(channelUID, onOffType);
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
            if (CaddxBindingConstants.PANEL_FIRMWARE_VERSION.equals(channelUID.getId())) {
                cmd = CaddxBindingConstants.PANEL_INTERFACE_CONFIGURATION_REQUEST;
                data = "";
            } else if (CaddxBindingConstants.PANEL_LOG_MESSAGE_N_0.equals(channelUID.getId())) {
                cmd = CaddxBindingConstants.PANEL_SYSTEM_STATUS_REQUEST;
                data = "";
            } else {
                return;
            }

            bridgeHandler.sendCommand(cmd, data);
        } else {
            logger.debug("Unknown command {}", command);
        }
    }

    @Override
    public void caddxEventReceived(CaddxEvent event, Thing thing) {
        logger.trace("caddxEventReceived(): Event Received - {}.", event);

        if (getThing().equals(thing)) {
            CaddxMessage message = event.getCaddxMessage();
            CaddxMessageType mt = message.getCaddxMessageType();
            ChannelUID channelUID = null;

            // Log event messages have special handling
            if (CaddxMessageType.SYSTEM_STATUS_MESSAGE.equals(mt)) {
                handleSystemStatusMessage(message);
            } else if (CaddxMessageType.LOG_EVENT_MESSAGE.equals(mt)) {
                handleLogEventMessage(message);
            } else {
                for (CaddxProperty p : mt.properties) {
                    if (!p.getId().isEmpty()) {
                        String value = message.getPropertyById(p.getId());
                        channelUID = new ChannelUID(getThing().getUID(), p.getId());
                        updateChannel(channelUID, value);
                    }
                }
            }

            updateStatus(ThingStatus.ONLINE);
        }
    }

    /*
     * Gets the pointer into the panel's log messages ring buffer
     * and sends the command for the retrieval of the last event_message
     */
    private void handleSystemStatusMessage(CaddxMessage message) {
        // Get the bridge handler
        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        String pointer = message.getPropertyById("panel_communicator_stack_pointer");
        communicatorStackPointer = pointer;

        // build map of log message channels to event numbers
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(pointer, CaddxBindingConstants.PANEL_LOG_MESSAGE_N_0);
        bridgeHandler.sendCommand(CaddxBindingConstants.PANEL_LOG_EVENT_REQUEST, pointer);
        panelLogMessagesMap = map;
    }

    /*
     * This function handles the panel log messages.
     * If the received event_number matches our communication stack pointer then this is the last panel message. The
     * channel gets updated and the required log message requests are generated for the update of the other log message
     * channels
     */
    private void handleLogEventMessage(CaddxMessage message) {
        // Get the bridge handler
        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        String eventNumberString = message.getPropertyById("panel_log_event_number");
        String eventSizeString = message.getPropertyById("panel_log_event_size");

        // build the message
        LogEventMessage logEventMessage = new LogEventMessage(message);

        logger.trace("Log_event: {}", logEventMessage);

        // get the channel id from the map
        HashMap<String, String> logMap = panelLogMessagesMap;
        if (logMap != null && logMap.containsKey(eventNumberString)) {
            String id = logMap.get(eventNumberString);
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), id);
            updateChannel(channelUID, logEventMessage.toString());
        }

        if (communicatorStackPointer != null && eventNumberString.equals(communicatorStackPointer)) {
            HashMap<String, String> map = new HashMap<String, String>();

            int eventNumber = Integer.parseInt(eventNumberString);
            int eventSize = Integer.parseInt(eventSizeString);

            // Retrieve at maximum the 10 last log messages from the panel
            int messagesToRetrieve = Math.min(eventSize, 10);
            for (int i = 1; i < messagesToRetrieve; i++) {
                eventNumber--;
                if (eventNumber < 0) {
                    eventNumber = eventSize;
                }

                map.put(Integer.toString(eventNumber), "panel_log_message_n_" + i);
                bridgeHandler.sendCommand(CaddxBindingConstants.PANEL_LOG_EVENT_REQUEST, Integer.toString(eventNumber));
            }

            communicatorStackPointer = null;
            panelLogMessagesMap = map;
        }
    }
}
