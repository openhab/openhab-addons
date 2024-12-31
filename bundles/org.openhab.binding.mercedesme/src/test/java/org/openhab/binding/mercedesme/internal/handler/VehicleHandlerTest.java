/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mercedesme.internal.Constants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.FileReader;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.MercedesMeCommandOptionProvider;
import org.openhab.binding.mercedesme.internal.MercedesMeStateOptionProvider;
import org.openhab.binding.mercedesme.internal.config.VehicleConfiguration;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinCommandStatus;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinCommandStatusUpdatesByPID;

/**
 * {@link VehicleHandlerTest} check state updates and command sending of vehicles
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Additional test for https://github.com/openhab/openhab-addons/issues/16932
 */
@NonNullByDefault
class VehicleHandlerTest {
    public static final int GROUP_COUNT = 12;

    public static final int ECOSCORE_UPDATE_COUNT = 4;
    public static final int HVAC_UPDATE_COUNT = 9;
    public static final int POSITIONING_UPDATE_COUNT = 3;

    private static final int EVENT_STORAGE_COUNT = HVAC_UPDATE_COUNT + POSITIONING_UPDATE_COUNT + ECOSCORE_UPDATE_COUNT
            + 76;

    public static Map<String, Object> createBEV() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        when(thingMock.getProperties()).thenReturn(Map.of(MB_KEY_COMMAND_CHARGE_PROGRAM_CONFIGURE, "true"));
        AccountHandlerMock ahm = new AccountHandlerMock();
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(ahm);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        Map<String, Object> instances = new HashMap<>();
        instances.put(ThingCallbackListener.class.getCanonicalName(), updateListener);
        instances.put(VehicleHandler.class.getCanonicalName(), vh);
        instances.put(AccountHandlerMock.class.getCanonicalName(), ahm);
        return instances;
    }

    public static Map<String, Object> createCombustion() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_COMB);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.COMBUSTION));
        AccountHandlerMock ahm = new AccountHandlerMock();
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(ahm);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        Map<String, Object> instances = new HashMap<>();
        instances.put(ThingCallbackListener.class.getCanonicalName(), updateListener);
        instances.put(VehicleHandler.class.getCanonicalName(), vh);
        instances.put(AccountHandlerMock.class.getCanonicalName(), ahm);
        return instances;
    }

    @Test
    public void testBEVFullUpdateNoCapacities() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(GROUP_COUNT, updateListener.updatesPerGroupMap.size(), "Group Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("doors"), "Doors Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("vehicle"), "Vehcile Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("windows"), "Windows Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("trip"), "Trip Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("tires"), "Tire Update Count");
        assertEquals(6, updateListener.getUpdatesForGroup("service"), "Service Update Count");
        assertEquals(7, updateListener.getUpdatesForGroup("range"), "Range Update Count");
        assertEquals(POSITIONING_UPDATE_COUNT, updateListener.getUpdatesForGroup("position"), "Position Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("lock"), "Lock Update Count");
        assertEquals(HVAC_UPDATE_COUNT, updateListener.getUpdatesForGroup("hvac"), "HVAC Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("charge"), "Charge Update Count");
    }

    @Test
    public void testBEVImperialUnits() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        MercedesMeDynamicStateDescriptionProviderMock<?> patternMock = new MercedesMeDynamicStateDescriptionProviderMock<>(
                mock(EventPublisher.class), mock(ItemChannelLinkRegistry.class),
                mock(ChannelTypeI18nLocalizationService.class));
        MercedesMeCommandOptionProviderMock commandOptionMock = new MercedesMeCommandOptionProviderMock();
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(), commandOptionMock, patternMock);

        AccountHandlerMock ahm = new AccountHandlerMock();
        vh.accountHandler = Optional.of(ahm);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-ImperialUnits.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(GROUP_COUNT, updateListener.updatesPerGroupMap.size(), "Group Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("doors"), "Doors Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("vehicle"), "Vehcile Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("windows"), "Windows Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("trip"), "Trip Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("tires"), "Tire Update Count");
        assertEquals(6, updateListener.getUpdatesForGroup("service"), "Service Update Count");
        assertEquals(7, updateListener.getUpdatesForGroup("range"), "Range Update Count");
        assertEquals(POSITIONING_UPDATE_COUNT, updateListener.getUpdatesForGroup("position"), "Position Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("lock"), "Lock Update Count");
        assertEquals(HVAC_UPDATE_COUNT, updateListener.getUpdatesForGroup("hvac"), "HVAC Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("charge"), "Charge Update Count");
        // Cable unplugged = 3
        assertEquals("3", updateListener.getResponse("test::bev:charge#status").toFullString(), "Charge Error");
        // No Error = 0
        assertEquals("0", updateListener.getResponse("test::bev:charge#error").toFullString(), "Charge Error");
        assertTrue(updateListener.getResponse("test::bev:range#mileage").toFullString().endsWith("mi"),
                "Mileague Unit");
        assertTrue(updateListener.getResponse("test::bev:range#range-electric").toFullString().endsWith("mi"),
                "Range Electric Unit");
        assertTrue(updateListener.getResponse("test::bev:trip#distance").toFullString().endsWith("mi"),
                "Range Electric Unit");
        assertTrue(updateListener.getResponse("test::bev:tires#pressure-front-left").toFullString().endsWith("psi"),
                "Pressure Unit");
        assertTrue(updateListener.getResponse("test::bev:hvac#temperature").toFullString().endsWith("°F"),
                "Temperature Unit");
        assertEquals("%.0f °F", patternMock.patternMap.get("test::bev:hvac#temperature"), "Temperature Pattern");
        commandOptionMock.getCommandList("test::bev:hvac#temperature").forEach(cmd -> {
            assertTrue(cmd.getCommand().endsWith(" °F"), "Command Option Fahrenheit Unit");
        });

        // overwrite with EU Units
        json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals("%.1f °C", patternMock.patternMap.get("test::bev:hvac#temperature"), "Temperature Pattern");
        commandOptionMock.getCommandList("test::bev:hvac#temperature").forEach(cmd -> {
            assertTrue(cmd.getCommand().endsWith(" °C"), "Command Option Celsius Unit");
        });
    }

    @Test
    public void testBEVCharging() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA-Charging.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(GROUP_COUNT, updateListener.updatesPerGroupMap.size(), "Group Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("doors"), "Doors Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("vehicle"), "Vehcile Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("windows"), "Windows Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("trip"), "Trip Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("tires"), "Tire Update Count");
        assertEquals(6, updateListener.getUpdatesForGroup("service"), "Service Update Count");
        assertEquals(7, updateListener.getUpdatesForGroup("range"), "Range Update Count");
        assertEquals(POSITIONING_UPDATE_COUNT, updateListener.getUpdatesForGroup("position"), "Position Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("lock"), "Lock Update Count");
        assertEquals(HVAC_UPDATE_COUNT, updateListener.getUpdatesForGroup("hvac"), "HVAC Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("charge"), "Charge Update Count");
        assertEquals("2023-09-06 13:55", ((DateTimeType) updateListener.getResponse("test::bev:charge#end-time"))
                .format("%1$tY-%1$tm-%1$td %1$tH:%1$tM"), "End of Charge Time");
        // Charging = 0
        assertEquals("0", updateListener.getResponse("test::bev:charge#status").toFullString(), "Charge Status");
        // No Error = 0
        assertEquals("0", updateListener.getResponse("test::bev:charge#error").toFullString(), "Charge Error");
    }

    @Test
    public void testBEVChargeEndtime() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA-Charging-Weekday.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals("2023-09-09 13:54", ((DateTimeType) updateListener.getResponse("test::bev:charge#end-time"))
                .format("%1$tY-%1$tm-%1$td %1$tH:%1$tM"), "End of Charge Time");

        json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA-Charging-Weekday-Underrun.json");
        update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals("2023-09-11 13:55", ((DateTimeType) updateListener.getResponse("test::bev:charge#end-time"))
                .format("%1$tY-%1$tm-%1$td %1$tH:%1$tM"), "End of Charge Time");
    }

    @Test
    public void testBEVPartialChargingUpdate() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/PartialUpdate-Charging.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, false);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(2, updateListener.updatesReceived.size(), "Update Count");
        assertEquals("2023-09-19 20:45", ((DateTimeType) updateListener.getResponse("test::bev:charge#end-time"))
                .format("%1$tY-%1$tm-%1$td %1$tH:%1$tM"), "End of Charge Time");
        assertEquals("2.1 kW", updateListener.getResponse("test::bev:charge#power").toFullString(), "Charge Power");
    }

    @Test
    public void testBEVPartialGPSUpdate() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/PartialUpdate-GPS.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, false);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();
        assertEquals(3, updateListener.updatesReceived.size(), "Update Count");
        assertEquals("1.23,4.56", updateListener.getResponse("test::bev:position#gps").toFullString(), "GPS update");
        assertEquals("41.9 °", updateListener.getResponse("test::bev:position#heading").toFullString(),
                "Heading Update");
    }

    @Test
    public void testBEVPartialRangeUpdate() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/PartialUpdate-Range.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, false);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(3, updateListener.updatesReceived.size(), "Update Count");
        assertEquals("15017 km", updateListener.getResponse("test::bev:range#mileage").toFullString(),
                "Mileage Update");
        assertEquals("246 km", updateListener.getResponse("test::bev:range#radius-electric").toFullString(),
                "Range Update");
        assertEquals("307 km", updateListener.getResponse("test::bev:range#range-electric").toFullString(),
                "Range Radius Update");
    }

    @Test
    public void testHybridFullUpdateNoCapacities() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_HYBRID);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.HYBRID));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-Hybrid-Charging.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(GROUP_COUNT, updateListener.updatesPerGroupMap.size(), "Group Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("doors"), "Doors Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("vehicle"), "Vehcile Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("windows"), "Windows Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("trip"), "Trip Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("tires"), "Trip Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("service"), "Trip Update Count");
        assertEquals(14, updateListener.getUpdatesForGroup("range"), "Update Upadte Count");
        assertEquals(POSITIONING_UPDATE_COUNT, updateListener.getUpdatesForGroup("position"), "Update Upadte Count");
        assertEquals(6, updateListener.getUpdatesForGroup("lock"), "Lock Update Count");
        assertEquals(HVAC_UPDATE_COUNT, updateListener.getUpdatesForGroup("hvac"), "HVAC Update Count");
        assertEquals(9, updateListener.getUpdatesForGroup("charge"), "Charge Update Count");
    }

    @Test
    public void testHybridFullUpadteWithCapacities() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_HYBRID);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", "hybrid"));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vehicleConfig.batteryCapacity = (float) 9.2;
        vehicleConfig.fuelCapacity = (float) 59.9;
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-Hybrid-Charging.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        // Test charged / uncharged battery and filled / unfilled tank volume
        assertEquals("5.800000190734863 kWh", updateListener.getResponse("test::hybrid:range#charged").toFullString(),
                "Battery Charged Update");
        assertEquals("3.4000000953674316 kWh",
                updateListener.getResponse("test::hybrid:range#uncharged").toFullString(), "Battery Uncharged Update");
        assertEquals("9.579999923706055 l", updateListener.getResponse("test::hybrid:range#tank-remain").toFullString(),
                "Tank Remain Update");
        assertEquals("50.31999969482422 l", updateListener.getResponse("test::hybrid:range#tank-open").toFullString(),
                "Tank Open Update");
    }

    @Test
    public void testEventStorage() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        updateListener.linked = true;
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(GROUP_COUNT, updateListener.updatesPerGroupMap.size(), "Group Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("doors"), "Doors Update Count");
        // 1 update more due to proto channel connected
        // assertEquals(6, updateListener.getUpdatesForGroup("vehicle"), "Vehcile Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("windows"), "Windows Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("trip"), "Trip Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("tires"), "Tire Update Count");
        assertEquals(6, updateListener.getUpdatesForGroup("service"), "Service Update Count");
        assertEquals(7, updateListener.getUpdatesForGroup("range"), "Range Update Count");
        assertEquals(POSITIONING_UPDATE_COUNT, updateListener.getUpdatesForGroup("position"), "Position Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("lock"), "Lock Update Count");
        assertEquals(HVAC_UPDATE_COUNT, updateListener.getUpdatesForGroup("hvac"), "HVAC Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("charge"), "Charge Update Count");

        /**
         * VehicleHandler fully updated eventStorage shall contain all data
         * Let's simulate an item ad causing a RefreshType command
         * Shall deliver data immediately
         */
        assertEquals(EVENT_STORAGE_COUNT, vh.eventStorage.size());
        assertEquals(EVENT_STORAGE_COUNT, updateListener.updatesReceived.size());
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
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();
        assertFalse(updateListener.updatesReceived.containsKey("test::bev:vehicle#proto-update"),
                "Proto Channel not updated");

        updateListener.linked = true;
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();
        assertTrue(updateListener.updatesReceived.containsKey("test::bev:vehicle#proto-update"),
                "Proto Channel not updated");
    }

    @Test
    public void testTemperaturePoints() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(mock(AccountHandler.class));
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        AccountHandlerMock ahm = new AccountHandlerMock();
        vh.accountHandler = Optional.of(ahm);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-Unknown.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();
        assertEquals("22 °C", updateListener.getResponse("test::bev:hvac#temperature").toFullString(),
                "Temperature Point One Updated");

        ChannelUID cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_HVAC, "zone");
        updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        vh.handleCommand(cuid, new DecimalType(2));
        assertEquals("2", updateListener.getResponse("test::bev:hvac#zone").toFullString(),
                "Temperature Point One Updated");
        assertEquals("19 °C", updateListener.getResponse("test::bev:hvac#temperature").toFullString(),
                "Temperature Point One Updated");
        vh.handleCommand(cuid, new DecimalType(-1));
    }

    @Test
    public void testTemperaturePointSelection() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        when(thingMock.getProperties()).thenReturn(Map.of(MB_KEY_COMMAND_ZEV_PRECONDITION_CONFIGURE, "true"));
        AccountHandlerMock ahm = new AccountHandlerMock();
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(ahm);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-Unknown.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        ChannelUID cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_HVAC, "temperature");
        updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        vh.handleCommand(cuid, QuantityType.valueOf("18 °C"));
        assertEquals("frontLeft", ahm.getCommand().get("zone").toString(), "Zone Selection");
        assertEquals(18, ahm.getCommand().getDouble("temperature_in_celsius"), "Temperature Selection");
        vh.handleCommand(cuid, QuantityType.valueOf("80 °F"));
        assertEquals("frontLeft", ahm.getCommand().get("zone").toString(), "Zone Selection");
        assertEquals(26, ahm.getCommand().getDouble("temperature_in_celsius"), "Temperature Selection");
    }

    @Test
    public void testChargeProgramSelection() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        when(thingMock.getProperties()).thenReturn(Map.of(MB_KEY_COMMAND_CHARGE_PROGRAM_CONFIGURE, "true"));
        AccountHandlerMock ahm = new AccountHandlerMock();
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = Optional.of(ahm);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = Optional.of(vehicleConfig);
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        ChannelUID cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_CHARGE, "max-soc");
        vh.handleCommand(cuid, QuantityType.valueOf("90 %"));
        int selectedChargeProgram = ((DecimalType) updateListener.getResponse("test::bev:charge#program")).intValue();
        assertEquals(selectedChargeProgram,
                Utils.getChargeProgramNumber(ahm.getCommand().get("charge_program").toString()),
                "Charge Program Command");
        assertEquals(90, ahm.getCommand().getInt("max_soc"), "Charge Program SOC Setting");

        cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_CHARGE, "program");
        vh.handleCommand(cuid, new DecimalType(3));
        assertEquals(3, Utils.getChargeProgramNumber(ahm.getCommand().get("charge_program").toString()),
                "Charge Program Command");
        assertEquals(100, ahm.getCommand().getInt("max_soc"), "Charge Program SOC Setting");
    }

    @Test
    /**
     * Testing UNRECOGNIZED (-1) values in CommandStatus which throws Exception
     */
    public void testCommandDistribution() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        AppTwinCommandStatus command = AppTwinCommandStatus.newBuilder().setStateValue(-1).setTypeValue(-1).build();
        AppTwinCommandStatusUpdatesByPID commandPid = AppTwinCommandStatusUpdatesByPID.newBuilder()
                .putUpdatesByPid(Long.MIN_VALUE, command).build();
        try {
            vh.distributeCommandStatus(commandPid);
        } catch (IllegalArgumentException iae) {
            fail();
        }
    }

    @Test
    public void testPositioning() {
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(POSITIONING_UPDATE_COUNT, updateListener.getUpdatesForGroup("position"), "Position Update Count");
        assertEquals("1.23,4.56", updateListener.getResponse("test::bev:position#gps").toFullString(),
                "Positioning GPS");
        assertEquals("44.5 °", updateListener.getResponse("test::bev:position#heading").toFullString(),
                "Positioning Heading");
        assertEquals(5, ((DecimalType) updateListener.getResponse("test::bev:position#status")).intValue(),
                "Positioning Status");
    }

    @Test
    public void testHVAC() {
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(HVAC_UPDATE_COUNT, updateListener.getUpdatesForGroup("hvac"), "HVAC Update Count");
        assertEquals(0, ((DecimalType) updateListener.getResponse("test::bev:hvac#ac-status")).intValue(),
                "AC Statuns");
        assertEquals(UnDefType.UNDEF, updateListener.getResponse("test::bev:hvac#aux-status"), "Aux Heating Status");
    }

    @Test
    public void testEcoScore() {
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-BEV-EQA.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals("72 %", updateListener.getResponse("test::bev:eco#accel").toFullString(), "Eco Acceleration");
        assertEquals("81 %", updateListener.getResponse("test::bev:eco#coasting").toFullString(), "Eco Coasting");
        assertEquals("60 %", updateListener.getResponse("test::bev:eco#constant").toFullString(), "Eco Constant");
        assertEquals("10.2 km", updateListener.getResponse("test::bev:eco#bonus").toFullString(), "Eco Bonus");
        assertEquals(ECOSCORE_UPDATE_COUNT, updateListener.getUpdatesForGroup("eco"), "ECO Update Count");
    }

    @Test
    public void testAdBlue() {
        Map<String, Object> instances = createCombustion();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);

        String json = FileReader.readFileInString("src/test/resources/proto-json/MB-Combustion.json");
        VEPUpdate update = ProtoConverter.json2Proto(json, true);
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals("29 %", updateListener.getResponse("test::combustion:range#adblue-level").toFullString(),
                "AdBlue Tank Level");
    }
}
