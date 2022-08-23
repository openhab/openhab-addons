/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.handler.feature;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.DeviceFeature.StateChangeType;
import org.openhab.binding.insteon.internal.device.DeviceType;
import org.openhab.binding.insteon.internal.device.DeviceTypeLoader;
import org.openhab.binding.insteon.internal.device.InsteonEngine;
import org.openhab.binding.insteon.internal.device.RampRate;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.GroupMessageStateMachine.GroupMessageType;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.utils.BitwiseUtils;
import org.openhab.binding.insteon.internal.utils.ByteUtils;
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
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public abstract class MessageHandler extends FeatureBaseHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    public MessageHandler(DeviceFeature feature) {
        super(feature);
    }

    /**
     * Handles incoming message. The cmd1 parameter
     * has been extracted earlier already (to make a decision which message handler to call),
     * and is passed in as an argument so cmd1 does not have to be extracted from the message again.
     *
     * @param group all-link group or -1 if not specified
     * @param cmd1 the insteon cmd1 field
     * @param msg the received insteon message
     */
    public abstract void handleMessage(int group, byte cmd1, Msg msg);

    /**
     * Returns if can handle a given message
     *
     * @param msg the message to be handled
     * @return true if handler not duplicate, valid and matches group/feature parameters
     */
    public boolean canHandle(Msg msg) {
        if (isDuplicate(msg)) {
            if (logger.isTraceEnabled()) {
                logger.trace("{}:{} ignoring msg as duplicate", getDevice().getAddress(), feature.getName());
            }
            return false;
        } else if (!isValid(msg)) {
            if (logger.isTraceEnabled()) {
                logger.trace("{}:{} ignoring msg as not valid", getDevice().getAddress(), feature.getName());
            }
            return false;
        } else if (!matchesGroup(msg) || !matches(msg)) {
            if (logger.isTraceEnabled()) {
                logger.trace("{}:{} ignoring msg as matches group:{} filter:{}", getDevice().getAddress(),
                        feature.getName(), matchesGroup(msg), matches(msg));
            }
            return false;
        }
        return true;
    }

    /**
     * Returns group parameter or -1 if no group is specified
     *
     * @return group parameter
     */
    protected int getGroup() {
        return getParameterAsInteger("group", -1);
    }

    /**
     * Returns if message group matches feature group parameter
     *
     * @param msg message to check
     * @return true if group matches or no group is specified/provided
     */
    protected boolean matchesGroup(Msg msg) {
        int msgGroup = msg.getGroup();
        int featureGroup = getGroup();
        return featureGroup == -1 || msgGroup == -1 || featureGroup == msgGroup;
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
    private boolean testMatch(Msg msg, String field, String param) throws FieldException {
        int mp = getParameterAsInteger(param, -1);
        // parameter not filtered for, declare this a match!
        if (mp == -1) {
            return true;
        }
        byte value = msg.getByte(field);
        return value == mp;
    }

    /**
     * Returns if message matches the filter parameters
     *
     * @param msg message to check
     * @return true if message matches
     */
    protected boolean matches(Msg msg) {
        try {
            int ext = getParameterAsInteger("ext", -1);
            if (ext != -1) {
                if ((!msg.isExtended() && ext != 0) || (msg.isExtended() && ext != 1 && ext != 2)) {
                    return false;
                }
                if (!testMatch(msg, "command1", "cmd1")) {
                    return false;
                }
            }
            if (!testMatch(msg, "command2", "cmd2")) {
                return false;
            }
            if (!testMatch(msg, "userData1", "d1")) {
                return false;
            }
            if (!testMatch(msg, "userData2", "d2")) {
                return false;
            }
            if (!testMatch(msg, "userData3", "d3")) {
                return false;
            }
        } catch (FieldException e) {
            logger.warn("error matching message: {}", msg, e);
            return false;
        }
        return true;
    }

    /**
     * Returns if an incoming ALL LINK message is a duplicate
     *
     * @param msg the received ALL LINK message
     * @return true if this message is a duplicate
     */
    protected boolean isDuplicate(Msg msg) {
        boolean isDuplicate = false;
        try {
            if (msg.isAllLinkBroadcast()) {
                int group = msg.getGroup();
                byte cmd1 = msg.getByte("command1");
                // if the command is 0x06, then it's success message
                // from the original broadcaster, with which the device
                // confirms that it got all cleanup replies successfully.
                GroupMessageType type = (cmd1 == 0x06) ? GroupMessageType.SUCCESS : GroupMessageType.BCAST;
                isDuplicate = !getDevice().getGroupState(group, type, cmd1);
            } else if (msg.isCleanup()) {
                int group = msg.getGroup();
                isDuplicate = !getDevice().getGroupState(group, GroupMessageType.CLEAN, (byte) 0x00);
            } else if (msg.isBroadcast()) {
                byte cmd1 = msg.getByte("command1");
                isDuplicate = !getDevice().getBroadcastState(cmd1);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("cannot parse msg: {}", msg, e);
        } catch (FieldException e) {
            logger.warn("cannot parse msg: {}", msg, e);
        }
        return isDuplicate;
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

    //
    //
    // ---------------- the various message handlers start here -------------------
    //
    //

    /**
     * Default message handler
     */
    public static class DefaultMsgHandler extends MessageHandler {
        DefaultMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: ignoring unimpl message with cmd1 {}", nm(), ByteUtils.getHexString(cmd1));
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            if (logger.isTraceEnabled()) {
                logger.trace("{}: ignoring message with cmd1 {}", nm(), ByteUtils.getHexString(cmd1));
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            // trigger poll with delay based on parameter, defaulting to 0 ms
            long delay = getParameterAsLong("delay", 0L);
            feature.triggerPoll(delay);
        }
    }

    /**
     * Custom event abstract message handler
     */
    public abstract static class CustomEventMsgHandler extends MessageHandler {
        CustomEventMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            try {
                byte cmd2 = msg.getByte("command2");
                String event = getEvent(group, cmd1, cmd2);
                if (event != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: device {} {} received event {}", nm(), getDevice().getAddress(),
                                feature.getName(), event);
                    }
                    feature.triggerEvent(event);
                    feature.pollRelatedDevices(0L);
                }
            } catch (FieldException e) {
                logger.warn("{}: error parsing msg: {}", nm(), msg, e);
            }
        }

        protected abstract @Nullable String getEvent(int group, byte cmd1, byte cmd2);
    }

    /**
     * Custom state abstract message handler based of parameters
     */
    public abstract static class CustomMsgHandler extends MessageHandler {
        private StateChangeType changeType = StateChangeType.CHANGED;

        CustomMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        public void setStateChangeType(StateChangeType changeType) {
            this.changeType = changeType;
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            try {
                // extract raw value from message
                int raw = getRawValue(group, msg);
                // apply mask and right shift bit manipulation
                int cooked = (raw & getParameterAsInteger("mask", 0xFF)) >> getParameterAsInteger("rshift", 0);
                // multiply with factor and add offset
                double value = cooked * getParameterAsDouble("factor", 1.0) + getParameterAsDouble("offset", 0.0);
                // get state to publish
                State state = getState(group, cmd1, value);
                // store extracted cooked message value
                feature.setLastMsgValue(value);
                // publish state if defined
                if (state != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: device {} {} changed to {}", nm(), getDevice().getAddress(),
                                feature.getName(), state);
                    }
                    feature.publishState(state, changeType);
                }
            } catch (FieldException e) {
                logger.warn("{}: error parsing msg {}", nm(), msg, e);
            }
        }

        private int getRawValue(int group, Msg msg) throws FieldException {
            // determine data field name based on parameter, default to cmd2 if is standard message
            String field = getParameterAsString("field", !msg.isExtended() ? "command2" : "");
            if (field.isEmpty()) {
                throw new FieldException("handler misconfigured, no field parameter specified!");
            }
            if (field.startsWith("address") && !msg.isBroadcast()) {
                throw new FieldException("not broadcast msg, cannot use address bytes!");
            }
            // return raw value based on field name
            switch (field) {
                case "group":
                    return group;
                case "addressHighByte":
                    // return broadcast address high byte value
                    return msg.getAddress("toAddress").getHighByte() & 0xFF;
                case "addressMiddleByte":
                    // return broadcast address middle byte value
                    return msg.getAddress("toAddress").getMiddleByte() & 0xFF;
                case "addressLowByte":
                    // return broadcast address low byte value
                    return msg.getAddress("toAddress").getLowByte() & 0xFF;
                default:
                    // return integer value starting from field name up to 4-bytes in size based on parameter
                    return msg.getBytesAsInt(field, getParameterAsInteger("num_bytes", 1));
            }
        }

        protected abstract @Nullable State getState(int group, byte cmd1, double value);
    }

    /**
     * Custom bitmask mmessage handler based of parameters
     */
    public static class CustomBitmaskMsgHandler extends CustomMsgHandler {
        CustomBitmaskMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            State state = null;
            // get bit number based on parameter
            int bit = getBitNumber();
            // get bit flag state from bitmask, if bit defined
            if (bit != -1) {
                boolean isSet = BitwiseUtils.isBitFlagSet((int) value, bit);
                state = getBitFlagState(isSet);
            } else {
                logger.debug("{}: no valid bit number defined for {}", nm(), feature.getName());
            }
            return state;
        }

        protected int getBitNumber() {
            int bit = getParameterAsInteger("bit", -1);
            // return bit if valid (0-7), otherwise -1
            return bit >= 0 && bit <= 7 ? bit : -1;
        }

        protected State getBitFlagState(boolean isSet) {
            return isSet ^ getParameterAsBoolean("inverted", false) ? OnOffType.ON : OnOffType.OFF;
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            try {
                int delta = msg.getInt("command2");
                // update link db delta
                getDevice().getLinkDB().updateDatabaseDelta(delta);
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            try {
                int version = msg.getInt("command2");
                InsteonEngine engine = InsteonEngine.valueOf(version);
                // set device insteon engine
                getDevice().setInsteonEngine(engine);
                // update device properties
                getDevice().updateProperties();
                // continue device polling
                getDevice().doPoll(0L);
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: successfully pinged device {}", nm(), getDevice().getAddress());
            }
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            // reset heartbeat monitor if message is broadcast but not replayed
            if (msg.isBroadcast() && !msg.isReplayed()) {
                getDevice().resetHeartbeatMonitor();
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            Instant instant = Instant.ofEpochMilli(msg.getTimestamp());
            ZonedDateTime timestamp = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
            ZonedDateTime lastTimestamp = getLastTimestamp();
            // set last time if not defined yet or message timestamp is greater than last value
            if (lastTimestamp == null || timestamp.compareTo(lastTimestamp) > 0) {
                feature.publishState(new DateTimeType(timestamp), StateChangeType.ALWAYS);
            }
        }

        private @Nullable ZonedDateTime getLastTimestamp() {
            State state = feature.getState();
            return state instanceof DateTimeType ? ((DateTimeType) state).getZonedDateTime() : null;
        }
    }

    /**
     * Button press event message handler
     */
    public static class ButtonPressEventHandler extends CustomEventMsgHandler {
        ButtonPressEventHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable String getEvent(int group, byte cmd1, byte cmd2) {
            switch (cmd1) {
                case 0x11:
                    return "PRESSED_ON";
                case 0x12:
                    return "DOUBLE_PRESSED_ON";
                case 0x13:
                    return "PRESSED_OFF";
                case 0x14:
                    return "DOUBLE_PRESSED_OFF";
                default:
                    logger.warn("{}: got unexpected command value: {}", nm(), ByteUtils.getHexString(cmd1));
                    return null;
            }
        }
    }

    /**
     * Button hold event message handler
     */
    public static class ButtonHoldEventHandler extends CustomEventMsgHandler {
        ButtonHoldEventHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean isDuplicate(Msg msg) {
            // Disable duplicate elimination because
            // there are no cleanup or success messages for button hold event.
            return false;
        }

        @Override
        protected @Nullable String getEvent(int group, byte cmd1, byte cmd2) {
            return cmd2 == 0x01 ? "HELD_UP" : "HELD_DOWN";
        }
    }

    /**
     * Button release event message handler
     */
    public static class ButtonReleaseEventHandler extends CustomEventMsgHandler {
        ButtonReleaseEventHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean isDuplicate(Msg msg) {
            // Disable duplicate elimination because
            // there are no cleanup or success messages for button release event.
            return false;
        }

        @Override
        protected @Nullable String getEvent(int group, byte cmd1, byte cmd2) {
            return "RELEASED";
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            String mode = getParameterAsString("mode", "REGULAR");
            State state = getState(mode);
            if (state != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: device {} changed to {} {}", nm(), getDevice().getAddress(), state, mode);
                }
                feature.publishState(state, StateChangeType.ALWAYS);
            }
        }

        protected abstract @Nullable State getState(String mode);
    }

    /**
     * Status request reply message handler
     */
    public static class StatusRequestReplyHandler extends CustomMsgHandler {
        StatusRequestReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            // update link db delta if is my request status reply message (0x19)
            if (feature.getLastQueryCommand() == 0x19) {
                getDevice().getLinkDB().updateDatabaseDelta(cmd1 & 0xFF);
            }
            super.handleMessage(group, cmd1, msg);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            return null;
        }
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
                    State onLevel = getDevice().getState(FEATURE_ON_LEVEL);
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            int queryCmd = feature.getLastQueryCommand();
            // trigger poll if is my bright/dim or manual change stop command reply,
            // handle fast on/off message if is my fast on/off command reply,
            // handle ramp dimmer message if is my ramp rate on/off command reply,
            // or handle my standard/instant on/off command reply ignoring manual change start messages
            if (queryCmd == 0x15 || queryCmd == 0x16 || queryCmd == 0x18) {
                feature.triggerPoll(0L);
            } else if (queryCmd == 0x12 || queryCmd == 0x14) {
                handleFastOnOffMessage(group, cmd1, msg);
            } else if (queryCmd == 0x2E || queryCmd == 0x2F || queryCmd == 0x34 || queryCmd == 0x35) {
                handleRampDimmerMessage(group, cmd1, msg);
            } else if (queryCmd != 0x17) {
                super.handleMessage(group, cmd1, msg);
            }
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            int level = (int) Math.round(value * 100 / 255.0);
            return new PercentType(level);
        }

        private void handleFastOnOffMessage(int group, byte cmd1, Msg msg) {
            FastOnOffMsgHandler handler = new FastOnOffMsgHandler(feature);
            handler.setParameters(parameters);
            handler.handleMessage(group, cmd1, msg);
        }

        private void handleRampDimmerMessage(int group, byte cmd1, Msg msg) {
            RampDimmerMsgHandler handler = new RampDimmerMsgHandler(feature);
            handler.setParameters(parameters);
            handler.handleMessage(group, cmd1, msg);
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            switch (cmd1) {
                case 0x14:
                    return PercentType.ZERO;
                case 0x12:
                    return PercentType.HUNDRED;
                default:
                    logger.warn("{}: got unexpected command value: {}", nm(), ByteUtils.getHexString(cmd1));
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
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
                    logger.warn("{}: got unexpected command value: {}", nm(), ByteUtils.getHexString(cmd1));
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            int level = (int) value;
            State state = null;
            if (level == 0x00 || level == 0xFF) {
                state = level == 0xFF ? OnOffType.ON : OnOffType.OFF;
            } else {
                logger.warn("{}: ignoring unexpected level received {}", nm(), ByteUtils.getHexString(level));
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            super.handleMessage(group, cmd1, msg);
            // trigger poll to account for radio button group changes
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            super.handleMessage(group, cmd1, msg);
            // trigger poll to account for radio button group changes
            feature.triggerPoll(0L);
        }
    }

    /**
     * Keypad bitmask message handler
     */
    public static class KeypadBitmaskMsgHandler extends CustomBitmaskMsgHandler {
        KeypadBitmaskMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected int getBitNumber() {
            int bit = getGroup() - 1;
            // return bit if representing keypad button 2-8, otherwise -1
            return bit >= 1 && bit <= 7 ? bit : -1;
        }
    }

    /**
     * Keypad button reply message handler
     */
    public static class KeypadButtonReplyHandler extends KeypadBitmaskMsgHandler {
        KeypadButtonReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            // trigger poll if is my command reply message (0x2E)
            if (feature.getLastQueryCommand() == 0x2E) {
                feature.triggerPoll(0L);
            } else {
                super.handleMessage(group, cmd1, msg);
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            // trigger poll if is my command reply message (0x20)
            if (feature.getLastQueryCommand() == 0x20) {
                feature.triggerPoll(0L);
            } else {
                super.handleMessage(group, cmd1, msg);
            }
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
        protected State getBitFlagState(boolean is8Button) {
            // update device type based on button config
            updateDeviceType(is8Button ? "8" : "6");
            // return button config state
            return new StringType(is8Button ? "8-BUTTON" : "6-BUTTON");
        }

        private void updateDeviceType(String buttonConfig) {
            DeviceType deviceType = getDevice().getType();
            if (deviceType == null) {
                logger.warn("{}: unknown device type for {}", nm(), getDevice().getAddress());
            } else {
                String name = deviceType.getName().replaceAll(".$", buttonConfig);
                DeviceType newType = DeviceTypeLoader.instance().getDeviceType(name);
                if (newType == null) {
                    logger.warn("{}: unknown device type {}", nm(), name);
                } else {
                    getDevice().updateType(newType);
                }
            }
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            int level = (int) Math.round(value * 100 / 127.0);
            State state = getDevice().getState(FEATURE_LED_ON_OFF);
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
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
            setStateChangeType(StateChangeType.ALWAYS);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            super.handleMessage(group, cmd1, msg);
            // poll battery powered sensor device while awake
            if (getDevice().isBatteryPowered()) {
                // set delay to 1500ms to allow all-link cleanup msg to be processed beforehand
                // only on non-replayed message, otherwise no delay
                long delay = msg.isReplayed() ? 0L : 1500L;
                getDevice().doPoll(delay);
            }
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            return value == 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        }
    }

    /**
     * Wireless sensor contact open message handler
     */
    public static class WirelessSensorContactOpenMsgHandler extends SensorMsgHandler {
        WirelessSensorContactOpenMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            // return open state if group parameter configured
            if (getGroup() != -1) {
                return OpenClosedType.OPEN;
            }
            switch (group) {
                case 1: // open event
                case 4: // heartbeat
                    return OpenClosedType.OPEN;
                default: // ignore
                    return null;
            }
        }
    }

    /**
     * Wireless sensor contact closed message handler
     */
    public static class WirelessSensorContactClosedMsgHandler extends SensorMsgHandler {
        WirelessSensorContactClosedMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            // return closed state if group parameter configured
            if (getGroup() != -1) {
                return OpenClosedType.CLOSED;
            }
            switch (group) {
                case 1: // closed event
                case 4: // heartbeat
                    return OpenClosedType.CLOSED;
                default: // ignore
                    return null;
            }
        }
    }

    /**
     * Wireless sensor state on message handler
     */
    public static class WirelessSensorStateOnMsgHandler extends SensorMsgHandler {
        WirelessSensorStateOnMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            // return on state if group parameter configured
            if (getGroup() != -1) {
                return OnOffType.ON;
            }
            switch (group) {
                case 1: // on event
                case 4: // heartbeat
                    return OnOffType.ON;
                default: // ignore
                    return null;
            }
        }
    }

    /**
     * Wireless sensor state off message handler
     */
    public static class WirelessSensorStateOffMsgHandler extends SensorMsgHandler {
        WirelessSensorStateOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            // return off state if group parameter configured
            if (getGroup() != -1) {
                return OnOffType.OFF;
            }
            switch (group) {
                case 1: // off event
                case 4: // heartbeat
                    return OnOffType.OFF;
                default: // ignore
                    return null;
            }
        }
    }

    /**
     * Leak sensor state on message handler
     */
    public static class LeakSensorStateOnMsgHandler extends SensorMsgHandler {
        LeakSensorStateOnMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            switch (group) {
                case 1: // dry event
                case 4: // heartbeat (dry)
                    return OnOffType.OFF;
                case 2: // wet event
                    return OnOffType.ON;
                default: // ignore
                    return null;
            }
        }
    }

    /**
     * Leak sensor state off message handler
     */
    public static class LeakSensorStateOffMsgHandler extends SensorMsgHandler {
        LeakSensorStateOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            switch (group) {
                case 4: // heartbeat (wet)
                    return OnOffType.ON;
                default: // ignore
                    return null;
            }
        }
    }

    /**
     * Smoke sensor state message handler
     */
    public static class SmokeSensorStateMsgHandler extends SensorMsgHandler {
        SmokeSensorStateMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            State state = null;
            if (group == getParameterAsInteger("event", -1)) {
                state = OnOffType.ON; // detected event
            } else if (group == 5) {
                state = OnOffType.OFF; // clear event
            }
            return state;
        }
    }

    /**
     * Motion sensor 2 battery level message handler
     */
    public static class MotionSensor2BatteryLevelMsgHandler extends CustomDimensionlessMsgHandler {
        MotionSensor2BatteryLevelMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            switch (group) {
                case 3: // low battery
                case 4: // heartbeat
                case 11: // alternate heartbeat
                    return super.getState(group, cmd1, value);
                default: // ignore
                    return null;
            }
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            // stage flag bit 1 = USB Powered
            boolean isBatteryPowered = !BitwiseUtils.isBitFlagSet((int) value, 1);
            // update device based on battery powered flag
            updateDeviceFlag(isBatteryPowered);
            // return battery powered state
            return isBatteryPowered ? OnOffType.ON : OnOffType.OFF;
        }

        private void updateDeviceFlag(boolean isBatteryPowered) {
            // update device batteryPowered flag
            getDevice().setFlag("batteryPowered", isBatteryPowered);
            // stop device polling if battery powered, otherwise start it
            if (isBatteryPowered) {
                getDevice().stopPolling();
            } else {
                getDevice().startPolling();
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            boolean isBatteryPowered = getDevice().isBatteryPowered();
            // temperature (°F) = 0.73 * value - 20.53 (battery powered); 0.72 * value - 24.61 (usb powered)
            double temperature = isBatteryPowered ? 0.73 * value - 20.53 : 0.72 * value - 24.61;
            return new QuantityType<Temperature>(temperature, ImperialUnits.FAHRENHEIT);
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String mode = getMode((int) value);
            return mode != null ? new StringType(mode) : UnDefType.UNDEF;
        }

        private @Nullable String getMode(int value) {
            if (value == 0x00) {
                return "OFF";
            } else if (value >= 0x01 && value <= 0x7F) {
                return "LOW";
            } else if (value >= 0x80 && value <= 0xFE) {
                return "MEDIUM";
            } else if (value == 0xFF) {
                return "HIGH";
            } else {
                logger.warn("{}: got unexpected fan mode reply value: {}", nm(), ByteUtils.getHexString(value));
                return null;
            }
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            int duration = getDuration((int) value);
            return new QuantityType<Time>(duration, Units.SECOND);
        }

        private int getDuration(int value) {
            int prescaler = value >> 8; // high byte
            int delay = value & 0xFF; // low byte
            if (delay == 0) {
                delay = 255;
            }
            return delay * prescaler / 10;
        }
    }

    /**
     * I/O linc relay mode reply message handler
     */
    public static class IOLincRelayModeReplyHandler extends CustomMsgHandler {
        IOLincRelayModeReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            // trigger poll if is my command reply message (0x20)
            if (feature.getLastQueryCommand() == 0x20) {
                feature.triggerPoll(5000L); // 5000ms delay to allow all op flag commands to be processed
            } else {
                super.handleMessage(group, cmd1, msg);
            }
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String mode;
            if (!BitwiseUtils.isBitFlagSet((int) value, 3)) {
                // set mode to latching, when momentary mode op flag (3) is off
                mode = "LATCHING";
            } else if (BitwiseUtils.isBitFlagSet((int) value, 7)) {
                // set mode to momentary c, when momentary sensor follow op flag (7) is on
                mode = "MOMENTARY_C";
            } else if (BitwiseUtils.isBitFlagSet((int) value, 4)) {
                // set mode to momentary b, when momentary trigger on/off op flag (4) is on
                mode = "MOMENTARY_B";
            } else {
                // set mode to momentary a, otherwise
                mode = "MOMENTARY_A";
            }
            return new StringType(mode);
        }
    }

    /**
     * Micro module operation mode reply message handler
     */
    public static class MicroModuleOpModeReplyHandler extends CustomMsgHandler {
        MicroModuleOpModeReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            // trigger poll if is my command reply message (0x20)
            if (feature.getLastQueryCommand() == 0x20) {
                feature.triggerPoll(2000L); // 2000ms delay to allow all op flag commands to be processed
            } else {
                super.handleMessage(group, cmd1, msg);
            }
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String mode;
            if (!BitwiseUtils.isBitFlagSet((int) value, 1)) {
                // set mode to latching, when momentary line op flag (1) is off
                mode = "LATCHING";
            } else if (!BitwiseUtils.isBitFlagSet((int) value, 0)) {
                // set mode to single momentary, when dual line op flag (0) is off
                mode = "SINGLE_MOMENTARY";
            } else {
                // set mode to dual momentary, otherwise
                mode = "DUAL_MOMENTARY";
            }
            return new StringType(mode);
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            return value == getGroup() || value == 0x03 ? OnOffType.ON : OnOffType.OFF;
        }
    }

    /**
     * Power meter kWh message handler
     */
    public static class PowerMeterKWhMsgHandler extends CustomMsgHandler {
        PowerMeterKWhMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            BigDecimal kWh = getKWh((int) value);
            return new QuantityType<Energy>(kWh, Units.KILOWATT_HOUR);
        }

        private BigDecimal getKWh(int energy) {
            BigDecimal kWh = BigDecimal.ZERO;
            int highByte = energy >> 24;
            if (highByte < 254) {
                kWh = new BigDecimal(energy * 65535.0 / (1000 * 60 * 60 * 60)).setScale(4, RoundingMode.HALF_UP);
            }
            return kWh;
        }
    }

    /**
     * Power meter watts message handler
     */
    public static class PowerMeterWattsMsgHandler extends CustomMsgHandler {
        PowerMeterWattsMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            int watts = getWatts((int) value);
            return new QuantityType<Power>(watts, Units.WATT);
        }

        private int getWatts(int watts) {
            if (watts > 32767) {
                watts -= 65535;
            }
            return watts;
        }
    }

    /**
     * Siren alarm reply message handler
     */
    public static class SirenAlarmReplyHandler extends StatusRequestReplyHandler {
        SirenAlarmReplyHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            // trigger poll if is my command reply message (0x11)
            if (feature.getLastQueryCommand() == 0x11) {
                feature.triggerPoll(127000L); // 127s delay to account for alarm max duration
            }
            super.handleMessage(group, cmd1, msg);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            int level = (int) value;
            return level == 0x00 ? OnOffType.OFF : OnOffType.ON;
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            boolean isArmed = BitwiseUtils.isBitFlagSet((int) value, 6) || BitwiseUtils.isBitFlagSet((int) value, 7);
            return isArmed ? OnOffType.ON : OnOffType.OFF;
        }
    }

    /**
     * Siren alarm type message handler
     */
    public static class SirenAlarmTypeMsgHandler extends CustomMsgHandler {
        SirenAlarmTypeMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String type = getType((int) value);
            return type != null ? new StringType(type) : UnDefType.UNDEF;
        }

        private @Nullable String getType(int value) {
            switch (value) {
                case 0:
                    return "CHIME";
                case 1:
                    return "LOUD_SIREN";
                default:
                    logger.warn("{}: got unexpected alert type value: {}", nm(), value);
                    return null;
            }
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String mode = getMode((int) value);
            return mode != null ? new StringType(mode) : UnDefType.UNDEF;
        }

        private @Nullable String getMode(int value) {
            switch (value) {
                case 0:
                    return "AUTO";
                case 1:
                    return "ON";
                default:
                    logger.warn("{}: got unexpected fan mode value: {}", nm(), value);
                    return null;
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String mode = getMode((int) value);
            return mode != null ? new StringType(mode) : UnDefType.UNDEF;
        }

        private @Nullable String getMode(int value) {
            switch (value) {
                case 0x08:
                    return "AUTO";
                case 0x07:
                    return "ON";
                default:
                    logger.warn("{}: got unexpected fan mode reply value: {}", nm(), ByteUtils.getHexString(value));
                    return null;
            }
        }
    }

    /**
     * Thermostat humidity control on message handler
     */
    public static class ThermostatHumidityControlOnMsgHandler extends CustomMsgHandler {
        ThermostatHumidityControlOnMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String state = group == 3 ? "DEHUMIDIFYING" : "HUMIDIFYING";
            return new StringType(state);
        }
    }

    /**
     * Thermostat humidity control off message handler
     */
    public static class ThermostatHumidityControlOffMsgHandler extends CustomMsgHandler {
        ThermostatHumidityControlOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            return new StringType("OFF");
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String mode = getMode((int) value);
            return mode != null ? new StringType(mode) : UnDefType.UNDEF;
        }

        private @Nullable String getMode(int value) {
            switch (value) {
                case 0:
                    return "OFF";
                case 1:
                    return "AUTO";
                case 2:
                    return "HEAT";
                case 3:
                    return "COOL";
                case 4:
                    return "PROGRAM";
                default:
                    logger.warn("{}: got unexpected system mode value: {}", nm(), value);
                    return null;
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String mode = getMode((int) value);
            return mode != null ? new StringType(mode) : UnDefType.UNDEF;
        }

        private @Nullable String getMode(int value) {
            switch (value) {
                case 0x09:
                    return "OFF";
                case 0x04:
                    return "HEAT";
                case 0x05:
                    return "COOL";
                case 0x06:
                    return "AUTO";
                case 0x0A:
                    return "PROGRAM";
                default:
                    logger.warn("{}: got unexpected system mode reply value: {}", nm(), ByteUtils.getHexString(value));
                    return null;
            }
        }
    }

    /**
     * Thermostat system state on message handler
     */
    public static class ThermostatSystemStateOnMsgHandler extends CustomMsgHandler {
        ThermostatSystemStateOnMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String state = group == 1 ? "COOLING" : "HEATING";
            return new StringType(state);
        }
    }

    /**
     * Thermostat system state off message handler
     */
    public static class ThermostatSystemStateOffMsgHandler extends CustomMsgHandler {
        ThermostatSystemStateOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            return new StringType("OFF");
        }
    }

    /**
     * Thermostat temperature format message handler
     */
    public static class ThermostatTemperatureFormatMsgHandler extends CustomBitmaskMsgHandler {
        ThermostatTemperatureFormatMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected State getBitFlagState(boolean isSet) {
            String format = isSet ? "CELSIUS" : "FAHRENHEIT";
            return new StringType(format);
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
        protected State getBitFlagState(boolean isSet) {
            String format = isSet ? "24H" : "12H";
            return new StringType(format);
        }
    }

    /**
     * Venstar thermostat fan state on message handler
     */
    public static class VenstarFanStateOnMsgHandler extends CustomMsgHandler {
        VenstarFanStateOnMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            return OnOffType.ON;
        }
    }

    /**
     * Venstar thermostat fan state off message handler
     */
    public static class VenstarFanStateOffMsgHandler extends CustomMsgHandler {
        VenstarFanStateOffMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            return OnOffType.OFF;
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String mode = getMode((int) value);
            return mode != null ? new StringType(mode) : UnDefType.UNDEF;
        }

        private @Nullable String getMode(int value) {
            switch (value) {
                case 0:
                    return "OFF";
                case 1:
                    return "HEAT";
                case 2:
                    return "COOL";
                case 3:
                    return "AUTO";
                case 4:
                    return "PROGRAM_AUTO";
                case 5:
                    return "PROGRAM_HEAT";
                case 6:
                    return "PROGRAM_COOL";
                default:
                    logger.warn("{}: got unexpected system mode value: {}", nm(), value);
                    return null;
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
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String mode = getMode((int) value);
            return mode != null ? new StringType(mode) : UnDefType.UNDEF;
        }

        private @Nullable String getMode(int value) {
            switch (value) {
                case 0x09:
                    return "OFF";
                case 0x04:
                    return "HEAT";
                case 0x05:
                    return "COOL";
                case 0x06:
                    return "AUTO";
                case 0x0A:
                    return "PROGRAM_HEAT";
                case 0x0B:
                    return "PROGRAM_COOL";
                case 0x0C:
                    return "PROGRAM_AUTO";
                default:
                    logger.warn("{}: got unexpected system mode reply value: {}", nm(), ByteUtils.getHexString(value));
                    return null;
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
            // use temperature format current state to determine temperature unit, defaulting to fahrenheit
            State state = getDevice().getState(FEATURE_TEMPERATURE_FORMAT);
            String format = state instanceof StringType ? ((StringType) state).toString() : "FAHRENHEIT";
            switch (format) {
                case "CELSIUS":
                    return SIUnits.CELSIUS;
                case "FAHRENHEIT":
                    return ImperialUnits.FAHRENHEIT;
                default:
                    logger.debug("{}: unable to determine temperature scale, defaulting to: FAHRENHEIT", nm());
                    return ImperialUnits.FAHRENHEIT;
            }
        }
    }

    /**
     * Venstar thermostat temperature format message handler
     */
    public static class VenstarTemperatureFormatMsgHandler extends CustomMsgHandler {
        VenstarTemperatureFormatMsgHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected @Nullable State getState(int group, byte cmd1, double value) {
            String format = BitwiseUtils.isBitFlagSet((int) value, 0) ? "CELSIUS" : "FAHRENHEIT";
            return new StringType(format);
        }
    }

    /**
     * IM button event message handler
     */
    public static class IMButtonEventHandler extends MessageHandler {
        IMButtonEventHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            try {
                int cmd = msg.getInt("buttonEvent");
                int button = getParameterAsInteger("button", 1);
                int mask = (button - 1) << 4;
                String event = getEvent(cmd ^ mask);
                if (event != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: IM {} received event {}", nm(), feature.getName(), event);
                    }
                    feature.triggerEvent(event);
                }
            } catch (FieldException e) {
                logger.warn("{}: error parsing msg {}", nm(), msg, e);
            }
        }

        private @Nullable String getEvent(int cmd) {
            switch (cmd) {
                case 0x02:
                    return "PRESSED";
                case 0x03:
                    return "HELD";
                case 0x04:
                    return "RELEASED";
                default:
                    return null;
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
        public void handleMessage(int group, byte cmd1, Msg msg) {
            try {
                int flags = msg.getInt("IMConfigurationFlags");
                int bit = getParameterAsInteger("bit", -1);
                if (bit < 3 || bit > 7) {
                    logger.debug("{}: no valid bit number defined for {}", nm(), feature.getName());
                    return;
                }
                boolean isSet = BitwiseUtils.isBitFlagSet(flags, bit);
                State state = isSet ^ getParameterAsBoolean("inverted", false) ? OnOffType.ON : OnOffType.OFF;
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: IM {} changed to {}", nm(), feature.getName(), state);
                }
                feature.setLastMsgValue(flags);
                feature.publishState(state, StateChangeType.ALWAYS);
            } catch (FieldException e) {
                logger.warn("{}: error parsing msg {}", nm(), msg, e);
            }
        }
    }

    /**
     * Process X10 messages that are generated when another controller
     * changes the state of an X10 device.
     */
    public static class X10OnHandler extends MessageHandler {
        X10OnHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: device {} changed to ON", nm(), getDevice().getAddress());
            }
            feature.publishState(OnOffType.ON, StateChangeType.ALWAYS);
        }
    }

    public static class X10OffHandler extends MessageHandler {
        X10OffHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: device {} changed to OFF", nm(), getDevice().getAddress());
            }
            feature.publishState(OnOffType.OFF, StateChangeType.ALWAYS);
        }
    }

    public static class X10BrightHandler extends MessageHandler {
        X10BrightHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: ignoring brighten message for device {}", nm(), getDevice().getAddress());
            }
        }
    }

    public static class X10DimHandler extends MessageHandler {
        X10DimHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: ignoring dim message for device {}", nm(), getDevice().getAddress());
            }
        }
    }

    public static class X10OpenHandler extends MessageHandler {
        X10OpenHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: device {} changed to OPEN", nm(), getDevice().getAddress());
            }
            feature.publishState(OpenClosedType.OPEN, StateChangeType.ALWAYS);
        }
    }

    public static class X10ClosedHandler extends MessageHandler {
        X10ClosedHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: device {} changed to CLOSED", nm(), getDevice().getAddress());
            }
            feature.publishState(OpenClosedType.CLOSED, StateChangeType.ALWAYS);
        }
    }

    /**
     * Factory method for creating default message handler
     *
     * @param feature the feature for which to create the handler
     * @return the default message handler which was created
     */
    public static DefaultMsgHandler makeDefaultHandler(DeviceFeature feature) {
        return new DefaultMsgHandler(feature);
    }

    /**
     * Factory method for creating handlers of a given name using java reflection
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
            logger.warn("error trying to create message handler: {}", name, e);
        }
        return null;
    }
}
