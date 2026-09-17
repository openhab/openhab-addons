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
 * {@link MapperTest} checks {@link Mapper#fromVehicleStatusUpdate(VehicleStatusUpdate)} against a real captured
 * {@code VehicleStatusUpdate} fixture ({@code vehiclestatusupdates/vsu-eqa-2.raw}, a full-update TextFormat dump
 * from a BEV, GPS position anonymized). The fixture covers one representative field per attribute-type category
 * (bool/int64/double/distance/ratio/consumption) plus the three complex array-typed fields (temperature points,
 * charge programs, auxiliary warnings). The enum-conversion regression guard is built directly, since this real
 * capture's ignitionstate happens to sit at its proto3 default.
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
        // Arrange - vsu-eqa-2.raw reports chargingactive with no explicit "value:" line (proto3 default,
        // matching the real vehicle's state: not charging), so the meaningful check here is that the
        // bool_value oneof is the one actually populated, not a fabricated int/double default.
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
        // Arrange - positionHeading is a plain double_value attribute (no unit), real value from
        // vsu-eqa-2.raw
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus positionHeading = attributes.get(MB_KEY_POSITION_HEADING);

        // Assert
        assertNotNull(positionHeading);
        assertEquals(224.1, positionHeading.getDoubleValue(), 0.0001);
    }

    @Test
    void whenEnumAttributeConvertedThenProtoDeclaredNumberIsUsed() {
        // Arrange - Ignitionstate.IGNITIONSTATE_ON is explicitly declared as 4 in vehicle-events.proto (value 3
        // is intentionally unused), so this also guards against a regression back to ordinal/positional
        // guessing instead of getValueValue(). vsu-eqa-2.raw's real ignitionstate sits at its proto3 default
        // (IGNITIONSTATE_LOCK = 0), which can't discriminate ordinal-vs-declared-number bugs, so this one
        // attribute is built directly instead of read from the fixture.
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
        // Arrange - backend sends the default int_value = 0 together with a non-VALID status
        // instead of setting nil_value; this must not surface as "0 %" on the State of Charge
        // channel
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
        // Arrange - Math.max(0, -1) would otherwise mask an unavailable reading as "0 kW", which
        // looks identical to "not charging" (community.openhab.org/t/mercedes-me/136866/199 reported
        // several attributes reading zero right after a command)
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
        // Arrange - a BEV reports liquidconsumptionstart/reset with a non-VALID status (no combustion engine)
        // while still carrying combustion_consumption_unit (real fixture data, see
        // src/test/resources/vehiclestatusupdates/vsu-eqa-2.raw). The observer/unit lookup must not be nested
        // inside the Utils.isNil() branch, or VehicleHandler.updateChannel() never calls
        // handleComplexTripPattern() for this update - the exact regression that made
        // VehicleHandlerTest's "Trip Update Count" assertions fail after this change was first written.
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
        // Arrange - parkbrakestatus is delivered as an enum (int_value oneof), not a bool - confirmed
        // against real captured data in src/test/resources/vehiclestatusupdates/vsu-eqa-1.raw and
        // vsu-eqa-2.raw (both show "parkbrakestatus { value: PARKBRAKESTATUS_ENGAGED ... }")
        VehicleAttributeStatus parkBrake = VehicleAttributeStatus.newBuilder().setIntValue(1).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_PARKBRAKESTATUS, parkBrake);

        // Assert
        assertEquals(OnOffType.ON, csm.getState());
    }

    @Test
    void whenWashWaterReportedViaIntValueThenChannelStateIsOffNotUndef() {
        // Arrange - WARNINGWASHWATER_INACTIVE = 0 is the proto3 default, so the raw dumps omit an
        // explicit "value:" line, but the oneof case is still int_value - must not surface as UNDEF
        VehicleAttributeStatus washWater = VehicleAttributeStatus.newBuilder().setIntValue(0).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_WARNINGWASHWATER, washWater);

        // Assert
        assertEquals(OnOffType.OFF, csm.getState());
    }

    @Test
    void whenChargingActiveReportedViaBoolValueThenChannelStateIsOn() {
        // Arrange - regression guard: genuine bool_value keys sharing the same "Switches" case must
        // keep working unchanged
        VehicleAttributeStatus chargingActive = VehicleAttributeStatus.newBuilder().setBoolValue(true).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_CHARGINGACTIVE, chargingActive);

        // Assert
        assertEquals(OnOffType.ON, csm.getState());
    }

    @Test
    void whenDoorOpenReportedViaIntValueThenChannelStateIsOpen() {
        // Arrange - Doorstatus is delivered as an enum (int_value oneof), not a bool: CLOSED=0, OPEN=1
        // (MBMobileSDK 1.68 Doorstatus). getChannelStateMap() must not read this via getBoolValue(),
        // which would always see the oneof default false (= CLOSED) and hide an open door.
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
        // Arrange - Doorlockstatus is delivered as an enum (int_value oneof), not a bool: LOCKED=0,
        // UNLOCKED=1 (MBMobileSDK 1.68 Doorlockstatus). getChannelStateMap() must not read this via
        // getBoolValue(), which would always see the oneof default false and report an unlocked
        // individual lock as still locked.
        VehicleAttributeStatus unlocked = VehicleAttributeStatus.newBuilder().setIntValue(1).build();

        // Act
        ChannelStateMap csm = Mapper.getChannelStateMap(MB_KEY_DOORLOCKSTATUSFRONTRIGHT, unlocked);

        // Assert - ON means locked for this channel (see Mapper.getChannelStateMap "sad but true" note)
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
        // Arrange - regression guard for PR #21343 review (wborn): getChannelStateMap() keeps a defensive
        // bool_value fallback (true = open) for Doorstatus/Decklidstatus/EngineHoodStatus, even though the
        // only currently active push path (Mapper.fromVehicleStatusUpdate()) always emits int_value - the
        // legacy VEPUpdate ingress that used to send bool_value is gone entirely.
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
        // Arrange - regression guard for PR #21343 review (wborn): getChannelStateMap() keeps a defensive
        // bool_value fallback for Doorlockstatus, reversed (false = locked) - see the "sad but true" note in
        // Mapper.getChannelStateMap(). The only currently active push path always emits int_value; the
        // legacy VEPUpdate ingress that used to send bool_value is gone entirely.
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
        assertEquals(20.0, value.getTemperaturePoints(0).getTemperature(), 0.0001);
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
        // Arrange - not yet verified against a live vehicle with an actual warning.
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act
        VehicleAttributeStatus auxheatwarnings = attributes.get(MB_KEY_AUXILIARY_WARNINGS);

        // Assert
        assertNotNull(auxheatwarnings);
        assertEquals(0, auxheatwarnings.getIntValue());
    }

    @Test
    void whenFullUpdateConvertedThenOnlyMappedFieldsAppearInMap() {
        // Arrange - vsu-eqa-2.raw is a real full_update capture that happens to set every one of the
        // currently mapped attributes (95 of them), plus roughly 68 further raw fields that have no
        // MB_KEY_*/channel mapping at all (battery_health, min_soc, weekly_profile, vehicle_health_status,
        // etc. - see docs/changes/remove-vepupdate/proposal.md, "Out of scope"). The meaningful regression
        // guard with a comprehensive real capture like this one is therefore an exact map size: it catches
        // both a fabricated entry sneaking in for an unmapped field and a mapped field silently dropping out.
        Map<String, VehicleAttributeStatus> attributes = loadFixture();

        // Act / Assert
        assertEquals(95, attributes.size(),
                "map must contain exactly the currently-mapped fields, neither more (unmapped raw fields "
                        + "leaking in) nor fewer (a mapped field silently dropped)");
    }

    @Test
    void whenPartialUpdateOnlyTouchesPrecondThenOtherAttributesAreAbsent() {
        // Arrange - mirrors a real captured trace: a delta VehicleStatusUpdate (full_update = false) whose
        // raw proto text dump only shows precond_now/precond_state/vtime as populated, yet the previous,
        // unguarded implementation still emitted map entries for all mapped fields.
        VSUMetadata metadata = VSUMetadata.newBuilder().setStatus(AttributeStatus.VALUE_VALID).build();
        PrecondNowEnumAttribute precondNow = PrecondNowEnumAttribute.newBuilder()
                .setValue(PrecondNow.PRECOND_NOW_ACTIVE).setMetadata(metadata).build();
        VehicleStatusUpdate update = VehicleStatusUpdate.newBuilder().setFinOrVin("UNIT_TEST_VIN").setFullUpdate(false)
                .setPrecondNow(precondNow).build();

        // Act
        Map<String, VehicleAttributeStatus> attributes = Mapper.fromVehicleStatusUpdate(update);

        // Assert - only the one field the update actually carried is present
        assertEquals(1, attributes.size(), "only precondNow was set on this partial update");
        assertNotNull(attributes.get(MB_KEY_PRECOND_NOW));
        assertNull(attributes.get(MB_KEY_SOC), "soc was absent from this update, must not appear as 0");
        assertNull(attributes.get(MB_KEY_OVERALL_RANGE), "overallRange was absent, must not appear as 0");
        assertNull(attributes.get(MB_KEY_CHARGING_POWER), "chargingPower was absent, must not appear as 0");
    }
}
