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
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.SensorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.ZWaveMultiLevelSensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * ZWaveMultiLevelSensorConverter class. Converter for communication with the {@link ZWaveMultiLevelSensorCommandClass}.
 * Implements polling of the sensor status and receiving of sensor events.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public class ZWaveMultiLevelSensorConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveMultiLevelSensorConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveMultiLevelSensorConverter} class.
     *
     */
    public ZWaveMultiLevelSensorConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveMultiLevelSensorCommandClass commandClass = (ZWaveMultiLevelSensorCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.SENSOR_MULTILEVEL, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint());

        String sensorType = channel.getArguments().get("type");

        SerialMessage serialMessage;
        if (sensorType != null) {
            serialMessage = node.encapsulate(commandClass.getMessage(SensorType.valueOf(sensorType)), commandClass,
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
        String sensorType = channel.getArguments().get("type");
        String sensorScale = channel.getArguments().get("config_scale");
        ZWaveMultiLevelSensorValueEvent sensorEvent = (ZWaveMultiLevelSensorValueEvent) event;

        // Don't trigger event if this item is bound to another sensor type
        if (sensorType == null) {
            logger.debug("NODE {}: No sensorType set for channel {}", event.getNodeId(), channel.getUID());
            return null;
        }

        if (SensorType.valueOf(sensorType) != sensorEvent.getSensorType()) {
            return null;
        }

        BigDecimal val = (BigDecimal) event.getValue();

        // Perform a scale conversion if needed
        if (sensorScale != null) {
            logger.debug("NODE {}: Sensor is reporting scale {}, requiring conversion to {}. Value is now {}.",
                    event.getNodeId(), sensorEvent.getSensorScale(), sensorScale, val);

            SensorType senType = SensorType.valueOf(sensorType);
            switch (senType) {
                case TEMPERATURE:
                    val = convertTemperature(sensorEvent.getSensorScale(), Integer.parseInt(sensorScale), val);
                    break;
                default:
                    logger.debug("NODE {}: Sensor conversion not performed for {}.", event.getNodeId(), senType);
                    break;
            }
        }

        return new DecimalType(val);
    }
}