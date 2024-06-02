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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSensorSettingsRequestPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetTemperaturePacket;
import org.openhab.binding.velbus.internal.packets.VelbusThermostatModePacket;
import org.openhab.binding.velbus.internal.packets.VelbusThermostatOperatingModePacket;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public abstract class VelbusThermostatHandler extends VelbusTemperatureSensorHandler {
    private static final double THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION = 0.5;

    private static final StringType OPERATING_MODE_HEATING = new StringType("HEATING");
    private static final StringType OPERATING_MODE_COOLING = new StringType("COOLING");

    private static final byte OPERATING_MODE_MASK = (byte) 0x80;
    private static final byte COOLING_MODE_MASK = (byte) 0x80;

    private static final StringType MODE_COMFORT = new StringType("COMFORT");
    private static final StringType MODE_DAY = new StringType("DAY");
    private static final StringType MODE_NIGHT = new StringType("NIGHT");
    private static final StringType MODE_SAFE = new StringType("SAFE");

    private static final byte MODE_MASK = (byte) 0x70;
    private static final byte COMFORT_MODE_MASK = (byte) 0x40;
    private static final byte DAY_MODE_MASK = (byte) 0x20;
    private static final byte NIGHT_MODE_MASK = (byte) 0x10;

    private final ChannelUID currentTemperatureSetpointChannel = new ChannelUID(thing.getUID(),
            CHANNEL_GROUP_THERMOSTAT, CHANNEL_THERMOSTAT_CURRENT_TEMPERATURE);
    private final ChannelUID heatingModeComfortTemperatureSetpointChannel = new ChannelUID(thing.getUID(),
            CHANNEL_GROUP_THERMOSTAT, CHANNEL_THERMOSTAT_HEATING_COMFORT);
    private final ChannelUID heatingModeDayTemperatureSetpointChannel = new ChannelUID(thing.getUID(),
            CHANNEL_GROUP_THERMOSTAT, CHANNEL_THERMOSTAT_HEATING_DAY);
    private final ChannelUID heatingModeNightTemperatureSetpointChannel = new ChannelUID(thing.getUID(),
            CHANNEL_GROUP_THERMOSTAT, CHANNEL_THERMOSTAT_HEATING_NIGHT);
    private final ChannelUID heatingModeAntifrostTemperatureSetpointChannel = new ChannelUID(thing.getUID(),
            CHANNEL_GROUP_THERMOSTAT, CHANNEL_THERMOSTAT_HEATING_ANTI_FROST);
    private final ChannelUID coolingModeComfortTemperatureSetpointChannel = new ChannelUID(thing.getUID(),
            CHANNEL_GROUP_THERMOSTAT, CHANNEL_THERMOSTAT_COOLING_COMFORT);
    private final ChannelUID coolingModeDayTemperatureSetpointChannel = new ChannelUID(thing.getUID(),
            CHANNEL_GROUP_THERMOSTAT, CHANNEL_THERMOSTAT_COOLING_DAY);
    private final ChannelUID coolingModeNightTemperatureSetpointChannel = new ChannelUID(thing.getUID(),
            CHANNEL_GROUP_THERMOSTAT, CHANNEL_THERMOSTAT_COOLING_NIGHT);
    private final ChannelUID coolingModeSafeTemperatureSetpointChannel = new ChannelUID(thing.getUID(),
            CHANNEL_GROUP_THERMOSTAT, CHANNEL_THERMOSTAT_COOLING_SAFE);
    private final ChannelUID operatingModeChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_THERMOSTAT,
            CHANNEL_THERMOSTAT_OPERATING_MODE);
    private final ChannelUID modeChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_THERMOSTAT,
            CHANNEL_THERMOSTAT_MODE);
    private final ChannelUID heaterChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_THERMOSTAT,
            CHANNEL_THERMOSTAT_HEATER);
    private final ChannelUID boostChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_THERMOSTAT,
            CHANNEL_THERMOSTAT_BOOST);
    private final ChannelUID pumpChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_THERMOSTAT,
            CHANNEL_THERMOSTAT_PUMP);
    private final ChannelUID coolerChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_THERMOSTAT,
            CHANNEL_THERMOSTAT_COOLER);
    private final ChannelUID alarm1Channel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_THERMOSTAT,
            CHANNEL_THERMOSTAT_ALARM1);
    private final ChannelUID alarm2Channel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_THERMOSTAT,
            CHANNEL_THERMOSTAT_ALARM2);
    private final ChannelUID alarm3Channel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_THERMOSTAT,
            CHANNEL_THERMOSTAT_ALARM3);
    private final ChannelUID alarm4Channel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_THERMOSTAT,
            CHANNEL_THERMOSTAT_ALARM4);

    public VelbusThermostatHandler(Thing thing, int numberOfSubAddresses, ChannelUID temperatureChannel) {
        super(thing, numberOfSubAddresses, temperatureChannel);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (isThermostatChannel(channelUID) && command instanceof RefreshType) {
            VelbusSensorSettingsRequestPacket packet = new VelbusSensorSettingsRequestPacket(
                    getModuleAddress().getAddress());

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else if (isThermostatChannel(channelUID)
                && (command instanceof QuantityType<?> || command instanceof DecimalType)) {
            byte temperatureVariable = determineTemperatureVariable(channelUID);
            QuantityType<?> temperatureInDegreesCelcius = (command instanceof QuantityType<?> qt)
                    ? qt.toUnit(SIUnits.CELSIUS)
                    : new QuantityType<>(((DecimalType) command), SIUnits.CELSIUS);

            if (temperatureInDegreesCelcius != null) {
                byte temperature = convertToTwoComplementByte(temperatureInDegreesCelcius.doubleValue(),
                        THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION);

                VelbusSetTemperaturePacket packet = new VelbusSetTemperaturePacket(getModuleAddress().getAddress(),
                        temperatureVariable, temperature);

                byte[] packetBytes = packet.getBytes();
                velbusBridgeHandler.sendPacket(packetBytes);
            }
        } else if (channelUID.equals(operatingModeChannel) && command instanceof StringType stringCommand) {
            byte commandByte = stringCommand.equals(OPERATING_MODE_HEATING) ? COMMAND_SET_HEATING_MODE
                    : COMMAND_SET_COOLING_MODE;

            VelbusThermostatOperatingModePacket packet = new VelbusThermostatOperatingModePacket(
                    getModuleAddress().getAddress(), commandByte);

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else if (channelUID.equals(modeChannel) && command instanceof StringType stringCommand) {
            byte commandByte = COMMAND_SWITCH_TO_SAFE_MODE;
            if (stringCommand.equals(MODE_COMFORT)) {
                commandByte = COMMAND_SWITCH_TO_COMFORT_MODE;
            } else if (stringCommand.equals(MODE_DAY)) {
                commandByte = COMMAND_SWITCH_TO_DAY_MODE;
            } else if (stringCommand.equals(MODE_NIGHT)) {
                commandByte = COMMAND_SWITCH_TO_NIGHT_MODE;
            }

            VelbusThermostatModePacket packet = new VelbusThermostatModePacket(getModuleAddress().getAddress(),
                    commandByte);

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else {
            logger.debug("The command '{}' is not supported by this handler.", command.getClass());
        }
    }

    @Override
    public boolean onPacketReceived(byte[] packet) {
        if (!super.onPacketReceived(packet)) {
            return false;
        }

        logger.trace("onPacketReceived() was called");

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte address = packet[2];
            byte command = packet[4];

            if (command == COMMAND_TEMP_SENSOR_SETTINGS_PART1 && packet.length >= 9) {
                byte currentTemperatureSetByte = packet[5];
                byte heatingModeComfortTemperatureSetByte = packet[6];
                byte heatingModeDayTemperatureSetByte = packet[7];
                byte heatingModeNightTemperatureSetByte = packet[8];
                byte heatingModeAntiFrostTemperatureSetByte = packet[9];

                double currentTemperatureSet = convertFromTwoComplementByte(currentTemperatureSetByte,
                        THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION);
                double heatingModeComfortTemperatureSet = convertFromTwoComplementByte(
                        heatingModeComfortTemperatureSetByte, THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION);
                double heatingModeDayTemperatureSet = convertFromTwoComplementByte(heatingModeDayTemperatureSetByte,
                        THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION);
                double heatingModeNightTemperatureSet = convertFromTwoComplementByte(heatingModeNightTemperatureSetByte,
                        THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION);
                double heatingModeAntiFrostTemperatureSet = convertFromTwoComplementByte(
                        heatingModeAntiFrostTemperatureSetByte, THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION);

                updateState(currentTemperatureSetpointChannel,
                        new QuantityType<>(currentTemperatureSet, SIUnits.CELSIUS));
                updateState(heatingModeComfortTemperatureSetpointChannel,
                        new QuantityType<>(heatingModeComfortTemperatureSet, SIUnits.CELSIUS));
                updateState(heatingModeDayTemperatureSetpointChannel,
                        new QuantityType<>(heatingModeDayTemperatureSet, SIUnits.CELSIUS));
                updateState(heatingModeNightTemperatureSetpointChannel,
                        new QuantityType<>(heatingModeNightTemperatureSet, SIUnits.CELSIUS));
                updateState(heatingModeAntifrostTemperatureSetpointChannel,
                        new QuantityType<>(heatingModeAntiFrostTemperatureSet, SIUnits.CELSIUS));
            } else if (command == COMMAND_TEMP_SENSOR_SETTINGS_PART2 && packet.length >= 8) {
                byte coolingModeComfortTemperatureSetByte = packet[5];
                byte coolingModeDayTemperatureSetByte = packet[6];
                byte coolingModeNightTemperatureSetByte = packet[7];
                byte coolingModeSafeTemperatureSetByte = packet[8];

                double coolingModeComfortTemperatureSet = convertFromTwoComplementByte(
                        coolingModeComfortTemperatureSetByte, THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION);
                double coolingModeDayTemperatureSet = convertFromTwoComplementByte(coolingModeDayTemperatureSetByte,
                        THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION);
                double coolingModeNightTemperatureSet = convertFromTwoComplementByte(coolingModeNightTemperatureSetByte,
                        THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION);
                double coolingModeSafeTemperatureSet = convertFromTwoComplementByte(coolingModeSafeTemperatureSetByte,
                        THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION);

                updateState(coolingModeComfortTemperatureSetpointChannel,
                        new QuantityType<>(coolingModeComfortTemperatureSet, SIUnits.CELSIUS));
                updateState(coolingModeDayTemperatureSetpointChannel,
                        new QuantityType<>(coolingModeDayTemperatureSet, SIUnits.CELSIUS));
                updateState(coolingModeNightTemperatureSetpointChannel,
                        new QuantityType<>(coolingModeNightTemperatureSet, SIUnits.CELSIUS));
                updateState(coolingModeSafeTemperatureSetpointChannel,
                        new QuantityType<>(coolingModeSafeTemperatureSet, SIUnits.CELSIUS));
            } else if (command == COMMAND_TEMP_SENSOR_STATUS && packet.length >= 9) {
                byte operatingMode = packet[5];
                byte targetTemperature = packet[9];

                if ((operatingMode & OPERATING_MODE_MASK) == COOLING_MODE_MASK) {
                    updateState(operatingModeChannel, OPERATING_MODE_COOLING);
                } else {
                    updateState(operatingModeChannel, OPERATING_MODE_HEATING);
                }

                if ((operatingMode & MODE_MASK) == COMFORT_MODE_MASK) {
                    updateState(modeChannel, MODE_COMFORT);
                } else if ((operatingMode & MODE_MASK) == DAY_MODE_MASK) {
                    updateState(modeChannel, MODE_DAY);
                } else if ((operatingMode & MODE_MASK) == NIGHT_MODE_MASK) {
                    updateState(modeChannel, MODE_NIGHT);
                } else {
                    updateState(modeChannel, MODE_SAFE);
                }

                double targetTemperatureValue = convertFromTwoComplementByte(targetTemperature,
                        THERMOSTAT_TEMPERATURE_SETPOINT_RESOLUTION);
                updateState(currentTemperatureSetpointChannel,
                        new QuantityType<>(targetTemperatureValue, SIUnits.CELSIUS));
            } else if (command == COMMAND_PUSH_BUTTON_STATUS) {
                ThingTypeUID thingTypeUID = this.thing.getThingTypeUID();
                if (thingTypeUID.equals(THING_TYPE_VMBELO) || thingTypeUID.equals(THING_TYPE_VMBGPO)
                        || thingTypeUID.equals(THING_TYPE_VMBGPOD) || thingTypeUID.equals(THING_TYPE_VMBGPOD_2)
                        || thingTypeUID.equals(THING_TYPE_VMBGPO_20)) {
                    // modules VMBELO, VMBGPO, VMBGPOD, VMBGPOD_2, VMBGPO_20 use sub-address 4 for sensor
                    if (address == this.getModuleAddress().getSubAddresses()[3]) {
                        byte outputChannelsJustActivated = packet[5];
                        byte outputChannelsJustDeactivated = packet[6];

                        triggerThermostatChannels(outputChannelsJustActivated, CommonTriggerEvents.PRESSED);
                        triggerThermostatChannels(outputChannelsJustDeactivated, CommonTriggerEvents.RELEASED);
                    }
                    // modules VMBEL1, VMBEL2, VMBEL4, VMBELPIR, VMBGP1, VMBGP1-2, VMBGP2, VMBGP2-2, VMBGP4, VMBGP4-2,
                    // VMBGP4PIR, VMBGP4PIR-2, VMBEL1-20, VMBEL2-20, VMBEL4-20, VMBELO-20, VMBGP1-20, VMBGP2-20,
                    // VMBGP4-20, VMBEL4PIR-20, VMBGP4PIR-20 use sub-address 1 for sensor, wich is not usable as push
                    // button
                } else if (thingTypeUID.equals(THING_TYPE_VMBEL1) || thingTypeUID.equals(THING_TYPE_VMBEL2)
                        || thingTypeUID.equals(THING_TYPE_VMBEL4) || thingTypeUID.equals(THING_TYPE_VMBELPIR)
                        || thingTypeUID.equals(THING_TYPE_VMBGP1) || thingTypeUID.equals(THING_TYPE_VMBGP1_2)
                        || thingTypeUID.equals(THING_TYPE_VMBGP2) || thingTypeUID.equals(THING_TYPE_VMBGP2_2)
                        || thingTypeUID.equals(THING_TYPE_VMBGP4) || thingTypeUID.equals(THING_TYPE_VMBGP4_2)
                        || thingTypeUID.equals(THING_TYPE_VMBGP4PIR) || thingTypeUID.equals(THING_TYPE_VMBGP4PIR_2)
                        || thingTypeUID.equals(THING_TYPE_VMBEL1_20) || thingTypeUID.equals(THING_TYPE_VMBEL2_20)
                        || thingTypeUID.equals(THING_TYPE_VMBEL4_20) || thingTypeUID.equals(THING_TYPE_VMBELO_20)
                        || thingTypeUID.equals(THING_TYPE_VMBGP1_20) || thingTypeUID.equals(THING_TYPE_VMBGP2_20)
                        || thingTypeUID.equals(THING_TYPE_VMBGP4_20) || thingTypeUID.equals(THING_TYPE_VMBEL4PIR_20)
                        || thingTypeUID.equals(THING_TYPE_VMBGP4PIR_20)) {
                    if (address != this.getModuleAddress().getAddress()) {
                        byte outputChannelsJustActivated = packet[5];
                        byte outputChannelsJustDeactivated = packet[6];

                        triggerThermostatChannels(outputChannelsJustActivated, CommonTriggerEvents.PRESSED);
                        triggerThermostatChannels(outputChannelsJustDeactivated, CommonTriggerEvents.RELEASED);
                    }
                }

            }
        }

        return true;
    }

    private void triggerThermostatChannels(byte outputChannels, String event) {
        if ((outputChannels & 0x01) == 0x01) {
            triggerChannel(heaterChannel, event);
        }
        if ((outputChannels & 0x02) == 0x02) {
            triggerChannel(boostChannel, event);
        }
        if ((outputChannels & 0x04) == 0x04) {
            triggerChannel(pumpChannel, event);
        }
        if ((outputChannels & 0x08) == 0x08) {
            triggerChannel(coolerChannel, event);
        }
        if ((outputChannels & 0x10) == 0x10) {
            triggerChannel(alarm1Channel, event);
        }
        if ((outputChannels & 0x20) == 0x20) {
            triggerChannel(alarm2Channel, event);
        }
        if ((outputChannels & 0x40) == 0x40) {
            triggerChannel(alarm3Channel, event);
        }
        if ((outputChannels & 0x80) == 0x80) {
            triggerChannel(alarm4Channel, event);
        }
    }

    protected boolean isThermostatChannel(ChannelUID channelUID) {
        return channelUID.equals(currentTemperatureSetpointChannel)
                || channelUID.equals(heatingModeComfortTemperatureSetpointChannel)
                || channelUID.equals(heatingModeDayTemperatureSetpointChannel)
                || channelUID.equals(heatingModeNightTemperatureSetpointChannel)
                || channelUID.equals(heatingModeAntifrostTemperatureSetpointChannel)
                || channelUID.equals(coolingModeComfortTemperatureSetpointChannel)
                || channelUID.equals(coolingModeDayTemperatureSetpointChannel)
                || channelUID.equals(coolingModeNightTemperatureSetpointChannel)
                || channelUID.equals(coolingModeSafeTemperatureSetpointChannel)
                || channelUID.equals(operatingModeChannel) || channelUID.equals(modeChannel);
    }

    protected byte determineTemperatureVariable(ChannelUID channelUID) {
        if (channelUID.equals(currentTemperatureSetpointChannel)) {
            return 0x00;
        } else if (channelUID.equals(heatingModeComfortTemperatureSetpointChannel)) {
            return 0x01;
        } else if (channelUID.equals(heatingModeDayTemperatureSetpointChannel)) {
            return 0x02;
        } else if (channelUID.equals(heatingModeNightTemperatureSetpointChannel)) {
            return 0x03;
        } else if (channelUID.equals(heatingModeAntifrostTemperatureSetpointChannel)) {
            return 0x04;
        } else if (channelUID.equals(coolingModeComfortTemperatureSetpointChannel)) {
            return 0x07;
        } else if (channelUID.equals(coolingModeDayTemperatureSetpointChannel)) {
            return 0x08;
        } else if (channelUID.equals(coolingModeNightTemperatureSetpointChannel)) {
            return 0x09;
        } else if (channelUID.equals(coolingModeSafeTemperatureSetpointChannel)) {
            return 0x0A;
        } else {
            throw new IllegalArgumentException("The given channelUID is not a thermostat channel: " + channelUID);
        }
    }

    protected double convertFromTwoComplementByte(byte value, double resolution) {
        return ((value & 0x80) == 0x00) ? value * resolution : ((value & 0x7F) - 0x80) * resolution;
    }

    protected byte convertToTwoComplementByte(double value, double resolution) {
        return (byte) (value / resolution);
    }
}
