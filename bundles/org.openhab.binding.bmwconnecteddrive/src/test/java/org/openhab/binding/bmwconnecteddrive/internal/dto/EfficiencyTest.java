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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.bmwconnecteddrive.internal.dto.efficiency.CharacterristicsScore;
import org.openhab.binding.bmwconnecteddrive.internal.dto.efficiency.Efficiency;
import org.openhab.binding.bmwconnecteddrive.internal.dto.efficiency.Score;
import org.openhab.binding.bmwconnecteddrive.internal.dto.efficiency.TripEntry;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;

import com.google.gson.Gson;

/**
 * The {@link EfficiencyTest} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class EfficiencyTest {
    private static final Gson GSON = new Gson();

    @Test
    public void testtestBEV_REX_Values() {
        String resource1 = FileReader.readFileInString("src/test/resources/efficiency.json");
        Efficiency eff = GSON.fromJson(resource1, Efficiency.class);

        assertEquals("Community", false, eff.communitySwitch);
        assertEquals("Quotient", 44, eff.efficiencyQuotient);
        assertEquals("Model", "I3", eff.modelType);

        List<Score> scoreList = eff.scoreList;
        assertEquals("Score List", 4, scoreList.size());
        List<TripEntry> lastTripList = eff.lastTripList;
        assertEquals("Score List", 5, lastTripList.size());
        List<CharacterristicsScore> characteristicList = eff.characteristicList;
        assertEquals("Score List", 5, characteristicList.size());

        if (eff.lastTripList != null) {
            eff.lastTripList.forEach(entry -> {
                System.out.println(entry.name + ":" + entry.lastTrip);
                if (entry.name.equals(TripEntry.LASTTRIP_DELTA_KM)) {
                    assertEquals("Trip Delta km", 2, entry.lastTrip, 0.01);
                } else if (entry.name.equals(TripEntry.ACTUAL_DISTANCE_WITHOUT_CHARGING)) {
                    assertEquals("Charge Delta km", 31, entry.lastTrip, 0.01);
                } else if (entry.name.equals(TripEntry.AVERAGE_ELECTRIC_CONSUMPTION)) {
                    assertEquals("Average Consumption", 14.5, entry.lastTrip, 0.01);
                } else if (entry.name.equals(TripEntry.AVERAGE_RECUPERATED_ENERGY_PER_100_KM)) {
                    assertEquals("Average Recuperation", 8.0, entry.lastTrip, 0.01);
                }
            });
        }
        if (eff.scoreList != null) {
            eff.scoreList.forEach(entry -> {
                if (entry.attrName.equals(Score.CUMULATED_ELECTRIC_DRIVEN_DISTANCE)) {
                    assertEquals("Trip Delta km", 16592.4, entry.lifeTime, 0.01);
                } else if (entry.attrName.equals(Score.LONGEST_DISTANCE_WITHOUT_CHARGING)) {
                    assertEquals("Trip Delta km", 185.5, entry.lifeTime, 0.01);
                } else if (entry.attrName.equals(Score.AVERAGE_ELECTRIC_CONSUMPTION)) {
                    assertEquals("Average Consumption", 16.5, entry.lifeTime, 0.01);
                } else if (entry.attrName.equals(Score.AVERAGE_RECUPERATED_ENERGY_PER_100_KM)) {
                    assertEquals("Average Recuperation", 4.5, entry.lifeTime, 0.01);
                }
            });
        }
    }
}
