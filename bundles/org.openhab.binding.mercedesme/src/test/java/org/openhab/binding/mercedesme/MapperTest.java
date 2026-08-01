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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.mercedesme.internal.Constants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.utils.Mapper;

import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgram;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.PushMessage;
import com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePointsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdate;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

/**
 * {@link MapperTest} checks {@link Mapper#fromVehicleStatusUpdate(VehicleStatusUpdate)} against a captured
 * {@code VehicleStatusUpdate} fixture ({@code proto-json/VehicleStatusUpdate-EQA.json}). The fixture covers one
 * representative field per attribute-type category (bool/int64/double/enum/distance/pressure/speed/ratio/clock
 * hour/consumption) plus the three complex array-typed fields (temperature points, charge programs, auxiliary
 * warnings) - see {@code docs/ATTRIBUTES_MAPPING.md} for the full field-by-field mapping this exercises.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MapperTest {

    private static Map<String, VehicleAttributeStatus> loadFixture() {
        String json = FileReader.readFileInString("src/test/resources/proto-json/VehicleStatusUpdate-EQA.json");
        PushMessage.Builder pmBuilder = PushMessage.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(json, pmBuilder);
        } catch (InvalidProtocolBufferException e) {
            fail(e.getMessage());
        }
        PushMessage pm = pmBuilder.build();
        assertTrue(pm.hasVehicleStatusUpdates(), "fixture must contain a vehicleStatusUpdates push message");
        VehicleStatusUpdate update = pm.getVehicleStatusUpdates().getVehicleStatusUpdatesMap().get("UNIT_TEST_VIN");
        assertNotNull(update, "fixture must contain an entry for UNIT_TEST_VIN");
        return Mapper.fromVehicleStatusUpdate(update);
    }

    @Test
    void whenBoolAttributeConvertedThenBoolValueIsSet() {
        // Arrange
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus chargingActive = attributes.get(MB_KEY_CHARGINGACTIVE);

        // Assert
        assertNotNull(chargingActive);
        assertTrue(chargingActive.getBoolValue());
    }

    @Test
    void whenInt64AttributeConvertedThenIntValueIsSet() {
        // Arrange
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus serviceIntervalDays = attributes.get(MB_KEY_SERVICEINTERVALDAYS);

        // Assert
        assertNotNull(serviceIntervalDays);
        assertEquals(365, serviceIntervalDays.getIntValue());
    }

    @Test
    void whenDoubleAttributeConvertedThenDoubleValueIsSet() {
        // Arrange
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus chargingPower = attributes.get(MB_KEY_CHARGING_POWER);

        // Assert
        assertNotNull(chargingPower);
        assertEquals(11.0, chargingPower.getDoubleValue(), 0.0001);
    }

    @Test
    void whenEnumAttributeConvertedThenProtoDeclaredNumberIsUsed() {
        // Arrange - Ignitionstate.IGNITIONSTATE_ON is explicitly declared as 4 in vehicle-events.proto (value 3
        // is intentionally unused), so this also guards against a regression back to ordinal/positional
        // guessing instead of getValueValue().
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus ignitionState = attributes.get(MB_KEY_IGNITIONSTATE);

        // Assert
        assertNotNull(ignitionState);
        assertEquals(4, ignitionState.getIntValue());
    }

    @Test
    void whenDistanceAttributeConvertedThenUnitAndDisplayValueAreSet() {
        // Arrange
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus rangeElectric = attributes.get(MB_KEY_RANGEELECTRIC);

        // Assert
        assertNotNull(rangeElectric);
        assertEquals(310, rangeElectric.getIntValue());
        assertEquals(VehicleAttributeStatus.DistanceUnit.KILOMETERS, rangeElectric.getDistanceUnit());
        assertEquals("310", rangeElectric.getDisplayValue());
    }

    @Test
    void whenRatioAttributeConvertedThenPercentUnitIsSet() {
        // Arrange
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus soc = attributes.get(MB_KEY_SOC);

        // Assert
        assertNotNull(soc);
        assertEquals(82, soc.getIntValue());
        assertEquals(VehicleAttributeStatus.RatioUnit.PERCENT, soc.getRatioUnit());
    }

    @Test
    void whenTemperaturePointsConvertedThenZoneNameMatchesLegacyLookup() {
        // Arrange - VehicleHandler resolves the zone via Utils.getZoneNumber(String), whose lookup table is
        // built from TemperatureConfigure.TemperaturePoint.Zone.values()[i].name() (vehicle-commands.proto) -
        // the canonical (first-declared) name for the FRONT_CENTER/frontCenter alias pair is the lowercase one.
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus temperaturePoints = attributes.get(MB_KEY_TEMPERATURE_POINTS);

        // Assert
        assertNotNull(temperaturePoints);
        assertTrue(temperaturePoints.hasTemperaturePointsValue());
        TemperaturePointsValue value = temperaturePoints.getTemperaturePointsValue();
        assertEquals(1, value.getTemperaturePointsCount());
        assertEquals("frontCenter", value.getTemperaturePoints(0).getZone());
        assertEquals(21.0, value.getTemperaturePoints(0).getTemperature(), 0.0001);
    }

    @Test
    void whenTemperaturePointsConvertedThenOuterUnitIsPropagatedFromFirstPoint() {
        // Arrange - regression test for the bug found while reviewing VehicleHandler line 904ff: the outer
        // VehicleAttributeStatus.temperature_unit must be set for VehicleHandler's UOM observer to pick up
        // anything other than the binding's default unit.
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus temperaturePoints = attributes.get(MB_KEY_TEMPERATURE_POINTS);

        // Assert
        assertNotNull(temperaturePoints);
        assertTrue(temperaturePoints.hasTemperatureUnit());
        assertEquals(VehicleAttributeStatus.TemperatureUnit.CELSIUS, temperaturePoints.getTemperatureUnit());
    }

    @Test
    void whenChargeProgramsConvertedThenChargeProgramParametersArePassedThrough() {
        // Arrange - ChargeProgramsArrayAttribute reuses the very same ChargeProgramParameters message the old
        // ChargeProgramsValue wraps, so this must be a lossless passthrough (no field-by-field conversion).
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus chargePrograms = attributes.get(MB_KEY_CHARGE_PROGRAMS);

        // Assert
        assertNotNull(chargePrograms);
        assertTrue(chargePrograms.hasChargeProgramsValue());
        ChargeProgramsValue value = chargePrograms.getChargeProgramsValue();
        assertEquals(4, value.getChargeProgramParametersCount());
        assertEquals(80, value.getChargeProgramParameters(0).getMaxSoc());
        assertEquals(ChargeProgram.INSTANT_CHARGE_PROGRAM, value.getChargeProgramParameters(1).getChargeProgram());
    }

    @Test
    void whenAuxheatwarningsEmptyThenIntValueIsNone() {
        // Arrange - not yet verified against a live vehicle with an actual warning, see
        // docs/ATTRIBUTES_MAPPING.md section 4.
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus auxheatwarnings = attributes.get(MB_KEY_AUXILIARY_WARNINGS);

        // Assert
        assertNotNull(auxheatwarnings);
        assertEquals(0, auxheatwarnings.getIntValue());
    }
}
