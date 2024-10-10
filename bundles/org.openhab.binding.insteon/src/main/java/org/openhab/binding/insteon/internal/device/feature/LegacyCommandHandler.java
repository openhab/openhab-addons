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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonLegacyChannelConfiguration;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.LegacyDevice;
import org.openhab.binding.insteon.internal.device.LegacyDeviceFeature;
import org.openhab.binding.insteon.internal.device.X10Address;
import org.openhab.binding.insteon.internal.device.X10Command;
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureListener.StateChangeType;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.ParameterParser;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command handler translates an openHAB command into an insteon message
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Bernd Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public abstract class LegacyCommandHandler {
    protected final Logger logger = LoggerFactory.getLogger(LegacyCommandHandler.class);

    LegacyDeviceFeature feature; // related DeviceFeature
    Map<String, String> parameters = new HashMap<>();

    /**
     * Constructor
     *
     * @param feature The DeviceFeature for which this command was intended.
     *            The openHAB commands are issued on an openhab item. The .items files bind
     *            an openHAB item to a DeviceFeature.
     */
    LegacyCommandHandler(LegacyDeviceFeature feature) {
        this.feature = feature;
    }

    /**
     * Implements what to do when an openHAB command is received
     *
     * @param conf the configuration for the item that generated the command
     * @param cmd the openhab command issued
     * @param device the Insteon device to which this command applies
     */
    public abstract void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device);

    /**
     * Returns parameter as integer
     *
     * @param key key of parameter
     * @param def default
     * @return value of parameter
     */
    protected int getIntParameter(String key, int def) {
        return ParameterParser.getParameterAsOrDefault(parameters.get(key), Integer.class, def);
    }

    /**
     * Returns parameter as String
     *
     * @param key key of parameter
     * @param def default
     * @return value of parameter
     */
    protected @Nullable String getStringParameter(String key, String def) {
        return parameters.get(key) == null ? def : parameters.get(key);
    }

    /**
     * Shorthand to return class name for logging purposes
     *
     * @return name of the class
     */
    protected String nm() {
        return this.getClass().getSimpleName();
    }

    protected int getMaxLightLevel(InsteonLegacyChannelConfiguration conf, int defaultLevel) {
        if (conf.getFeature().contains("dimmer")) {
            String dimmerMax = conf.getParameter("dimmermax");
            if (dimmerMax != null) {
                String item = conf.getChannelName();
                try {
                    int i = Integer.parseInt(dimmerMax);
                    if (i > 1 && i <= 99) {
                        int level = (int) Math.ceil((i * 255.0) / 100); // round up
                        if (level < defaultLevel) {
                            logger.debug("item {}: using dimmermax value of {}", item, dimmerMax);
                            return level;
                        }
                    } else {
                        logger.warn("item {}: dimmermax must be between 1-99 inclusive: {}", item, dimmerMax);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("item {}: invalid int value for dimmermax: {}", item, dimmerMax);
                }
            }
        }

        return defaultLevel;
    }

    void setParameters(Map<String, String> map) {
        parameters = map;
    }

    /**
     * Helper function to extract the group parameter from the binding config,
     *
     * @param config the binding configuration to test
     * @return the value of the "group" parameter, or -1 if none
     */
    protected static int getGroup(InsteonLegacyChannelConfiguration config) {
        return ParameterParser.getParameterAsOrDefault(config.getParameter("group"), Integer.class, -1);
    }

    public static class WarnCommandHandler extends LegacyCommandHandler {
        public WarnCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            logger.warn("{}: command {} is not implemented yet!", nm(), cmd);
        }
    }

    public static class NoOpCommandHandler extends LegacyCommandHandler {
        NoOpCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            // do nothing, not even log
        }
    }

    public static class LightOnOffCommandHandler extends LegacyCommandHandler {
        LightOnOffCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                int ext = getIntParameter("ext", 0);
                int direc = 0x00;
                int level = 0x00;
                Msg msg = null;
                if (cmd == OnOffType.ON) {
                    level = getMaxLightLevel(conf, 0xff);
                    direc = 0x11;
                    logger.debug("{}: sent msg to switch {} to {}", nm(), device.getAddress(),
                            level == 0xff ? "on" : level);
                } else if (cmd == OnOffType.OFF) {
                    direc = 0x13;
                    logger.debug("{}: sent msg to switch {} off", nm(), device.getAddress());
                }
                int group = getGroup(conf);
                if (group != -1) {
                    msg = Msg.makeBroadcastMessage(group, (byte) direc, (byte) level);
                } else if (ext == 0) {
                    msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) direc, (byte) level);
                } else {
                    byte[] data = new byte[] { (byte) getIntParameter("d1", 0), (byte) getIntParameter("d2", 0),
                            (byte) getIntParameter("d3", 0) };
                    msg = Msg.makeExtendedMessage((InsteonAddress) device.getAddress(), (byte) direc, (byte) level,
                            data, false);
                    logger.debug("{}: was an extended message for device {}", nm(), device.getAddress());
                    if (ext == 1) {
                        msg.setCRC();
                    } else if (ext == 2) {
                        msg.setCRC2();
                    }
                }
                logger.debug("Sending message to {}", device.getAddress());
                device.enqueueMessage(msg, feature);
                // expect to get a direct ack after this!
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class FastOnOffCommandHandler extends LegacyCommandHandler {
        FastOnOffCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                int cmd1 = cmd == OnOffType.ON ? 0x12 : 0x14;
                int level = cmd == OnOffType.ON ? getMaxLightLevel(conf, 0xff) : 0x00;
                int group = getGroup(conf);
                Msg msg;
                if (group != -1) {
                    msg = Msg.makeBroadcastMessage(group, (byte) cmd1, (byte) level);
                } else {
                    msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) cmd1, (byte) level);
                }
                device.enqueueMessage(msg, feature);
                logger.debug("{}: sent fast {} to switch {} level {}", nm(), cmd, device.getAddress(), level);
                // expect to get a direct ack after this!
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class RampOnOffCommandHandler extends RampCommandHandler {
        RampOnOffCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                byte cmd1 = cmd == OnOffType.ON ? getOnCmd() : getOffCmd();
                double ramptime = getRampTime(conf, 0);
                int ramplevel = getRampLevel(conf, 100);
                byte cmd2 = encode(ramptime, ramplevel);
                int group = getGroup(conf);
                Msg msg;
                if (group != -1) {
                    msg = Msg.makeBroadcastMessage(group, cmd1, cmd2);
                } else {
                    msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), cmd1, cmd2);
                }
                device.enqueueMessage(msg, feature);
                logger.debug("{}: sent ramp {} to switch {} time {} level {} cmd1 {}", nm(), cmd, device.getAddress(),
                        ramptime, ramplevel, cmd1);
                // expect to get a direct ack after this!
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        private int getRampLevel(InsteonLegacyChannelConfiguration conf, int defaultValue) {
            String str = conf.getParameter("ramplevel");
            return str != null ? Integer.parseInt(str) : defaultValue;
        }
    }

    public static class ManualChangeCommandHandler extends LegacyCommandHandler {
        ManualChangeCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                if (cmd instanceof DecimalType decimalCommand) {
                    int value = decimalCommand.intValue();
                    int cmd1 = (value != 1) ? 0x17 : 0x18; // start or stop
                    int cmd2 = (value == 2) ? 0x01 : 0; // up or down
                    int group = getGroup(conf);
                    Msg msg;
                    if (group != -1) {
                        msg = Msg.makeBroadcastMessage(group, (byte) cmd1, (byte) cmd2);
                    } else {
                        msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) cmd1, (byte) cmd2);
                    }
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: cmd {} sent manual change {} {} to {}", nm(), value,
                            (cmd1 == 0x17) ? "START" : "STOP", (cmd2 == 0x01) ? "UP" : "DOWN", device.getAddress());
                } else {
                    logger.warn("{}: invalid command type: {}", nm(), cmd);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    /**
     * Sends ALLLink broadcast commands to group
     */
    public static class GroupBroadcastCommandHandler extends LegacyCommandHandler {
        GroupBroadcastCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                if (cmd == OnOffType.ON || cmd == OnOffType.OFF) {
                    int cmd1 = cmd == OnOffType.ON ? 0x11 : 0x13;
                    int cmd2 = cmd == OnOffType.ON ? 0xFF : 0x00;
                    int group = getGroup(conf);
                    if (group == -1) {
                        logger.warn("no group=xx specified in item {}", conf.getChannelName());
                        return;
                    }
                    Msg msg = Msg.makeBroadcastMessage(group, (byte) cmd1, (byte) cmd2);
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: sent {} broadcast to group {}", nm(), cmd, group);
                    feature.pollRelatedDevices();
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class LEDOnOffCommandHandler extends LegacyCommandHandler {
        LEDOnOffCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                if (cmd == OnOffType.ON) {
                    Msg msg = Msg.makeExtendedMessage((InsteonAddress) device.getAddress(), (byte) 0x20, (byte) 0x09,
                            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00 }, true);
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: sent msg to switch {} on", nm(), device.getAddress());
                } else if (cmd == OnOffType.OFF) {
                    Msg msg = Msg.makeExtendedMessage((InsteonAddress) device.getAddress(), (byte) 0x20, (byte) 0x08,
                            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00 }, true);
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: sent msg to switch {} off", nm(), device.getAddress());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class X10OnOffCommandHandler extends LegacyCommandHandler {
        X10OnOffCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                X10Address address = (X10Address) device.getAddress();
                if (cmd == OnOffType.ON || cmd == OnOffType.OFF) {
                    byte cmdCode = (byte) (address.getHouseCode() << 4
                            | (cmd == OnOffType.ON ? X10Command.ON.code() : X10Command.OFF.code()));
                    Msg munit = Msg.makeX10AddressMessage(address); // send unit code
                    device.enqueueMessage(munit, feature);
                    Msg mcmd = Msg.makeX10CommandMessage(cmdCode); // send command code
                    device.enqueueMessage(mcmd, feature);
                    String onOff = cmd == OnOffType.ON ? "ON" : "OFF";
                    logger.debug("{}: sent msg to switch {} {}", nm(), address, onOff);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class X10PercentCommandHandler extends LegacyCommandHandler {
        X10PercentCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                X10Address address = (X10Address) device.getAddress();
                Msg munit = Msg.makeX10AddressMessage(address); // send unit code
                device.enqueueMessage(munit, feature);
                PercentType pc = (PercentType) cmd;
                logger.debug("{}: changing level of {} to {}", nm(), address, pc.intValue());
                int level = (pc.intValue() * 32) / 100;
                byte cmdCode = (level >= 16) ? X10Command.PRESET_DIM_2.code() : X10Command.PRESET_DIM_1.code();
                level = level % 16;
                if (level <= 0) {
                    level = 0;
                }
                byte levelCode = (byte) x10CodeForLevel[level];
                cmdCode |= (levelCode << 4);
                Msg mcmd = Msg.makeX10CommandMessage(cmdCode); // send command code
                device.enqueueMessage(mcmd, feature);
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        private final int[] x10CodeForLevel = { 0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15 };
    }

    public static class X10IncreaseDecreaseCommandHandler extends LegacyCommandHandler {
        X10IncreaseDecreaseCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                X10Address address = (X10Address) device.getAddress();
                if (cmd == IncreaseDecreaseType.INCREASE || cmd == IncreaseDecreaseType.DECREASE) {
                    byte cmdCode = (byte) (address.getHouseCode() << 4
                            | (cmd == IncreaseDecreaseType.INCREASE ? X10Command.BRIGHT.code()
                                    : X10Command.DIM.code()));
                    Msg munit = Msg.makeX10AddressMessage(address); // send unit code
                    device.enqueueMessage(munit, feature);
                    Msg mcmd = Msg.makeX10CommandMessage(cmdCode); // send command code
                    device.enqueueMessage(mcmd, feature);
                    String bd = cmd == IncreaseDecreaseType.INCREASE ? "BRIGHTEN" : "DIM";
                    logger.debug("{}: sent msg to switch {} {}", nm(), address, bd);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class IOLincOnOffCommandHandler extends LegacyCommandHandler {
        IOLincOnOffCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                if (cmd == OnOffType.ON) {
                    Msg msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) 0x11, (byte) 0xff);
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: sent msg to switch {} on", nm(), device.getAddress());
                } else if (cmd == OnOffType.OFF) {
                    Msg msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) 0x13, (byte) 0x00);
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: sent msg to switch {} off", nm(), device.getAddress());
                }
                // This used to be configurable, but was made static to make
                // the architecture of the binding cleaner.
                int delay = 2000;
                delay = Math.max(1000, delay);
                delay = Math.min(10000, delay);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Msg msg = feature.makePollMsg();
                        LegacyDevice device = feature.getDevice();
                        if (msg != null) {
                            device.enqueueMessage(msg, feature);
                        }
                    }
                }, delay);
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error: ", nm(), e);
            }
        }
    }

    public static class IncreaseDecreaseCommandHandler extends LegacyCommandHandler {
        IncreaseDecreaseCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                if (cmd == IncreaseDecreaseType.INCREASE) {
                    Msg msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) 0x15, (byte) 0x00);
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: sent msg to brighten {}", nm(), device.getAddress());
                } else if (cmd == IncreaseDecreaseType.DECREASE) {
                    Msg msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) 0x16, (byte) 0x00);
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: sent msg to dimm {}", nm(), device.getAddress());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class PercentHandler extends LegacyCommandHandler {
        PercentHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                PercentType percent = (PercentType) cmd;
                logger.debug("changing level of {} to {}", device.getAddress(), percent.intValue());
                int level = (int) Math.ceil((percent.intValue() * 255.0) / 100); // round up
                if (level > 0) { // make light on message with given level
                    level = getMaxLightLevel(conf, level);
                    Msg msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) 0x11, (byte) level);
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: sent msg to set {} to {}", nm(), device.getAddress(), level);
                } else { // switch off
                    Msg msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) 0x13, (byte) 0x00);
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: sent msg to set {} to zero by switching off", nm(), device.getAddress());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    private abstract static class RampCommandHandler extends LegacyCommandHandler {
        private static double[] halfRateRampTimes = new double[] { 0.1, 0.3, 2, 6.5, 19, 23.5, 28, 32, 38.5, 47, 90,
                150, 210, 270, 360, 480 };

        private byte onCmd;
        private byte offCmd;

        RampCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
            // Can't process parameters here because they are set after constructor is invoked.
            // Unfortunately, this means we can't declare the onCmd, offCmd to be final.
        }

        @Override
        void setParameters(Map<String, String> params) {
            super.setParameters(params);
            onCmd = (byte) getIntParameter("on", 0x2E);
            offCmd = (byte) getIntParameter("off", 0x2F);
        }

        protected final byte getOnCmd() {
            return onCmd;
        }

        protected final byte getOffCmd() {
            return offCmd;
        }

        protected byte encode(double ramptimeSeconds, int ramplevel) throws FieldException {
            if (ramplevel < 0 || ramplevel > 100) {
                throw new FieldException("ramplevel must be in the range 0-100 (inclusive)");
            }

            if (ramptimeSeconds < 0) {
                throw new FieldException("ramptime must be greater than 0");
            }

            int ramptime;
            int insertionPoint = Arrays.binarySearch(halfRateRampTimes, ramptimeSeconds);
            if (insertionPoint > 0) {
                ramptime = 15 - insertionPoint;
            } else {
                insertionPoint = -insertionPoint - 1;
                if (insertionPoint == 0) {
                    ramptime = 15;
                } else {
                    double d1 = Math.abs(halfRateRampTimes[insertionPoint - 1] - ramptimeSeconds);
                    double d2 = Math.abs(halfRateRampTimes[insertionPoint] - ramptimeSeconds);
                    ramptime = 15 - (d1 > d2 ? insertionPoint : insertionPoint - 1);
                    logger.debug("ramp encoding: time {} insert {} d1 {} d2 {} ramp {}", ramptimeSeconds,
                            insertionPoint, d1, d2, ramptime);
                }
            }

            int r = (int) Math.round(ramplevel / (100.0 / 15.0));
            return (byte) (((r & 0x0f) << 4) | (ramptime & 0xf));
        }

        protected double getRampTime(InsteonLegacyChannelConfiguration conf, double defaultValue) {
            String str = conf.getParameter("ramptime");
            return str != null ? Double.parseDouble(str) : defaultValue;
        }
    }

    public static class RampPercentHandler extends RampCommandHandler {

        RampPercentHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                PercentType percent = (PercentType) cmd;
                double ramptime = getRampTime(conf, 0);
                int level = percent.intValue();
                if (level > 0) { // make light on message with given level
                    level = getMaxLightLevel(conf, level);
                    byte cmd2 = encode(ramptime, level);
                    Msg msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), getOnCmd(), cmd2);
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: sent msg to set {} to {} with {} second ramp time.", nm(), device.getAddress(),
                            level, ramptime);
                } else { // switch off
                    Msg msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), getOffCmd(), (byte) 0x00);
                    device.enqueueMessage(msg, feature);
                    logger.debug("{}: sent msg to set {} to zero by switching off with {} ramp time.", nm(),
                            device.getAddress(), ramptime);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class PowerMeterCommandHandler extends LegacyCommandHandler {
        PowerMeterCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            String cmdParam = conf.getParameter(CMD);
            if (cmdParam == null) {
                logger.warn("{} ignoring cmd {} because no cmd= is configured!", nm(), cmd);
                return;
            }
            try {
                if (cmd == OnOffType.ON) {
                    if (cmdParam.equals(CMD_RESET)) {
                        Msg msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) 0x80,
                                (byte) 0x00);
                        device.enqueueMessage(msg, feature);
                        logger.debug("{}: sent reset msg to power meter {}", nm(), device.getAddress());
                        feature.publish(OnOffType.OFF, StateChangeType.ALWAYS, CMD, CMD_RESET);
                    } else if (cmdParam.equals(CMD_UPDATE)) {
                        Msg msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) 0x82,
                                (byte) 0x00);
                        device.enqueueMessage(msg, feature);
                        logger.debug("{}: sent update msg to power meter {}", nm(), device.getAddress());
                        feature.publish(OnOffType.OFF, StateChangeType.ALWAYS, CMD, CMD_UPDATE);
                    } else {
                        logger.warn("{}: ignoring unknown cmd {} for power meter {}", nm(), cmdParam,
                                device.getAddress());
                    }
                } else if (cmd == OnOffType.OFF) {
                    logger.debug("{}: ignoring off request for power meter {}", nm(), device.getAddress());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    /**
     * Command handler that sends a command with a numerical value to a device.
     * The handler is very parameterizable so it can be reused for different devices.
     * First used for setting thermostat parameters.
     */

    public static class NumberCommandHandler extends LegacyCommandHandler {
        NumberCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        public int transform(int cmd) {
            return cmd;
        }

        @Override
        public void handleCommand(InsteonLegacyChannelConfiguration conf, Command cmd, LegacyDevice device) {
            try {
                int value = transform(((DecimalType) cmd).intValue());
                int intFactor = getIntParameter("factor", 1);
                // determine what level should be, and what field it should be in
                int ilevel = value * intFactor;
                byte level = (byte) (ilevel > 255 ? 0xFF : ((ilevel < 0) ? 0 : ilevel));
                String vfield = getStringParameter("value", "");
                if (vfield == null || vfield.isEmpty()) {
                    logger.warn("{} has no value field specified", nm());
                    return;
                }
                // figure out what cmd1, cmd2, d1, d2, d3 are supposed to be
                // to form a proper message
                int cmd1 = getIntParameter("cmd1", -1);
                if (cmd1 < 0) {
                    logger.warn("{} has no cmd1 specified!", nm());
                    return;
                }
                int cmd2 = getIntParameter("cmd2", 0);
                int ext = getIntParameter("ext", 0);
                Msg msg = null;
                if (ext == 1 || ext == 2) {
                    byte[] data = new byte[] { (byte) getIntParameter("d1", 0), (byte) getIntParameter("d2", 0),
                            (byte) getIntParameter("d3", 0) };
                    msg = Msg.makeExtendedMessage((InsteonAddress) device.getAddress(), (byte) cmd1, (byte) cmd2, data,
                            false);
                    msg.setByte(vfield, level);
                    if (ext == 1) {
                        msg.setCRC();
                    } else if (ext == 2) {
                        msg.setCRC2();
                    }
                } else {
                    msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) cmd1, (byte) cmd2);
                    msg.setByte(vfield, level);
                }
                device.enqueueMessage(msg, feature);
                logger.debug("{}: sent msg to change level to {}", nm(), ((DecimalType) cmd).intValue());
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    /**
     * Handler to set the thermostat system mode
     */
    public static class ThermostatSystemModeCommandHandler extends NumberCommandHandler {
        ThermostatSystemModeCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public int transform(int cmd) {
            switch (cmd) {
                case 0:
                    return 0x09; // off
                case 1:
                    return 0x04; // heat
                case 2:
                    return 0x05; // cool
                case 3:
                    return 0x06; // auto (aka manual auto)
                case 4:
                    return 0x0A; // program (aka auto)
                default:
                    break;
            }
            return 0x0A; // when in doubt go to program
        }
    }

    /**
     * Handler to set the thermostat fan mode
     */
    public static class ThermostatFanModeCommandHandler extends NumberCommandHandler {
        ThermostatFanModeCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public int transform(int cmd) {
            switch (cmd) {
                case 0:
                    return 0x08; // fan mode auto
                case 1:
                    return 0x07; // fan always on
                default:
                    break;
            }
            return 0x08; // when in doubt go auto mode
        }
    }

    /**
     * Handler to set the fanlinc fan mode
     */
    public static class FanLincFanCommandHandler extends NumberCommandHandler {
        FanLincFanCommandHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public int transform(int cmd) {
            switch (cmd) {
                case 0:
                    return 0x00; // fan off
                case 1:
                    return 0x55; // fan low
                case 2:
                    return 0xAA; // fan medium
                case 3:
                    return 0xFF; // fan high
                default:
                    break;
            }
            return 0x00; // all other modes are "off"
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
    @Nullable
    public static <T extends LegacyCommandHandler> T makeHandler(String name, Map<String, String> params,
            LegacyDeviceFeature feature) {
        try {
            String className = LegacyCommandHandler.class.getName() + "$" + name;
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
