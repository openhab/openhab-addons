/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caddx.internal.CaddxBindingConstants;
import org.openhab.binding.caddx.internal.CaddxEvent;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.openhab.binding.caddx.internal.CaddxMessageContext;
import org.openhab.binding.caddx.internal.CaddxMessageType;
import org.openhab.binding.caddx.internal.CaddxProperty;
import org.openhab.binding.caddx.internal.action.CaddxPanelActions;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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
    private long lastRefreshTime = 0;

    public ThingHandlerPanel(Thing thing) {
        super(thing, CaddxThingType.PANEL);
    }

    @Override
    public void initialize() {
        super.initialize();

        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        String cmd = CaddxBindingConstants.PANEL_SYSTEM_STATUS_REQUEST;
        String data = "";
        bridgeHandler.sendCommand(CaddxMessageContext.COMMAND, cmd, data);
    }

    @Override
    public void updateChannel(ChannelUID channelUID, String data) {
        if (channelUID.getId().equals(CaddxBindingConstants.PANEL_FIRMWARE_VERSION)
                || channelUID.getId().startsWith("panel_log_message_")) {
            updateState(channelUID, new StringType(data));
        } else {
            // All Panel channels are OnOffType
            OnOffType onOffType;

            onOffType = OnOffType.from("true".equals(data));
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
            if (CaddxBindingConstants.PANEL_LOG_MESSAGE_N_0.equals(channelUID.getId())) {
                cmd = CaddxBindingConstants.PANEL_SYSTEM_STATUS_REQUEST;
                data = "";
            } else if (System.currentTimeMillis() - lastRefreshTime > 2000) {
                // Refresh only if 2 seconds have passed from the last refresh
                cmd = CaddxBindingConstants.PANEL_SYSTEM_STATUS_REQUEST;
                data = "";
            } else {
                return;
            }

            bridgeHandler.sendCommand(CaddxMessageContext.COMMAND, cmd, data);
            lastRefreshTime = System.currentTimeMillis();
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

            for (CaddxProperty p : mt.properties) {
                if (!p.getId().isEmpty()) {
                    String value = message.getPropertyById(p.getId());
                    channelUID = new ChannelUID(getThing().getUID(), p.getId());
                    updateChannel(channelUID, value);
                    logger.trace("Updating panel channel: {}", channelUID.getAsString());
                }
            }

            // Log event messages have special handling
            if (CaddxMessageType.SYSTEM_STATUS_MESSAGE.equals(mt)) {
                handleSystemStatusMessage(message);
            } else if (CaddxMessageType.LOG_EVENT_MESSAGE.equals(mt)) {
                handleLogEventMessage(message);
            } else if (CaddxMessageType.ZONES_SNAPSHOT_MESSAGE.equals(mt)) {
                handleZonesSnapshotMessage(message);
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
        HashMap<String, String> map = new HashMap<>();
        map.put(pointer, CaddxBindingConstants.PANEL_LOG_MESSAGE_N_0);
        bridgeHandler.sendCommand(CaddxMessageContext.COMMAND, CaddxBindingConstants.PANEL_LOG_EVENT_REQUEST, pointer);
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
        if (logMap != null) {
            String id = logMap.get(eventNumberString);
            if (id != null) {
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), id);
                updateChannel(channelUID, logEventMessage.toString());
            }
        }

        if (communicatorStackPointer != null && eventNumberString.equals(communicatorStackPointer)) {
            HashMap<String, String> map = new HashMap<>();

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
                bridgeHandler.sendCommand(CaddxMessageContext.COMMAND, CaddxBindingConstants.PANEL_LOG_EVENT_REQUEST,
                        Integer.toString(eventNumber));
            }

            communicatorStackPointer = null;
            panelLogMessagesMap = map;
        }
    }

    private void handleZonesSnapshotMessage(CaddxMessage message) {
        // Get the bridge handler
        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        int zoneOffset = Integer.parseInt(message.getPropertyById("zone_offset"));

        for (int i = 1; i <= 16; i++) {
            int zoneNumber = zoneOffset * 16 + i;

            String zoneFaulted = message.getPropertyById("zone_" + i + "_faulted");
            String zoneBypassed = message.getPropertyById("zone_" + i + "_bypassed");
            String zoneTrouble = message.getPropertyById("zone_" + i + "_trouble");
            String zoneAlarmMemory = message.getPropertyById("zone_" + i + "_alarm_memory");

            logger.debug("Flags for zone {}. faulted:{}, bypassed:{}, trouble:{}, alarm_memory:{}", zoneNumber,
                    zoneFaulted, zoneBypassed, zoneTrouble, zoneAlarmMemory);

            // Get thing
            Thing thing = bridgeHandler.findThing(CaddxThingType.ZONE, null, zoneNumber, null);
            if (thing != null) {
                ChannelUID channelUID;

                logger.debug("Thing found for zone {}.", zoneNumber);

                channelUID = new ChannelUID(thing.getUID(), "zone_faulted");
                updateChannel(channelUID, zoneFaulted);
                channelUID = new ChannelUID(thing.getUID(), "zone_bypassed");
                updateChannel(channelUID, zoneBypassed);
                channelUID = new ChannelUID(thing.getUID(), "zone_trouble");
                updateChannel(channelUID, zoneTrouble);
                channelUID = new ChannelUID(thing.getUID(), "zone_alarm_memory");
                updateChannel(channelUID, zoneAlarmMemory);
            }
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(CaddxPanelActions.class);
    }

    private void sendPrimaryCommand(String pin, String function) {
        String cmd = CaddxBindingConstants.PARTITION_PRIMARY_COMMAND_WITH_PIN;

        // Build the data
        StringBuilder sb = new StringBuilder();
        sb.append("0x").append(pin.charAt(1)).append(pin.charAt(0)).append(",0x").append(pin.charAt(3))
                .append(pin.charAt(2)).append(",0x").append(pin.charAt(5)).append(pin.charAt(4)).append(",")
                .append(function).append(",").append("255");

        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }
        bridgeHandler.sendCommand(CaddxMessageContext.COMMAND, cmd, sb.toString());
    }

    private void sendSecondaryCommand(String function) {
        String cmd = CaddxBindingConstants.PARTITION_SECONDARY_COMMAND;

        // Build the data
        StringBuilder sb = new StringBuilder();
        sb.append(function).append(",").append("255");

        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }
        bridgeHandler.sendCommand(CaddxMessageContext.COMMAND, cmd, sb.toString());
    }

    public void turnOffAnySounderOrAlarm(String pin) {
        sendPrimaryCommand(pin, "0");
    }

    public void disarm(String pin) {
        sendPrimaryCommand(pin, "1");
    }

    public void armInAwayMode(String pin) {
        sendPrimaryCommand(pin, "2");
    }

    public void armInStayMode(String pin) {
        sendPrimaryCommand(pin, "3");
    }

    public void cancel(String pin) {
        sendPrimaryCommand(pin, "4");
    }

    public void initiateAutoArm(String pin) {
        sendPrimaryCommand(pin, "5");
    }

    public void startWalkTestMode(String pin) {
        sendPrimaryCommand(pin, "6");
    }

    public void stopWalkTestMode(String pin) {
        sendPrimaryCommand(pin, "7");
    }

    public void stay() {
        sendSecondaryCommand("0");
    }

    public void chime() {
        sendSecondaryCommand("1");
    }

    public void exit() {
        sendSecondaryCommand("2");
    }

    public void bypassInteriors() {
        sendSecondaryCommand("3");
    }

    public void firePanic() {
        sendSecondaryCommand("4");
    }

    public void medicalPanic() {
        sendSecondaryCommand("5");
    }

    public void policePanic() {
        sendSecondaryCommand("6");
    }

    public void smokeDetectorReset() {
        sendSecondaryCommand("7");
    }

    public void autoCallbackDownload() {
        sendSecondaryCommand("8");
    }

    public void manualPickupDownload() {
        sendSecondaryCommand("9");
    }

    public void enableSilentExit() {
        sendSecondaryCommand("10");
    }

    public void performTest() {
        sendSecondaryCommand("11");
    }

    public void groupBypass() {
        sendSecondaryCommand("12");
    }

    public void auxiliaryFunction1() {
        sendSecondaryCommand("13");
    }

    public void auxiliaryFunction2() {
        sendSecondaryCommand("14");
    }

    public void startKeypadSounder() {
        sendSecondaryCommand("15");
    }
}
