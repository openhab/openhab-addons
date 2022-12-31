/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.siemensrds.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.measure.quantity.ElectricCurrent;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.siemensrds.internal.RdsAccessToken;
import org.openhab.binding.siemensrds.internal.RdsCloudException;
import org.openhab.binding.siemensrds.internal.RdsDataPoints;
import org.openhab.binding.siemensrds.internal.RdsPlants;
import org.openhab.binding.siemensrds.internal.RdsPlants.PlantInfo;
import org.openhab.binding.siemensrds.points.BasePoint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * test suite
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class TestRdsData {

    private String load(String fileName) {
        try (FileReader file = new FileReader(String.format("src/test/resources/%s.json", fileName),
                StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(file)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return "";
    }

    @Test
    public void testRdsDataPointsFullNew() {
        RdsDataPoints dataPoints = RdsDataPoints.createFromJson(load("datapoints_full_set_new"));
        assertNotNull(dataPoints);
        try {
            assertEquals("Downstairs", dataPoints.getDescription());
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }
        @Nullable
        Map<String, @Nullable BasePoint> points = dataPoints.points;
        assertNotNull(points);
        assertEquals(70, points.size());
    }

    @Test
    public void confirmDegreeSymbolCodingNotTrashed() {
        /*
         * note: temperature symbols with a degree sign: the MVN Spotless trashes the
         * "degree" (looks like *) symbol, so we must escape these symbols as octal \260
         * or unicode \u00B00 - the following test will indicate is all is ok
         */
        assertTrue("\260C".equals(BasePoint.DEGREES_CELSIUS));
        assertTrue("\u00B0C".equals(BasePoint.DEGREES_CELSIUS));
        assertTrue("\260F".equals(BasePoint.DEGREES_FAHRENHEIT));
        assertTrue("\u00B0F".equals(BasePoint.DEGREES_FAHRENHEIT));
        assertTrue(BasePoint.DEGREES_FAHRENHEIT.startsWith(BasePoint.DEGREES_CELSIUS.substring(0, 1)));
    }

    @Test
    public void testRdsDataPointsRefresh() {
        RdsDataPoints refreshPoints = RdsDataPoints.createFromJson(load("datapoints_refresh_set"));
        assertNotNull(refreshPoints);

        assertNotNull(refreshPoints.points);
        Map<String, @Nullable BasePoint> refreshMap = refreshPoints.points;
        assertNotNull(refreshMap);

        @Nullable
        BasePoint point;
        State state;

        // check the parsed values
        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!Online");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(1, ((DecimalType) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!00000000E000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(12.60, ((QuantityType<?>) state).floatValue(), 0.01);

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000083000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(16.0, ((QuantityType<?>) state).floatValue(), 0.01);

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000085000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(39.13, ((QuantityType<?>) state).floatValue(), 0.01);

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000086000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(21.51, ((QuantityType<?>) state).floatValue(), 0.01);

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000051000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(2, ((QuantityType<?>) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000052000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(5, ((QuantityType<?>) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000053000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(2, ((QuantityType<?>) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000056000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(1, ((QuantityType<?>) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!01300005A000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(2, ((QuantityType<?>) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000074000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(4, ((QuantityType<?>) state).intValue());

        RdsDataPoints originalPoints = RdsDataPoints.createFromJson(load("datapoints_full_set"));
        assertNotNull(originalPoints);
        assertNotNull(originalPoints.points);

        // check that the refresh point types match the originals
        Map<String, @Nullable BasePoint> originalMap = originalPoints.points;
        assertNotNull(originalMap);
        @Nullable
        BasePoint refreshPoint;
        @Nullable
        BasePoint originalPoint;
        for (String key : refreshMap.keySet()) {
            refreshPoint = refreshMap.get(key);
            assertTrue(refreshPoint instanceof BasePoint);
            originalPoint = originalMap.get(key);
            assertTrue(originalPoint instanceof BasePoint);
            assertEquals(refreshPoint.getState().getClass(), originalPoint.getState().getClass());
        }
    }

    @Test
    public void testAccessToken() {
        RdsAccessToken accessToken = RdsAccessToken.createFromJson(load("access_token"));
        assertNotNull(accessToken);
        try {
            assertEquals("this-is-not-a-valid-access_token", accessToken.getToken());
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }
        assertTrue(accessToken.isExpired());
    }

    @Test
    public void testRdsDataPointsFull() {
        RdsDataPoints dataPoints = RdsDataPoints.createFromJson(load("datapoints_full_set"));
        assertNotNull(dataPoints);
        try {
            assertEquals("Upstairs", dataPoints.getDescription());
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }

        @Nullable
        Map<String, @Nullable BasePoint> points = dataPoints.points;
        assertNotNull(points);
        assertEquals(67, points.size());

        try {
            assertEquals("AAS-20:SU=SiUn;APT=HvacFnct18z_A;APTV=2.003;APS=1;",
                    dataPoints.getPointByClass("ApplicationSoftwareVersion").getState().toString());
            assertEquals("Device object", dataPoints.getPointByClass("Device Description").getState().toString());
            assertEquals("FW=02.32.02.27;SVS-300.1:SBC=13.22;I",
                    dataPoints.getPointByClass("FirmwareRevision").getState().toString());
            assertEquals("RDS110", dataPoints.getPointByClass("ModelName").getState().toString());
            assertEquals(0, dataPoints.getPointByClass("SystemStatus").asInt());
            assertEquals(0, dataPoints.getPointByClass("UtcOffset").asInt());
            assertEquals(19, dataPoints.getPointByClass("DatabaseRevision").asInt());
            assertEquals(0, dataPoints.getPointByClass("LastRestartReason").asInt());
            assertEquals("MDL:ASN= RDS110;HW=0.2.0;",
                    dataPoints.getPointByClass("ModelInformation").getState().toString());
            assertEquals(1, dataPoints.getPointByClass("Active SystemLanguge").asInt());
            assertEquals(26, dataPoints.getPointByClass("TimeZone").asInt());
            assertEquals("160100096D", dataPoints.getPointByClass("SerialNumber").getState().toString());
            assertEquals("'10010'B", dataPoints.getPointByClass("Device Features").getState().toString());
            assertEquals("Upstairs", dataPoints.getPointByClass("'Description").getState().toString());
            assertEquals("192.168.1.1", dataPoints.getPointByClass("'IP gefault gateway").getState().toString());
            assertEquals("255.255.255.0", dataPoints.getPointByClass("'IP subnet mask").getState().toString());
            assertEquals("192.168.1.42", dataPoints.getPointByClass("'IP address").getState().toString());
            assertEquals(47808, dataPoints.getPointByClass("'UDP Port").asInt());
            assertEquals("'F0C77F6C1895'H", dataPoints.getPointByClass("'BACnet MAC address").getState().toString());
            assertEquals("sth.connectivity.ccl-siemens.com",
                    dataPoints.getPointByClass("'Connection URI").getState().toString());
            assertEquals("this-is-not-a-valid-activation-key",
                    dataPoints.getPointByClass("'Activation Key").getState().toString());
            assertEquals(60, dataPoints.getPointByClass("'Reconection delay").asInt());
            assertEquals(0, dataPoints.getPointByClass("#Item Updates per Minute").asInt());
            assertEquals(286849, dataPoints.getPointByClass("#Item Updates Total").asInt());
            assertEquals("-;en", dataPoints.getPointByClass("#Languages").getState().toString());
            assertEquals(1, dataPoints.getPointByClass("#Online").asInt());
            assertEquals(1473, dataPoints.getPointByClass("#Traffic Inbound per Minute").asInt());
            assertEquals(178130801, dataPoints.getPointByClass("#Traffic Inbound Total").asInt());
            assertEquals(616, dataPoints.getPointByClass("#Traffic Outbound per Minute").asInt());
            assertEquals(60624666, dataPoints.getPointByClass("#Traffic Outbound Total").asInt());
            assertEquals(0, dataPoints.getPointByClass("#Item Updates per Minute").asInt());

            State state;
            QuantityType<?> celsius;
            state = dataPoints.getPointByClass("'TOa").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(18.55, celsius.floatValue(), 0.01);

            assertEquals(new QuantityType<ElectricCurrent>(0, Units.AMPERE),
                    dataPoints.getPointByClass("'HDevElLd").getState());

            state = dataPoints.getPointByClass("'SpHPcf").getState();
            assertTrue(state instanceof QuantityType<?>);
            QuantityType<?> fahrenheit = ((QuantityType<?>) state).toUnit(ImperialUnits.FAHRENHEIT);
            assertNotNull(fahrenheit);
            assertEquals(24.00, fahrenheit.floatValue(), 0.01);

            state = dataPoints.getPointByClass("'SpHEco").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(16.00, celsius.floatValue(), 0.01);

            state = dataPoints.getPointByClass("'SpHPrt").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(6.00, celsius.floatValue(), 0.01);

            state = dataPoints.getPointByClass("'SpTR").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(24.00, celsius.floatValue(), 0.01);

            state = dataPoints.getPointByClass("'SpTRShft").getState();
            assertTrue(state instanceof QuantityType<?>);
            QuantityType<?> kelvin = ((QuantityType<?>) state).toUnit(Units.KELVIN);
            assertNotNull(kelvin);
            assertEquals(0, kelvin.floatValue(), 0.01);

            state = dataPoints.getPointByClass("'RHuRel").getState();
            assertTrue(state instanceof QuantityType<?>);
            QuantityType<?> relativeHumidity = ((QuantityType<?>) state).toUnit(Units.PERCENT);
            assertNotNull(relativeHumidity);
            assertEquals(46.86865, relativeHumidity.floatValue(), 0.1);

            state = dataPoints.getPointByClass("'RTemp").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(23.76, celsius.floatValue(), 0.01);

            state = dataPoints.getPointByClass("'SpTRMaxHCmf").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(35.00, celsius.floatValue(), 0.01);

            assertEquals(new QuantityType<>(30, Units.ONE), dataPoints.getPointByClass("'WarmUpGrdnt").getState());

            state = dataPoints.getPointByClass("'TRBltnMsvAdj").getState();
            assertTrue(state instanceof QuantityType<?>);
            kelvin = ((QuantityType<?>) state).toUnit(Units.KELVIN);
            assertNotNull(kelvin);
            assertEquals(35.0, celsius.floatValue(), 0.01);

            assertEquals(new QuantityType<>(0, Units.AMPERE), dataPoints.getPointByClass("'Q22Q24ElLd").getState());
            assertEquals(new QuantityType<>(713, Units.PARTS_PER_MILLION),
                    dataPoints.getPointByClass("'RAQual").getState());

            assertEquals(new QuantityType<>(0, Units.ONE), dataPoints.getPointByClass("'TmpCmfBtn").getState());
            assertEquals(new QuantityType<>(0, Units.ONE), dataPoints.getPointByClass("'CmfBtn").getState());
            assertEquals(new QuantityType<>(0, Units.ONE), dataPoints.getPointByClass("'RPscDet").getState());
            assertEquals(new QuantityType<>(1, Units.ONE), dataPoints.getPointByClass("'EnHCtl").getState());
            assertEquals(new QuantityType<>(0, Units.ONE), dataPoints.getPointByClass("'EnRPscDet").getState());
            assertEquals(new QuantityType<>(2, Units.ONE), dataPoints.getPointByClass("'OffPrtCnf").getState());
            assertEquals(new QuantityType<>(3, Units.ONE), dataPoints.getPointByClass("'OccMod").getState());
            assertEquals(new QuantityType<>(5, Units.ONE), dataPoints.getPointByClass("'REei").getState());
            assertEquals(new QuantityType<>(2, Units.ONE), dataPoints.getPointByClass("'DhwMod").getState());
            assertEquals(new QuantityType<>(2, Units.ONE), dataPoints.getPointByClass("'HCSta").getState());
            assertEquals(new QuantityType<>(4, Units.ONE), dataPoints.getPointByClass("'PrOpModRsn").getState());
            assertEquals(new QuantityType<>(6, Units.ONE), dataPoints.getPointByClass("'HCtrSet").getState());
            assertEquals(new QuantityType<>(2, Units.ONE), dataPoints.getPointByClass("'OsscSet").getState());
            assertEquals(new QuantityType<>(4, Units.ONE), dataPoints.getPointByClass("'RAQualInd").getState());

            assertEquals(new QuantityType<>(500, Units.HOUR), dataPoints.getPointByClass("'KickCyc").getState());
            assertEquals(new QuantityType<>(3, Units.MINUTE), dataPoints.getPointByClass("'BoDhwTiOnMin").getState());
            assertEquals(new QuantityType<>(3, Units.MINUTE), dataPoints.getPointByClass("'BoDhwTiOffMin").getState());

            assertEquals(UnDefType.UNDEF, dataPoints.getPointByClass("'ROpModSched").getState());
            assertEquals(UnDefType.UNDEF, dataPoints.getPointByClass("'DhwSched").getState());
            assertEquals(UnDefType.UNDEF, dataPoints.getPointByClass("'ROpModSched").getState());
            assertEquals(UnDefType.UNDEF, dataPoints.getPointByClass("'DhwSched").getState());

            assertEquals(new QuantityType<>(253140, Units.MINUTE), dataPoints.getPointByClass("'OphH").getState());
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }

        // test for a missing element
        State test = null;
        try {
            test = dataPoints.getPointByClass("missing-element").getState();
            fail("expected exception did not occur");
        } catch (RdsCloudException e) {
            assertEquals(null, test);
        }

        try {
            // test the all-the-way-round lookup loop
            assertNotNull(dataPoints.points);
            Map<String, @Nullable BasePoint> pointsMap = dataPoints.points;
            assertNotNull(pointsMap);
            @Nullable
            BasePoint point;
            for (Entry<String, @Nullable BasePoint> entry : pointsMap.entrySet()) {
                point = entry.getValue();
                assertTrue(point instanceof BasePoint);
                // ignore UNDEF points where all-the-way-round lookup fails
                if (!"UNDEF".equals(point.getState().toString())) {
                    @Nullable
                    String x = entry.getKey();
                    assertNotNull(x);
                    String y = point.getPointClass();
                    String z = dataPoints.pointClassToId(y);
                    assertEquals(x, z);
                }
            }

            State state = null;

            // test the specific points that we use
            state = dataPoints.getPointByClass(HIE_DESCRIPTION).getState();
            assertEquals("Upstairs", state.toString());

            state = dataPoints.getPointByClass(HIE_ROOM_TEMP).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(23.761879, ((QuantityType<?>) state).floatValue(), 0.01);

            state = dataPoints.getPointByClass(HIE_OUTSIDE_TEMP).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(18.55, ((QuantityType<?>) state).floatValue(), 0.01);

            state = dataPoints.getPointByClass(HIE_TARGET_TEMP).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(24, ((QuantityType<?>) state).floatValue(), 0.01);

            state = dataPoints.getPointByClass(HIE_ROOM_HUMIDITY).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(46.86, ((QuantityType<?>) state).floatValue(), 0.01);

            state = dataPoints.getPointByClass(HIE_ROOM_AIR_QUALITY).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Good", state.toString());
            assertEquals("Good", dataPoints.getPointByClass(HIE_ROOM_AIR_QUALITY).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_ENERGY_SAVINGS_LEVEL).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Excellent", state.toString());
            assertEquals("Excellent", dataPoints.getPointByClass(HIE_ENERGY_SAVINGS_LEVEL).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_OUTPUT_STATE).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Heating", state.toString());
            assertEquals("Heating", dataPoints.getPointByClass(HIE_OUTPUT_STATE).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_STAT_OCC_MODE_PRESENT).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(3, ((QuantityType<?>) state).intValue());
            assertEquals(3, dataPoints.getPointByClass(HIE_STAT_OCC_MODE_PRESENT).asInt());

            state = dataPoints.getPointByClass(HIE_STAT_OCC_MODE_PRESENT).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Present", state.toString());
            assertEquals("Present", dataPoints.getPointByClass(HIE_STAT_OCC_MODE_PRESENT).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_DHW_OUTPUT_STATE).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(2, ((QuantityType<?>) state).intValue());
            assertEquals(2, dataPoints.getPointByClass(HIE_DHW_OUTPUT_STATE).asInt());

            state = dataPoints.getPointByClass(HIE_DHW_OUTPUT_STATE).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("On", state.toString());
            assertEquals("On", dataPoints.getPointByClass(HIE_DHW_OUTPUT_STATE).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_PR_OP_MOD_RSN).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(4, ((QuantityType<?>) state).intValue());
            assertEquals(4, dataPoints.getPointByClass(HIE_PR_OP_MOD_RSN).asInt());

            state = dataPoints.getPointByClass(HIE_PR_OP_MOD_RSN).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Comfort", state.toString());
            assertEquals("Comfort", dataPoints.getPointByClass(HIE_PR_OP_MOD_RSN).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_STAT_CMF_BTN).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(0, ((QuantityType<?>) state).intValue());
            assertEquals(0, dataPoints.getPointByClass(HIE_STAT_CMF_BTN).asInt());

            state = dataPoints.getPointByClass(HIE_STAT_CMF_BTN).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Inactive", state.toString());
            assertEquals("Inactive", dataPoints.getPointByClass(HIE_STAT_CMF_BTN).getEnum().toString());

            // test online code
            assertTrue(dataPoints.isOnline());

            // test present priority code
            assertEquals(15, dataPoints.getPointByClass(HIE_TARGET_TEMP).getPresentPriority());

            // test temperature units code (C)
            BasePoint tempPoint = dataPoints.getPointByClass("'SpTR");
            assertTrue(tempPoint instanceof BasePoint);
            assertEquals(SIUnits.CELSIUS, tempPoint.getUnit());

            // test temperature units code (F)
            tempPoint = dataPoints.getPointByClass("'SpHPcf");
            assertTrue(tempPoint instanceof BasePoint);
            assertEquals(ImperialUnits.FAHRENHEIT, tempPoint.getUnit());

            // test temperature units code (K)
            tempPoint = dataPoints.getPointByClass("'SpHPcf");
            assertTrue(tempPoint instanceof BasePoint);
            assertEquals(ImperialUnits.FAHRENHEIT, tempPoint.getUnit());

            tempPoint = dataPoints.getPointByClass("'SpTRShft");
            assertTrue(tempPoint instanceof BasePoint);
            assertEquals(Units.KELVIN, tempPoint.getUnit());
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRdsPlants() {
        try {
            RdsPlants plants = RdsPlants.createFromJson(load("plants"));
            assertNotNull(plants);

            @Nullable
            List<PlantInfo> plantList = plants.getPlants();
            assertNotNull(plantList);

            @Nullable
            PlantInfo plant;
            plant = plantList.get(0);
            assertTrue(plant instanceof PlantInfo);
            assertEquals("Pd1774247-7de7-4896-ac76-b7e0dd943c40", plant.getId());
            assertTrue(plant.isOnline());

            plant = plantList.get(1);
            assertTrue(plant instanceof PlantInfo);
            assertEquals("Pfaf770c8-abeb-4742-ad65-ead39030d369", plant.getId());
            assertTrue(plant.isOnline());
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }
    }
}
