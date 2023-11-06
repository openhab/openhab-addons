/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.wlanthermo.internal.api.nano;

import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.*;
import static org.openhab.binding.wlanthermo.internal.WlanThermoUtil.requireNonNull;

import java.awt.Color;
import java.math.BigInteger;
import java.util.List;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wlanthermo.internal.WlanThermoInputException;
import org.openhab.binding.wlanthermo.internal.WlanThermoUnknownChannelException;
import org.openhab.binding.wlanthermo.internal.api.nano.dto.data.Channel;
import org.openhab.binding.wlanthermo.internal.api.nano.dto.data.Data;
import org.openhab.binding.wlanthermo.internal.api.nano.dto.data.Pm;
import org.openhab.binding.wlanthermo.internal.api.nano.dto.data.System;
import org.openhab.binding.wlanthermo.internal.api.nano.dto.settings.Settings;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link WlanThermoNanoV1CommandHandler} is responsible for mapping the Commands to the respective data fields
 * of the API.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoNanoV1CommandHandler {

    public static State getState(ChannelUID channelUID, Data data, Settings settings)
            throws WlanThermoUnknownChannelException, WlanThermoInputException {
        String groupId = requireNonNull(channelUID.getGroupId());
        System system = data.getSystem();
        Unit<Temperature> unit = "F".equals(system.getUnit()) ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS;
        List<Channel> channelList = data.getChannel();

        if (SYSTEM.equals(groupId)) {
            switch (channelUID.getIdWithoutGroup()) {
                case SYSTEM_SOC:
                    return new DecimalType(system.getSoc());
                case SYSTEM_CHARGE:
                    return OnOffType.from(system.getCharge());
                case SYSTEM_RSSI_SIGNALSTRENGTH:
                    int dbm = system.getRssi();
                    if (dbm >= -80) {
                        return SIGNAL_STRENGTH_4;
                    } else if (dbm >= -95) {
                        return SIGNAL_STRENGTH_3;
                    } else if (dbm >= -105) {
                        return SIGNAL_STRENGTH_2;
                    } else {
                        return SIGNAL_STRENGTH_1;
                    }
                case SYSTEM_RSSI:
                    return new QuantityType<>(system.getRssi(), Units.DECIBEL_MILLIWATTS);
            }
        } else if (channelUID.getId().startsWith(CHANNEL_PREFIX)) {
            int channelId = Integer.parseInt(groupId.substring(CHANNEL_PREFIX.length())) - 1;
            if (!channelList.isEmpty() && channelId < channelList.size()) {
                Channel channel = channelList.get(channelId);
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_NAME:
                        return new StringType(channel.getName());
                    case CHANNEL_TYP:
                        return new StringType(settings.sensors.get(channel.getTyp()));
                    case CHANNEL_TEMP:
                        return channel.getTemp() == 999.0 ? UnDefType.UNDEF
                                : new QuantityType<>(channel.getTemp(), unit);
                    case CHANNEL_MIN:
                        return new QuantityType<>(channel.getMin(), unit);
                    case CHANNEL_MAX:
                        return new QuantityType<>(channel.getMax(), unit);
                    case CHANNEL_ALARM_DEVICE:
                        return OnOffType.from(BigInteger.valueOf(channel.getAlarm()).testBit(1));
                    case CHANNEL_ALARM_PUSH:
                        return OnOffType.from(BigInteger.valueOf(channel.getAlarm()).testBit(0));
                    case CHANNEL_ALARM_OPENHAB_HIGH:
                        if (channel.getTemp() != 999 && channel.getTemp() > channel.getMax()) {
                            return OnOffType.ON;
                        } else {
                            return OnOffType.OFF;
                        }
                    case CHANNEL_ALARM_OPENHAB_LOW:
                        if (channel.getTemp() != 999 && channel.getTemp() < channel.getMin()) {
                            return OnOffType.ON;
                        } else {
                            return OnOffType.OFF;
                        }
                    case CHANNEL_COLOR:
                        String color = channel.getColor();
                        if (color != null && !color.isEmpty()) {
                            Color c = Color.decode(color);
                            return HSBType.fromRGB(c.getRed(), c.getGreen(), c.getBlue());
                        } else {
                            return UnDefType.UNDEF;
                        }
                    case CHANNEL_COLOR_NAME:
                        String colorHex = channel.getColor();
                        if (colorHex != null && !colorHex.isEmpty()) {
                            return new StringType(WlanThermoNanoV1Util.toColorName(colorHex));
                        } else {
                            return UnDefType.UNDEF;
                        }
                }
            }
        } else if (channelUID.getId().startsWith(CHANNEL_PITMASTER_1)) {
            if (data.getPitmaster() != null && data.getPitmaster().getPm() != null
                    && !data.getPitmaster().getPm().isEmpty()) {
                Pm pm = data.getPitmaster().getPm().get(0);
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_PITMASTER_CHANNEL_ID:
                        return new DecimalType(pm.getChannel());
                    case CHANNEL_PITMASTER_PIDPROFILE:
                        return new DecimalType(pm.getPid());
                    case CHANNEL_PITMASTER_DUTY_CYCLE:
                        return new DecimalType(pm.getValue());
                    case CHANNEL_PITMASTER_SETPOINT:
                        return new QuantityType<>(pm.getSet(), unit);
                    case CHANNEL_PITMASTER_STATE:
                        return new StringType(pm.getTyp());
                }
            } else {
                return UnDefType.UNDEF;
            }
        }
        throw new WlanThermoUnknownChannelException(channelUID);
    }

    public static boolean setState(ChannelUID channelUID, Command command, Data data) {
        String groupId;
        try {
            groupId = requireNonNull(channelUID.getGroupId());
        } catch (WlanThermoInputException e) {
            return false;
        }

        List<Channel> channelList = data.getChannel();
        System system = data.getSystem();
        Unit<Temperature> unit = "F".equals(system.getUnit()) ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS;

        if (channelUID.getId().startsWith(CHANNEL_PREFIX)) {
            int channelId = Integer.parseInt(groupId.substring(CHANNEL_PREFIX.length())) - 1;
            if (!channelList.isEmpty() && channelId < channelList.size()) {
                Channel channel = channelList.get(channelId);
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_NAME:
                        if (command instanceof StringType) {
                            channel.setName(command.toFullString());
                            return true;
                        }
                        return false;
                    case CHANNEL_MIN:
                        if (command instanceof QuantityType quantityCommand) {
                            try {
                                channel.setMin(requireNonNull(quantityCommand.toUnit(unit)).doubleValue());
                                return true;
                            } catch (WlanThermoInputException ignore) {
                                return false;
                            }
                        }
                        return false;
                    case CHANNEL_MAX:
                        if (command instanceof QuantityType quantityCommand) {
                            try {
                                channel.setMax(requireNonNull(quantityCommand.toUnit(unit)).doubleValue());
                                return true;
                            } catch (WlanThermoInputException ignore) {
                                return false;
                            }
                        }
                        return false;
                    case CHANNEL_ALARM_DEVICE:
                        if (command instanceof OnOffType) {
                            BigInteger value;
                            if (command == OnOffType.ON) {
                                value = BigInteger.valueOf(channel.getAlarm()).setBit(1);
                            } else {
                                value = BigInteger.valueOf(channel.getAlarm()).clearBit(1);
                            }
                            channel.setAlarm(value.intValue());
                            return true;
                        }
                        return false;
                    case CHANNEL_ALARM_PUSH:
                        if (command instanceof OnOffType) {
                            BigInteger value;
                            if (command == OnOffType.ON) {
                                value = BigInteger.valueOf(channel.getAlarm()).setBit(0);
                            } else {
                                value = BigInteger.valueOf(channel.getAlarm()).clearBit(0);
                            }
                            channel.setAlarm(value.intValue());
                            return true;
                        }
                        return false;
                    case CHANNEL_COLOR_NAME:
                        if (command instanceof StringType stringCommand) {
                            channel.setColor(WlanThermoNanoV1Util.toHex(stringCommand.toString()));
                            return true;
                        }
                        return false;
                }
            }
        } else if (channelUID.getId().startsWith(CHANNEL_PITMASTER_1)) {
            if (data.getPitmaster() != null && data.getPitmaster().getPm() != null
                    && !data.getPitmaster().getPm().isEmpty()) {
                Pm pm = data.getPitmaster().getPm().get(0);
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_PITMASTER_CHANNEL_ID:
                        pm.setChannel(((DecimalType) command).intValue());
                        return true;
                    case CHANNEL_PITMASTER_PIDPROFILE:
                        pm.setPid(((DecimalType) command).intValue());
                        return true;
                    case CHANNEL_PITMASTER_SETPOINT:
                        try {
                            pm.setSet(requireNonNull(((QuantityType<?>) command).toUnit(unit)).doubleValue());
                            return true;
                        } catch (WlanThermoInputException ignore) {
                            return false;
                        }
                    case CHANNEL_PITMASTER_STATE:
                        String state = ((StringType) command).toString();
                        if ("off".equalsIgnoreCase(state) || "manual".equalsIgnoreCase(state)
                                || "auto".equalsIgnoreCase(state)) {
                            pm.setTyp(state);
                            return true;
                        }
                        return false;
                }
            }
        }
        return false;
    }

    public static String getTrigger(ChannelUID channelUID, Data data)
            throws WlanThermoUnknownChannelException, WlanThermoInputException {
        String groupId = requireNonNull(channelUID.getGroupId());
        List<Channel> channelList = data.getChannel();

        if (channelUID.getId().startsWith(CHANNEL_PREFIX)) {
            int channelId = Integer.parseInt(groupId.substring(CHANNEL_PREFIX.length())) - 1;
            if (!channelList.isEmpty() && channelId < channelList.size()) {
                Channel channel = channelList.get(channelId);
                if (CHANNEL_ALARM_OPENHAB.equals(channelUID.getIdWithoutGroup())) {
                    if (channel.getTemp() != 999) {
                        if (channel.getTemp() > channel.getMax()) {
                            return TRIGGER_ALARM_MAX;
                        } else if (channel.getTemp() < channel.getMin()) {
                            return TRIGGER_ALARM_MIN;
                        } else {
                            return TRIGGER_NONE;
                        }
                    }
                }
            }
        }
        throw new WlanThermoUnknownChannelException(channelUID);
    }
}
