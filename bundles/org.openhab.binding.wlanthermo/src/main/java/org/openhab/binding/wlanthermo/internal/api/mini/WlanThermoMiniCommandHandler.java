/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.wlanthermo.internal.api.mini;

import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.*;

import java.awt.*;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants;
import org.openhab.binding.wlanthermo.internal.api.mini.dto.builtin.*;
import org.openhab.core.library.types.*;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
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

    public static final String ERROR = "er";

    public static State getState(ChannelUID channelUID, App app) {
        State state = null;

        String groupId = channelUID.getGroupId();
        if (groupId == null || app == null) {
            return null;
        }

        Unit<Temperature> unit = "fahrenheit".equals(app.getTempUnit()) ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS;

        if (SYSTEM.equals(groupId)) {
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
        } else if (channelUID.getId().startsWith(CHANNEL_PREFIX)) {
            int channelId = Integer.parseInt(groupId.substring(CHANNEL_PREFIX.length()));
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
                        if (data.getState().equals(ERROR)) {
                            state = UnDefType.UNDEF;
                        } else {
                            state = new QuantityType<>(data.getTemp(), unit);
                        }
                        break;
                    case WlanThermoBindingConstants.CHANNEL_MIN:
                        state = new QuantityType<>(data.getTempMin(), unit);
                        break;
                    case WlanThermoBindingConstants.CHANNEL_MAX:
                        state = new QuantityType<>(data.getTempMax(), unit);
                        break;
                    case WlanThermoBindingConstants.CHANNEL_ALARM_DEVICE:
                        state = OnOffType.from(data.getAlert());
                        break;
                    case WlanThermoBindingConstants.CHANNEL_ALARM_OPENHAB_HIGH:
                        if (!data.getState().equals(ERROR) && data.getTemp() > data.getTempMax()) {
                            state = OnOffType.ON;
                        } else {
                            state = OnOffType.OFF;
                        }
                        break;
                    case WlanThermoBindingConstants.CHANNEL_ALARM_OPENHAB_LOW:
                        if (!data.getState().equals(ERROR) && data.getTemp() < data.getTempMin()) {
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
        } else if (channelUID.getId().startsWith(CHANNEL_PITMASTER_PREFIX)) {
            Pit pit;
            if (groupId.equals(CHANNEL_PITMASTER_1)) {
                pit = app.getPit();
            } else if (groupId.equals(CHANNEL_PITMASTER_2)) {
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
                    state = new QuantityType<>(pit.getSetpoint(), unit);
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

    public static String getTrigger(ChannelUID channelUID, App app) {
        String trigger = null;
        String groupId = channelUID.getGroupId();
        if (groupId == null || app == null) {
            return null;
        }
        if (channelUID.getId().startsWith(CHANNEL_PREFIX)) {
            int channelId = Integer.parseInt(groupId.substring(CHANNEL_PREFIX.length())) - 1;
            if (channelId >= 0 && channelId <= 9) {
                Channel channel = app.getChannel();
                if (channel == null) {
                    return "";
                }
                Data data = channel.getData(channelId);
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_ALARM_OPENHAB:
                        if (!data.getState().equals(ERROR)) {
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
