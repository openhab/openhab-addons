/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.hdmicec.internal;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HdmiCecEquipmentHandler} is responsible for handling commands,
 * which are sent to one of the channels.
 *
 * @author David Masshardt - Initial contribution
 * @author Sam Spencer - Discovery, Conversion to OH3 and submission
 */
@NonNullByDefault
public class HdmiCecEquipmentHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HdmiCecEquipmentHandler.class);

    private @Nullable HdmiCecBindingConfiguration config;

    // config paramaters
    private String deviceIndex = "unknown"; // hex number, like 0 or e
    private String address = "unkonwn"; // of the form 0.0.0.0

    private @Nullable HdmiCecBridgeHandler bridgeHandler;
    private @Nullable TimerWrapper myTimer = null;

    private ThingUID uid;
    private Bridge bridge;

    public HdmiCecEquipmentHandler(Thing thing) {
        super(thing);
        uid = thing.getUID();
        bridge = getBridge();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        try {

            config = getConfigAs(HdmiCecBindingConfiguration.class);

            if (config != null) {
                if (config.device != null) {
                    z deviceIndex = "" + config.device;
                }
                if (config.address != null) {
                    address = "" + config.address;
                }
            }
            thing.setLabel(thing.getLabel().replace("Equipment", uid.getId()));

            logger.debug("Initializing thing {}", uid);
            bridgeHandler = (HdmiCecBridgeHandler) bridge.getHandler();

            if (bridge.getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } catch (Exception e) {
            logger.error("Error in initialize: {} at {}", e.toString(), e.getStackTrace());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        if (bridgeHandler == null) {
            return;
        }
        if (id.equals(HdmiCecBindingConstants.CHANNEL_POWER)) {
            if (command.equals(OnOffType.ON)) {
                bridgeHandler.sendCommand("on " + getDeviceIndex());
                // Send remote command power on to be sure
                bridgeHandler.sendCommand("txn " + bridgeDeviceIndex() + getDeviceIndex() + ":44:6D");
            } else if (command.equals(OnOffType.OFF)) {
                bridgeHandler.sendCommand("standby " + getDeviceIndex());
                bridgeHandler.sendCommand("txn " + bridgeDeviceIndex() + getDeviceIndex() + ":44:6C");
            }
        } else if (id.equals(HdmiCecBindingConstants.CHANNEL_ACTIVE_SOURCE)) {
            if (command.equals(OnOffType.ON)) {
                bridgeHandler.sendCommand("tx " + bridgeDeviceIndex() + "F:86:" + getAddressAsFrame());
            } else if (command.equals(OnOffType.OFF)) {
                bridgeHandler.sendCommand("tx " + bridgeDeviceIndex() + "F:9D:" + getAddressAsFrame());
            }
        } else if (id.equals(HdmiCecBindingConstants.CHANNEL_SEND)) {
            if (command instanceof StringType) {
                // think about this, do we want to have a controlled vocabulary
                // or just transmit something raw, or both?
                bridgeHandler.sendCommand(command.toString());
            }
        } else if (id.equals(HdmiCecBindingConstants.CHANNEL_SEND_CEC)) {
            /* Sends message from bridge device to the target device index */
            if (command instanceof StringType) {
                bridgeHandler.sendCommand("tx " + bridgeDeviceIndex() + deviceIndex + ":" + command.toString());
            }
        } else if (channelUID.getId().equals(HdmiCecBindingConstants.CHANNEL_REMOTE_BUTTON)) {
            if (command instanceof StringType) {
                sendRemoteButton(command.toString());
            }
        }
    }

    private void sendRemoteButton(String command) {
        if (myTimer != null) {
            myTimer.cancel();
        }
        myTimer = new TimerWrapper();
        String opcode = RemoteButtonCode.opcodeFromString(command);
        bridgeHandler.sendCommand("txn " + bridgeDeviceIndex() + deviceIndex + ":44:" + opcode);
        // myTimer.schedule(250, () -> {
        bridgeHandler.sendCommand("txn " + bridgeDeviceIndex() + deviceIndex + ":45");
        // });
    }

    private @Nullable String bridgeDeviceIndex() {
        return (bridgeHandler != null) ? bridgeHandler.getBridgeIndex() : null;
    }

    public String getDeviceIndex() {
        return deviceIndex;
    }

    public String getAddress() {
        return address;
    }

    public String getAddressAsFrame() {
        return address.replace(".", "").substring(0, 2) + ":" + address.replace(".", "").substring(2);
    }

    public void cecClientStatus(boolean online, String status) {
        if (online) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, status);
        }
        logger.debug("Cec client status: online = {} status = {}", online, status);
    }

    void cecMatchLine(String line) {
        Matcher matcher = bridgeHandler.getPowerOn().matcher(line);
        if (matcher.matches()) {
            updateState(HdmiCecBindingConstants.CHANNEL_POWER, OnOffType.ON);
            return;
        }
        matcher = bridgeHandler.getPowerOff().matcher(line);
        if (matcher.matches()) {
            updateState(HdmiCecBindingConstants.CHANNEL_POWER, OnOffType.OFF);
            return;
        }
        matcher = bridgeHandler.getActiveSourceOn().matcher(line);
        if (matcher.matches()) {
            updateState(HdmiCecBindingConstants.CHANNEL_ACTIVE_SOURCE, OnOffType.ON);
            return;
        }
        matcher = bridgeHandler.getActiveSourceOff().matcher(line);
        if (matcher.matches()) {
            updateState(HdmiCecBindingConstants.CHANNEL_ACTIVE_SOURCE, OnOffType.OFF);
            return;
        }
        matcher = bridgeHandler.getEventPattern().matcher(line);
        if (matcher.matches()) {
            triggerChannel(HdmiCecBindingConstants.CHANNEL_EVENT, matcher.group(2));
            return;
        }
    }

    public void setActiveStatus(boolean status) {
        updateState(HdmiCecBindingConstants.CHANNEL_ACTIVE_SOURCE, status ? OnOffType.ON : OnOffType.OFF);
    }
}

/**
 * Wrapper for Timer that enables a lambda, which makes passing params much
 * easier
 *
 * @author Sam Spencer - Initial contribution
 */
@NonNullByDefault
class TimerWrapper {
    private final Timer t = new Timer();

    public TimerTask schedule(long delay, final Runnable r) {
        t.purge();
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        };
        t.schedule(task, delay);
        return task;
    }

    public void cancel() {
        t.cancel();
    }
}
