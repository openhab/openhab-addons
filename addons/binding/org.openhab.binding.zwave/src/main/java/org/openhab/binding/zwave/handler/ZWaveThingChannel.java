/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.handler;

import java.util.Map;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.openhab.binding.zwave.internal.converter.ZWaveCommandClassConverter;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

public class ZWaveThingChannel {
    public enum DataType {
        DecimalType,
        HSBType,
        IncreaseDecreaseType,
        OnOffType,
        OpenClosedType,
        PercentType,
        StringType,
        DateTimeType,
        UpDownType,
        StopMoveType;
    }

    // int nodeId;
    int endpoint;
    ChannelUID uid;
    String commandClass;
    ZWaveCommandClassConverter converter;
    DataType dataType;
    Map<String, String> arguments;

    public ZWaveThingChannel(ZWaveControllerHandler controller, ChannelUID uid, DataType dataType,
            String commandClassName, int endpoint, Map<String, String> arguments) {
        this.uid = uid;
        this.arguments = arguments;
        this.commandClass = commandClassName;
        this.endpoint = endpoint;
        this.dataType = dataType;

        // Get the converter
        CommandClass commandClass = ZWaveCommandClass.CommandClass.getCommandClass(commandClassName);
        if (commandClass == null) {
            // logger.warn("NODE {}: Error finding command class {} on channel {}", nodeId, uid, commandClassName);
        } else {
            this.converter = ZWaveCommandClassConverter.getConverter(controller, commandClass);
            if (this.converter == null) {
                // logger.warn("NODE {}: No converter found for {}, class {}", nodeId, uid, commandClassName);
            }
        }
    }

    public ChannelUID getUID() {
        return uid;
    }

    public String getCommandClass() {
        return commandClass;
    }

    public int getEndpoint() {
        return endpoint;
    }

    public DataType getDataType() {
        return dataType;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }
}
