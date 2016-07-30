/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
 * Handles the Alarm command class.
 * The event is reported as occurs (0xFF) or does not occur (0x00).
 *
 * @author Chris Jackson
 */
@XStreamAlias("alarmCommandClass")
public class ZWaveAlarmCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassDynamicState, ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveAlarmCommandClass.class);

    private static final int MAX_SUPPORTED_VERSION = 3;

    private static final int ALARM_GET = 0x04;
    private static final int ALARM_REPORT = 0x05;

    // Version 2
    private static final int ALARM_SUPPORTED_GET = 0x07;
    private static final int ALARM_SUPPORTED_REPORT = 0x08;

    // Version 3
    private static final int ALARM_EVENTSUPPORTED_GET = 0x01;
    private static final int ALARM_EVENTSUPPORTED_REPORT = 0x02;

    private final Map<AlarmType, Alarm> alarms = new HashMap<AlarmType, Alarm>();

    @XStreamOmitField
    private boolean supportedInitialised = false;
    @XStreamOmitField
    private boolean eventsSupportedInitialised = false;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveAlarmCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveAlarmCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.ALARM;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received Alarm Request V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case ALARM_REPORT:
                logger.debug("NODE {}: Process Alarm Report, V{}, length {}", getNode().getNodeId(), getVersion(),
                        serialMessage.getMessagePayload().length);

                int alarmTypeCode = serialMessage.getMessagePayloadByte(offset + 1);
                int value = serialMessage.getMessagePayloadByte(offset + 2);
                int sensor = 0;
                int event = 0;
                int status = 0;

                // Check if this message is a V1 message based on length
                int version = getVersion();
                if (serialMessage.getMessagePayload().length < 4) {
                    version = 1;
                }

                if (version == 1) {
                    logger.debug("NODE {}: Alarm report - {} = {}", getNode().getNodeId(), alarmTypeCode, value);
                } else {
                    alarmTypeCode = serialMessage.getMessagePayloadByte(offset + 5);
                    sensor = serialMessage.getMessagePayloadByte(offset + 3);
                    event = serialMessage.getMessagePayloadByte(offset + 6);
                    status = serialMessage.getMessagePayloadByte(offset + 4);
                    logger.debug("NODE {}: Alarm report - {} = {}, sensor={}, event={}, status={}",
                            getNode().getNodeId(), alarmTypeCode, value, sensor, event, status);
                }

                AlarmType alarmType = AlarmType.getAlarmType(alarmTypeCode);

                if (alarmType == null) {
                    logger.error("NODE {}: Unknown Alarm Type = {}, ignoring report.", getNode().getNodeId(),
                            alarmTypeCode);
                    return;
                }

                // alarm type seems to be supported, add it to the list.
                Alarm alarm = alarms.get(alarmType);
                if (alarm == null) {
                    alarm = new Alarm(alarmType);
                    this.alarms.put(alarmType, alarm);
                }
                alarm.setInitialised();

                logger.debug("NODE {}: Alarm Type = {} ({})", getNode().getNodeId(), alarmType.getLabel(),
                        alarmTypeCode);

                ZWaveAlarmValueEvent zEvent = new ZWaveAlarmValueEvent(getNode().getNodeId(), endpoint, alarmType,
                        event, status, value);
                this.getController().notifyEventListeners(zEvent);

                dynamicDone = true;
                break;
            case ALARM_SUPPORTED_REPORT:
                logger.debug("NODE {}: Process Alarm Supported Report", getNode().getNodeId());

                int numBytes = serialMessage.getMessagePayloadByte(offset + 1);
                for (int i = 0; i < numBytes; ++i) {
                    for (int bit = 0; bit < 8; ++bit) {
                        if (((serialMessage.getMessagePayloadByte(offset + i + 2)) & (1 << bit)) == 0) {
                            continue;
                        }

                        int index = (i << 3) + bit;
                        if (index >= AlarmType.values().length) {
                            continue;
                        }

                        // (n)th bit is set. n is the index for the alarm type enumeration.
                        // Alarm type seems to be supported, add it to the list if it's not already there.
                        getAlarm(index);
                    }
                }
                supportedInitialised = true;
                break;
            case ALARM_EVENTSUPPORTED_REPORT:
                logger.debug("NODE {}: Process Alarm Event Supported Report", this.getNode().getNodeId());
                int notificationType = serialMessage.getMessagePayloadByte(offset + 1);
                numBytes = serialMessage.getMessagePayloadByte(offset + 2);
                List<Integer> types = new ArrayList<>();
                for (int i = 0; i < numBytes; ++i) {
                    for (int bit = 0; bit < 8; ++bit) {
                        if (((serialMessage.getMessagePayloadByte(offset + i + 3)) & (1 << bit)) == 0) {
                            continue;
                        }

                        int index = (i << 3) + bit;
                        types.add(index);
                        getAlarm(notificationType).getReportedEvents().add(index);
                    }
                }
                logger.debug("NODE {}: AlarmType: {} reported events -> {}", this.getNode().getNodeId(),
                        AlarmType.getAlarmType(notificationType), types);

                eventsSupportedInitialised = true;
                break;

            default:
                logger.warn(String.format("Unsupported Command 0x%02X for command class %s (0x%02X).", command,
                        getCommandClass().getLabel(), getCommandClass().getKey()));
                break;
        }
    }

    private Alarm getAlarm(int alarmTypeCode) {
        AlarmType alarmType = AlarmType.getAlarmType(alarmTypeCode);
        if (alarmType == null) {
            logger.error("NODE {}: Unknown Alarm Type = {}, ignoring report.", getNode().getNodeId(), alarmTypeCode);
            return null;
        }

        // Add alarm to the list if it's not already there.
        Alarm alarm = alarms.get(alarmType);
        if (alarm == null) {
            logger.debug("NODE {}: Adding new alarm type {}({})", getNode().getNodeId(), alarmType.getLabel(),
                    alarmTypeCode);
            alarm = new Alarm(alarmType);
            alarms.put(alarmType, alarm);
        }

        return alarm;
    }

    /**
     * Gets a SerialMessage with the ALARM_GET command
     *
     * @return the serial message
     */
    @Override
    public SerialMessage getValueMessage() {
        // TODO: Why does this return!!!???!!!
        for (Map.Entry<AlarmType, Alarm> entry : alarms.entrySet()) {
            return getMessage(entry.getValue().getAlarmType());
        }

        return getMessage(AlarmType.GENERAL);
    }

    /**
     * Gets a SerialMessage with the SENSOR_ALARM_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public SerialMessage getSupportedMessage() {
        if (getVersion() == 1) {
            logger.debug("NODE {}: ALARM_GET_SUPPORTED not supported for V1", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command ALARM_GET_SUPPORTED", getNode().getNodeId());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.High);
        byte[] newPayload = { (byte) getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
                (byte) ALARM_SUPPORTED_GET };
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Gets a SerialMessage with the ALARM_EVENTSUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public SerialMessage getSupportedEventMessage(int index) {
        if (getVersion() < 2) {
            logger.debug("NODE {}: ALARM_EVENTSUPPORTED_GET not supported for V1-2", this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command ALARM_EVENTSUPPORTED_GET", this.getNode().getNodeId());

        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.High);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 3, (byte) getCommandClass().getKey(),
                (byte) ALARM_EVENTSUPPORTED_GET, (byte) index };
        result.setMessagePayload(newPayload);

        return result;
    }

    /**
     * Gets a SerialMessage with the ALARM_GET command
     *
     * @return the serial message
     */
    public SerialMessage getMessage(AlarmType alarmType) {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command ALARM_GET V{}", getNode().getNodeId(),
                getVersion());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] newPayload = null;
        switch (getVersion()) {
            case 1:
            default:
                newPayload = new byte[] { (byte) getNode().getNodeId(), 3, (byte) getCommandClass().getKey(),
                        (byte) ALARM_GET, (byte) alarmType.getKey() };
                result.setMessagePayload(newPayload);
                break;
            case 2:
                newPayload = new byte[] { (byte) getNode().getNodeId(), 4, (byte) getCommandClass().getKey(),
                        (byte) ALARM_GET, 0, (byte) alarmType.getKey() };
                break;
            case 3:
                newPayload = new byte[] { (byte) getNode().getNodeId(), 5, (byte) getCommandClass().getKey(),
                        (byte) ALARM_GET, 0, (byte) alarmType.getKey(), 1 };
                break;
        }

        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Z-Wave AlarmType enumeration. The alarm type indicates the type of alarm that is reported.
     */
    @XStreamAlias("alarmType")
    public enum AlarmType {
        GENERAL(0, "General"),
        SMOKE(1, "Smoke"),
        CARBON_MONOXIDE(2, "Carbon Monoxide"),
        CARBON_DIOXIDE(3, "Carbon Dioxide"),
        HEAT(4, "Heat"),
        FLOOD(5, "Flood"),
        ACCESS_CONTROL(6, "Access Control"),
        BURGLAR(7, "Burglar"),
        POWER_MANAGEMENT(8, "Power Management"),
        SYSTEM(9, "System"),
        // DEADBOLT_JAMMED(9, "Bolt is Jammed"),
        EMERGENCY(10, "Emergency"),
        CLOCK(11, "Clock"),
        APPLIANCE(12, "Appliance"),
        HOME_HEALTH(13, "Home Health"),
        // CODE_ADDED(13, "Code was Added"),
        UNLOCKED(16, "Unlocked"),
        KEYPAD_LOCKED(18, "Locked with Keypad"),
        KEYPAD_UNLOCK(19, "Unlocked by Keypad"),
        MANUAL_LOCKED(21, "Manual Locked"),
        MANUAL_UNLOCK(22, "Manual unlocked"),
        RF_LOCKED(24, "Locked by RF module"),
        RF_UNLOCK(25, "Unlocked by RF module"),
        AUTO_LOCKED(27, "Auto Locked"),
        ALL_CODES_DELETED(32, "All Codes Deleted"),
        CODE_DELETED(33, "User Code Deleted"),
        CODE_CHANGED(112, "User Code Changed"),
        DUPLICATE_CODE(113, "Duplicate Code"),
        POWER_CYCLED(130, "Power Cycled"),
        TAMPER_ALARM(161, "Failed Code"),
        BATTERY_LOW(167, "Battery Low"),
        BATTERY_CRITICAL(168, "Battery Critical"),
        BATTEERY_TOO_LOW(169, "Battery too low to operate");

        /**
         * A mapping between the integer code and its corresponding Alarm type to facilitate lookup by code.
         */
        private static Map<Integer, AlarmType> codeToAlarmTypeMapping;

        private int key;
        private String label;

        private AlarmType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToAlarmTypeMapping = new HashMap<Integer, AlarmType>();
            for (AlarmType s : values()) {
                codeToAlarmTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the alarm type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the alarm type.
         */
        public static AlarmType getAlarmType(int i) {
            if (codeToAlarmTypeMapping == null) {
                initMapping();
            }

            return codeToAlarmTypeMapping.get(i);
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
     * Class to hold alarm state
     *
     */
    @XStreamAlias("alarmState")
    private class Alarm {
        AlarmType alarmType;

        @XStreamOmitField
        boolean initialised = false;

        List<Integer> reportedEvents = new ArrayList<>();

        public Alarm(AlarmType type) {
            alarmType = type;
        }

        public AlarmType getAlarmType() {
            return alarmType;
        }

        public void setInitialised() {
            initialised = true;
        }

        public boolean getInitialised() {
            return initialised;
        }

        public List<Integer> getReportedEvents() {
            return reportedEvents;
        }
    }

    /**
     * Z-Wave Alarm Event class. Indicates that an alarm value changed.
     */
    public class ZWaveAlarmValueEvent extends ZWaveCommandClassValueEvent {

        private AlarmType alarmType;
        private int alarmEvent;
        private int alarmStatus;

        /**
         * Constructor. Creates a instance of the ZWaveAlarmValueEvent class.
         *
         * @param nodeId the nodeId of the event
         * @param endpoint the endpoint of the event.
         * @param alarmType the alarm type that triggered the event;
         * @param value the value for the event.
         */
        public ZWaveAlarmValueEvent(int nodeId, int endpoint, AlarmType alarmType, int alarmEvent, int alarmStatus,
                Object value) {
            super(nodeId, endpoint, CommandClass.ALARM, value);
            this.alarmType = alarmType;
            this.alarmEvent = alarmEvent;
            this.alarmStatus = alarmStatus;
        }

        /**
         * Gets the alarm type for this alarm value event.
         */
        public AlarmType getAlarmType() {
            return alarmType;
        }

        /**
         * Gets the alarm event for this event
         */
        public int getAlarmEvent() {
            return alarmEvent;
        }

        /**
         * Gets the alarm status for this event
         */
        public int getAlarmStatus() {
            return alarmStatus;
        }

        @Override
        public Integer getValue() {
            return (Integer) super.getValue();
        }
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();

        if (refresh == true) {
            supportedInitialised = false;
            eventsSupportedInitialised = false;
        }

        if (getVersion() > 1 && supportedInitialised == false) {
            result.add(getSupportedMessage());
        }

        if (getVersion() > 2 && eventsSupportedInitialised == false) {
            for (Entry<AlarmType, Alarm> alarmEntry : alarms.entrySet()) {
                if (alarmEntry.getValue().getReportedEvents().isEmpty()) {
                    result.add(getSupportedEventMessage(alarmEntry.getKey().key));
                }
            }
        }

        return result;
    }

    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();

        for (Map.Entry<AlarmType, Alarm> entry : this.alarms.entrySet()) {
            if (refresh == true || entry.getValue().getInitialised() == false) {
                result.add(getMessage(entry.getValue().getAlarmType()));
            }
        }

        return result;
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        // TODO: False logic!
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }
}
