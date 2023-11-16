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
package org.openhab.binding.caddx.internal.handler;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caddx.internal.CaddxBindingConstants;
import org.openhab.binding.caddx.internal.CaddxEvent;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.openhab.binding.caddx.internal.CaddxMessageContext;
import org.openhab.binding.caddx.internal.CaddxMessageType;
import org.openhab.binding.caddx.internal.CaddxProperty;
import org.openhab.binding.caddx.internal.action.CaddxPartitionActions;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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
    private long lastRefreshTime = 0;

    public ThingHandlerPartition(Thing thing) {
        super(thing, CaddxThingType.PARTITION);
    }

    @Override
    public void initialize() {
        super.initialize();

        CaddxBridgeHandler bridgeHandler = getCaddxBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        String cmd = CaddxBindingConstants.PARTITION_STATUS_REQUEST;
        String data = String.format("%d", getPartitionNumber() - 1);
        bridgeHandler.sendCommand(CaddxMessageContext.COMMAND, cmd, data);
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
            // Refresh only if 2 seconds have passed from the last refresh
            if (System.currentTimeMillis() - lastRefreshTime > 2000) {
                cmd = CaddxBindingConstants.PARTITION_STATUS_REQUEST;
                data = String.format("%d", getPartitionNumber() - 1);
            } else {
                return;
            }
            lastRefreshTime = System.currentTimeMillis();
        } else if (channelUID.getId().equals(CaddxBindingConstants.PARTITION_SECONDARY_COMMAND)) {
            cmd = channelUID.getId();
            data = String.format("%s,%d", command.toString(), (1 << getPartitionNumber() - 1));
        } else {
            logger.debug("Unknown command {}", command);
            return;
        }

        if (!data.startsWith("-")) {
            bridgeHandler.sendCommand(CaddxMessageContext.COMMAND, cmd, data);
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
                    logger.trace("Updating partition channel: {}", channelUID.getAsString());
                }
            }

            // Reset the command
            String value = "-1";
            channelUID = new ChannelUID(getThing().getUID(), CaddxBindingConstants.PARTITION_SECONDARY_COMMAND);
            updateChannel(channelUID, value);

            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(CaddxPartitionActions.class);
    }

    private void sendPrimaryCommand(String pin, String function) {
        String cmd = CaddxBindingConstants.PARTITION_PRIMARY_COMMAND_WITH_PIN;

        // Build the data
        StringBuilder sb = new StringBuilder();
        sb.append("0x").append(pin.charAt(1)).append(pin.charAt(0)).append(",0x").append(pin.charAt(3))
                .append(pin.charAt(2)).append(",0x").append(pin.charAt(5)).append(pin.charAt(4)).append(",")
                .append(function).append(",").append(Integer.toString(1 << getPartitionNumber() - 1));

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
        sb.append(function).append(",").append(Integer.toString(1 << getPartitionNumber() - 1));

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
