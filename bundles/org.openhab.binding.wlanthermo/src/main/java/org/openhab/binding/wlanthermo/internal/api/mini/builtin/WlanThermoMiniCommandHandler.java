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
package org.openhab.binding.wlanthermo.internal.api.mini.builtin;

import java.awt.*;

import org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link WlanThermoMiniCommandHandler} is responsible for mapping the Commands to the respective data fields
 * of the API.
 *
 * @author Christian Schlipp - Initial contribution
 */
public class WlanThermoMiniCommandHandler {

    public State getState(ChannelUID channelUID, App app) {
        State state = null;
        if ("system".equals(channelUID.getGroupId())) {
            switch (channelUID.getIdWithoutGroup()) {
                case WlanThermoBindingConstants.SYSTEM_CPU_TEMP:
                    if (app.getCpuTemp() == null) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = new DecimalType(app.getCpuTemp());
                    }
                    break;
                case WlanThermoBindingConstants.SYSTEM_CPU_LOAD:
                    if (app.getCpuLoad() == null) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = new DecimalType(app.getCpuLoad());
                    }
                    break;
            }
        } else if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length()));
            if (channelId >= 0 && channelId <= 9) {
                Channel channel = app.getChannel();
                if (channel == null) {
                    return UnDefType.UNDEF;
                }
                Data data = channel.getData(channelId);
                switch (channelUID.getIdWithoutGroup()) {
                    case WlanThermoBindingConstants.CHANNEL_NAME:
                        state = new StringType(data.getName());
                        break;
                    case WlanThermoBindingConstants.CHANNEL_TEMP:
                        if (data.getState().equals("er")) {
                            state = UnDefType.UNDEF;
                        } else {
                            state = new DecimalType(data.getTemp());
                        }
                        break;
                    case WlanThermoBindingConstants.CHANNEL_MIN:
                        state = new DecimalType(data.getTempMin());
                        break;
                    case WlanThermoBindingConstants.CHANNEL_MAX:
                        state = new DecimalType(data.getTempMax());
                        break;
                    case WlanThermoBindingConstants.CHANNEL_ALARM_DEVICE:
                        state = OnOffType.from(data.getAlert());
                        break;
                    case WlanThermoBindingConstants.CHANNEL_ALARM_OPENHAB_HIGH:
                        if (!data.getState().equals("er") && data.getTemp() > data.getTempMax()) {
                            state = OnOffType.ON;
                        } else {
                            state = OnOffType.OFF;
                        }
                        break;
                    case WlanThermoBindingConstants.CHANNEL_ALARM_OPENHAB_LOW:
                        if (!data.getState().equals("er") && data.getTemp() < data.getTempMin()) {
                            state = OnOffType.ON;
                        } else {
                            state = OnOffType.OFF;
                        }
                        break;
                    case WlanThermoBindingConstants.CHANNEL_COLOR:
                        Color c = Color.decode(UtilMini.toHex(data.getColor()));
                        state = HSBType.fromRGB(c.getRed(), c.getGreen(), c.getBlue());
                        break;
                    case WlanThermoBindingConstants.CHANNEL_COLOR_NAME:
                        state = new StringType(data.getColor());
                        break;
                }
            }
        } else if (channelUID.getId().startsWith("pit")) {
            Pit pit;
            if (channelUID.getGroupId().equals("pit1")) {
                pit = app.getPit();
            } else if (channelUID.getGroupId().equals("pit2")) {
                pit = app.getPit2();
            } else {
                return UnDefType.UNDEF;
            }
            if (pit == null || !pit.getEnabled()) {
                return UnDefType.UNDEF;
            }
            switch (channelUID.getIdWithoutGroup()) {
                case WlanThermoBindingConstants.CHANNEL_PITMASTER_ENABLED:
                    state = OnOffType.from(pit.getEnabled());
                    break;
                case WlanThermoBindingConstants.CHANNEL_PITMASTER_CURRENT:
                    state = new DecimalType(pit.getCurrent());
                    break;
                case WlanThermoBindingConstants.CHANNEL_PITMASTER_SETPOINT:
                    state = new DecimalType(pit.getSetpoint());
                    break;
                case WlanThermoBindingConstants.CHANNEL_PITMASTER_DUTY_CYCLE:
                    state = new DecimalType(pit.getControlOut());
                    break;
                case WlanThermoBindingConstants.CHANNEL_PITMASTER_LID_OPEN:
                    state = OnOffType.from(pit.getOpenLid().equals("True"));
                    break;
                case WlanThermoBindingConstants.CHANNEL_PITMASTER_CHANNEL_ID:
                    state = new DecimalType(pit.getCh());
                    break;
            }
        }
        return state;
    }

    public String getTrigger(ChannelUID channelUID, App app) {
        String trigger = null;
        if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length())) - 1;
            if (channelId >= 0 && channelId <= 9) {
                Channel channel = app.getChannel();
                if (channel == null) {
                    return "";
                }
                Data data = channel.getData(channelId);
                switch (channelUID.getIdWithoutGroup()) {
                    case "alarm_openhab":
                        if (!data.getState().equals("er")) {
                            if (data.getTemp() > data.getTempMax()) {
                                trigger = WlanThermoBindingConstants.TRIGGER_ALARM_MAX;
                            } else if (data.getTemp() < data.getTempMin()) {
                                trigger = WlanThermoBindingConstants.TRIGGER_ALARM_MIN;
                            }
                        }
                }
            }
        }
        return trigger;
    }
}
