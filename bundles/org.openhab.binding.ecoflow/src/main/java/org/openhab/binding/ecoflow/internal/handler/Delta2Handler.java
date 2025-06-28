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

import static org.openhab.binding.ecoflow.internal.EcoflowBindingConstants.DeltaChannels.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
public class Delta2Handler extends AbstractEcoflowHandler {
    private int nextControlId = 1;

    private static final ValueConverter PERCENT_DECIMAL_CONVERTER = value -> new DecimalType(value.getAsNumber());
    private static final ValueConverter PERCENT_DIMMER_CONVERTER = value -> new PercentType(value.getAsInt());
    private static final ValueConverter SWITCH_CONVERTER = value -> value.getAsInt() != 0 ? OnOffType.ON
            : OnOffType.OFF;

    private static final ValueConverter CHARGER_TYPE_CONVERTER = value -> switch (value.getAsInt()) {
        case 1 -> new StringType("ac");
        case 2 -> new StringType("dc");
        case 3 -> new StringType("solar");
        default -> UnDefType.NULL;
    };

    private static final ValueConverter SOLAR_INPUT_STATE_CONVERTER = value -> switch (value.getAsInt()) {
        case 0 -> new StringType("disabled");
        case 1 -> new StringType("charging");
        case 2 -> new StringType("standby");
        default -> UnDefType.NULL;
    };

    private static final ValueConverter SOLAR_INPUT_TYPE_CONVERTER = value -> switch (value.getAsInt()) {
        case 0 -> new StringType("none");
        case 1 -> new StringType("dc");
        case 2 -> new StringType("solar");
        default -> UnDefType.NULL;
    };

    private static final List<ChannelMapping> MAPPINGS = List.of(
            // Status
            new ChannelMapping("bms_emsStatus", "f32LcdShowSoc", CHANNEL_ID_BATTERY_SOC, PERCENT_DECIMAL_CONVERTER),
            new ChannelMapping("bms_bmsStatus", "inputWatts", CHANNEL_ID_INPUT_POWER, 1, Units.WATT),
            new ChannelMapping("bms_bmsStatus", "outputWatts", CHANNEL_ID_OUTPUT_POWER, 1, Units.WATT),
            new ChannelMapping("bms_emsStatus", "chgRemainTime", CHANNEL_ID_REMAINING_CHARGE_TIME, 1, Units.MINUTE),
            new ChannelMapping("bms_emsStatus", "dsgRemainTime", CHANNEL_ID_REMAINING_DISCHARGE_TIME, 1, Units.MINUTE),
            // Battery
            new ChannelMapping("bms_bmsStatus", "temp", CHANNEL_ID_BATTERY_TEMPERATURE, 1, SIUnits.CELSIUS),
            new ChannelMapping("bms_bmsStatus", "vol", CHANNEL_ID_BATTERY_VOLTAGE, 0.001, Units.VOLT),
            new ChannelMapping("bms_bmsStatus", "amp", CHANNEL_ID_BATTERY_CURRENT, 0.001, Units.AMPERE),
            new ChannelMapping("inv", "chargerType", CHANNEL_ID_BATTERY_CHARGER_TYPE, CHARGER_TYPE_CONVERTER),
            new ChannelMapping("bms_emsStatus", "minDsgSoc", CHANNEL_ID_BATTERY_DISCHARGE_LIMIT,
                    PERCENT_DIMMER_CONVERTER),
            new ChannelMapping("bms_emsStatus", "maxChargeSoc", CHANNEL_ID_BATTERY_CHARGE_LIMIT,
                    PERCENT_DIMMER_CONVERTER),
            // AC input
            new ChannelMapping("inv", "SlowChgWatts", CHANNEL_ID_AC_IN_CHARGING_POWER, 1, Units.WATT),
            new ChannelMapping("inv", "acInVol", CHANNEL_ID_AC_IN_VOLTAGE, 0.001, Units.VOLT),
            new ChannelMapping("inv", "acInAmp", CHANNEL_ID_AC_IN_CURRENT, 0.001, Units.AMPERE),
            new ChannelMapping("inv", "inputWatts", CHANNEL_ID_AC_IN_POWER, 1, Units.WATT),
            new ChannelMapping("inv", "acInFreq", CHANNEL_ID_AC_IN_FREQUENCY, 1, Units.HERTZ),
            new ChannelMapping("pd", "chgPowerAC", CHANNEL_ID_AC_IN_ENERGY, 1, Units.WATT_HOUR),
            // AC output
            new ChannelMapping("inv", "cfgAcEnabled", CHANNEL_ID_AC_OUT_ENABLED, SWITCH_CONVERTER),
            new ChannelMapping("inv", "cfgAcXboost", CHANNEL_ID_AC_OUT_XBOOST_ENABLED, SWITCH_CONVERTER),
            new ChannelMapping("inv", "invOutVol", CHANNEL_ID_AC_OUT_VOLTAGE, 0.001, Units.VOLT),
            new ChannelMapping("inv", "invOutAmp", CHANNEL_ID_AC_OUT_CURRENT, 0.001, Units.AMPERE),
            new ChannelMapping("inv", "outputWatts", CHANNEL_ID_AC_OUT_POWER, 1, Units.WATT),
            new ChannelMapping("inv", "invOutFreq", CHANNEL_ID_AC_OUT_FREQUENCY, 1, Units.HERTZ),
            new ChannelMapping("pd", "dsgPowerAC", CHANNEL_ID_AC_OUT_ENERGY, 1, Units.WATT_HOUR),
            new ChannelMapping("inv", "outTemp", CHANNEL_ID_AC_OUT_TEMPERATURE, 1, SIUnits.CELSIUS),
            // DC output
            new ChannelMapping("pd", "dcOutState", CHANNEL_ID_USB_OUT_ENABLED, SWITCH_CONVERTER),
            new ChannelMapping("mppt", "carState", CHANNEL_ID_12V_OUT_ENABLED, SWITCH_CONVERTER),
            new ChannelMapping("pd", "usb1Watts", CHANNEL_ID_USB1_OUTPUT_POWER, 1, Units.WATT),
            new ChannelMapping("pd", "usb2Watts", CHANNEL_ID_USB2_OUTPUT_POWER, 1, Units.WATT),
            new ChannelMapping("pd", "qcUsb1Watts", CHANNEL_ID_QCUSB1_OUTPUT_POWER, 1, Units.WATT),
            new ChannelMapping("pd", "qcUsb2Watts", CHANNEL_ID_QCUSB2_OUTPUT_POWER, 1, Units.WATT),
            new ChannelMapping("pd", "typec1Watts", CHANNEL_ID_USBC1_OUTPUT_POWER, 1, Units.WATT),
            new ChannelMapping("pd", "typec2Watts", CHANNEL_ID_USBC2_OUTPUT_POWER, 1, Units.WATT),
            new ChannelMapping("mppt", "carOutVol", CHANNEL_ID_12V_OUT_VOLTAGE, 0.001, Units.VOLT),
            new ChannelMapping("mppt", "carOutAmp", CHANNEL_ID_12V_OUT_CURRENT, 0.001, Units.AMPERE),
            new ChannelMapping("mppt", "carOutWatts", CHANNEL_ID_12V_OUT_POWER, 1, Units.WATT),
            new ChannelMapping("pd", "dsgPowerDC", CHANNEL_ID_DC_OUT_ENERGY, 1, Units.WATT_HOUR),
            // Solar
            new ChannelMapping("mppt", "inVol", CHANNEL_ID_PV_IN_VOLTAGE, 0.001, Units.VOLT),
            new ChannelMapping("mppt", "inAmp", CHANNEL_ID_PV_IN_CURRENT, 0.001, Units.AMPERE),
            new ChannelMapping("mppt", "inWatts", CHANNEL_ID_PV_IN_POWER, 1, Units.WATT),
            new ChannelMapping("mppt", "chgState", CHANNEL_ID_PV_IN_STATE, SOLAR_INPUT_STATE_CONVERTER),
            new ChannelMapping("mppt", "chgType", CHANNEL_ID_PV_IN_TYPE, SOLAR_INPUT_TYPE_CONVERTER),
            new ChannelMapping("pd", "chgPowerDC", CHANNEL_ID_SOLAR_ENERGY, 1, Units.WATT_HOUR),
            new ChannelMapping("pd", "XT150Watts1", CHANNEL_ID_EXTRA_BATTERY_POWER, 1, Units.WATT));

    private static final List<ChannelMapping> MAX_ONLY_MAPPINGS = List.of(
            new ChannelMapping("mppt", "pv2InVol", CHANNEL_ID_MAX_PV2_IN_VOLTAGE, 0.001, Units.VOLT),
            new ChannelMapping("mppt", "pv2InAmp", CHANNEL_ID_MAX_PV2_IN_CURRENT, 0.001, Units.AMPERE),
            new ChannelMapping("mppt", "pv2InWatts", CHANNEL_ID_MAX_PV2_IN_POWER, 1, Units.WATT),
            new ChannelMapping("mppt", "pv2ChgState", CHANNEL_ID_MAX_PV2_IN_STATE, SOLAR_INPUT_STATE_CONVERTER),
            new ChannelMapping("mppt", "pv2ChgType", CHANNEL_ID_MAX_PV2_IN_TYPE, SOLAR_INPUT_STATE_CONVERTER),
            new ChannelMapping("pd", "XT150Watts2", CHANNEL_ID_MAX_EXTRA_BATTERY2_POWER, 1, Units.WATT));

    public Delta2Handler(Thing thing, boolean isDelta2Max) {
        super(thing, isDelta2Max ? Stream.concat(MAPPINGS.stream(), MAX_ONLY_MAPPINGS.stream()).toList() : MAPPINGS,
                "params");
    }

    @Override
    protected Optional<String> extractGroupKeyFromMqttMessage(JsonObject payload) {
        return switch (payload.get("moduleType").getAsInt()) {
            case 1 -> Optional.of("pd");
            case 2 -> Optional.of("bms_" + payload.get("typeCode").getAsString());
            case 3 -> Optional.of("inv");
            case 5 -> Optional.of("mppt");
            default -> Optional.empty();
        };
    }

    @Override
    protected Optional<JsonObject> convertCommand(String channelId, Command command) {
        if (CHANNEL_ID_USB_OUT_ENABLED.equals(channelId) && command instanceof OnOffType onOff) {
            return Optional.of(createSwitchRequest(1, "dcOutCfg", onOff));
        } else if (CHANNEL_ID_12V_OUT_ENABLED.equals(channelId) && command instanceof OnOffType onOff) {
            return Optional.of(createSwitchRequest(5, "mpptCar", onOff));
        } else if (CHANNEL_ID_AC_OUT_ENABLED.equals(channelId) && command instanceof OnOffType onOff) {
            JsonObject params = new JsonObject();
            params.addProperty("enabled", onOff == OnOffType.ON ? 1 : 0);
            params.addProperty("xboost", 255);
            params.addProperty("out_voltage", -1);
            params.addProperty("out_freq", 255);
            return Optional.of(createControlRequest(3, "acOutCfg", params));
        } else if (CHANNEL_ID_AC_OUT_XBOOST_ENABLED.equals(channelId) && command instanceof OnOffType onOff) {
            JsonObject params = new JsonObject();
            params.addProperty("xboost", onOff == OnOffType.ON ? 1 : 0);
            return Optional.of(createControlRequest(3, "acOutCfg", params));
        } else if (CHANNEL_ID_AC_IN_CHARGING_POWER.equals(channelId)) {
            final int targetPower;
            if (command instanceof QuantityType<?> quantity) {
                QuantityType<?> valueInWatts = quantity.toUnit(Units.WATT);
                if (valueInWatts == null) {
                    return Optional.empty();
                }
                targetPower = valueInWatts.intValue();
            } else if (command instanceof DecimalType decimal) {
                targetPower = decimal.intValue();
            } else {
                return Optional.empty();
            }
            JsonObject params = new JsonObject();
            params.addProperty("slowChgWatts", targetPower);
            params.addProperty("fastChgWatts", 2400);
            params.addProperty("chgPauseFlag", targetPower < 100 ? 1 : 0);
            return Optional.of(createControlRequest(3, "acChgCfg", params));
        } else if (CHANNEL_ID_BATTERY_CHARGE_LIMIT.equals(channelId) && command instanceof DecimalType value) {
            JsonObject params = new JsonObject();
            params.addProperty("maxChgSoc", value);
            return Optional.of(createControlRequest(2, "upsConfig", params));
        } else if (CHANNEL_ID_BATTERY_DISCHARGE_LIMIT.equals(channelId) && command instanceof DecimalType value) {
            JsonObject params = new JsonObject();
            params.addProperty("minDsgSoc", value);
            return Optional.of(createControlRequest(2, "dsgCfg", params));
        }
        return Optional.empty();
    }

    private JsonObject createSwitchRequest(int moduleType, String operation, OnOffType onOff) {
        JsonObject params = new JsonObject();
        params.addProperty("enabled", onOff == OnOffType.ON ? 1 : 0);
        return createControlRequest(moduleType, operation, params);
    }

    private JsonObject createControlRequest(int moduleType, String operation, JsonObject params) {
        JsonObject result = new JsonObject();
        result.add("params", params);
        result.addProperty("operateType", operation);
        result.addProperty("moduleType", moduleType);
        result.addProperty("id", nextControlId++);
        result.addProperty("version", "1.0");
        return result;
    }
}
