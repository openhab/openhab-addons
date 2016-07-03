/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass.SensorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass.ZWaveBinarySensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveBinarySensorConverter class. Converter for communication with the {@link ZWaveBinarySensorConverter}. Implements
 * polling of the binary sensor status and receiving of binary sensor events.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public class ZWaveBinarySensorConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveBinarySensorConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveBinarySensorConverter} class.
     *
     */
    public ZWaveBinarySensorConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveBinarySensorCommandClass commandClass = (ZWaveBinarySensorCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.SENSOR_BINARY, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint());

        String sensorType = channel.getArguments().get("type");

        SerialMessage serialMessage;
        if (sensorType != null && commandClass.getVersion() > 1) {
            serialMessage = node.encapsulate(commandClass.getValueMessage(SensorType.valueOf(sensorType)), commandClass,
                    channel.getEndpoint());
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
        // logger.debug("ZWaveBinarySensorValueEvent 1");

        String sensorType = channel.getArguments().get("type");
        // logger.debug("ZWaveBinarySensorValueEvent 2");
        ZWaveBinarySensorValueEvent sensorEvent = (ZWaveBinarySensorValueEvent) event;
        // logger.debug("ZWaveBinarySensorValueEvent 3");

        // Don't trigger event if this item is bound to another alarm type
        if (sensorType != null && SensorType.valueOf(sensorType) != sensorEvent.getSensorType()) {
            // logger.debug("ZWaveBinarySensorValueEvent 4");
            return null;
        }

        switch (channel.getDataType()) {
            case OnOffType:
                return sensorEvent.getValue() == 0 ? OnOffType.OFF : OnOffType.ON;
            case OpenClosedType:
                return sensorEvent.getValue() == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
            default:
                logger.debug("Unknwon data type {} for BinarySensor", channel.getDataType());
                break;
        }

        return null;
    }
}
