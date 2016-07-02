/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.proprietary.FibaroFGRM222CommandClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Z-Wave Command Class. Z-Wave device functions are controlled by command classes. A command class can be have one or
 * multiple commands allowing the use of a certain function of the device.
 *
 * @author Chris Jackson
 * @author Brian Crosby
 */
public abstract class ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveCommandClass.class);

    private static final int SIZE_MASK = 0x07;
    // private static final SCALE_MASK = 0x18; // unused
    // private static final SCALE_SHIFT = 0x03; // unused
    private static final int PRECISION_MASK = 0xe0;
    private static final int PRECISION_SHIFT = 0x05;

    @XStreamOmitField
    private ZWaveNode node;
    @XStreamOmitField
    private ZWaveController controller;

    private ZWaveEndpoint endpoint;

    private int version = 0;
    private int instances = 0;

    @XStreamOmitField
    protected int versionMax = 1;

    @SuppressWarnings("unused")
    private int versionSupported = 0;

    /**
     * Protected constructor. Initiates a new instance of a Command Class.
     *
     * @param node the node this instance commands.
     * @param controller the controller to send messages to.
     * @param endpoint the endpoint this Command class belongs to.
     */
    protected ZWaveCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        this.node = node;
        this.controller = controller;
        this.endpoint = endpoint;
        logger.debug("NODE {}: Command class {}, endpoint {} created", node.getNodeId(), getCommandClass().getLabel(),
                endpoint);
    }

    /**
     * Returns the node this command class belongs to.
     *
     * @return node
     */
    protected ZWaveNode getNode() {
        return node;
    }

    /**
     * Sets the node this command class belongs to.
     *
     * @param node the node to set
     */
    public void setNode(ZWaveNode node) {
        this.node = node;
    }

    /**
     * Returns the controller to send messages to.
     *
     * @return controller
     */
    protected ZWaveController getController() {
        return controller;
    }

    /**
     * Sets the controller to send messages to.
     *
     * @param controller the controller to set
     */
    public void setController(ZWaveController controller) {
        this.controller = controller;
    }

    /**
     * Returns the endpoint this command class belongs to.
     *
     * @return the endpoint this command class belongs to.
     */
    public ZWaveEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the endpoint this command class belongs to.
     *
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(ZWaveEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Returns the version of the command class.
     *
     * @return node
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version number for this command class.
     *
     * @param version. The version number to set.
     */
    public void setVersion(int version) {
        // Record the version supported by the device
        // This gets recorded in the XML so we know more about the device
        versionSupported = version;

        // Now set the version we are actually going to support
        if (version > versionMax) {
            this.version = versionMax;
            logger.debug(
                    "NODE {}: Version = {}, version set to maximum supported by the binding. Enabling extra functionality.",
                    getNode().getNodeId(), versionMax);
        } else {
            this.version = version;
            logger.debug("NODE {}: Version = {}, version set. Enabling extra functionality.", getNode().getNodeId(),
                    version);
        }
    }

    /**
     * The maximum version implemented by this command class.
     */
    public int getMaxVersion() {
        return versionMax;
    }

    /**
     * Set options for this command class.
     * Options are provided from the device configuration database
     *
     * @param options class
     * @return true if options set ok
     */
    public boolean setOptions(Map<String, String> options) {
        return false;
    }

    /**
     * Returns the number of instances of this command class
     * in case the node supports the MULTI_INSTANCE command class (Version 1).
     *
     * @return the number of instances
     */
    public int getInstances() {
        return instances;
    }

    /**
     * Returns the number of instances of this command class
     * in case the node supports the MULTI_INSTANCE command class (Version 1).
     *
     * @param instances. The number of instances.
     */
    public void setInstances(int instances) {
        this.instances = instances;
    }

    /**
     * Returns the command class.
     *
     * @return command class
     */
    public abstract CommandClass getCommandClass();

    /**
     * Handles an incoming application command request.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     */
    public abstract void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException;

    /**
     * Gets an instance of the right command class.
     * Returns null if the command class is not found.
     *
     * @param i the code to instantiate
     * @param node the node this instance commands.
     * @param controller the controller to send messages to.
     * @return the ZWaveCommandClass instance that was instantiated, null otherwise
     */
    public static ZWaveCommandClass getInstance(int i, ZWaveNode node, ZWaveController controller) {
        return ZWaveCommandClass.getInstance(i, node, controller, null);
    }

    /**
     * Gets an instance of the right command class.
     * Returns null if the command class is not found.
     *
     * @param classId the code to instantiate
     * @param node the node this instance commands.
     * @param controller the controller to send messages to.
     * @param endpoint the endpoint this Command class belongs to
     * @return the ZWaveCommandClass instance that was instantiated, null otherwise
     */
    public static ZWaveCommandClass getInstance(int classId, ZWaveNode node, ZWaveController controller,
            ZWaveEndpoint endpoint) {
        try {
            CommandClass commandClass = CommandClass.getCommandClass(classId);
            if (commandClass != null && commandClass.equals(CommandClass.MANUFACTURER_PROPRIETARY)) {
                commandClass = CommandClass.getCommandClass(node.getManufacturer(), node.getDeviceType());
            }
            if (commandClass == null) {
                logger.warn(String.format("NODE %d: Unknown command class 0x%02x", node.getNodeId(), classId));
                return null;
            }
            Class<? extends ZWaveCommandClass> commandClassClass = commandClass.getCommandClassClass();

            if (commandClassClass == null) {
                logger.warn("NODE {}: Unsupported command class {}", node.getNodeId(), commandClass.getLabel(),
                        classId);
                return null;
            }
            logger.debug("NODE {}: Creating new instance of command class {}", node.getNodeId(),
                    commandClass.getLabel());

            Constructor<? extends ZWaveCommandClass> constructor = commandClassClass.getConstructor(ZWaveNode.class,
                    ZWaveController.class, ZWaveEndpoint.class);
            return constructor.newInstance(new Object[] { node, controller, endpoint });
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.error(String.format("NODE %d: Error instantiating command class 0x%02x", node.getNodeId(), classId));
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract a decimal value from a byte array.
     *
     * @param buffer the buffer to be parsed.
     * @param offset the offset at which to start reading
     * @return the extracted decimal value
     */
    protected BigDecimal extractValue(byte[] buffer, int offset) {
        int size = buffer[offset] & SIZE_MASK;
        int precision = (buffer[offset] & PRECISION_MASK) >> PRECISION_SHIFT;

        if ((size + offset) >= buffer.length) {
            logger.error("Error extracting value - length={}, offset={}, size={}.", buffer.length, offset, size);
            throw new NumberFormatException();
        }

        int value = 0;
        int i;
        for (i = 0; i < size; ++i) {
            value <<= 8;
            value |= buffer[offset + i + 1] & 0xFF;
        }

        // Deal with sign extension. All values are signed
        BigDecimal result;
        if ((buffer[offset + 1] & 0x80) == 0x80) {
            // MSB is signed
            if (size == 1) {
                value |= 0xffffff00;
            } else if (size == 2) {
                value |= 0xffff0000;
            }
        }

        result = BigDecimal.valueOf(value);

        BigDecimal divisor = BigDecimal.valueOf(Math.pow(10, precision));
        return result.divide(divisor);
    }

    /**
     * Extract a decimal value from a byte array.
     *
     * @param buffer the buffer to be parsed.
     * @param offset the offset at which to start reading
     * @return the extracted decimal value
     */
    protected int extractValue(byte[] buffer, int offset, int size) {
        int value = 0;
        for (int i = 0; i < size; ++i) {
            value <<= 8;
            value |= buffer[offset + i] & 0xFF;
        }

        // Deal with sign extension. All values are signed
        if ((buffer[offset] & 0x80) == 0x80) {
            // MSB is signed
            if (size == 1) {
                value |= 0xffffff00;
            } else if (size == 2) {
                value |= 0xffff0000;
            }
        }

        return value;
    }

    /**
     * Encodes a decimal value as a byte array.
     *
     * @param value the decimal value to encode
     * @param index the value index
     * @return the value buffer
     * @throws ArithmeticException when the supplied value is out of range.
     * @since 1.4.0
     */
    protected byte[] encodeValue(BigDecimal value) throws ArithmeticException {

        // Remove any trailing zero's so we send the least amount of bytes possible
        BigDecimal normalizedValue = value.stripTrailingZeros();

        // Make our scale at least 0, precision cannot be more than 7 but
        // this is guarded by the Integer min / max values already.
        if (normalizedValue.scale() < 0) {
            normalizedValue = normalizedValue.setScale(0);
        }

        if (normalizedValue.unscaledValue().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new ArithmeticException();
        } else if (normalizedValue.unscaledValue().compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
            throw new ArithmeticException();
        }

        // default size = 4
        int size = 4;

        // it might fit in a byte or short
        if (normalizedValue.unscaledValue().intValue() >= Byte.MIN_VALUE
                && normalizedValue.unscaledValue().intValue() <= Byte.MAX_VALUE) {
            size = 1;
        } else if (normalizedValue.unscaledValue().intValue() >= Short.MIN_VALUE
                && normalizedValue.unscaledValue().intValue() <= Short.MAX_VALUE) {
            size = 2;
        }

        int precision = normalizedValue.scale();

        byte[] result = new byte[size + 1];
        // precision + scale (unused) + size
        result[0] = (byte) ((precision << PRECISION_SHIFT) | size);
        int unscaledValue = normalizedValue.unscaledValue().intValue(); // ie. 22.5 = 225
        for (int i = 0; i < size; i++) {
            result[size - i] = (byte) ((unscaledValue >> (i * 8)) & 0xFF);
        }
        return result;
    }

    /**
     * Command class enumeration. Lists all command classes available.
     * Unsupported command classes by the binding return null for the command class Class.
     *
     * @author Jan-Willem Spuij
     */
    @XStreamAlias("commandClass")
    public enum CommandClass {
        NO_OPERATION(0x00, "NO_OPERATION", ZWaveNoOperationCommandClass.class),
        BASIC(0x20, "BASIC", ZWaveBasicCommandClass.class),
        CONTROLLER_REPLICATION(0x21, "CONTROLLER_REPLICATION", ZWaveControllerReplicationCommandClass.class),
        APPLICATION_STATUS(0x22, "APPLICATION_STATUS", ZWaveApplicationStatusClass.class),
        ZIP_SERVICES(0x23, "ZIP_SERVICES", null),
        ZIP_SERVER(0x24, "ZIP_SERVER", null),
        SWITCH_BINARY(0x25, "SWITCH_BINARY", ZWaveBinarySwitchCommandClass.class),
        SWITCH_MULTILEVEL(0x26, "SWITCH_MULTILEVEL", ZWaveMultiLevelSwitchCommandClass.class),
        SWITCH_ALL(0x27, "SWITCH_ALL", ZWaveSwitchAllCommandClass.class),
        SWITCH_TOGGLE_BINARY(0x28, "SWITCH_TOGGLE_BINARY", ZWaveBinaryToggleSwitchCommandClass.class),
        SWITCH_TOGGLE_MULTILEVEL(0x29, "SWITCH_TOGGLE_MULTILEVEL", ZWaveMultiLevelToggleSwitchCommandClass.class),
        CHIMNEY_FAN(0x2A, "CHIMNEY_FAN", ZWaveChimneyFanCommandClass.class),
        SCENE_ACTIVATION(0x2B, "SCENE_ACTIVATION", ZWaveSceneActivationCommandClass.class),
        SCENE_ACTUATOR_CONF(0x2C, "SCENE_ACTUATOR_CONF", ZWaveSceneActuatorConfigurationCommandClass.class),
        SCENE_CONTROLLER_CONF(0x2D, "SCENE_CONTROLLER_CONF", ZWaveSceneControllerConfigurationCommandClass.class),
        ZIP_CLIENT(0x2E, "ZIP_CLIENT", null),
        ZIP_ADV_SERVICES(0x2F, "ZIP_ADV_SERVICES", null),
        SENSOR_BINARY(0x30, "SENSOR_BINARY", ZWaveBinarySensorCommandClass.class),
        SENSOR_MULTILEVEL(0x31, "SENSOR_MULTILEVEL", ZWaveMultiLevelSensorCommandClass.class),
        METER(0x32, "METER", ZWaveMeterCommandClass.class),
        COLOR(0x33, "COLOR", ZWaveColorCommandClass.class),
        ZIP_ADV_CLIENT(0x34, "ZIP_ADV_CLIENT", null),
        METER_PULSE(0x35, "METER_PULSE", ZWaveMeterPulseCommandClass.class),
        METER_TBL_CONFIG(0x3C, "METER_TBL_CONFIG", ZWaveMeterTblConfigurationCommandClass.class),
        METER_TBL_MONITOR(0x3D, "METER_TBL_MONITOR", ZWaveMeterTblMonitorCommandClass.class),
        METER_TBL_PUSH(0x3E, "METER_TBL_PUSH", null),
        HRV_STATUS(0x37, "HRV_STATUS", ZWaveHrvStatusCommandClass.class),
        THERMOSTAT_HEATING(0x38, "THERMOSTAT_HEATING", null),
        HRV_CONTROL(0x39, "HRV_CONTROL", ZWaveHrvControlCommandClass.class),
        THERMOSTAT_MODE(0x40, "THERMOSTAT_MODE", ZWaveThermostatModeCommandClass.class),
        THERMOSTAT_OPERATING_STATE(0x42, "THERMOSTAT_OPERATING_STATE", ZWaveThermostatOperatingStateCommandClass.class),
        THERMOSTAT_SETPOINT(0x43, "THERMOSTAT_SETPOINT", ZWaveThermostatSetpointCommandClass.class),
        THERMOSTAT_FAN_MODE(0x44, "THERMOSTAT_FAN_MODE", ZWaveThermostatFanModeCommandClass.class),
        THERMOSTAT_FAN_STATE(0x45, "THERMOSTAT_FAN_STATE", ZWaveThermostatFanStateCommandClass.class),
        CLIMATE_CONTROL_SCHEDULE(0x46, "CLIMATE_CONTROL_SCHEDULE", ZWaveClimateControlScheduleCommandClass.class),
        THERMOSTAT_SETBACK(0x47, "THERMOSTAT_SETBACK", ZWaveThermostatSetbackCommandClass.class),
        DOOR_LOCK_LOGGING(0x4C, "DOOR_LOCK_LOGGING", ZWaveDoorLockLoggingCommandClass.class),
        SCHEDULE_ENTRY_LOCK(0x4E, "SCHEDULE_ENTRY_LOCK", ZWaveScheduleEntryLockCommandClass.class),
        BASIC_WINDOW_COVERING(0x50, "BASIC_WINDOW_COVERING", ZWaveBasicWindowCoveringCommandClass.class),
        MTP_WINDOW_COVERING(0x51, "MTP_WINDOW_COVERING", ZWaveMtpWindowCoveringCommandClass.class),
        SCHEDULE(0x53, "SCHEDULE", ZWaveScheduleCommandClass.class),
        CRC_16_ENCAP(0x56, "CRC_16_ENCAP", ZWaveCRC16EncapsulationCommandClass.class),
        ASSOCIATION_GROUP_INFO(0x59, "ASSOCIATION_GROUP_INFO", ZwaveAssociationGroupInfoCommandClass.class),
        DEVICE_RESET_LOCALLY(0x5a, "DEVICE_RESET_LOCALLY", ZWaveDeviceResetLocallyCommandClass.class),
        CENTRAL_SCENE(0x5b, "CENTRAL_SCENE", ZWaveCentralSceneCommandClass.class),
        ZWAVE_PLUS_INFO(0x5e, "ZWAVE_PLUS_INFO", ZWavePlusCommandClass.class),
        MULTI_INSTANCE(0x60, "MULTI_INSTANCE", ZWaveMultiInstanceCommandClass.class),
        DOOR_LOCK(0x62, "DOOR_LOCK", ZWaveDoorLockCommandClass.class),
        USER_CODE(0x63, "USER_CODE", ZWaveUserCodeCommandClass.class),
        BARRIER_OPERATOR(0x66, "BARRIER_OPERATOR", ZWaveBarrierOperatorCommandClass.class),
        CONFIGURATION(0x70, "CONFIGURATION", ZWaveConfigurationCommandClass.class),
        ALARM(0x71, "ALARM", ZWaveAlarmCommandClass.class),
        MANUFACTURER_SPECIFIC(0x72, "MANUFACTURER_SPECIFIC", ZWaveManufacturerSpecificCommandClass.class),
        POWERLEVEL(0x73, "POWERLEVEL", ZWavePowerLevelCommandClass.class),
        PROTECTION(0x75, "PROTECTION", ZWaveProtectionCommandClass.class),
        LOCK(0x76, "LOCK", ZWaveLockCommandClass.class),
        NODE_NAMING(0x77, "NODE_NAMING", ZWaveNodeNamingCommandClass.class),
        FIRMWARE_UPDATE_MD(0x7A, "FIRMWARE_UPDATE_MD", ZWaveFirmwareUpdateCommandClass.class),
        GROUPING_NAME(0x7B, "GROUPING_NAME", ZWaveGroupingNameCommandClass.class),
        REMOTE_ASSOCIATION_ACTIVATE(0x7C, "REMOTE_ASSOCIATION_ACTIVATE", null),
        REMOTE_ASSOCIATION(0x7D, "REMOTE_ASSOCIATION", null),
        BATTERY(0x80, "BATTERY", ZWaveBatteryCommandClass.class),
        CLOCK(0x81, "CLOCK", ZWaveClockCommandClass.class),
        HAIL(0x82, "HAIL", ZWaveHailCommandClass.class),
        WAKE_UP(0x84, "WAKE_UP", ZWaveWakeUpCommandClass.class),
        ASSOCIATION(0x85, "ASSOCIATION", ZWaveAssociationCommandClass.class),
        VERSION(0x86, "VERSION", ZWaveVersionCommandClass.class),
        INDICATOR(0x87, "INDICATOR", ZWaveIndicatorCommandClass.class),
        PROPRIETARY(0x88, "PROPRIETARY", null),
        LANGUAGE(0x89, "LANGUAGE", ZWaveLanguageCommandClass.class),
        TIME(0x8A, "TIME", ZWaveTimeCommandClass.class),
        TIME_PARAMETERS(0x8B, "TIME_PARAMETERS", ZWaveTimeParametersCommandClass.class),
        GEOGRAPHIC_LOCATION(0x8C, "GEOGRAPHIC_LOCATION", null),
        COMPOSITE(0x8D, "COMPOSITE", null),
        MULTI_INSTANCE_ASSOCIATION(0x8E, "MULTI_INSTANCE_ASSOCIATION", ZWaveMultiAssociationCommandClass.class),
        MULTI_CMD(0x8F, "MULTI_CMD", ZWaveMultiCommandCommandClass.class),
        ENERGY_PRODUCTION(0x90, "ENERGY_PRODUCTION", ZWaveEnergyProductionCommandClass.class),
        // Note that MANUFACTURER_PROPRIETARY shouldn't be instantiated directly
        // The getInstance method will catch this and translate to the correct
        // class for the device.
        MANUFACTURER_PROPRIETARY(0x91, "MANUFACTURER_PROPRIETARY", null),
        SCREEN_MD(0x92, "SCREEN_MD", null),
        SCREEN_ATTRIBUTES(0x93, "SCREEN_ATTRIBUTES", null),
        SIMPLE_AV_CONTROL(0x94, "SIMPLE_AV_CONTROL", null),
        AV_CONTENT_DIRECTORY_MD(0x95, "AV_CONTENT_DIRECTORY_MD", null),
        AV_RENDERER_STATUS(0x96, "AV_RENDERER_STATUS", null),
        AV_CONTENT_SEARCH_MD(0x97, "AV_CONTENT_SEARCH_MD", null),
        SECURITY(0x98, "SECURITY", ZWaveSecurityCommandClassWithInitialization.class),
        AV_TAGGING_MD(0x99, "AV_TAGGING_MD", null),
        IP_CONFIGURATION(0x9A, "IP_CONFIGURATION", null),
        ASSOCIATION_COMMAND_CONFIGURATION(0x9B, "ASSOCIATION_COMMAND_CONFIGURATION", null),
        SENSOR_ALARM(0x9C, "SENSOR_ALARM", ZWaveAlarmSensorCommandClass.class),
        SILENCE_ALARM(0x9D, "SILENCE_ALARM", ZWaveAlarmSilenceCommandClass.class),
        SENSOR_CONFIGURATION(0x9E, "SENSOR_CONFIGURATION", ZWaveSensorConfigurationCommandClass.class),
        MARK(0xEF, "MARK", null),
        NON_INTEROPERABLE(0xF0, "NON_INTEROPERABLE", null),

        // MANUFACTURER_PROPRIETARY class definitions are defined by the manufacturer and device id
        FIBARO_FGRM_222(0x010F, 0x0301, "FIBARO_FGRM_222", FibaroFGRM222CommandClass.class);

        /**
         * A mapping between the integer code and its corresponding
         * Command class to facilitate lookup by code.
         */
        private static Map<Integer, CommandClass> codeToCommandClassMapping;

        /**
         * A mapping between the string label and its corresponding
         * Command class to facilitate lookup by label.
         */
        private static Map<String, CommandClass> labelToCommandClassMapping;

        /**
         * Get unique command class code for manufacturer and device ID.
         *
         * To support manufacturer specific implementations of a manufacturer proprietary command class we use the
         * manufacturer and the device id to generate a unique key.
         *
         * @param manufacturer the manufacturer ID
         * @param deviceId the device ID
         * @return a unique command class key
         */
        private static int getKeyFromManufacturerAndDeviceId(int manufacturer, int deviceId) {
            return manufacturer << 16 | deviceId;
        }

        private int key;
        private String label;
        private Class<? extends ZWaveCommandClass> commandClassClass;

        private CommandClass(int key, String label, Class<? extends ZWaveCommandClass> commandClassClass) {
            this.key = key;
            this.label = label;
            this.commandClassClass = commandClassClass;
        }

        private CommandClass(int manufacturer, int deviceId, String label,
                Class<? extends ZWaveCommandClass> commandClassClass) {
            this(getKeyFromManufacturerAndDeviceId(manufacturer, deviceId), label, commandClassClass);
        }

        private static void initMapping() {
            codeToCommandClassMapping = new HashMap<Integer, CommandClass>();
            labelToCommandClassMapping = new HashMap<String, CommandClass>();
            for (CommandClass s : values()) {
                codeToCommandClassMapping.put(s.key, s);
                labelToCommandClassMapping.put(s.label.toLowerCase(), s);
            }
        }

        /**
         * Lookup function based on the command class code.
         * Returns null if there is no command class with code i
         *
         * @param i the code to lookup
         * @return enumeration value of the command class.
         */
        public static CommandClass getCommandClass(int i) {
            if (codeToCommandClassMapping == null) {
                initMapping();
            }

            return codeToCommandClassMapping.get(i);
        }

        /**
         * Lookup function based on the manufacturer and device ID.
         *
         * @param manufacturer the manufacturer ID
         * @param deviceId the device ID
         * @return enumeration value of the command class or null if there is no command class.
         */
        public static CommandClass getCommandClass(int manufacturer, int deviceId) {
            return getCommandClass(getKeyFromManufacturerAndDeviceId(manufacturer, deviceId));
        }

        /**
         * Lookup function based on the command class label.
         * Returns null if there is no command class with that label.
         *
         * @param label the label to lookup
         * @return enumeration value of the command class.
         */
        public static CommandClass getCommandClass(String label) {
            if (labelToCommandClassMapping == null) {
                initMapping();
            }

            return labelToCommandClassMapping.get(label.toLowerCase());
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

        /**
         * @return the command class Class
         */
        public Class<? extends ZWaveCommandClass> getCommandClassClass() {
            return commandClassClass;
        }
    }
}
