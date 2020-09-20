/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.awt.*;
import java.math.BigInteger;
import java.util.List;

import org.openhab.binding.wlanthermo.internal.api.nano.data.Channel;
import org.openhab.binding.wlanthermo.internal.api.nano.data.Data;
import org.openhab.binding.wlanthermo.internal.api.nano.data.Pm;
import org.openhab.binding.wlanthermo.internal.api.nano.data.System;
import org.openhab.binding.wlanthermo.internal.api.nano.settings.Settings;
import org.openhab.core.library.types.*;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link WlanThermoNanoCommandHandler} is responsible for mapping the Commands to the respective data fields
 * of the API.
 *
 * @author Christian Schlipp - Initial contribution
 */
public class WlanThermoNanoCommandHandler {

    public State getState(ChannelUID channelUID, Data data, Settings settings) {
        State state = null;
        System system = data.getSystem();
        List<Channel> channel = data.getChannel();
        if ("system".equals(channelUID.getGroupId()) && system != null) {
            switch (channelUID.getIdWithoutGroup()) {
                case SYSTEM_SOC:
                    state = new DecimalType(system.getSoc());
                    break;
                case SYSTEM_CHARGE:
                    state = OnOffType.from(system.getCharge());
                    break;
                case SYSTEM_RSSI_SIGNALSTRENGTH:
                    int dbm = system.getRssi();
                    if (dbm >= -80) {
                        state = new DecimalType(4);
                    } else if (dbm >= -95) {
                        state = new DecimalType(3);
                    } else if (dbm >= -105) {
                        state = new DecimalType(2);
                    } else {
                        state = new DecimalType(1);
                    }
                    break;
                case SYSTEM_RSSI:
                    state = new DecimalType(system.getRssi());
                    break;
            }
        } else if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length())) - 1;
            if (channel.size() > 0 && channelId <= channel.size()) {
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_NAME:
                        state = new StringType(channel.get(channelId).getName());
                        break;
                    case CHANNEL_TYP:
                        state = new StringType(settings.sensors.get(channel.get(channelId).getTyp()));
                        break;
                    case CHANNEL_TEMP:
                        if (channel.get(channelId).getTemp() == 999.0) {
                            state = UnDefType.UNDEF;
                        } else {
                            state = new DecimalType(channel.get(channelId).getTemp());
                        }
                        break;
                    case CHANNEL_MIN:
                        state = new DecimalType(channel.get(channelId).getMin());
                        break;
                    case CHANNEL_MAX:
                        state = new DecimalType(channel.get(channelId).getMax());
                        break;
                    case CHANNEL_ALARM_DEVICE:
                        state = OnOffType.from(BigInteger.valueOf(channel.get(channelId).getAlarm()).testBit(1));
                        break;
                    case CHANNEL_ALARM_PUSH:
                        state = OnOffType.from(BigInteger.valueOf(channel.get(channelId).getAlarm()).testBit(0));
                        break;
                    case CHANNEL_ALARM_OPENHAB_HIGH:
                        if (channel.get(channelId).getTemp() != 999
                                && channel.get(channelId).getTemp() > channel.get(channelId).getMax()) {
                            state = OnOffType.ON;
                        } else {
                            state = OnOffType.OFF;
                        }
                        break;
                    case CHANNEL_ALARM_OPENHAB_LOW:
                        if (channel.get(channelId).getTemp() != 999
                                && channel.get(channelId).getTemp() < channel.get(channelId).getMin()) {
                            state = OnOffType.ON;
                        } else {
                            state = OnOffType.OFF;
                        }
                        break;
                    case CHANNEL_COLOR:
                        String color = channel.get(channelId).getColor();
                        if (color != null && !color.isEmpty()) {
                            Color c = Color.decode(color);
                            state = HSBType.fromRGB(c.getRed(), c.getGreen(), c.getBlue());
                        }
                        break;
                    case CHANNEL_COLOR_NAME:
                        String colorHex = channel.get(channelId).getColor();
                        if (colorHex != null && !colorHex.isEmpty()) {
                            state = new StringType(UtilNano.toColorName(colorHex));
                        }
                        break;
                }
            }
        } else if (channelUID.getId().startsWith("pit1")) {
            if (data.getPitmaster() != null && data.getPitmaster().getPm() != null
                    && data.getPitmaster().getPm().size() > 0) {
                Pm pm = data.getPitmaster().getPm().get(0);
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_PITMASTER_CHANNEL_ID:
                        state = new DecimalType(pm.getChannel());
                        break;
                    case CHANNEL_PITMASTER_PIDPROFILE:
                        state = new DecimalType(pm.getPid());
                        break;
                    case CHANNEL_PITMASTER_DUTY_CYCLE:
                        state = new DecimalType(pm.getValue());
                        break;
                    case CHANNEL_PITMASTER_SETPOINT:
                        state = new DecimalType(pm.getSet());
                        break;
                    case CHANNEL_PITMASTER_STATE:
                        state = new StringType(pm.getTyp());
                }
            } else {
                return UnDefType.UNDEF;
            }
        }
        return state;
    }

    public boolean setState(ChannelUID channelUID, Command command, Data data) {
        boolean success = false;
        List<Channel> channel = data.getChannel();
        if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length())) - 1;
            if (channel.size() > 0 && channelId <= channel.size()) {
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_NAME:
                        if (command instanceof StringType) {
                            channel.get(channelId).setName(command.toFullString());
                            success = true;
                        }
                        break;
                    case CHANNEL_MIN:
                        if (command instanceof QuantityType) {
                            channel.get(channelId).setMin(((QuantityType) command).doubleValue());
                            success = true;
                        }
                        break;
                    case CHANNEL_MAX:
                        if (command instanceof QuantityType) {
                            channel.get(channelId).setMax(((QuantityType) command).doubleValue());
                            success = true;
                        }
                        break;
                    case CHANNEL_ALARM_DEVICE:
                        if (command instanceof OnOffType) {
                            BigInteger value;
                            if (command == OnOffType.ON) {
                                value = BigInteger.valueOf(channel.get(channelId).getAlarm()).setBit(1);
                            } else {
                                value = BigInteger.valueOf(channel.get(channelId).getAlarm()).clearBit(1);
                            }
                            channel.get(channelId).setAlarm(value.intValue());
                            success = true;
                        }
                        break;
                    case CHANNEL_ALARM_PUSH:
                        if (command instanceof OnOffType) {
                            BigInteger value;
                            if (command == OnOffType.ON) {
                                value = BigInteger.valueOf(channel.get(channelId).getAlarm()).setBit(0);
                            } else {
                                value = BigInteger.valueOf(channel.get(channelId).getAlarm()).clearBit(0);
                            }
                            channel.get(channelId).setAlarm(value.intValue());
                            success = true;
                        }
                        break;
                    case CHANNEL_COLOR_NAME:
                        if (command instanceof StringType) {
                            channel.get(channelId).setColor(UtilNano.toHex(((StringType) command).toString()));
                            success = true;
                        }
                        break;
                }
            }
        } else if (channelUID.getId().equals("pit1")) {
            if (data.getPitmaster() != null && data.getPitmaster().getPm() != null
                    && data.getPitmaster().getPm().size() > 0) {
                Pm pm = data.getPitmaster().getPm().get(0);
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_PITMASTER_CHANNEL_ID:
                        pm.setChannel(((QuantityType) command).intValue());
                        success = true;
                        break;
                    case CHANNEL_PITMASTER_PIDPROFILE:
                        pm.setPid(((QuantityType) command).intValue());
                        success = true;
                        break;
                    case CHANNEL_PITMASTER_SETPOINT:
                        pm.setSet(((QuantityType) command).doubleValue());
                        success = true;
                        break;
                    case CHANNEL_PITMASTER_STATE:
                        String state = ((StringType) command).toString();
                        if (state.equalsIgnoreCase("off") || state.equalsIgnoreCase("manual")
                                || state.equalsIgnoreCase("auto")) {
                            pm.setTyp(state);
                            success = true;
                        }
                }
            }
        }
        return success;
    }

    public String getTrigger(ChannelUID channelUID, Data data) {
        String trigger = null;
        List<Channel> channel = data.getChannel();
        if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length())) - 1;
            if (channel.size() > 0 && channelId <= channel.size()) {
                if (CHANNEL_ALARM_OPENHAB.equals(channelUID.getIdWithoutGroup())) {
                    if (channel.get(channelId).getTemp() != 999) {
                        if (channel.get(channelId).getTemp() > channel.get(channelId).getMax()) {
                            trigger = TRIGGER_ALARM_MAX;
                        } else if (channel.get(channelId).getTemp() < channel.get(channelId).getMin()) {
                            trigger = TRIGGER_ALARM_MIN;
                        }
                    }
                }
            }
        }
        return trigger;
    }
}
