/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.message;

import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.type.DataType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link StatusMessage} class is responsible for parsing messages received from Domintell system
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class StatusMessage extends BaseMessage {
    /**
     * Class logger.
     */
    private final Logger logger = LoggerFactory.getLogger(StatusMessage.class);

    /**
     * Module type
     */
    private ModuleType moduleType;

    /**
     * Serial number
     */
    private SerialNumber serialNumber;

    /**
     * IO number
     */
    private Integer ioNumber;

    /**
     * Date type
     */
    private DataType dataType;

    /**
     * Received data
     */
    private String data;

    /**
     * Constructor
     *
     * @param message Message to parse
     */
    StatusMessage(String message) {
        super(Type.DATA, message);
        parseMessage(message);
    }

    /**
     * Parse the message
     *
     * @param message Message
     */
    private void parseMessage(String message) {
        try {
            moduleType = ModuleType.valueOf(message.substring(0, 3));
            serialNumber = new SerialNumber("0x" + message.substring(3, 9).trim());
            int dataTypeIdx = 9;
            if (message.charAt(9) == '-') {
                if (ModuleType.DAL == moduleType) {
                    ioNumber = Integer.parseInt(message.substring(10, 12), 16);
                    dataTypeIdx = 12;
                } else {
                    ioNumber = Integer.parseInt(message.substring(10, 11), 16);
                    dataTypeIdx = 11;
                }
            }
            if (message.contains("[")) {
                data = message.substring(dataTypeIdx);
            } else {
                dataType = DataType.valueOf(message.substring(dataTypeIdx, dataTypeIdx + 1));
                data = message.substring(dataTypeIdx + 1);
            }
        } catch (IllegalArgumentException e) {
            logger.debug("Unknown module type: {}", message);
        }
    }

    //getters

    public ModuleType getModuleType() {
        return moduleType;
    }

    public SerialNumber getSerialNumber() {
        return serialNumber;
    }

    public Integer getIoNumber() {
        return ioNumber;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getData() {
        return data != null ? data.trim() : null;
    }

    @Override
    public String toString() {
        return "StatusMessage{" +
                "moduleType=" + moduleType +
                ", serialNumber=" + serialNumber +
                ", ioNumber=" + ioNumber +
                ", dataType=" + dataType +
                ", data='" + data + '\'' +
                '}';
    }
}
