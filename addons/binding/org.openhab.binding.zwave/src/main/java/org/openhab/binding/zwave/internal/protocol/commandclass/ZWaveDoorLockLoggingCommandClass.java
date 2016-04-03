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
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the door lock logging command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("doorLockLoggingCommandClass")
public class ZWaveDoorLockLoggingCommandClass extends ZWaveCommandClass implements ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveDoorLockLoggingCommandClass.class);

    private static final int LOGGING_SUPPORTED_GET = 1;
    private static final int LOGGING_SUPPORTED_REPORT = 2;
    private static final int LOGGING_RECORD_GET = 3;
    private static final int LOGGING_RECORD_REPORT = 4;

    private int supportedMessages = -1;

    /**
     * Creates a new instance of the ZWaveDoorLockLoggingCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveDoorLockLoggingCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.DOOR_LOCK_LOGGING;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received door lock logging command (v{})", this.getNode().getNodeId(),
                this.getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case LOGGING_SUPPORTED_REPORT:
                supportedMessages = serialMessage.getMessagePayloadByte(offset + 1);
                logger.debug("NODE {}: LOGGING_SUPPORTED_REPORT supports {} entries", this.getNode().getNodeId(),
                        supportedMessages);
                break;
            case LOGGING_RECORD_REPORT:
                LogType eventType = LogType.getLogType(serialMessage.getMessagePayloadByte(offset + 1));
                int eventOffset = serialMessage.getMessagePayloadByte(offset + 9);
                if (eventOffset > supportedMessages) {
                    eventOffset = supportedMessages;
                }
                int year = serialMessage.getMessagePayloadByte(offset + 2) << 8
                        + serialMessage.getMessagePayloadByte(offset + 3);
                int month = serialMessage.getMessagePayloadByte(offset + 4) & 0x0f;
                int day = serialMessage.getMessagePayloadByte(offset + 5) & 0x1f;
                int hour = serialMessage.getMessagePayloadByte(offset + 6) & 0x1f;
                int minute = serialMessage.getMessagePayloadByte(offset + 7) & 0x3f;
                int second = serialMessage.getMessagePayloadByte(offset + 8) & 0x3f;
                boolean valid = ((serialMessage.getMessagePayloadByte(offset + 6) & 0xe0) > 0) ? true : false;

                int userCode = serialMessage.getMessagePayloadByte(offset + 10);
                int userCodeLength = serialMessage.getMessagePayloadByte(offset + 11);

                logger.debug("NODE {}: Received door lock log report {}", this.getNode().getNodeId(), eventType);
                break;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        this.getNode().getNodeId(), command, this.getCommandClass().getLabel(),
                        this.getCommandClass().getKey()));
        }
    }

    public SerialMessage getSupported() {
        logger.debug("NODE {}: Creating new message for application command LOGGING_SUPPORTED_GET",
                this.getNode().getNodeId());
        SerialMessage message = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write((byte) this.getNode().getNodeId());
        outputData.write(2);
        outputData.write((byte) getCommandClass().getKey());
        outputData.write((byte) LOGGING_SUPPORTED_GET);
        message.setMessagePayload(outputData.toByteArray());
        return message;
    }

    public SerialMessage getEntry(int id) {
        logger.debug("NODE {}: Creating new message for application command LOGGING_RECORD_GET",
                this.getNode().getNodeId());
        SerialMessage message = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write((byte) this.getNode().getNodeId());
        outputData.write(3);
        outputData.write((byte) getCommandClass().getKey());
        outputData.write((byte) LOGGING_RECORD_GET);
        outputData.write((byte) id);
        message.setMessagePayload(outputData.toByteArray());
        return message;
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        if (refresh == false && supportedMessages != -1) {
            return null;
        }
        Collection<SerialMessage> result = new ArrayList<SerialMessage>();
        result.add(getSupported());
        return result;
    }

    @XStreamAlias("logType")
    public enum LogType {
        LOCKED_USING_CODE(1),
        UNLOCKED_USING_CODE(2),
        LOCKED_USING_BUTTON(3),
        UNLOCKED_USING_BUTTON(4),
        LOCK_ATTEMPT_OUT_OF_SCHEDULE_CODE(5),
        UNLOCK_ATTEMPT_OUT_OF_SCHEDULE_CODE(6),
        ILLEGAL_CODE(7),
        LOCKED_MANUALLY(8),
        UNLOCKED_MANUALLY(9),
        LOCKED_AUTO(10),
        UNLOCKED_AUTO(11),
        LOCKED_REMOTE_OUT_OF_SCHEDULE_CODE(12),
        UNLOCKED_REMOTE_OUT_OF_SCHEDULE_CODE(13),
        LOCKED_USING_REMOTE(14),
        UNLOCKED_USING_REMOTE(15),
        ILLEGAL_REMOTE_CODE(16),
        LOCKED_MANUALLY_2(17),
        UNLOCKED_MANUALLY_2(18),
        LOCK_SECURED(19),
        LOCK_UNSECURED(20),
        USER_CODE_ADDED(21),
        USER_CODE_DELETED(22),
        ALL_CODES_DELETED(23),
        MASTER_CODE_CHANGED(24),
        USER_CODE_CHANGED(25),
        LOCK_RESET(26),
        CONFIG_CHANGED(27),
        LOW_BATTERY(28),
        NEW_BATTERY_INSTALLED(29);

        /**
         * A mapping between the integer code and its corresponding Alarm type to facilitate lookup by code.
         */
        private static Map<Integer, LogType> codeToTypeMapping;

        private int key;

        private LogType(int key) {
            this.key = key;
        }

        private static void initMapping() {
            codeToTypeMapping = new HashMap<Integer, LogType>();
            for (LogType s : values()) {
                codeToTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the alarm type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the alarm type.
         */
        public static LogType getLogType(int i) {
            if (codeToTypeMapping == null) {
                initMapping();
            }

            return codeToTypeMapping.get(i);
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }
    }

}
