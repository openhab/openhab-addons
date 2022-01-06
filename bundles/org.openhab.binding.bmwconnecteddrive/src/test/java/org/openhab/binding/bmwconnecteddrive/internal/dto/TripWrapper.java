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
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTrip;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTripContainer;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * The {@link TripWrapper} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class TripWrapper {
    private static final Gson GSON = new Gson();
    private static final Unit<Length> MILES = ImperialUnits.MILE;

    private LastTrip lastTrip;
    private boolean imperial;
    private boolean isElectric;
    private boolean hasFuel;
    private boolean isHybrid;

    private Map<String, State> specialHandlingMap = new HashMap<String, State>();

    public TripWrapper(String type, boolean imperial, String statusJson) {
        this.imperial = imperial;
        hasFuel = type.equals(VehicleType.CONVENTIONAL.toString()) || type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString());
        isElectric = type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString()) || type.equals(VehicleType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;
        LastTripContainer container = GSON.fromJson(statusJson, LastTripContainer.class);
        assertNotNull(container);
        assertNotNull(container.lastTrip);
        lastTrip = container.lastTrip;
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
        assertTrue(channels.size() == states.size(), "Same list sizes");
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
    public TripWrapper append(Map<String, State> compareMap) {
        specialHandlingMap.putAll(compareMap);
        return this;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void checkResult(ChannelUID channelUID, State state) {
        String cUid = channelUID.getIdWithoutGroup();
        QuantityType<Length> qt;
        DateTimeType dtt;
        switch (cUid) {
            case DATE:
                assertTrue(state instanceof DateTimeType);
                dtt = ((DateTimeType) state);
                DateTimeType expected = DateTimeType.valueOf(Converter.getLocalDateTimeWithoutOffest(lastTrip.date));
                assertEquals(expected.toString(), dtt.toString(), "Trip Date");
                break;
            case DURATION:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(Units.MINUTE, qt.getUnit(), "Minute");
                assertEquals(lastTrip.duration, qt.floatValue(), 0.1, "Duration");
                break;
            case DISTANCE:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                if (imperial) {
                    assertEquals(MILES, qt.getUnit(), "Miles");
                    assertEquals(lastTrip.totalDistance / Converter.MILES_TO_KM_RATIO, qt.floatValue(), 0.1,
                            "Distance");

                } else {
                    assertEquals(Constants.KILOMETRE_UNIT, qt.getUnit(), "KM");
                    assertEquals(lastTrip.totalDistance, qt.floatValue(), 0.1, "Distance");
                }
                break;
            case AVG_CONSUMPTION:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(Units.KILOWATT_HOUR, qt.getUnit(), "kw/h");
                if (imperial) {
                    assertEquals(lastTrip.avgElectricConsumption * Converter.MILES_TO_KM_RATIO, qt.floatValue(), 0.1,
                            "Avg Consumption");
                } else {
                    assertEquals(lastTrip.avgElectricConsumption, qt.floatValue(), 0.1, "Avg Consumption");
                }
                break;
            case AVG_COMBINED_CONSUMPTION:
                assertTrue(isHybrid, "Is Hybrid");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(Units.LITRE, qt.getUnit(), "Liter");
                if (imperial) {
                    assertEquals(Converter.round(lastTrip.avgCombinedConsumption * Converter.MILES_TO_KM_RATIO),
                            qt.floatValue(), 0.01, "Percent");
                } else {
                    assertEquals(Converter.round(lastTrip.avgCombinedConsumption), qt.floatValue(), 0.01, "Percent");
                }
                break;
            case AVG_RECUPERATION:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(Units.KILOWATT_HOUR, qt.getUnit(), "kw/h");
                if (imperial) {
                    assertEquals(lastTrip.avgRecuperation * Converter.MILES_TO_KM_RATIO, qt.floatValue(), 0.1,
                            "Avg Recuperation");
                } else {
                    assertEquals(lastTrip.avgRecuperation, qt.floatValue(), 0.1, "Avg Recuperation");
                }
                break;
            default:
                // fail in case of unknown update
                assertFalse(true, "Channel " + channelUID + " " + state + " not found");
                break;
        }
    }
}
