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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass.ZWaveConfigurationParameterEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveConfigurationConverter class. Converter for communication with the {@link ZWaveConfigurationCommandClass}.
 *
 * @author Chris Jackson
 */
public class ZWaveConfigurationConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveConfigurationConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveConfigurationConverter} class.
     *
     */
    public ZWaveConfigurationConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveConfigurationCommandClass commandClass = (ZWaveConfigurationCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.CONFIGURATION, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint());
        String parmNumber = channel.getArguments().get("parameter");
        if (parmNumber == null) {
            logger.error("NODE {}: 'parameter' option must be specified.", node.getNodeId());
            return null;
        }
        int parmValue = Integer.parseInt(parmNumber);
        if (parmValue < 0 || parmValue > 255) {
            logger.error("NODE {}: 'parameter' option must be between 0 and 255.", node.getNodeId());
            return null;
        }

        SerialMessage serialMessage = node.encapsulate(commandClass.getConfigMessage(parmValue), commandClass,
                channel.getEndpoint());
        List<SerialMessage> response = new ArrayList<SerialMessage>(1);
        response.add(serialMessage);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String parmNumber = channel.getArguments().get("parameter");
        ZWaveConfigurationParameterEvent cfgEvent = (ZWaveConfigurationParameterEvent) event;
        // Make sure this is for the parameter we want
        if (cfgEvent.getParameter() != null && cfgEvent.getParameter().getIndex() != Integer.parseInt(parmNumber)) {
            return null;
        }

        State state;
        switch (channel.getDataType()) {
            case DecimalType:
                state = new DecimalType(cfgEvent.getParameter().getValue());
                break;
            case PercentType:
                state = new DecimalType(cfgEvent.getParameter().getValue());
                break;
            default:
                state = null;
                logger.warn("No conversion from {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> receiveCommand(ZWaveThingChannel channel, ZWaveNode node, Command command) {

        String parmNumber = channel.getArguments().get("parameter");
        if (parmNumber == null) {
            logger.error("NODE {}: 'parameter' option must be specified.", node.getNodeId());
            return null;
        }

        int paramIndex = Integer.parseInt(parmNumber);
        if (paramIndex < 0 || paramIndex > 255) {
            logger.error("NODE {}: 'parameter' option must be between 0 and 255.", node.getNodeId());
            return null;
        }
        /*
         * ZWaveProductDatabase database = new ZWaveProductDatabase();
         * if (database.FindProduct(node.getManufacturer(), node.getDeviceType(), node.getDeviceId(),
         * node.getApplicationVersion()) == false) {
         * logger.error("NODE {}: database can't find product.", node.getNodeId());
         * return;
         * }
         *
         * List<ZWaveDbConfigurationParameter> configList = database.getProductConfigParameters();
         * if (configList == null) {
         * logger.error("NODE {}: Device has no configuration.", node.getNodeId());
         * return;
         * }
         *
         * ZWaveDbConfigurationParameter dbParameter = null;
         * for (ZWaveDbConfigurationParameter parameter : configList) {
         * if (parameter.Index == paramIndex) {
         * dbParameter = parameter;
         * break;
         * }
         * }
         * if (dbParameter == null) {
         * logger.error("NODE {}: Device has no parameter {}.", node.getNodeId(), paramIndex);
         * return;
         * }
         * ConfigurationParameter configurationParameter = new ConfigurationParameter(paramIndex,
         * (Integer) converter.convertFromCommandToValue(item, command), dbParameter.Size);
         *
         * // Set the parameter
         * SerialMessage serialMessage = commandClass.setConfigMessage(configurationParameter);
         * if (serialMessage == null) {
         * logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
         * commandClass.getCommandClass().getLabel(), endpointId);
         * return;
         * }
         *
         * this.getController().sendData(serialMessage);
         *
         * // And request a read-back
         * serialMessage = commandClass.getConfigMessage(paramIndex);
         * this.getController().sendData(serialMessage);
         */
        SerialMessage serialMessage = null;
        List<SerialMessage> messages = new ArrayList<SerialMessage>();
        messages.add(serialMessage);
        return messages;

        // if (command instanceof State) {
        // TODO: this.getEventPublisher().postUpdate(item.getName(), (State) command);
        // }
    }
}
