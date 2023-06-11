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
package org.openhab.binding.insteon.internal.device;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceFeatureListener.StateChangeType;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.utils.Utils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command handler translates an openHAB command into a insteon message
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Bernd Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public abstract class CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    DeviceFeature feature; // related DeviceFeature
    Map<String, String> parameters = new HashMap<>();

    /**
     * Constructor
     *
     * @param feature The DeviceFeature for which this command was intended.
     *            The openHAB commands are issued on an openhab item. The .items files bind
     *            an openHAB item to a DeviceFeature.
     */
    CommandHandler(DeviceFeature feature) {
        this.feature = feature;
    }

    /**
     * Implements what to do when an openHAB command is received
     *
     * @param conf the configuration for the item that generated the command
     * @param cmd the openhab command issued
     * @param device the Insteon device to which this command applies
     */
    public abstract void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice device);

    /**
     * Returns parameter as integer
     *
     * @param key key of parameter
     * @param def default
     * @return value of parameter
     */
    protected int getIntParameter(String key, int def) {
        String val = parameters.get(key);
        if (val == null) {
            return (def); // param not found
        }
        int ret = def;
        try {
            ret = Utils.strToInt(val);
        } catch (NumberFormatException e) {
            logger.warn("malformed int parameter in command handler: {}", key);
        }
        return ret;
    }

    /**
     * Returns parameter as String
     *
     * @param key key of parameter
     * @param def default
     * @return value of parameter
     */
    protected @Nullable String getStringParameter(String key, String def) {
        return (parameters.get(key) == null ? def : parameters.get(key));
    }

    /**
     * Shorthand to return class name for logging purposes
     *
     * @return name of the class
     */
    protected String nm() {
        return (this.getClass().getSimpleName());
    }

    protected int getMaxLightLevel(InsteonChannelConfiguration conf, int defaultLevel) {
        Map<String, String> params = conf.getParameters();
        if (conf.getFeature().contains("dimmer")) {
            String dimmerMax = params.get("dimmermax");
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
     * @param c the binding configuration to test
     * @return the value of the "group" parameter, or -1 if none
     */
    protected static int getGroup(InsteonChannelConfiguration c) {
        String v = c.getParameters().get("group");
        int iv = -1;
        try {
            iv = (v == null) ? -1 : Utils.strToInt(v);
        } catch (NumberFormatException e) {
            logger.warn("malformed int parameter in for item {}", c.getChannelName());
        }
        return iv;
    }

    public static class WarnCommandHandler extends CommandHandler {
        WarnCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            logger.warn("{}: command {} is not implemented yet!", nm(), cmd);
        }
    }

    public static class NoOpCommandHandler extends CommandHandler {
        NoOpCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            // do nothing, not even log
        }
    }

    public static class LightOnOffCommandHandler extends CommandHandler {
        LightOnOffCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                int ext = getIntParameter("ext", 0);
                int direc = 0x00;
                int level = 0x00;
                Msg m = null;
                if (cmd == OnOffType.ON) {
                    level = getMaxLightLevel(conf, 0xff);
                    direc = 0x11;
                    logger.debug("{}: sent msg to switch {} to {}", nm(), dev.getAddress(),
                            level == 0xff ? "on" : level);
                } else if (cmd == OnOffType.OFF) {
                    direc = 0x13;
                    logger.debug("{}: sent msg to switch {} off", nm(), dev.getAddress());
                }
                if (ext == 1 || ext == 2) {
                    byte[] data = new byte[] { (byte) getIntParameter("d1", 0), (byte) getIntParameter("d2", 0),
                            (byte) getIntParameter("d3", 0) };
                    m = dev.makeExtendedMessage((byte) 0x0f, (byte) direc, (byte) level, data);
                    logger.debug("{}: was an extended message for device {}", nm(), dev.getAddress());
                    if (ext == 1) {
                        m.setCRC();
                    } else if (ext == 2) {
                        m.setCRC2();
                    }
                } else {
                    m = dev.makeStandardMessage((byte) 0x0f, (byte) direc, (byte) level, getGroup(conf));
                }
                logger.debug("Sending message to {}", dev.getAddress());
                dev.enqueueMessage(m, feature);
                // expect to get a direct ack after this!
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class FastOnOffCommandHandler extends CommandHandler {
        FastOnOffCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                if (cmd == OnOffType.ON) {
                    int level = getMaxLightLevel(conf, 0xff);
                    Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x12, (byte) level, getGroup(conf));
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent fast on to switch {} level {}", nm(), dev.getAddress(),
                            level == 0xff ? "on" : level);
                } else if (cmd == OnOffType.OFF) {
                    Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x14, (byte) 0x00, getGroup(conf));
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent fast off to switch {}", nm(), dev.getAddress());
                }
                // expect to get a direct ack after this!
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class RampOnOffCommandHandler extends RampCommandHandler {
        RampOnOffCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                if (cmd == OnOffType.ON) {
                    double ramptime = getRampTime(conf, 0);
                    int ramplevel = getRampLevel(conf, 100);
                    byte cmd2 = encode(ramptime, ramplevel);
                    Msg m = dev.makeStandardMessage((byte) 0x0f, getOnCmd(), cmd2, getGroup(conf));
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent ramp on to switch {} time {} level {} cmd1 {}", nm(), dev.getAddress(),
                            ramptime, ramplevel, getOnCmd());
                } else if (cmd == OnOffType.OFF) {
                    double ramptime = getRampTime(conf, 0);
                    int ramplevel = getRampLevel(conf, 0 /* ignored */);
                    byte cmd2 = encode(ramptime, ramplevel);
                    Msg m = dev.makeStandardMessage((byte) 0x0f, getOffCmd(), cmd2, getGroup(conf));
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent ramp off to switch {} time {} cmd1 {}", nm(), dev.getAddress(), ramptime,
                            getOffCmd());
                }
                // expect to get a direct ack after this!
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        private int getRampLevel(InsteonChannelConfiguration conf, int defaultValue) {
            String str = conf.getParameters().get("ramplevel");
            return str != null ? Integer.parseInt(str) : defaultValue;
        }
    }

    public static class ManualChangeCommandHandler extends CommandHandler {
        ManualChangeCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                if (cmd instanceof DecimalType) {
                    int v = ((DecimalType) cmd).intValue();
                    int cmd1 = (v != 1) ? 0x17 : 0x18; // start or stop
                    int cmd2 = (v == 2) ? 0x01 : 0; // up or down
                    Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) cmd1, (byte) cmd2, getGroup(conf));
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: cmd {} sent manual change {} {} to {}", nm(), v,
                            (cmd1 == 0x17) ? "START" : "STOP", (cmd2 == 0x01) ? "UP" : "DOWN", dev.getAddress());
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
    public static class GroupBroadcastCommandHandler extends CommandHandler {
        GroupBroadcastCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                if (cmd == OnOffType.ON || cmd == OnOffType.OFF) {
                    byte cmd1 = (byte) ((cmd == OnOffType.ON) ? 0x11 : 0x13);
                    byte value = (byte) ((cmd == OnOffType.ON) ? 0xFF : 0x00);
                    int group = getGroup(conf);
                    if (group == -1) {
                        logger.warn("no group=xx specified in item {}", conf.getChannelName());
                        return;
                    }
                    logger.debug("{}: sending {} broadcast to group {}", nm(), (cmd1 == 0x11) ? "ON" : "OFF",
                            getGroup(conf));
                    Msg m = dev.makeStandardMessage((byte) 0x0f, cmd1, value, group);
                    dev.enqueueMessage(m, feature);
                    feature.pollRelatedDevices();
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class LEDOnOffCommandHandler extends CommandHandler {
        LEDOnOffCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                if (cmd == OnOffType.ON) {
                    Msg m = dev.makeExtendedMessage((byte) 0x1f, (byte) 0x20, (byte) 0x09,
                            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00 });
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent msg to switch {} on", nm(), dev.getAddress());
                } else if (cmd == OnOffType.OFF) {
                    Msg m = dev.makeExtendedMessage((byte) 0x1f, (byte) 0x20, (byte) 0x08,
                            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00 });
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent msg to switch {} off", nm(), dev.getAddress());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class X10OnOffCommandHandler extends CommandHandler {
        X10OnOffCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                byte houseCode = dev.getX10HouseCode();
                byte houseUnitCode = (byte) (houseCode << 4 | dev.getX10UnitCode());
                if (cmd == OnOffType.ON || cmd == OnOffType.OFF) {
                    byte houseCommandCode = (byte) (houseCode << 4
                            | (cmd == OnOffType.ON ? X10.Command.ON.code() : X10.Command.OFF.code()));
                    Msg munit = dev.makeX10Message(houseUnitCode, (byte) 0x00); // send unit code
                    dev.enqueueMessage(munit, feature);
                    Msg mcmd = dev.makeX10Message(houseCommandCode, (byte) 0x80); // send command code
                    dev.enqueueMessage(mcmd, feature);
                    String onOff = cmd == OnOffType.ON ? "ON" : "OFF";
                    logger.debug("{}: sent msg to switch {} {}", nm(), dev.getAddress(), onOff);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class X10PercentCommandHandler extends CommandHandler {
        X10PercentCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                //
                // I did not have hardware that would respond to the PRESET_DIM codes.
                // This code path needs testing.
                //
                byte houseCode = dev.getX10HouseCode();
                byte houseUnitCode = (byte) (houseCode << 4 | dev.getX10UnitCode());
                Msg munit = dev.makeX10Message(houseUnitCode, (byte) 0x00); // send unit code
                dev.enqueueMessage(munit, feature);
                PercentType pc = (PercentType) cmd;
                logger.debug("{}: changing level of {} to {}", nm(), dev.getAddress(), pc.intValue());
                int level = (pc.intValue() * 32) / 100;
                byte cmdCode = (level >= 16) ? X10.Command.PRESET_DIM_2.code() : X10.Command.PRESET_DIM_1.code();
                level = level % 16;
                if (level <= 0) {
                    level = 0;
                }
                houseCode = (byte) x10CodeForLevel[level];
                cmdCode |= (houseCode << 4);
                Msg mcmd = dev.makeX10Message(cmdCode, (byte) 0x80); // send command code
                dev.enqueueMessage(mcmd, feature);
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        private final int[] x10CodeForLevel = { 0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15 };
    }

    public static class X10IncreaseDecreaseCommandHandler extends CommandHandler {
        X10IncreaseDecreaseCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                byte houseCode = dev.getX10HouseCode();
                byte houseUnitCode = (byte) (houseCode << 4 | dev.getX10UnitCode());
                if (cmd == IncreaseDecreaseType.INCREASE || cmd == IncreaseDecreaseType.DECREASE) {
                    byte houseCommandCode = (byte) (houseCode << 4
                            | (cmd == IncreaseDecreaseType.INCREASE ? X10.Command.BRIGHT.code()
                                    : X10.Command.DIM.code()));
                    Msg munit = dev.makeX10Message(houseUnitCode, (byte) 0x00); // send unit code
                    dev.enqueueMessage(munit, feature);
                    Msg mcmd = dev.makeX10Message(houseCommandCode, (byte) 0x80); // send command code
                    dev.enqueueMessage(mcmd, feature);
                    String bd = cmd == IncreaseDecreaseType.INCREASE ? "BRIGHTEN" : "DIM";
                    logger.debug("{}: sent msg to switch {} {}", nm(), dev.getAddress(), bd);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class IOLincOnOffCommandHandler extends CommandHandler {
        IOLincOnOffCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                if (cmd == OnOffType.ON) {
                    Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x11, (byte) 0xff);
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent msg to switch {} on", nm(), dev.getAddress());
                } else if (cmd == OnOffType.OFF) {
                    Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x13, (byte) 0x00);
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent msg to switch {} off", nm(), dev.getAddress());
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
                        Msg m = feature.makePollMsg();
                        InsteonDevice dev = feature.getDevice();
                        if (m != null) {
                            dev.enqueueMessage(m, feature);
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

    public static class IncreaseDecreaseCommandHandler extends CommandHandler {
        IncreaseDecreaseCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                if (cmd == IncreaseDecreaseType.INCREASE) {
                    Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x15, (byte) 0x00);
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent msg to brighten {}", nm(), dev.getAddress());
                } else if (cmd == IncreaseDecreaseType.DECREASE) {
                    Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x16, (byte) 0x00);
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent msg to dimm {}", nm(), dev.getAddress());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class PercentHandler extends CommandHandler {
        PercentHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                PercentType pc = (PercentType) cmd;
                logger.debug("changing level of {} to {}", dev.getAddress(), pc.intValue());
                int level = (int) Math.ceil((pc.intValue() * 255.0) / 100); // round up
                if (level > 0) { // make light on message with given level
                    level = getMaxLightLevel(conf, level);
                    Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x11, (byte) level);
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent msg to set {} to {}", nm(), dev.getAddress(), level);
                } else { // switch off
                    Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x13, (byte) 0x00);
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent msg to set {} to zero by switching off", nm(), dev.getAddress());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    private abstract static class RampCommandHandler extends CommandHandler {
        private static double[] halfRateRampTimes = new double[] { 0.1, 0.3, 2, 6.5, 19, 23.5, 28, 32, 38.5, 47, 90,
                150, 210, 270, 360, 480 };

        private byte onCmd;
        private byte offCmd;

        RampCommandHandler(DeviceFeature f) {
            super(f);
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

        protected double getRampTime(InsteonChannelConfiguration conf, double defaultValue) {
            String str = conf.getParameters().get("ramptime");
            return str != null ? Double.parseDouble(str) : defaultValue;
        }
    }

    public static class RampPercentHandler extends RampCommandHandler {

        RampPercentHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                PercentType pc = (PercentType) cmd;
                double ramptime = getRampTime(conf, 0);
                int level = pc.intValue();
                if (level > 0) { // make light on message with given level
                    level = getMaxLightLevel(conf, level);
                    byte cmd2 = encode(ramptime, level);
                    Msg m = dev.makeStandardMessage((byte) 0x0f, getOnCmd(), cmd2);
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent msg to set {} to {} with {} second ramp time.", nm(), dev.getAddress(),
                            level, ramptime);
                } else { // switch off
                    Msg m = dev.makeStandardMessage((byte) 0x0f, getOffCmd(), (byte) 0x00);
                    dev.enqueueMessage(m, feature);
                    logger.debug("{}: sent msg to set {} to zero by switching off with {} ramp time.", nm(),
                            dev.getAddress(), ramptime);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    public static class PowerMeterCommandHandler extends CommandHandler {
        PowerMeterCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            String cmdParam = conf.getParameters().get(InsteonDeviceHandler.CMD);
            if (cmdParam == null) {
                logger.warn("{} ignoring cmd {} because no cmd= is configured!", nm(), cmd);
                return;
            }
            try {
                if (cmd == OnOffType.ON) {
                    if (cmdParam.equals(InsteonDeviceHandler.CMD_RESET)) {
                        Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x80, (byte) 0x00);
                        dev.enqueueMessage(m, feature);
                        logger.debug("{}: sent reset msg to power meter {}", nm(), dev.getAddress());
                        feature.publish(OnOffType.OFF, StateChangeType.ALWAYS, InsteonDeviceHandler.CMD,
                                InsteonDeviceHandler.CMD_RESET);
                    } else if (cmdParam.equals(InsteonDeviceHandler.CMD_UPDATE)) {
                        Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x82, (byte) 0x00);
                        dev.enqueueMessage(m, feature);
                        logger.debug("{}: sent update msg to power meter {}", nm(), dev.getAddress());
                        feature.publish(OnOffType.OFF, StateChangeType.ALWAYS, InsteonDeviceHandler.CMD,
                                InsteonDeviceHandler.CMD_UPDATE);
                    } else {
                        logger.warn("{}: ignoring unknown cmd {} for power meter {}", nm(), cmdParam, dev.getAddress());
                    }
                } else if (cmd == OnOffType.OFF) {
                    logger.debug("{}: ignoring off request for power meter {}", nm(), dev.getAddress());
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

    public static class NumberCommandHandler extends CommandHandler {
        NumberCommandHandler(DeviceFeature f) {
            super(f);
        }

        public int transform(int cmd) {
            return (cmd);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration conf, Command cmd, InsteonDevice dev) {
            try {
                int dc = transform(((DecimalType) cmd).intValue());
                int intFactor = getIntParameter("factor", 1);
                //
                // determine what level should be, and what field it should be in
                //
                int ilevel = dc * intFactor;
                byte level = (byte) (ilevel > 255 ? 0xFF : ((ilevel < 0) ? 0 : ilevel));
                String vfield = getStringParameter("value", "");
                if (vfield == null || vfield.isEmpty()) {
                    logger.warn("{} has no value field specified", nm());
                }
                //
                // figure out what cmd1, cmd2, d1, d2, d3 are supposed to be
                // to form a proper message
                //
                int cmd1 = getIntParameter("cmd1", -1);
                if (cmd1 < 0) {
                    logger.warn("{} has no cmd1 specified!", nm());
                    return;
                }
                int cmd2 = getIntParameter("cmd2", 0);
                int ext = getIntParameter("ext", 0);
                Msg m = null;
                if (ext == 1 || ext == 2) {
                    byte[] data = new byte[] { (byte) getIntParameter("d1", 0), (byte) getIntParameter("d2", 0),
                            (byte) getIntParameter("d3", 0) };
                    m = dev.makeExtendedMessage((byte) 0x0f, (byte) cmd1, (byte) cmd2, data);
                    m.setByte(vfield, level);
                    if (ext == 1) {
                        m.setCRC();
                    } else if (ext == 2) {
                        m.setCRC2();
                    }
                } else {
                    m = dev.makeStandardMessage((byte) 0x0f, (byte) cmd1, (byte) cmd2);
                    m.setByte(vfield, level);
                }
                dev.enqueueMessage(m, feature);
                logger.debug("{}: sent msg to change level to {}", nm(), ((DecimalType) cmd).intValue());
                m = null;
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
        ThermostatSystemModeCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public int transform(int cmd) {
            switch (cmd) {
                case 0:
                    return (0x09); // off
                case 1:
                    return (0x04); // heat
                case 2:
                    return (0x05); // cool
                case 3:
                    return (0x06); // auto (aka manual auto)
                case 4:
                    return (0x0A); // program (aka auto)
                default:
                    break;
            }
            return (0x0A); // when in doubt go to program
        }
    }

    /**
     * Handler to set the thermostat fan mode
     */
    public static class ThermostatFanModeCommandHandler extends NumberCommandHandler {
        ThermostatFanModeCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public int transform(int cmd) {
            switch (cmd) {
                case 0:
                    return (0x08); // fan mode auto
                case 1:
                    return (0x07); // fan always on
                default:
                    break;
            }
            return (0x08); // when in doubt go auto mode
        }
    }

    /**
     * Handler to set the fanlinc fan mode
     */
    public static class FanLincFanCommandHandler extends NumberCommandHandler {
        FanLincFanCommandHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public int transform(int cmd) {
            switch (cmd) {
                case 0:
                    return (0x00); // fan off
                case 1:
                    return (0x55); // fan low
                case 2:
                    return (0xAA); // fan medium
                case 3:
                    return (0xFF); // fan high
                default:
                    break;
            }
            return (0x00); // all other modes are "off"
        }
    }

    /**
     * Factory method for creating handlers of a given name using java reflection
     *
     * @param name the name of the handler to create
     * @param params
     * @param f the feature for which to create the handler
     * @return the handler which was created
     */
    @Nullable
    public static <T extends CommandHandler> T makeHandler(String name, Map<String, String> params, DeviceFeature f) {
        String cname = CommandHandler.class.getName() + "$" + name;
        try {
            Class<?> c = Class.forName(cname);
            @SuppressWarnings("unchecked")
            Class<? extends T> dc = (Class<? extends T>) c;
            @Nullable
            T ch = dc.getDeclaredConstructor(DeviceFeature.class).newInstance(f);
            ch.setParameters(params);
            return ch;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            logger.warn("error trying to create message handler: {}", name, e);
        }
        return null;
    }
}
