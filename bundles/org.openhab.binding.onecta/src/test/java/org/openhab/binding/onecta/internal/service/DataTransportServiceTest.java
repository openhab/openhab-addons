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
package org.openhab.binding.onecta.internal.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.binding.onecta.internal.api.OnectaConnectionClient;
import org.openhab.binding.onecta.internal.api.dto.units.Unit;
import org.openhab.binding.onecta.internal.api.dto.units.Units;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */

@ExtendWith(MockitoExtension.class)
public class DataTransportServiceTest {

    private DataTransportService dataTransportService;
    private String jsonString;
    private static JsonArray rawData = new JsonArray();
    private static Units onectaData = new Units();

    @Mock
    private OnectaConnectionClient onectaConnectionClientMock;

    @BeforeEach
    public void setUp() throws IOException {

        jsonString = new String(Files.readAllBytes(Paths.get("src/test/resources/aircoUnits.json")),
                StandardCharsets.UTF_8);
        rawData = JsonParser.parseString(jsonString).getAsJsonArray();
        onectaData.getAll().clear();
        for (int i = 0; i < rawData.size(); i++) {
            onectaData.getAll()
                    .add(Objects.requireNonNull(new Gson().fromJson(rawData.get(i).getAsJsonObject(), Unit.class)));
        }
    }

    @Test
    public void firstClimateControlTest() throws NoSuchFieldException, IllegalAccessException {
        final String UNITID = "1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6";
        dataTransportService = new DataTransportService(UNITID, Enums.ManagementPoint.CLIMATECONTROL);
        Field privateField = DataTransportService.class.getDeclaredField("onectaConnectionClient");
        privateField.setAccessible(true);
        privateField.set(dataTransportService, onectaConnectionClientMock);

        when(onectaConnectionClientMock.getUnit(UNITID)).thenReturn(onectaData.findById(UNITID));

        dataTransportService.refreshUnit();

        assertEquals("climateControl", dataTransportService.getEmbeddedId());
        assertEquals(true, dataTransportService.isAvailable());
        assertEquals(Enums.OperationMode.COLD, dataTransportService.getCurrentOperationMode());
        assertEquals(Enums.FanSpeed.AUTO, dataTransportService.getCurrentFanspeed());
        assertEquals(Enums.FanMovementHor.NOTAVAILABLE, dataTransportService.getCurrentFanDirectionHor());
        assertEquals(Enums.FanMovementVer.SWING, dataTransportService.getCurrentFanDirectionVer());
        assertEquals(Enums.FanMovement.VERTICAL, dataTransportService.getCurrentFanDirection());
        assertEquals("off", dataTransportService.getPowerOnOff());
        assertEquals("off", dataTransportService.getPowerfulModeOnOff());
        assertEquals("off", dataTransportService.getEconoMode());
        assertEquals("Kantoor Jeanette", dataTransportService.getUnitName());
        assertEquals((float) 20.0, dataTransportService.getCurrentTemperatureSet());
        assertEquals((float) 18.0, dataTransportService.getCurrentTemperatureSetMin());
        assertEquals((float) 32.0, dataTransportService.getCurrentTemperatureSetMax());
        assertEquals((float) 0.5, dataTransportService.getCurrentTemperatureSetStep());
        assertEquals(null, dataTransportService.getCurrentTankTemperatureSet());
        assertEquals(null, dataTransportService.getCurrentTankTemperatureSetMax());
        assertEquals(null, dataTransportService.getCurrentTankTemperatureSetMin());
        assertEquals((float) 24.0, dataTransportService.getIndoorTemperature());

        assertEquals((float) 55.0, dataTransportService.getIndoorHumidity());
        assertEquals("2023-07-30T08:31:10.853Z", dataTransportService.getTimeStamp().toString());
        assertEquals("on", dataTransportService.getStreamerMode());
        assertEquals("OFF", dataTransportService.getHolidayMode());

        assertEquals(false, dataTransportService.getIsHolidayModeActive());
        assertEquals(false, dataTransportService.getIsHolidayModeActive());
        assertEquals(false, dataTransportService.getIsPowerfulModeActive());
        assertEquals(null, dataTransportService.getIsInEmergencyState());

        assertArrayEquals((new Float[] { 0f, 0f, 0f, 0f, 0f, 0f, 0.1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.1f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f }), dataTransportService.getConsumptionCoolingDay());
        assertArrayEquals(
                (new Float[] { 0.2f, 0.1f, 0.1f, 0.1f, 0.1f, 0.2f, 0.1f, 0.1f, 0.1f, 0.1f, 0.2f, 0.1f, 0.1f, 0.1f }),
                dataTransportService.getConsumptionCoolingWeek());
        assertArrayEquals((new Float[] { 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 1.2f, 5.9f,
                3.7f, 0f, 0f, 0f, 0f, 0f }), dataTransportService.getConsumptionCoolingMonth());
        assertArrayEquals((new Float[] { 1f, 2f, 3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f }), dataTransportService.getConsumptionHeatingDay());
        assertArrayEquals((new Float[] { 4f, 5f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f }),
                dataTransportService.getConsumptionHeatingWeek());
        assertArrayEquals((new Float[] { 0f, 2f, 3f, 4f, 5f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 1.2f, 5.9f, 0f,
                0f, 0f, 0f, 0f, 0f }), dataTransportService.getConsumptionHeatingMonth());
    }

    @Test
    public void secondClimateControlTest() throws NoSuchFieldException, IllegalAccessException {
        final String UNITID = "5e41c4af-a5b8-4175-ac76-a1b2c3d4e5f6";
        dataTransportService = new DataTransportService(UNITID, Enums.ManagementPoint.CLIMATECONTROL);
        Field privateField = DataTransportService.class.getDeclaredField("onectaConnectionClient");
        privateField.setAccessible(true);
        privateField.set(dataTransportService, onectaConnectionClientMock);
        when(onectaConnectionClientMock.getUnit(UNITID)).thenReturn(onectaData.findById(UNITID));

        dataTransportService.refreshUnit();

        assertEquals(Enums.FanSpeed.LEVEL_5, dataTransportService.getCurrentFanspeed());
        assertEquals(Enums.FanMovementHor.SWING, dataTransportService.getCurrentFanDirectionHor());

        assertEquals(null, dataTransportService.getCurrentTankTemperatureSet());
    }

    @Test
    public void firstGatewayControlTest() throws NoSuchFieldException, IllegalAccessException {
        final String UNITID = "1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6";
        dataTransportService = new DataTransportService(UNITID, Enums.ManagementPoint.GATEWAY);
        Field privateField = DataTransportService.class.getDeclaredField("onectaConnectionClient");
        privateField.setAccessible(true);
        privateField.set(dataTransportService, onectaConnectionClientMock);

        when(onectaConnectionClientMock.getUnit(UNITID)).thenReturn(onectaData.findById(UNITID));

        dataTransportService.refreshUnit();

        assertEquals(true, dataTransportService.getDaylightSavingTimeEnabled());
        assertEquals("1_24_0", dataTransportService.getFirmwareVerion());
        assertEquals(true, dataTransportService.getIsFirmwareUpdateSupported());
        assertEquals(false, dataTransportService.getIsInErrorState());
        assertEquals("foutje", dataTransportService.getErrorCode());
        assertEquals(false, dataTransportService.getIsInErrorState());
        assertEquals(false, dataTransportService.getIsInInstallerState());
        assertEquals(true, dataTransportService.getIsInWarningState());
        assertEquals(true, dataTransportService.getIsLedEnabled());
        assertEquals("eu", dataTransportService.getRegionCode());
        assertEquals("0000000001234567", dataTransportService.getSerialNumber());
        assertEquals("DaikinAP33905", dataTransportService.getSsid());
        assertEquals("Europe/Amsterdam", dataTransportService.getTimeZone());
        assertEquals("Goliath", dataTransportService.getWifiConectionSSid());
        assertEquals(-51, dataTransportService.getWifiConectionStrength());
        assertEquals("BRP069C4x", dataTransportService.getModelInfo());
    }

    @Test
    public void firstIndoorunitTest() throws NoSuchFieldException, IllegalAccessException {
        final String UNITID = "1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6";
        dataTransportService = new DataTransportService(UNITID, Enums.ManagementPoint.INDOORUNIT);
        Field privateField = DataTransportService.class.getDeclaredField("onectaConnectionClient");
        privateField.setAccessible(true);
        privateField.set(dataTransportService, onectaConnectionClientMock);

        when(onectaConnectionClientMock.getUnit(UNITID)).thenReturn(onectaData.findById(UNITID));

        dataTransportService.refreshUnit();

        assertEquals("0J001234", dataTransportService.getSerialNumber());
        assertEquals("FVXM25A3V1B9", dataTransportService.getModelInfo());
        assertEquals("1695", dataTransportService.getEepromVerion());
        assertEquals("2200EB01", dataTransportService.getSoftwareVersion());
        assertEquals("on", dataTransportService.getDryKeepSetting());

        assertEquals((float) 1234.0, dataTransportService.getFanMotorRotationSpeed());
        assertEquals((float) 9, dataTransportService.getDeltaD());
        assertEquals((float) 14, dataTransportService.getHeatExchangerTemperature());
        assertEquals((float) 21, dataTransportService.getSuctionTemperature());

        assertEquals(null, dataTransportService.getIsInInstallerState());
        assertEquals(null, dataTransportService.getErrorCode());
        assertEquals(null, dataTransportService.getIsInErrorState());
        assertEquals(null, dataTransportService.getIsFirmwareUpdateSupported());
        assertEquals(null, dataTransportService.getFirmwareVerion());
        assertEquals(null, dataTransportService.getDaylightSavingTimeEnabled());
        assertEquals(null, dataTransportService.getIsInWarningState());
        assertEquals(null, dataTransportService.getIsLedEnabled());
        assertEquals(null, dataTransportService.getRegionCode());
        assertEquals(null, dataTransportService.getSsid());
        assertEquals(null, dataTransportService.getTimeZone());
        assertEquals(null, dataTransportService.getWifiConectionSSid());
        assertEquals(null, dataTransportService.getWifiConectionStrength());
    }

    @Test
    public void hotWaterTankCLIMATECONTROLTest() throws NoSuchFieldException, IllegalAccessException {
        final String UNITID = "c9cd8376-a32d-423b-acff-a1b2c3d4e5f6";
        dataTransportService = new DataTransportService(UNITID, Enums.ManagementPoint.WATERTANK);
        Field privateField = DataTransportService.class.getDeclaredField("onectaConnectionClient");
        privateField.setAccessible(true);
        privateField.set(dataTransportService, onectaConnectionClientMock);
        when(onectaConnectionClientMock.getUnit(UNITID)).thenReturn(onectaData.findById(UNITID));

        dataTransportService.refreshUnit();

        assertEquals((float) 50.0, dataTransportService.getCurrentTankTemperatureSet());
        assertEquals((float) 30.0, dataTransportService.getCurrentTankTemperatureSetMin());
        assertEquals((float) 60.0, dataTransportService.getCurrentTankTemperatureSetMax());
        assertEquals((float) 1.0, dataTransportService.getCurrentTankTemperatureSetStep());

        assertEquals(null, dataTransportService.getCurrentTemperatureSetStep());
        assertEquals(null, dataTransportService.getCurrentTemperatureSetMin());
        assertEquals(null, dataTransportService.getCurrentTemperatureSet());
        assertEquals(null, dataTransportService.getLeavingWaterTemperature());
        assertEquals(null, dataTransportService.getSetpointLeavingWaterTemperature());
        assertEquals(null, dataTransportService.getSetpointLeavingWaterOffset());
        assertEquals(null, dataTransportService.getSetpointLeavingWaterOffset());
        assertEquals(null, dataTransportService.getIndoorTemperature());
        assertEquals((float) 43.0, dataTransportService.getTankTemperature());
        assertEquals(null, dataTransportService.getIndoorHumidity());
        assertEquals("2023-07-20T16:16:16.499Z", dataTransportService.getTimeStamp().toString());
        assertEquals((float) 30.0, dataTransportService.getOutdoorTemperature());
        assertEquals(null, dataTransportService.getStreamerMode());
        assertEquals(null, dataTransportService.getHolidayMode());

        dataTransportService = new DataTransportService("c9cd8376-a32d-423b-acff-a1b2c3d4e5f6",
                Enums.ManagementPoint.CLIMATECONTROL);
        privateField.set(dataTransportService, onectaConnectionClientMock);
        when(onectaConnectionClientMock.getUnit("c9cd8376-a32d-423b-acff-a1b2c3d4e5f6"))
                .thenReturn(onectaData.findById("c9cd8376-a32d-423b-acff-a1b2c3d4e5f6"));
        dataTransportService.refreshUnit();

        assertEquals(null, dataTransportService.getCurrentTankTemperatureSetStep());
        assertEquals((float) 10.0, dataTransportService.getLeavingWaterTemperature());
        assertEquals((float) 10.0, dataTransportService.getSetpointLeavingWaterTemperature());
        assertEquals((float) 0.0, dataTransportService.getSetpointLeavingWaterOffset());
        assertEquals((float) 28.0, dataTransportService.getIndoorTemperature());
        assertEquals(null, dataTransportService.getTankTemperature());

        assertEquals((float) 30.0, dataTransportService.getOutdoorTemperature());
        assertEquals(false, dataTransportService.getIsInEmergencyState());
    }

    @Test
    public void settersClimatecontrolTest() throws NoSuchFieldException, IllegalAccessException {
        final String UNITID = "1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6";
        final Enums.ManagementPoint MANAGEMENTPOINT = Enums.ManagementPoint.CLIMATECONTROL;

        dataTransportService = new DataTransportService(UNITID, MANAGEMENTPOINT);
        Field privateField = DataTransportService.class.getDeclaredField("onectaConnectionClient");
        privateField.setAccessible(true);
        privateField.set(dataTransportService, onectaConnectionClientMock);
        when(onectaConnectionClientMock.getUnit(UNITID)).thenReturn(onectaData.findById(UNITID));

        dataTransportService.refreshUnit();

        dataTransportService.setCurrentOperationMode(Enums.OperationMode.AUTO);
        verify(onectaConnectionClientMock).setCurrentOperationMode(UNITID, MANAGEMENTPOINT, Enums.OperationMode.AUTO);

        dataTransportService.setFanSpeed(Enums.FanSpeed.LEVEL_3);
        verify(onectaConnectionClientMock).setFanSpeed(UNITID, MANAGEMENTPOINT.getValue(), Enums.OperationMode.COLD,
                Enums.FanSpeed.LEVEL_3);

        dataTransportService.setCurrentFanDirection(Enums.FanMovement.VERTICAL_AND_HORIZONTAL);
        verify(onectaConnectionClientMock).setCurrentFanDirection(UNITID, MANAGEMENTPOINT.getValue(),
                Enums.OperationMode.COLD, Enums.FanMovement.VERTICAL_AND_HORIZONTAL);

        dataTransportService.setCurrentFanDirectionHor(Enums.FanMovementHor.SWING);
        verify(onectaConnectionClientMock).setCurrentFanDirectionHor(UNITID, MANAGEMENTPOINT.getValue(),
                Enums.OperationMode.COLD, Enums.FanMovementHor.SWING);

        dataTransportService.setCurrentFanDirectionVer(Enums.FanMovementVer.WINDNICE);
        verify(onectaConnectionClientMock).setCurrentFanDirectionVer(UNITID, MANAGEMENTPOINT.getValue(),
                Enums.OperationMode.COLD, Enums.FanMovementVer.WINDNICE);

        dataTransportService.setPowerOnOff(Enums.OnOff.OFF);
        verify(onectaConnectionClientMock).setPowerOnOff(UNITID, MANAGEMENTPOINT, Enums.OnOff.OFF);

        dataTransportService.setPowerfulModeOnOff(Enums.OnOff.OFF);
        verify(onectaConnectionClientMock).setPowerfulModeOnOff(UNITID, MANAGEMENTPOINT, Enums.OnOff.OFF);

        dataTransportService.setEconoMode(Enums.OnOff.OFF);
        verify(onectaConnectionClientMock).setEconoMode(UNITID, MANAGEMENTPOINT, Enums.OnOff.OFF);

        dataTransportService.setStreamerMode(Enums.OnOff.OFF);
        verify(onectaConnectionClientMock).setStreamerMode(UNITID, MANAGEMENTPOINT.getValue(), Enums.OnOff.OFF);

        dataTransportService.setHolidayMode(Enums.OnOff.OFF);
        verify(onectaConnectionClientMock).setHolidayMode(UNITID, MANAGEMENTPOINT.getValue(), Enums.OnOff.OFF);

        dataTransportService.setCurrentTemperatureSet(20f);
        dataTransportService.setCurrentTemperatureSet(50f);
        verify(onectaConnectionClientMock, times(1)).setCurrentTemperatureRoomSet(UNITID, MANAGEMENTPOINT.getValue(),
                Enums.OperationMode.COLD, 20f);
    }

    @Test
    public void settersWaterTankCLIMATECONTROLTest() throws NoSuchFieldException, IllegalAccessException {
        final String UNITID = "c9cd8376-a32d-423b-acff-a1b2c3d4e5f6";
        final Enums.ManagementPoint MANAGEMENTPOINT = Enums.ManagementPoint.WATERTANK;

        dataTransportService = new DataTransportService(UNITID, MANAGEMENTPOINT);
        Field privateField = DataTransportService.class.getDeclaredField("onectaConnectionClient");
        privateField.setAccessible(true);
        privateField.set(dataTransportService, onectaConnectionClientMock);
        when(onectaConnectionClientMock.getUnit(UNITID)).thenReturn(onectaData.findById(UNITID));

        dataTransportService.refreshUnit();

        dataTransportService.setCurrentTankTemperatureSet(20f);
        dataTransportService.setCurrentTankTemperatureSet(90f);
        verify(onectaConnectionClientMock, times(1)).setCurrentTemperatureHotWaterSet(UNITID, "2",
                Enums.OperationMode.HEAT, 20f);

        dataTransportService.setSetpointLeavingWaterTemperature(20f);
        verify(onectaConnectionClientMock, times(1)).setSetpointLeavingWaterTemperature(UNITID, "2",
                Enums.OperationMode.HEAT, 20f);

        dataTransportService.setSetpointLeavingWaterOffset(20f);
        verify(onectaConnectionClientMock, times(1)).setSetpointLeavingWaterOffset(UNITID, "2",
                Enums.OperationMode.HEAT, 20f);
    }
}
