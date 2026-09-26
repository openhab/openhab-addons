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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mercedesme.internal.Constants.*;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.FileReader;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.MercedesMeCommandOptionProvider;
import org.openhab.binding.mercedesme.internal.MercedesMeStateOptionProvider;
import org.openhab.binding.mercedesme.internal.config.VehicleConfiguration;
import org.openhab.binding.mercedesme.internal.utils.Mapper;
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

import com.daimler.mbcarkit.proto.VehicleEvents.AttributeStatus;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargingErrorDetails;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargingErrorDetailsEnumAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Chargingstatus;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargingstatusEnumAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleDistanceAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoublePressureAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleTemperatureAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Int64ClockHourAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Int64DistanceAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Int64RatioAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePointsArrayAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.VSUMetadata;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdate;
import com.daimler.mbcarkit.proto.VehicleEvents.Weekday;
import com.daimler.mbcarkit.proto.VehicleEvents.WeekdayEnumAttribute;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinCommandStatus;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinCommandStatusUpdatesByPID;
import com.google.protobuf.Timestamp;

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
            + 77;

    private static final String DATE_TIME_FORMAT = "%1$tY-%1$tm-%1$td %1$tH:%1$tM";

    @BeforeAll
    public static void init() {
        Utils.initialize(Utils.timeZoneProvider, Utils.localeProvider);
    }

    public static Map<String, Object> createBEV() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        when(thingMock.getProperties()).thenReturn(Map.of(MB_KEY_COMMAND_CHARGE_PROGRAM_CONFIGURE, "true"));
        AccountHandlerMock ahm = new AccountHandlerMock();
        MercedesMeCommandOptionProviderMock commandOptionMock = new MercedesMeCommandOptionProviderMock();
        MercedesMeDynamicStateDescriptionProviderMock<?> patternMock = new MercedesMeDynamicStateDescriptionProviderMock<>(
                mock(EventPublisher.class), mock(ItemChannelLinkRegistry.class),
                mock(ChannelTypeI18nLocalizationService.class));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(), commandOptionMock, patternMock);
        vh.accountHandler = ahm;
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = vehicleConfig;
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        Map<String, Object> instances = new HashMap<>();
        instances.put(Thing.class.getCanonicalName(), thingMock);
        instances.put(ThingCallbackListener.class.getCanonicalName(), updateListener);
        instances.put(VehicleHandler.class.getCanonicalName(), vh);
        instances.put(MercedesMeCommandOptionProviderMock.class.getCanonicalName(), commandOptionMock);
        instances.put(MercedesMeDynamicStateDescriptionProviderMock.class.getCanonicalName(), patternMock);
        return instances;
    }

    public static Map<String, Object> createCombustion() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_COMB);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.COMBUSTION));
        AccountHandlerMock ahm = new AccountHandlerMock();
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = ahm;
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = vehicleConfig;
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        Map<String, Object> instances = new HashMap<>();
        instances.put(ThingCallbackListener.class.getCanonicalName(), updateListener);
        instances.put(VehicleHandler.class.getCanonicalName(), vh);
        instances.put(AccountHandlerMock.class.getCanonicalName(), ahm);
        return instances;
    }

    private static String loadRaw(String filename) {
        return FileReader.readRawFileInString(filename);
    }

    /**
     * Two-zone temperature update (frontLeft 22 °C, frontRight 19 °C) built directly: no real capture has
     * more than one temperature zone, and the exact zone layout matters for the zone-switch command tests.
     */
    private static VehicleStatusAttributes buildTwoZoneTemperatureUpdate() {
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID).build();
        TemperaturePointsArrayAttribute.TemperaturePoint zone1 = TemperaturePointsArrayAttribute.TemperaturePoint
                .newBuilder().setZone(TemperaturePointsArrayAttribute.TemperaturePoint.Zone.FRONT_LEFT)
                .setTemperature(DoubleTemperatureAttribute.newBuilder().setValue(22.0)
                        .setUnit(VehicleAttributeStatus.TemperatureUnit.CELSIUS).setDisplayValue("22.0").build())
                .setActive(true).build();
        TemperaturePointsArrayAttribute.TemperaturePoint zone2 = TemperaturePointsArrayAttribute.TemperaturePoint
                .newBuilder().setZone(TemperaturePointsArrayAttribute.TemperaturePoint.Zone.FRONT_RIGHT)
                .setTemperature(DoubleTemperatureAttribute.newBuilder().setValue(19.0)
                        .setUnit(VehicleAttributeStatus.TemperatureUnit.CELSIUS).setDisplayValue("19.0").build())
                .setActive(true).build();
        TemperaturePointsArrayAttribute temperaturePoints = TemperaturePointsArrayAttribute.newBuilder().addValue(zone1)
                .addValue(zone2).setMetadata(metadata).build();
        VehicleStatusUpdate vsu = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(true)
                .setTemperaturePoints(temperaturePoints).build();
        return new VehicleStatusAttributes(true, Mapper.fromVehicleStatusUpdate(vsu));
    }

    @Test
    public void testBEVFullUpdateNoCapacities() {
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);

        String raw = loadRaw("src/test/resources/vehiclestatusupdates/vsu-eqa-2.raw");
        VehicleStatusAttributes update = ProtoConverter.raw2Proto(raw, true);
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(GROUP_COUNT, updateListener.updatesPerGroupMap.size(), "Group Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("doors"), "Doors Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("vehicle"), "Vehcile Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("windows"), "Windows Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("trip"), "Trip Update Count");
        assertEquals(10, updateListener.getUpdatesForGroup("tires"), "Tire Update Count");
        assertEquals(6, updateListener.getUpdatesForGroup("service"), "Service Update Count");
        assertEquals(8, updateListener.getUpdatesForGroup("range"), "Range Update Count");
        assertEquals(POSITIONING_UPDATE_COUNT, updateListener.getUpdatesForGroup("position"), "Position Update Count");
        assertEquals(5, updateListener.getUpdatesForGroup("lock"), "Lock Update Count");
        assertEquals(HVAC_UPDATE_COUNT, updateListener.getUpdatesForGroup("hvac"), "HVAC Update Count");
        assertEquals(12, updateListener.getUpdatesForGroup("charge"), "Charge Update Count");
    }

    @Test
    public void testBEVImperialUnits() {
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        MercedesMeCommandOptionProviderMock commandOptionMock = (MercedesMeCommandOptionProviderMock) instances
                .get(MercedesMeCommandOptionProviderMock.class.getCanonicalName());
        MercedesMeDynamicStateDescriptionProviderMock<?> patternMock = (MercedesMeDynamicStateDescriptionProviderMock<?>) instances
                .get(MercedesMeDynamicStateDescriptionProviderMock.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);
        assertNotNull(commandOptionMock);
        assertNotNull(patternMock);

        // no real capture with imperial units - one representative field per UOM category
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID).build();
        Int64DistanceAttribute rangeElectricMiles = Int64DistanceAttribute.newBuilder().setValue(190)
                .setUnit(VehicleAttributeStatus.DistanceUnit.MILES).setDisplayValue("190").setMetadata(metadata)
                .build();
        DoubleDistanceAttribute distanceStartMiles = DoubleDistanceAttribute.newBuilder().setValue(0.1)
                .setUnit(VehicleAttributeStatus.DistanceUnit.MILES).setDisplayValue("0.1").setMetadata(metadata)
                .build();
        DoublePressureAttribute tirePressurePsi = DoublePressureAttribute.newBuilder().setValue(305.0)
                .setUnit(VehicleAttributeStatus.PressureUnit.PSI).setDisplayValue("3.0").setMetadata(metadata).build();
        TemperaturePointsArrayAttribute.TemperaturePoint zoneF = TemperaturePointsArrayAttribute.TemperaturePoint
                .newBuilder().setZone(TemperaturePointsArrayAttribute.TemperaturePoint.Zone.FRONT_CENTER)
                .setTemperature(DoubleTemperatureAttribute.newBuilder().setValue(68.0)
                        .setUnit(VehicleAttributeStatus.TemperatureUnit.FAHRENHEIT).setDisplayValue("68.0").build())
                .setActive(true).build();
        TemperaturePointsArrayAttribute temperaturePointsF = TemperaturePointsArrayAttribute.newBuilder()
                .addValue(zoneF).setMetadata(metadata).build();
        ChargingstatusEnumAttribute chargingStatus = ChargingstatusEnumAttribute.newBuilder()
                .setValue(Chargingstatus.CHARGINGSTATUS_CHARGE_CABLE_UNPLUGGED).setMetadata(metadata).build();
        ChargingErrorDetailsEnumAttribute chargingError = ChargingErrorDetailsEnumAttribute.newBuilder()
                .setValue(ChargingErrorDetails.CHARGING_ERROR_DETAILS_NO_ERROR).setMetadata(metadata).build();
        VehicleStatusUpdate vsu = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(true)
                .setRangeelectric(rangeElectricMiles).setDistanceStart(distanceStartMiles)
                .setTirepressureFrontLeft(tirePressurePsi).setTemperaturePoints(temperaturePointsF)
                .setChargingstatus(chargingStatus).setChargingErrorDetails(chargingError).build();
        VehicleStatusAttributes update = new VehicleStatusAttributes(true, Mapper.fromVehicleStatusUpdate(vsu));
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        // Cable unplugged = 3
        assertEquals("3", updateListener.getResponse("test::bev:charge#status").toFullString(), "Charge Status");
        // No Error = 0
        assertEquals("0", updateListener.getResponse("test::bev:charge#error").toFullString(), "Charge Error");
        assertTrue(updateListener.getResponse("test::bev:range#range-electric").toFullString().endsWith("mi"),
                "Range Electric Unit");
        assertTrue(updateListener.getResponse("test::bev:trip#distance").toFullString().endsWith("mi"),
                "Trip Distance Unit");
        assertTrue(updateListener.getResponse("test::bev:tires#pressure-front-left").toFullString().endsWith("psi"),
                "Pressure Unit");
        assertTrue(updateListener.getResponse("test::bev:hvac#temperature").toFullString().endsWith("°F"),
                "Temperature Unit");
        assertEquals("%.0f °F", patternMock.patternMap.get("test::bev:hvac#temperature"), "Temperature Pattern");
        commandOptionMock.getCommandList("test::bev:hvac#temperature").forEach(cmd -> {
            assertTrue(cmd.getCommand().endsWith(" °F"), "Command Option Fahrenheit Unit");
        });

        // overwrite with EU Units, using a real capture
        String raw = loadRaw("src/test/resources/vehiclestatusupdates/vsu-eqa-2.raw");
        update = ProtoConverter.raw2Proto(raw, true);
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals("%.1f °C", patternMock.patternMap.get("test::bev:hvac#temperature"), "Temperature Pattern");
        commandOptionMock.getCommandList("test::bev:hvac#temperature").forEach(cmd -> {
            assertTrue(cmd.getCommand().endsWith(" °C"), "Command Option Celsius Unit");
        });
    }

    @Test
    public void testBEVCharging() {
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);

        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vehicleConfig.batteryCapacity = (float) 66.5;
        vHandler.config = vehicleConfig;

        // no real capture with an active charging session - soc/maxSoc chosen for exact arithmetic
        long timestampMs = 1700000000000L;
        int minutesAfterMidnight = 835; // 13:55
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID)
                .setTimestamp(Timestamp.newBuilder().setSeconds(timestampMs / 1000).build()).build();
        Int64RatioAttribute soc = Int64RatioAttribute.newBuilder().setValue(70)
                .setUnit(VehicleAttributeStatus.RatioUnit.PERCENT).setDisplayValue("70").setMetadata(metadata).build();
        Int64RatioAttribute maxSoc = Int64RatioAttribute.newBuilder().setValue(80)
                .setUnit(VehicleAttributeStatus.RatioUnit.PERCENT).setDisplayValue("80").setMetadata(metadata).build();
        ChargingstatusEnumAttribute chargingStatus = ChargingstatusEnumAttribute.newBuilder()
                .setValue(Chargingstatus.CHARGINGSTATUS_CHARGING).setMetadata(metadata).build();
        ChargingErrorDetailsEnumAttribute chargingError = ChargingErrorDetailsEnumAttribute.newBuilder()
                .setValue(ChargingErrorDetails.CHARGING_ERROR_DETAILS_NO_ERROR).setMetadata(metadata).build();
        Int64ClockHourAttribute endOfCharge = Int64ClockHourAttribute.newBuilder().setValue(minutesAfterMidnight)
                .setMetadata(metadata).build();
        VehicleStatusUpdate vsu = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(true)
                .setSoc(soc).setMaxSoc(maxSoc).setChargingstatus(chargingStatus).setChargingErrorDetails(chargingError)
                .setEndofchargetime(endOfCharge).build();
        VehicleStatusAttributes update = new VehicleStatusAttributes(true, Mapper.fromVehicleStatusUpdate(vsu));
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        DateTimeType expectedEndTime = Utils.getEndOfChargeTime(timestampMs, minutesAfterMidnight);
        assertEquals(expectedEndTime.format(DATE_TIME_FORMAT),
                ((DateTimeType) updateListener.getResponse("test::bev:charge#end-time")).format(DATE_TIME_FORMAT),
                "End of Charge Time");
        // Charging = 0
        assertEquals("0", updateListener.getResponse("test::bev:charge#status").toFullString(), "Charge Status");
        // No Error = 0
        assertEquals("0", updateListener.getResponse("test::bev:charge#error").toFullString(), "Charge Error");

        QuantityType<?> energy = (QuantityType<?>) updateListener.getResponse("test::bev:range#energy-to-max-soc");
        assertEquals(6.65, energy.doubleValue(), 0.001, "Energy to max SoC Update");
    }

    @Test
    public void testBEVChargeEndtime() {
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);

        // no real capture for the weekday path - reported weekday is 2 days after the first update's timestamp
        long timestampMs = 1700000000000L;
        int minutesAfterMidnight = 835; // 13:55
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID)
                .setTimestamp(Timestamp.newBuilder().setSeconds(timestampMs / 1000).build()).build();
        Int64ClockHourAttribute endOfCharge = Int64ClockHourAttribute.newBuilder().setValue(minutesAfterMidnight)
                .setMetadata(metadata).build();
        VehicleStatusUpdate vsu1 = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(true)
                .setEndofchargetime(endOfCharge).build();
        VehicleStatusAttributes update1 = new VehicleStatusAttributes(true, Mapper.fromVehicleStatusUpdate(vsu1));
        vHandler.enqueueUpdate(update1);
        updateListener.waitForUpdates();

        DateTimeType baseEndTime = (DateTimeType) updateListener.getResponse("test::bev:charge#end-time");
        assertEquals(Utils.getEndOfChargeTime(timestampMs, minutesAfterMidnight).format(DATE_TIME_FORMAT),
                baseEndTime.format(DATE_TIME_FORMAT), "End of Charge Time");

        int storedWeekday = baseEndTime.getZonedDateTime(ZoneId.systemDefault()).getDayOfWeek().getValue();
        int estimatedJavaWeekday = ((storedWeekday - 1 + 2) % 7) + 1;
        // proto Weekday is 0-based starting Monday; java DayOfWeek is 1-based starting Monday
        Weekday estimatedProtoWeekday = Weekday.values()[estimatedJavaWeekday - 1];
        WeekdayEnumAttribute weekdayAttr = WeekdayEnumAttribute.newBuilder().setValue(estimatedProtoWeekday)
                .setMetadata(metadata).build();
        VehicleStatusUpdate vsu2 = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(true)
                .setEndofchargetime(endOfCharge).setEndofChargeTimeWeekday(weekdayAttr).build();
        VehicleStatusAttributes update2 = new VehicleStatusAttributes(true, Mapper.fromVehicleStatusUpdate(vsu2));
        vHandler.enqueueUpdate(update2);
        updateListener.waitForUpdates();

        DateTimeType expectedShifted = new DateTimeType(
                baseEndTime.getZonedDateTime(ZoneId.systemDefault()).plusDays(2));
        assertEquals(expectedShifted.format(DATE_TIME_FORMAT),
                ((DateTimeType) updateListener.getResponse("test::bev:charge#end-time")).format(DATE_TIME_FORMAT),
                "End of Charge Time");
    }

    @Test
    public void testBEVPartialChargingUpdate() {
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);

        // no real capture is a pure two-field delta - built directly
        long timestampMs = 1700000000000L;
        int minutesAfterMidnight = 1245; // 20:45
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID)
                .setTimestamp(Timestamp.newBuilder().setSeconds(timestampMs / 1000).build()).build();
        Int64ClockHourAttribute endOfCharge = Int64ClockHourAttribute.newBuilder().setValue(minutesAfterMidnight)
                .setMetadata(metadata).build();
        DoubleAttribute chargingPower = DoubleAttribute.newBuilder().setValue(2.1).setMetadata(metadata).build();
        VehicleStatusUpdate vsu = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(false)
                .setEndofchargetime(endOfCharge).setChargingPower(chargingPower).build();
        VehicleStatusAttributes update = new VehicleStatusAttributes(false, Mapper.fromVehicleStatusUpdate(vsu));
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(2, updateListener.updatesReceived.size(), "Update Count");
        DateTimeType expectedEndTime = Utils.getEndOfChargeTime(timestampMs, minutesAfterMidnight);
        assertEquals(expectedEndTime.format(DATE_TIME_FORMAT),
                ((DateTimeType) updateListener.getResponse("test::bev:charge#end-time")).format(DATE_TIME_FORMAT),
                "End of Charge Time");
        assertEquals("2.1 kW", updateListener.getResponse("test::bev:charge#power").toFullString(), "Charge Power");
    }

    @Test
    public void testBEVPartialGPSUpdate() {
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);

        // no real capture for a GPS delta - built with the binding's usual anonymized coordinates
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID).build();
        DoubleAttribute lat = DoubleAttribute.newBuilder().setValue(1.23).setMetadata(metadata).build();
        DoubleAttribute lon = DoubleAttribute.newBuilder().setValue(4.56).setMetadata(metadata).build();
        DoubleAttribute heading = DoubleAttribute.newBuilder().setValue(41.9).setMetadata(metadata).build();
        VehicleStatusUpdate vsu = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(false)
                .setPositionLat(lat).setPositionLong(lon).setPositionHeading(heading).build();
        VehicleStatusAttributes update = new VehicleStatusAttributes(false, Mapper.fromVehicleStatusUpdate(vsu));
        vHandler.enqueueUpdate(update);

        updateListener.waitForUpdates();
        assertEquals(3, updateListener.updatesReceived.size(), "Update Count");
        assertEquals("1.23,4.56", updateListener.getResponse("test::bev:position#gps").toFullString(), "GPS update");
        assertEquals("41.9 °", updateListener.getResponse("test::bev:position#heading").toFullString(),
                "Heading Update");
    }

    @Test
    public void testBEVPartialRangeUpdate() {
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);

        String raw = loadRaw("src/test/resources/vehiclestatusupdates/vsu-partial-eqa.raw");
        VehicleStatusAttributes update = ProtoConverter.raw2Proto(raw, false);
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        // The capture carries 3 raw fields (maxrange, rangeelectric, overall_range), but maxrange has no
        // channel mapping and overall_range is blocked for a BEV thing, so only rangeelectric yields updates
        assertEquals(2, updateListener.updatesReceived.size(), "Update Count");
        assertEquals("345 km", updateListener.getResponse("test::bev:range#range-electric").toFullString(),
                "Range Electric Update");
        assertEquals("276 km", updateListener.getResponse("test::bev:range#radius-electric").toFullString(),
                "Range Radius Update");
    }

    @Test
    public void testHybridFullUpdateNoCapacities() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_HYBRID);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.HYBRID));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = mock(AccountHandler.class);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = vehicleConfig;
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        // No real hybrid capture exists, so this focuses on the hybrid-specific blocking regressions:
        // range-hybrid/fuel-level are blocked for a BEV but must be populated for HYBRID
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID).build();
        DoubleDistanceAttribute overallRange = DoubleDistanceAttribute.newBuilder().setValue(520.0)
                .setUnit(VehicleAttributeStatus.DistanceUnit.KILOMETERS).setDisplayValue("520").setMetadata(metadata)
                .build();
        Int64RatioAttribute tankLevel = Int64RatioAttribute.newBuilder().setValue(60)
                .setUnit(VehicleAttributeStatus.RatioUnit.PERCENT).setDisplayValue("60").setMetadata(metadata).build();
        VehicleStatusUpdate vsu = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(true)
                .setOverallRange(overallRange).setTanklevelpercent(tankLevel).build();
        VehicleStatusAttributes update = new VehicleStatusAttributes(true, Mapper.fromVehicleStatusUpdate(vsu));
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals("520 km", updateListener.getResponse("test::hybrid:range#range-hybrid").toFullString(),
                "Range Hybrid Update (blocked for BEV, must be populated for HYBRID)");
        assertEquals("60 %", updateListener.getResponse("test::hybrid:range#fuel-level").toFullString(),
                "Fuel Level Update (blocked for BEV, must be populated for a non-BEV type)");
        assertEquals("0 l", updateListener.getResponse("test::hybrid:range#tank-remain").toFullString(),
                "Tank Remain without configured fuel capacity");
        assertEquals("0 l", updateListener.getResponse("test::hybrid:range#tank-open").toFullString(),
                "Tank Open without configured fuel capacity");
    }

    @Test
    public void testHybridFullUpadteWithCapacities() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_HYBRID);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", "hybrid"));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = mock(AccountHandler.class);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vehicleConfig.batteryCapacity = (float) 9.2;
        vehicleConfig.fuelCapacity = (float) 59.9;
        vh.config = vehicleConfig;
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        // No real hybrid capture exists; soc and tank level are both 50 % so the charged/uncharged/tank
        // arithmetic is exact, and maxSoc stays unset so energy-to-max-soc remains UNDEF
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID).build();
        Int64RatioAttribute soc = Int64RatioAttribute.newBuilder().setValue(50)
                .setUnit(VehicleAttributeStatus.RatioUnit.PERCENT).setDisplayValue("50").setMetadata(metadata).build();
        Int64RatioAttribute tankLevel = Int64RatioAttribute.newBuilder().setValue(50)
                .setUnit(VehicleAttributeStatus.RatioUnit.PERCENT).setDisplayValue("50").setMetadata(metadata).build();
        VehicleStatusUpdate vsu = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(true)
                .setSoc(soc).setTanklevelpercent(tankLevel).build();
        VehicleStatusAttributes update = new VehicleStatusAttributes(true, Mapper.fromVehicleStatusUpdate(vsu));
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        // Test charged / uncharged battery and filled / unfilled tank volume
        QuantityType<?> charged = (QuantityType<?>) updateListener.getResponse("test::hybrid:range#charged");
        assertEquals(4.6, charged.doubleValue(), 0.001, "Battery Charged Update");
        QuantityType<?> uncharged = (QuantityType<?>) updateListener.getResponse("test::hybrid:range#uncharged");
        assertEquals(4.6, uncharged.doubleValue(), 0.001, "Battery Uncharged Update");
        assertEquals(UnDefType.NULL.toFullString(),
                updateListener.getResponse("test::hybrid:range#energy-to-max-soc").toFullString(), "Energy to Max SoC");
        QuantityType<?> tankRemain = (QuantityType<?>) updateListener.getResponse("test::hybrid:range#tank-remain");
        assertEquals(29.95, tankRemain.doubleValue(), 0.001, "Tank Remain Update");
        QuantityType<?> tankOpen = (QuantityType<?>) updateListener.getResponse("test::hybrid:range#tank-open");
        assertEquals(29.95, tankOpen.doubleValue(), 0.001, "Tank Open Update");
    }

    @Test
    public void testEventStorage() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = mock(AccountHandler.class);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = vehicleConfig;
        ThingCallbackListener updateListener = new ThingCallbackListener();
        updateListener.linked = true;
        vh.setCallback(updateListener);

        String raw = loadRaw("src/test/resources/vehiclestatusupdates/vsu-eqa-2.raw");
        VehicleStatusAttributes update = ProtoConverter.raw2Proto(raw, true);
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
        assertEquals(8, updateListener.getUpdatesForGroup("range"), "Range Update Count");
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
        vh.accountHandler = mock(AccountHandler.class);
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = vehicleConfig;
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String raw = loadRaw("src/test/resources/vehiclestatusupdates/vsu-eqa-2.raw");
        VehicleStatusAttributes update = ProtoConverter.raw2Proto(raw, true);
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
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        Thing thing = (Thing) instances.get(Thing.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);
        assertNotNull(thing);

        VehicleStatusAttributes update = buildTwoZoneTemperatureUpdate();
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();
        assertEquals("22 °C", updateListener.getResponse("test::bev:hvac#temperature").toFullString(),
                "Temperature Point One Updated");

        ChannelUID cuid = new ChannelUID(thing.getUID(), Constants.GROUP_HVAC, "zone");
        updateListener = new ThingCallbackListener();
        vHandler.setCallback(updateListener);
        vHandler.handleCommand(cuid, new DecimalType(2));
        assertEquals("2", updateListener.getResponse("test::bev:hvac#zone").toFullString(),
                "Temperature Point One Updated");
        assertEquals("19 °C", updateListener.getResponse("test::bev:hvac#temperature").toFullString(),
                "Temperature Point One Updated");
        vHandler.handleCommand(cuid, new DecimalType(-1));
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
        vh.accountHandler = ahm;
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = vehicleConfig;
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);
        VehicleStatusAttributes update = buildTwoZoneTemperatureUpdate();
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
        vh.accountHandler = ahm;
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = vehicleConfig;
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        String raw = loadRaw("src/test/resources/vehiclestatusupdates/vsu-eqa-2.raw");
        VehicleStatusAttributes update = ProtoConverter.raw2Proto(raw, true);
        vh.enqueueUpdate(update);
        updateListener.waitForUpdates();

        ChannelUID cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_CHARGE, "max-soc");
        vh.handleCommand(cuid, QuantityType.valueOf("90 %"));
        int selectedChargeProgram = ((DecimalType) updateListener.getResponse("test::bev:charge#program")).intValue();
        // The capture leaves selected_charge_program at its proto3 default (program 0) and protobuf omits
        // default scalar fields, so the outgoing command genuinely carries no "charge_program" key
        assertEquals(0, selectedChargeProgram, "Charge Program initially selected by vehicle");
        assertFalse(ahm.getCommand().has("charge_program"), "Charge Program Command omitted for default (0) program");
        assertEquals(90, ahm.getCommand().getInt("max_soc"), "Charge Program SOC Setting");

        cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_CHARGE, "program");
        vh.handleCommand(cuid, new DecimalType(3));
        assertEquals(3, Utils.getChargeProgramNumber(ahm.getCommand().get("charge_program").toString()),
                "Charge Program Command");
        assertEquals(100, ahm.getCommand().getInt("max_soc"), "Charge Program SOC Setting");
    }

    @Test
    public void testDoorLockUnlockSelection() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        when(thingMock.getProperties()).thenReturn(Map.of(MB_KEY_COMMAND_DOORS_LOCK, "true"));
        AccountHandlerMock ahm = new AccountHandlerMock();
        ahm.config.pin = "1234";
        VehicleHandler vh = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vh.accountHandler = ahm;
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vh.config = vehicleConfig;
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vh.setCallback(updateListener);

        ChannelUID cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_VEHICLE, "lock");

        // Lock (2) requires no PIN
        vh.handleCommand(cuid, new DecimalType(2));
        assertEquals("doorsLock", ahm.getCommand().get("commandType"), "Lock Command Type");

        // Unlock (0) requires the account's PIN
        vh.handleCommand(cuid, new DecimalType(0));
        assertEquals("doorsUnlock", ahm.getCommand().get("commandType"), "Unlock Command Type");
        assertEquals("1234", ahm.getCommand().get("pin"), "Unlock PIN");

        // Missing PIN must block the unlock command and leave the previous trace untouched
        ahm.config.pin = Constants.NOT_SET;
        vh.handleCommand(cuid, new DecimalType(0));
        assertEquals("doorsUnlock", ahm.getCommand().get("commandType"), "Unlock blocked without PIN");
        assertEquals("1234", ahm.getCommand().get("pin"), "Unlock blocked without PIN keeps last trace");
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

        String raw = loadRaw("src/test/resources/vehiclestatusupdates/vsu-eqa-2.raw");
        VehicleStatusAttributes update = ProtoConverter.raw2Proto(raw, true);
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals(POSITIONING_UPDATE_COUNT, updateListener.getUpdatesForGroup("position"), "Position Update Count");
        // position_lat/position_long are anonymized in the fixture (1.23/4.56)
        assertEquals("1.23,4.56", updateListener.getResponse("test::bev:position#gps").toFullString(),
                "Positioning GPS");
        QuantityType<?> heading = (QuantityType<?>) updateListener.getResponse("test::bev:position#heading");
        assertEquals(224.1, heading.doubleValue(), 0.0001, "Positioning Heading");
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

        String raw = loadRaw("src/test/resources/vehiclestatusupdates/vsu-eqa-2.raw");
        VehicleStatusAttributes update = ProtoConverter.raw2Proto(raw, true);
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

        String raw = loadRaw("src/test/resources/vehiclestatusupdates/vsu-eqa-2.raw");
        VehicleStatusAttributes update = ProtoConverter.raw2Proto(raw, true);
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals("2 %", updateListener.getResponse("test::bev:eco#accel").toFullString(), "Eco Acceleration");
        assertEquals("3 %", updateListener.getResponse("test::bev:eco#coasting").toFullString(), "Eco Coasting");
        assertEquals("6 %", updateListener.getResponse("test::bev:eco#constant").toFullString(), "Eco Constant");
        // ecoscorebonusrange is the proto3 default 0.0 in the capture, so compare numerically
        QuantityType<?> bonus = (QuantityType<?>) updateListener.getResponse("test::bev:eco#bonus");
        assertEquals(0.0, bonus.doubleValue(), 0.0001, "Eco Bonus");
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

        // no real capture reports AdBlue (CLA 250.raw/GLB 250.raw report VALUE_NOT_AVAILABLE)
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID).build();
        Int64RatioAttribute adBlueLevel = Int64RatioAttribute.newBuilder().setValue(29)
                .setUnit(VehicleAttributeStatus.RatioUnit.PERCENT).setDisplayValue("29").setMetadata(metadata).build();
        VehicleStatusUpdate vsu = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(true)
                .setTankLevelAdBlue(adBlueLevel).build();
        VehicleStatusAttributes update = new VehicleStatusAttributes(true, Mapper.fromVehicleStatusUpdate(vsu));
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals("29 %", updateListener.getResponse("test::combustion:range#adblue-level").toFullString(),
                "AdBlue Tank Level");
    }

    @Test
    public void testChargeProgramUpdate() {
        Map<String, Object> instances = createBEV();
        ThingCallbackListener updateListener = (ThingCallbackListener) instances
                .get(ThingCallbackListener.class.getCanonicalName());
        VehicleHandler vHandler = (VehicleHandler) instances.get(VehicleHandler.class.getCanonicalName());
        assertNotNull(updateListener);
        assertNotNull(vHandler);

        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vehicleConfig.batteryCapacity = (float) 66.5;
        vHandler.config = vehicleConfig;

        // Exercises the flat-maxSoc fallback (no chargePrograms list), with exact arithmetic:
        // (80-70)*66.5/100 = 6.65, then (90-70)*66.5/100 = 13.3
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID).build();
        Int64RatioAttribute soc = Int64RatioAttribute.newBuilder().setValue(70)
                .setUnit(VehicleAttributeStatus.RatioUnit.PERCENT).setDisplayValue("70").setMetadata(metadata).build();
        Int64RatioAttribute maxSoc80 = Int64RatioAttribute.newBuilder().setValue(80)
                .setUnit(VehicleAttributeStatus.RatioUnit.PERCENT).setDisplayValue("80").setMetadata(metadata).build();
        VehicleStatusUpdate initVsu = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(true)
                .setSoc(soc).setMaxSoc(maxSoc80).build();
        VehicleStatusAttributes update = new VehicleStatusAttributes(true, Mapper.fromVehicleStatusUpdate(initVsu));
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals("80 %", updateListener.getResponse("test::bev:charge#max-soc").toFullString(), "Max SoC init");
        QuantityType<?> energy = (QuantityType<?>) updateListener.getResponse("test::bev:range#energy-to-max-soc");
        assertEquals(6.65, energy.doubleValue(), 0.001, "Energy to max SoC init");

        // Partial update - only maxSoc changes
        Int64RatioAttribute maxSoc90 = Int64RatioAttribute.newBuilder().setValue(90)
                .setUnit(VehicleAttributeStatus.RatioUnit.PERCENT).setDisplayValue("90").setMetadata(metadata).build();
        VehicleStatusUpdate partialVsu = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN")
                .setFullUpdate(false).setMaxSoc(maxSoc90).build();
        update = new VehicleStatusAttributes(false, Mapper.fromVehicleStatusUpdate(partialVsu));
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        energy = (QuantityType<?>) updateListener.getResponse("test::bev:range#energy-to-max-soc");
        assertEquals("90 %", updateListener.getResponse("test::bev:charge#max-soc").toFullString(), "Max SoC update");
        assertEquals(13.3, energy.doubleValue(), 0.001, "Energy to max SoC Update");
    }

    /**
     * MB-BEV-CLA reports flat maxSoc attributes with no chargePrograms list: charge#max-soc must still be
     * populated, and commanding it must send ChargingConfigure instead of ChargeProgramConfigure.
     */
    @Test
    public void testMaxSocFallbackWithoutChargePrograms() {
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getUID()).thenReturn(new ThingUID("test", Constants.BEV));
        when(thingMock.getProperties()).thenReturn(Map.of(MB_KEY_COMMAND_CHARGE_PROGRAM_CONFIGURE, "true"));
        AccountHandlerMock ahm = new AccountHandlerMock();
        VehicleHandler vHandler = new VehicleHandler(thingMock, new LocationProviderMock(),
                mock(MercedesMeCommandOptionProvider.class), mock(MercedesMeStateOptionProvider.class));
        vHandler.accountHandler = ahm;
        VehicleConfiguration vehicleConfig = new VehicleConfiguration();
        vHandler.config = vehicleConfig;
        ThingCallbackListener updateListener = new ThingCallbackListener();
        vHandler.setCallback(updateListener);

        // CLA 250.raw is a real capture of this scenario: no chargePrograms list, flat maxSoc (100 %)
        String raw = loadRaw("src/test/resources/vehiclestatusupdates/CLA 250.raw");
        VehicleStatusAttributes update = ProtoConverter.raw2Proto(raw, true);
        vHandler.enqueueUpdate(update);
        updateListener.waitForUpdates();

        assertEquals("100 %", updateListener.getResponse("test::bev:charge#max-soc").toFullString(),
                "Max SoC from flat attribute fallback");

        ChannelUID cuid = new ChannelUID(thingMock.getUID(), Constants.GROUP_CHARGE, "max-soc");
        vHandler.handleCommand(cuid, QuantityType.valueOf("70 %"));
        assertEquals(70, ahm.getCommand().getInt("max_soc"),
                "ChargingConfigure command sent instead of ChargeProgramConfigure");
    }
}
