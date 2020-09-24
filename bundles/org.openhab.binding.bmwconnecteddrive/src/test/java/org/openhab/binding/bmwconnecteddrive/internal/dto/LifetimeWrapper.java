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
package org.openhab.binding.bmwconnecteddrive.internal.dto;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.VehicleType;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.AllTrips;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.AllTripsContainer;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.SmartHomeUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * The {@link LifetimeWrapper} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class LifetimeWrapper {
    private static final Gson GSON = new Gson();
    private static final Unit<Length> KILOMETRE = MetricPrefix.KILO(SIUnits.METRE);

    private AllTrips allTrips;
    private boolean imperial;
    private boolean isElectric;
    private boolean hasFuel;
    private boolean isHybrid;

    private Map<String, State> specialHandlingMap = new HashMap<String, State>();

    public LifetimeWrapper(String type, boolean imperial, String statusJson) {
        this.imperial = imperial;
        hasFuel = type.equals(VehicleType.CONVENTIONAL.toString()) || type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString());
        isElectric = type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString()) || type.equals(VehicleType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;
        AllTripsContainer container = GSON.fromJson(statusJson, AllTripsContainer.class);
        assertNotNull(container);
        assertNotNull(container.allTrips);
        allTrips = container.allTrips;
    }

    /**
     * Test results auctomatically against json values
     *
     * @param channels
     * @param states
     * @return
     */
    public boolean checkResults(@Nullable List<ChannelUID> channels, @Nullable List<State> states) {
        assertNotNull(channels);
        assertNotNull(states);
        assertTrue(channels.size() == states.size(), "Same list sizes ");
        for (int i = 0; i < channels.size(); i++) {
            checkResult(channels.get(i), states.get(i));
        }
        return true;
    }

    /**
     * Add a specific check for a value e.g. hard coded "Upcoming Service" in order to check the right ordering
     *
     * @param specialHand
     * @return
     */
    public LifetimeWrapper append(Map<String, State> compareMap) {
        specialHandlingMap.putAll(compareMap);
        return this;
    }

    private void checkResult(ChannelUID channelUID, State state) {
        String cUid = channelUID.getIdWithoutGroup();
        QuantityType<Length> qt;
        StringType st;
        StringType wanted;
        DateTimeType dtt;
        DecimalType dt;
        switch (cUid) {
            case DISTANCE_SINCE_CHARGING:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(KILOMETRE, qt.getUnit(), "KM");
                assertEquals(allTrips.chargecycleRange.userCurrentChargeCycle, qt.floatValue(), 0.1,
                        "Distance since charging");
                break;
            case SINGLE_LONGEST_DISTANCE:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(KILOMETRE, qt.getUnit(), "KM");
                assertEquals(allTrips.chargecycleRange.userHigh, qt.floatValue(), 0.1, "Longest Distance");
                break;
            case CUMULATED_DRIVEN_DISTANCE:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(KILOMETRE, qt.getUnit(), "KM");
                assertEquals(allTrips.totalElectricDistance.userTotal, qt.floatValue(), 0.1, "Cumulated Distance");
                break;
            case AVG_CONSUMPTION:
                assertTrue(isElectric, "Is Electric");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(SmartHomeUnits.KILOWATT_HOUR, qt.getUnit(), "kw");
                assertEquals(allTrips.avgElectricConsumption.userAverage, qt.floatValue(), 0.1, "Avg Consumption");
                break;
            case AVG_RECUPERATION:
                assertTrue(isElectric, "Is Electric");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(SmartHomeUnits.KILOWATT_HOUR, qt.getUnit(), "kw");
                assertEquals(allTrips.avgRecuperation.userAverage, qt.floatValue(), 0.1, "Avg Recuperation");
                break;
            case AVG_COMBINED_CONSUMPTION:
                assertTrue(isHybrid, "Is Hybrid");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(SmartHomeUnits.LITRE, qt.getUnit(), "Liter");
                assertEquals(allTrips.avgCombinedConsumption.userAverage, qt.floatValue(), 0.01,
                        "Avg Combined Consumption");
                break;
            default:
                // fail in case of unknown update
                assertFalse(true, "Channel " + channelUID + " " + state + " not found");
                break;
        }
    }
}
