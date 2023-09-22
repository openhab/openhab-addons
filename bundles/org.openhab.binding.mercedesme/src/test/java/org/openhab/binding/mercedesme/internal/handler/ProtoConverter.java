package org.openhab.binding.mercedesme.internal.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.daimler.mbcarkit.proto.VehicleEvents;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgram;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramParameters;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePoint;
import com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePointsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;
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

public class ProtoConverter {

    public static VEPUpdate json2Proto(String json, boolean fullUpdate) {
        JSONObject jsonObj = new JSONObject(json);
        Map<String, VehicleAttributeStatus> updateMap = new HashMap<String, VehicleEvents.VehicleAttributeStatus>();
        Iterator<String> keyIter = jsonObj.keys();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            JSONObject value = jsonObj.getJSONObject(key);
            Iterator<String> valueIter = value.keys();
            com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus.Builder builder = VehicleAttributeStatus
                    .newBuilder();
            while (valueIter.hasNext()) {
                String valueKey = valueIter.next();
                // System.out.println("J2P - handle " + valueKey);
                switch (valueKey) {
                    case "timestamp_in_ms":
                        builder.setTimestampInMs(value.getLong(valueKey));
                        break;
                    case "timestamp":
                        builder.setTimestamp(value.getLong(valueKey));
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
                        List<TemperaturePoint> tpList = new ArrayList<VehicleEvents.TemperaturePoint>();
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
                                TemperaturePoint tpProto = tpBuilder.build();
                                tpList.add(tpProto);
                            }
                        }
                        TemperaturePointsValue tpValueProto = TemperaturePointsValue.newBuilder()
                                .addAllTemperaturePoints(tpList).build();
                        builder.setTemperaturePointsValue(tpValueProto);
                        break;
                    case "charge_programs_value":
                        List<ChargeProgramParameters> chargeProgramsList = new ArrayList<ChargeProgramParameters>();
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
        return VEPUpdate.newBuilder().setFullUpdate(fullUpdate).putAllAttributes(updateMap).build();
    }
}
