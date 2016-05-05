/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the DoorLock command class.
 *
 * @author Dave Badia
 * @author Chris Jackson
 */
@XStreamAlias("doorLockCommandClass")
public class ZWaveDoorLockCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveSetCommands, ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {
    public enum Type {
        DOOR_LOCK_STATE,
        DOOR_LOCK_TIMEOUT
    }

    private static final Logger logger = LoggerFactory.getLogger(ZWaveDoorLockCommandClass.class);

    static final int DOOR_LOCK_SET = 1;
    /**
     * Request the state of the door lock, ie {@link #DOOR_LOCK_REPORT}
     */
    private static final int DOOR_LOCK_GET = 2;
    /**
     * Details about the state of the door lock (secured, unsecured)
     */
    private static final int DOOR_LOCK_REPORT = 3;
    private static final int DOOR_LOCK_CONFIG_SET = 4;
    /**
     * Request the config of the door lock, ie {@link #DOOR_LOCK_CONFIG_REPORT}
     */
    private static final int DOOR_LOCK_CONFIG_GET = 5;
    /**
     * Details about the config of the door lock (timed autolock, etc)
     */
    private static final int DOOR_LOCK_CONFIG_REPORT = 6;

    @XStreamOmitField
    private boolean initialisationDone = false;
    @XStreamOmitField
    private boolean dynamicDone = false;

    boolean lockTimeoutSet = false;
    int lockTimeout = 0;
    int lockTimeoutMinutes = 0;
    int lockTimeoutSeconds = 0;
    int insideHandleMode = 0;
    int outsideHandleMode = 0;

    /**
     * Creates a new instance of the ZWaveDoorLockCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveDoorLockCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.DOOR_LOCK;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received DOORLOCK command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case DOOR_LOCK_CONFIG_REPORT:
                initialisationDone = true;
                lockTimeoutSet = serialMessage.getMessagePayloadByte(offset + 1) == 2 ? true : false;
                insideHandleMode = serialMessage.getMessagePayloadByte(offset + 2) & 0x0f;
                outsideHandleMode = (serialMessage.getMessagePayloadByte(offset + 2) & 0xf0) >> 4;
                lockTimeoutMinutes = serialMessage.getMessagePayloadByte(offset + 3);
                lockTimeoutSeconds = serialMessage.getMessagePayloadByte(offset + 4);

                lockTimeout = 0;
                if (lockTimeoutSet == true) {
                    if (lockTimeoutSeconds != 0xfe) {
                        lockTimeout = lockTimeoutSeconds;
                    }
                    if (lockTimeoutMinutes != 0xfe) {
                        lockTimeout += lockTimeoutMinutes * 60;
                    }
                }
                logger.info(
                        "NODE {}: Door-Lock config report - timeoutEnabled={} timeoutMinutes={}, "
                                + "timeoutSeconds={}",
                        getNode().getNodeId(), lockTimeoutSet, lockTimeoutMinutes, lockTimeoutSeconds);
                ZWaveCommandClassValueEvent configEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(),
                        endpoint, CommandClass.DOOR_LOCK, lockTimeout, Type.DOOR_LOCK_TIMEOUT);
                getController().notifyEventListeners(configEvent);
                return;
            case DOOR_LOCK_REPORT:
                dynamicDone = true;

                int lockState = serialMessage.getMessagePayloadByte(offset + 1);
                int handlesMode = serialMessage.getMessagePayloadByte(offset + 2);
                int doorCondition = serialMessage.getMessagePayloadByte(offset + 3);
                int statusTimeoutMinutes = serialMessage.getMessagePayloadByte(offset + 4);
                int statusTimeoutSeconds = serialMessage.getMessagePayloadByte(offset + 5);

                DoorLockStateType doorLockState = DoorLockStateType.getDoorLockStateType(lockState);
                logger.info(String.format(
                        "NODE %d: Door-Lock state report - lockState=%s, "
                                + "handlesMode=0x%02x, doorCondition=0x%02x, timeoutMinutes=0x%02x, "
                                + "timeoutSeconds=0x%02x",
                        getNode().getNodeId(), doorLockState.label, handlesMode, doorCondition, statusTimeoutMinutes,
                        statusTimeoutSeconds));
                ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                        CommandClass.DOOR_LOCK, lockState, Type.DOOR_LOCK_STATE);
                getController().notifyEventListeners(zEvent);
                break;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command 0x%02X for command class %s (0x%02X).",
                        getNode().getNodeId(), command, getCommandClass().getLabel(), getCommandClass().getKey()));
                break;
        }
    }

    /**
     * Gets a SerialMessage with the DOORLOCK_GET command
     *
     * @return the serial message
     */
    @Override
    public SerialMessage getValueMessage() {
        logger.debug("NODE {}: Creating new message for application command DOORLOCK_GET", getNode().getNodeId());
        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] newPayload = { (byte) getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
                (byte) DOOR_LOCK_GET, };
        result.setMessagePayload(newPayload);
        return result;
    }

    @Override
    public SerialMessage setValueMessage(int value) {
        logger.debug("NODE {}: Creating new message for application command DOORLOCK_SET", getNode().getNodeId());
        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);
        byte[] newPayload = { (byte) getNode().getNodeId(), 3, (byte) getCommandClass().getKey(), (byte) DOOR_LOCK_SET,
                (byte) value };
        result.setMessagePayload(newPayload);
        return result;
    }

    public SerialMessage getConfigMessage() {
        logger.debug("NODE {}: Creating new message for application command DOORLOCK_CONFIG_GET",
                getNode().getNodeId());
        SerialMessage message = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write((byte) getNode().getNodeId());
        outputData.write(2);
        outputData.write((byte) getCommandClass().getKey());
        outputData.write((byte) DOOR_LOCK_CONFIG_GET);
        message.setMessagePayload(outputData.toByteArray());
        return message;
    }

    public SerialMessage setConfigMessage(boolean timeoutEnabled, int timeoutValue) {
        lockTimeout = timeoutValue;
        lockTimeoutSet = timeoutEnabled;
        if (lockTimeoutSet == true) {
            lockTimeoutMinutes = timeoutValue / 60;
            if (lockTimeoutMinutes == 0) {
                lockTimeoutMinutes = 0xfe;
            }
            lockTimeoutSeconds = timeoutValue % 60;
        } else {
            lockTimeout = 0;
            lockTimeoutMinutes = 0xfe;
            lockTimeoutSeconds = 0xfe;
        }

        logger.debug("NODE {}: Creating new message for application command DOORLOCK_GET", getNode().getNodeId());
        SerialMessage message = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write((byte) getNode().getNodeId());
        outputData.write(6);
        outputData.write((byte) getCommandClass().getKey());
        outputData.write((byte) DOOR_LOCK_CONFIG_SET);

        if (lockTimeoutSet == true) {
            outputData.write((byte) 2);
        } else {
            outputData.write((byte) 1);
        }
        outputData.write((byte) (((outsideHandleMode << 4) & 0xf0) + (insideHandleMode & 0x0f)));
        outputData.write((byte) lockTimeoutMinutes);
        outputData.write((byte) lockTimeoutSeconds);

        message.setMessagePayload(outputData.toByteArray());
        return message;
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        if (refresh) {
            initialisationDone = false;
        }

        if (initialisationDone) {
            return null;
        }

        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        result.add(getConfigMessage());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        if (refresh) {
            dynamicDone = false;
        }

        if (dynamicDone) {
            return null;
        }

        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        result.add(getConfigMessage());
        result.add(getValueMessage());
        return result;
    }

    /**
     * Z-Wave DoorLockState enumeration. The door lock state type indicates
     * the state of the door that is reported.
     */
    @XStreamAlias("doorLockState")
    enum DoorLockStateType {
        UNSECURED(0x00, "Unsecured"),
        UNSECURED_TIMEOUT(0x01, "Unsecure with Timeout"),
        INSIDE_UNSECURED(0x10, "Inside Handle Unsecured"),
        INSIDE_UNSECURED_TIMEOUT(0x11, "Inside Handle Unsecured with Timeout"),
        OUTSIDE_UNSECURED(0x20, "Outside Handle Unsecured"),
        OUTSIDE_UNSECURED_TIMEOUT(0x21, "Outside Handle Unsecured with Timeout"),
        SECURED(0xFF, "Secured"),
        UNKNOWN(0xFE, "Unknown"), // This isn't per the spec, it's just our definition
        ;
        /**
         * A mapping between the integer code and its corresponding door
         * lock state type to facilitate lookup by code.
         */
        private static Map<Integer, DoorLockStateType> codeToDoorLockStateTypeMapping;

        private int key;
        private String label;

        private static void initMapping() {
            codeToDoorLockStateTypeMapping = new ConcurrentHashMap<Integer, DoorLockStateType>();
            for (DoorLockStateType d : values()) {
                codeToDoorLockStateTypeMapping.put(d.key, d);
            }
        }

        /**
         * Lookup function based on the fan mode type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the fan mode type.
         */
        public static DoorLockStateType getDoorLockStateType(int i) {
            if (codeToDoorLockStateTypeMapping == null) {
                initMapping();
            }
            return codeToDoorLockStateTypeMapping.get(i);
        }

        DoorLockStateType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }
}
