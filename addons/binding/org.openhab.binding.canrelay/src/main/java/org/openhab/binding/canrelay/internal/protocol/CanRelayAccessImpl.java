/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.protocol;

import static org.eclipse.smarthome.core.library.types.OnOffType.*;
import static org.openhab.binding.canrelay.internal.CanRelayBindingConstants.nodeAsString;
import static org.openhab.binding.canrelay.internal.canbus.CanBusDeviceStatus.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.canrelay.internal.canbus.CanBusCommunicationException;
import org.openhab.binding.canrelay.internal.canbus.CanBusDevice;
import org.openhab.binding.canrelay.internal.canbus.CanBusDeviceListener;
import org.openhab.binding.canrelay.internal.canbus.CanBusDeviceStatus;
import org.openhab.binding.canrelay.internal.canbus.CanMessage;
import org.openhab.binding.canrelay.internal.canbus.USBTinDevice;
import org.openhab.binding.canrelay.internal.protocol.CanRelayDetection.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of CanRelayAccess using the USBTin can bus device implementation.
 * See https://github.com/PoJD/can for details about the custom protocol
 *
 * @author Lubos Housa - Initial contribution
 */
@Component(service = CanRelayAccess.class, immediate = true, configurationPid = "access.canrelay")
@NonNullByDefault
public class CanRelayAccessImpl implements CanRelayAccess, CanBusDeviceListener {

    private static final Logger logger = LoggerFactory.getLogger(CanRelayAccessImpl.class);

    /**
     * these values represent databytes to be sent across CANBUS to indicate switch ON or OFF operation for the given
     * node or GET operation for all nodes or default operation (used e.g. in mapping request where the data has no
     * impact
     */
    private static final short CAN_DATA_SWITCH_ON = 0x40;
    private static final short CAN_DATA_SWITCH_OFF = 0x80;
    private static final short CAN_DATA_SWITCH_TOGGLE = 0x00;
    private static final short CAN_DATA_GET_ALL = 0xC0;
    private static final short CAN_DATA_DEFAULT = 0;

    /**
     * These are used to compose the canID and the solution was designed to allow complex message types from some home
     * automation system. It is designed so that the underlying network works on its own, uses lower CAN IDs
     * and thus physical switches in the wall would always have precedence against any automation sending "COMPLEX"
     * traffic over. This message type is used for both switch commands and detection of status of all lights too.
     *
     * Response would always be just 1 CanMessage
     */
    private static final short CAN_MESSAGETYPE_COMPLEX_REQUEST = 0x300;
    private static final short CAN_MESSAGETYPE_COMPLEX_RESPONSE = 0x400;

    /**
     * Normal message types are only transmitted over the CANBUS by physical light switches (CanSwitch device)
     */
    private static final short CAN_MESSAGETYPE_NORMAL_REQUEST = 0x000;

    /**
     * Mapping request and response message types. Mind that the mapping responses would very likely be composed of more
     * than 1 CamNessage since a given CanMessage can only carry up to 8 bytes of data. The last message would then
     * contain a special data byte used as a marker (the last data byte, in fact 2)
     */
    private static final short CAN_MESSAGETYPE_MAPPING_REQUEST = 0x500;
    private static final short CAN_MESSAGETYPE_MAPPING_RESPONSE = 0x600;
    private static final short CAN_DATA_LAST_MAPPING_MESSAGE_MARKER = 0xFF;

    /**
     * Baudrate of the CANBUS
     */
    private static final int CAN_BAUDRATE = 50000;

    /**
     * Max 8 bit value used when parsing incoming can messages
     */
    private static final short NODE_ID_MAX_VALUE = 0xFF;

    /**
     * Configured device within this canRelay
     */
    private final CanBusDevice device;

    /**
     * Current can bus device status
     */
    CanBusDeviceStatus status = UNITIALIZED;

    /**
     * used during the detection phase for various data to be received by multiple listeners and threads
     */
    private final CanRelayDetection detection = new CanRelayDetection();

    /**
     * Listeners for canrelay events as detected on the CANBUS
     */
    private final List<CanRelayChangeListener> listeners = new LinkedList<>();

    public CanRelayAccessImpl(CanBusDevice device) {
        this.device = device;
        this.device.registerCanBusDeviceListener(this);
    }

    public CanRelayAccessImpl() {
        this(new USBTinDevice());
    }

    /*
     * Private methods
     */

    private boolean sendCanMessage(CanMessage message) {
        try {
            device.send(message);
            return true;
        } catch (CanBusCommunicationException e) {
            logger.warn(
                    "Error sending CAN message to CanRelay, most likely this operation/command was not reflected on the CANBUS",
                    e);
            return false;
        }
    }

    private void processCanRelayMappingReplies() {
        try {
            // update status to avoid getting sporadic can traffic now when processing the messages
            detection.processingMappingReplies();
            LightStateCache lightStateCache = detection.getLightStateCache();
            // so now check all the CanMessages and take always 2 pairs of data - 1 = nodeID, 2nd = output light
            for (CanMessage message : detection.getReceivedCanMessages()) {
                List<Short> data = message.getData();
                for (int i = 0; i < data.size() - 1; i += 2) {
                    short nodeID = data.get(i);
                    short lightNum = data.get(i + 1);
                    // just skip the last message marker
                    if (lightNum != CAN_DATA_LAST_MAPPING_MESSAGE_MARKER) {
                        lightStateCache.addMapping(detection.getFloor(), nodeID, lightNum);
                    }
                }
            }
        } finally {
            // remove the messages now since we are done with processing these
            detection.getReceivedCanMessages().clear();
        }
    }

    private void processCanRelayOutputsReply() {
        try {
            // update status to avoid getting sporadic can traffic now when processing the messages
            detection.processingOutputReply();
            // we should only really see 1 message, so just take the first if present
            // check is to just be super sure, but we should only get here if a message was received anyway
            if (detection.getReceivedCanMessages().isEmpty()) {
                return;
            }

            // in case someone managed to sneak in more CANMessages, always take the first one, assuming that should be
            // the first one after we sent the request out
            CanMessage message = detection.getReceivedCanMessages().poll();
            List<Short> data = message.getData();
            if (data.size() < 8) {
                logger.warn(
                        "Invalid outputs response message received. Expected 8 bytes of data, but only got {} bytes in message {}",
                        data.size(), message);
                return;
            }

            // see https://github.com/PoJD/can, COMPLEX REPLY in CAN DATA section
            short outputsCount = data.get(0);
            if (outputsCount > 32) {
                // 32 is the max count we can keep in 4 8bits values below, so just checking that. Current CanRelay
                // supports only 30 anyway, so if this is to change, both this check and below logic would need to be
                // updated
                logger.warn("Invalid outputs response. Output count is {}, expected a value between 0-32",
                        outputsCount);
                return;
            }
            // each output represents 1 bit here, so up to 30bits top, form a long (could do with int too, but would
            // need to move the bits more ackwardly, now simply use 32bits with the lowest 2 bits always ignored)
            long outputs = ((long) data.get(1) << 24) + ((long) data.get(2) << 16) + ((long) data.get(3) << 8)
                    + data.get(4);
            short txErrorCount = data.get(5);
            short rxErrorCount = data.get(6);
            short firmwareVersion = data.get(7);
            if (txErrorCount + rxErrorCount > 0) {
                logger.warn(
                        "CanRelay for floor {} has errors in CANBUS transmit (TX) or receive (RX) registry. txErrorCount: {}, rxErrorCount: {}",
                        detection.getFloor(), txErrorCount, rxErrorCount);
            }

            if (logger.isDebugEnabled()) {
                // so we now have 32 bits of data and want just the highest outputsCount bits as a binary String
                String outputsInBinaryString = String
                        .format("%" + outputsCount + "s", Long.toBinaryString(outputs >> (32 - outputsCount)))
                        .replace(' ', '0');
                logger.debug(
                        "Parsed outputs reply message. Output count {}, outputs {}, txErrorCount {}, rxErrorCount {}",
                        outputsCount, outputsInBinaryString, txErrorCount, rxErrorCount);
                logger.debug("This CanRelay (floor {}) reports firmware version {}", detection.getFloor(),
                        firmwareVersion);
            }

            // now loop through all the bits in outputs and call the runtime configuration respectively
            // lights are 1-index based in CanRelay (matching the silkscreen, so act accordingly)
            for (int light = 1; light <= outputsCount; light++) {
                OnOffType lightState = (((outputs >> (32 - light)) & 1) == 1) ? ON : OFF;
                detection.getLightStateCache().updateLightUsingLightNum(detection.getFloor(), light, lightState);
            }
        } finally {
            // remove the message(s) now since we are done with processing these
            detection.getReceivedCanMessages().clear();
        }
    }

    private boolean isDeviceReady() {
        if (device.getStatus() != CONNECTED) {
            logger.debug("Waiting for the HW bridge device to get ready.");
            detection.waitForDevice();
            if (detection.getStatus() == Status.CANCELLED) {
                logger.debug("Cancelling this background detection as requested (just resumed from wait for device)");
                return false;
            }
            if (device.getStatus() != CONNECTED) {
                logger.warn("HW bridge device still not ready, no lights would be found in this scan.");
                return false;
            }
            logger.debug("Device is ready now.");
        }
        return true;
    }

    private boolean getCanRelayMappings() {
        logger.debug("Sending mapping request to the CanRelay for FLOOR {}", detection.getFloor());

        if (!sendCanMessage(
                CanMessage.newBuilder().id(CAN_MESSAGETYPE_MAPPING_REQUEST + detection.getFloor().getValue())
                        .withDataByte(CAN_DATA_DEFAULT).build())) {
            return false;
        }

        logger.debug("Waiting for Mapping responses from the CanRelay (can be more than 1 CanMessage).");
        detection.waitForMappingReplies();
        if (detection.getStatus() == Status.CANCELLED) {
            logger.debug(
                    "Cancelling this background detection as requested (just resumed from wait for mapping replies)");
            return false;
        }

        return true;
    }

    private boolean getCanRelayOutputs() {
        logger.debug("Sending outputs state request to CanRelay for FLOOR {}", detection.getFloor());

        if (!sendCanMessage(
                CanMessage.newBuilder().id(CAN_MESSAGETYPE_COMPLEX_REQUEST + detection.getFloor().getValue())
                        .withDataByte(CAN_DATA_GET_ALL).build())) {
            return false;
        }

        logger.debug("Waiting for CanRelay outputs state response");
        detection.waitForOutputsReply();
        if (detection.getStatus() == Status.CANCELLED) {
            logger.debug("Cancelling this background detection as requested (just resumed from wait for outputs)");
            return false;
        }

        return true;
    }

    private void notifyListeners(int nodeID, OnOffType state) {
        // safe to iterate over this, this method can only be called from one thread since this is called from the
        // serial port event and that would be always just 1 thread anyway
        listeners.forEach((listener) -> listener.onLightSwitchChanged(nodeID, state));
    }

    private void notifyListenersCanRelayOffline(String error) {
        listeners.forEach((listener) -> listener.onCanRelayOffline(error));
    }

    private void processStandardMessage(CanMessage canMessage) {
        // only interested in NORMAL messages for all floors, so just check first the highest 3 bits being equal
        // to NORMAL_MESSAGE_TYPE
        short canID = canMessage.getId();
        if ((canID & 0b11100000000) == CAN_MESSAGETYPE_NORMAL_REQUEST) {
            logger.debug("Received {} with NORMAL message type. Processing", canMessage);
            // lowest 8 bits then represent the nodeID, so take it
            int nodeID = canID & NODE_ID_MAX_VALUE;
            if (canMessage.getData().isEmpty()) {
                logger.debug("Received normal message type has no data bytes, ignoring the {}", canMessage);
                return;
            }

            OnOffType command = null;
            // highest 2 bits of data represent the message type, ignore the rest of the data byte
            int operation = canMessage.getData().get(0) & 0b11000000;
            switch (operation) {
                case CAN_DATA_SWITCH_ON:
                    command = ON;
                    break;
                case CAN_DATA_SWITCH_OFF:
                    command = OFF;
                    break;
                case CAN_DATA_SWITCH_TOGGLE:
                    // keep command as null in this case since TOGGLE does not exist in OnOffType :)
                    break;
                default:
                    logger.debug(
                            "Received {} is not ON/OFF or TOGGLE operation, ignoring. Detected operation 0b{} ignored",
                            canMessage, Integer.toBinaryString(operation));
                    return;
            }

            // ok we found valid command changing lights (not for example get operation)
            String nodeString = nodeAsString(nodeID);

            // try to see if nodeID does not happen to be the floor exactly
            Floor floor = Floor.fromValue(nodeID);
            if (floor != null) {
                // if so, then we know we have to do the command for all lights for that floor
                // if the command is toggle, openHAB does not support that for a switch at the moment, so the cache
                // would return a map of nodeID->OnOffType command we can later traverse through to notify all listeners
                logger.debug(
                        "This message is for floor {}, meaning the command '{}' would be invoked for all lights on that floor",
                        floor, command);
                detection.getLightStateCache().updateAllLights(command, floor).forEach((n, c) -> notifyListeners(n, c));
                return;
            }

            // ok, so nodeID is not equal to floor, so we have just 1 individual light state to update
            logger.debug("Updating light {} using command {}", nodeString, command);
            command = detection.getLightStateCache().updateLight(nodeID, command);
            if (command == null) {
                logger.debug("Light {} not found, not updating it.", nodeString);
                return;
            }

            // light found, so notify the listeners about the message then
            notifyListeners(nodeID, command);
        }
    }

    private void processMappingsMessage(CanMessage canMessage) {
        // only check for mapping responses and if received, proceed and wake up the waiting detection in case last
        // CanMessage with mappings was received.
        if (canMessage.getId() == CAN_MESSAGETYPE_MAPPING_RESPONSE + detection.getFloor().getValue()) {
            logger.debug("Received mapping response {} from CanRelay for floor {}", canMessage, detection.getFloor());
            detection.canMessageReceived(canMessage);

            // check if the message contains the "last marker" and only in that case wake up the waiting
            // detection
            if (!canMessage.getData().isEmpty() && CAN_DATA_LAST_MAPPING_MESSAGE_MARKER == canMessage.getData()
                    .get(canMessage.getData().size() - 1)) {
                logger.debug(
                        "Received mapping response contains last message marker, letting the scan to proceed, we know all the mappings now");
                detection.signalMappingReplies();
            }
        }
    }

    private void processOutputsMessage(CanMessage canMessage) {
        // only interested in complex response message types
        if (canMessage.getId() == CAN_MESSAGETYPE_COMPLEX_RESPONSE + detection.getFloor().getValue()) {
            logger.debug("Received outputs state response {} from CanRelay for floor {}", canMessage,
                    detection.getFloor());
            detection.canMessageReceived(canMessage);
            detection.signalOutputsReply();
        }
    }

    private synchronized Collection<LightState> detectLightStates(boolean update) {
        try {
            long start = System.currentTimeMillis();
            if (!update) {
                // erase any potential stored light states from before unless this is an update only detection
                detection.clear();
            }
            // simply try to detect lights for both floors
            for (Floor floor : Floor.values()) {
                logger.debug("Detecting CanRelay light states for floor {}", floor);

                detection.start(floor);
                LightStateCache lightStateCache = detection.getLightStateCache();
                if (!isDeviceReady()) {
                    return new ArrayList<LightState>();
                }

                // for updates skip mappings, assume we already know these
                if (!update) {
                    if (!getCanRelayMappings()) {
                        continue;
                    }
                    processCanRelayMappingReplies();

                    // if no result found so far from mapping, do not continue with outputs
                    if (lightStateCache.isEmpty()) {
                        logger.debug("No mappings found for CanRelay for floor {}. Skipping outputs detection", floor);
                        continue;
                    }
                }
                if (!getCanRelayOutputs()) {
                    // we may have received some mappings before, but do not know their state, so rather erase and
                    // try next floor since otherwise things/lights may be detected in wrong state
                    lightStateCache.clear();
                    continue;
                }
                processCanRelayOutputsReply();

                // all fine, so now we should have the respective light state cache updated
                detection.finishedProcessingFloor();
            }
            logger.debug("Finished detecting light states in {} ms. ", System.currentTimeMillis() - start);
        } finally {
            detection.finish();
        }
        return detection.getLightStateCache().getAllLights();
    }

    @Override
    public CanBusDeviceStatus connect(String portName) {
        logger.debug("Attempting to connect device to port {} with CANBUS baudrate {}", portName, CAN_BAUDRATE);

        this.status = device.connect(portName, CAN_BAUDRATE);
        logger.debug("Attempt to connect finished. Status of the device: {}", status);
        return status;
    }

    @Override
    public void disconnect() {
        logger.debug("Disconnecting the underlying device and cancelling any potentially waiting background tasks.");
        try {
            detection.cancel();
            device.disconnect();
            listeners.clear();
        } finally {
            this.status = UNITIALIZED;
        }
        logger.debug("Disconnected.");
    }

    @Override
    public CanBusDeviceStatus getStatus() {
        return this.status;
    }

    @Override
    public Collection<LightState> detectLightStates() {
        return detectLightStates(false);
    }

    @Override
    public void initCache() {
        if (detection.isCacheEmpty()) {
            // if cache not yet prepared (perhaps after restart and bridge restored in openHAB from persistence) do
            // detect the lights and trigger updates in the listeners
            logger.debug("Initializing internal light cache and updating UI accordingly now...");
            detectLightStates(false)
                    .forEach((lightState) -> notifyListeners(lightState.getNodeID(), lightState.getState()));
        }
    }

    @Override
    public Collection<LightState> refreshCache() {
        logger.debug("Refreshing cache...");
        // need to do a deep clone
        List<LightState> oldLights = detection.getLightStateCache().getAllLights().stream()
                .map(lightState -> new LightState(lightState)).collect(Collectors.toList());
        detectLightStates(true);

        // now remove all light states present in old list from the new list, so we are left only with new list light
        // states that differ to the old state (or were not present at all in old state, but these would then be ignored
        // later anyway
        List<LightState> result = detection.getLightStateCache().getAllLights().stream()
                .filter((lightState) -> !oldLights.contains(lightState)).collect(Collectors.toList());
        logger.debug("Cache refreshed. List of outdated lights: {}", result);
        return result;
    }

    @Override
    public boolean handleSwitchCommand(int nodeID, OnOffType command) {
        logger.debug("About to switch {} the light for nodeID {}", command, nodeAsString(nodeID));

        if (device.getStatus() != CONNECTED) {
            logger.warn(
                    "CanBusDevice is not ready to receive traffic. Either connect was not called or it failed or the HW bridge has some issues. Ignoring the handleSwitchCommand for nodeID {} to switch the light {}",
                    nodeAsString(nodeID), command);
            return false;
        }
        // canID - highest 3 bits = message type, lower 8 bits = nodeID
        // can data = just the flag as per the constants
        boolean result = sendCanMessage(CanMessage.newBuilder().id(CAN_MESSAGETYPE_COMPLEX_REQUEST + nodeID)
                .withDataByte(command == ON ? CAN_DATA_SWITCH_ON : CAN_DATA_SWITCH_OFF).build());

        // if success, then update cache too
        if (result) {
            logger.debug("The switch {} command for the light {} was successfull, updating the lights cache", command,
                    nodeAsString(nodeID));
            detection.getLightStateCache().updateLight(nodeID, command);
        }
        return result;
    }

    @Override
    public void registerListener(CanRelayChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unRegisterListener(CanRelayChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onMessage(CanMessage canMessage) {
        // decide based on what detection phase we are at. Each is interested in different types of CanMessages, so if
        // for example mapping message is received while in IDLE, it would get ignored
        switch (detection.getStatus()) {
            case FINISHED:
                processStandardMessage(canMessage);
                break;
            case WAITING_MAPPINGS:
                processMappingsMessage(canMessage);
                break;
            case WAITING_OUTPUS:
                processOutputsMessage(canMessage);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDeviceReady() {
        detection.signalDeviceReady();
    }

    @Override
    public void onDeviceFatalError(String desc) {
        logger.warn("CanBusDevice reported fatal error: {}.", desc);
        notifyListenersCanRelayOffline(desc);
        disconnect();
    }

    @Reference
    public void setSerialPortManager(SerialPortManager serialPortManager) {
        this.device.setSerialPortManager(serialPortManager);
    }

    public void unsetSerialPortManager(SerialPortManager serialPortManager) {
        this.device.setSerialPortManager(null);
    }
}
