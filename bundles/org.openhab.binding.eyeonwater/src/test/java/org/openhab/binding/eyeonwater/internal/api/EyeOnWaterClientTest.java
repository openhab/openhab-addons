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
package org.openhab.binding.eyeonwater.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.eyeonwater.internal.api.EyeOnWaterClient.EyeOnWaterMeterData;

/**
 * Tests for {@link EyeOnWaterClient}.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
class EyeOnWaterClientTest {

    private final EyeOnWaterClient client = new EyeOnWaterClient("eyeonwater.com", "testuser", "testpassword",
            Mockito.mock(HttpClient.class));

    @Test
    void testParseMetersFromDashboardStandard() {
        String html = "<html><head><script>\n" + "AQ.Views.MeterPicker.meters = [\n" + "  {\n"
                + "    \"meter_uuid\": \"12345678-abcd-1234-abcd-123456789012\",\n"
                + "    \"meter_id\": \"METER-123\"\n" + "  }\n" + "];\n" + "</script></head><body></body></html>";

        List<EyeOnWaterMeterData> meters = client.parseMetersFromDashboard(html);
        assertEquals(1, meters.size());
        assertEquals("12345678-abcd-1234-abcd-123456789012", meters.get(0).getMeterUuid());
        assertEquals("METER-123", meters.get(0).getMeterId());
    }

    @Test
    void testParseMetersFromDashboardMinified() {
        String html = "<html><body><script>AQ.Views.MeterPicker.meters=[{\"meter_uuid\":\"uuid-999\",\"meter_id\":\"id-999\"}];var x=10;</script></body></html>";

        List<EyeOnWaterMeterData> meters = client.parseMetersFromDashboard(html);
        assertEquals(1, meters.size());
        assertEquals("uuid-999", meters.get(0).getMeterUuid());
        assertEquals("id-999", meters.get(0).getMeterId());
    }

    @Test
    void testParseMetersFromDashboardNoMatch() {
        String html = "<html><body>No meters here!</body></html>";

        List<EyeOnWaterMeterData> meters = client.parseMetersFromDashboard(html);
        assertTrue(meters.isEmpty());
    }

    @Test
    void testParseMetersFromDashboardIncompleteData() {
        String html = "<html><body><script>AQ.Views.MeterPicker.meters=[{\"meter_id\":\"missing-uuid\"}];</script></body></html>";

        List<EyeOnWaterMeterData> meters = client.parseMetersFromDashboard(html);
        assertTrue(meters.isEmpty());
    }
}
