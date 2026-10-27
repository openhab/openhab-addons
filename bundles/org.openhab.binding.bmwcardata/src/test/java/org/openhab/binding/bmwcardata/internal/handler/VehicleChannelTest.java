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
package org.openhab.binding.bmwcardata.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Tests the conversion of descriptor values by {@link VehicleChannel}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class VehicleChannelTest {

    @Test
    void descriptorsAreUniqueAndResolvable() {
        for (VehicleChannel channel : VehicleChannel.values()) {
            assertEquals(channel, VehicleChannel.fromDescriptor(channel.getDescriptor()));
        }
        assertEquals(VehicleChannel.values().length, VehicleChannel.descriptors().size());
        assertNull(VehicleChannel.fromDescriptor("vehicle.unknown"));
    }

    @Test
    void channelIdsFollowNamingConvention() {
        Pattern lowerCaseHyphen = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");
        for (VehicleChannel channel : VehicleChannel.values()) {
            String channelUid = channel.getChannelUid();
            if (channelUid != null) {
                for (String segment : channelUid.split("#")) {
                    assertTrue(lowerCaseHyphen.matcher(segment).matches(), channelUid);
                }
            }
        }
        assertEquals("cabin-sunroof-tilt-status", DynamicChannel.channelId("vehicle.cabin.sunroof.tiltStatus"));
        assertEquals("chassis-axle-row1-wheel-left-tire-pressure",
                DynamicChannel.channelId("vehicle.chassis.axle.row1.wheel.left.tire.pressure"));
        assertEquals("cabin-hvac-preconditioning-status-is-exterior-mirror-heating-active",
                DynamicChannel.channelId("vehicle.cabin.hvac.preconditioning.status.isExteriorMirrorHeatingActive"));
    }

    @Test
    void quantityUsesReportedUnit() {
        assertEquals(new QuantityType<>(120, ImperialUnits.MILE),
                VehicleChannel.MILEAGE.toState(value(new JsonPrimitive("120"), "miles")));
        assertEquals(new QuantityType<>(80, Units.PERCENT),
                VehicleChannel.SOC.toState(value(new JsonPrimitive(80), "percent")));
        assertEquals(QuantityType.valueOf("11 kW"),
                VehicleChannel.CHARGING_POWER.toState(value(new JsonPrimitive(11), "kW")));
    }

    @Test
    void quantityFallsBackToDefaultUnit() {
        assertEquals(QuantityType.valueOf("12345 km"),
                VehicleChannel.MILEAGE.toState(value(new JsonPrimitive(12345), null)));
        // the catalogue documents the charging power in W
        assertEquals(QuantityType.valueOf("11 W"),
                VehicleChannel.CHARGING_POWER.toState(value(new JsonPrimitive(11), null)));
        // a unit the binding does not know is a guess, which is logged as a warning once
        assertEquals(QuantityType.valueOf("12345 km"),
                VehicleChannel.MILEAGE.toState(value(new JsonPrimitive(12345), "unknownUnit")));
    }

    @Test
    void quantityRejectsIncompatibleUnitAndNonNumbers() {
        // a unit of another dimension is not interpreted with the default unit
        assertEquals(UnDefType.UNDEF, VehicleChannel.MILEAGE.toState(value(new JsonPrimitive(1), "V")));
        assertEquals(UnDefType.UNDEF, VehicleChannel.MILEAGE.toState(value(new JsonPrimitive("n/a"), "km")));
        assertNull(VehicleChannel.MILEAGE.toState(value(null, "km")));
    }

    @Test
    void booleansAndStrings() {
        assertEquals(OnOffType.ON, VehicleChannel.PLUGGED.toState(value(new JsonPrimitive(true), null)));
        assertEquals(OnOffType.OFF, VehicleChannel.MOVING.toState(value(new JsonPrimitive("false"), null)));
        assertEquals(UnDefType.UNDEF, VehicleChannel.MOVING.toState(value(new JsonPrimitive("UNKNOWN"), null)));
        assertEquals(UnDefType.UNDEF, VehicleChannel.MOVING.toState(value(new JsonPrimitive("maybe"), null)));
        assertEquals(OpenClosedType.OPEN, VehicleChannel.TRUNK.toState(value(new JsonPrimitive(true), null)));
        assertEquals(OpenClosedType.CLOSED, VehicleChannel.HOOD.toState(value(new JsonPrimitive("false"), null)));
        assertEquals(new StringType("SECURED"),
                VehicleChannel.DOOR_LOCK.toState(value(new JsonPrimitive("SECURED"), null)));
    }

    @Test
    void doorStatesAndPlaceholders() {
        assertEquals(OpenClosedType.OPEN,
                VehicleChannel.DOOR_FRONT_DRIVER.toState(value(new JsonPrimitive("OPEN"), null)));
        assertEquals(OpenClosedType.CLOSED,
                VehicleChannel.DOOR_REAR_PASSENGER.toState(value(new JsonPrimitive("CLOSED"), null)));
        assertEquals(UnDefType.UNDEF,
                VehicleChannel.DOOR_FRONT_DRIVER.toState(value(new JsonPrimitive("UNKNOWN"), null)));
        assertEquals(UnDefType.UNDEF, VehicleChannel.AC_CURRENT.toState(value(new JsonPrimitive("-NA-"), "A")));
        assertEquals(UnDefType.UNDEF,
                VehicleChannel.WINDOW_FRONT_DRIVER.toState(value(new JsonPrimitive("INVALID"), null)));
    }

    @Test
    void catalogueUnits() {
        assertEquals(QuantityType.valueOf("230 kPa"),
                VehicleChannel.PRESSURE_FRONT_LEFT.toState(value(new JsonPrimitive("230"), "kPa")));
        assertEquals(new QuantityType<>(21, SIUnits.CELSIUS),
                VehicleChannel.TEMPERATURE_REAR_RIGHT.toState(value(new JsonPrimitive("21"), "Celsius")));
        assertEquals(QuantityType.valueOf("16 A"), VehicleChannel.AC_LIMIT.toState(value(new JsonPrimitive(16), "A")));
    }

    @Test
    void tripEndTimeFormats() {
        DateTimeType expected = new DateTimeType(Instant.parse("2017-05-15T15:51:00Z"));
        assertEquals(expected,
                VehicleChannel.TRIP_END.toState(value(new JsonPrimitive("15.05.2017 15:51:00 UTC"), null)));
        assertEquals(expected, VehicleChannel.TRIP_END.toState(value(new JsonPrimitive("2017-05-15T15:51:00Z"), null)));
        assertEquals(expected, VehicleChannel.TRIP_END.toState(value(new JsonPrimitive(1494863460), null)));
        assertEquals(expected, VehicleChannel.TRIP_END.toState(value(new JsonPrimitive("1494863460"), null)));
        assertEquals(UnDefType.UNDEF, VehicleChannel.TRIP_END.toState(value(new JsonPrimitive("yesterday"), null)));
    }

    @Test
    void zeroBatteryCapacityIsUndefined() {
        assertEquals(UnDefType.UNDEF, VehicleChannel.BATTERY_CAPACITY.toState(value(new JsonPrimitive("0"), "kWh")));
        assertEquals(UnDefType.UNDEF, VehicleChannel.BATTERY_CAPACITY.toState(value(new JsonPrimitive(0.0), "kWh")));
        assertEquals(QuantityType.valueOf("80.7 kWh"),
                VehicleChannel.BATTERY_CAPACITY.toState(value(new JsonPrimitive(80.7), "kWh")));
        // other channels keep 0 as a value
        assertEquals(QuantityType.valueOf("0 kWh"),
                VehicleChannel.BATTERY_ENERGY.toState(value(new JsonPrimitive(0), "kWh")));
    }

    @Test
    void doorStatusUpdatesTheDoorLock() {
        assertEquals(VehicleChannel.DOOR_LOCK.getChannelUid(), VehicleChannel.DOOR_STATUS.getChannelUid());
        assertEquals(new StringType("SECURED"),
                VehicleChannel.DOOR_STATUS.toState(value(new JsonPrimitive("SECURED"), null)));
    }

    @Test
    void engineStatesAndInspectionDate() {
        assertEquals(OnOffType.ON, VehicleChannel.ENGINE.toState(value(new JsonPrimitive("ASN_isTrue"), null)));
        assertEquals(OnOffType.OFF, VehicleChannel.IGNITION.toState(value(new JsonPrimitive("ASN_isFalse"), null)));
        assertEquals(UnDefType.UNDEF, VehicleChannel.IGNITION.toState(value(new JsonPrimitive("ASN_isUnknown"), null)));
        assertEquals(new DateTimeType(Instant.parse("2018-09-30T23:00:00Z")),
                VehicleChannel.INSPECTION_DATE.toState(value(new JsonPrimitive("30.09.2018 23:00 UTC"), null)));
    }

    @Test
    void numbersWithoutDimensionIgnoreUnit() {
        assertEquals(new DecimalType(new BigDecimal("12.5")),
                VehicleChannel.TRIP_RECUPERATION.toState(value(new JsonPrimitive("12.5"), "kWh/100km")));
        assertEquals(new DecimalType(new BigDecimal("4.5")),
                VehicleChannel.TRIP_ACCELERATION_RATING.toState(value(new JsonPrimitive("4.5"), "stars")));
    }

    @Test
    void openingsAreOpenWhenPartlyOpenAndReportTheirState() {
        TelematicValue intermediate = value(new JsonPrimitive("INTERMEDIATE"), null);
        assertEquals(OpenClosedType.OPEN, VehicleChannel.WINDOW_FRONT_DRIVER.toState(intermediate));
        assertEquals(new StringType("INTERMEDIATE"), VehicleChannel.WINDOW_FRONT_DRIVER.toDetailState(intermediate));
        assertEquals("status#window-front-driver-state", VehicleChannel.WINDOW_FRONT_DRIVER.getDetailChannelUid());

        TelematicValue tilted = value(new JsonPrimitive("OPEN_TILT"), null);
        assertEquals(OpenClosedType.OPEN, VehicleChannel.SUNROOF.toState(tilted));
        assertEquals(new StringType("OPEN_TILT"), VehicleChannel.SUNROOF.toDetailState(tilted));
        assertEquals(OpenClosedType.CLOSED, VehicleChannel.SUNROOF.toState(value(new JsonPrimitive("CLOSED"), null)));

        // doors only report OPEN or CLOSED and have no state channel
        assertNull(VehicleChannel.DOOR_FRONT_DRIVER.getDetailChannelUid());
    }

    @Test
    void wordsAreNormalized() {
        assertEquals(new StringType("SELECTIVELOCKED"),
                VehicleChannel.DOOR_LOCK.toState(value(new JsonPrimitive("SELECTIVE-LOCKED"), null)));
        assertEquals(new StringType("SECURED"),
                VehicleChannel.DOOR_LOCK.toState(value(new JsonPrimitive("ASN_secured"), null)));
        assertEquals(OnOffType.ON, VehicleChannel.DEEP_SLEEP.toState(value(new JsonPrimitive("ACTIVE"), null)));
        assertEquals(OnOffType.OFF, VehicleChannel.DEEP_SLEEP.toState(value(new JsonPrimitive("Off"), null)));
        assertEquals(UnDefType.UNDEF, VehicleChannel.DOOR_LOCK.toState(value(new JsonPrimitive("N/A"), null)));
        assertEquals(UnDefType.UNDEF, VehicleChannel.CHARGING_STATUS.toState(value(new JsonPrimitive(""), null)));
    }

    @Test
    void moreUnitsAndDateFormats() {
        assertEquals(new QuantityType<>(30, ImperialUnits.MILES_PER_HOUR),
                VehicleChannel.AVG_SPEED.toState(value(new JsonPrimitive(30), "mph")));
        assertEquals(new QuantityType<>(12, ImperialUnits.GALLON_LIQUID_US),
                VehicleChannel.REMAINING_FUEL.toState(value(new JsonPrimitive(12), "gal")));
        assertEquals(new DateTimeType(Instant.parse("2018-09-30T23:00:00Z")),
                VehicleChannel.INSPECTION_DATE.toState(value(new JsonPrimitive("09/30/2018 23:00 UTC"), null)));
        assertEquals(new DateTimeType(Instant.parse("2027-03-01T00:00:00Z")),
                VehicleChannel.INSPECTION_DATE.toState(value(new JsonPrimitive("01.03.2027"), null)));
    }

    @Test
    void dynamicChannelsInferSwitchesOnlyFromBooleans() {
        assertEquals(DynamicChannel.TYPE_SWITCH, Objects
                .requireNonNull(DynamicChannel.define(value(new JsonPrimitive("ASN_isTrue"), null))).channelTypeId());
        // ACTIVE may be one value of a longer list, e.g. ACTIVE, INACTIVE, STANDBY
        assertEquals(DynamicChannel.TYPE_STRING, Objects
                .requireNonNull(DynamicChannel.define(value(new JsonPrimitive("ACTIVE"), null))).channelTypeId());
    }

    static TelematicValue value(@Nullable JsonElement element, @Nullable String unit) {
        TelematicValue value = new TelematicValue();
        value.value = element;
        value.unit = unit;
        return value;
    }
}
