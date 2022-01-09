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
package org.openhab.binding.evnotify.api.v2;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.binding.evnotify.api.ChargingData;

import com.google.gson.Gson;

/**
 * Test cases for the {@link ChargingDataDTO} class
 *
 * @author Michael Schmidt - Initial contribution
 */
class ChargingDataDTOTest {

    @Test
    void shouldCreateChargingDataForValidData() {
        // given
        BasicChargingDataDTO basicChargingDataDTO = getValidBasicChargingDataDTO();
        ExtendedChargingDataDTO extendedChargingDataDTO = getValidExtendedChargingDataDTO();

        // when
        ChargingData chargingData = new ChargingDataDTO(basicChargingDataDTO, extendedChargingDataDTO);

        // then
        assertEquals(Float.valueOf(93.0f), chargingData.getStateOfChargeDisplay());
        assertEquals(Float.valueOf(88.5f), chargingData.getStateOfChargeBms());
        assertEquals(1631220014L, chargingData.getLastStateOfCharge().toEpochSecond());
        assertTrue(chargingData.isCharging());
        assertFalse(chargingData.isRapidChargePort());
        assertTrue(chargingData.isNormalChargePort());
        assertFalse(chargingData.isSlowChargePort());
        assertEquals(Float.valueOf(100.0f), chargingData.getStateOfHealth());
        assertEquals(Float.valueOf(14.5f), chargingData.getAuxBatteryVoltage());
        assertEquals(Float.valueOf(362.1f), chargingData.getDcBatteryVoltage());
        assertEquals(Float.valueOf(-8.7f), chargingData.getDcBatteryCurrent());
        assertEquals(Float.valueOf(-3.15027f), chargingData.getDcBatteryPower());
        assertEquals(Float.valueOf(3881.5f), chargingData.getCumulativeEnergyCharged());
        assertEquals(Float.valueOf(3738.8f), chargingData.getCumulativeEnergyDischarged());
        assertEquals(Float.valueOf(25f), chargingData.getBatteryMinTemperature());
        assertEquals(Float.valueOf(26f), chargingData.getBatteryMaxTemperature());
        assertEquals(Float.valueOf(24f), chargingData.getBatteryInletTemperature());
        assertNull(chargingData.getExternalTemperature());
        assertEquals(1631220014L, chargingData.getLastExtended().toEpochSecond());
    }

    private BasicChargingDataDTO getValidBasicChargingDataDTO() {
        String validChargingDataJson = "{\"soc_display\": 93,\"soc_bms\": 88.5,\"last_soc\": 1631220014}";
        return new Gson().fromJson(validChargingDataJson, BasicChargingDataDTO.class);
    }

    private ExtendedChargingDataDTO getValidExtendedChargingDataDTO() {
        String validChargingDataJson = "{\"soh\": 100,\"charging\": 1,\"rapid_charge_port\": 0,\"normal_charge_port\": 1,"
                + "\"slow_charge_port\": null,\"aux_battery_voltage\": 14.5,\"dc_battery_voltage\": 362.1,"
                + "\"dc_battery_current\": -8.7,\"dc_battery_power\": -3.15027,"
                + "\"cumulative_energy_charged\": 3881.5,\"cumulative_energy_discharged\": 3738.8,"
                + "\"battery_min_temperature\": 25,\"battery_max_temperature\": 26,"
                + "\"battery_inlet_temperature\": 24,\"external_temperature\": null,\"odo\": null,"
                + "\"last_extended\": 1631220014}";
        return new Gson().fromJson(validChargingDataJson, ExtendedChargingDataDTO.class);
    }

    @Test
    void shouldThrowExceptionForNullData() {
        // when
        assertThrows(IllegalArgumentException.class, () -> new ChargingDataDTO(null, null));
    }
}
