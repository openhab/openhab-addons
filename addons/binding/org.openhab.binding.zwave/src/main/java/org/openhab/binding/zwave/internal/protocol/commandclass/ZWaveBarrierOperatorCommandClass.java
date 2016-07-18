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
 * Handles Barrier Operator Command Class for Items like Garage Door Opener etc...
 *
 * @author Chris Jackson
 * @author sankala
 *
 */
@XStreamAlias("barrierOperatorCommandClass")
public class ZWaveBarrierOperatorCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveSetCommands, ZWaveCommandClassDynamicState {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveBarrierOperatorCommandClass.class);

    public static final int BARRIER_OPERATOR_SET = 1;
    public static final int BARRIER_OPERATOR_GET = 2;
    public static final int BARRIER_OPERATOR_REPORT = 3;
    public static final int BARRIER_OPERATOR_SIGNAL_SUPPORTED_GET = 4;
    public static final int BARRIER_OPERATOR_SIGNAL_SUPPORTED_REPORT = 5;
    public static final int BARRIER_OPERATOR_SIGNAL_SET = 6;
    public static final int BARRIER_OPERATOR_SIGNAL_GET = 7;
    public static final int BARRIER_OPERATOR_SIGNAL_REPORT = 8;

    @XStreamOmitField
    private boolean dynamicDone = false;

    public ZWaveBarrierOperatorCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.BARRIER_OPERATOR;
    }

    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received BARRIER_OPERATOR command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case BARRIER_OPERATOR_REPORT:
                logger.trace("Process Barrier Operator Report");
                int value = serialMessage.getMessagePayloadByte(offset + 1);
                logger.debug("NODE {}: Barrier Operator report, value = {}", getNode().getNodeId(), value);

                ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                        getCommandClass(), BarrierOperatorStateType.getBarrierOperatorStateType(value));

                getController().notifyEventListeners(zEvent);
                break;
            default:
                logger.warn(String.format("Unsupported Command 0x%02X for command class %s (0x%02X).", command,
                        getCommandClass().getLabel(), getCommandClass().getKey()));
        }
    }

    @Override
    public SerialMessage setValueMessage(int value) {
        logger.debug("NODE {}: Creating new message for application command BARRIER_OPERATOR_SET",
                getNode().getNodeId());
        SerialMessage message = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write((byte) getNode().getNodeId());
        outputData.write(3);
        outputData.write((byte) getCommandClass().getKey());
        outputData.write(BARRIER_OPERATOR_SET);
        outputData.write(value > 0 ? 0xFF : 0x00);
        message.setMessagePayload(outputData.toByteArray());

        return message;
    }

    @Override
    public SerialMessage getValueMessage() {
        logger.debug("NODE {}: Creating new message for command BARRIER_OPERATOR_GET", getNode().getNodeId());
        SerialMessage message = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write((byte) getNode().getNodeId());
        outputData.write(2);
        outputData.write((byte) getCommandClass().getKey());
        outputData.write(BARRIER_OPERATOR_GET);
        message.setMessagePayload(outputData.toByteArray());

        return message;
    }

    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        if (refresh == true || dynamicDone == false) {
            return null;
        }

        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        result.add(getValueMessage());
        return result;
    }

    @XStreamAlias("barrierOperatorState")
    public static enum BarrierOperatorStateType {
        STATE_CLOSED(0x00, "Closed"),
        STATE_CLOSING(0xFC, "Closing"),
        STATE_OPENED(0xFF, "Open"),
        STATE_OPENING(0xFE, "Opening"),
        STATE_STOPPED(0xFD, "Stopped");

        private static Map<Integer, BarrierOperatorStateType> codeToBarrierOperatorStateTypeMapping;

        private int key;
        private String label;

        private static void initMapping() {
            codeToBarrierOperatorStateTypeMapping = new ConcurrentHashMap<Integer, ZWaveBarrierOperatorCommandClass.BarrierOperatorStateType>();
            for (BarrierOperatorStateType s : values()) {
                codeToBarrierOperatorStateTypeMapping.put(s.key, s);
            }
        }

        public static BarrierOperatorStateType getBarrierOperatorStateType(int i) {
            if (codeToBarrierOperatorStateTypeMapping == null) {
                initMapping();
            }
            BarrierOperatorStateType barrierOperatorStateType = codeToBarrierOperatorStateTypeMapping.get(i);
            // If the state is stopped, then the value indicates what is the percentage of opening. So it will not
            // directly yield us the "StateStopped" . We have to force it to StateStopped.
            if (barrierOperatorStateType == null && (i < 99 && i > 0)) {
                barrierOperatorStateType = STATE_STOPPED;
            }

            return barrierOperatorStateType;
        }

        private BarrierOperatorStateType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        public static Map<Integer, BarrierOperatorStateType> getCodeToBarrierOperatorStateTypeMapping() {
            return codeToBarrierOperatorStateTypeMapping;
        }

        public int getKey() {
            return key;
        }

        public int getValue() {
            return key;
        }

        public String getLabel() {
            return label;
        }
    }
}
