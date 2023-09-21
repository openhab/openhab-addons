package org.openhab.binding.mercedesme.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.FileReader;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.MercedesMeCommandOptionProvider;
import org.openhab.binding.mercedesme.internal.MercedesMeDynamicStateDescriptionProvider;
import org.openhab.binding.mercedesme.internal.MercedesMeStateOptionProvider;
import org.openhab.binding.mercedesme.internal.config.VehicleConfiguration;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

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

class VehicleHandlerTest {

    @Test
    public void testBEVFullUpdateNoCapacities() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, mock(MercedesMeCommandOptionProvider.class),
                mock(MercedesMeStateOptionProvider.class), mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        THC updateListener = new THC();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/Proto2Json.json");
        VEPUpdate update = json2Proto(json, true);
        vh.distributeContent(update);

        assertEquals(11, updateListener.updatesPerGroupMap.size(), "Group Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("doors"), "Doors Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("vehicle"), "Vehcile Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("windows"), "Windows Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("trip"), "Trip Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("tires"), "Tire Update Count");
        assertEquals(6, updateListener.getUpdatesForGroup("service"), "Service Update Count");
        assertEquals(6, updateListener.getUpdatesForGroup("range"), "Range Update Count");
        assertEquals(2, updateListener.getUpdatesForGroup("position"), "Position Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("lock"), "Lock Update Count");
        assertEquals(7, updateListener.getUpdatesForGroup("hvac"), "HVAC Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("charge"), "Charge Update Count");
    }

    @Test
    public void testHybridFullUpdateNoCapacities() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_HYBRID);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.HYBRID));
        VehicleHandler vh = new VehicleHandler(thingMock, mock(MercedesMeCommandOptionProvider.class),
                mock(MercedesMeStateOptionProvider.class), mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        THC updateListener = new THC();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MBHybrid.json");
        VEPUpdate update = json2Proto(json, true);
        vh.distributeContent(update);

        assertEquals(11, updateListener.updatesPerGroupMap.size(), "Group Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("doors"), "Doors Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("vehicle"), "Vehcile Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("windows"), "Windows Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("trip"), "Trip Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("tires"), "Trip Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("service"), "Trip Update Count");
        assertEquals(13, updateListener.getUpdatesForGroup("range"), "Update Upadte Count");
        assertEquals(2, updateListener.getUpdatesForGroup("position"), "Update Upadte Count");
        assertEquals(6, updateListener.getUpdatesForGroup("lock"), "Lock Update Count");
        assertEquals(7, updateListener.getUpdatesForGroup("hvac"), "HVAC Update Count");
        assertEquals(7, updateListener.getUpdatesForGroup("charge"), "Charge Update Count");
    }

    @Test
    public void testHybridFullUpadteWithCapacities() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_HYBRID);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", "hybrid"));
        VehicleHandler vh = new VehicleHandler(thingMock, mock(MercedesMeCommandOptionProvider.class),
                mock(MercedesMeStateOptionProvider.class), mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vehicleConfig.batteryCapacity = (float) 9.2;
        vehicleConfig.fuelCapacity = (float) 59.9;
        vh.config = Optional.of(vehicleConfig);
        THC updateListener = new THC();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MBHybrid.json");
        VEPUpdate update = json2Proto(json, true);
        vh.distributeContent(update);
    }

    private VEPUpdate json2Proto(String json, boolean fullUpdate) {
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
                        System.out.println("J2P - handle temperaturePoints " + value.getJSONObject(valueKey));
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
                        System.out
                                .println("J2P - handle charge programs " + value.getJSONArray("charge_programs_value"));
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

    class THC implements ThingHandlerCallback {
        Map<String, State> updatesReceived = new HashMap<String, State>();
        Map<String, Map<String, State>> updatesPerGroupMap = new HashMap<String, Map<String, State>>();

        public int getUpdatesForGroup(String group) {
            Map<String, State> groupMap = updatesPerGroupMap.get(group);
            if (groupMap != null) {
                return groupMap.size();
            }
            return 0;
        }

        @Override
        public void stateUpdated(ChannelUID channelUID, State state) {
            if (Constants.GROUP_LOCK.equals(channelUID.getGroupId())) {
                System.out.println(channelUID.toString() + " received " + state.toFullString());
            }
            updatesReceived.put(channelUID.toString(), state);
            Map<String, State> groupMap = updatesPerGroupMap.get(channelUID.getGroupId());
            if (groupMap == null) {
                groupMap = new HashMap<String, State>();
                updatesPerGroupMap.put(channelUID.getGroupId(), groupMap);
            }
            groupMap.put(channelUID.toString(), state);
        }

        @Override
        public void postCommand(ChannelUID channelUID, Command command) {
            // TODO Auto-generated method stub
        }

        @Override
        public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
            // TODO Auto-generated method stub
        }

        @Override
        public void thingUpdated(Thing thing) {
            // TODO Auto-generated method stub
        }

        @Override
        public void validateConfigurationParameters(Thing thing,
                Map<@NonNull String, @NonNull Object> configurationParameters) {
            // TODO Auto-generated method stub
        }

        @Override
        public void validateConfigurationParameters(Channel channel,
                Map<@NonNull String, @NonNull Object> configurationParameters) {
            // TODO Auto-generated method stub
        }

        @Override
        public @Nullable ConfigDescription getConfigDescription(ChannelTypeUID channelTypeUID) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable ConfigDescription getConfigDescription(ThingTypeUID thingTypeUID) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void configurationUpdated(Thing thing) {
            // TODO Auto-generated method stub
        }

        @Override
        public void migrateThingType(Thing thing, ThingTypeUID thingTypeUID, Configuration configuration) {
            // TODO Auto-generated method stub
        }

        @Override
        public void channelTriggered(Thing thing, ChannelUID channelUID, String event) {
            // TODO Auto-generated method stub
        }

        @Override
        public ChannelBuilder createChannelBuilder(ChannelUID channelUID, ChannelTypeUID channelTypeUID) {
            return mock(ChannelBuilder.class);
        }

        @Override
        public ChannelBuilder editChannel(Thing thing, ChannelUID channelUID) {
            return mock(ChannelBuilder.class);
        }

        @Override
        public List<@NonNull ChannelBuilder> createChannelBuilders(ChannelGroupUID channelGroupUID,
                ChannelGroupTypeUID channelGroupTypeUID) {
            return new ArrayList<ChannelBuilder>();
        }

        @Override
        public boolean isChannelLinked(ChannelUID channelUID) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public @Nullable Bridge getBridge(ThingUID bridgeUID) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
