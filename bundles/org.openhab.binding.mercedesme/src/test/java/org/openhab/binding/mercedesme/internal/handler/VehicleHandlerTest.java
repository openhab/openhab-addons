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
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

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
        VehicleHandler vh = new VehicleHandler(thingMock, mock(MercedesMeCommandOptionProvider.class),
                mock(MercedesMeStateOptionProvider.class), mock(MercedesMeDynamicStateDescriptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
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
    }
}
