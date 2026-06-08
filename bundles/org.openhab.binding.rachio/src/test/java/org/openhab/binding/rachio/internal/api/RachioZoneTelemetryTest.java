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

import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;

/**
 * Tests zone telemetry model helpers.
 *
 * @author openHAB Contributors - Initial contribution
 */
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
}
