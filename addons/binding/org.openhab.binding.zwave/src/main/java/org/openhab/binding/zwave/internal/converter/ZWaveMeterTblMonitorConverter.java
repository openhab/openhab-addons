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
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterTblMonitorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterTblMonitorCommandClass.MeterTblMonitorScale;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterTblMonitorCommandClass.ZWaveMeterTblMonitorValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveMeterTblMonitorConverter class. Converter for communication with the {@link ZWaveMeterTblMonitorCommandClass}.
 * Implements polling of the sensor status and receiving of sensor events.
 *
 * @author Jorg de Jong
 */
public class ZWaveMeterTblMonitorConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveMeterTblMonitorConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveMeterTblMonitorConverter} class.
     *
     * @param controller the {@link ZWaveController} to use for sending messages.
     */
    public ZWaveMeterTblMonitorConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveMeterTblMonitorCommandClass commandClass = (ZWaveMeterTblMonitorCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.METER_TBL_MONITOR, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint());

        List<SerialMessage> response = new ArrayList<SerialMessage>();

        for (SerialMessage msg : commandClass.getDynamicValues(true)) {
            response.add(node.encapsulate(msg, commandClass, channel.getEndpoint()));
        }
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String meterScale = channel.getArguments().get("type");
        ZWaveMeterTblMonitorValueEvent meterEvent = (ZWaveMeterTblMonitorValueEvent) event;

        // Don't trigger event if this item is bound to another sensor type
        if (meterScale != null && MeterTblMonitorScale.getMeterScale(meterScale) != meterEvent.getMeterScale()) {
            logger.debug("Not the right scale {} <> {}", meterScale, meterEvent.getMeterScale());
            return null;
        }

        BigDecimal val = (BigDecimal) event.getValue();

        return new DecimalType(val);
    }
}
