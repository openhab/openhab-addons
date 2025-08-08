/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.insteon.internal.device.feature;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.InsteonEngine;
import org.openhab.binding.insteon.internal.device.RampRate;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ButtonEvent;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.FanLincFanSpeed;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.IMButtonEvent;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.IOLincRelayMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.KeypadButtonConfig;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.KeypadButtonToggleMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.MicroModuleOpMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.RemoteSceneButtonConfig;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.RemoteSwitchButtonConfig;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.SirenAlertType;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ThermostatFanMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ThermostatSystemMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ThermostatSystemState;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ThermostatTemperatureScale;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ThermostatTimeFormat;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.VenstarSystemMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.X10Event;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.BinaryUtils;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.binding.insteon.internal.utils.ParameterParser;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message handler processes incoming Insteon messages
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Bernd Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public abstract class MessageHandler extends BaseFeatureHandler {
    private static final Set<Integer> SUPPORTED_GROUP_COMMANDS = Set.of(0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
            0x2E);

    protected final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    public MessageHandler(DeviceFeature feature) {
        super(feature);
    }

    /**
     * Returns handler id
     *
     * @return handler id based on command and group parameters
     */
    public String getId() {
        int command = getParameterAsInteger("command", -1);
        int group = getGroup();
        return MessageHandler.generateId(command, group);
    }

    /**
     * Returns handler group
     *
     * @return handler group based on feature or handler group parameter, if supports group, otherwise -1
     */
    public int getGroup() {
        int command = getParameterAsInteger("command", -1);
        // return -1 if handler doesn't support groups
        if (!MessageHandler.supportsGroup(command)) {
            return -1;
        }
        int group = ParameterParser.getParameterAsOrDefault(parameters.get("group"), Integer.class, -1);
        // return handler group parameter if non-standard
        if (group > 1) {
            return group;
        }
        // return feature group parameter if defined, otherwise handler group parameter
        return feature.getParameterAsInteger("group", group);
    }

    /**
     * Returns if can handle a given message
     *
     * @param msg the message to be handled
     * @return true if handler not duplicate, valid and matches filter parameters
     */
    public boolean canHandle(Msg msg) {
        if (isDuplicate(msg)) {
            logger.trace("{}:{} ignoring msg as duplicate", getDevice().getAddress(), feature.getName());
            return false;
        } else if (!isValid(msg)) {
            logger.trace("{}:{} ignoring msg as not valid", getDevice().getAddress(), feature.getName());
            return false;
        } else if (!matchesFilters(msg)) {
            logger.trace("{}:{} ignoring msg as unmatch filters", getDevice().getAddress(), feature.getName());
            return false;
        }
        return true;
    }

    /**
     * Returns if an incoming message is a duplicate
     *
     * @param msg the received message
     * @return true if group or broadcast message is duplicate
     */
    protected boolean isDuplicate(Msg msg) {
        if (msg.isAllLinkBroadcastOrCleanup() || msg.isBroadcast()) {
            return getInsteonDevice().isDuplicateMsg(msg);
        }
        return false;
    }

    /**
     * Returns if an incoming DIRECT message is valid
     *
     * @param msg the received DIRECT message
     * @return true if this message is valid
     */
    protected boolean isValid(Msg msg) {
        if (msg.isDirect()) {
            int ext = getParameterAsInteger("ext", -1);
            // extended message crc is only included in incoming message when using the newer 2-byte method
            if (ext == 2) {
                return msg.hasValidCRC2();
            }
        }
        return true;
    }

    /**
     * Returns if message matches the filter parameters
     *
     * @param msg message to check
     * @return true if message matches
     */
    protected boolean matchesFilters(Msg msg) {
        try {
            int ext = getParameterAsInteger("ext", -1);
            if (ext != -1) {
                if ((!msg.isExtended() && ext != 0) || (msg.isExtended() && ext != 1 && ext != 2)) {
                    return false;
                }
                if (!matchesParameter(msg, "command1", "cmd1")) {
                    return false;
                }
            }
            if (!matchesParameter(msg, "command2", "cmd2")) {
                return false;
            }
            if (!matchesParameter(msg, "userData1", "d1")) {
                return false;
            }
            if (!matchesParameter(msg, "userData2", "d2")) {
                return false;
            }
            if (!matchesParameter(msg, "userData3", "d3")) {
                return false;
            }
        } catch (FieldException e) {
            logger.warn("error matching message: {}", msg, e);
            return false;
        }
        return true;
    }

    /**
     * Returns if parameter matches value
     *
     * @param msg message to check
     * @param field field name to match
     * @param param name of parameter to match
     * @return true if parameter matches
     * @throws FieldException if field not there
     */
    private boolean matchesParameter(Msg msg, String field, String param) throws FieldException {
        int value = getParameterAsInteger(param, -1);
        // parameter not filtered for, declare this a match!
        return value == -1 || msg.getInt(field) == value;
    }

    /**
     * Handles incoming message. The cmd1 parameter
     * has been extracted earlier already (to make a decision which message handler to call),
     * and is passed in as an argument so cmd1 does not have to be extracted from the message again.
     *
     * @param cmd1 the insteon cmd1 field
     * @param msg the received insteon message
     */
    public abstract void handleMessage(byte cmd1, Msg msg);

    /**
     * Default message handler
     */
    public static class DefaultMsgHandler extends MessageHandler {
        DefaultMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: ignoring unimpl message with cmd1 {}", nm(), HexUtils.getHexString(cmd1));
            }
        }
    }

    /**
     * No-op message handler
     */
    public static class NoOpMsgHandler extends MessageHandler {
        NoOpMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            if (logger.isTraceEnabled()) {
                logger.trace("{}: ignoring message with cmd1 {}", nm(), HexUtils.getHexString(cmd1));
            }
        }
    }

    /**
     * Trigger poll message handler
     */
    public static class TriggerPollMsgHandler extends MessageHandler {
        TriggerPollMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            // trigger poll with delay based on parameter, defaulting to 0 ms
            long delay = getParameterAsLong("delay", 0L);
            feature.triggerPoll(delay);
        }
    }

    /**
     * Custom state abstract message handler based of parameters
     */
    public abstract static class CustomMsgHandler extends MessageHandler {
        CustomMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            try {
                // extract raw value from message
                int raw = getRawValue(msg);
                // apply mask and right shift bit manipulation
                int cooked = (raw & getParameterAsInteger("mask", 0xFF)) >> getParameterAsInteger("rshift", 0);
                // multiply with factor and add offset
                double value = cooked * getParameterAsDouble("factor", 1.0) + getParameterAsDouble("offset", 0.0);
                // get state to update
                State state = getState(cmd1, value);
                // store extracted cooked message value
                feature.setLastMsgValue(value);
                // update state if defined
                if (state != null) {
                    logger.debug("{}: device {} {} is {}", nm(), getInsteonDevice().getAddress(), feature.getName(),
                            state);
                    feature.updateState(state);
                }
            } catch (FieldException e) {
                logger.warn("{}: error parsing msg {}", nm(), msg, e);
            }
        }

        private int getRawValue(Msg msg) throws FieldException {
            // determine data field name based on parameter, default to cmd2 if is standard message
            String field = getParameterAsString("field", !msg.isExtended() ? "command2" : "");
            if (field.isEmpty()) {
                throw new FieldException("handler misconfigured, no field parameter specified!");
            }
            if (field.startsWith("address") && !msg.isBroadcast() && !msg.isAllLinkBroadcast()) {
                throw new FieldException("not broadcast msg, cannot use address bytes!");
            }
            // return raw value based on field name
            switch (field) {
                case "group":
                    return msg.getGroup();
                case "addressHighByte":
                    // return broadcast address high byte value
                    return msg.getInsteonAddress("toAddress").getHighByte() & 0xFF;
                case "addressMiddleByte":
                    // return broadcast address middle byte value
                    return msg.getInsteonAddress("toAddress").getMiddleByte() & 0xFF;
                case "addressLowByte":
                    // return broadcast address low byte value
                    return msg.getInsteonAddress("toAddress").getLowByte() & 0xFF;
                default:
                    // return integer value starting from field name up to 4-bytes in size based on parameter
                    return msg.getInt(field, getParameterAsInteger("num_bytes", 1));
            }
        }

        protected abstract @Nullable State getState(byte cmd1, double value);
    }

    /**
     * Custom bitmask message handler based of parameters
     */
    public static class CustomBitmaskMsgHandler extends CustomMsgHandler {
        CustomBitmaskMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            State state = null;
            // get bit number based on parameter
            int bit = getBitNumber();
            // get bit state from bitmask value, if bit defined
            if (bit != -1) {
                boolean isSet = BinaryUtils.isBitSet((int) value, bit);
                state = getBitState(isSet);
            } else {
                logger.debug("{}: invalid bit number defined for {}", nm(), feature.getName());
            }
            return state;
        }

        protected int getBitNumber() {
            int bit = getParameterAsInteger("bit", -1);
            // return bit if valid (0-7), otherwise -1
            return bit >= 0 && bit <= 7 ? bit : -1;
        }

        protected State getBitState(boolean isSet) {
            return OnOffType.from(isSet ^ getParameterAsBoolean("inverted", false));
        }
    }

    /**
     * Custom cache message handler based of parameters
     */
    public static class CustomCacheMsgHandler extends CustomMsgHandler {
        CustomCacheMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            // only cache extracted message value
            // mostly used for hidden features which are used by others
            return null;
        }
    }

    /**
     * Custom decimal type message handler based of parameters
     */
    public static class CustomDecimalMsgHandler extends CustomMsgHandler {
        CustomDecimalMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return new DecimalType(value);
        }
    }

    /**
     * Custom on/off type message handler based of parameters
     */
    public static class CustomOnOffMsgHandler extends CustomMsgHandler {
        CustomOnOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            int onLevel = getParameterAsInteger("on", 0xFF);
            int offLevel = getParameterAsInteger("off", 0x00);
            return value == onLevel ? OnOffType.ON : value == offLevel ? OnOffType.OFF : null;
        }
    }

    /**
     * Custom percent type message handler based of parameters
     */
    public static class CustomPercentMsgHandler extends CustomMsgHandler {
        CustomPercentMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            int minValue = getParameterAsInteger("min", 0x00);
            int maxValue = getParameterAsInteger("max", 0xFF);
            double clampValue = Math.max(minValue, Math.min(maxValue, value));
            int level = (int) Math.round((clampValue - minValue) / (maxValue - minValue) * 100);
            return new PercentType(level);
        }
    }

    /**
     * Custom dimensionless quantity type message handler based of parameters
     */
    public static class CustomDimensionlessMsgHandler extends CustomMsgHandler {
        CustomDimensionlessMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            int minValue = getParameterAsInteger("min", 0);
            int maxValue = getParameterAsInteger("max", 100);
            double clampValue = Math.max(minValue, Math.min(maxValue, value));
            int level = (int) Math.round((clampValue - minValue) * 100 / (maxValue - minValue));
            return new QuantityType<Dimensionless>(level, Units.PERCENT);
        }
    }

    /**
     * Custom temperature quantity type message handler based of parameters
     */
    public static class CustomTemperatureMsgHandler extends CustomMsgHandler {
        CustomTemperatureMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            Unit<Temperature> unit = getTemperatureUnit();
            return new QuantityType<Temperature>(value, unit);
        }

        protected Unit<Temperature> getTemperatureUnit() {
            String scale = getParameterAsString("scale", "");
            switch (scale) {
                case "celsius":
                    return SIUnits.CELSIUS;
                case "fahrenheit":
                    return ImperialUnits.FAHRENHEIT;
                default:
                    logger.debug("{}: no valid temperature scale parameter found, defaulting to: CELSIUS", nm());
                    return SIUnits.CELSIUS;
            }
        }
    }

    /**
     * Custom time quantity type message handler based of parameters
     */
    public static class CustomTimeMsgHandler extends CustomMsgHandler {
        CustomTimeMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            Unit<Time> unit = getTimeUnit();
            return new QuantityType<Time>(value, unit);
        }

        protected Unit<Time> getTimeUnit() {
            String scale = getParameterAsString("scale", "");
            switch (scale) {
                case "hour":
                    return Units.HOUR;
                case "minute":
                    return Units.MINUTE;
                case "second":
                    return Units.SECOND;
                default:
                    logger.debug("{}: no valid time scale parameter found, defaulting to: SECONDS", nm());
                    return Units.SECOND;
            }
        }
    }

    /**
     * Database delta reply message handler
     */
    public static class DatabaseDeltaReplyHandler extends MessageHandler {
        DatabaseDeltaReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            try {
                int delta = msg.getInt("command2");
                // update link db delta
                getInsteonDevice().getLinkDB().updateDatabaseDelta(delta);
            } catch (FieldException e) {
                logger.warn("{}: error parsing msg: {}", nm(), msg, e);
            }
        }
    }

    /**
     * Insteon engine reply message handler
     */
    public static class InsteonEngineReplyHandler extends MessageHandler {
        InsteonEngineReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            try {
                int version = msg.getInt("command2");
                InsteonEngine engine = InsteonEngine.valueOf(version);
                // set device insteon engine
                getInsteonDevice().setInsteonEngine(engine);
                // continue device polling
                getInsteonDevice().poll(500L);
            } catch (FieldException e) {
                logger.warn("{}: error parsing msg: {}", nm(), msg, e);
            }
        }
    }

    /**
     * Ping reply message handler
     */
    public static class PingReplyHandler extends MessageHandler {
        PingReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            logger.debug("{}: successfully pinged device {}", nm(), getInsteonDevice().getAddress());
        }
    }

    /**
     * Heartbeat monitor message handler
     */
    public static class HeartbeatMonitorMsgHandler extends MessageHandler {
        HeartbeatMonitorMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            // reset device heartbeat timeout on all link broadcast or cleanup message not replayed
            if (msg.isAllLinkBroadcastOrCleanup() && !msg.isReplayed()) {
                getInsteonDevice().resetHeartbeatTimeout();
            }
        }
    }

    /**
     * Last time message handler
     */
    public static class LastTimeMsgHandler extends MessageHandler {
        LastTimeMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            Instant timestamp = Instant.ofEpochMilli(msg.getTimestamp());
            Instant lastTimestamp = feature.getState() instanceof DateTimeType datetime ? datetime.getInstant() : null;
            // update state if not defined or is older than message timestamp
            if (lastTimestamp == null || lastTimestamp.isBefore(timestamp)) {
                feature.updateState(new DateTimeType(timestamp));
            }
        }
    }

    /**
     * Button event message handler
     */
    public static class ButtonEventMsgHandler extends MessageHandler {
        ButtonEventMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected boolean isDuplicate(Msg msg) {
            // Disable duplicate elimination based on parameter because
            // some button events such as hold or release have no cleanup or success messages.
            return getParameterAsBoolean("duplicate", super.isDuplicate(msg));
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            try {
                byte cmd2 = msg.getByte("command2");
                ButtonEvent event = ButtonEvent.valueOf(cmd1, cmd2);
                logger.debug("{}: device {} {} received event {}", nm(), getInsteonDevice().getAddress(),
                        feature.getName(), event);
                feature.triggerEvent(event.toString());
                feature.pollRelatedDevices(0L);
            } catch (FieldException e) {
                logger.warn("{}: error parsing msg: {}", nm(), msg, e);
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected button event: {}", nm(), HexUtils.getHexString(cmd1));
            }
        }
    }

    /**
     * Status request reply message handler
     */
    public static class StatusRequestReplyHandler extends CustomMsgHandler {
        StatusRequestReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            // update link db delta if is my request status reply message (0x19)
            if (feature.getQueryCommand() == 0x19) {
                getInsteonDevice().getLinkDB().updateDatabaseDelta(cmd1 & 0xFF);
            }
            super.handleMessage(cmd1, msg);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return null;
        }
    }

    /**
     * On/Off abstract message handler
     */
    public abstract static class OnOffMsgHandler extends MessageHandler {
        OnOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            String mode = getParameterAsString("mode", "REGULAR");
            State state = getState(mode);
            if (state != null) {
                logger.debug("{}: device {} is {} ({})", nm(), getInsteonDevice().getAddress(), state, mode);
                feature.updateState(state);
            }
        }

        protected abstract @Nullable State getState(String mode);
    }

    /**
     * Dimmer on message handler
     */
    public static class DimmerOnMsgHandler extends OnOffMsgHandler {
        DimmerOnMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(String mode) {
            switch (mode) {
                case "FAST":
                    // set to 100% for fast on change
                    return PercentType.HUNDRED;
                default:
                    // set to device on level if the current state not at that level already, defaulting to 100%
                    // this is due to subsequent dimmer on button press cycling between on level and 100%
                    State onLevel = getInsteonDevice().getFeatureState(FEATURE_ON_LEVEL);
                    State state = feature.getState();
                    return onLevel instanceof PercentType && !state.equals(onLevel) ? onLevel : PercentType.HUNDRED;
            }
        }
    }

    /**
     * Dimmer off message handler
     */
    public static class DimmerOffMsgHandler extends OnOffMsgHandler {
        DimmerOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(String mode) {
            return PercentType.ZERO;
        }
    }

    /**
     * Dimmer request reply message handler
     */
    public static class DimmerRequestReplyHandler extends StatusRequestReplyHandler {
        DimmerRequestReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            int queryCmd = feature.getQueryCommand();
            // 1) trigger poll if is my bright/dim or manual change stop command reply
            // 2) handle fast on/off message if is my fast on/off command reply
            // 3) handle ramp dimmer message if is my ramp rate on/off command reply
            // 4) handle my standard/instant on/off command reply ignoring manual change start messages
            if (queryCmd == 0x15 || queryCmd == 0x16 || queryCmd == 0x18) {
                feature.triggerPoll(0L);
            } else if (queryCmd == 0x12 || queryCmd == 0x14) {
                handleFastOnOffMessage(cmd1, msg);
            } else if (queryCmd == 0x2E || queryCmd == 0x2F || queryCmd == 0x34 || queryCmd == 0x35) {
                handleRampDimmerMessage(cmd1, msg);
            } else if (queryCmd != 0x17) {
                super.handleMessage(cmd1, msg);
            }
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            int level = (int) Math.round(value * 100 / 255.0);
            return new PercentType(level);
        }

        private void handleFastOnOffMessage(byte cmd1, Msg msg) {
            FastOnOffMsgHandler handler = new FastOnOffMsgHandler(feature);
            handler.setParameters(parameters);
            handler.handleMessage(cmd1, msg);
        }

        private void handleRampDimmerMessage(byte cmd1, Msg msg) {
            RampDimmerMsgHandler handler = new RampDimmerMsgHandler(feature);
            handler.setParameters(parameters);
            handler.handleMessage(cmd1, msg);
        }
    }

    /**
     * Fast on/off message handler
     */
    public static class FastOnOffMsgHandler extends CustomMsgHandler {
        FastOnOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            switch (cmd1) {
                case 0x14:
                    return PercentType.ZERO;
                case 0x12:
                    return PercentType.HUNDRED;
                default:
                    logger.warn("{}: got unexpected command value: {}", nm(), HexUtils.getHexString(cmd1));
                    return null;
            }
        }
    }

    /**
     * Ramp dimmer message handler
     */
    public static class RampDimmerMsgHandler extends CustomMsgHandler {
        RampDimmerMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            switch (cmd1) {
                case 0x2F:
                case 0x35:
                    return PercentType.ZERO;
                case 0x2E:
                case 0x34:
                    int highByte = ((int) value) >> 4;
                    int level = (int) Math.round((highByte * 16 + 0x0F) * 100 / 255.0);
                    return new PercentType(level);
                default:
                    logger.warn("{}: got unexpected command value: {}", nm(), HexUtils.getHexString(cmd1));
                    return null;
            }
        }
    }

    /**
     * Switch on message handler
     */
    public static class SwitchOnMsgHandler extends OnOffMsgHandler {
        SwitchOnMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(String mode) {
            return OnOffType.ON;
        }
    }

    /**
     * Switch off message handler
     */
    public static class SwitchOffMsgHandler extends OnOffMsgHandler {
        SwitchOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(String mode) {
            return OnOffType.OFF;
        }
    }

    /**
     * Switch request reply message handler
     */
    public static class SwitchRequestReplyHandler extends StatusRequestReplyHandler {
        SwitchRequestReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            int level = (int) value;
            State state = null;
            if (level == 0x00 || level == 0xFF) {
                state = OnOffType.from(level == 0xFF);
            } else {
                logger.warn("{}: ignoring unexpected level received {}", nm(), HexUtils.getHexString(level));
            }
            return state;
        }
    }

    /**
     * Keypad button on message handler
     */
    public static class KeypadButtonOnMsgHandler extends SwitchOnMsgHandler {
        KeypadButtonOnMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            super.handleMessage(cmd1, msg);
            // trigger poll to account for button group changes
            feature.triggerPoll(0L);
        }
    }

    /**
     * Keypad button off message handler
     */
    public static class KeypadButtonOffMsgHandler extends SwitchOffMsgHandler {
        KeypadButtonOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            super.handleMessage(cmd1, msg);
            // trigger poll to account for button group changes
            feature.triggerPoll(0L);
        }
    }

    /**
     * Keypad button reply message handler
     */
    public static class KeypadButtonReplyHandler extends CustomBitmaskMsgHandler {
        KeypadButtonReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            // trigger poll if is my command reply message (0x2E)
            if (feature.getQueryCommand() == 0x2E) {
                feature.triggerPoll(0L);
            } else {
                super.handleMessage(cmd1, msg);
            }
        }

        @Override
        protected int getBitNumber() {
            int bit = feature.getGroup() - 1;
            // return bit if representing keypad button 2-8, otherwise -1
            return bit >= 1 && bit <= 7 ? bit : -1;
        }
    }

    /**
     * Keypad button toggle mode message handler
     */
    public static class KeypadButtonToggleModeMsgHandler extends MessageHandler {
        KeypadButtonToggleModeMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            try {
                int bit = feature.getGroup() - 1;
                if (bit < 0 || bit > 7) {
                    logger.debug("{}: invalid bit number defined for {}", nm(), feature.getName());
                } else {
                    int value = msg.getByte("userData10") << 8 | msg.getByte("userData13");
                    KeypadButtonToggleMode mode = KeypadButtonToggleMode.valueOf(value, bit);
                    logger.debug("{}: device {} {} is {}", nm(), getInsteonDevice().getAddress(), feature.getName(),
                            mode);
                    feature.setLastMsgValue(value);
                    feature.updateState(new StringType(mode.toString()));
                }
            } catch (FieldException e) {
                logger.warn("{}: error parsing msg: {}", nm(), msg, e);
            }
        }
    }

    /**
     * Operating flags reply message handler
     */
    public static class OpFlagsReplyHandler extends CustomBitmaskMsgHandler {
        OpFlagsReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            // trigger poll if is my command reply message (0x20)
            if (feature.getQueryCommand() == 0x20) {
                long delay = getPollDelay();
                feature.triggerPoll(delay);
            } else {
                super.handleMessage(cmd1, msg);
            }
        }

        protected long getPollDelay() {
            return 0L;
        }
    }

    /**
     * Link operating flags reply message handler
     */
    public static class LinkOpFlagsReplyHandler extends OpFlagsReplyHandler {
        LinkOpFlagsReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            super.handleMessage(cmd1, msg);
            // update default links
            getInsteonDevice().updateDefaultLinks();
        }
    }

    /**
     * Heartbeat on/off operating flag reply message handler
     */
    public static class HeartbeatOnOffReplyHandler extends OpFlagsReplyHandler {
        HeartbeatOnOffReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            super.handleMessage(cmd1, msg);
            // reset device heartbeat timeout
            getInsteonDevice().resetHeartbeatTimeout();
        }
    }

    /**
     * Keypad button config operating flag reply message handler
     */
    public static class KeypadButtonConfigReplyHandler extends OpFlagsReplyHandler {
        KeypadButtonConfigReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected State getBitState(boolean is8Button) {
            KeypadButtonConfig config = KeypadButtonConfig.from(is8Button);
            // update device type based on button config
            getInsteonDevice().updateType(config);
            // return button config state
            return new StringType(config.toString());
        }
    }

    /**
     * LED brightness message handler
     */
    public static class LEDBrightnessMsgHandler extends CustomMsgHandler {
        LEDBrightnessMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            int level = (int) Math.round(value * 100 / 127.0);
            State state = getInsteonDevice().getFeatureState(FEATURE_LED_ON_OFF);
            return OnOffType.OFF.equals(state) ? PercentType.ZERO : new PercentType(level);
        }
    }

    /**
     * Ramp rate message handler
     */
    public static class RampRateMsgHandler extends CustomMsgHandler {
        RampRateMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            RampRate rampRate = RampRate.valueOf((int) value);
            return new QuantityType<Time>(rampRate.getTimeInSeconds(), Units.SECOND);
        }
    }

    /**
     * Sensor abstract message handler
     */
    public abstract static class SensorMsgHandler extends CustomMsgHandler {
        SensorMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            super.handleMessage(cmd1, msg);
            // poll related devices
            feature.pollRelatedDevices(0L);
        }
    }

    /**
     * Contact open message handler
     */
    public static class ContactOpenMsgHandler extends SensorMsgHandler {
        ContactOpenMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return OpenClosedType.OPEN;
        }
    }

    /**
     * Contact closed message handler
     */
    public static class ContactClosedMsgHandler extends SensorMsgHandler {
        ContactClosedMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return OpenClosedType.CLOSED;
        }
    }

    /**
     * Contact request reply message handler
     */
    public static class ContactRequestReplyHandler extends StatusRequestReplyHandler {
        ContactRequestReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return value == 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        }
    }

    /**
     * Wireless sensor open message handler
     */
    public static class WirelessSensorOpenMsgHandler extends SensorMsgHandler {
        WirelessSensorOpenMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return OpenClosedType.OPEN;
        }
    }

    /**
     * Wireless sensor closed message handler
     */
    public static class WirelessSensorClosedMsgHandler extends SensorMsgHandler {
        WirelessSensorClosedMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return OpenClosedType.CLOSED;
        }
    }

    /**
     * Wireless sensor on message handler
     */
    public static class WirelessSensorOnMsgHandler extends SensorMsgHandler {
        WirelessSensorOnMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return OnOffType.ON;
        }
    }

    /**
     * Wireless sensor off message handler
     */
    public static class WirelessSensorOffMsgHandler extends SensorMsgHandler {
        WirelessSensorOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return OnOffType.OFF;
        }
    }

    /**
     * Motion sensor 2 battery powered reply message handler
     */
    public static class MotionSensor2BatteryPoweredReplyHandler extends CustomMsgHandler {
        MotionSensor2BatteryPoweredReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            // stage flag bit 1 = USB Powered
            boolean isBatteryPowered = !BinaryUtils.isBitSet((int) value, 1);
            // update device based on battery powered flag
            updateDeviceFlag(isBatteryPowered);
            // return battery powered state
            return OnOffType.from(isBatteryPowered);
        }

        private void updateDeviceFlag(boolean isBatteryPowered) {
            // update device batteryPowered flag
            getInsteonDevice().setFlag("batteryPowered", isBatteryPowered);
            // stop device polling if battery powered, otherwise start it
            if (isBatteryPowered) {
                getInsteonDevice().stopPolling();
            } else {
                getInsteonDevice().startPolling();
            }
        }
    }

    /**
     * Motion sensor 2 temperature message handler
     */
    public static class MotionSensor2TemperatureMsgHandler extends CustomMsgHandler {
        MotionSensor2TemperatureMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            boolean isBatteryPowered = getInsteonDevice().isBatteryPowered();
            // temperature (°F) = 0.73 * value - 20.53 (battery powered); 0.72 * value - 24.61 (usb powered)
            double temperature = isBatteryPowered ? 0.73 * value - 20.53 : 0.72 * value - 24.61;
            return new QuantityType<Temperature>(temperature, ImperialUnits.FAHRENHEIT);
        }
    }

    /**
     * Heartbeat interval message handler
     */
    public static class HeartbeatIntervalMsgHandler extends CustomMsgHandler {
        HeartbeatIntervalMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            super.handleMessage(cmd1, msg);
            // reset device heartbeat timeout
            getInsteonDevice().resetHeartbeatTimeout();
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            int interval = getInterval((int) value);
            return interval > 0 ? new QuantityType<Time>(interval, Units.MINUTE) : null;
        }

        private int getInterval(int value) {
            int preset = getParameterAsInteger("preset", 0);
            int increment = getParameterAsInteger("increment", 0);
            return value == 0x00 ? preset : value * increment;
        }
    }

    /**
     * FanLinc fan mode reply message handler
     */
    public static class FanLincFanReplyHandler extends CustomMsgHandler {
        FanLincFanReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            try {
                FanLincFanSpeed speed = FanLincFanSpeed.valueOf((int) value);
                return new StringType(speed.toString());
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected fan speed reply value: {}", nm(), HexUtils.getHexString((int) value));
                return UnDefType.UNDEF;
            }
        }
    }

    /**
     * I/O linc relay switch on message handler
     */
    public static class IOLincRelaySwitchOnMsgHandler extends SwitchOnMsgHandler {
        IOLincRelaySwitchOnMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            State state = getInsteonDevice().getFeatureState(FEATURE_RELAY_SENSOR_FOLLOW);
            // handle message only if relay sensor follow is on
            if (OnOffType.ON.equals(state)) {
                super.handleMessage(cmd1, msg);
            }
        }
    }

    /**
     * I/O linc relay switch off message handler
     */
    public static class IOLincRelaySwitchOffMsgHandler extends SwitchOffMsgHandler {
        IOLincRelaySwitchOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            State state = getInsteonDevice().getFeatureState(FEATURE_RELAY_SENSOR_FOLLOW);
            // handle message only if relay sensor follow is on
            if (OnOffType.ON.equals(state)) {
                super.handleMessage(cmd1, msg);
            }
        }
    }

    /**
     * I/O linc relay switch reply message handler
     */
    public static class IOLincRelaySwitchReplyHandler extends SwitchRequestReplyHandler {
        private static final int DEFAULT_DURATION = 2;

        IOLincRelaySwitchReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            super.handleMessage(cmd1, msg);
            // trigger poll with delay based on momentary duration if not status reply and relay mode not latching
            if (feature.getQueryCommand() != 0x19 && getRelayMode() != IOLincRelayMode.LATCHING) {
                long delay = getPollDelay();
                feature.triggerPoll(delay);
            }
        }

        private @Nullable IOLincRelayMode getRelayMode() {
            try {
                State state = getInsteonDevice().getFeatureState(FEATURE_RELAY_MODE);
                if (state instanceof StringType mode) {
                    return IOLincRelayMode.valueOf(mode.toString());
                }
            } catch (IllegalArgumentException ignored) {
            }
            return null;
        }

        private long getPollDelay() {
            double delay = DEFAULT_DURATION;
            State state = getInsteonDevice().getFeatureState(FEATURE_MOMENTARY_DURATION);
            if (state instanceof QuantityType<?> duration) {
                delay = Objects.requireNonNullElse(duration.toInvertibleUnit(Units.SECOND), duration).doubleValue();
            }
            return (long) (delay * 1000);
        }
    }

    /**
     * I/O linc momentary duration message handler
     */
    public static class IOLincMomentaryDurationMsgHandler extends CustomMsgHandler {
        IOLincMomentaryDurationMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            double duration = getDuration((int) value);
            return new QuantityType<Time>(duration, Units.SECOND);
        }

        private double getDuration(int value) {
            int multiplier = Math.max(value >> 8, 1); // high byte
            int delay = value & 0xFF; // low byte
            if (delay == 0) {
                delay = 255;
            }
            return delay * multiplier / 10.0;
        }
    }

    /**
     * I/O linc relay mode reply message handler
     */
    public static class IOLincRelayModeReplyHandler extends OpFlagsReplyHandler {
        IOLincRelayModeReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected long getPollDelay() {
            return 5000L; // delay to allow all op flag commands to be processed
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            IOLincRelayMode mode = IOLincRelayMode.valueOf((int) value);
            return new StringType(mode.toString());
        }
    }

    /**
     * Micro module operation mode reply message handler
     */
    public static class MicroModuleOpModeReplyHandler extends OpFlagsReplyHandler {
        MicroModuleOpModeReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected long getPollDelay() {
            return 2000L; // delay to allow all op flag commands to be processed
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            MicroModuleOpMode mode = MicroModuleOpMode.valueOf((int) value);
            return new StringType(mode.toString());
        }
    }

    /**
     * Outlet switch reply message handler
     *
     * 0x00 = Both Outlets Off
     * 0x01 = Only Top Outlet On
     * 0x02 = Only Bottom Outlet On
     * 0x03 = Both Outlets On
     */
    public static class OutletSwitchReplyHandler extends CustomMsgHandler {
        OutletSwitchReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return OnOffType.from(value == feature.getGroup() || value == 0x03);
        }
    }

    /**
     * Power meter energy message handler
     */
    public static class PowerMeterEnergyMsgHandler extends CustomMsgHandler {
        PowerMeterEnergyMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            BigDecimal energy = getEnergy((int) value);
            return new QuantityType<Energy>(energy, Units.KILOWATT_HOUR);
        }

        private BigDecimal getEnergy(int value) {
            return (value >> 24) < 254
                    ? new BigDecimal(value * 65535.0 / (1000 * 60 * 60 * 60)).setScale(4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
        }
    }

    /**
     * Power meter power message handler
     */
    public static class PowerMeterPowerMsgHandler extends CustomMsgHandler {
        PowerMeterPowerMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            int power = getPower((int) value);
            return new QuantityType<Power>(power, Units.WATT);
        }

        private int getPower(int power) {
            return power > 32767 ? power - 65535 : power;
        }
    }

    /**
     * Remote scene button config reply message handler
     */
    public static class RemoteSceneButtonConfigReplyHandler extends OpFlagsReplyHandler {
        RemoteSceneButtonConfigReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected long getPollDelay() {
            return 2000L; // delay to allow all op flag commands to be processed
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            RemoteSceneButtonConfig config = RemoteSceneButtonConfig.valueOf((int) value);
            // update device type based on button config
            getInsteonDevice().updateType(config);
            // return button config state
            return new StringType(config.toString());
        }
    }

    /**
     * Remote switch button config reply message handler
     */
    public static class RemoteSwitchButtonConfigReplyHandler extends OpFlagsReplyHandler {
        RemoteSwitchButtonConfigReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected long getPollDelay() {
            return 2000L; // delay to allow all op flag commands to be processed
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            RemoteSwitchButtonConfig config = RemoteSwitchButtonConfig.valueOf((int) value);
            // update device type based on button config
            getInsteonDevice().updateType(config);
            // return button config state
            return new StringType(config.toString());
        }
    }

    /**
     * Siren request reply message handler
     */
    public static class SirenRequesteplyHandler extends StatusRequestReplyHandler {
        SirenRequesteplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            int level = (int) value;
            return OnOffType.from(level != 0x00);
        }
    }

    /**
     * Siren armed reply message handler
     */
    public static class SirenArmedReplyHandler extends CustomMsgHandler {
        SirenArmedReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            boolean isArmed = BinaryUtils.isBitSet((int) value, 6) || BinaryUtils.isBitSet((int) value, 7);
            return OnOffType.from(isArmed);
        }
    }

    /**
     * Siren alert type message handler
     */
    public static class SirenAlertTypeMsgHandler extends CustomMsgHandler {
        SirenAlertTypeMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            try {
                SirenAlertType type = SirenAlertType.valueOf((int) value);
                return new StringType(type.toString());
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected alert type value: {}", nm(), (int) value);
                return UnDefType.UNDEF;
            }
        }
    }

    /**
     * Sprinkler valve message handler
     */
    public static class SprinklerValveMsgHandler extends CustomMsgHandler {
        SprinklerValveMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            int valve = getParameterAsInteger("valve", -1);
            if (valve < 0 || valve > 8) {
                logger.debug("{}: invalid valve number defined for {}", nm(), feature.getName());
                return UnDefType.UNDEF;
            }
            boolean isValveOn = BinaryUtils.isBitSet((int) value, 7) && (((int) value) & 0x07) == valve
                    || BinaryUtils.isBitSet((int) value, 6) && valve == 7;
            return OnOffType.from(isValveOn);
        }
    }

    /**
     * Sprinkler program message handler
     */
    public static class SprinklerProgramMsgHandler extends CustomMsgHandler {
        SprinklerProgramMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            int program = getParameterAsInteger("program", -1);
            if (program < 0 || program > 4) {
                logger.debug("{}: invalid program number defined for {}", nm(), feature.getName());
                return UnDefType.UNDEF;
            }
            boolean isProgramOn = BinaryUtils.isBitSet((int) value, 5) && (((int) value) & 0x18) >> 3 == program;
            return OnOffType.from(isProgramOn);
        }
    }

    /**
     * Thermostat fan mode message handler
     */
    public static class ThermostatFanModeMsgHandler extends CustomMsgHandler {
        ThermostatFanModeMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            try {
                ThermostatFanMode mode = ThermostatFanMode.fromStatus((int) value);
                return new StringType(mode.toString());
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected fan mode status: {}", nm(), HexUtils.getHexString((int) value));
                return UnDefType.UNDEF;
            }
        }
    }

    /**
     * Thermostat fan mode reply message handler
     */
    public static class ThermostatFanModeReplyHandler extends CustomMsgHandler {
        ThermostatFanModeReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            try {
                ThermostatFanMode mode = ThermostatFanMode.valueOf((int) value);
                return new StringType(mode.toString());
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected fan mode reply: {}", nm(), HexUtils.getHexString((int) value));
                return UnDefType.UNDEF;
            }
        }
    }

    /**
     * Thermostat humidifier dehumidifying message handler
     */
    public static class ThermostatHumidifierDehumidifyingMsgHandler extends CustomMsgHandler {
        ThermostatHumidifierDehumidifyingMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return new StringType(ThermostatSystemState.DEHUMIDIFYING.toString());
        }
    }

    /**
     * Thermostat humidifier humidifying message handler
     */
    public static class ThermostatHumidifierHumidifyingMsgHandler extends CustomMsgHandler {
        ThermostatHumidifierHumidifyingMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return new StringType(ThermostatSystemState.HUMIDIFYING.toString());
        }
    }

    /**
     * Thermostat humidifier off message handler
     */
    public static class ThermostatHumidifierOffMsgHandler extends CustomMsgHandler {
        ThermostatHumidifierOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return new StringType(ThermostatSystemState.OFF.toString());
        }
    }

    /**
     * Termostat system mode message handler
     */
    public static class ThermostatSystemModeMsgHandler extends CustomMsgHandler {
        ThermostatSystemModeMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            try {
                ThermostatSystemMode mode = ThermostatSystemMode.fromStatus((int) value);
                return new StringType(mode.toString());
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected system mode status: {}", nm(), HexUtils.getHexString((int) value));
                return UnDefType.UNDEF;
            }
        }
    }

    /**
     * Thermostat system mode reply message handler
     */
    public static class ThermostatSystemModeReplyHandler extends CustomMsgHandler {
        ThermostatSystemModeReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            try {
                ThermostatSystemMode mode = ThermostatSystemMode.valueOf((int) value);
                return new StringType(mode.toString());
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected system mode reply: {}", nm(), HexUtils.getHexString((int) value));
                return UnDefType.UNDEF;
            }
        }
    }

    /**
     * Thermostat system cooling message handler
     */
    public static class ThermostatSystemCoolingMsgHandler extends CustomMsgHandler {
        ThermostatSystemCoolingMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return new StringType(ThermostatSystemState.COOLING.toString());
        }
    }

    /**
     * Thermostat system heating message handler
     */
    public static class ThermostatSystemHeatingMsgHandler extends CustomMsgHandler {
        ThermostatSystemHeatingMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return new StringType(ThermostatSystemState.HEATING.toString());
        }
    }

    /**
     * Thermostat system off message handler
     */
    public static class ThermostatSystemOffMsgHandler extends CustomMsgHandler {
        ThermostatSystemOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            return new StringType(ThermostatSystemState.OFF.toString());
        }
    }

    /**
     * Thermostat temperature scale message handler
     */
    public static class ThermostatTemperatureScaleMsgHandler extends CustomBitmaskMsgHandler {
        ThermostatTemperatureScaleMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected State getBitState(boolean isCelsius) {
            ThermostatTemperatureScale format = ThermostatTemperatureScale.from(isCelsius);
            return new StringType(format.toString());
        }
    }

    /**
     * Thermostat time format message handler
     */
    public static class ThermostatTimeFormatMsgHandler extends CustomBitmaskMsgHandler {
        ThermostatTimeFormatMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected State getBitState(boolean is24Hr) {
            ThermostatTimeFormat format = ThermostatTimeFormat.from(is24Hr);
            return new StringType(format.toString());
        }
    }

    /**
     * Thermostat status reporting reply message handler
     */
    public static class ThermostatStatusReportingReplyHandler extends MessageHandler {
        ThermostatStatusReportingReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            logger.debug("{}: thermostat status reporting enabled on {}", nm(), getInsteonDevice().getAddress());
        }
    }

    /**
     * Venstar thermostat system mode message handler
     */
    public static class VenstarSystemModeMsgHandler extends CustomMsgHandler {
        VenstarSystemModeMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            try {
                VenstarSystemMode mode = VenstarSystemMode.fromStatus((int) value);
                return new StringType(mode.toString());
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected system mode status: {}", nm(), HexUtils.getHexString((int) value));
                return UnDefType.UNDEF;
            }
        }
    }

    /**
     * Venstar thermostat system mode message handler
     */
    public static class VenstarSystemModeReplyHandler extends CustomMsgHandler {
        VenstarSystemModeReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(byte cmd1, double value) {
            try {
                VenstarSystemMode mode = VenstarSystemMode.valueOf((int) value);
                return new StringType(mode.toString());
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected system mode reply: {}", nm(), HexUtils.getHexString((int) value));
                return UnDefType.UNDEF;
            }
        }
    }

    /**
     * Venstar thermostat temperature message handler
     */
    public static class VenstarTemperatureMsgHandler extends CustomTemperatureMsgHandler {
        VenstarTemperatureMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected Unit<Temperature> getTemperatureUnit() {
            try {
                // use temperature scale current state to determine temperature unit, defaulting to fahrenheit
                State state = getInsteonDevice().getFeatureState(FEATURE_TEMPERATURE_SCALE);
                if (state != null
                        && ThermostatTemperatureScale.valueOf(state.toString()) == ThermostatTemperatureScale.CELSIUS) {
                    return SIUnits.CELSIUS;
                }
            } catch (IllegalArgumentException e) {
                logger.debug("{}: unable to determine temperature unit, defaulting to: FAHRENHEIT", nm());
            }
            return ImperialUnits.FAHRENHEIT;
        }
    }

    /**
     * IM button event message handler
     */
    public static class IMButtonEventMsgHandler extends MessageHandler {
        IMButtonEventMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            try {
                int cmd = msg.getInt("buttonEvent");
                int button = getParameterAsInteger("button", 1);
                int mask = (button - 1) << 4;
                IMButtonEvent event = IMButtonEvent.valueOf(cmd ^ mask);
                logger.debug("{}: IM {} received event {}", nm(), feature.getName(), event);
                feature.triggerEvent(event.toString());
            } catch (FieldException e) {
                logger.warn("{}: error parsing msg {}", nm(), msg, e);
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected button event", nm(), e);
            }
        }
    }

    /**
     * IM config message handler
     */
    public static class IMConfigMsgHandler extends MessageHandler {
        IMConfigMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            try {
                int flags = msg.getInt("IMConfigurationFlags");
                int bit = getParameterAsInteger("bit", -1);
                if (bit < 3 || bit > 7) {
                    logger.debug("{}: invalid bit number defined for {}", nm(), feature.getName());
                    return;
                }
                boolean isSet = BinaryUtils.isBitSet(flags, bit);
                State state = OnOffType.from(isSet ^ getParameterAsBoolean("inverted", false));
                logger.debug("{}: IM {} is {}", nm(), feature.getName(), state);
                feature.setLastMsgValue(flags);
                feature.updateState(state);
            } catch (FieldException e) {
                logger.warn("{}: error parsing msg {}", nm(), msg, e);
            }
        }
    }

    /**
     * X10 on message handler
     */
    public static class X10OnMsgHandler extends MessageHandler {
        X10OnMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            logger.debug("{}: device {} is ON", nm(), getX10Device().getAddress());
            feature.updateState(OnOffType.ON);
        }
    }

    /**
     * X10 off message handler
     */
    public static class X10OffMsgHandler extends MessageHandler {
        X10OffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            logger.debug("{}: device {} is OFF", nm(), getX10Device().getAddress());
            feature.updateState(OnOffType.OFF);
        }
    }

    /**
     * X10 brighten message handler
     */
    public static class X10BrightMsgHandler extends MessageHandler {
        X10BrightMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            logger.debug("{}: ignoring brighten message for device {}", nm(), getX10Device().getAddress());
        }
    }

    /**
     * X10 dim message handler
     */
    public static class X10DimMsgHandler extends MessageHandler {
        X10DimMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            logger.debug("{}: ignoring dim message for device {}", nm(), getX10Device().getAddress());
        }
    }

    /**
     * X10 open message handler
     */
    public static class X10OpenMsgHandler extends MessageHandler {
        X10OpenMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            logger.debug("{}: device {} is OPEN", nm(), getX10Device().getAddress());
            feature.updateState(OpenClosedType.OPEN);
        }
    }

    /**
     * X10 closed message handler
     */
    public static class X10ClosedMsgHandler extends MessageHandler {
        X10ClosedMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            logger.debug("{}: device {} is CLOSED", nm(), getX10Device().getAddress());
            feature.updateState(OpenClosedType.CLOSED);
        }
    }

    /**
     * X10 event message handler
     */
    public static class X10EventMsgHandler extends MessageHandler {
        X10EventMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(byte cmd1, Msg msg) {
            try {
                X10Event event = X10Event.valueOf(cmd1);
                logger.debug("{}: device {} {} received event {}", nm(), getDevice().getAddress(), feature.getName(),
                        event);
                feature.triggerEvent(event.toString());
                feature.pollRelatedDevices(0L);
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected x10 event: {}", nm(), HexUtils.getHexString(cmd1));
            }
        }
    }

    /**
     * Factory method for dermining if a message handler command supports group
     *
     * @param command the handler command
     * @return true if handler supports group, otherwise false
     */
    public static boolean supportsGroup(int command) {
        return SUPPORTED_GROUP_COMMANDS.contains(command);
    }

    /**
     * Factory method for generating a message handler id
     *
     * @param command the handler command
     * @param group the handler group
     * @return the generated handler id
     */
    public static String generateId(int command, int group) {
        if (command == -1) {
            return "default";
        }
        String id = HexUtils.getHexString(command);
        if (group != -1) {
            id += ":" + group;
        }
        return id;
    }

    /**
     * Factory method for creating a default message handler
     *
     * @param feature the feature for which to create the handler
     * @return the default message handler which was created
     */
    public static DefaultMsgHandler makeDefaultHandler(DeviceFeature feature) {
        return new DefaultMsgHandler(feature);
    }

    /**
     * Factory method for creating a message handler for a given name using java reflection
     *
     * @param name the name of the handler to create
     * @param parameters the parameters of the handler to create
     * @param feature the feature for which to create the handler
     * @return the handler which was created
     */
    public static @Nullable <T extends MessageHandler> T makeHandler(String name, Map<String, String> parameters,
            DeviceFeature feature) {
        try {
            String className = MessageHandler.class.getName() + "$" + name;
            @SuppressWarnings("unchecked")
            Class<? extends T> classRef = (Class<? extends T>) Class.forName(className);
            @Nullable
            T handler = classRef.getDeclaredConstructor(DeviceFeature.class).newInstance(feature);
            handler.setParameters(parameters);
            return handler;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
    }
}
