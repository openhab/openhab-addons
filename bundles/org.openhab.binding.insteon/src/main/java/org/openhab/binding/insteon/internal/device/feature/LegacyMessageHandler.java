/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.insteon.internal.InsteonLegacyBindingConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.DeviceAddress;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.LegacyDevice;
import org.openhab.binding.insteon.internal.device.LegacyDeviceFeature;
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureListener.StateChangeType;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.LegacyGroupMessageStateMachine.GroupMessage;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.transport.message.MsgType;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.binding.insteon.internal.utils.ParameterParser;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message handler processes incoming Insteon messages and reacts by publishing
 * corresponding messages on the openhab bus, updating device state etc.
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Bernd Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public abstract class LegacyMessageHandler {
    protected final Logger logger = LoggerFactory.getLogger(LegacyMessageHandler.class);

    protected LegacyDeviceFeature feature;
    protected Map<String, String> parameters = new HashMap<>();

    LegacyMessageHandler(LegacyDeviceFeature feature) {
        this.feature = feature;
    }

    /**
     * Method that processes incoming message. The cmd1 parameter
     * has been extracted earlier already (to make a decision which message handler to call),
     * and is passed in as an argument so cmd1 does not have to be extracted from the message again.
     *
     * @param group all-link group or -1 if not specified
     * @param cmd1 the insteon cmd1 field
     * @param msg the received insteon message
     * @param feature the DeviceFeature to which this message handler is attached
     */
    public abstract void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature feature);

    /**
     * Method to send an extended insteon message for querying a device
     *
     * @param feature DeviceFeature that is being currently handled
     * @param cmd1 cmd1 for message to be sent
     * @param cmd2 cmd2 for message to be sent
     */
    public void sendExtendedQuery(LegacyDeviceFeature feature, byte cmd1, byte cmd2) {
        LegacyDevice device = feature.getDevice();
        try {
            Msg msg = Msg.makeExtendedMessage((InsteonAddress) device.getAddress(), cmd1, cmd2, true);
            msg.setQuietTime(500L);
            device.enqueueMessage(msg, feature);
        } catch (InvalidMessageTypeException e) {
            logger.warn("msg exception sending query message to device {}", device.getAddress());
        } catch (FieldException e) {
            logger.warn("field exception sending query message to device {}", device.getAddress());
        }
    }

    /**
     * Check if group matches
     *
     * @param group group to test for
     * @return true if group matches or no group is specified
     */
    public boolean matchesGroup(int group) {
        int parameter = getIntParameter("group", -1);
        return parameter == -1 || parameter == group;
    }

    /**
     * Retrieve group parameter or -1 if no group is specified
     *
     * @return group parameter
     */
    public int getGroup() {
        return getIntParameter("group", -1);
    }

    /**
     * Helper function to get an integer parameter for the handler
     *
     * @param key name of the int parameter (as specified in device features!)
     * @param def default to return if parameter not found
     * @return value of int parameter (or default if not found)
     */
    protected int getIntParameter(String key, int def) {
        return ParameterParser.getParameterAsOrDefault(parameters.get(key), Integer.class, def);
    }

    /**
     * Helper function to get a String parameter for the handler
     *
     * @param key name of the String parameter (as specified in device features!)
     * @param def default to return if parameter not found
     * @return value of parameter (or default if not found)
     */
    protected @Nullable String getStringParameter(String key, @Nullable String def) {
        String str = parameters.get(key);
        return str != null ? str : def;
    }

    /**
     * Helper function to get a double parameter for the handler
     *
     * @param key name of the parameter (as specified in device features!)
     * @param def default to return if parameter not found
     * @return value of parameter (or default if not found)
     */
    protected double getDoubleParameter(String key, double def) {
        try {
            String str = parameters.get(key);
            return str != null ? Double.parseDouble(str) : def;
        } catch (NumberFormatException e) {
            logger.warn("malformed int parameter in message handler: {}", key);
        }
        return def;
    }

    protected boolean getBooleanDeviceConfig(String key, boolean def) {
        Object value = feature.getDevice().getDeviceConfigMap().get(key);
        if (value != null) {
            if (value instanceof Boolean booleanValue) {
                return booleanValue;
            } else {
                logger.warn("{} {}: The value for the '{}' key is not boolean in the device configuration parameter.",
                        nm(), feature.getDevice().getAddress(), key);
            }
        }

        return def;
    }

    /**
     * Test if message refers to the button configured for given feature
     *
     * @param msg received message
     * @param feature device feature to test
     * @return true if we have no button configured or the message is for this button
     */
    protected boolean isMybutton(Msg msg, LegacyDeviceFeature feature) {
        int myButton = getIntParameter("button", -1);
        // if there is no button configured for this handler
        // the message is assumed to refer to this feature
        // no matter what button is addressed in the message
        if (myButton == -1) {
            return true;
        }

        int button = getButtonInfo(msg, feature);
        return button != -1 && myButton == button;
    }

    /**
     * Test if parameter matches value
     *
     * @param param name of parameter to match
     * @param msg message to search
     * @param field field name to match
     * @return true if parameter matches
     * @throws FieldException if field not there
     */
    protected boolean testMatch(String param, Msg msg, String field) throws FieldException {
        int mp = getIntParameter(param, -1);
        // parameter not filtered for, declare this a match!
        if (mp == -1) {
            return true;
        }
        byte value = msg.getByte(field);
        return value == mp;
    }

    /**
     * Test if message matches the filter parameters
     *
     * @param msg message to be tested against
     * @return true if message matches
     */
    public boolean matches(Msg msg) {
        try {
            int ext = getIntParameter("ext", -1);
            if (ext != -1) {
                if ((msg.isExtended() && ext != 1) || (!msg.isExtended() && ext != 0)) {
                    return false;
                }
                if (!testMatch("match_cmd1", msg, "command1")) {
                    return false;
                }
            }
            if (!testMatch("match_cmd2", msg, "command2")) {
                return false;
            }
            if (!testMatch("match_d1", msg, "userData1")) {
                return false;
            }
            if (!testMatch("match_d2", msg, "userData2")) {
                return false;
            }
            if (!testMatch("match_d3", msg, "userData3")) {
                return false;
            }
        } catch (FieldException e) {
            logger.warn("error matching message: {}", msg, e);
            return false;
        }
        return true;
    }

    /**
     * Determines is an incoming ALL LINK message is a duplicate
     *
     * @param msg the received ALL LINK message
     * @return true if this message is a duplicate
     */
    protected boolean isDuplicate(Msg msg) {
        boolean isDuplicate = false;
        try {
            MsgType msgType = MsgType.valueOf(msg.getByte("messageFlags"));
            if (msgType == MsgType.ALL_LINK_BROADCAST) {
                int group = msg.getInsteonAddress("toAddress").getLowByte() & 0xff;
                byte cmd1 = msg.getByte("command1");
                // if the command is 0x06, then it's success message
                // from the original broadcaster, with which the device
                // confirms that it got all cleanup replies successfully.
                GroupMessage gm = (cmd1 == 0x06) ? GroupMessage.SUCCESS : GroupMessage.BCAST;
                isDuplicate = !feature.getDevice().getGroupState(group, gm, cmd1);
            } else if (msgType == MsgType.ALL_LINK_CLEANUP) {
                // the cleanup messages are direct messages, so the
                // group # is not in the toAddress, but in cmd2
                int group = msg.getByte("command2") & 0xff;
                isDuplicate = !feature.getDevice().getGroupState(group, GroupMessage.CLEAN, (byte) 0);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("cannot parse msg: {}", msg, e);
        } catch (FieldException e) {
            logger.warn("cannot parse msg: {}", msg, e);
        }
        return isDuplicate;
    }

    /**
     * Extract button information from message
     *
     * @param msg the message to extract from
     * @param feature the device feature (needed for debug printing)
     * @return the button number or -1 if no button found
     */
    protected int getButtonInfo(Msg msg, LegacyDeviceFeature feature) {
        // the cleanup messages have the button number in the command2 field
        // the broadcast messages have it as the lsb of the toAddress
        try {
            int bclean = msg.getByte("command2") & 0xff;
            int bbcast = msg.getInsteonAddress("toAddress").getLowByte() & 0xff;
            int button = msg.isAllLinkCleanup() ? bclean : bbcast;
            logger.trace("{} button: {} bclean: {} bbcast: {}", feature.getDevice().getAddress(), button, bclean,
                    bbcast);
            return button;
        } catch (FieldException e) {
            logger.warn("field exception while parsing msg {}: ", msg, e);
        }
        return -1;
    }

    /**
     * Shorthand to return class name for logging purposes
     *
     * @return name of the class
     */
    protected String nm() {
        return this.getClass().getSimpleName();
    }

    /**
     * Set parameter map
     *
     * @param map the parameter map for this message handler
     */
    public void setParameters(Map<String, String> map) {
        parameters = map;
    }

    public static class DefaultMsgHandler extends LegacyMessageHandler {
        public DefaultMsgHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} ignoring unimpl message with cmd1:{}", nm(), HexUtils.getHexString(cmd1));
            }
        }
    }

    public static class NoOpMsgHandler extends LegacyMessageHandler {
        NoOpMsgHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            if (logger.isTraceEnabled()) {
                logger.trace("{} ignore msg {}: {}", nm(), HexUtils.getHexString(cmd1), msg);
            }
        }
    }

    public static class LightOnDimmerHandler extends LegacyMessageHandler {
        LightOnDimmerHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            if (!isMybutton(msg, f)) {
                return;
            }
            DeviceAddress address = f.getDevice().getAddress();
            if (msg.isAckOfDirect()) {
                logger.warn("{}: device {}: ignoring ack of direct.", nm(), address);
            } else {
                String mode = getStringParameter("mode", "REGULAR");
                logger.debug("{}: device {} was turned on {}. " + "Sending poll request to get actual level", nm(),
                        address, mode);
                feature.publish(PercentType.HUNDRED, StateChangeType.ALWAYS);
                // need to poll to find out what level the dimmer is at now.
                // it may not be at 100% because dimmers can be configured
                // to switch to e.g. 75% when turned on.
                Msg m = f.makePollMsg();
                if (m != null) {
                    f.getDevice().enqueueDelayedMessage(m, f, 1000);
                }
            }
        }
    }

    public static class LightOffDimmerHandler extends LegacyMessageHandler {
        LightOffDimmerHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            if (isMybutton(msg, f)) {
                String mode = getStringParameter("mode", "REGULAR");
                logger.debug("{}: device {} was turned off {}.", nm(), f.getDevice().getAddress(), mode);
                f.publish(PercentType.ZERO, StateChangeType.ALWAYS);
            }
        }
    }

    public static class LightOnSwitchHandler extends LegacyMessageHandler {
        LightOnSwitchHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            if (isMybutton(msg, f)) {
                String mode = getStringParameter("mode", "REGULAR");
                logger.debug("{}: device {} was switched on {}.", nm(), f.getDevice().getAddress(), mode);
                f.publish(OnOffType.ON, StateChangeType.ALWAYS);
            } else {
                logger.debug("ignored message: {}", isMybutton(msg, f));
            }
        }
    }

    public static class LightOffSwitchHandler extends LegacyMessageHandler {
        LightOffSwitchHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            if (isMybutton(msg, f)) {
                String mode = getStringParameter("mode", "REGULAR");
                logger.debug("{}: device {} was switched off {}.", nm(), f.getDevice().getAddress(), mode);
                f.publish(OnOffType.OFF, StateChangeType.ALWAYS);
            }
        }
    }

    /**
     * This message handler processes replies to Ramp ON/OFF commands.
     * Currently, it's been tested for the 2672-222 LED Bulb. Other
     * devices may use a different pair of commands (0x2E, 0x2F). This
     * handler and the command handler will need to be extended to support
     * those devices.
     */
    public static class RampDimmerHandler extends LegacyMessageHandler {
        private int onCmd;
        private int offCmd;

        RampDimmerHandler(LegacyDeviceFeature feature) {
            super(feature);
            // Can't process parameters here because they are set after constructor is invoked.
            // Unfortunately, this means we can't declare the onCmd, offCmd to be final.
        }

        @Override
        public void setParameters(Map<String, String> params) {
            super.setParameters(params);
            onCmd = getIntParameter("on", 0x2E);
            offCmd = getIntParameter("off", 0x2F);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            if (cmd1 == onCmd) {
                int level = getLevel(msg);
                logger.debug("{}: device {} was switched on using ramp to level {}.", nm(), f.getDevice().getAddress(),
                        level);
                if (level == 100) {
                    f.publish(OnOffType.ON, StateChangeType.ALWAYS);
                } else {
                    // The publisher will convert an ON at level==0 to an OFF.
                    // However, this is not completely accurate since a ramp
                    // off at level == 0 may not turn off the dimmer completely
                    // (if I understand the Insteon docs correctly). In any
                    // case,
                    // it would be an odd scenario to turn ON a light at level
                    // == 0
                    // rather than turn if OFF.
                    f.publish(new PercentType(level), StateChangeType.ALWAYS);
                }
            } else if (cmd1 == offCmd) {
                logger.debug("{}: device {} was switched off using ramp.", nm(), f.getDevice().getAddress());
                f.publish(new PercentType(0), StateChangeType.ALWAYS);
            }
        }

        private int getLevel(Msg msg) {
            try {
                byte cmd2 = msg.getByte("command2");
                return (int) Math.round(((cmd2 >> 4) & 0x0f) * (100 / 15d));
            } catch (FieldException e) {
                logger.warn("Can't access command2 byte", e);
                return 0;
            }
        }
    }

    /**
     * A message handler that processes replies to queries.
     * If command2 == 0xFF then the light has been turned on
     * else if command2 == 0x00 then the light has been turned off
     */

    public static class SwitchRequestReplyHandler extends LegacyMessageHandler {
        SwitchRequestReplyHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            try {
                DeviceAddress address = f.getDevice().getAddress();
                int cmd2 = msg.getByte("command2") & 0xff;
                int button = this.getIntParameter("button", -1);
                if (button < 0) {
                    handleNoButtons(cmd2, address, msg);
                } else {
                    boolean isOn = isLEDLit(cmd2, button);
                    logger.debug("{}: dev {} button {} switched to {}", nm(), address, button, isOn ? "ON" : "OFF");
                    feature.publish(OnOffType.from(isOn), StateChangeType.CHANGED);
                }
            } catch (FieldException e) {
                logger.warn("{} error parsing {}: ", nm(), msg, e);
            }
        }

        /**
         * Handle the case where no buttons have been configured.
         * In this situation, the only return values should be 0 (light off)
         * or 0xff (light on)
         *
         * @param cmd2
         */
        void handleNoButtons(int cmd2, DeviceAddress a, Msg msg) {
            if (cmd2 == 0) {
                logger.debug("{}: set device {} to OFF", nm(), a);
                feature.publish(OnOffType.OFF, StateChangeType.CHANGED);
            } else if (cmd2 == 0xff) {
                logger.debug("{}: set device {} to ON", nm(), a);
                feature.publish(OnOffType.ON, StateChangeType.CHANGED);
            } else {
                logger.warn("{}: {} ignoring unexpected cmd2 in msg: {}", nm(), a, msg);
            }
        }

        /**
         * Test if cmd byte indicates that button is lit.
         * The cmd byte has the LED status bitwise from the left:
         * 87654321
         * Note that the 2487S has buttons assigned like this:
         * 22|6543|11
         * They used the basis of the 8-button remote, and assigned
         * the ON button to 1+2, the OFF button to 7+8
         *
         * @param cmd cmd byte as received in message
         * @param button button to test (number in range 1..8)
         * @return true if button is lit, false otherwise
         */
        private boolean isLEDLit(int cmd, int button) {
            boolean isSet = (cmd & (0x1 << (button - 1))) != 0;
            logger.trace("cmd: {} button {}", Integer.toBinaryString(cmd), button);
            logger.trace("msk: {} isSet: {}", Integer.toBinaryString(0x1 << (button - 1)), isSet);
            return isSet;
        }
    }

    /**
     * Handles Dimmer replies to status requests.
     * In the dimmers case the command2 byte represents the light level from 0-255
     */
    public static class DimmerRequestReplyHandler extends LegacyMessageHandler {
        DimmerRequestReplyHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            LegacyDevice dev = f.getDevice();
            try {
                int cmd2 = msg.getByte("command2") & 0xff;
                if (cmd2 == 0xfe) {
                    // sometimes dimmer devices are returning 0xfe when on instead of 0xff
                    cmd2 = 0xff;
                }

                if (cmd2 == 0) {
                    logger.debug("{}: set device {} to level 0", nm(), dev.getAddress());
                    feature.publish(PercentType.ZERO, StateChangeType.CHANGED);
                } else if (cmd2 == 0xff) {
                    logger.debug("{}: set device {} to level 100", nm(), dev.getAddress());
                    feature.publish(PercentType.HUNDRED, StateChangeType.CHANGED);
                } else {
                    int level = cmd2 * 100 / 255;
                    if (level == 0) {
                        level = 1;
                    }
                    logger.debug("{}: set device {} to level {}", nm(), dev.getAddress(), level);
                    feature.publish(new PercentType(level), StateChangeType.CHANGED);
                }
            } catch (FieldException e) {
                logger.warn("{}: error parsing {}: ", nm(), msg, e);
            }
        }
    }

    public static class DimmerStopManualChangeHandler extends LegacyMessageHandler {
        DimmerStopManualChangeHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean isDuplicate(Msg msg) {
            // Disable duplicate elimination because
            // there are no cleanup or success messages for start/stop.
            return false;
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            Msg m = f.makePollMsg();
            if (m != null) {
                f.getDevice().enqueueMessage(m, f);
            }
        }
    }

    public static class StartManualChangeHandler extends LegacyMessageHandler {
        StartManualChangeHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean isDuplicate(Msg msg) {
            // Disable duplicate elimination because
            // there are no cleanup or success messages for start/stop.
            return false;
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            try {
                int cmd2 = msg.getByte("command2") & 0xff;
                int upDown = (cmd2 == 0) ? 0 : 2;
                logger.debug("{}: dev {} manual state change: {}", nm(), f.getDevice().getAddress(),
                        (upDown == 0) ? "DOWN" : "UP");
                feature.publish(new DecimalType(upDown), StateChangeType.ALWAYS);
            } catch (FieldException e) {
                logger.warn("{} error parsing {}: ", nm(), msg, e);
            }
        }
    }

    public static class StopManualChangeHandler extends LegacyMessageHandler {
        StopManualChangeHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean isDuplicate(Msg msg) {
            // Disable duplicate elimination because
            // there are no cleanup or success messages for start/stop.
            return false;
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            logger.debug("{}: dev {} manual state change: {}", nm(), f.getDevice().getAddress(), 0);
            feature.publish(new DecimalType(1), StateChangeType.ALWAYS);
        }
    }

    public static class InfoRequestReplyHandler extends LegacyMessageHandler {
        InfoRequestReplyHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            LegacyDevice dev = f.getDevice();
            if (!msg.isExtended()) {
                logger.warn("{} device {} expected extended msg as info reply, got {}", nm(), dev.getAddress(), msg);
                return;
            }
            try {
                int cmd2 = msg.getByte("command2") & 0xff;
                switch (cmd2) {
                    case 0x00: // this is a product data response message
                        int prodKey = msg.getInt24("userData2");
                        int devCat = msg.getByte("userData5");
                        int subCat = msg.getByte("userData6");
                        logger.debug("{} {} got product data: cat: {} subcat: {} key: {} ", nm(), dev.getAddress(),
                                devCat, subCat, HexUtils.getHexString(prodKey));
                        break;
                    case 0x02: // this is a device text string response message
                        logger.debug("{} {} got text str {} ", nm(), dev.getAddress(), msg);
                        break;
                    default:
                        logger.warn("{} unknown cmd2 = {} in info reply message {}", nm(), cmd2, msg);
                        break;
                }
            } catch (FieldException e) {
                logger.warn("error parsing {}: ", msg, e);
            }
        }
    }

    public static class MotionSensorDataReplyHandler extends LegacyMessageHandler {
        MotionSensorDataReplyHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            LegacyDevice dev = f.getDevice();
            if (!msg.isExtended()) {
                logger.trace("{} device {} ignoring non-extended msg {}", nm(), dev.getAddress(), msg);
                return;
            }
            try {
                int cmd2 = msg.getByte("command2") & 0xff;
                int batteryLevel;
                int lightLevel;
                int temperatureLevel;
                switch (cmd2) {
                    case 0x00: // this is a product data response message
                        batteryLevel = msg.getByte("userData12") & 0xff;
                        lightLevel = msg.getByte("userData11") & 0xff;
                        logger.debug("{}: {} got light level: {}, battery level: {}", nm(), dev.getAddress(),
                                lightLevel, batteryLevel);
                        feature.publish(new DecimalType(lightLevel), StateChangeType.CHANGED, FIELD, FIELD_LIGHT_LEVEL);
                        feature.publish(new DecimalType(batteryLevel), StateChangeType.CHANGED, FIELD,
                                FIELD_BATTERY_LEVEL);
                        break;
                    case 0x03: // this is the 2844-222 data response message
                        batteryLevel = msg.getByte("userData6") & 0xff;
                        lightLevel = msg.getByte("userData7") & 0xff;
                        temperatureLevel = msg.getByte("userData8") & 0xff;
                        logger.debug("{}: {} got light level: {}, battery level: {}, temperature level: {}", nm(),
                                dev.getAddress(), lightLevel, batteryLevel, temperatureLevel);
                        feature.publish(new DecimalType(lightLevel), StateChangeType.CHANGED, FIELD, FIELD_LIGHT_LEVEL);
                        feature.publish(new DecimalType(batteryLevel), StateChangeType.CHANGED, FIELD,
                                FIELD_BATTERY_LEVEL);
                        feature.publish(new DecimalType(temperatureLevel), StateChangeType.CHANGED, FIELD,
                                FIELD_TEMPERATURE_LEVEL);

                        // per 2844-222 dev doc: working battery level range is 0xd2 - 0x70
                        int batteryPercentage;
                        if (batteryLevel >= 0xd2) {
                            batteryPercentage = 100;
                        } else if (batteryLevel <= 0x70) {
                            batteryPercentage = 0;
                        } else {
                            batteryPercentage = (batteryLevel - 0x70) * 100 / (0xd2 - 0x70);
                        }
                        logger.debug("{}: {} battery percentage: {}", nm(), dev.getAddress(), batteryPercentage);
                        feature.publish(new QuantityType<>(batteryPercentage, Units.PERCENT), StateChangeType.CHANGED,
                                FIELD, FIELD_BATTERY_PERCENTAGE);
                        break;
                    default:
                        logger.warn("unknown cmd2 = {} in info reply message {}", cmd2, msg);
                        break;
                }
            } catch (FieldException e) {
                logger.warn("error parsing {}: ", msg, e);
            }
        }
    }

    public static class MotionSensor2AlternateHeartbeatHandler extends LegacyMessageHandler {
        MotionSensor2AlternateHeartbeatHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            LegacyDevice dev = f.getDevice();
            try {
                // group 0x0B (11) - alternate heartbeat group
                InsteonAddress toAddr = msg.getInsteonAddress("toAddress");
                int batteryLevel = toAddr.getHighByte() & 0xff;
                int lightLevel = toAddr.getMiddleByte() & 0xff;
                int temperatureLevel = msg.getByte("command2") & 0xff;

                logger.debug("{}: {} got light level: {}, battery level: {}, temperature level: {}", nm(),
                        dev.getAddress(), lightLevel, batteryLevel, temperatureLevel);
                feature.publish(new DecimalType(lightLevel), StateChangeType.CHANGED, FIELD, FIELD_LIGHT_LEVEL);
                feature.publish(new DecimalType(batteryLevel), StateChangeType.CHANGED, FIELD, FIELD_BATTERY_LEVEL);
                feature.publish(new DecimalType(temperatureLevel), StateChangeType.CHANGED, FIELD,
                        FIELD_TEMPERATURE_LEVEL);

                // per 2844-222 dev doc: working battery level range is 0xd2 - 0x70
                int batteryPercentage;
                if (batteryLevel >= 0xd2) {
                    batteryPercentage = 100;
                } else if (batteryLevel <= 0x70) {
                    batteryPercentage = 0;
                } else {
                    batteryPercentage = (batteryLevel - 0x70) * 100 / (0xd2 - 0x70);
                }
                logger.debug("{}: {} battery percentage: {}", nm(), dev.getAddress(), batteryPercentage);
                feature.publish(new QuantityType<>(batteryPercentage, Units.PERCENT), StateChangeType.CHANGED, FIELD,
                        FIELD_BATTERY_PERCENTAGE);
            } catch (FieldException e) {
                logger.warn("error parsing {}: ", msg, e);
            }
        }
    }

    public static class HiddenDoorSensorDataReplyHandler extends LegacyMessageHandler {
        HiddenDoorSensorDataReplyHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            LegacyDevice dev = f.getDevice();
            if (!msg.isExtended()) {
                logger.trace("{} device {} ignoring non-extended msg {}", nm(), dev.getAddress(), msg);
                return;
            }
            try {
                int cmd2 = msg.getByte("command2") & 0xff;
                switch (cmd2) {
                    case 0x00: // this is a product data response message
                        int batteryLevel = msg.getByte("userData4") & 0xff;
                        int batteryWatermark = msg.getByte("userData7") & 0xff;
                        logger.debug("{}: {} got light level: {}, battery level: {}", nm(), dev.getAddress(),
                                batteryWatermark, batteryLevel);
                        feature.publish(new DecimalType(batteryWatermark), StateChangeType.CHANGED, FIELD,
                                FIELD_BATTERY_WATERMARK_LEVEL);
                        feature.publish(new DecimalType(batteryLevel), StateChangeType.CHANGED, FIELD,
                                FIELD_BATTERY_LEVEL);
                        break;
                    default:
                        logger.warn("unknown cmd2 = {} in info reply message {}", cmd2, msg);
                        break;
                }
            } catch (FieldException e) {
                logger.warn("error parsing {}: ", msg, e);
            }
        }
    }

    public static class PowerMeterUpdateHandler extends LegacyMessageHandler {
        PowerMeterUpdateHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            if (msg.isExtended()) {
                try {
                    // see iMeter developer notes 2423A1dev-072013-en.pdf
                    int b7 = msg.getByte("userData7") & 0xff;
                    int b8 = msg.getByte("userData8") & 0xff;
                    int watts = (b7 << 8) | b8;
                    if (watts > 32767) {
                        watts -= 65535;
                    }

                    int b9 = msg.getByte("userData9") & 0xff;
                    int b10 = msg.getByte("userData10") & 0xff;
                    int b11 = msg.getByte("userData11") & 0xff;
                    int b12 = msg.getByte("userData12") & 0xff;
                    BigDecimal kwh = BigDecimal.ZERO;
                    if (b9 < 254) {
                        int e = (b9 << 24) | (b10 << 16) | (b11 << 8) | b12;
                        kwh = new BigDecimal(e * 65535.0 / (1000 * 60 * 60 * 60)).setScale(4, RoundingMode.HALF_UP);
                    }

                    logger.debug("{}:{} watts: {} kwh: {} ", nm(), f.getDevice().getAddress(), watts, kwh);
                    feature.publish(new QuantityType<>(kwh, Units.KILOWATT_HOUR), StateChangeType.CHANGED, FIELD,
                            FIELD_KWH);
                    feature.publish(new QuantityType<>(watts, Units.WATT), StateChangeType.CHANGED, FIELD, FIELD_WATTS);
                } catch (FieldException e) {
                    logger.warn("error parsing {}: ", msg, e);
                }
            }
        }
    }

    public static class PowerMeterResetHandler extends LegacyMessageHandler {
        PowerMeterResetHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            LegacyDevice dev = f.getDevice();
            logger.debug("{}: power meter {} was reset", nm(), dev.getAddress());

            // poll device to get updated kilowatt hours and watts
            Msg m = f.makePollMsg();
            if (m != null) {
                f.getDevice().enqueueMessage(m, f);
            }
        }
    }

    public static class LastTimeHandler extends LegacyMessageHandler {
        LastTimeHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1a, Msg msg, LegacyDeviceFeature f) {
            feature.publish(new DateTimeType(), StateChangeType.ALWAYS);
        }
    }

    public static class ContactRequestReplyHandler extends LegacyMessageHandler {
        ContactRequestReplyHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1a, Msg msg, LegacyDeviceFeature f) {
            byte cmd = 0x00;
            byte cmd2 = 0x00;
            try {
                cmd = msg.getByte("Cmd");
                cmd2 = msg.getByte("command2");
            } catch (FieldException e) {
                logger.debug("{} no cmd found, dropping msg {}", nm(), msg);
                return;
            }
            if (msg.isAckOfDirect() && (f.getQueryStatus() == LegacyDeviceFeature.QueryStatus.QUERY_PENDING)
                    && cmd == 0x50) {
                OpenClosedType oc = (cmd2 == 0) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                logger.debug("{}: set contact {} to: {}", nm(), f.getDevice().getAddress(), oc);
                feature.publish(oc, StateChangeType.CHANGED);
            }
        }
    }

    public static class ClosedContactHandler extends LegacyMessageHandler {
        ClosedContactHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            feature.publish(OpenClosedType.CLOSED, StateChangeType.ALWAYS);
        }
    }

    public static class OpenedContactHandler extends LegacyMessageHandler {
        OpenedContactHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            feature.publish(OpenClosedType.OPEN, StateChangeType.ALWAYS);
        }
    }

    public static class OpenedOrClosedContactHandler extends LegacyMessageHandler {
        OpenedOrClosedContactHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            try {
                byte cmd2 = msg.getByte("command2");
                switch (cmd1) {
                    case 0x11:
                        switch (cmd2) {
                            case 0x02:
                                feature.publish(OpenClosedType.CLOSED, StateChangeType.CHANGED);
                                break;
                            case 0x01:
                            case 0x04:
                                feature.publish(OpenClosedType.OPEN, StateChangeType.CHANGED);
                                break;
                            default: // do nothing
                                break;
                        }
                        break;
                    case 0x13:
                        switch (cmd2) {
                            case 0x04:
                                feature.publish(OpenClosedType.CLOSED, StateChangeType.CHANGED);
                                break;
                            default: // do nothing
                                break;
                        }
                        break;
                }
            } catch (FieldException e) {
                logger.debug("{} no cmd2 found, dropping msg {}", nm(), msg);
                return;
            }
        }
    }

    public static class ClosedSleepingContactHandler extends LegacyMessageHandler {
        ClosedSleepingContactHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            feature.publish(OpenClosedType.CLOSED, StateChangeType.ALWAYS);
            if (f.getDevice().hasProductKey(MOTION_SENSOR_II_PRODUCT_KEY)) {
                if (!getBooleanDeviceConfig("heartbeatOnly", false)) {
                    sendExtendedQuery(f, (byte) 0x2e, (byte) 03);
                }
            } else {
                sendExtendedQuery(f, (byte) 0x2e, (byte) 00);
            }
        }
    }

    public static class OpenedSleepingContactHandler extends LegacyMessageHandler {
        OpenedSleepingContactHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            feature.publish(OpenClosedType.OPEN, StateChangeType.ALWAYS);
            if (f.getDevice().hasProductKey(MOTION_SENSOR_II_PRODUCT_KEY)) {
                if (!getBooleanDeviceConfig("heartbeatOnly", false)) {
                    sendExtendedQuery(f, (byte) 0x2e, (byte) 03);
                }
            } else {
                sendExtendedQuery(f, (byte) 0x2e, (byte) 00);
            }
        }
    }

    /**
     * Triggers a poll when a message comes in. Use this handler to react
     * to messages that notify of a status update, but don't carry the information
     * that you are interested in. Example: you send a command to change a setting,
     * get a DIRECT ack back, but the ack does not have the value of the updated setting.
     * Then connect this handler to the ACK, such that the device will be polled, and
     * the settings updated.
     */
    public static class TriggerPollMsgHandler extends LegacyMessageHandler {
        TriggerPollMsgHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            feature.getDevice().doPoll(2000); // 2000 ms delay
        }
    }

    /**
     * Flexible handler to extract numerical data from messages.
     */
    public static class NumberMsgHandler extends LegacyMessageHandler {
        NumberMsgHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            try {
                // first do the bit manipulations to focus on the right area
                int mask = getIntParameter("mask", 0xFFFF);
                int rawValue = extractValue(msg, group);
                int cooked = (rawValue & mask) >> getIntParameter("rshift", 0);
                // now do an arbitrary transform on the data
                double value = transform(cooked);
                // last, multiply with factor and add an offset
                double dvalue = getDoubleParameter("offset", 0) + value * getDoubleParameter("factor", 1.0);

                @Nullable
                State state;
                String scale = getStringParameter("scale", null);
                if ("celsius".equals(scale)) {
                    state = new QuantityType<>(dvalue, SIUnits.CELSIUS);
                } else if ("fahrenheit".equals(scale)) {
                    state = new QuantityType<>(dvalue, ImperialUnits.FAHRENHEIT);
                } else {
                    state = new DecimalType(dvalue);
                }
                feature.publish(state, StateChangeType.CHANGED);
            } catch (FieldException e) {
                logger.warn("error parsing {}: ", msg, e);
            }
        }

        public int transform(int raw) {
            return raw;
        }

        private int extractValue(Msg msg, int group) throws FieldException {
            String lowByte = getStringParameter("low_byte", null);
            if (lowByte == null) {
                logger.warn("{} handler misconfigured, missing low_byte!", nm());
                return 0;
            }
            int value = 0;
            if ("group".equals(lowByte)) {
                value = group;
            } else {
                value = msg.getByte(lowByte) & 0xFF;
            }
            String highByte = getStringParameter("high_byte", null);
            if (highByte != null) {
                value |= (msg.getByte(highByte) & 0xFF) << 8;
            }
            return value;
        }
    }

    /**
     * Convert system mode field to number 0...4. Insteon has two different
     * conventions for numbering, we use the one of the status update messages
     */
    public static class ThermostatSystemModeMsgHandler extends NumberMsgHandler {
        ThermostatSystemModeMsgHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public int transform(int raw) {
            switch (raw) {
                case 0:
                    return 0; // off
                case 1:
                    return 3; // auto
                case 2:
                    return 1; // heat
                case 3:
                    return 2; // cool
                case 4:
                    return 4; // program
                default:
                    break;
            }
            return 4; // when in doubt assume to be in "program" mode
        }
    }

    /**
     * Handle reply to system mode change command
     */
    public static class ThermostatSystemModeReplyHandler extends NumberMsgHandler {
        ThermostatSystemModeReplyHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public int transform(int raw) {
            switch (raw) {
                case 0x09:
                    return 0; // off
                case 0x04:
                    return 1; // heat
                case 0x05:
                    return 2; // cool
                case 0x06:
                    return 3; // auto
                case 0x0A:
                    return 4; // program
                default:
                    break;
            }
            return 4; // when in doubt assume to be in "program" mode
        }
    }

    /**
     * Handle reply to fan mode change command
     */
    public static class ThermostatFanModeReplyHandler extends NumberMsgHandler {
        ThermostatFanModeReplyHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public int transform(int raw) {
            switch (raw) {
                case 0x08:
                    return 0; // auto
                case 0x07:
                    return 1; // always on
                default:
                    break;
            }
            return 0; // when in doubt assume to be auto mode
        }
    }

    /**
     * Handle reply to fanlinc fan speed change command
     */
    public static class FanLincFanReplyHandler extends NumberMsgHandler {
        FanLincFanReplyHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public int transform(int raw) {
            switch (raw) {
                case 0x00:
                    return 0; // off
                case 0x55:
                    return 1; // low
                case 0xAA:
                    return 2; // medium
                case 0xFF:
                    return 3; // high
                default:
                    logger.warn("fanlinc got unexpected level: {}", raw);
            }
            return 0; // when in doubt assume to be off
        }
    }

    /**
     * Process X10 messages that are generated when another controller
     * changes the state of an X10 device.
     */
    public static class X10OnHandler extends LegacyMessageHandler {
        X10OnHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            DeviceAddress address = f.getDevice().getAddress();
            logger.debug("{}: set X10 device {} to ON", nm(), address);
            feature.publish(OnOffType.ON, StateChangeType.ALWAYS);
        }
    }

    public static class X10OffHandler extends LegacyMessageHandler {
        X10OffHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            DeviceAddress address = f.getDevice().getAddress();
            logger.debug("{}: set X10 device {} to OFF", nm(), address);
            feature.publish(OnOffType.OFF, StateChangeType.ALWAYS);
        }
    }

    public static class X10BrightHandler extends LegacyMessageHandler {
        X10BrightHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            DeviceAddress address = f.getDevice().getAddress();
            logger.debug("{}: ignoring brighten message for device {}", nm(), address);
        }
    }

    public static class X10DimHandler extends LegacyMessageHandler {
        X10DimHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            DeviceAddress address = f.getDevice().getAddress();
            logger.debug("{}: ignoring dim message for device {}", nm(), address);
        }
    }

    public static class X10OpenHandler extends LegacyMessageHandler {
        X10OpenHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature f) {
            DeviceAddress address = f.getDevice().getAddress();
            logger.debug("{}: set X10 device {} to OPEN", nm(), address);
            feature.publish(OpenClosedType.OPEN, StateChangeType.ALWAYS);
        }
    }

    public static class X10ClosedHandler extends LegacyMessageHandler {
        X10ClosedHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleMessage(int group, byte cmd1, Msg msg, LegacyDeviceFeature feature) {
            DeviceAddress address = feature.getDevice().getAddress();
            logger.debug("{}: set X10 device {} to CLOSED", nm(), address);
            feature.publish(OpenClosedType.CLOSED, StateChangeType.ALWAYS);
        }
    }

    /**
     * Factory method for creating handlers of a given name using java reflection
     *
     * @param name the name of the handler to create
     * @param params
     * @param feature the feature for which to create the handler
     * @return the handler which was created
     */
    public static @Nullable <T extends LegacyMessageHandler> T makeHandler(String name, Map<String, String> params,
            LegacyDeviceFeature feature) {
        try {
            String className = LegacyMessageHandler.class.getName() + "$" + name;
            @SuppressWarnings("unchecked")
            Class<? extends T> classRef = (Class<? extends T>) Class.forName(className);
            @Nullable
            T handler = classRef.getDeclaredConstructor(LegacyDeviceFeature.class).newInstance(feature);
            handler.setParameters(params);
            return handler;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
    }
}
