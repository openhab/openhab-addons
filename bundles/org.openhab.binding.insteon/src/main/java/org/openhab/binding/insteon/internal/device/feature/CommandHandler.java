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

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.device.RampRate;
import org.openhab.binding.insteon.internal.device.X10Address;
import org.openhab.binding.insteon.internal.device.X10Command;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.FanLincFanSpeed;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.IOLincRelayMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.KeypadButtonConfig;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.KeypadButtonToggleMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.MicroModuleOpMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.RemoteSceneButtonConfig;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.RemoteSwitchButtonConfig;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.SirenAlertType;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ThermostatFanMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ThermostatSystemMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ThermostatTemperatureScale;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ThermostatTimeFormat;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.VenstarSystemMode;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.BinaryUtils;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
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
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public abstract class CommandHandler extends BaseFeatureHandler {
    private static final Set<String> SUPPORTED_COMMAND_TYPES = Set.of("DecimalType", "IncreaseDecreaseType",
            "OnOffType", "NextPreviousType", "PercentType", "PlayPauseType", "QuantityType", "RefreshType",
            "RewindFastforwardType", "StopMoveType", "StringType", "UpDownType");

    protected final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    /**
     * Constructor
     *
     * @param feature The DeviceFeature for which this command was intended.
     *            The openHAB commands are issued on an openhab item. The .items files bind
     *            an openHAB item to a DeviceFeature.
     */
    public CommandHandler(DeviceFeature feature) {
        super(feature);
    }

    /**
     * Returns handler id
     *
     * @return handler id based on command parameter
     */
    public String getId() {
        return getParameterAsString("command", "default");
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
    public abstract void handleCommand(InsteonChannelConfiguration config, Command cmd);

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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
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
                InsteonAddress address = getInsteonDevice().getAddress();
                boolean setCRC = getInsteonDevice().getInsteonEngine().supportsChecksum();
                Msg msg;
                if (ext == 0) {
                    msg = Msg.makeStandardMessage(address, (byte) cmd1, (byte) cmd2);
                } else {
                    // set userData1 to d1 parameter if defined, fallback to group parameter
                    byte[] data = { (byte) getParameterAsInteger("d1", getParameterAsInteger("group", 0)),
                            (byte) getParameterAsInteger("d2", 0), (byte) getParameterAsInteger("d3", 0) };
                    msg = Msg.makeExtendedMessage(address, (byte) cmd1, (byte) cmd2, data, false);
                }
                // set field to clamped byte-size value
                msg.setByte(field, (byte) Math.min(value, 0xFF));
                // set crc based on message type if supported
                if (setCRC) {
                    if (ext == 1) {
                        msg.setCRC();
                    } else if (ext == 2) {
                        msg.setCRC2();
                    }
                }
                // send request
                feature.sendRequest(msg);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: sent {} {} request to {}", nm(), feature.getName(), HexUtils.getHexString(value),
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

        protected @Nullable Boolean shouldSetBit(Command cmd) {
            return OnOffType.ON.equals(cmd) ^ getParameterAsBoolean("inverted", false);
        }

        protected int getBitmask(Command cmd) {
            // get bit number based on parameter
            int bit = getBitNumber();
            // get last bitmask message value received by this feature
            int bitmask = feature.getLastMsgValueAsInteger(-1);
            // determine if bit should be set
            Boolean shouldSetBit = shouldSetBit(cmd);
            // update last bitmask value specific bit based on cmd state, if defined and bit number valid
            if (bit < 0 || bit > 7) {
                logger.debug("{}: invalid bit number {} for {}", nm(), bit, feature.getName());
            } else if (bitmask == -1) {
                logger.debug("{}: unable to determine last bitmask for {}", nm(), feature.getName());
            } else if (shouldSetBit == null) {
                logger.debug("{}: unable to determine if bit should be set, ignoring request", nm());
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("{}: bitmask:{} bit:{} set:{}", nm(), BinaryUtils.getBinaryString(bitmask), bit,
                            shouldSetBit);
                }
                return BinaryUtils.updateBit(bitmask, bit, shouldSetBit);
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
            double value = Objects.requireNonNullElse(temperature.toInvertibleUnit(unit), temperature).doubleValue();
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
            return Objects.requireNonNullElse(time.toInvertibleUnit(unit), time).doubleValue();
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                int cmd1 = getCommandCode(config, cmd);
                int level = getLevel(config, cmd);
                int group = getGroup(config);
                // ignore request if cmd1/level not defined, send broadcast msg if group defined, otherwise direct msg
                if (cmd1 == -1 || level == -1) {
                    logger.debug("{}: unable to determine cmd1 or level value, ignoring request", nm());
                } else if (group != -1) {
                    Msg msg = Msg.makeBroadcastMessage(group, (byte) cmd1, (byte) level);
                    feature.sendRequest(msg);
                    logger.debug("{}: sent broadcast {} request to group {}", nm(), cmd, group);
                    // poll related devices to broadcast group,
                    // allowing each responder feature to determine its own poll delay
                    feature.pollRelatedDevices(group, -1);
                } else {
                    InsteonAddress address = getInsteonDevice().getAddress();
                    int componentId = feature.getGroup();
                    Msg msg;
                    if (componentId > 1) {
                        byte[] data = { (byte) componentId };
                        boolean setCRC = getInsteonDevice().getInsteonEngine().supportsChecksum();
                        msg = Msg.makeExtendedMessage(address, (byte) cmd1, (byte) level, data, setCRC);
                    } else {
                        msg = Msg.makeStandardMessage(address, (byte) cmd1, (byte) level);
                    }
                    feature.sendRequest(msg);
                    logger.debug("{}: sent {} request to {}", nm(), cmd, address);
                    // adjust related devices if original channel config (initial request) and device sync enabled
                    if (config.isOriginal() && getInsteonDevice().isDeviceSyncEnabled()) {
                        feature.adjustRelatedDevices(config, cmd);
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

        private int getOnLevel(InsteonChannelConfiguration config) {
            int level = config.getOnLevel();
            if (level == -1) {
                State state = getInsteonDevice().getFeatureState(FEATURE_ON_LEVEL);
                level = (state instanceof PercentType percent ? percent : PercentType.HUNDRED).intValue();

            }
            logger.trace("{}: using on level {}%", nm(), level);
            return (int) Math.ceil(level * 255.0 / 100); // round up
        }
    }

    /**
     * Dimmer on/off command handler
     */
    public static class DimmerOnOffCommandHandler extends OnOffCommandHandler {
        DimmerOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            RampRate rampRate = config.getRampRate();
            if (rampRate == null) {
                // standard command if ramp rate parameter not configured
                super.handleCommand(config, cmd);
            } else if (rampRate == RampRate.INSTANT) {
                // instant dimmer command if ramp rate parameter is instant (0.1 sec)
                setInstantDimmer(config, cmd);
            } else {
                // ramp dimmer command otherwise
                setRampDimmer(config, cmd);
            }
            // update state since dimmer related channels not automatically updated by the framework
            PercentType state = getState(config, cmd);
            feature.updateState(state);
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.OFF.equals(cmd) ? 0x13 : 0x11;
        }

        @Override
        protected int getGroup(InsteonChannelConfiguration config) {
            return config.getOnLevel() == -1 && getInsteonDevice().isDeviceSyncEnabled()
                    ? feature.getBroadcastGroup(config)
                    : -1;
        }

        protected PercentType getState(InsteonChannelConfiguration config, Command cmd) {
            if (OnOffType.OFF.equals(cmd)) {
                return PercentType.ZERO;
            }
            int level = config.getOnLevel();
            if (level != -1) {
                return new PercentType(level);
            }
            State state = getInsteonDevice().getFeatureState(FEATURE_ON_LEVEL);
            if (state instanceof PercentType percent) {
                return percent;
            }
            return PercentType.HUNDRED;
        }

        private void setInstantDimmer(InsteonChannelConfiguration config, Command cmd) {
            InstantDimmerCommandHandler handler = new InstantDimmerCommandHandler(feature);
            handler.setParameters(parameters);
            handler.handleCommand(config, cmd);
        }

        private void setRampDimmer(InsteonChannelConfiguration config, Command cmd) {
            RampDimmerCommandHandler handler = new RampDimmerCommandHandler(feature);
            handler.setParameters(parameters);
            handler.handleCommand(config, cmd);
        }
    }

    /**
     * Dimmer percent command handler
     */
    public static class DimmerPercentCommandHandler extends DimmerOnOffCommandHandler {
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

        @Override
        protected int getGroup(InsteonChannelConfiguration config) {
            return -1;
        }

        @Override
        protected PercentType getState(InsteonChannelConfiguration config, Command cmd) {
            return (PercentType) cmd;
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
            return getInsteonDevice().isDeviceSyncEnabled() ? feature.getBroadcastGroup(config) : -1;
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
            return getInsteonDevice().isDeviceSyncEnabled() ? feature.getBroadcastGroup(config) : -1;
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
            return getInsteonDevice().isDeviceSyncEnabled() ? feature.getBroadcastGroup(config) : -1;
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
            if (cmd instanceof PercentType percent) {
                return (int) Math.ceil(percent.intValue() * 255.0 / 100); // round up
            } else {
                return super.getLevel(config, cmd);
            }
        }

        @Override
        protected int getGroup(InsteonChannelConfiguration config) {
            return -1;
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                InsteonAddress address = getInsteonDevice().getAddress();
                int level = getLevel(config, cmd);
                RampRate rampRate = getRampRate(config);
                int cmd1 = getCommandCode(level);
                int cmd2 = getEncodedValue(level, rampRate.getValue());
                Msg msg = Msg.makeStandardMessage(address, (byte) cmd1, (byte) cmd2);
                feature.sendRequest(msg);
                logger.debug("{}: sent level {} with ramp time {} to {}", nm(), cmd, rampRate, address);
                if (config.isOriginal() && getInsteonDevice().isDeviceSyncEnabled()) {
                    feature.adjustRelatedDevices(config, cmd);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        private RampRate getRampRate(InsteonChannelConfiguration config) {
            return Objects.requireNonNullElse(config.getRampRate(), RampRate.DEFAULT);
        }

        private int getCommandCode(int level) {
            ProductData productData = getInsteonDevice().getProductData();
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
            return getInsteonDevice().isDeviceSyncEnabled() ? feature.getBroadcastGroup(config) : -1;
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            if (getGroup(config) != -1) {
                super.handleCommand(config, cmd);
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
            return feature.getBroadcastGroup(config);
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
     * Broadcast refresh command handler
     */
    public static class BroadcastRefreshCommandHandler extends RefreshCommandHandler {
        BroadcastRefreshCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            int group = feature.getBroadcastGroup(config);
            if (group != -1) {
                feature.pollRelatedDevices(group, 0L);
            }
        }
    }

    /**
     * Keypad button on/off command handler
     */
    public static class KeypadButtonOnOffCommandHandler extends CustomBitmaskCommandHandler {
        KeypadButtonOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            OnOffType onOffCmd = getOnOffCommand(cmd);
            int group = getGroup(config);
            KeypadButtonToggleMode toggleMode = getToggleMode();
            if (KeypadButtonToggleMode.ALWAYS_ON.equals(toggleMode) && OnOffType.OFF.equals(onOffCmd)
                    || KeypadButtonToggleMode.ALWAYS_OFF.equals(toggleMode) && OnOffType.ON.equals(onOffCmd)) {
                // ignore command when keypad button toggle mode is always on or off
                logger.debug("{}: {} toggle mode is {}, ignoring {} command", nm(), feature.getName(), toggleMode,
                        onOffCmd);
            } else if (group != -1) {
                // send broadcast message if group defined
                logger.debug("{}: sending broadcast message", nm());
                sendBroadcastOnOff(config, onOffCmd);
                // update state since button channels not automatically updated by the framework
                feature.updateState(onOffCmd);
            } else {
                // set button led bitmask otherwise
                logger.debug("{}: setting button led bitmask", nm());
                super.handleCommand(config, onOffCmd);
                // update state since button channels not automatically updated by the framework
                feature.updateState(onOffCmd);
                // adjust related devices if original channel config and device sync enabled
                if (config.isOriginal() && getInsteonDevice().isDeviceSyncEnabled()) {
                    feature.adjustRelatedDevices(config, cmd);
                }
            }
        }

        @Override
        protected int getBitNumber() {
            return feature.getGroup() - 1;
        }

        @Override
        protected int getBitmask(Command cmd) {
            int bitmask = super.getBitmask(cmd);
            if (bitmask != -1) {
                int onMask = getInsteonDevice().getLastMsgValueAsInteger(FEATURE_TYPE_KEYPAD_BUTTON_ON_MASK,
                        feature.getGroup(), -1);
                int offMask = getInsteonDevice().getLastMsgValueAsInteger(FEATURE_TYPE_KEYPAD_BUTTON_OFF_MASK,
                        feature.getGroup(), -1);
                if (onMask == -1 || offMask == -1) {
                    logger.debug("{}: undefined button on/off mask last values for {}", nm(), feature.getName());
                    bitmask = -1;
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("{}: bitmask:{} onMask:{} offMask:{}", nm(), BinaryUtils.getBinaryString(bitmask),
                                BinaryUtils.getBinaryString(onMask), BinaryUtils.getBinaryString(offMask));
                    }
                    // apply button on/off mask
                    bitmask = bitmask & ~offMask | onMask;
                    // update last bitmask value
                    updateLastBitmaskValue(bitmask);
                }
            }
            return bitmask;
        }

        protected OnOffType getOnOffCommand(Command cmd) {
            return (OnOffType) cmd;
        }

        protected int getGroup(InsteonChannelConfiguration config) {
            return getInsteonDevice().isDeviceSyncEnabled() ? feature.getBroadcastGroup(config) : -1;
        }

        private KeypadButtonToggleMode getToggleMode() {
            try {
                State state = getInsteonDevice().getFeatureState(FEATURE_TYPE_KEYPAD_BUTTON_TOGGLE_MODE,
                        feature.getGroup());
                if (state != null) {
                    return KeypadButtonToggleMode.valueOf(state.toString());
                }
            } catch (IllegalArgumentException e) {
            }
            return KeypadButtonToggleMode.TOGGLE;
        }

        private void sendBroadcastOnOff(InsteonChannelConfiguration config, Command cmd) {
            BroadcastOnOffCommandHandler handler = new BroadcastOnOffCommandHandler(feature);
            handler.setParameters(parameters);
            handler.handleCommand(config, cmd);
        }

        private void updateLastBitmaskValue(int value) {
            DeviceFeature groupFeature = feature.getGroupFeature();
            if (groupFeature != null) {
                // set button group feature last msg value
                groupFeature.setLastMsgValue(value);
                // set button related features last msg value
                groupFeature.getConnectedFeatures().forEach(feature -> feature.setLastMsgValue(value));
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
            return OnOffType.from(!PercentType.ZERO.equals(cmd));
        }

        @Override
        protected int getGroup(InsteonChannelConfiguration config) {
            return -1;
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

        @Override
        protected int getGroup(InsteonChannelConfiguration config) {
            return -1;
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
            try {
                String config = ((StringType) cmd).toString();
                return KeypadButtonConfig.valueOf(config).shouldSetFlag() ? getParameterAsInteger("on", -1)
                        : getParameterAsInteger("off", -1);
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected button config command: {}, ignoring request", nm(), cmd);
                return -1;
            }
        }

        @Override
        protected boolean isStateRetrievable() {
            return true;
        }
    }

    /**
     * Keypad button toggle mode command handler
     */
    public static class KeypadButtonToggleModeCommandHandler extends CommandHandler {
        KeypadButtonToggleModeCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof DecimalType || cmd instanceof StringType;
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                if (cmd instanceof DecimalType decimalCmd) {
                    setToggleMode(decimalCmd.intValue() >> 8, decimalCmd.intValue() & 0xFF);
                } else if (cmd instanceof StringType stringCmd) {
                    int bit = feature.getGroup() - 1;
                    if (bit < 0 || bit > 7) {
                        logger.debug("{}: invalid bit number {} for {}", nm(), bit, feature.getName());
                        return;
                    }
                    int lastValue = feature.getLastMsgValueAsInteger(-1);
                    if (lastValue == -1) {
                        logger.debug("{}: undefined toggle mode last value for {}", nm(), feature.getName());
                        return;
                    }
                    KeypadButtonToggleMode mode = KeypadButtonToggleMode.valueOf(stringCmd.toString());
                    int nonToggleMask = BinaryUtils.updateBit(lastValue >> 8, bit,
                            mode != KeypadButtonToggleMode.TOGGLE);
                    int alwaysOnOffMask = BinaryUtils.updateBit(lastValue & 0xFF, bit,
                            mode == KeypadButtonToggleMode.ALWAYS_ON);
                    setToggleMode(nonToggleMask, alwaysOnOffMask);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected toggle mode command: {}, ignoring request", nm(), cmd);
            }
        }

        private void setToggleMode(int nonToggleMask, int alwaysOnOffMask) {
            try {
                InsteonAddress address = getInsteonDevice().getAddress();
                boolean setCRC = getInsteonDevice().getInsteonEngine().supportsChecksum();
                // define ext command message to set keypad button non toggle mask
                Msg nonToggleMaskMsg = Msg.makeExtendedMessage(address, (byte) 0x2E, (byte) 0x00,
                        new byte[] { (byte) 0x01, (byte) 0x08, (byte) nonToggleMask }, setCRC);
                // define ext command message to set keypad button always on/off mask
                Msg alwaysOnOffMaskMsg = Msg.makeExtendedMessage(address, (byte) 0x2E, (byte) 0x00,
                        new byte[] { (byte) 0x01, (byte) 0x0B, (byte) alwaysOnOffMask }, setCRC);
                // send requests
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: sent keypad button non toggle mask {} request to {}", nm(),
                            HexUtils.getHexString(nonToggleMask), address);
                }
                feature.sendRequest(nonToggleMaskMsg);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: sent keypad button always on/off mask {} request to {}", nm(),
                            HexUtils.getHexString(alwaysOnOffMask), address);
                }
                feature.sendRequest(alwaysOnOffMaskMsg);
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }
    }

    /**
     * Heartbeat interval command handler
     */
    public static class HeartbeatIntervalCommandHandler extends CustomCommandHandler {
        HeartbeatIntervalCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof DecimalType || cmd instanceof QuantityType;
        }

        @Override
        protected double getValue(Command cmd) {
            int interval = getInterval(cmd);
            int increment = getParameterAsInteger("increment", -1);
            int preset = getParameterAsInteger("preset", 0);
            if (increment == -1) {
                logger.warn("{}: no increment parameter specified in command handler", nm());
            } else if (interval == -1) {
                logger.warn("{}: got unexpected heartbeat interval command: {}, ignoring request", nm(), cmd);
            } else {
                int value = (int) Math.floor(interval / increment); // round down
                return interval == preset ? 0x00 : Math.max(0x00, Math.min(value, 0xFF));
            }
            return -1;
        }

        private int getInterval(Command cmd) {
            if (cmd instanceof DecimalType time) {
                return time.intValue();
            } else if (cmd instanceof QuantityType<?> time) {
                return Objects.requireNonNullElse(time.toInvertibleUnit(Units.MINUTE), time).intValue();
            }
            return -1;
        }
    }

    /**
     * Motion sensor 2 heartbeat interval command handler
     */
    public static class MotionSensor2HeartbeatIntervalCommandHandler extends HeartbeatIntervalCommandHandler {
        MotionSensor2HeartbeatIntervalCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                int heartbeatInterval = (int) getValue(cmd);
                int lowBatteryThreshold = getInsteonDevice().getLastMsgValueAsInteger(FEATURE_LOW_BATTERY_THRESHOLD,
                        -1);
                if (heartbeatInterval != -1 && lowBatteryThreshold != -1) {
                    InsteonAddress address = getInsteonDevice().getAddress();
                    byte[] data = { (byte) 0x00, (byte) 0x09, (byte) lowBatteryThreshold, (byte) heartbeatInterval };
                    Msg msg = Msg.makeExtendedMessage(address, (byte) 0x2E, (byte) 0x00, data, true);
                    feature.sendRequest(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: sent heartbeat interval {} request to {}", nm(),
                                HexUtils.getHexString(heartbeatInterval), address);
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
     * Siren on/off command handler
     */
    public static class SirenOnOffCommandHandler extends SwitchOnOffCommandHandler {
        SirenOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.OFF.equals(cmd) ? 0x00 : 0x7F; // no delay + max duration (127 seconds)
        }
    }

    /**
     * Siren armed command handler
     */
    public static class SirenArmedCommandHandler extends OpFlagsCommandHandler {
        SirenArmedCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected byte[] getOpFlagData(Command cmd) {
            return OnOffType.ON.equals(cmd) ? new byte[] { (byte) 0x01 } : new byte[0];
        }

        @Override
        protected boolean isStateRetrievable() {
            return true;
        }
    }

    /**
     * Siren alert duration command handler
     */
    public static class SirenAlertDurationCommandHandler extends CustomCommandHandler {
        SirenAlertDurationCommandHandler(DeviceFeature feature) {
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
                logger.debug("{}: unable to determine last value for {}", nm(), feature.getName());
            } else if (duration == -1) {
                logger.warn("{}: got unexpected siren alert duration cmd {}, ignoring request", nm(), cmd);
            } else {
                return value & 0x80 | duration;
            }
            return -1;
        }

        private int getDuration(Command cmd) {
            int duration = -1;
            if (cmd instanceof DecimalType time) {
                duration = time.intValue();
            } else if (cmd instanceof QuantityType<?> time) {
                duration = Objects.requireNonNullElse(time.toInvertibleUnit(Units.SECOND), time).intValue();
            }
            return duration != -1 ? Math.max(0, Math.min(duration, 127)) : -1; // allowed range 0-127 seconds
        }
    }

    /**
     * Siren alert type command handler
     */
    public static class SirenAlertTypeCommandHandler extends CustomCommandHandler {
        SirenAlertTypeCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected double getValue(Command cmd) {
            try {
                String type = ((StringType) cmd).toString();
                return SirenAlertType.valueOf(type).getValue();
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected alert type command: {}, ignoring request", nm(), cmd);
                return -1;

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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                int level = getLevel(cmd);
                int userData2 = getParameterAsInteger("d2", -1);
                if (userData2 != -1) {
                    // set led on/off
                    setLEDOnOff(config, OnOffType.from(level > 0));
                    // set led brightness level
                    InsteonAddress address = getInsteonDevice().getAddress();
                    byte[] data = { (byte) 0x01, (byte) userData2, (byte) level };
                    boolean setCRC = getInsteonDevice().getInsteonEngine().supportsChecksum();
                    Msg msg = Msg.makeExtendedMessage(address, (byte) 0x2E, (byte) 0x00, data, setCRC);
                    feature.sendRequest(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: sent led brightness level {} request to {}", nm(),
                                HexUtils.getHexString(level), address);
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
            if (cmd instanceof PercentType percent) {
                level = percent.intValue();
            } else {
                level = OnOffType.OFF.equals(cmd) ? 0 : 100;
            }
            return (int) Math.round(level * 127.0 / 100);
        }

        private void setLEDOnOff(InsteonChannelConfiguration config, Command cmd) {
            State state = getInsteonDevice().getFeatureState(FEATURE_LED_ON_OFF);
            if (!((State) cmd).equals(state)) {
                feature.handleCommand(config, cmd);
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                int cmd1 = getParameterAsInteger("cmd1", -1);
                if (cmd1 != -1) {
                    InsteonAddress address = getInsteonDevice().getAddress();
                    Msg msg = Msg.makeStandardMessage(address, (byte) cmd1, (byte) 0x00);
                    feature.sendRequest(msg);
                    logger.debug("{}: sent {} request to {}", nm(), feature.getName(), address);
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                int cmd2 = getOpFlagCommand(cmd);
                if (cmd2 != -1) {
                    byte[] data = getOpFlagData(cmd);
                    Msg msg = getOpFlagMessage(cmd2, data);
                    feature.sendRequest(msg);
                    logger.debug("{}: sent op flag {} {} request to {}", nm(), feature.getName(), cmd,
                            getInsteonDevice().getAddress());
                    // update state if not retrievable (e.g. stayAwake)
                    if (!isStateRetrievable()) {
                        feature.updateState((State) cmd);
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

        protected byte[] getOpFlagData(Command cmd) {
            return new byte[0];
        }

        protected Msg getOpFlagMessage(int cmd2, byte[] data) throws FieldException, InvalidMessageTypeException {
            InsteonAddress address = getInsteonDevice().getAddress();
            if (getInsteonDevice().getInsteonEngine().supportsChecksum()) {
                return Msg.makeExtendedMessage(address, (byte) 0x20, (byte) cmd2, data, true);
            } else {
                return Msg.makeStandardMessage(address, (byte) 0x20, (byte) cmd2);
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                for (Map.Entry<Integer, String> entry : getOpFlagCommands(cmd).entrySet()) {
                    Msg msg = getOpFlagMessage(entry.getKey(), new byte[0]);
                    feature.sendRequest(msg);
                    logger.debug("{}: sent op flag {} request to {}", nm(), entry.getValue(),
                            getInsteonDevice().getAddress());
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                RampRate rampRate = getRampRate(cmd);
                if (rampRate != null) {
                    InsteonAddress address = getInsteonDevice().getAddress();
                    byte[] data = { (byte) feature.getGroup(), (byte) 0x05, (byte) rampRate.getValue() };
                    boolean setCRC = getInsteonDevice().getInsteonEngine().supportsChecksum();
                    Msg msg = Msg.makeExtendedMessage(address, (byte) 0x2E, (byte) 0x00, data, setCRC);
                    feature.sendRequest(msg);
                    logger.debug("{}: sent ramp time {} to {}", nm(), rampRate, address);
                } else {
                    logger.warn("{}: got unexpected ramp rate command {}, ignoreing request", nm(), cmd);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        private @Nullable RampRate getRampRate(Command cmd) {
            double rampTime = -1;
            if (cmd instanceof DecimalType time) {
                rampTime = time.doubleValue();
            } else if (cmd instanceof QuantityType<?> time) {
                rampTime = Objects.requireNonNullElse(time.toInvertibleUnit(Units.SECOND), time).doubleValue();
            }
            return rampTime != -1 ? RampRate.fromTime(rampTime) : null;
        }
    }

    /**
     * FanLinc fan speed command handler
     */
    public static class FanLincFanSpeedCommandHandler extends OnOffCommandHandler {
        FanLincFanSpeedCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            try {
                String speed = ((StringType) cmd).toString();
                return FanLincFanSpeed.valueOf(speed) == FanLincFanSpeed.OFF ? 0x13 : 0x11;
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected fan speed command: {}, ignoring request", nm(), cmd);
                return -1;
            }
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            try {
                String speed = ((StringType) cmd).toString();
                return FanLincFanSpeed.valueOf(speed).getValue();
            } catch (IllegalArgumentException e) {
                return -1;
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                double duration = getDuration(cmd);
                if (duration != -1) {
                    InsteonAddress address = getInsteonDevice().getAddress();
                    int prescaler = 1;
                    int delay = (int) Math.round(duration * 10);
                    if (delay > 255) {
                        prescaler = (int) Math.ceil(delay / 255.0);
                        delay = (int) Math.round(delay / (double) prescaler);
                    }
                    boolean setCRC = getInsteonDevice().getInsteonEngine().supportsChecksum();
                    // define ext command message to set momentary duration delay
                    Msg delayMsg = Msg.makeExtendedMessage(address, (byte) 0x2E, (byte) 0x00,
                            new byte[] { (byte) 0x01, (byte) 0x06, (byte) delay }, setCRC);
                    // define ext command message to set momentary duration prescaler
                    Msg prescalerMsg = Msg.makeExtendedMessage(address, (byte) 0x2E, (byte) 0x00,
                            new byte[] { (byte) 0x01, (byte) 0x07, (byte) prescaler }, setCRC);
                    // send requests
                    feature.sendRequest(delayMsg);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: sent momentary duration delay {} request to {}", nm(),
                                HexUtils.getHexString(delay), address);
                    }
                    feature.sendRequest(prescalerMsg);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: sent momentary duration prescaler {} request to {}", nm(),
                                HexUtils.getHexString(prescaler), address);
                    }
                } else {
                    logger.warn("{}: got unexpected momentary duration command {}, ignoring request", nm(), cmd);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("{}: invalid message: ", nm(), e);
            } catch (FieldException e) {
                logger.warn("{}: command send message creation error ", nm(), e);
            }
        }

        private double getDuration(Command cmd) {
            if (cmd instanceof DecimalType time) {
                return time.doubleValue();
            } else if (cmd instanceof QuantityType<?> time) {
                return Objects.requireNonNullElse(time.toInvertibleUnit(Units.SECOND), time).doubleValue();
            }
            return -1;
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
            try {
                String mode = ((StringType) cmd).toString();
                switch (IOLincRelayMode.valueOf(mode)) {
                    case LATCHING:
                        commands.put(0x07, "momentary mode OFF");
                        break;
                    case MOMENTARY_A:
                        commands.put(0x06, "momentary mode ON");
                        commands.put(0x13, "momentary trigger on/off OFF");
                        commands.put(0x15, "momentary sensor follow OFF");
                        break;
                    case MOMENTARY_B:
                        commands.put(0x06, "momentary mode ON");
                        commands.put(0x12, "momentary trigger on/off ON");
                        commands.put(0x15, "momentary sensor follow OFF");
                        break;
                    case MOMENTARY_C:
                        commands.put(0x06, "momentary mode ON");
                        commands.put(0x13, "momentary trigger on/off OFF");
                        commands.put(0x14, "momentary sensor follow ON");
                        break;
                }
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected relay mode command: {}, ignoring request", nm(), cmd);
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
            try {
                String mode = ((StringType) cmd).toString();
                switch (MicroModuleOpMode.valueOf(mode)) {
                    case LATCHING:
                        commands.put(0x20, "momentary line OFF");
                        break;
                    case SINGLE_MOMENTARY:
                        commands.put(0x21, "momentary line ON");
                        commands.put(0x1E, "dual line OFF");
                        break;
                    case DUAL_MOMENTARY:
                        commands.put(0x21, "momentary line ON");
                        commands.put(0x1E, "dual line ON");
                        break;
                }
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected operation mode command: {}, ignoring request", nm(), cmd);
            }
            return commands;
        }
    }

    /**
     * Remote scene button config command handler
     */
    public static class RemoteSceneButtonConfigCommandHandler extends MultiOpFlagsCommandHandler {
        RemoteSceneButtonConfigCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected Map<Integer, String> getOpFlagCommands(Command cmd) {
            Map<Integer, String> commands = new HashMap<>();
            try {
                String mode = ((StringType) cmd).toString();
                switch (RemoteSceneButtonConfig.valueOf(mode)) {
                    case BUTTON_4:
                        commands.put(0x0F, "grouped ON");
                        commands.put(0x09, "toggle off ON");
                        break;
                    case BUTTON_8_ALWAYS_ON:
                        commands.put(0x0E, "grouped OFF");
                        commands.put(0x09, "toggle off ON");
                        break;
                    case BUTTON_8_TOGGLE:
                        commands.put(0x0E, "grouped OFF");
                        commands.put(0x08, "toggle off OFF");
                        break;
                }
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected button config command: {}, ignoring request", nm(), cmd);
            }
            return commands;
        }
    }

    /**
     * Remote switch button config command handler
     */
    public static class RemoteSwitchButtonConfigCommandHandler extends MultiOpFlagsCommandHandler {
        RemoteSwitchButtonConfigCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected Map<Integer, String> getOpFlagCommands(Command cmd) {
            Map<Integer, String> commands = new HashMap<>();
            try {
                String mode = ((StringType) cmd).toString();
                switch (RemoteSwitchButtonConfig.valueOf(mode)) {
                    case BUTTON_1:
                        commands.put(0x0F, "grouped ON");
                        commands.put(0x09, "toggle off ON");
                        break;
                    case BUTTON_2_ALWAYS_ON:
                        commands.put(0x0E, "grouped OFF");
                        commands.put(0x09, "toggle off ON");
                        break;
                    case BUTTON_2_TOGGLE:
                        commands.put(0x0E, "grouped OFF");
                        commands.put(0x08, "toggle off OFF");
                        break;
                }
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected button config command: {}, ignoring request", nm(), cmd);
            }
            return commands;
        }
    }

    /**
     * Sprinkler valve on/off command handler
     */
    public static class SprinklerValveOnOffCommandHandler extends OnOffCommandHandler {
        SprinklerValveOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return OnOffType.ON.equals(cmd) ? 0x40 : 0x41;
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return getParameterAsInteger("valve", -1);
        }
    }

    /**
     * Sprinkler program on/off command handler
     */
    public static class SprinklerProgramOnOffCommandHandler extends OnOffCommandHandler {
        SprinklerProgramOnOffCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof PlayPauseType;
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return PlayPauseType.PLAY.equals(cmd) ? 0x42 : 0x43;
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return getParameterAsInteger("program", -1);
        }
    }

    /**
     * Sprinkler program next/previous command handler
     */
    public static class SprinklerProgramNextPreviousCommandHandler extends OnOffCommandHandler {
        SprinklerProgramNextPreviousCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof NextPreviousType;
        }

        @Override
        protected int getCommandCode(InsteonChannelConfiguration config, Command cmd) {
            return 0x44; // sprinkler control
        }

        @Override
        protected int getLevel(InsteonChannelConfiguration config, Command cmd) {
            return NextPreviousType.NEXT.equals(cmd) ? 0x05 : 0x06; // skip forward or back
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
            try {
                String mode = ((StringType) cmd).toString();
                return ThermostatFanMode.valueOf(mode).getValue();
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected fan mode command: {}, ignoring request", nm(), cmd);
                return -1;
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
            try {
                String mode = ((StringType) cmd).toString();
                return ThermostatSystemMode.valueOf(mode).getValue();
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected system mode command: {}, ignoring request", nm(), cmd);
                return -1;
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
            try {
                String mode = ((StringType) cmd).toString();
                return VenstarSystemMode.valueOf(mode).getValue();
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected system mode command: {}, ignoring request", nm(), cmd);
                return -1;
            }
        }
    }

    /**
     * Thermostat temperature scale command handler
     */
    public static class ThermostatTemperatureScaleCommandHandler extends CustomBitmaskCommandHandler {
        ThermostatTemperatureScaleCommandHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean canHandle(Command cmd) {
            return cmd instanceof StringType;
        }

        @Override
        protected @Nullable Boolean shouldSetBit(Command cmd) {
            try {
                String scale = ((StringType) cmd).toString();
                return ThermostatTemperatureScale.valueOf(scale) == ThermostatTemperatureScale.CELSIUS;
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected temperature scale command: {}, ignoring request", nm(), cmd);
                return null;
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
        protected @Nullable Boolean shouldSetBit(Command cmd) {
            try {
                String format = ((StringType) cmd).toString();
                return ThermostatTimeFormat.from(format) == ThermostatTimeFormat.HR_24;
            } catch (IllegalArgumentException e) {
                logger.warn("{}: got unexpected temperature format command: {}, ignoring request", nm(), cmd);
                return null;
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                InsteonAddress address = getInsteonDevice().getAddress();
                ZonedDateTime time = ZonedDateTime.now();
                byte[] data = { (byte) 0x02, (byte) (time.getDayOfWeek().getValue() % 7), (byte) time.getHour(),
                        (byte) time.getMinute(), (byte) time.getSecond() };
                Msg msg = Msg.makeExtendedMessageCRC2(address, (byte) 0x2E, (byte) 0x02, data);
                feature.sendRequest(msg);
                logger.debug("{}: sent set time data request to {}", nm(), address);
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                Msg msg = getIMMessage(cmd);
                feature.sendRequest(msg);
                logger.debug("{}: sent {} request to {}", nm(), cmd, getInsteonModem().getAddress());
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            // set led control
            setLEDControl(config);
            // set led on/off
            super.handleCommand(config, cmd);
            // update state since not retrievable
            feature.updateState((State) cmd);
        }

        @Override
        protected Msg getIMMessage(Command cmd) throws InvalidMessageTypeException, FieldException {
            return Msg.makeMessage(OnOffType.OFF.equals(cmd) ? "LEDOff" : "LEDOn");
        }

        private void setLEDControl(InsteonChannelConfiguration config) {
            State state = getInsteonModem().getFeatureState(FEATURE_LED_CONTROL);
            if (!OnOffType.ON.equals(state)) {
                feature.handleCommand(config, OnOffType.ON);
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                int bitmask = getBitmask(cmd);
                if (bitmask != -1) {
                    Msg msg = Msg.makeMessage("SetIMConfig");
                    msg.setByte("IMConfigurationFlags", (byte) bitmask);
                    feature.sendRequest(msg);
                    logger.debug("{}: sent {} request to {}", nm(), cmd, getInsteonModem().getAddress());
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
        public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
            try {
                X10Address address = getX10Device().getAddress();
                int cmdCode = getCommandCode(cmd, address.getHouseCode());
                Msg addrMsg = Msg.makeX10AddressMessage(address);
                feature.sendRequest(addrMsg);
                Msg cmdMsg = Msg.makeX10CommandMessage((byte) cmdCode);
                feature.sendRequest(cmdMsg);
                logger.debug("{}: sent {} request to {}", nm(), cmd, address);
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
            int cmdCode = OnOffType.OFF.equals(cmd) ? X10Command.OFF.code() : X10Command.ON.code();
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
            int level = ((PercentType) cmd).intValue() * 32 / 100;
            int levelCode = X10_LEVEL_CODES[level % 16];
            int cmdCode = level >= 16 ? X10Command.PRESET_DIM_2.code() : X10Command.PRESET_DIM_1.code();
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
            int cmdCode = IncreaseDecreaseType.INCREASE.equals(cmd) ? X10Command.BRIGHT.code() : X10Command.DIM.code();
            return houseCode << 4 | cmdCode;
        }
    }

    /**
     * Factory method to dermine if a command handler supports a given command type
     *
     * @param type the handler command type
     * @return true if handler supports command type, otherwise false
     */
    public static boolean supportsCommandType(String type) {
        return SUPPORTED_COMMAND_TYPES.contains(type);
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
            return null;
        }
    }
}
