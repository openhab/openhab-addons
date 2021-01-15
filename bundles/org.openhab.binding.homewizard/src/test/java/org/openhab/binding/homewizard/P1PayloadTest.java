/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.homewizard;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homewizard.data.P1Payload;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests {@link P1Payload}.
 *
 * @author Daniël van Os - Initial contribution
 */
public class P1PayloadTest {

    private P1Payload payload;

    @BeforeEach
    public void setUp() {
        payload = new P1Payload();
    }

    @Test
    public void stringProperiesTest() {
        String model = "Nice Model";
        String ssid = "FreeWifi";

        payload.setMeter_model(model);
        payload.setWifi_ssid(ssid);

        assertThat(payload.getMeter_model(), is(model));
        assertThat(payload.getWifi_ssid(), is(ssid));
    }

    @Test
    public void intPropertiesTest() {
        int version = 42;
        int rssi = 1337;

        payload.setSmr_version(version);
        payload.setWifi_strength(rssi);

        assertThat(payload.getSmr_version(), is(version));
        assertThat(payload.getWifi_strength(), is(rssi));
    }

    @Test
    public void longPropertiesTest() {
        long ts = 137137137;
        payload.setGas_timestamp(ts);
        assertThat(payload.getGas_timestamp(), is(ts));
    }

    @Test
    public void doublePropertiesTest() {
        double it1 = 1.337;
        double it2 = 2.137;
        double et1 = 3.3379;
        double et2 = 4.1379;
        double apt = 5.2734337;
        double ap1 = 6.2746537;
        double ap2 = 7.2735467;
        double ap3 = 8.5642737;
        double gas = 9.784352789435;

        payload.setTotal_power_import_t1_kwh(it1);
        payload.setTotal_power_import_t2_kwh(it2);
        payload.setTotal_power_export_t1_kwh(et1);
        payload.setTotal_power_export_t2_kwh(et2);

        payload.setActive_power_w(apt);
        payload.setActive_power_l1_w(ap1);
        payload.setActive_power_l2_w(ap2);
        payload.setActive_power_l3_w(ap3);

        payload.setTotal_gas_m3(gas);

        assertThat(payload.getTotal_power_import_t1_kwh(), is(it1));
        assertThat(payload.getTotal_power_import_t2_kwh(), is(it2));
        assertThat(payload.getTotal_power_export_t1_kwh(), is(et1));
        assertThat(payload.getTotal_power_export_t2_kwh(), is(et2));

        assertThat(payload.getActive_power_w(), is(apt));
        assertThat(payload.getActive_power_l1_w(), is(ap1));
        assertThat(payload.getActive_power_l2_w(), is(ap2));
        assertThat(payload.getActive_power_l3_w(), is(ap3));

        assertThat(payload.getTotal_gas_m3(), is(gas));
    }

    @Test
    public void jsonTest() {
        final GsonBuilder builder = new GsonBuilder();
        final Gson gson = builder.create();
        final double delta = 0.0005;

        int version = 42;
        String model = "Nice Model";
        String ssid = "FreeWifi";
        int rssi = 1337;
        double it1 = 1.337;
        double it2 = 2.137;
        double et1 = 3.3379;
        double et2 = 4.1379;
        double apt = 5.2734337;
        double ap1 = 6.2746537;
        double ap2 = 7.2735467;
        double ap3 = 8.5642737;
        double gas = 9.784352789435;
        long ts = 137137137;

        String json = String.format("{\"smr_version\":%d,\"meter_model\":\"%s\",\"wifi_ssid\":\"%s\","
                + "\"wifi_strength\":%d,\"total_power_import_t1_kwh\":%.3f,"
                + "\"total_power_import_t2_kwh\":%.3f,\"total_power_export_t1_kwh\":%.3f,"
                + "\"total_power_export_t2_kwh\":%.3f,\"active_power_w\":%.3f,\"active_power_l1_w\":%.3f,"
                + "\"active_power_l2_w\":%.3f,\"active_power_l3_w\":%.3f,\"total_gas_m3\":%.3f,\"gas_timestamp\":%d}",
                version, model, ssid, rssi, it1, it2, et1, et2, apt, ap1, ap2, ap3, gas, ts);

        payload = gson.fromJson(json, P1Payload.class);

        assertThat(payload.getMeter_model(), is(model));
        assertThat(payload.getWifi_ssid(), is(ssid));
        assertThat(payload.getSmr_version(), is(version));
        assertThat(payload.getWifi_strength(), is(rssi));
        assertThat(payload.getGas_timestamp(), is(ts));
        assertThat(payload.getTotal_power_import_t1_kwh(), closeTo(it1, delta));
        assertThat(payload.getTotal_power_import_t2_kwh(), closeTo(it2, delta));
        assertThat(payload.getTotal_power_export_t1_kwh(), closeTo(et1, delta));
        assertThat(payload.getTotal_power_export_t2_kwh(), closeTo(et2, delta));
        assertThat(payload.getActive_power_w(), closeTo(apt, delta));
        assertThat(payload.getActive_power_l1_w(), closeTo(ap1, delta));
        assertThat(payload.getActive_power_l2_w(), closeTo(ap2, delta));
        assertThat(payload.getActive_power_l3_w(), closeTo(ap3, delta));
        assertThat(payload.getTotal_gas_m3(), closeTo(gas, delta));
    }
}
