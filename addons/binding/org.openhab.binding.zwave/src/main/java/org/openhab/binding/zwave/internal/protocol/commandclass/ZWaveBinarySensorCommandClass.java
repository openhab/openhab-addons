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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Binary Sensor command class. Binary sensors indicate there status or event as on (0xFF) or off (0x00).
 * The commands include the possibility to get a given value and report a value.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 * @author Jorg de Jong
 */
@XStreamAlias("binarySensorCommandClass")
public class ZWaveBinarySensorCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassDynamicState, ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveBinarySensorCommandClass.class);

    private static final int MAX_SUPPORTED_VERSION = 2;

    private static final int SENSOR_BINARY_GET = 2;
    private static final int SENSOR_BINARY_REPORT = 3;

    // version 2
    private static final int SENSOR_BINARY_SUPPORTEDSENSOR_GET = 1;
    private static final int SENSOR_BINARY_SUPPORTEDSENSOR_REPORT = 4;

    @XStreamOmitField
    private boolean initialiseDone = false;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    private Set<SensorType> types = new HashSet<>();

    /**
     * Creates a new instance of the ZWaveBinarySensorCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveBinarySensorCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.SENSOR_BINARY;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.trace("Handle Message Sensor Binary Request");
        logger.debug("NODE {}: Received SENSOR_BINARY command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case SENSOR_BINARY_REPORT:
                logger.trace("Process Sensor Binary Report");
                int value = serialMessage.getMessagePayloadByte(offset + 1);

                SensorType sensorType = SensorType.UNKNOWN;
                if (getVersion() > 1 && serialMessage.getMessagePayload().length > offset + 2) {
                    logger.debug("Processing Sensor Type {}", serialMessage.getMessagePayloadByte(offset + 2));
                    // For V2, we have the sensor type after the value
                    sensorType = SensorType.getSensorType(serialMessage.getMessagePayloadByte(offset + 2));
                    logger.debug("Sensor Type is {}", sensorType);
                    if (sensorType == null) {
                        sensorType = SensorType.UNKNOWN;
                    }
                }

                logger.debug("NODE {}: Sensor Binary report, type={}, value={}", getNode().getNodeId(),
                        sensorType.getLabel(), value);

                ZWaveBinarySensorValueEvent zEvent = new ZWaveBinarySensorValueEvent(getNode().getNodeId(), endpoint,
                        sensorType, value);
                getController().notifyEventListeners(zEvent);

                dynamicDone = true;
                break;
            case SENSOR_BINARY_SUPPORTEDSENSOR_REPORT:
                logger.trace("Process Sensor Binary Supported Sensors Report");

                int numBytes = serialMessage.getMessagePayload().length - offset - 1;

                for (int i = 0; i < numBytes; ++i) {
                    for (int bit = 0; bit < 8; ++bit) {
                        if (((serialMessage.getMessagePayloadByte(offset + i + 1)) & (1 << bit)) == 0) {
                            continue;
                        }

                        int index = (i << 3) + bit;
                        if (index >= SensorType.values().length) {
                            continue;
                        }

                        // (n)th bit is set. n is the index for the sensor type enumeration.
                        // sensor type seems to be supported, add it to the list if it's not already there.
                        types.add(SensorType.getSensorType(index));
                        logger.debug("NODE {}: found binary sensor {}", getNode().getNodeId(),
                                SensorType.getSensorType(index));
                    }
                }

                initialiseDone = true;
                break;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command 0x%02X for command class %s (0x%02X).",
                        getNode().getNodeId(), command, getCommandClass().getLabel(), getCommandClass().getKey()));
        }
    }

    /**
     * Gets a SerialMessage with the SENSOR_BINARY_GET command
     *
     * @return the serial message
     */
    @Override
    public SerialMessage getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command SENSOR_BINARY_GET", getNode().getNodeId());
        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(getNode().getNodeId());
        outputData.write(2);
        outputData.write(getCommandClass().getKey());
        outputData.write(SENSOR_BINARY_GET);

        result.setMessagePayload(outputData.toByteArray());
        return result;
    }

    public SerialMessage getValueMessage(SensorType type) {
        if (getVersion() == 1) {
            logger.debug("NODE {}: Node doesn't support SENSOR_BINARY_GET with SensorType", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command SENSOR_BINARY_GET for {}",
                getNode().getNodeId(), type);
        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(getNode().getNodeId());
        outputData.write(3);
        outputData.write(getCommandClass().getKey());
        outputData.write(SENSOR_BINARY_GET);
        outputData.write(type.getKey());

        result.setMessagePayload(outputData.toByteArray());
        return result;
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Gets a SerialMessage with the SENSOR_BINARY_SUPPORTEDSENSOR_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public SerialMessage getSupportedMessage() {
        if (getVersion() == 1) {
            logger.debug("NODE {}: SENSOR_BINARY_SUPPORTEDSENSOR_GET not supported for V1", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command SENSOR_BINARY_SUPPORTEDSENSOR_GET",
                getNode().getNodeId());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(getNode().getNodeId());
        outputData.write(2);
        outputData.write(getCommandClass().getKey());
        outputData.write(SENSOR_BINARY_SUPPORTEDSENSOR_GET);

        result.setMessagePayload(outputData.toByteArray());

        return result;
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        if (refresh == true || initialiseDone == false) {
            if (getVersion() > 1) {
                result.add(getSupportedMessage());
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        if (refresh == true || dynamicDone == false) {
            if (getVersion() == 1) {
                result.add(getValueMessage());
            } else {
                for (SensorType type : types) {
                    result.add(getValueMessage(type));
                }
            }
        }

        return result;
    }

    /**
     * Return the supported binary sensor types as reported by the device.
     *
     * @return the supported sensor types
     */
    public Set<SensorType> getSupportedTypes() {
        return ImmutableSet.copyOf(types);
    }

    /**
     * Z-Wave SensorType enumeration. The sensor type indicates the type of sensor that is reported.
     *
     * @author Chris Jackson
     */
    @XStreamAlias("binarySensorType")
    public enum SensorType {
        UNKNOWN(0x00, "Unknown"),
        GENERAL(0x01, "General Purpose"),
        SMOKE(0x02, "Smoke"),
        CO(0x03, "Carbon Monoxide"),
        CO2(0x04, "Carbon Dioxide"),
        HEAT(0x05, "Heat"),
        WATER(0x06, "Water"),
        FREEZE(0x07, "Freeze"),
        TAMPER(0x08, "Tamper"),
        AUX(0x09, "Aux"),
        DOORWINDOW(0x0a, "Door/Window"),
        TILT(0x0b, "Tilt"),
        MOTION(0x0c, "Motion"),
        GLASSBREAK(0x0d, "Glass Break");

        /**
         * A mapping between the integer code and its corresponding Sensor type to facilitate lookup by code.
         */
        private static Map<Integer, SensorType> codeToSensorTypeMapping;

        private int key;
        private String label;

        private SensorType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToSensorTypeMapping = new HashMap<Integer, SensorType>();
            for (SensorType s : values()) {
                codeToSensorTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the sensor type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the sensor type.
         */
        public static SensorType getSensorType(int i) {
            if (codeToSensorTypeMapping == null) {
                initMapping();
            }

            return codeToSensorTypeMapping.get(i);
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }

    /**
     * Z-Wave Binary Sensor Event class. Indicates that an sensor value changed.
     *
     * @author Chris Jackson
     */
    public class ZWaveBinarySensorValueEvent extends ZWaveCommandClassValueEvent {

        private SensorType sensorType;

        /**
         * Constructor. Creates a instance of the ZWaveBinarySensorValueEvent class.
         *
         * @param nodeId the nodeId of the event
         * @param endpoint the endpoint of the event.
         * @param sensorType the sensor type that triggered the event;
         * @param value the value for the event.
         */
        private ZWaveBinarySensorValueEvent(int nodeId, int endpoint, SensorType sensorType, Object value) {
            super(nodeId, endpoint, CommandClass.SENSOR_BINARY, value);
            this.sensorType = sensorType;
        }

        /**
         * Gets the alarm type for this alarm sensor value event.
         */
        public SensorType getSensorType() {
            return sensorType;
        }

        @Override
        public Integer getValue() {
            return (Integer) super.getValue();
        }
    }
}
