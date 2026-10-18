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
import org.openhab.binding.mercedesme.internal.handler.ProtoConverter;
import org.openhab.binding.mercedesme.internal.utils.ChannelStateMap;
import org.openhab.binding.mercedesme.internal.utils.Mapper;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

import com.daimler.mbcarkit.proto.VehicleEvents.AttributeStatus;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgram;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.Ignitionstate;
import com.daimler.mbcarkit.proto.VehicleEvents.IgnitionstateEnumAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.PrecondNow;
import com.daimler.mbcarkit.proto.VehicleEvents.PrecondNowEnumAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePointsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.VSUMetadata;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdate;

/**
 * {@link MapperTest} checks {@link Mapper#fromVehicleStatusUpdate(VehicleStatusUpdate)} against real captured
 * {@code .raw} fixtures from a BEV. The enum-conversion regression guard is built directly, since the capture's
 * ignitionstate sits at its proto3 default.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MapperTest {

    private static Map<String, VehicleAttributeStatus> loadFixture() {
        String raw = FileReader.readRawFileInString("src/test/resources/vehiclestatusupdates/vsu-eqa-2.raw");
        return ProtoConverter.raw2Proto(raw, true).attributes();
    }

    @Test
    void whenBoolAttributeConvertedThenBoolValueIsSet() {
        // Arrange - chargingactive sits at its proto3 default (not charging), so only bool_value is populated
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus chargingActive = attributes.get(MB_KEY_CHARGINGACTIVE);

        // Assert
        assertNotNull(chargingActive);
        assertFalse(chargingActive.getBoolValue());
    }

    @Test
    void whenInt64AttributeConvertedThenIntValueIsSet() {
        // Arrange
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus serviceIntervalDays = attributes.get(MB_KEY_SERVICEINTERVALDAYS);

        // Assert
        assertNotNull(serviceIntervalDays);
        assertEquals(-14, serviceIntervalDays.getIntValue());
    }

    @Test
    void whenDoubleAttributeConvertedThenDoubleValueIsSet() {
        // Arrange
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus positionHeading = attributes.get(MB_KEY_POSITION_HEADING);

        // Assert
        assertNotNull(positionHeading);
        assertEquals(224.1, positionHeading.getDoubleValue(), 0.0001);
    }

    @Test
    void whenEnumAttributeConvertedThenProtoDeclaredNumberIsUsed() {
        // Arrange - IGNITIONSTATE_ON is declared as 4 (value 3 is unused), guarding against a regression to
        // ordinal-based conversion
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID).build();
        IgnitionstateEnumAttribute ignition = IgnitionstateEnumAttribute.newBuilder()
                .setValue(Ignitionstate.IGNITIONSTATE_ON).setMetadata(metadata).build();
        VehicleStatusUpdate update = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(true)
                .setIgnitionstate(ignition).build();

        // Act
        Map<String, VehicleAttributeStatus> attributes = Mapper.fromVehicleStatusUpdate(update);
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
        assertEquals(299, rangeElectric.getIntValue());
        assertEquals(VehicleAttributeStatus.DistanceUnit.KILOMETERS, rangeElectric.getDistanceUnit());
        assertEquals("299", rangeElectric.getDisplayValue());
    }

    @Test
    void whenRatioAttributeConvertedThenPercentUnitIsSet() {
        // Arrange
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus soc = attributes.get(MB_KEY_SOC);

        // Assert
        assertNotNull(soc);
        assertEquals(71, soc.getIntValue());
        assertEquals(VehicleAttributeStatus.RatioUnit.PERCENT, soc.getRatioUnit());
    }

    @Test
    void whenSocStatusNotReceivedThenChannelStateIsUndefNotZeroPercent() {
        // Arrange - int_value = 0 with a non-VALID status instead of nil_value must not surface as "0 %"
        VehicleAttributeStatus soc = VehicleAttributeStatus.newBuilder()
                .setStatus(AttributeStatus.VALUE_NOT_RECEIVED_VALUE).setIntValue(0).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_SOC, soc);

        // Assert
        assertTrue(csm.isValid());
        assertEquals(UnDefType.UNDEF, csm.getState());
    }

    @Test
    void whenSocStatusValidThenChannelStateIsPercentQuantity() {
        // Arrange
        VehicleAttributeStatus soc = VehicleAttributeStatus.newBuilder().setStatus(AttributeStatus.VALUE_VALID_VALUE)
                .setIntValue(74).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_SOC, soc);

        // Assert
        assertEquals(QuantityType.valueOf(74, Units.PERCENT), csm.getState());
    }

    @Test
    void whenChargingPowerStatusNotReceivedThenChannelStateIsUndefNotZeroKw() {
        // Arrange - Math.max(0, -1) would mask an unavailable reading as "0 kW" (= not charging)
        VehicleAttributeStatus chargingPower = VehicleAttributeStatus.newBuilder()
                .setStatus(AttributeStatus.VALUE_NOT_RECEIVED_VALUE).setDoubleValue(0).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_CHARGING_POWER, chargingPower);

        // Assert
        assertEquals(UnDefType.UNDEF, csm.getState());
    }

    @Test
    void whenChargingPowerStatusValidThenChannelStateIsKwQuantity() {
        // Arrange
        VehicleAttributeStatus chargingPower = VehicleAttributeStatus.newBuilder()
                .setStatus(AttributeStatus.VALUE_VALID_VALUE).setDoubleValue(11.0).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_CHARGING_POWER, chargingPower);

        // Assert
        assertEquals(QuantityType.valueOf(11.0, KILOWATT_UNIT), csm.getState());
    }

    @Test
    void whenNilValueAttributeStillCarriesUnitThenObserverIsNotDropped() {
        // Arrange - value is nil but the unit is present; the observer lookup must not sit inside Utils.isNil()
        VehicleAttributeStatus liquidConsumption = VehicleAttributeStatus.newBuilder().setNilValue(true)
                .setCombustionConsumptionUnit(VehicleAttributeStatus.CombustionConsumptionUnit.LITER_PER_100KM).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_LIQUIDCONSUMPTIONSTART, liquidConsumption);

        // Assert
        assertEquals(UnDefType.UNDEF, csm.getState());
        assertTrue(csm.hasUomObserver(), "UOMObserver must still be set even though the value itself is nil");
    }

    @Test
    void whenOdoStatusInvalidThenChannelStateIsUndefNotNegativeOneKm() {
        // Arrange - Utils.getDouble()'s nil sentinel (-1) must not leak through as a literal reading
        VehicleAttributeStatus odo = VehicleAttributeStatus.newBuilder().setStatus(AttributeStatus.VALUE_INVALID_VALUE)
                .setIntValue(0).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_ODO, odo);

        // Assert
        assertEquals(UnDefType.UNDEF, csm.getState());
    }

    @Test
    void whenParkBrakeReportedViaIntValueThenChannelStateIsOn() {
        // Arrange - parkbrakestatus is delivered as an enum (int_value oneof), not a bool
        VehicleAttributeStatus parkBrake = VehicleAttributeStatus.newBuilder().setIntValue(1).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_PARKBRAKESTATUS, parkBrake);

        // Assert
        assertEquals(OnOffType.ON, csm.getState());
    }

    @Test
    void whenWashWaterReportedViaIntValueThenChannelStateIsOffNotUndef() {
        // Arrange - WARNINGWASHWATER_INACTIVE = 0 is the proto3 default, but the oneof case is still int_value
        VehicleAttributeStatus washWater = VehicleAttributeStatus.newBuilder().setIntValue(0).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_WARNINGWASHWATER, washWater);

        // Assert
        assertEquals(OnOffType.OFF, csm.getState());
    }

    @Test
    void whenChargingActiveReportedViaBoolValueThenChannelStateIsOn() {
        // Arrange - regression guard: genuine bool_value keys sharing the same "Switches" case
        VehicleAttributeStatus chargingActive = VehicleAttributeStatus.newBuilder().setBoolValue(true).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_CHARGINGACTIVE, chargingActive);

        // Assert
        assertEquals(OnOffType.ON, csm.getState());
    }

    @Test
    void whenDoorOpenReportedViaIntValueThenChannelStateIsOpen() {
        // Arrange - Doorstatus is delivered as an enum (int_value oneof), not a bool: CLOSED=0, OPEN=1
        VehicleAttributeStatus doorOpen = VehicleAttributeStatus.newBuilder().setIntValue(1).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_DOORSTATUSFRONTRIGHT, doorOpen);

        // Assert
        assertEquals(OpenClosedType.OPEN, csm.getState());
    }

    @Test
    void whenDoorClosedReportedViaIntValueThenChannelStateIsClosed() {
        // Arrange - Doorstatus CLOSED=0
        VehicleAttributeStatus doorClosed = VehicleAttributeStatus.newBuilder().setIntValue(0).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_DOORSTATUSFRONTRIGHT, doorClosed);

        // Assert
        assertEquals(OpenClosedType.CLOSED, csm.getState());
    }

    @Test
    void whenLockUnlockedReportedViaIntValueThenChannelStateIsOff() {
        // Arrange - Doorlockstatus is delivered as an enum (int_value oneof), not a bool: LOCKED=0, UNLOCKED=1
        VehicleAttributeStatus unlocked = VehicleAttributeStatus.newBuilder().setIntValue(1).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_DOORLOCKSTATUSFRONTRIGHT, unlocked);

        // Assert - ON means locked for this channel
        assertEquals(OnOffType.OFF, csm.getState());
    }

    @Test
    void whenLockLockedReportedViaIntValueThenChannelStateIsOn() {
        // Arrange - Doorlockstatus LOCKED=0
        VehicleAttributeStatus locked = VehicleAttributeStatus.newBuilder().setIntValue(0).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_DOORLOCKSTATUSFRONTRIGHT, locked);

        // Assert
        assertEquals(OnOffType.ON, csm.getState());
    }

    @Test
    void whenDoorOpenReportedViaBoolValueThenChannelStateIsOpen() {
        // Arrange - regression guard: defensive bool_value fallback (true = open); the active path emits int_value
        VehicleAttributeStatus doorOpen = VehicleAttributeStatus.newBuilder().setBoolValue(true).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_DOORSTATUSFRONTRIGHT, doorOpen);

        // Assert
        assertEquals(OpenClosedType.OPEN, csm.getState());
    }

    @Test
    void whenDoorClosedReportedViaBoolValueThenChannelStateIsClosed() {
        // Arrange - legacy bool_value false = closed
        VehicleAttributeStatus doorClosed = VehicleAttributeStatus.newBuilder().setBoolValue(false).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_DOORSTATUSFRONTRIGHT, doorClosed);

        // Assert
        assertEquals(OpenClosedType.CLOSED, csm.getState());
    }

    @Test
    void whenLockLockedReportedViaBoolValueThenChannelStateIsOn() {
        // Arrange - regression guard: defensive bool_value fallback for Doorlockstatus, reversed (false = locked)
        VehicleAttributeStatus locked = VehicleAttributeStatus.newBuilder().setBoolValue(false).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_DOORLOCKSTATUSFRONTRIGHT, locked);

        // Assert - ON means locked for this channel
        assertEquals(OnOffType.ON, csm.getState());
    }

    @Test
    void whenLockUnlockedReportedViaBoolValueThenChannelStateIsOff() {
        // Arrange - legacy bool_value true = unlocked (reversed)
        VehicleAttributeStatus unlocked = VehicleAttributeStatus.newBuilder().setBoolValue(true).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_DOORLOCKSTATUSFRONTRIGHT, unlocked);

        // Assert
        assertEquals(OnOffType.OFF, csm.getState());
    }

    @Test
    void whenTemperaturePointsConvertedThenZoneNameMatchesLegacyLookup() {
        // Arrange - the canonical (first-declared) FRONT_CENTER alias is the lowercase "frontCenter"
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus temperaturePoints = attributes.get(MB_KEY_TEMPERATURE_POINTS);

        // Assert
        assertNotNull(temperaturePoints);
        assertTrue(temperaturePoints.hasTemperaturePointsValue());
        TemperaturePointsValue value = temperaturePoints.getTemperaturePointsValue();
        assertEquals(1, value.getTemperaturePointsCount());
        assertEquals("frontCenter", value.getTemperaturePoints(0).getZone());
        assertEquals(20.0, value.getTemperaturePoints(0).getTemperature(), 0.0001);
    }

    @Test
    void whenTemperaturePointsConvertedThenOuterUnitIsPropagatedFromFirstPoint() {
        // Arrange - the outer temperature_unit must be set for the UOM observer to see a non-default unit
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
        // Arrange - ChargeProgramsArrayAttribute reuses the same message, so this is a lossless passthrough
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
        // Arrange - not yet verified against a live vehicle with an actual warning
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus auxheatwarnings = attributes.get(MB_KEY_AUXILIARY_WARNINGS);

        // Assert
        assertNotNull(auxheatwarnings);
        assertEquals(0, auxheatwarnings.getIntValue());
    }

    @Test
    void whenFullUpdateConvertedThenOnlyMappedFieldsAppearInMap() {
        // Arrange - the capture sets every currently mapped attribute (95) plus roughly 68 further raw fields
        // with no MB_KEY_*/channel mapping, so the exact map size is the regression guard
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act / Assert
        assertEquals(95, attributes.size(),
                "map must contain exactly the currently-mapped fields, neither more (unmapped raw fields "
                        + "leaking in) nor fewer (a mapped field silently dropped)");
    }

    @Test
    void whenPartialUpdateOnlyTouchesPrecondThenOtherAttributesAreAbsent() {
        // Arrange - delta update (full_update = false) with only precond_now; absent fields must not appear
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID).build();
        PrecondNowEnumAttribute precondNow = PrecondNowEnumAttribute.newBuilder()
                .setValue(PrecondNow.PRECOND_NOW_ACTIVE).setMetadata(metadata).build();
        VehicleStatusUpdate update = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(false)
                .setPrecondNow(precondNow).build();

        // Act
        Map<String, VehicleAttributeStatus> attributes = Mapper.fromVehicleStatusUpdate(update);

        // Assert
        assertEquals(1, attributes.size(), "only precondNow was set on this partial update");
        assertNotNull(attributes.get(MB_KEY_PRECOND_NOW));
        assertNull(attributes.get(MB_KEY_SOC), "soc was absent from this update, must not appear as 0");
        assertNull(attributes.get(MB_KEY_OVERALL_RANGE), "overallRange was absent, must not appear as 0");
        assertNull(attributes.get(MB_KEY_CHARGING_POWER), "chargingPower was absent, must not appear as 0");
    }
}
