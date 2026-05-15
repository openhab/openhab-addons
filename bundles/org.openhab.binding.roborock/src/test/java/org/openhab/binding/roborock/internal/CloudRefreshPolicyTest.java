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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault({})
class CloudRefreshPolicyTest {

    @Test
    void directModeRespectsCloudMapAndMetadataRefreshFlags() {
        RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();
        config.cloudMapRefresh = RoborockVacuumConfiguration.REFRESH_OFF;
        config.cloudMetadataRefresh = RoborockVacuumConfiguration.REFRESH_OFF;

        assertFalse(CloudRefreshPolicy.isCloudMapRefreshAllowed(RoborockCommunicationMode.DIRECT, config));
        assertFalse(CloudRefreshPolicy.isCloudMetadataRefreshAllowed(RoborockCommunicationMode.DIRECT, config));
    }

    @Test
    void cloudModeAlwaysAllowsCloudMapAndMetadataRefresh() {
        RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();
        config.cloudMapRefresh = RoborockVacuumConfiguration.REFRESH_OFF;
        config.cloudMetadataRefresh = RoborockVacuumConfiguration.REFRESH_OFF;

        assertTrue(CloudRefreshPolicy.isCloudMapRefreshAllowed(RoborockCommunicationMode.CLOUD, config));
        assertTrue(CloudRefreshPolicy.isCloudMetadataRefreshAllowed(RoborockCommunicationMode.CLOUD, config));
    }

    @Test
    void cloudOnlyRefreshDueUsesConfiguredSecondsInterval() {
        assertTrue(CloudRefreshPolicy.isCloudOnlyRefreshDue(1_000L, 0L, 120));
        assertFalse(CloudRefreshPolicy.isCloudOnlyRefreshDue(61_000L, 1_000L, 120));
        assertTrue(CloudRefreshPolicy.isCloudOnlyRefreshDue(121_000L, 1_000L, 120));
    }
}
