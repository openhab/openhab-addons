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

import static org.junit.Assert.*;
import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.VehicleType;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTrip;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTripContainer;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;

import com.google.gson.Gson;

/**
 * The {@link TripWrapper} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TripWrapper {
    private static final Gson GSON = new Gson();
    private static final Unit<Length> KILOMETRE = MetricPrefix.KILO(SIUnits.METRE);

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
        assertTrue("Same list sizes ", channels.size() == states.size());
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

    private void checkResult(ChannelUID channelUID, State state) {
        String cUid = channelUID.getIdWithoutGroup();
        QuantityType<Length> qt;
        StringType st;
        StringType wanted;
        DateTimeType dtt;
        DecimalType dt;
        switch (cUid) {
            case DATE:
                assertTrue(state instanceof DateTimeType);
                dtt = ((DateTimeType) state);
                DateTimeType expected = DateTimeType.valueOf(Converter.getLocalDateTime(lastTrip.date));
                assertEquals("Trip Date", expected.toString(), dtt.toString());
                break;
            case DURATION:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals("Minute", SmartHomeUnits.MINUTE, qt.getUnit());
                assertEquals("Duration", lastTrip.duration, qt.floatValue(), 0.1);
                break;
            case DISTANCE:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals("KM", KILOMETRE, qt.getUnit());
                assertEquals("Distance", lastTrip.totalDistance, qt.floatValue(), 0.1);
                break;
            case AVG_CONSUMPTION:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals("kw", SmartHomeUnits.KILOWATT_HOUR, qt.getUnit());
                assertEquals("Avg Consumption", lastTrip.avgElectricConsumption, qt.floatValue(), 0.1);
                break;
            case AVG_COMBINED_CONSUMPTION:
                assertTrue("Is Hybrid", isHybrid);
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals("Liter", SmartHomeUnits.LITRE, qt.getUnit());
                assertEquals("Percent", Converter.round(lastTrip.avgCombinedConsumption), qt.floatValue(), 0.01);
                break;
            case AVG_RECUPERATION:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals("kw", SmartHomeUnits.KILOWATT_HOUR, qt.getUnit());
                assertEquals("Avg Recuperation", lastTrip.avgRecuperation, qt.floatValue(), 0.1);
                break;
            default:
                // fail in case of unknown update
                assertFalse("Channel " + channelUID + " " + state + " not found", true);
                break;
        }
    }
}
