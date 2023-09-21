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
package org.openhab.binding.wlanthermo.internal.api.mini;

import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.*;
import static org.openhab.binding.wlanthermo.internal.WlanThermoUtil.requireNonNull;

import java.awt.Color;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants;
import org.openhab.binding.wlanthermo.internal.WlanThermoInputException;
import org.openhab.binding.wlanthermo.internal.WlanThermoUnknownChannelException;
import org.openhab.binding.wlanthermo.internal.api.mini.dto.builtin.App;
import org.openhab.binding.wlanthermo.internal.api.mini.dto.builtin.Channel;
import org.openhab.binding.wlanthermo.internal.api.mini.dto.builtin.Data;
import org.openhab.binding.wlanthermo.internal.api.mini.dto.builtin.Pit;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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
@NonNullByDefault
public class WlanThermoMiniCommandHandler {

    public static final String ERROR = "er";

    public static State getState(ChannelUID channelUID, App app)
            throws WlanThermoUnknownChannelException, WlanThermoInputException {
        String groupId = requireNonNull(channelUID.getGroupId());
        Unit<Temperature> unit = "fahrenheit".equals(app.getTempUnit()) ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS;

        if (SYSTEM.equals(groupId)) {
            switch (channelUID.getIdWithoutGroup()) {
                case WlanThermoBindingConstants.SYSTEM_CPU_TEMP:
                    if (app.getCpuTemp() == null) {
                        return UnDefType.UNDEF;
                    } else {
                        return new DecimalType(app.getCpuTemp());
                    }
                case WlanThermoBindingConstants.SYSTEM_CPU_LOAD:
                    if (app.getCpuLoad() == null) {
                        return UnDefType.UNDEF;
                    } else {
                        return new DecimalType(app.getCpuLoad());
                    }
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
                        return new StringType(data.getName());
                    case WlanThermoBindingConstants.CHANNEL_TEMP:
                        if (data.getState().equals(ERROR)) {
                            return UnDefType.UNDEF;
                        } else {
                            return new QuantityType<>(data.getTemp(), unit);
                        }
                    case WlanThermoBindingConstants.CHANNEL_MIN:
                        return new QuantityType<>(data.getTempMin(), unit);
                    case WlanThermoBindingConstants.CHANNEL_MAX:
                        return new QuantityType<>(data.getTempMax(), unit);
                    case WlanThermoBindingConstants.CHANNEL_ALARM_DEVICE:
                        return OnOffType.from(data.getAlert());
                    case WlanThermoBindingConstants.CHANNEL_ALARM_OPENHAB_HIGH:
                        if (!data.getState().equals(ERROR) && data.getTemp() > data.getTempMax()) {
                            return OnOffType.ON;
                        } else {
                            return OnOffType.OFF;
                        }
                    case WlanThermoBindingConstants.CHANNEL_ALARM_OPENHAB_LOW:
                        if (!data.getState().equals(ERROR) && data.getTemp() < data.getTempMin()) {
                            return OnOffType.ON;
                        } else {
                            return OnOffType.OFF;
                        }
                    case WlanThermoBindingConstants.CHANNEL_COLOR:
                        Color c = Color.decode(WlanThermoMiniUtil.toHex(data.getColor()));
                        return HSBType.fromRGB(c.getRed(), c.getGreen(), c.getBlue());
                    case WlanThermoBindingConstants.CHANNEL_COLOR_NAME:
                        return new StringType(data.getColor());
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
                    return OnOffType.from(pit.getEnabled());
                case WlanThermoBindingConstants.CHANNEL_PITMASTER_CURRENT:
                    return new DecimalType(pit.getCurrent());
                case WlanThermoBindingConstants.CHANNEL_PITMASTER_SETPOINT:
                    return new QuantityType<>(pit.getSetpoint(), unit);
                case WlanThermoBindingConstants.CHANNEL_PITMASTER_DUTY_CYCLE:
                    return new DecimalType(pit.getControlOut());
                case WlanThermoBindingConstants.CHANNEL_PITMASTER_LID_OPEN:
                    return OnOffType.from("True".equals(pit.getOpenLid()));
                case WlanThermoBindingConstants.CHANNEL_PITMASTER_CHANNEL_ID:
                    return new DecimalType(pit.getCh());
            }
        }
        throw new WlanThermoUnknownChannelException(channelUID);
    }

    public static String getTrigger(ChannelUID channelUID, App app)
            throws WlanThermoUnknownChannelException, WlanThermoInputException {
        String groupId = requireNonNull(channelUID.getGroupId());

        if (channelUID.getId().startsWith(CHANNEL_PREFIX)) {
            int channelId = Integer.parseInt(groupId.substring(CHANNEL_PREFIX.length())) - 1;
            if (channelId >= 0 && channelId <= 9) {
                Channel channel = app.getChannel();
                if (channel == null) {
                    throw new WlanThermoInputException();
                }
                Data data = channel.getData(channelId);
                if (CHANNEL_ALARM_OPENHAB.equals(channelUID.getIdWithoutGroup())) {
                    if (!data.getState().equals(ERROR)) {
                        if (data.getTemp() > data.getTempMax()) {
                            return TRIGGER_ALARM_MAX;
                        } else if (data.getTemp() < data.getTempMin()) {
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
