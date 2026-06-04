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
package org.openhab.binding.shelly.internal.config;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ShellyThingConfiguration}.
 *
 * Fields are private and written by the openHAB framework via reflection-based injection.
 * Tests simulate that injection pattern to verify that every getter correctly exposes
 * the value that was set and that all default values match the documented configuration.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyThingConfigurationTest {

    @Test
    void defaultValues() {
        ShellyThingConfiguration config = new ShellyThingConfiguration();
        assertThat(config.getDeviceIp(), is(""));
        assertThat(config.getDeviceAddress(), is(""));
        assertThat(config.getUserId(), is(""));
        assertThat(config.getPassword(), is(""));
        assertThat(config.getUpdateInterval(), is(60));
        assertThat(config.getLowBattery(), is(15));
        assertThat(config.getBrightnessAutoOn(), is(true));
        assertThat(config.getFavoriteUP(), is(0));
        assertThat(config.getFavoriteDOWN(), is(0));
        assertThat(config.getEventsButton(), is(false));
        assertThat(config.getEventsSwitch(), is(true));
        assertThat(config.getEventsPush(), is(true));
        assertThat(config.getEventsRoller(), is(true));
        assertThat(config.getEventsSensorReport(), is(true));
        assertThat(config.getEventsCoIoT(), is(false));
        assertThat(config.getEnableBluGateway(), is(false));
        assertThat(config.getEnableRangeExtender(), is(true));
    }

    @Test
    void injectedDeviceIdentification() throws Exception {
        ShellyThingConfiguration config = new ShellyThingConfiguration();
        setField(config, "deviceIp", "192.168.1.10");
        setField(config, "deviceAddress", "bc026ec3a6c7");
        assertThat(config.getDeviceIp(), is("192.168.1.10"));
        assertThat(config.getDeviceAddress(), is("bc026ec3a6c7"));
    }

    @Test
    void injectedAuthCredentials() throws Exception {
        ShellyThingConfiguration config = new ShellyThingConfiguration();
        setField(config, "userId", "admin");
        setField(config, "password", "s3cr3t");
        assertThat(config.getUserId(), is("admin"));
        assertThat(config.getPassword(), is("s3cr3t"));
    }

    @Test
    void injectedOperationParameters() throws Exception {
        ShellyThingConfiguration config = new ShellyThingConfiguration();
        setField(config, "updateInterval", 30);
        setField(config, "lowBattery", 20);
        setField(config, "brightnessAutoOn", false);
        setField(config, "favoriteUP", 80);
        setField(config, "favoriteDOWN", 20);
        assertThat(config.getUpdateInterval(), is(30));
        assertThat(config.getLowBattery(), is(20));
        assertThat(config.getBrightnessAutoOn(), is(false));
        assertThat(config.getFavoriteUP(), is(80));
        assertThat(config.getFavoriteDOWN(), is(20));
    }

    @Test
    void injectedEventFlags() throws Exception {
        ShellyThingConfiguration config = new ShellyThingConfiguration();
        setField(config, "eventsButton", true);
        setField(config, "eventsSwitch", false);
        setField(config, "eventsPush", false);
        setField(config, "eventsRoller", false);
        setField(config, "eventsSensorReport", false);
        setField(config, "eventsCoIoT", true);
        assertThat(config.getEventsButton(), is(true));
        assertThat(config.getEventsSwitch(), is(false));
        assertThat(config.getEventsPush(), is(false));
        assertThat(config.getEventsRoller(), is(false));
        assertThat(config.getEventsSensorReport(), is(false));
        assertThat(config.getEventsCoIoT(), is(true));
    }

    @Test
    void injectedGen2Features() throws Exception {
        ShellyThingConfiguration config = new ShellyThingConfiguration();
        setField(config, "enableBluGateway", true);
        setField(config, "enableRangeExtender", false);
        assertThat(config.getEnableBluGateway(), is(true));
        assertThat(config.getEnableRangeExtender(), is(false));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
