/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.converter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.MeterScale;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.ZWaveMeterValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveMultiLevelSensorConverter class. Converter for communication with the {@link ZWaveMultiLevelSensorCommandClass}.
 * Implements polling of the sensor status and receiving of sensor events.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public class ZWaveMeterConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveMeterConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveMeterConverter} class.
     *
     * @param controller the {@link ZWaveController} to use for sending messages.
     */
    public ZWaveMeterConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveMeterCommandClass commandClass = (ZWaveMeterCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.METER, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint());
        SerialMessage serialMessage;

        // Don't refresh channels that are the reset button
        if ("true".equalsIgnoreCase(channel.getArguments().get("reset"))) {
            return null;
        }

        String meterScale = channel.getArguments().get("type");
        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint());

        if (meterScale != null) {
            serialMessage = node.encapsulate(commandClass.getMessage(MeterScale.getMeterScale(meterScale)),
                    commandClass, channel.getEndpoint());
        } else {
            serialMessage = node.encapsulate(commandClass.getValueMessage(), commandClass, channel.getEndpoint());
        }

        List<SerialMessage> response = new ArrayList<SerialMessage>(1);
        response.add(serialMessage);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        // We ignore any meter reports for item bindings configured with 'reset=true'
        // since we don't want to be updating the 'reset' switch
        if ("true".equalsIgnoreCase(channel.getArguments().get("reset"))) {
            return null;
        }

        String meterScale = channel.getArguments().get("type");
        String meterZero = channel.getArguments().get("zero"); // needs to be a config setting - not arg
        ZWaveMeterValueEvent meterEvent = (ZWaveMeterValueEvent) event;
        // logger.debug("Meter converter: scale {} <> {}", meterScale, meterEvent.getMeterScale());

        // Don't trigger event if this item is bound to another sensor type
        if (meterScale != null && MeterScale.getMeterScale(meterScale) != meterEvent.getMeterScale()) {
            logger.debug("Not the right scale {} <> {}", meterScale, meterEvent.getMeterScale());
            return null;
        }

        BigDecimal val = (BigDecimal) event.getValue();

        // If we've set a zero, then anything below this value needs to be considered ZERO
        if (meterZero != null) {
            if (val.doubleValue() <= Double.parseDouble(meterZero)) {
                val = BigDecimal.ZERO;
            }
        }

        return new DecimalType(val);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> receiveCommand(ZWaveThingChannel channel, ZWaveNode node, Command command) {
        // Is this channel a reset button - if not, just return
        if ("true".equalsIgnoreCase(channel.getArguments().get("reset")) == false) {
            return null;
        }

        // It's not an ON command from a button switch, do not reset
        if (command != OnOffType.ON) {
            return null;
        }

        ZWaveMeterCommandClass commandClass = (ZWaveMeterCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.METER, channel.getEndpoint());

        // Get the reset message - will return null if not supported
        SerialMessage serialMessage = node.encapsulate(commandClass.getResetMessage(), commandClass,
                channel.getEndpoint());

        if (serialMessage == null) {
            return null;
        }

        // Queue reset message
        List<SerialMessage> messages = new ArrayList<SerialMessage>();
        messages.add(serialMessage);

        // And poll the device
        for (SerialMessage serialGetMessage : commandClass.getDynamicValues(true)) {
            messages.add(node.encapsulate(serialGetMessage, commandClass, channel.getEndpoint()));
        }
        return messages;
    }
}
