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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
 * Handles the Meter Tbl Monitor Command command class.
 *
 * @author Jorg de Jong
 */
@XStreamAlias("meterTblMonitorCommandClass")
public class ZWaveMeterTblMonitorCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveMeterTblMonitorCommandClass.class);

    private static final byte METER_TBL_TABLE_ID_GET = 3;
    private static final byte METER_TBL_TABLE_ID_REPORT = 4;
    private static final byte METER_TBL_TABLE_CAPABILITY_GET = 5;
    private static final byte METER_TBL_REPORT = 6;
    private static final byte METER_TBL_CURRENT_DATA_GET = 12;
    private static final byte METER_TBL_CURRENT_DATA_REPORT = 13;

    // unsuported private static final byte METER_TBL_STATUS_REPORT = 11;
    // unsuported private static final byte METER_TBL_STATUS_DATE_GET = 10;
    // unsuported private static final byte METER_TBL_STATUS_DEPTH_GET = 9;
    // unsuported private static final byte METER_TBL_STATUS_SUPPORTED_GET = 7;
    // unsuported private static final byte METER_TBL_STATUS_SUPPORTED_REPORT = 8;
    // unsuported private static final byte METER_TBL_HISTORICAL_DATA_GET = 14;
    // unsuported private static final byte METER_TBL_HISTORICAL_DATA_REPORT = 15;
    // unsuported private static final byte METER_TBL_TABLE_POINT_ADM_NO_GET = 1;
    // unsuported private static final byte METER_TBL_TABLE_POINT_ADM_NO_REPORT = 2;

    @XStreamOmitField
    private boolean initialiseDone = false;
    @XStreamOmitField
    private boolean dynamicDone = false;

    private MeterTblMonitorType meterType;
    private String tableName;
    private int rateType;
    private int dataset;

    /**
     * Creates a new instance of the ZWaveMeterTblMonitorCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWaveMeterTblMonitorCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.METER_TBL_MONITOR;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpointId)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received Meter Tbl Monitor Request", this.getNode().getNodeId());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case METER_TBL_CURRENT_DATA_REPORT:
                handleDataReport(serialMessage, offset, endpointId);
                break;
            case METER_TBL_TABLE_ID_REPORT:
                handleTableIdReport(serialMessage, offset, endpointId);
                break;
            case METER_TBL_REPORT:
                handleReport(serialMessage, offset, endpointId);
                break;
            default:
                logger.warn(String.format("Unsupported Command 0x%02X for command class %s (0x%02X).", command,
                        getCommandClass().getLabel(), getCommandClass().getKey()));

        }
    }

    private void handleTableIdReport(SerialMessage serialMessage, int offset, int endpointId)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received Meter Tbl Monitor Table ID Report", this.getNode().getNodeId());
        int numBytes = serialMessage.getMessagePayloadByte(offset + 1) & 0x1F;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Check for null terminations - ignore anything after the first null
        for (int c = 0; c < numBytes; c++) {
            if (serialMessage.getMessagePayloadByte(c + offset + 2) > 32
                    && serialMessage.getMessagePayloadByte(c + offset + 2) < 127) {
                baos.write((byte) (serialMessage.getMessagePayloadByte(c + offset + 2)));
            }
        }
        String name;

        try {
            name = new String(baos.toByteArray(), "ASCII");
        } catch (UnsupportedEncodingException e) {
            name = "unsupported";
        }
        tableName = name;
        logger.debug("NODE {}: table name: {}", this.getNode().getNodeId(), name);
    }

    private void handleReport(SerialMessage serialMessage, int offset, int endpointId)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received Meter Tbl Monitor Report", this.getNode().getNodeId());
        int meterType = serialMessage.getMessagePayloadByte(offset + 1) & 0x3F;
        int rateType = (serialMessage.getMessagePayloadByte(offset + 1) & 0xC0) >> 6;
        int properties = serialMessage.getMessagePayloadByte(offset + 2);
        int datasetSupported = extractValue(serialMessage.getMessagePayload(), offset + 3, 3);
        int datasetSupportedHistory = extractValue(serialMessage.getMessagePayload(), offset + 6, 3);
        int dataSupportedHistory = extractValue(serialMessage.getMessagePayload(), offset + 9, 3);

        logger.debug("NODE {}: meterType              : {} {}", this.getNode().getNodeId(), meterType,
                MeterTblMonitorType.getMeterType(meterType));
        logger.debug("NODE {}: rateType               : {}", this.getNode().getNodeId(), rateType);
        logger.debug("NODE {}: properties             : {}", this.getNode().getNodeId(), properties);
        logger.debug("NODE {}: datasetSupported       : {}", this.getNode().getNodeId(), datasetSupported);
        logger.debug("NODE {}: datasetSupportedHistory: {}", this.getNode().getNodeId(), datasetSupportedHistory);
        logger.debug("NODE {}: dataSupportedHistory   : {}", this.getNode().getNodeId(), dataSupportedHistory);

        this.meterType = MeterTblMonitorType.getMeterType(meterType);
        this.rateType = rateType;
        this.dataset = datasetSupported;

        initialiseDone = true;
    }

    private void handleDataReport(SerialMessage serialMessage, int offset, int endpointId)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received Meter Tbl Monitor Data Report", this.getNode().getNodeId());
        int numReports = serialMessage.getMessagePayloadByte(offset + 1);
        int rateType = serialMessage.getMessagePayloadByte(offset + 2) & 0x03;
        boolean operatingStatus = (serialMessage.getMessagePayloadByte(offset + 2) & 0x80) > 0;

        int dataset = extractValue(serialMessage.getMessagePayload(), offset + 3, 3);
        int year = extractValue(serialMessage.getMessagePayload(), offset + 6, 2);
        int month = serialMessage.getMessagePayloadByte(offset + 8);
        int day = serialMessage.getMessagePayloadByte(offset + 9);
        int hour = serialMessage.getMessagePayloadByte(offset + 10);
        int minutes = serialMessage.getMessagePayloadByte(offset + 11);
        int seconds = serialMessage.getMessagePayloadByte(offset + 12);

        int scaleIndex = serialMessage.getMessagePayloadByte(offset + 13) & 0x1F;
        int presision = (serialMessage.getMessagePayloadByte(offset + 13) & 0xE0) >> 5;

        int valueRaw = extractValue(serialMessage.getMessagePayload(), offset + 14, 4);

        logger.trace("NODE {}: numReports:{}", this.getNode().getNodeId(), numReports);
        logger.trace("NODE {}: rateType  :{}", this.getNode().getNodeId(), rateType);
        logger.trace("NODE {}: operating :{}", this.getNode().getNodeId(), operatingStatus);
        logger.trace("NODE {}: dataset   :{}", this.getNode().getNodeId(), dataset);
        logger.trace(String.format("NODE %d: time      :%04d-%02d-%02d %02d:%02d:%02d", this.getNode().getNodeId(),
                year, month, day, hour, minutes, seconds));

        logger.trace("NODE {}: scale     :{}", this.getNode().getNodeId(), scaleIndex);
        logger.trace("NODE {}: presision :{}", this.getNode().getNodeId(), presision);
        logger.trace("NODE {}: value     :{}", this.getNode().getNodeId(), valueRaw);

        MeterTblMonitorScale scale = MeterTblMonitorScale.getMeterScale(meterType, scaleIndex);
        if (scale == null) {
            logger.warn("NODE {}: Invalid meter scale {}", getNode().getNodeId(), scaleIndex);
            return;
        }

        try {
            BigDecimal value = new BigDecimal(valueRaw / Math.pow(10, presision)).setScale(presision,
                    BigDecimal.ROUND_HALF_UP);
            logger.debug("NODE {}: Meter Tbl Monitor: Type={}, Scale={}({}), Value={}, Dataset={}",
                    getNode().getNodeId(), meterType.getLabel(), scale.getUnit(), scale.getScale(), value, dataset);

            ZWaveMeterTblMonitorValueEvent zEvent = new ZWaveMeterTblMonitorValueEvent(getNode().getNodeId(),
                    endpointId, meterType, scale, value);
            this.getController().notifyEventListeners(zEvent);
        } catch (NumberFormatException e) {
            logger.error("NODE {}: Meter Tbl Monitor Value Error {}", getNode().getNodeId(), e);
            return;
        }

        dynamicDone = true;
    }

    /**
     * Gets a SerialMessage with the METER_TBL_CURRENT_DATA_GET command
     *
     * @return the serial message
     */
    public SerialMessage getCurrentData(int dataset) {
        logger.debug("NODE {}: Creating new message for application command METER_TBL_CURRENT_DATA_GET",
                getNode().getNodeId());
        SerialMessage message = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(this.getNode().getNodeId());
        outputData.write(5);
        outputData.write(getCommandClass().getKey());
        outputData.write(METER_TBL_CURRENT_DATA_GET);
        outputData.write(dataset >> 16);
        outputData.write(dataset >> 8);
        outputData.write(dataset);
        message.setMessagePayload(outputData.toByteArray());

        return message;
    }

    /**
     * Gets a SerialMessage with the METER_TBL_TABLE_CAPABILITY_GET command
     *
     * @return the serial message
     */
    public SerialMessage getCapabilityGet() {
        logger.debug("NODE {}: Creating new message for application command METER_TBL_TABLE_CAPABILITY_GET",
                getNode().getNodeId());
        SerialMessage message = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(this.getNode().getNodeId());
        outputData.write(2);
        outputData.write(getCommandClass().getKey());
        outputData.write(METER_TBL_TABLE_CAPABILITY_GET);
        message.setMessagePayload(outputData.toByteArray());

        return message;
    }

    /**
     * Gets a SerialMessage with the METER_TBL_TABLE_ID_GET command
     *
     * @return the serial message
     */
    public SerialMessage getTableIDGet() {
        logger.debug("NODE {}: Creating new message for application command METER_TBL_TABLE_ID_GET",
                getNode().getNodeId());
        SerialMessage message = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(this.getNode().getNodeId());
        outputData.write(2);
        outputData.write(getCommandClass().getKey());
        outputData.write(METER_TBL_TABLE_ID_GET);
        message.setMessagePayload(outputData.toByteArray());

        return message;
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        // If we're already initialized, then don't do it again unless we're refreshing
        if (refresh == true || initialiseDone == false) {
            result.add(getCapabilityGet());
            result.add(getTableIDGet());
        }
        return result;
    }

    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();

        if (refresh == true || dynamicDone == false) {
            result.add(getCurrentData(dataset));
        }

        return result;
    }

    /**
     * Z-Wave MeterTblMonitorType enumeration. The meter type indicates the type of meter that is reported.
     *
     */
    public enum MeterTblMonitorType {
        UNKNOWN(0, "Unknown"),
        ELECTRIC(1, "Electric"),
        GAS(2, "Gas"),
        WATER(3, "Water"),
        TWIN_ELECTRIC(4, "Twin Electric");

        /**
         * A mapping between the integer code and its corresponding Meter type
         * to facilitate lookup by code.
         */
        private static Map<Integer, MeterTblMonitorType> codeToMeterTypeMapping;

        private int key;
        private String label;

        private MeterTblMonitorType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToMeterTypeMapping = new HashMap<Integer, MeterTblMonitorType>();
            for (MeterTblMonitorType s : values()) {
                codeToMeterTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the meter type code. Returns null if the
         * code does not exist.
         *
         * @param i
         *            the code to lookup
         * @return enumeration value of the meter type.
         */
        public static MeterTblMonitorType getMeterType(int i) {
            if (codeToMeterTypeMapping == null) {
                initMapping();
            }

            return codeToMeterTypeMapping.get(i);
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
     * Z-Wave MeterScale enumeration. The meter scale indicates the meter scale that is reported.
     *
     */
    @XStreamAlias("meterTblMonitorScale")
    public enum MeterTblMonitorScale {
        E_KWh(0, MeterTblMonitorType.ELECTRIC, "kWh", "Energy"),
        E_KVAh(1, MeterTblMonitorType.ELECTRIC, "kVAh", "Energy"),
        E_W(2, MeterTblMonitorType.ELECTRIC, "W", "Power"),
        E_Pulses(3, MeterTblMonitorType.ELECTRIC, "Pulses", "Count"),
        E_V(4, MeterTblMonitorType.ELECTRIC, "V", "Voltage"),
        E_A(5, MeterTblMonitorType.ELECTRIC, "A", "Current"),
        E_Power_Factor(6, MeterTblMonitorType.ELECTRIC, "Power Factor", "Power Factor"),
        G_Cubic_Meters(0, MeterTblMonitorType.GAS, "Cubic Meters", "Volume"),
        G_Cubic_Feet(1, MeterTblMonitorType.GAS, "Cubic Feet", "Volume"),
        G_Pulses(3, MeterTblMonitorType.GAS, "Pulses", "Count"),
        W_Cubic_Meters(0, MeterTblMonitorType.WATER, "Cubic Meters", "Volume"),
        W_Cubic_Feet(1, MeterTblMonitorType.WATER, "Cubic Feet", "Volume"),
        W_Gallons(2, MeterTblMonitorType.WATER, "US gallons", "Volume"),
        W_Pulses(3, MeterTblMonitorType.WATER, "Pulses", "Count"),
        TE_KWh(0, MeterTblMonitorType.TWIN_ELECTRIC, "kWh", "Energy"),
        TE_KVAh(1, MeterTblMonitorType.TWIN_ELECTRIC, "kVAh", "Energy"),
        TE_W(2, MeterTblMonitorType.TWIN_ELECTRIC, "W", "Power"),
        TE_Pulses(3, MeterTblMonitorType.TWIN_ELECTRIC, "Pulses", "Count"),
        TE_V(4, MeterTblMonitorType.TWIN_ELECTRIC, "V", "Voltage"),
        TE_A(5, MeterTblMonitorType.TWIN_ELECTRIC, "A", "Current");

        private final int scale;
        private final MeterTblMonitorType meterType;
        private final String unit;
        private final String label;

        /**
         * A mapping between the integer code, Meter type and its corresponding Meter scale
         * to facilitate lookup by code.
         */
        private static Map<MeterTblMonitorType, Map<Integer, MeterTblMonitorScale>> codeToMeterScaleMapping;

        /**
         * A mapping between the name,and its corresponding Meter scale to facilitate lookup by enumeration name.
         */
        private static Map<String, MeterTblMonitorScale> nameToMeterScaleMapping;

        /**
         * Constructor. Creates a new enumeration value.
         *
         * @param scale the scale number
         * @param meterType the meter type
         * @param unit the unit
         * @param label the label.
         */
        private MeterTblMonitorScale(int scale, MeterTblMonitorType meterType, String unit, String label) {
            this.scale = scale;
            this.meterType = meterType;
            this.unit = unit;
            this.label = label;
        }

        private static void initMapping() {
            codeToMeterScaleMapping = new HashMap<MeterTblMonitorType, Map<Integer, MeterTblMonitorScale>>();
            nameToMeterScaleMapping = new HashMap<String, MeterTblMonitorScale>();
            for (MeterTblMonitorScale s : values()) {
                if (!codeToMeterScaleMapping.containsKey(s.getMeterType())) {
                    codeToMeterScaleMapping.put(s.getMeterType(), new HashMap<Integer, MeterTblMonitorScale>());
                }
                codeToMeterScaleMapping.get(s.getMeterType()).put(s.getScale(), s);
                nameToMeterScaleMapping.put(s.name().toLowerCase(), s);
            }
        }

        /**
         * Lookup function based on the meter type and code. Returns null if the code does not exist.
         *
         * @param meterType the meter type to use to lookup the scale
         * @param i the code to lookup
         * @return enumeration value of the meter scale.
         */
        public static MeterTblMonitorScale getMeterScale(MeterTblMonitorType meterType, int i) {
            if (codeToMeterScaleMapping == null) {
                initMapping();
            }
            if (!codeToMeterScaleMapping.containsKey(meterType)) {
                return null;
            }

            return codeToMeterScaleMapping.get(meterType).get(i);
        }

        /**
         * Lookup function based on the name. Returns null if the name does not exist.
         *
         * @param name the name to lookup
         * @return enumeration value of the meter scale.
         */
        public static MeterTblMonitorScale getMeterScale(String name) {
            if (nameToMeterScaleMapping == null) {
                initMapping();
            }

            return nameToMeterScaleMapping.get(name.toLowerCase());
        }

        /**
         * Returns the scale code.
         *
         * @return the scale code.
         */
        protected int getScale() {
            return scale;
        }

        /**
         * Returns the meter type.
         *
         * @return the meterType
         */
        protected MeterTblMonitorType getMeterType() {
            return meterType;
        }

        /**
         * Returns the unit as string.
         *
         * @return the unit
         */
        protected String getUnit() {
            return unit;
        }

        /**
         * Returns the label (category).
         *
         * @return the label
         */
        protected String getLabel() {
            return label;
        }

    }

    /**
     * Z-Wave Meter tbl monitor value Event class. Indicates that a meter value changed.
     *
     */
    public class ZWaveMeterTblMonitorValueEvent extends ZWaveCommandClassValueEvent {

        private MeterTblMonitorType meterType;
        private MeterTblMonitorScale meterScale;

        /**
         * Constructor. Creates a instance of the ZWaveMeterTblMonitorValueEvent class.
         *
         * @param nodeId
         *            the nodeId of the event
         * @param endpoint
         *            the endpoint of the event.
         * @param meterType
         *            the meter type that triggered the event;
         * @param meterType
         *            the meter scale for the event;
         * @param value
         *            the value for the event.
         */
        public ZWaveMeterTblMonitorValueEvent(int nodeId, int endpoint, MeterTblMonitorType meterType,
                MeterTblMonitorScale meterScale, Object value) {
            super(nodeId, endpoint, CommandClass.METER_TBL_MONITOR, value);
            this.meterType = meterType;
            this.meterScale = meterScale;
        }

        /**
         * Gets the meter type for this meter tbl monitor value event.
         *
         * @return the meter type for this meter tbl monitor value event.
         */
        public MeterTblMonitorType getMeterType() {
            return meterType;
        }

        /**
         * Gets the meter scale for this meter tbl monitor value event.
         *
         * @return the meter scale for this meter tbl monitor value event.
         */
        public MeterTblMonitorScale getMeterScale() {
            return meterScale;
        }

    }
}
