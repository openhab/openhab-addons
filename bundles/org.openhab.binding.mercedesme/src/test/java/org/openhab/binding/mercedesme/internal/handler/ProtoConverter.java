/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.utils.Mapper;
import org.openhab.binding.mercedesme.internal.utils.Utils;

import com.daimler.mbcarkit.proto.Client.ClientMessage;
import com.daimler.mbcarkit.proto.VehicleCommands.CommandRequest;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgram;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramParameters;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePoint;
import com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePointsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus.ClockHourUnit;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus.CombustionConsumptionUnit;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus.DistanceUnit;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus.ElectricityConsumptionUnit;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus.GasConsumptionUnit;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus.PressureUnit;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus.RatioUnit;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus.SpeedUnit;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus.TemperatureUnit;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdate;
import com.google.protobuf.Int32Value;
import com.google.protobuf.TextFormat;

/**
 * {@link ProtoConverter} Proto conversions for Unit Tests and not necessary for binding
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ProtoConverter {

    public static VehicleStatusAttributes json2Proto(String json, boolean fullUpdate) {
        JSONObject jsonObj = new JSONObject(json);
        Map<String, VehicleAttributeStatus> updateMap = new HashMap<>();
        Iterator<String> keyIter = jsonObj.keys();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            JSONObject value = jsonObj.getJSONObject(key);
            Iterator<String> valueIter = value.keys();
            com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus.Builder builder = VehicleAttributeStatus
                    .newBuilder();
            while (valueIter.hasNext()) {
                String valueKey = valueIter.next();
                switch (valueKey) {
                    case "timestamp_in_ms":
                        builder.setTimestampInMs(value.getLong(valueKey));
                        break;
                    case "timestamp":
                        builder.setTimestampInMs(value.getLong(valueKey) * 1000);
                        break;
                    case "bool_value":
                        builder.setBoolValue(value.getBoolean(valueKey));
                        break;
                    case "nil_value":
                        builder.setNilValue(value.getBoolean(valueKey));
                        break;
                    case "status":
                        builder.setStatus(value.getInt(valueKey));
                        break;
                    case "int_value":
                        builder.setIntValue(value.getInt(valueKey));
                        break;
                    case "display_value":
                        builder.setDisplayValue(value.getString(valueKey));
                        break;
                    case "double_value":
                        builder.setDoubleValue(value.getDouble(valueKey));
                        break;
                    case "distance_unit":
                        builder.setDistanceUnit(DistanceUnit.valueOf(value.getString(valueKey)));
                        break;
                    case "electricity_consumption_unit":
                        builder.setElectricityConsumptionUnit(
                                ElectricityConsumptionUnit.valueOf(value.getString(valueKey)));
                        break;
                    case "speed_unit":
                        builder.setSpeedUnit(SpeedUnit.valueOf(value.getString(valueKey)));
                        break;
                    case "ratio_unit":
                        builder.setRatioUnit(RatioUnit.valueOf(value.getString(valueKey)));
                        break;
                    case "gas_consumption_unit":
                        builder.setGasConsumptionUnit(GasConsumptionUnit.valueOf(value.getString(valueKey)));
                        break;
                    case "pressure_unit":
                        builder.setPressureUnit(PressureUnit.valueOf(value.getString(valueKey)));
                        break;
                    case "combustion_consumption_unit":
                        builder.setCombustionConsumptionUnit(
                                CombustionConsumptionUnit.valueOf(value.getString(valueKey)));
                        break;
                    case "temperature_unit":
                        builder.setTemperatureUnit(TemperatureUnit.valueOf(value.getString(valueKey)));
                        break;
                    case "clock_hour_unit":
                        builder.setClockHourUnit(ClockHourUnit.valueOf(value.getString(valueKey)));
                        break;
                    case "temperature_points_value":
                        JSONArray temperaturepointsJson = value.getJSONObject(valueKey)
                                .getJSONArray("temperature_points");
                        List<TemperaturePoint> tpList = new ArrayList<>();
                        for (int i = 0; i < temperaturepointsJson.length(); i++) {
                            com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePoint.Builder tpBuilder = TemperaturePoint
                                    .newBuilder();
                            JSONObject tpJson = temperaturepointsJson.getJSONObject(i);
                            Iterator<String> tempPointJsonIterator = tpJson.keys();
                            while (tempPointJsonIterator.hasNext()) {
                                String tpValueKey = tempPointJsonIterator.next();
                                switch (tpValueKey) {
                                    case "temperature":
                                        tpBuilder.setTemperature(tpJson.getDouble(tpValueKey));
                                        break;
                                    case "zone":
                                        tpBuilder.setZone(tpJson.getString(tpValueKey));
                                        break;
                                    case "temperature_display_value":
                                        tpBuilder.setTemperatureDisplayValue(tpJson.getString(tpValueKey));
                                        break;
                                }
                            }
                            TemperaturePoint tpProto = tpBuilder.build();
                            tpList.add(tpProto);
                        }
                        TemperaturePointsValue tpValueProto = TemperaturePointsValue.newBuilder()
                                .addAllTemperaturePoints(tpList).build();
                        builder.setTemperaturePointsValue(tpValueProto);
                        break;
                    case "charge_programs_value":
                        List<ChargeProgramParameters> chargeProgramsList = new ArrayList<>();
                        JSONArray chargeProgramsJsonArray = value.getJSONArray("charge_programs_value");
                        for (int i = 0; i < chargeProgramsJsonArray.length(); i++) {
                            com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramParameters.Builder chargeProgramBuilder = ChargeProgramParameters
                                    .newBuilder();
                            JSONObject chargeProgramJson = chargeProgramsJsonArray.getJSONObject(i);
                            Iterator<String> chargeProgramJsonIterator = chargeProgramJson.keys();
                            while (chargeProgramJsonIterator.hasNext()) {
                                String chargeProgramKey = chargeProgramJsonIterator.next();
                                switch (chargeProgramKey) {
                                    case "charge_program":
                                        chargeProgramBuilder.setChargeProgram(
                                                ChargeProgram.valueOf(chargeProgramJson.getString(chargeProgramKey)));
                                        break;
                                    case "max_soc":
                                        chargeProgramBuilder.setMaxSoc(chargeProgramJson.getInt(chargeProgramKey));
                                        break;
                                }
                            }
                            chargeProgramsList.add(chargeProgramBuilder.build());
                        }
                        ChargeProgramsValue cpv = ChargeProgramsValue.newBuilder()
                                .addAllChargeProgramParameters(chargeProgramsList).build();
                        builder.setChargeProgramsValue(cpv);
                        break;
                }
            }
            updateMap.put(key, builder.build());
        }
        return new VehicleStatusAttributes(fullUpdate, updateMap);
    }

    /**
     * Parses a {@code VehicleStatusUpdate} capture in protobuf TextFormat ({@code .raw} fixtures under
     * {@code src/test/resources/vehiclestatusupdates}) into the internal carrier type used by the live push path.
     *
     * @param rawText the TextFormat dump of a single {@code VehicleStatusUpdate} message
     * @param fullUpdate the full/partial flag to attach, since a raw capture itself carries none
     */
    public static VehicleStatusAttributes raw2Proto(String rawText, boolean fullUpdate) {
        String protoText = stripLogPrefix(rawText);
        VehicleStatusUpdate.Builder vsuBuilder = VehicleStatusUpdate.newBuilder();
        try {
            TextFormat.getParser().merge(protoText, vsuBuilder);
        } catch (TextFormat.ParseException e) {
            throw new IllegalArgumentException("Failed to parse VehicleStatusUpdate raw fixture", e);
        }
        Map<String, VehicleAttributeStatus> updateMap = Mapper.fromVehicleStatusUpdate(vsuBuilder.build());
        return new VehicleStatusAttributes(fullUpdate, updateMap);
    }

    /**
     * Some {@code .raw} fixtures still carry the leading TRACE line pasted from the openHAB log; strip it so
     * only the protobuf TextFormat content is parsed.
     */
    private static String stripLogPrefix(String rawText) {
        int firstNewline = rawText.indexOf('\n');
        if (firstNewline < 0) {
            return rawText;
        }
        String firstLine = rawText.substring(0, firstNewline);
        if (firstLine.contains("Raw VehicleStatusUpdate for")) {
            return rawText.substring(firstNewline + 1);
        }
        return rawText;
    }

    public static JSONObject clientMessage2Json(ClientMessage cm) {
        JSONObject cmJson = new JSONObject();
        CommandRequest cr = cm.getCommandRequest();
        if (cr.hasTemperatureConfigure()) {
            return Utils.getJsonObject(cr.getTemperatureConfigure().getTemperaturePoints(0).getAllFields());
        }
        if (cr.hasChargeProgramConfigure()) {
            JSONObject cpv = Utils.getJsonObject(cr.getChargeProgramConfigure().getAllFields());
            Int32Value soc = (Int32Value) cpv.get("max_soc");
            cpv.put("max_soc", soc.getValue());
            return cpv;
        }
        // ChargingConfigure carries max_soc as an Int32Value wrapper, same as
        // ChargeProgramConfigure - unwrap it before returning.
        if (cr.hasChargingConfigure()) {
            JSONObject cc = Utils.getJsonObject(cr.getChargingConfigure().getAllFields());
            Int32Value soc = (Int32Value) cc.get("max_soc");
            cc.put("max_soc", soc.getValue());
            return cc;
        }
        // DoorsLock/DoorsUnlock carry no distinguishing fields, so expose which oneof case was set
        if (cr.hasDoorsLock()) {
            JSONObject dl = Utils.getJsonObject(cr.getDoorsLock().getAllFields());
            dl.put("commandType", "doorsLock");
            return dl;
        }
        if (cr.hasDoorsUnlock()) {
            JSONObject du = Utils.getJsonObject(cr.getDoorsUnlock().getAllFields());
            du.put("commandType", "doorsUnlock");
            return du;
        }
        return cmJson;
    }
}
