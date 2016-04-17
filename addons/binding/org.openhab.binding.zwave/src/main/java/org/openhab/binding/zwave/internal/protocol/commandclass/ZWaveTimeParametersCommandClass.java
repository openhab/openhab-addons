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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

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
 * Handles the time parameters command class.
 *
 * @author Jorg de Jong
 *
 */
@XStreamAlias("timeParametersCommandClass")
public class ZWaveTimeParametersCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveTimeParametersCommandClass.class);

    private static final int TIME_SET = 1;
    private static final int TIME_GET = 2;
    private static final int TIME_REPORT = 3;

    // acceptable amount of time the clock on the node may be off before
    // we provide the node with a clock update.
    private static final int CLOCK_PRESISION = 1 * 60 * 1000;

    @XStreamOmitField
    private boolean updateClock = false;

    /**
     * Creates a new instance of the ZWaveTimeParametersCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveTimeParametersCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.TIME_PARAMETERS;
    }

    /**
     * Gets a SerialMessage with the TIME_GET command
     *
     * @return the serial message.
     */
    @Override
    public SerialMessage getValueMessage() {
        logger.debug("NODE {}: Creating new message for command TIME_GET", getNode().getNodeId());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(getNode().getNodeId());
        outputData.write(2);
        outputData.write(getCommandClass().getKey());
        outputData.write(TIME_GET);
        result.setMessagePayload(outputData.toByteArray());
        return result;
    }

    /**
     * Gets a SerialMessage with the TIME_SET command
     *
     * @return the serial message.
     */
    public SerialMessage getSetMessage(Date date) {
        logger.debug("NODE {}: Creating new message for command TIME_SET", getNode().getNodeId());

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Immediate);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(getNode().getNodeId());
        outputData.write(9);
        outputData.write(getCommandClass().getKey());
        outputData.write(TIME_SET);
        outputData.write((cal.get(Calendar.YEAR) & 0xff00) >> 8);
        outputData.write((cal.get(Calendar.YEAR) & 0xff));
        outputData.write(cal.get(Calendar.MONTH) + 1);
        outputData.write(cal.get(Calendar.DAY_OF_MONTH));
        outputData.write(cal.get(Calendar.HOUR_OF_DAY));
        outputData.write(cal.get(Calendar.MINUTE));
        outputData.write(cal.get(Calendar.SECOND));
        result.setMessagePayload(outputData.toByteArray());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received clock command (v{})", this.getNode().getNodeId(), this.getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case TIME_GET:
                logger.debug("NODE %d: requested the time", getNode().getNodeId());
                getController().sendData(getSetMessage(new Date()));

            case TIME_REPORT:

                int year = (serialMessage.getMessagePayloadByte(offset + 1) << 8
                        | serialMessage.getMessagePayloadByte(offset + 2));
                int month = serialMessage.getMessagePayloadByte(offset + 3);
                int day = serialMessage.getMessagePayloadByte(offset + 4);
                int hour = serialMessage.getMessagePayloadByte(offset + 5);
                int minute = serialMessage.getMessagePayloadByte(offset + 6);
                int second = serialMessage.getMessagePayloadByte(offset + 7);

                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(year, month - 1, day, hour, minute, second);
                logger.debug("NODE {}: Received time report: {}", getNode().getNodeId(), cal.getTime());

                Date nodeTime = cal.getTime();
                long clockOffset = (nodeTime.getTime() - System.currentTimeMillis()) / 1000;
                ZWaveTimeValueEvent zEvent = new ZWaveTimeValueEvent(getNode().getNodeId(), endpoint, clockOffset);
                this.getController().notifyEventListeners(zEvent);

                Date low = new Date(System.currentTimeMillis() - CLOCK_PRESISION);
                Date high = new Date(System.currentTimeMillis() + CLOCK_PRESISION);

                // auto correct the time if off by more than 1 minutes
                if (nodeTime.before(low) || nodeTime.after(high)) {
                    updateClock = true;
                }
                break;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        this.getNode().getNodeId(), command, this.getCommandClass().getLabel(),
                        this.getCommandClass().getKey()));
        }
    }

    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        if (refresh == true && getEndpoint() == null) {
            result.add(getValueMessage());
        }
        if (updateClock) {
            logger.warn("NODE {}: adjusting clock", getNode().getNodeId());
            updateClock = false;
            result.add(getSetMessage(new Date()));
            result.add(getValueMessage());
        }
        return result;
    }

    /**
     * Z-Wave Clock Event class. Indicates the current time offset in seconds.
     *
     * @author Jorg de Jong
     */
    public class ZWaveTimeValueEvent extends ZWaveCommandClassValueEvent {

        /**
         * Constructor. Creates a instance of the ZWaveClockValueEvent class.
         *
         * @param nodeId the nodeId of the event
         * @param endpoint the endpoint of the event.
         * @param offset the current clock offset in seconds.
         */
        private ZWaveTimeValueEvent(int nodeId, int endpoint, long offset) {
            super(nodeId, endpoint, CommandClass.TIME_PARAMETERS, offset);
        }
    }
}
