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
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.DeviceFeature.StateChangeType;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.device.RampRate;
import org.openhab.binding.insteon.internal.device.X10;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.utils.BitwiseUtils;
import org.openhab.binding.insteon.internal.utils.ByteUtils;
import org.openhab.binding.insteon.internal.utils.StringUtils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command handler translates an openHAB command into a insteon message
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Bernd Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public abstract class CommandHandler extends FeatureBaseHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    /**
     * Constructor
     *
     * @param f The DeviceFeature for which this command was intended.
     *            The openHAB commands are issued on an openhab item. The .items files bind
     *            an openHAB item to a DeviceFeature.
     */
    public CommandHandler(DeviceFeature feature) {
        super(feature);
    }

    /**
     * Returns if handler can handle the openHAB command received
     *
     * @param cmd the openhab command received
     * @return true if can handle
     */
    public abstract boolean canHandle(Command cmd);

    /**
     * Implements what to do when an openHAB command is received
     *
     * @param channelUID the channel uid that generated the command
     * @param config the channel configuration that generated the command
     * @param cmd the openhab command to handle
     */
    public abstract void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config,
            Command cmd);

    //
    //
    // ---------------- the various command handlers start here -------------------
    //
    //

    /**
     * Default command handler
     */
    public static class DefaultCommandHandler extends CommandHandler {
        DefaultCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return true;
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            logger.warn("{}: command {}:{} is not supported", nm(), cmd.getClass().getSimpleName(), cmd);
        }
    }

    /**
     * No-op command handler
     */
    public static class NoOpCommandHandler extends CommandHandler {
        NoOpCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return true;
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            // do nothing, not even log
        }
    }

    /**
     * Refresh command handler
     */
    public static class RefreshCommandHandler extends CommandHandler {
        RefreshCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof RefreshType;
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            feature.triggerPoll(0L);
        }
    }

    /**
     * Custom abstract command handler based of parameters
     */
    public abstract static class CustomCommandHandler extends CommandHandler {
        CustomCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            int cmd1 = getParameterAsInteger("cmd1", -1);
            int cmd2 = getParameterAsInteger("cmd2", 0);
            int ext = getParameterAsInteger("ext", 0);
            if (cmd1 == -1) {
                logger.warn("{}: handler misconfigured, no cmd1 parameter specified", nm());
                return;
            }
            if (ext < 0 || ext > 2) {
                logger.warn("{}: handler misconfigured, invalid ext parameter specified", nm());
                return;
            }
            // determine data field based on parameter, default to cmd2 if is standard message
            String field = getParameterAsString("field", ext == 0 ? "command2" : "");
            if (field.isEmpty()) {
                logger.warn("{}: handler misconfigured, no field parameter specified", nm());
                return;
            }
            // determine cmd value and apply factor ratio based of parameters
            int value = (int) Math.round(getValue(cmd) * getParameterAsInteger("factor", 1));
            if (value == -1) {
                logger.debug("{}: unable to determine command value, ignoring request", nm());
                return;
            }
            try {
                InsteonAddress address = getDevice().getAddress();
                Msg msg;
                if (ext == 0) {
                    msg = Msg.makeStandardMessage(address, (byte) cmd1, (byte) cmd2);
                } else {
                    // set userData1 to d1 parameter if defined, fallback to group parameter
                    byte[] data = { (byte) getParameterAsInteger("d1", getParameterAsInteger("group", 0)),
                            (byte) getParameterAsInteger("d2", 0), (byte) getParameterAsInteger("d3", 0) };
                    boolean setCRC = getDevice().getInsteonEngine().supportsChecksum();
                    msg = Msg.makeExtendedMessage(address, (byte) cmd1, (byte) cmd2, data, setCRC);
                }
                // set field to clamped byte-size value
                msg.setByte(field, (byte) Math.min(value, 0xFF));
                // set crc based on message type
                if (ext == 1) {
                    msg.setCRC();
                } else if (ext == 2) {
                    msg.setCRC2();
                }
                // send request
                feature.sendRequest(msg);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: sent {} {} request to {}", nm(), feature.getName(), ByteUtils.getHexString(value),
                            address);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        protected abstract double getValue(Command cmd);
    }

    /**
     * Custom bitmask command handler based of parameters
     */
    public static class CustomBitmaskCommandHandler extends CustomCommandHandler {
        CustomBitmaskCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof OnOffType;
        }

        @Override
        protected double getValue(Command cmd) {
            return getBitmask(cmd);
        }

        protected int getBitNumber() {
            return getParameterAsInteger("bit", -1);
        }

        protected boolean shouldSetBit(Command cmd) {
            return OnOffType.ON.equals(cmd) ^ getParameterAsBoolean("inverted", false);
        }

        protected int getBitmask(Command cmd) {
            // get bit number based on parameter
            int bit = getBitNumber();
            // get last bitmask message value received by this feature
            int bitmask = feature.getLastMsgValueAsInteger(-1);
            // update last bitmask value specific bit based on cmd state, if defined and bit number valid
            if (bit < 0 || bit > 7) {
                logger.warn("{}: incorrect bit number {} for feature {}", nm(), bit, feature.getName());
            } else if (bitmask == -1) {
                logger.debug("{}: unable to determine last bit mask for feature {}", nm(), feature.getName());
            } else {
                boolean shouldSetBit = shouldSetBit(cmd);
                if (logger.isTraceEnabled()) {
                    logger.trace("{}: bitmask:{} bit:{} set:{}", nm(), ByteUtils.getBinaryString(bitmask), bit,
                            shouldSetBit);
                }
                return shouldSetBit ? BitwiseUtils.setBitFlag(bitmask, bit) : BitwiseUtils.clearBitFlag(bitmask, bit);
            }
            return -1;
        }
    }

    /**
     * Custom on/off type command handler based of parameters
     */
    public static class CustomOnOffCommandHandler extends CustomCommandHandler {
        CustomOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof OnOffType;
        }

        @Override
        protected double getValue(Command cmd) {
            return OnOffType.OFF.equals(cmd) ? getParameterAsInteger("off", 0x00) : getParameterAsInteger("on", 0xFF);
        }
    }

    /**
     * Custom decimal type command handler based of parameters
     */
    public static class CustomDecimalCommandHandler extends CustomCommandHandler {
        CustomDecimalCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof DecimalType;
        }

        @Override
        protected double getValue(Command cmd) {
            return ((DecimalType) cmd).doubleValue();
        }
    }

    /**
     * Custom percent type command handler based of parameters
     */
    public static class CustomPercentCommandHandler extends CustomCommandHandler {
        CustomPercentCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof PercentType;
        }

        @Override
        protected double getValue(Command cmd) {
            int minValue = getParameterAsInteger("min", 0x00);
            int maxValue = getParameterAsInteger("max", 0xFF);
            double value = ((PercentType) cmd).doubleValue();
            return Math.round(value * (maxValue - minValue) / 100.0) + minValue;
        }
    }

    /**
     * Custom dimensionless quantity type command handler based of parameters
     */
    public static class CustomDimensionlessCommandHandler extends CustomCommandHandler {
        CustomDimensionlessCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof QuantityType;
        }

        @Override
        protected double getValue(Command cmd) {
            int minValue = getParameterAsInteger("min", 0);
            int maxValue = getParameterAsInteger("max", 100);
            @SuppressWarnings("unchecked")
            double value = ((QuantityType<Dimensionless>) cmd).doubleValue();
            return Math.round(value * (maxValue - minValue) / 100.0) + minValue;
        }
    }

    /**
     * Custom temperature quantity type command handler based of parameters
     */
    public static class CustomTemperatureCommandHandler extends CustomCommandHandler {
        CustomTemperatureCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof QuantityType;
        }

        @Override
        protected double getValue(Command cmd) {
            @SuppressWarnings("unchecked")
            QuantityType<Temperature> temperature = (QuantityType<Temperature>) cmd;
            Unit<Temperature> unit = getTemperatureUnit();
            QuantityType<Temperature> convertedTemp = temperature.toUnit(unit);
            double value = (convertedTemp != null ? convertedTemp : temperature).doubleValue();
            double increment = SIUnits.CELSIUS.equals(unit) ? 0.5 : 1;
            return Math.round(value / increment) * increment; // round in increment based on temperature unit
        }

        private Unit<Temperature> getTemperatureUnit() {
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
     * Custom time quantity type command handler based of parameters
     */
    public static class CustomTimeCommandHandler extends CustomCommandHandler {
        CustomTimeCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof QuantityType;
        }

        @Override
        protected double getValue(Command cmd) {
            @SuppressWarnings("unchecked")
            QuantityType<Time> time = (QuantityType<Time>) cmd;
            Unit<Time> unit = getTimeUnit();
            QuantityType<Time> convertedTime = time.toUnit(unit);
            return (convertedTime != null ? convertedTime : time).doubleValue();
        }

        private Unit<Time> getTimeUnit() {
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
     * Generic on/off abstract command handler
     */
    public abstract static class OnOffCommandHandler extends CommandHandler {
        OnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof OnOffType;
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                int cmd1 = getCommandCode(config, cmd);
                int level = getLevel(config, cmd);
                int group = getGroup(config);
                if (group != -1) {
                    Msg msg = Msg.makeBroadcastMessage(group, (byte) cmd1, (byte) level);
                    feature.sendRequest(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: sent broadcast {} request to group {}", nm(), cmd, group);
                    }
                    long delay = getPollDelay(config);
                    feature.triggerPoll(delay);
                    if (channelUID != null) {
                        feature.pollRelatedDevices(channelUID, delay);
                    }
                } else {
                    InsteonAddress address = getDevice().getAddress();
                    int componentId = getParameterAsInteger("group", 1);
                    Msg msg;
                    if (componentId > 1) {
                        byte[] data = { (byte) componentId };
                        boolean setCRC = getDevice().getInsteonEngine().supportsChecksum();
                        msg = Msg.makeExtendedMessage(address, (byte) cmd1, (byte) level, data, setCRC);
                    } else {
                        msg = Msg.makeStandardMessage(address, (byte) cmd1, (byte) level);
                    }
                    feature.sendRequest(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: sent {} request to {}", nm(), cmd, address);
                    }
                    if (channelUID != null && getDevice().isDeviceSyncEnabled()) {
                        feature.adjustRelatedDevices(channelUID, cmd);
                    }
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.OFF.equals(cmd) ? getParameterAsInteger("off", 0x13) : getParameterAsInteger("on", 0x11);
        }

        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.OFF.equals(cmd) ? 0x00 : getOnLevel(config);
        }

        protected int getGroup(InsteonChannelConfiguration config) {
            return -1;
        }

        protected long getPollDelay(InsteonChannelConfiguration config) {
            return 500L;
        }

        protected int getOnLevel(InsteonChannelConfiguration config) {
            int level = config.getOnLevel();
            if (level == -1) {
                State state = getDevice().getState(FEATURE_ON_LEVEL);
                PercentType percent = state instanceof PercentType ? (PercentType) state : PercentType.HUNDRED;
                level = percent.intValue();
            }
            if (logger.isTraceEnabled()) {
                logger.trace("{}: using on level {}%", nm(), level);
            }
            return (int) Math.ceil(level * 255.0 / 100); // round up
        }
    }

    /**
     * Dimmer abstract command handler
     */
    public abstract static class DimmerCommandHandler extends OnOffCommandHandler {
        DimmerCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            RampRate rampRate = config.getRampRate();
            if (rampRate == null) {
                // standard command if ramp rate parameter not configured
                super.handleCommand(channelUID, config, cmd);
            } else if (rampRate == RampRate.INSTANT) {
                // instant dimmer command if ramp rate parameter is instant (0.1 sec)
                setInstantDimmer(channelUID, config, cmd);
            } else {
                // ramp dimmer command otherwise
                setRampDimmer(channelUID, config, cmd);
            }
        }

        @Override
        protected long getPollDelay(InsteonChannelConfiguration config) {
            RampRate rampRate = config.getRampRate();
            if (rampRate == null) {
                State state = getDevice().getState(FEATURE_RAMP_RATE);
                if (state instanceof QuantityType) {
                    @SuppressWarnings("unchecked")
                    QuantityType<Time> rampTime = (QuantityType<Time>) state;
                    QuantityType<Time> convertedTime = rampTime.toUnit(Units.SECOND);
                    rampRate = RampRate.fromTime((convertedTime != null ? convertedTime : rampTime).doubleValue());
                } else {
                    rampRate = RampRate.DEFAULT;
                }
            }
            return rampRate.getTimeInMilliseconds();
        }

        private void setInstantDimmer(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config,
                Command cmd) {
            InstantDimmerCommandHandler handler = new InstantDimmerCommandHandler(feature);
            handler.setParameters(parameters);
            handler.handleCommand(channelUID, config, cmd);
        }

        private void setRampDimmer(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            RampDimmerCommandHandler handler = new RampDimmerCommandHandler(feature);
            handler.setParameters(parameters);
            handler.handleCommand(channelUID, config, cmd);
        }
    }

    /**
     * Dimmer on/off command handler
     */
    public static class DimmerOnOffCommandHandler extends DimmerCommandHandler {
        DimmerOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.OFF.equals(cmd) ? 0x13 : 0x11;
        }

        @Override
        protected int getGroup(InsteonChannelConfiguration config) {
            return config.getOnLevel() == -1 && getDevice().isDeviceSyncEnabled() ? config.getGroup() : -1;
        }
    }

    /**
     * Dimmer percent command handler
     */
    public static class DimmerPercentCommandHandler extends DimmerCommandHandler {
        DimmerPercentCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof PercentType;
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return PercentType.ZERO.equals(cmd) ? 0x13 : 0x11;
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            int level = ((PercentType) cmd).intValue();
            return (int) Math.ceil(level * 255.0 / 100); // round up
        }
    }

    /**
     * Dimmer increase/decrease command handler
     */
    public static class DimmerIncreaseDecreaseCommandHandler extends OnOffCommandHandler {
        DimmerIncreaseDecreaseCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof IncreaseDecreaseType;
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return IncreaseDecreaseType.INCREASE.equals(cmd) ? 0x15 : 0x16;
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return 0x00; // not parsed
        }

        @Override
        protected int getGroup(InsteonChannelConfiguration config) {
            return getDevice().isDeviceSyncEnabled() ? config.getGroup() : -1;
        }
    }

    /**
     * Rollershutter up/down command handler
     */
    public static class RollershutterUpDownCommandHandler extends OnOffCommandHandler {
        RollershutterUpDownCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof UpDownType;
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return 0x17; // manual change start
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return UpDownType.UP.equals(cmd) ? 0x01 : 0x00; // up or down
        }

        @Override
        protected int getGroup(InsteonChannelConfiguration config) {
            return getDevice().isDeviceSyncEnabled() ? config.getGroup() : -1;
        }
    }

    /**
     * Rollershutter stop command handler
     */
    public static class RollershutterStopCommandHandler extends OnOffCommandHandler {
        RollershutterStopCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return StopMoveType.STOP.equals(cmd);
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return 0x18; // manual change stop
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return 0x00; // not parsed
        }

        @Override
        protected int getGroup(InsteonChannelConfiguration config) {
            return getDevice().isDeviceSyncEnabled() ? config.getGroup() : -1;
        }
    }

    /**
     * Instant dimmer command handler
     */
    public static class InstantDimmerCommandHandler extends OnOffCommandHandler {
        InstantDimmerCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof OnOffType || cmd instanceof PercentType;
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return 0x21;
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            if (cmd instanceof OnOffType) {
                return super.getLevel(config, cmd);
            } else {
                int level = ((PercentType) cmd).intValue();
                return (int) Math.ceil(level * 255.0 / 100); // round up
            }
        }
    }

    /**
     * Ramp dimmer command handler
     */
    public static class RampDimmerCommandHandler extends InstantDimmerCommandHandler {
        RampDimmerCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                int level = getLevel(config, cmd);
                RampRate rampRate = getRampRate(config);
                int cmd1 = getCommandCode(level);
                int cmd2 = getEncodedValue(level, rampRate.getValue());
                Msg msg = Msg.makeStandardMessage(getDevice().getAddress(), (byte) cmd1, (byte) cmd2);
                feature.sendRequest(msg);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: sent level {} with ramp time {}s to {}", nm(), cmd, rampRate.getTimeInSeconds(),
                            getDevice().getAddress());
                }
                if (channelUID != null && getDevice().isDeviceSyncEnabled()) {
                    feature.adjustRelatedDevices(channelUID, cmd);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        private RampRate getRampRate(InsteonChannelConfiguration config) {
            RampRate rampRate = config.getRampRate();
            return rampRate == null ? RampRate.DEFAULT : rampRate;
        }

        private int getCommandCode(int level) {
            ProductData productData = getDevice().getProductData();
            // newer device with firmware >= 0x44 supports commands 0x34/0x35, while older supports 0x2E/0x2F
            if (productData != null && productData.getFirmwareVersion() >= 0x44) {
                return level > 0 ? 0x34 : 0x35;
            } else {
                return level > 0 ? 0x2E : 0x2F;
            }
        }

        private int getEncodedValue(int level, int rampRate) {
            int highByte = (int) Math.round(Math.max(0, level - 0x0F) / 16.0);
            int lowByte = (int) Math.round(Math.max(0, rampRate - 0x01) / 2.0);
            return highByte << 4 | lowByte;
        }
    }

    /**
     * Switch on/off command handler
     */
    public static class SwitchOnOffCommandHandler extends OnOffCommandHandler {
        SwitchOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.OFF.equals(cmd) ? 0x13 : 0x11;
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.OFF.equals(cmd) ? 0x00 : 0xFF;
        }

        @Override
        protected int getGroup(InsteonChannelConfiguration config) {
            return getDevice().isDeviceSyncEnabled() ? config.getGroup() : -1;
        }
    }

    /**
     * Switch percent command handler
     */
    public static class SwitchPercentCommandHandler extends OnOffCommandHandler {
        SwitchPercentCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof PercentType;
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return PercentType.ZERO.equals(cmd) ? 0x13 : 0x11;
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return PercentType.ZERO.equals(cmd) ? 0x00 : 0xFF;
        }
    }

    /**
     * Switch increment command handler
     */
    public static class SwitchIncrementCommandHandler extends OnOffCommandHandler {
        SwitchIncrementCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return IncreaseDecreaseType.INCREASE.equals(cmd) || UpDownType.UP.equals(cmd);
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return 0x11;
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return 0xFF;
        }
    }

    /**
     * Broadcast on/off command handler
     */
    public static class BroadcastOnOffCommandHandler extends OnOffCommandHandler {
        BroadcastOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            if (config.getGroup() != -1) {
                super.handleCommand(channelUID, config, cmd);
            }
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.OFF.equals(cmd) ? 0x13 : 0x11;
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return 0x00; // not parsed
        }

        @Override
        protected int getGroup(InsteonChannelConfiguration config) {
            return config.getGroup();
        }
    }

    /**
     * Broadcast fast on/off command handler
     */
    public static class BroadcastFastOnOffCommandHandler extends BroadcastOnOffCommandHandler {
        BroadcastFastOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.OFF.equals(cmd) ? 0x14 : 0x12;
        }
    }

    /**
     * Broadcast manual change up/down command handler
     */
    public static class BroadcastManualChangeUpDownCommandHandler extends BroadcastOnOffCommandHandler {
        BroadcastManualChangeUpDownCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof UpDownType;
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return 0x17; // manual change start
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return UpDownType.UP.equals(cmd) ? 0x01 : 0x00; // up or down
        }
    }

    /**
     * Broadcast manual change stop command handler
     */
    public static class BroadcastManualChangeStopCommandHandler extends BroadcastOnOffCommandHandler {
        BroadcastManualChangeStopCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return StopMoveType.STOP.equals(cmd);
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return 0x18; // manual change stop
        }
    }

    /**
     * Keypad bitmask command handler
     */
    public static class KeypadBitmaskCommandHandler extends CustomBitmaskCommandHandler {
        KeypadBitmaskCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected int getBitNumber() {
            return getParameterAsInteger("group", 0) - 1;
        }
    }

    /**
     * Keypad button on/off command handler
     */
    public static class KeypadButtonOnOffCommandHandler extends BroadcastOnOffCommandHandler {
        KeypadButtonOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            OnOffType onOffCmd = getOnOffCommand(cmd);
            if (OnOffType.OFF.equals(onOffCmd) && isAlwaysOnToggle()) {
                // ignore off command when keypad button toggle mode is always on
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: {} toggle mode is always on, ignoring off command", nm(), feature.getName());
                }
                // reset to current state
                State state = feature.getState();
                feature.publishState(state, StateChangeType.ALWAYS);
            } else if (config.getGroup() == -1 || !getDevice().isDeviceSyncEnabled()) {
                // set led button if broadcast group not defined or device sync disabled
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: setting led button state", nm());
                }
                setLEDButton(channelUID, config, onOffCmd);
                // adjust related devices after setting led button state
                if (channelUID != null && getDevice().isDeviceSyncEnabled()) {
                    feature.adjustRelatedDevices(channelUID, cmd);
                }
            } else {
                // handle command as broadcast message
                super.handleCommand(channelUID, config, onOffCmd);
            }
        }

        protected OnOffType getOnOffCommand(Command cmd) {
            return (OnOffType) cmd;
        }

        private String getButtonSuffix() {
            return StringUtils.capitalize(feature.getName()); // e.g. "buttonA" => "ButtonA"
        }

        private boolean isAlwaysOnToggle() {
            State toggleMode = feature.getDevice().getState(FEATURE_TOGGLE_MODE + getButtonSuffix());
            return toggleMode == OnOffType.OFF; // always on when toggle mode off
        }

        private void setLEDButton(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            KeypadLEDButtonCommandHandler handler = new KeypadLEDButtonCommandHandler(feature);
            handler.setParameters(parameters);
            handler.handleCommand(channelUID, config, cmd);
        }

        private class KeypadLEDButtonCommandHandler extends KeypadBitmaskCommandHandler {
            KeypadLEDButtonCommandHandler(DeviceFeature feature) {
                super(feature);
            }

            @Override
            protected int getBitmask(Command cmd) {
                int bitmask = super.getBitmask(cmd);
                if (bitmask != -1) {
                    int onMask = getDevice().getLastMsgValueAsInteger(FEATURE_ON_MASK + getButtonSuffix(), 0);
                    int offMask = getDevice().getLastMsgValueAsInteger(FEATURE_OFF_MASK + getButtonSuffix(), 0);
                    if (logger.isTraceEnabled()) {
                        logger.trace("{}: bitmask:{} onMask:{} offMask:{}", nm(), ByteUtils.getBinaryString(bitmask),
                                ByteUtils.getBinaryString(onMask), ByteUtils.getBinaryString(offMask));
                    }
                    // apply keypad button on/off mask (radio group support)
                    bitmask = bitmask & ~offMask | onMask;
                }
                return bitmask;
            }
        }
    }

    /**
     * Keypad button percent command handler
     */
    public static class KeypadButtonPercentCommandHandler extends KeypadButtonOnOffCommandHandler {
        KeypadButtonPercentCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof PercentType;
        }

        @Override
        protected OnOffType getOnOffCommand(Command cmd) {
            return PercentType.ZERO.equals(cmd) ? OnOffType.OFF : OnOffType.ON;
        }
    }

    /**
     * Keypad button increment command handler
     */
    public static class KeypadButtonIncrementCommandHandler extends KeypadButtonOnOffCommandHandler {
        KeypadButtonIncrementCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return IncreaseDecreaseType.INCREASE.equals(cmd) || UpDownType.UP.equals(cmd);
        }

        @Override
        protected OnOffType getOnOffCommand(Command cmd) {
            return OnOffType.ON;
        }
    }

    /**
     * Keypad button config command handler
     */
    public static class KeypadButtonConfigCommandHandler extends OpFlagsCommandHandler {
        KeypadButtonConfigCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected int getOpFlagCommand(Command cmd) {
            String config = ((StringType) cmd).toString();
            switch (config) {
                case "8-BUTTON":
                    return 0x06;
                case "6-BUTTON":
                    return 0x07;
                default:
                    logger.warn("{}: got unexpected button config command: {}, defaulting to: 6-BUTTON", nm(), config);
                    return 0x07;
            }
        }

        @Override
        protected boolean isStateRetrievable() {
            return true;
        }
    }

    /**
     * Siren alarm command handler
     */
    public static class SirenAlarmCommandHandler extends SwitchOnOffCommandHandler {
        SirenAlarmCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.OFF.equals(cmd) ? 0x00 : 0x7F; // no delay + max duration (127 seconds)
        }
    }

    /**
     * Siren alarm duration command handler
     */
    public static class SirenAlarmDurationCommandHandler extends CustomCommandHandler {
        SirenAlarmDurationCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof DecimalType || cmd instanceof QuantityType;
        }

        @Override
        protected double getValue(Command cmd) {
            int duration = getDuration(cmd);
            int value = feature.getLastMsgValueAsInteger(-1);
            if (value == -1) {
                logger.debug("{}: unable to determine last value for feature {}", nm(), feature.getName());
            } else {
                return value & 0x80 | duration;
            }
            return -1;
        }

        private int getDuration(Command cmd) {
            int duration;
            if (cmd instanceof DecimalType) {
                duration = ((DecimalType) cmd).intValue();
            } else {
                @SuppressWarnings("unchecked")
                QuantityType<Time> time = (QuantityType<Time>) cmd;
                QuantityType<Time> convertedTime = time.toUnit(Units.SECOND);
                duration = (convertedTime != null ? convertedTime : time).intValue();
            }
            return Math.max(0, Math.min(duration, 127)); // allowed range 0-127 seconds
        }
    }

    /**
     * Siren alarm type command handler
     */
    public static class SirenAlarmTypeCommandHandler extends CustomCommandHandler {
        SirenAlarmTypeCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected double getValue(Command cmd) {
            String type = ((StringType) cmd).toString();
            switch (type) {
                case "CHIME":
                    return 0x00;
                case "LOUD_SIREN":
                    return 0x01;
                default:
                    logger.warn("{}: got unexpected alert type command: {}, defaulting to: CHIME", nm(), type);
                    return 0x00;
            }
        }
    }

    /**
     * LED brightness command handler
     */
    public static class LEDBrightnessCommandHandler extends CommandHandler {
        LEDBrightnessCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof OnOffType || cmd instanceof PercentType;
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                int level = getLevel(cmd);
                int userData2 = getParameterAsInteger("d2", -1);
                if (userData2 != -1) {
                    // set led on/off
                    setLEDOnOff(channelUID, config, level > 0 ? OnOffType.ON : OnOffType.OFF);
                    // set led brightness level
                    byte[] data = { (byte) 0x01, (byte) userData2, (byte) level };
                    boolean setCRC = getDevice().getInsteonEngine().supportsChecksum();
                    Msg msg = Msg.makeExtendedMessage(getDevice().getAddress(), (byte) 0x2E, (byte) 0x00, data, setCRC);
                    feature.sendRequest(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: sent led brightness level {} request to {}", nm(),
                                ByteUtils.getHexString(level), getDevice().getAddress());
                    }
                } else {
                    logger.warn("{}: no d2 parameter specified in command handler", nm());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        private int getLevel(Command cmd) {
            int level;
            if (cmd instanceof OnOffType) {
                level = OnOffType.OFF.equals(cmd) ? 0 : 100;
            } else {
                level = ((PercentType) cmd).intValue();
            }
            return (int) Math.round(level * 127.0 / 100);
        }

        private void setLEDOnOff(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            DeviceFeature feature = getDevice().getFeature(FEATURE_LED_ON_OFF);
            if (feature != null && feature.getState() != (State) cmd) {
                feature.handleCommand(channelUID, config, cmd);
            }
        }
    }

    /**
     * Momentary on command handler
     */
    public static class MomentaryOnCommandHandler extends CommandHandler {
        MomentaryOnCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return OnOffType.ON.equals(cmd);
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                int cmd1 = getParameterAsInteger("cmd1", -1);
                if (cmd1 != -1) {
                    Msg msg = Msg.makeStandardMessage(getDevice().getAddress(), (byte) cmd1, (byte) 0x00);
                    feature.sendRequest(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: sent {} request to {}", nm(), feature.getName(), getDevice().getAddress());
                    }
                } else {
                    logger.warn("{}: no cmd1 field specified", nm());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    /**
     * Operating flags command handler
     */
    public static class OpFlagsCommandHandler extends CommandHandler {
        OpFlagsCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof OnOffType;
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                int cmd2 = getOpFlagCommand(cmd);
                if (cmd2 != -1) {
                    Msg msg = getOpFlagMessage(cmd2);
                    feature.sendRequest(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: sent op flag {} {} request to {}", nm(), feature.getName(), cmd,
                                getDevice().getAddress());
                    }
                    // publish state if not retrievable (e.g. stayAwake)
                    if (!isStateRetrievable()) {
                        feature.publishState((State) cmd, StateChangeType.CHANGED);
                    }
                } else {
                    logger.warn("{}: unable to determine op flags command, ignoring request", nm());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        protected int getOpFlagCommand(Command cmd) {
            return OnOffType.OFF.equals(cmd) ? getParameterAsInteger("off", -1) : getParameterAsInteger("on", -1);
        }

        protected Msg getOpFlagMessage(int cmd2) throws FieldException, InvalidMessageTypeException {
            if (getDevice().getInsteonEngine().supportsChecksum()) {
                return Msg.makeExtendedMessage(getDevice().getAddress(), (byte) 0x20, (byte) cmd2, true);
            } else {
                return Msg.makeStandardMessage(getDevice().getAddress(), (byte) 0x20, (byte) cmd2);
            }
        }

        protected boolean isStateRetrievable() {
            // op flag state is retrieved if a valid bit (0-7) parameter is defined
            int bit = getParameterAsInteger("bit", -1);
            return bit >= 0 && bit <= 7;
        }
    }

    /**
     * Multi-operating flags abstract command handler
     */
    public abstract static class MultiOpFlagsCommandHandler extends OpFlagsCommandHandler {
        MultiOpFlagsCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                for (Entry<Integer, String> entry : getOpFlagCommands(cmd).entrySet()) {
                    Msg msg = getOpFlagMessage(entry.getKey());
                    feature.sendRequest(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: sent op flag {} request to {}", nm(), entry.getValue(),
                                getDevice().getAddress());
                    }
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        protected abstract Map<Integer, String> getOpFlagCommands(Command cmd);
    }

    /**
     * Ramp rate command handler
     */
    public static class RampRateCommandHandler extends CommandHandler {
        RampRateCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof DecimalType || cmd instanceof QuantityType;
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                RampRate rampRate = getRampRate(cmd);
                byte[] data = { (byte) getParameterAsInteger("group", 1), (byte) 0x05, (byte) rampRate.getValue() };
                boolean setCRC = getDevice().getInsteonEngine().supportsChecksum();
                Msg msg = Msg.makeExtendedMessage(getDevice().getAddress(), (byte) 0x2E, (byte) 0x00, data, setCRC);
                feature.sendRequest(msg);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: sent ramp time {}s to {}", nm(), rampRate.getTimeInSeconds(),
                            getDevice().getAddress());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        private RampRate getRampRate(Command cmd) {
            double rampTime;
            if (cmd instanceof DecimalType) {
                rampTime = ((DecimalType) cmd).doubleValue();
            } else {
                @SuppressWarnings("unchecked")
                QuantityType<Time> time = (QuantityType<Time>) cmd;
                QuantityType<Time> convertedTime = time.toUnit(Units.SECOND);
                rampTime = (convertedTime != null ? convertedTime : time).doubleValue();
            }
            return RampRate.fromTime(rampTime);
        }
    }

    /**
     * FanLinc fan mode command handler
     */
    public static class FanLincFanModeCommandHandler extends OnOffCommandHandler {
        FanLincFanModeCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            String mode = ((StringType) cmd).toString();
            return "OFF".equals(mode) ? 0x13 : 0x11;
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            String mode = ((StringType) cmd).toString();
            switch (mode) {
                case "OFF":
                    return 0x00;
                case "LOW":
                    return 0x55;
                case "MEDIUM":
                    return 0xAA;
                case "HIGH":
                    return 0xFF;
                default:
                    logger.warn("{}: got unexpected fan mode command: {}, defaulting to: OFF", nm(), mode);
                    return 0x00;
            }
        }
    }

    /**
     * FanLinc fan on/off command handler
     */
    public static class FanLincFanOnOffCommandHandler extends OnOffCommandHandler {
        FanLincFanOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.OFF.equals(cmd) ? 0x13 : 0x11;
        }
    }

    /**
     * FanLinc fan percent command handler
     */
    public static class FanLincFanPercentCommandHandler extends OnOffCommandHandler {
        FanLincFanPercentCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof PercentType;
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return PercentType.ZERO.equals(cmd) ? 0x13 : 0x11;
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            int level = ((PercentType) cmd).intValue();
            return (int) Math.ceil(level * 255.0 / 100); // round up
        }
    }

    /**
     * I/O linc momentary duration command handler
     */
    public static class IOLincMomentaryDurationCommandHandler extends CommandHandler {
        IOLincMomentaryDurationCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof DecimalType || cmd instanceof QuantityType;
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                int prescaler = 1;
                int delay = (int) Math.round(getDuration(cmd) * 10);
                if (delay > 255) {
                    prescaler = (int) Math.ceil(delay / 255.0);
                    delay = (int) Math.round(delay / (double) prescaler);
                }
                boolean setCRC = getDevice().getInsteonEngine().supportsChecksum();
                // define ext command message to set momentary duration delay
                Msg mdelay = Msg.makeExtendedMessage(getDevice().getAddress(), (byte) 0x2E, (byte) 0x00,
                        new byte[] { (byte) 0x01, (byte) 0x06, (byte) delay }, setCRC);
                // define ext command message to set momentary duration prescaler
                Msg mprescaler = Msg.makeExtendedMessage(getDevice().getAddress(), (byte) 0x2E, (byte) 0x00,
                        new byte[] { (byte) 0x01, (byte) 0x07, (byte) prescaler }, setCRC);
                // send requests
                feature.sendRequest(mdelay);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: sent momentary duration delay {} request to {}", nm(),
                            ByteUtils.getHexString(delay), getDevice().getAddress());
                }
                feature.sendRequest(mprescaler);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: sent momentary duration prescaler {} request to {}", nm(),
                            ByteUtils.getHexString(prescaler), getDevice().getAddress());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        private double getDuration(Command cmd) {
            if (cmd instanceof DecimalType) {
                return ((DecimalType) cmd).doubleValue();
            } else {
                @SuppressWarnings("unchecked")
                QuantityType<Time> time = (QuantityType<Time>) cmd;
                QuantityType<Time> convertedTime = time.toUnit(Units.SECOND);
                return (convertedTime != null ? convertedTime : time).doubleValue();
            }
        }
    }

    /**
     * I/O linc relay mode command handler
     */
    public static class IOLincRelayModeCommandHandler extends MultiOpFlagsCommandHandler {
        IOLincRelayModeCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected Map<Integer, String> getOpFlagCommands(Command cmd) {
            Map<Integer, String> commands = new HashMap<>();
            String mode = ((StringType) cmd).toString();
            switch (mode) {
                case "LATCHING":
                    commands.put(0x07, "momentary mode OFF");
                    break;
                case "MOMENTARY_A":
                    commands.put(0x06, "momentary mode ON");
                    commands.put(0x13, "momentary trigger on/off OFF");
                    commands.put(0x15, "momentary sensor follow OFF");
                    break;
                case "MOMENTARY_B":
                    commands.put(0x06, "momentary mode ON");
                    commands.put(0x12, "momentary trigger on/off ON");
                    commands.put(0x15, "momentary sensor follow OFF");
                    break;
                case "MOMENTARY_C":
                    commands.put(0x06, "momentary mode ON");
                    commands.put(0x13, "momentary trigger on/off OFF");
                    commands.put(0x14, "momentary sensor follow ON");
                    break;
                default:
                    logger.warn("{}: got unexpected relay mode command: {}", nm(), mode);
            }
            return commands;
        }
    }

    /**
     * Micro module operation mode command handler
     */
    public static class MicroModuleOpModeCommandHandler extends MultiOpFlagsCommandHandler {
        MicroModuleOpModeCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected Map<Integer, String> getOpFlagCommands(Command cmd) {
            Map<Integer, String> commands = new HashMap<>();
            String mode = ((StringType) cmd).toString();
            switch (mode) {
                case "LATCHING":
                    commands.put(0x20, "momentary line OFF");
                    break;
                case "SINGLE_MOMENTARY":
                    commands.put(0x21, "momentary line ON");
                    commands.put(0x1E, "dual line OFF");
                    break;
                case "DUAL_MOMENTARY":
                    commands.put(0x21, "momentary line ON");
                    commands.put(0x1E, "dual line ON");
                    break;
                default:
                    logger.warn("{}: got unexpected operation mode command: {}", nm(), mode);
            }
            return commands;
        }
    }

    /**
     * Thermostat fan mode command handler
     */
    public static class ThermostatFanModeCommandHandler extends CustomCommandHandler {
        ThermostatFanModeCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected double getValue(Command cmd) {
            String mode = ((StringType) cmd).toString();
            switch (mode) {
                case "AUTO":
                    return 0x08;
                case "ON":
                    return 0x07;
                default:
                    logger.warn("{}: got unexpected fan mode command: {}, defaulting to: AUTO", nm(), mode);
                    return 0x08;
            }
        }
    }

    /**
     * Thermostat system mode command handler
     */
    public static class ThermostatSystemModeCommandHandler extends CustomCommandHandler {
        ThermostatSystemModeCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected double getValue(Command cmd) {
            String mode = ((StringType) cmd).toString();
            switch (mode) {
                case "OFF":
                    return 0x09;
                case "HEAT":
                    return 0x04;
                case "COOL":
                    return 0x05;
                case "AUTO":
                    return 0x06;
                case "PROGRAM":
                    return 0x0A;
                default:
                    logger.warn("{}: got unexpected system mode command: {}, defaulting to: PROGRAM", nm(), mode);
                    return 0x0A;
            }
        }
    }

    /**
     * Venstar thermostat system mode handler
     */
    public static class VenstarSystemModeCommandHandler extends CustomCommandHandler {
        VenstarSystemModeCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected double getValue(Command cmd) {
            String mode = ((StringType) cmd).toString();
            switch (mode) {
                case "OFF":
                    return 0x09;
                case "HEAT":
                    return 0x04;
                case "COOL":
                    return 0x05;
                case "AUTO":
                    return 0x06;
                case "PROGRAM_HEAT":
                    return 0x0A;
                case "PROGRAM_COOL":
                    return 0x0B;
                case "PROGRAM_AUTO":
                    return 0x0C;
                default:
                    logger.warn("{}: got unexpected system mode command: {}, defaulting to: PROGRAM_AUTO", nm(), mode);
                    return 0x0C;
            }
        }
    }

    /**
     * Thermostat temperature format command handler
     */
    public static class ThermostatTemperatureFormatCommandHandler extends CustomBitmaskCommandHandler {
        ThermostatTemperatureFormatCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected boolean shouldSetBit(Command cmd) {
            String format = ((StringType) cmd).toString();
            switch (format) {
                case "FAHRENHEIT":
                    return false; // 0x00 (clear)
                case "CELSIUS":
                    return true; // 0x01 (set)
                default:
                    logger.warn("{}: got unexpected temperature format command: {}, defaulting to: FAHRENHEIT", nm(),
                            format);
                    return false;
            }
        }
    }

    /**
     * Venstar thermostat temperature format command handler
     */
    public static class VenstarTemperatureFormatCommandHandler extends CustomCommandHandler {
        VenstarTemperatureFormatCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected double getValue(Command cmd) {
            String format = ((StringType) cmd).toString();
            switch (format) {
                case "FAHRENHEIT":
                    return 0x00;
                case "CELSIUS":
                    return 0x01;
                default:
                    logger.warn("{}: got unexpected temperature format command: {}, defaulting to: FAHRENHEIT", nm(),
                            format);
                    return 0x00;
            }
        }
    }

    /**
     * Thermostat time format command handler
     */
    public static class ThermostatTimeFormatCommandHandler extends CustomBitmaskCommandHandler {
        ThermostatTimeFormatCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected boolean shouldSetBit(Command cmd) {
            String format = ((StringType) cmd).toString();
            switch (format) {
                case "12H":
                    return false; // 0x00 (clear)
                case "24H":
                    return true; // 0x01 (set)
                default:
                    logger.warn("{}: got unexpected temperature format command: {}, defaulting to: 12H", nm(), format);
                    return false;
            }
        }
    }

    /**
     * Thermostat sync time command handler
     */
    public static class ThermostatSyncTimeCommandHandler extends MomentaryOnCommandHandler {
        ThermostatSyncTimeCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                ZonedDateTime time = ZonedDateTime.now();
                byte[] data = { (byte) 0x02, (byte) (time.getDayOfWeek().getValue() % 7), (byte) time.getHour(),
                        (byte) time.getMinute(), (byte) time.getSecond() };
                Msg msg = Msg.makeExtendedMessageCRC2(getDevice().getAddress(), (byte) 0x2E, (byte) 0x02, data);
                feature.sendRequest(msg);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: sent set time data request to {}", nm(), getDevice().getAddress());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    /**
     * IM generic abstract command handler
     */
    public abstract static class IMCommandHandler extends CommandHandler {
        IMCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                Msg msg = getIMMessage(cmd);
                feature.sendRequest(msg);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: sent {} request to {}", nm(), cmd, getDevice().getAddress());
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        protected abstract Msg getIMMessage(Command cmd) throws InvalidMessageTypeException, FieldException;
    }

    /**
     * IM led on/off command handler
     */
    public static class IMLEDOnOffCommandHandler extends IMCommandHandler {
        IMLEDOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof OnOffType;
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            // set led control
            setLEDControl(channelUID, config);
            // set led on/off
            super.handleCommand(channelUID, config, cmd);
            // publish state since not retrievable
            feature.publishState((State) cmd, StateChangeType.CHANGED);
        }

        @Override
        protected Msg getIMMessage(Command cmd) throws InvalidMessageTypeException, FieldException {
            return Msg.makeMessage(OnOffType.OFF.equals(cmd) ? "LEDOff" : "LEDOn");
        }

        private void setLEDControl(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config) {
            DeviceFeature feature = getDevice().getFeature(FEATURE_LED_CONTROL);
            if (feature != null && feature.getState() != OnOffType.ON) {
                feature.handleCommand(channelUID, config, OnOffType.ON);
            }
        }
    }

    /**
     * IM beep command handler
     */
    public static class IMBeepCommandHandler extends IMCommandHandler {
        IMBeepCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return OnOffType.ON.equals(cmd);
        }

        @Override
        protected Msg getIMMessage(Command cmd) throws InvalidMessageTypeException, FieldException {
            return Msg.makeMessage("Beep");
        }
    }

    /**
     * IM config command handler
     */
    public static class IMConfigCommandHandler extends CustomBitmaskCommandHandler {
        IMConfigCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                int flags = getBitmask(cmd);
                if (flags != -1) {
                    Msg msg = Msg.makeMessage("SetIMConfig");
                    msg.setByte("IMConfigurationFlags", (byte) flags);
                    feature.sendRequest(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: sent {} request to {}", nm(), cmd, getDevice().getAddress());
                    }
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    /**
     * X10 generic abstract command handler
     */
    public abstract static class X10CommandHandler extends CommandHandler {
        X10CommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
            try {
                InsteonAddress address = getDevice().getAddress();
                int addrCode = address.getX10Code();
                int cmdCode = getCommandCode(cmd, address.getX10HouseCode());
                Msg maddr = Msg.makeX10Message((byte) addrCode, X10.Flag.ADDRESS.code());
                feature.sendRequest(maddr);
                Msg mcmd = Msg.makeX10Message((byte) cmdCode, X10.Flag.COMMAND.code());
                feature.sendRequest(mcmd);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: sent {} request to {}", nm(), cmd, address);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        protected abstract int getCommandCode(Command cmd, byte houseCode);
    }

    /**
     * X10 on/off command handler
     */
    public static class X10OnOffCommandHandler extends X10CommandHandler {
        X10OnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof OnOffType;
        }

        @Override
        protected int getCommandCode(Command cmd, byte houseCode) {
            int cmdCode = OnOffType.OFF.equals(cmd) ? X10.Command.OFF.code() : X10.Command.ON.code();
            return houseCode << 4 | cmdCode;
        }
    }

    /**
     * X10 percent command handler
     */
    public static class X10PercentCommandHandler extends X10CommandHandler {

        private static final int[] X10_LEVEL_CODES = { 0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15 };

        X10PercentCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof PercentType;
        }

        @Override
        protected int getCommandCode(Command cmd, byte houseCode) {
            //
            // I did not have hardware that would respond to the PRESET_DIM codes.
            // This code path needs testing.
            //
            int level = ((PercentType) cmd).intValue() * 32 / 100;
            int levelCode = X10_LEVEL_CODES[level % 16];
            int cmdCode = level >= 16 ? X10.Command.PRESET_DIM_2.code() : X10.Command.PRESET_DIM_1.code();
            return levelCode << 4 | cmdCode;
        }
    }

    /**
     * X10 increase/decrease command handler
     */
    public static class X10IncreaseDecreaseCommandHandler extends X10CommandHandler {
        X10IncreaseDecreaseCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof IncreaseDecreaseType;
        }

        @Override
        protected int getCommandCode(Command cmd, byte houseCode) {
            int cmdCode = IncreaseDecreaseType.INCREASE.equals(cmd) ? X10.Command.BRIGHT.code()
                    : X10.Command.DIM.code();
            return houseCode << 4 | cmdCode;
        }
    }

    /**
     * Factory method for creating default command handler
     *
     * @param feature the feature for which to create the handler
     * @return the default command handler which was created
     */
    public static DefaultCommandHandler makeDefaultHandler(DeviceFeature feature) {
        return new DefaultCommandHandler(feature);
    }

    /**
     * Factory method for creating handlers of a given name using java reflection
     *
     * @param name the name of the handler to create
     * @param parameters the parameters of the handler to create
     * @param feature the feature for which to create the handler
     * @return the handler which was created
     */
    public static @Nullable <T extends CommandHandler> T makeHandler(String name, Map<String, String> parameters,
            DeviceFeature feature) {
        try {
            String className = CommandHandler.class.getName() + "$" + name;
            @SuppressWarnings("unchecked")
            Class<? extends T> classRef = (Class<? extends T>) Class.forName(className);
            @Nullable
            T handler = classRef.getDeclaredConstructor(DeviceFeature.class).newInstance(feature);
            handler.setParameters(parameters);
            return handler;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            logger.warn("error trying to create command handler: {}", name, e);
        }
        return null;
    }
}
