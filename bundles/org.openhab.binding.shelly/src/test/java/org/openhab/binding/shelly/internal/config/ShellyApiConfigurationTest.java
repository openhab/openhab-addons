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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ShellyApiConfiguration} and its inner classes.
 *
 * The inner classes {@code ShellyAuthCredentials} and {@code ShellyApiUrls} have private
 * fields and no standalone getters, so their behaviour is verified through the
 * {@link ShellyApiConfiguration} public API (getUserId, getPassword, getBearer,
 * getDeviceApiUrl, getWebSocketCallback, getEventCallbackUrl).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyApiConfigurationTest {

    private static final String LOCAL_IP = "192.168.1.100";
    private static final String DEVICE_IP = "192.168.1.50";

    // ── Auth credentials — tested via ShellyApiConfiguration getters ──────────

    @Test
    void credentialsThingValuesOverrideBindingDefaults() throws Exception {
        ShellyThingConfiguration thing = thingConfigWithCredentials("thingUser", "thingPass");
        ShellyApiConfiguration config = new ShellyApiConfiguration(thing, bindingConfig("bindUser", "bindPass"), "",
                false);
        assertThat(config.getUserId(), is("thingUser"));
        assertThat(config.getPassword(), is("thingPass"));
        assertThat(config.getBearer(), is("thingUser:thingPass"));
    }

    @Test
    void credentialsBlankThingValuesFallBackToBindingDefaults() throws Exception {
        ShellyThingConfiguration thing = thingConfigWithCredentials("", "");
        ShellyApiConfiguration config = new ShellyApiConfiguration(thing, bindingConfig("bindUser", "bindPass"), "",
                false);
        assertThat(config.getUserId(), is("bindUser"));
        assertThat(config.getPassword(), is("bindPass"));
        assertThat(config.getBearer(), is("bindUser:bindPass"));
    }

    // ── URL construction — tested via ShellyApiConfiguration getters ──────────

    @Test
    void blankLocalIpThrowsIllegalArgument() {
        // ShellyApiUrls validates localIp is non-blank; the exception propagates from the constructor
        ShellyBindingConfiguration emptyLocalIp = new ShellyBindingConfiguration().withHttpPort(8080);
        assertThrows(IllegalArgumentException.class,
                () -> new ShellyApiConfiguration(emptyLocalIp, "realm", DEVICE_IP));
    }

    @Test
    void urlsAreConstructedCorrectly() {
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig(), "realm", DEVICE_IP);
        assertThat(config.getDeviceApiUrl(), is("http://" + DEVICE_IP));
        assertThat(config.getWebSocketCallback(), is("ws://" + LOCAL_IP + ":8080/shelly/wsevent"));
        assertThat(config.getEventCallbackUrl(), startsWith("http://" + LOCAL_IP + ":8080/shelly/event"));
    }

    // ── Discovery constructor ─────────────────────────────────────────────────

    @Test
    void discoveryConstructorInitialState() {
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig("myUser", "myPass"), "my-shelly",
                DEVICE_IP);
        assertThat(config.getRealm(), is("my-shelly"));
        assertThat(config.getDeviceIp(), is(DEVICE_IP));
        assertThat(config.getUserId(), is("myUser"));
        assertThat(config.getPassword(), is("myPass"));
    }

    @Test
    void discoveryConstructorAllOptionalFeaturesDisabled() {
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig(), "realm", DEVICE_IP);
        assertThat(config.getEnableCoIOT(), is(false));
        assertThat(config.getEnableBluGateway(), is(false));
        assertThat(config.getEnableRangeExtender(), is(false));
        assertThat(config.getEventsButton(), is(false));
        assertThat(config.getEventsSwitch(), is(false));
        assertThat(config.getEventsPush(), is(false));
        assertThat(config.getEventsRoller(), is(false));
        assertThat(config.getEventsSensorReport(), is(false));
    }

    // ── Thing handler constructor ─────────────────────────────────────────────

    @Test
    void thingConstructorGen1HonorsEventsCoIoT() {
        ShellyApiConfiguration config = new ShellyApiConfiguration(thingConfig(DEVICE_IP, true), bindingConfig(), "",
                false);
        assertThat(config.getEnableCoIOT(), is(true));
    }

    @Test
    void thingConstructorGen2ForcesCoIoTDisabled() {
        ShellyApiConfiguration config = new ShellyApiConfiguration(thingConfig(DEVICE_IP, true), bindingConfig(), "",
                true);
        assertThat(config.getEnableCoIOT(), is(false));
    }

    @Test
    void thingConstructorPropagatesAllEventFlags() throws Exception {
        ShellyThingConfiguration thing = new ShellyThingConfiguration();
        setField(thing, "deviceIp", DEVICE_IP);
        setField(thing, "eventsButton", true);
        setField(thing, "eventsSwitch", false);
        setField(thing, "eventsPush", false);
        setField(thing, "eventsRoller", false);
        setField(thing, "eventsSensorReport", false);
        ShellyApiConfiguration config = new ShellyApiConfiguration(thing, bindingConfig(), "", false);
        assertThat(config.getEventsButton(), is(true));
        assertThat(config.getEventsSwitch(), is(false));
        assertThat(config.getEventsPush(), is(false));
        assertThat(config.getEventsRoller(), is(false));
        assertThat(config.getEventsSensorReport(), is(false));
    }

    @Test
    void thingConstructorPropagatesOptionalFeatureFlags() throws Exception {
        ShellyThingConfiguration thing = new ShellyThingConfiguration();
        setField(thing, "deviceIp", DEVICE_IP);
        setField(thing, "enableBluGateway", true);
        setField(thing, "enableRangeExtender", false);
        ShellyApiConfiguration config = new ShellyApiConfiguration(thing, bindingConfig(), "realm", false);
        assertThat(config.getRealm(), is("realm"));
        assertThat(config.getEnableBluGateway(), is(true));
        assertThat(config.getEnableRangeExtender(), is(false));
    }

    @Test
    void thingConstructorBluDeviceAddressNormalized() throws Exception {
        ShellyThingConfiguration thing = new ShellyThingConfiguration();
        setField(thing, "deviceAddress", "BC:02:6E:C3:A6:C7");
        ShellyApiConfiguration config = new ShellyApiConfiguration(thing, bindingConfig(), "", false);
        // MAC address must be lowercased and colons stripped; deviceIp must be empty for BLU devices
        assertThat(config.getDeviceAddress(), is("bc026ec3a6c7"));
        assertThat(config.getDeviceIp(), is(""));
    }

    // ── Mutable state ─────────────────────────────────────────────────────────

    @Test
    void setRealmUpdatesValue() {
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig(), "old-realm", DEVICE_IP);
        config.setRealm("new-realm");
        assertThat(config.getRealm(), is("new-realm"));
        config.setRealm("newer-realm");
        assertThat(config.getRealm(), is("newer-realm"));
    }

    @Test
    void setEnableCoIoTToggles() {
        ShellyApiConfiguration config = new ShellyApiConfiguration(thingConfig(DEVICE_IP, true), bindingConfig(), "",
                false);
        assertThat(config.getEnableCoIOT(), is(true));
        config.setEnableCoIOT(false);
        assertThat(config.getEnableCoIOT(), is(false));
        config.setEnableCoIOT(true);
        assertThat(config.getEnableCoIOT(), is(true));
    }

    @Test
    void setCredentialsUpdatesAllGetters() {
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig(), "realm", DEVICE_IP);
        config.setCredentials("newUser", "newPass");
        assertThat(config.getUserId(), is("newUser"));
        assertThat(config.getPassword(), is("newPass"));
        assertThat(config.getBearer(), is("newUser:newPass"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ShellyBindingConfiguration bindingConfig() {
        return ShellyBindingConfiguration.fromProperties(LOCAL_IP, Map.of()).withHttpPort(8080);
    }

    private ShellyBindingConfiguration bindingConfig(String userId, String password) {
        return ShellyBindingConfiguration
                .fromProperties(LOCAL_IP, Map.of(ShellyBindingConfiguration.CONFIG_DEF_HTTP_USER, userId,
                        ShellyBindingConfiguration.CONFIG_DEF_HTTP_PWD, password))
                .withHttpPort(8080);
    }

    private ShellyThingConfiguration thingConfig(String deviceIp, boolean eventsCoIoT) {
        ShellyThingConfiguration thing = new ShellyThingConfiguration();
        try {
            setField(thing, "deviceIp", deviceIp);
            setField(thing, "eventsCoIoT", eventsCoIoT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return thing;
    }

    private ShellyThingConfiguration thingConfigWithCredentials(String userId, String password) throws Exception {
        ShellyThingConfiguration thing = new ShellyThingConfiguration();
        setField(thing, "deviceIp", DEVICE_IP);
        setField(thing, "userId", userId);
        setField(thing, "password", password);
        return thing;
    }

    private static void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
