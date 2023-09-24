package org.openhab.binding.mercedesme.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.FileReader;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.MercedesMeCommandOptionProvider;
import org.openhab.binding.mercedesme.internal.MercedesMeDynamicStateDescriptionProvider;
import org.openhab.binding.mercedesme.internal.MercedesMeStateOptionProvider;
import org.openhab.binding.mercedesme.internal.config.VehicleConfiguration;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.RefreshType;

import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;

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
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
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
    public void testBEVImperialUnits() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        MercedesMeCommandOptionProvider commandOptionMock = new MercedesMeCommandOptionProviderMock();
        VehicleHandler vh = new VehicleHandler(thingMock, commandOptionMock, mock(MercedesMeStateOptionProvider.class),
                mock(MercedesMeDynamicStateDescriptionProvider.class));
        AuccountHandlerMock ahm = new AuccountHandlerMock();
        vh.accountHandler = Optional.of(ahm);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-ImperialUnits.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
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
        assertTrue(updateListener.updatesReceived.get("test::bev:range#mileage").toFullString().endsWith("mi"),
                "Mileague Unit");
        assertTrue(updateListener.updatesReceived.get("test::bev:range#range-electric").toFullString().endsWith("mi"),
                "Range Electric Unit");
        assertTrue(updateListener.updatesReceived.get("test::bev:trip#distance").toFullString().endsWith("mi"),
                "Range Electric Unit");
        assertTrue(updateListener.updatesReceived.get("test::bev:tires#pressure-front-left").toFullString()
                .endsWith("psi"), "Pressure Unit");
        assertTrue(updateListener.updatesReceived.get("test::bev:hvac#temperature").toFullString().endsWith("°F"),
                "Temperature Unit");

        System.out.println("--- Switch to Celsius");
        // overwrite with EU Units
        json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        update = ProtoConverter.json2Proto(json, true);
        vh.distributeContent(update);
    }

    @Test
    public void testBEVCharging() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, mock(MercedesMeCommandOptionProvider.class),
                mock(MercedesMeStateOptionProvider.class), mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA-Charging.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
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
        assertEquals("2023-09-06T13:55:00.000+0200",
                updateListener.updatesReceived.get("test::bev:charge#end-time").toFullString(), "End of Charge Time");
    }

    @Test
    public void testBEVPartialChargingUpdate() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, mock(MercedesMeCommandOptionProvider.class),
                mock(MercedesMeStateOptionProvider.class), mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/PartialUpdate-Charging.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, false);
        vh.distributeContent(update);
        assertEquals(2, updateListener.updatesReceived.size(), "Update Count");
        assertEquals("2023-09-19T20:45:00.000+0200",
                updateListener.updatesReceived.get("test::bev:charge#end-time").toFullString(), "End of Charge Time");
        assertEquals("2.1 kW", updateListener.updatesReceived.get("test::bev:charge#power").toFullString(),
                "Charge Power");
    }

    @Test
    public void testBEVPartialGPSUpdate() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, mock(MercedesMeCommandOptionProvider.class),
                mock(MercedesMeStateOptionProvider.class), mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/PartialUpdate-GPS.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, false);
        vh.distributeContent(update);
        assertEquals(2, updateListener.updatesReceived.size(), "Update Count");
        assertEquals("1.23,4.56", updateListener.updatesReceived.get("test::bev:position#gps").toFullString(),
                "GPS update");
        assertEquals("41.9 °", updateListener.updatesReceived.get("test::bev:position#heading").toFullString(),
                "Heading Update");
    }

    @Test
    public void testBEVPartialRangeUpdate() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, mock(MercedesMeCommandOptionProvider.class),
                mock(MercedesMeStateOptionProvider.class), mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/PartialUpdate-Range.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, false);
        vh.distributeContent(update);
        assertEquals(3, updateListener.updatesReceived.size(), "Update Count");
        assertEquals("15017 km", updateListener.updatesReceived.get("test::bev:range#mileage").toFullString(),
                "Mileage Update");
        assertEquals("246 km", updateListener.updatesReceived.get("test::bev:range#radius-electric").toFullString(),
                "Range Update");
        assertEquals("307 km", updateListener.updatesReceived.get("test::bev:range#range-electric").toFullString(),
                "Range Radius Update");
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
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-Hybrid-Charging.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
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
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-Hybrid-Charging.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.distributeContent(update);

        // Test charged / uncharged battery and filled / unfilled tank volume
        assertEquals("5.800000190734863 kWh",
                updateListener.updatesReceived.get("test::hybrid:range#charged").toFullString(),
                "Battery Charged Update");
        assertEquals("3.4000000953674316 kWh",
                updateListener.updatesReceived.get("test::hybrid:range#uncharged").toFullString(),
                "Battery Uncharged Update");
        assertEquals("9.579999923706055 l",
                updateListener.updatesReceived.get("test::hybrid:range#tank-remain").toFullString(),
                "Tank Remain Update");
        assertEquals("50.31999969482422 l",
                updateListener.updatesReceived.get("test::hybrid:range#tank-open").toFullString(), "Tank Open Update");
    }

    @Test
    public void testEventStorage() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, mock(MercedesMeCommandOptionProvider.class),
                mock(MercedesMeStateOptionProvider.class), mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        updateListener.linked = true;
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.distributeContent(update);

        assertEquals(11, updateListener.updatesPerGroupMap.size(), "Group Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("doors"), "Doors Update Count");
        // 1 update more due to proto channel connected
        // assertEquals(6, updateListener.getUpdatesForGroup("vehicle"), "Vehcile Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("windows"), "Windows Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("trip"), "Trip Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("tires"), "Tire Update Count");
        assertEquals(6, updateListener.getUpdatesForGroup("service"), "Service Update Count");
        assertEquals(6, updateListener.getUpdatesForGroup("range"), "Range Update Count");
        assertEquals(2, updateListener.getUpdatesForGroup("position"), "Position Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("lock"), "Lock Update Count");
        assertEquals(7, updateListener.getUpdatesForGroup("hvac"), "HVAC Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("charge"), "Charge Update Count");

        /**
         * VehicleHandler fully updated eventStorage shall contain all data
         * Let's simulate an item ad causing a RefreshType command
         * Shall deliver data immediately
         */
        assertEquals(82, vh.eventStorage.size());
        assertEquals(82, updateListener.updatesReceived.size());
        updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        ChannelUID mileageChannelUID = new ChannelUID(new ThingUID("test", Constants.BEV), Constants.GROUP_RANGE,
                "mileage");

        vh.handleCommand(mileageChannelUID, RefreshType.REFRESH);
        assertEquals(1, updateListener.updatesReceived.size());
    }

    @Test
    public void testProtoChannelLinked() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, mock(MercedesMeCommandOptionProvider.class),
                mock(MercedesMeStateOptionProvider.class), mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.distributeContent(update);
        assertFalse(updateListener.updatesReceived.containsKey("test::bev:vehicle#proto-update"),
                "Proto Channel not updated");

        updateListener.linked = true;
        vh.distributeContent(update);
        assertTrue(updateListener.updatesReceived.containsKey("test::bev:vehicle#proto-update"),
                "Proto Channel not updated");
    }

    @Test
    public void testTemperaturePoints() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, mock(MercedesMeCommandOptionProvider.class),
                mock(MercedesMeStateOptionProvider.class), mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-Unknown.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.distributeContent(update);
        assertEquals("22 °C", updateListener.updatesReceived.get("test::bev:hvac#temperature").toFullString(),
                "Temperature Point One Updated");

        System.out.println("---");
        ChannelUID cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_HVAC, "zone");
        updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        vh.handleCommand(cuid, new DecimalType(2));
        assertEquals("19 °C", updateListener.updatesReceived.get("test::bev:hvac#temperature").toFullString(),
                "Temperature Point One Updated");
    }

    @Test
    public void testTemperaturePointSelection() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        MercedesMeCommandOptionProvider commandOptionMock = new MercedesMeCommandOptionProviderMock();
        AuccountHandlerMock ahm = new AuccountHandlerMock();
        VehicleHandler vh = new VehicleHandler(thingMock, commandOptionMock, mock(MercedesMeStateOptionProvider.class),
                mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(ahm);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        System.out.println("---");
        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-Unknown.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.distributeContent(update);

        ChannelUID cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_HVAC, "temperature");
        updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        vh.handleCommand(cuid, QuantityType.valueOf("18 °C"));
        System.out.println(ahm.getCommand());
        System.out.println(Utils.getZoneNumber(ahm.getCommand().get("zone").toString()));
        vh.handleCommand(cuid, QuantityType.valueOf("80 °F"));
        System.out.println(ahm.getCommand());
        System.out.println(Utils.getZoneNumber(ahm.getCommand().get("zone").toString()));
    }

    @Test
    public void testChargeProgramSelection() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        MercedesMeCommandOptionProvider commandOptionMock = new MercedesMeCommandOptionProviderMock();
        AuccountHandlerMock ahm = new AuccountHandlerMock();
        VehicleHandler vh = new VehicleHandler(thingMock, commandOptionMock, mock(MercedesMeStateOptionProvider.class),
                mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(ahm);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.distributeContent(update);

        ChannelUID cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_CHARGE, "max-soc");
        updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        vh.handleCommand(cuid, QuantityType.valueOf("90 %"));
        System.out.println(ahm.getCommand());
        System.out.println("---");

        cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_CHARGE, "program");
        vh.handleCommand(cuid, new DecimalType(3));
        System.out.println("---");

        cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_CHARGE, "program");
        vh.handleCommand(cuid, new DecimalType(2));
        System.out.println("---");

        cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_CHARGE, "program");
        vh.handleCommand(cuid, new DecimalType(0));
        System.out.println("---");

        cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_CHARGE, "program");
        vh.handleCommand(cuid, new DecimalType(5));
    }
}
