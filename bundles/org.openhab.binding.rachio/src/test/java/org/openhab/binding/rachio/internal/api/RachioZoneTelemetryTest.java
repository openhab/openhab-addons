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
package org.openhab.binding.rachio.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_IMAGE_PATH;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_IMAGE_URL_BASE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;

/**
 * Tests zone telemetry model helpers.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioZoneTelemetryTest {
    @Test
    void zoneKeepsOriginalRachioImageUrlForNativeImageDownload() {
        RachioCloudZone cloudZone = new RachioCloudZone();
        cloudZone.id = "zone-id";
        cloudZone.name = "Front Lawn";
        cloudZone.zoneNumber = 1;
        cloudZone.imageUrl = SERVLET_IMAGE_URL_BASE + "photo-id";

        RachioZone zone = new RachioZone(cloudZone, "controller-id");

        assertThat(zone.imageUrl, is(SERVLET_IMAGE_PATH + "/photo-id"));
        assertThat(zone.getImageDownloadUrl(), is(SERVLET_IMAGE_URL_BASE + "photo-id"));
    }

    @Test
    void zoneUpdateCopiesApiBackedFieldsWithoutDerivingMoisture() {
        RachioCloudZone originalCloudZone = new RachioCloudZone();
        originalCloudZone.id = "zone-id";
        originalCloudZone.name = "Old Name";
        originalCloudZone.zoneNumber = 1;
        RachioZone zone = new RachioZone(originalCloudZone, "controller-id");
        zone.setMoistureLevel(42);
        zone.setMoisturePercent(0.4);

        RachioCloudZone updatedCloudZone = new RachioCloudZone();
        updatedCloudZone.id = "zone-id";
        updatedCloudZone.name = "Front Lawn";
        updatedCloudZone.zoneNumber = 2;
        updatedCloudZone.enabled = false;
        updatedCloudZone.runtime = 600;
        updatedCloudZone.availableWater = 0.07;
        updatedCloudZone.depthOfWater = 1.07;
        updatedCloudZone.saturatedDepthOfWater = 1.18;
        updatedCloudZone.rootZoneDepth = 9;
        updatedCloudZone.managementAllowedDepletion = 0.5;
        updatedCloudZone.efficiency = 0.8;
        updatedCloudZone.yardAreaSquareFeet = 500;
        updatedCloudZone.imageUrl = "https://example.com/zone.jpg";
        updatedCloudZone.lastWateredDate = 1_523_129_743_000L;
        updatedCloudZone.fixedRuntime = 300;
        updatedCloudZone.maxRuntime = 10_800;
        updatedCloudZone.runtimeNoMultiplier = 627;
        updatedCloudZone.scheduleDataModified = true;
        RachioZone updatedZone = new RachioZone(updatedCloudZone, "controller-id");

        assertThat(zone.compare(updatedZone), is(false));
        zone.update(updatedZone);

        assertThat(zone.name, is("Front Lawn"));
        assertThat(zone.zoneNumber, is(2));
        assertThat(zone.enabled, is(false));
        assertThat(zone.runtime, is(600));
        assertThat(zone.availableWater, is(0.07));
        assertThat(zone.depthOfWater, is(1.07));
        assertThat(zone.saturatedDepthOfWater, is(1.18));
        assertThat(zone.rootZoneDepth, is(9.0));
        assertThat(zone.managementAllowedDepletion, is(0.5));
        assertThat(zone.efficiency, is(0.8));
        assertThat(zone.yardAreaSquareFeet, is(500));
        assertThat(zone.imageUrl, is("https://example.com/zone.jpg"));
        assertThat(zone.lastWateredDate, is(1_523_129_743_000L));
        assertThat(zone.fixedRuntime, is(300));
        assertThat(zone.maxRuntime, is(10_800));
        assertThat(zone.runtimeNoMultiplier, is(627));
        assertThat(zone.scheduleDataModified, is(true));
        assertThat(zone.getMoistureLevel(), is(42.0));
        assertThat(zone.getMoisturePercent(), is(0.4));
        assertThat(zone.compare(updatedZone), is(true));
    }
}
