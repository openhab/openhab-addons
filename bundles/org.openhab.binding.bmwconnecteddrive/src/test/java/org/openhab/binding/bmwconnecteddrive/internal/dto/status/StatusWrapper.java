/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.status;

import static org.junit.Assert.*;
import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.*;

import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.CarType;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;

import com.google.gson.Gson;

/**
 * The {@link StatusWrapper} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class StatusWrapper {
    private static final Gson GSON = new Gson();
    private static final Unit KILOMETRE = MetricPrefix.KILO(SIUnits.METRE);

    private VehicleStatus vStatus;
    private boolean imperial;
    private boolean isElectric;
    private boolean hasFuel;
    private boolean isHybrid;

    public StatusWrapper(@Nullable String type, boolean imperial, @Nullable String statusJson) {
        this.imperial = imperial;
        hasFuel = type.equals(CarType.CONVENTIONAL.toString()) || type.equals(CarType.PLUGIN_HYBRID.toString())
                || type.equals(CarType.ELECTRIC_REX.toString());
        isElectric = type.equals(CarType.PLUGIN_HYBRID.toString()) || type.equals(CarType.ELECTRIC_REX.toString())
                || type.equals(CarType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;
        VehicleStatusContainer container = GSON.fromJson(statusJson, VehicleStatusContainer.class);
        assertNotNull(container);
        assertNotNull(container.vehicleStatus);
        vStatus = container.vehicleStatus;

    }

    public boolean checkResults(@Nullable List<ChannelUID> channels, @Nullable List<State> states) {
        assertNotNull(channels);
        assertNotNull(states);
        assertTrue("Same list sizes ", channels.size() == states.size());
        for (int i = 0; i < channels.size(); i++) {
            checkResult(channels.get(i), states.get(i));
        }
        return true;
    }

    private void checkResult(ChannelUID channelUID, State state) {
        String cUid = channelUID.getIdWithoutGroup();
        QuantityType qt;
        StringType st;
        DecimalType dt;
        switch (cUid) {
            case MILEAGE:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                if (imperial) {
                    assertEquals("Miles", ImperialUnits.MILE, qt.getUnit());
                } else {
                    assertEquals("KM", KILOMETRE, qt.getUnit());
                }
                assertEquals("Mileage", qt.intValue(), vStatus.mileage);
                break;
            case REMAINING_RANGE_ELECTRIC:
                assertTrue("Is Eelctric", isElectric);
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                if (imperial) {
                    assertEquals("Miles", ImperialUnits.MILE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeElectricMls), 0.01);
                } else {
                    assertEquals("KM", KILOMETRE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeElectric), 0.01);
                }
                break;
            case REMAINING_RANGE_FUEL:
                assertTrue("Has Fuel", hasFuel);
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                if (imperial) {
                    assertEquals("Miles", ImperialUnits.MILE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeFuelMls), 0.01);
                } else {
                    assertEquals("KM", KILOMETRE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeFuel), 0.01);
                }
                break;
            case REMAINING_RANGE_HYBRID:
                assertTrue("Is Hybrid", isHybrid);
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                if (imperial) {
                    assertEquals("Miles", ImperialUnits.MILE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeElectricMls + vStatus.remainingRangeFuelMls), 0.01);
                } else {
                    assertEquals("KM", KILOMETRE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeElectric + vStatus.remainingRangeFuel), 0.01);
                }
                break;
            case REMAINING_FUEL:
                assertTrue("Has Fuel", hasFuel);
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals("Percent", SmartHomeUnits.LITRE, qt.getUnit());
                assertEquals("Percent", Converter.round(vStatus.remainingFuel), Converter.round(qt.floatValue()), 0.01);
                break;
            case REMAINING_SOC:
                assertTrue("Is Eelctric", isElectric);
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals("Percent", SmartHomeUnits.PERCENT, qt.getUnit());
                assertEquals("Percent", Converter.round(vStatus.chargingLevelHv), Converter.round(qt.floatValue()),
                        0.01);
                break;
            case LOCK:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals("Vehicle locked", Converter.toTitleCase(vStatus.doorLockState), st.toString());
                break;
            case DOORS:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                Doors doorState = GSON.fromJson(GSON.toJson(vStatus), Doors.class);
                assertEquals("Doors Closed", VehicleStatus.checkClosed(doorState), st.toString());
                break;
            case WINDOWS:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                Windows windowState = GSON.fromJson(GSON.toJson(vStatus), Windows.class);
                assertEquals("Windows Closed", VehicleStatus.checkClosed(windowState), st.toString());
                break;
            case CHECK_CONTROL:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals("Check Control", vStatus.getCheckControl(), st.toString());
                break;
            case SERVICE:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals("Next Service", vStatus.getNextService(imperial), st.toString());
                break;
            case CHARGING_STATUS:
                assertTrue("Is Eelctric", isElectric);
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals("Charge Status", Converter.toTitleCase(vStatus.chargingStatus), st.toString());
                break;
            case LAST_UPDATE:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                if (vStatus.internalDataTimeUTC != null) {
                    assertEquals("Last Update", Converter.getLocalDateTime(vStatus.internalDataTimeUTC), st.toString());
                } else {
                    assertEquals("Last Update", Converter.getZonedDateTime(vStatus.updateTime), st.toString());
                }
                break;
            case LATITUDE:
                assertTrue(state instanceof DecimalType);
                dt = (DecimalType) state;
                assertNotNull(vStatus.position);
                assertEquals("Latitude", Converter.round(vStatus.position.lat), Converter.round(dt.floatValue()), 0.01);
                break;
            case LONGITUDE:
                assertTrue(state instanceof DecimalType);
                dt = (DecimalType) state;
                assertNotNull(vStatus.position);
                assertEquals("Longitude", Converter.round(vStatus.position.lon), Converter.round(dt.floatValue()),
                        0.01);
                break;
            case HEADING:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals("Angle Unit", SmartHomeUnits.DEGREE_ANGLE, qt.getUnit());
                assertNotNull(vStatus.position);
                assertEquals("Heading", vStatus.position.heading, qt.intValue(), 0.01);
                break;
            case LATLONG:
                assertTrue(state instanceof StringType);
                st = ((StringType) state);
                assertNotNull(vStatus.position);
                assertEquals("Lat/Long", vStatus.position.lat + "," + vStatus.position.lon, st.toString());
                break;
            case RANGE_RADIUS:
                assertTrue(state instanceof DecimalType);
                dt = (DecimalType) state;
                int totalRange = 0;
                if (imperial) {
                    if (isElectric) {
                        totalRange += vStatus.remainingRangeElectricMls * Constants.MILES_TO_FEET_FACTOR;
                    }
                    if (hasFuel) {
                        totalRange += vStatus.remainingRangeFuelMls * Constants.MILES_TO_FEET_FACTOR;
                    }
                } else {
                    if (isElectric) {
                        totalRange += vStatus.remainingRangeElectric * 1000;
                    }
                    if (hasFuel) {
                        totalRange += vStatus.remainingRangeFuel * 1000;
                    }
                }
                assertEquals("Range Circle", totalRange, dt.intValue());
                break;
            default:
                // fail in case of unknown update
                assertFalse("Channel " + channelUID + " " + state + " not found", true);
                break;
        }
    }

}
