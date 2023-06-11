/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.ZoneId;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.TrendDescription;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.core.i18n.TimeZoneProvider;

/**
 * @author Gaël L'hopital - Initial contribution
 */
public class NAObjectTest {
    private static NADeserializer gson;

    @BeforeAll
    public static void init() {
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.systemDefault());
        gson = new NADeserializer(timeZoneProvider);
    }

    @Test
    public void testNAObject() throws Exception {
        String naObject = "{id:\"5954e7f249c75f97428b7b23\",name:\"Your House\"}";
        NAObject object = gson.deserialize(NAObject.class, naObject);
        assertEquals(object.getName(), "Your House");
        assertEquals(object.getId(), "5954e7f249c75f97428b7b23");
    }

    @Test
    public void testWebHookEvent() throws NetatmoException {
        String event = "{" + "  \"user_id\": \"5c810xxxxxxx45f4\"," + "  \"snapshot_id\": \"5d19bxxxxxx6380342\","
                + "  \"snapshot_key\": \"f0134210ff83fxxxxxxxf770090a423d9a5\","
                + "  \"snapshot_url\": \"https://netatmocameraimage.blob.core.windows.net/production/5d1xxxa5\","
                + "  \"event_type\": \"movement\"," + "  \"camera_id\": \"70:exxxxxdd:a7\","
                + "  \"device_id\": \"70:exxxxdd:a7\"," + "  \"home_id\": \"5c5d79xxxx08cd594\","
                + "  \"home_name\": \"Boulogne Billan.\"," + "  \"event_id\": \"5d19baae369359e896380341\","
                + "  \"message\": \"Boulogne Billan: Movement detected by Indoor Camera\","
                + "  \"push_type\": \"NACamera-movement\"" + "}";
        WebhookEvent object = gson.deserialize(WebhookEvent.class, event);
        assertEquals(object.getEventType(), EventType.MOVEMENT);
    }

    @Test
    public void testDashboardData() throws NetatmoException {
        String dashboard = "{time_utc:1623160336,Temperature:22.1,CO2:511,"
                + "Humidity:66,Noise:36,Pressure:1026.1,AbsolutePressure:1009.3,"
                + "min_temp:20,max_temp:22.4,date_max_temp:1623147932,"
                + "Sdate_min_temp:1623125249,pressure_trend:\"nonexistent\",temp_trend:\"stable\"}";
        Dashboard object = gson.deserialize(Dashboard.class, dashboard);
        assertEquals(511, object.getCo2(), 0);
        assertEquals(TrendDescription.UNKNOWN, object.getPressureTrend());
        assertEquals(TrendDescription.STABLE, object.getTempTrend());
    }
}
