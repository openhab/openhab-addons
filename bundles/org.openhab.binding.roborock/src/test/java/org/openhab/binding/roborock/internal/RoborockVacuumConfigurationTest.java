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
package org.openhab.binding.roborock.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault({})
class RoborockVacuumConfigurationTest {

    @Test
    void refreshDefaultsAreLegacyMinutesConvertedToSeconds() {
        RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();

        assertEquals(300, config.getRefreshIntervalSeconds());
        assertEquals(15, config.getFastRefreshIntervalSeconds());
        assertEquals(300, config.getCloudRefreshIntervalSeconds());
        assertEquals(30, config.getMapRefreshCloudCleaningIntervalSeconds());
        assertEquals(15, config.getMapRefreshDirectCleaningIntervalSeconds());
    }

    @Test
    void legacyRefreshInputIsInterpretedAsMinutes() {
        RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();

        config.refresh = 1;
        assertEquals(60, config.getRefreshIntervalSeconds());

        config.refresh = 2;
        assertEquals(120, config.getRefreshIntervalSeconds());

        config.refresh = 0;
        assertEquals(300, config.getRefreshIntervalSeconds());

        config.refresh = -10;
        assertEquals(300, config.getRefreshIntervalSeconds());
    }

    @Test
    void fastRefreshIntervalUsesConfiguredValueAndFallsBackToDefault() {
        RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();

        config.fastRefreshInterval = 20;
        assertEquals(20, config.getFastRefreshIntervalSeconds());

        config.fastRefreshInterval = 0;
        assertEquals(15, config.getFastRefreshIntervalSeconds());

        config.fastRefreshInterval = -10;
        assertEquals(15, config.getFastRefreshIntervalSeconds());
    }

    @Test
    void cloudRefreshIntervalUsesConfiguredValueAndFallsBackToRefreshInterval() {
        RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();
        config.refresh = 2;

        assertEquals(120, config.getCloudRefreshIntervalSeconds());

        config.cloudRefreshInterval = 30;
        assertEquals(60, config.getCloudRefreshIntervalSeconds());

        config.cloudRefreshInterval = 90;
        assertEquals(90, config.getCloudRefreshIntervalSeconds());

        config.cloudRefreshInterval = 0;
        assertEquals(120, config.getCloudRefreshIntervalSeconds());

        config.cloudRefreshInterval = -10;
        assertEquals(120, config.getCloudRefreshIntervalSeconds());
    }

    @Test
    void cloudOnlyRefreshDefaultsToEnabled() {
        RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();

        assertTrue(config.isCloudMapRefreshEnabled());
        assertTrue(config.isCloudMetadataRefreshEnabled());
    }

    @Test
    void cloudOnlyRefreshCanBeDisabledWithOffValue() {
        RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();
        config.cloudMapRefresh = RoborockVacuumConfiguration.REFRESH_OFF;
        config.cloudMetadataRefresh = RoborockVacuumConfiguration.REFRESH_OFF;

        assertFalse(config.isCloudMapRefreshEnabled());
        assertFalse(config.isCloudMetadataRefreshEnabled());
    }

    @Test
    void cloudOnlyRefreshTreatsUnknownValuesAsEnabled() {
        RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();
        config.cloudMapRefresh = "invalid";
        config.cloudMetadataRefresh = "enabled";

        assertTrue(config.isCloudMapRefreshEnabled());
        assertTrue(config.isCloudMetadataRefreshEnabled());
    }

    @Test
    void mapRefreshCleaningIntervalsAreClampedToSafeMinimums() {
        RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();

        config.mapRefreshCloudCleaningInterval = 10;
        assertEquals(30, config.getMapRefreshCloudCleaningIntervalSeconds());

        config.mapRefreshDirectCleaningInterval = 10;
        assertEquals(15, config.getMapRefreshDirectCleaningIntervalSeconds());
    }

    @Test
    void mapRefreshDuringCleaningUsesCommunicationModeSpecificInterval() {
        RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();
        config.mapRefreshCloudCleaningInterval = 40;
        config.mapRefreshDirectCleaningInterval = 20;

        assertEquals(40, config.getMapRefreshDuringCleaningIntervalSeconds(RoborockCommunicationMode.CLOUD));
        assertEquals(20, config.getMapRefreshDuringCleaningIntervalSeconds(RoborockCommunicationMode.DIRECT));
    }
}
