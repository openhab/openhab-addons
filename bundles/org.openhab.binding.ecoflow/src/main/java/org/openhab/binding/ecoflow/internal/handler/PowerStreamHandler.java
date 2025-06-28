/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.ecoflow.internal.handler;

import static org.openhab.binding.ecoflow.internal.EcoflowBindingConstants.PowerStreamChannels.*;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class PowerStreamHandler extends AbstractEcoflowHandler {
    private static final ValueConverter INVERTER_STATUS_CONVERTER = value -> {
        return switch (value.getAsInt()) {
            case 1 -> new StringType("idle");
            case 2 -> new StringType("starting");
            case 6 -> new StringType("synchronized");
            case 11 -> new StringType("disconnected");
            default -> UnDefType.NULL;
        };
    };
    private static final ValueConverter SUPPLY_PRIORITY_CONVERTER = //
            value -> new StringType(value.getAsInt() > 0 ? "prioIsStorage" : "prioIsSupply");
    private static final ValueConverter INV_SWITCH_CONVERTER = value -> value.getAsInt() != 0 ? OnOffType.OFF
            : OnOffType.ON;
    private static final ValueConverter PERCENT_DECIMAL_CONVERTER = value -> new DecimalType(value.getAsNumber());
    private static final ValueConverter PERCENT_DIMMER_CONVERTER = value -> new PercentType(value.getAsInt());

    private static final List<ChannelMapping> MAPPINGS = List.of(
            new ChannelMapping("20_1", "invStatue", CHANNEL_ID_INV_STATUS, INVERTER_STATUS_CONVERTER),
            new ChannelMapping("20_1", "invInputVolt", CHANNEL_ID_AC_IN_VOLTAGE, 0.1, Units.VOLT),
            new ChannelMapping("20_1", "invFreq", CHANNEL_ID_AC_IN_FREQUENCY, 0.1, Units.HERTZ),
            new ChannelMapping("20_1", "invOutputWatts", CHANNEL_ID_AC_OUT_POWER, 0.1, Units.WATT),
            new ChannelMapping("20_1", "permanentWatts", CHANNEL_ID_AC_OUT_TARGET_POWER, 0.1, Units.WATT),
            new ChannelMapping("20_1", "supplyPriority", CHANNEL_ID_SUPPLY_PRIORITY, SUPPLY_PRIORITY_CONVERTER),
            new ChannelMapping("20_1", "batInputVolt", CHANNEL_ID_BATTERY_VOLTAGE, 0.1, Units.VOLT),
            new ChannelMapping("20_1", "batInputCur", CHANNEL_ID_BATTERY_CURRENT, 0.1, Units.AMPERE),
            new ChannelMapping("20_1", "batInputWatts", CHANNEL_ID_BATTERY_POWER, 0.1, Units.WATT),
            new ChannelMapping("20_1", "batTemp", CHANNEL_ID_BATTERY_TEMPERATURE, 0.1, SIUnits.CELSIUS),
            new ChannelMapping("20_1", "batOffFlag", CHANNEL_ID_BATTERY_ACTIVE, INV_SWITCH_CONVERTER),
            new ChannelMapping("20_1", "batSoc", CHANNEL_ID_BATTERY_LEVEL, PERCENT_DECIMAL_CONVERTER),
            new ChannelMapping("20_1", "lowerLimit", CHANNEL_ID_BATTERY_DISCHARGE_LIMIT, PERCENT_DIMMER_CONVERTER),
            new ChannelMapping("20_1", "upperLimit", CHANNEL_ID_BATTERY_CHARGE_LIMIT, PERCENT_DIMMER_CONVERTER),
            new ChannelMapping("20_1", "pv1InputVolt", CHANNEL_ID_PV1_IN_VOLTAGE, 0.1, Units.VOLT),
            new ChannelMapping("20_1", "pv1VolInTag", CHANNEL_ID_PV1_IN_TARGET_VOLTAGE, 0.1, Units.VOLT),
            new ChannelMapping("20_1", "pv1InputCur", CHANNEL_ID_PV1_IN_CURRENT, 0.1, Units.AMPERE),
            new ChannelMapping("20_1", "pv1InputWatts", CHANNEL_ID_PV1_IN_POWER, 0.1, Units.WATT),
            new ChannelMapping("20_1", "pv1CtrlMpptOffFlag", CHANNEL_ID_PV1_MPPT_ACTIVE, INV_SWITCH_CONVERTER),
            new ChannelMapping("20_1", "pv2InputVolt", CHANNEL_ID_PV2_IN_VOLTAGE, 0.1, Units.VOLT),
            new ChannelMapping("20_1", "pv2VolInTag", CHANNEL_ID_PV2_IN_TARGET_VOLTAGE, 0.1, Units.VOLT),
            new ChannelMapping("20_1", "pv2InputCur", CHANNEL_ID_PV2_IN_CURRENT, 0.1, Units.AMPERE),
            new ChannelMapping("20_1", "pv2InputWatts", CHANNEL_ID_PV2_IN_POWER, 0.1, Units.WATT),
            new ChannelMapping("20_1", "pv2CtrlMpptOffFlag", CHANNEL_ID_PV2_MPPT_ACTIVE, INV_SWITCH_CONVERTER));

    public PowerStreamHandler(Thing thing) {
        super(thing, MAPPINGS, "param");
    }

    @Override
    protected Optional<String> extractGroupKeyFromMqttMessage(JsonObject payload) {
        return Optional.of(payload.get("cmdFunc").getAsString() + "_" + payload.get("cmdId").getAsString());
    }

    @Override
    protected Optional<JsonObject> convertCommand(String channelId, Command command) {
        if (CHANNEL_ID_AC_OUT_TARGET_POWER.equals(channelId)) {
            @Nullable
            Number targetPower = null;
            if (command instanceof QuantityType<?> quantity) {
                targetPower = quantity.toUnit(Units.WATT);
            } else if (command instanceof DecimalType decimal) {
                targetPower = decimal;
            }
            if (targetPower != null) {
                int targetPowerInt = Math.round(targetPower.floatValue() * 10);
                JsonObject req = createControlRequest("WN511_SET_PERMANENT_WATTS_PACK", "permanentWatts",
                        targetPowerInt);
                return Optional.of(req);
            }
        } else if (CHANNEL_ID_SUPPLY_PRIORITY.equals(channelId) && command instanceof StringType value) {
            final int mode;
            switch (value.toFullString()) {
                case "prioIsSupply":
                    mode = 0;
                    break;
                case "prioIsStorage":
                    mode = 1;
                    break;
                default:
                    logger.warn("{}: Got unexpected power supply priority value {}", serialNumber, value);
                    return Optional.empty();
            }
            return Optional.of(createControlRequest("WN511_SET_SUPPLY_PRIORITY_PACK", "supplyPriority", mode));
        } else if (CHANNEL_ID_BATTERY_CHARGE_LIMIT.equals(channelId) && command instanceof DecimalType value) {
            return Optional.of(createControlRequest("WN511_SET_BAT_UPPER_PACK", "upperLimit", value));
        } else if (CHANNEL_ID_BATTERY_DISCHARGE_LIMIT.equals(channelId) && command instanceof DecimalType value) {
            return Optional.of(createControlRequest("WN511_SET_BAT_LOWER_PACK", "lowerLimit", value));
        }
        return Optional.empty();
    }

    private JsonObject createControlRequest(String command, String paramName, Number paramValue) {
        JsonObject params = new JsonObject();
        params.addProperty(paramName, paramValue);
        JsonObject result = new JsonObject();
        result.addProperty("cmdCode", command);
        result.add("params", params);
        return result;
    }
}
