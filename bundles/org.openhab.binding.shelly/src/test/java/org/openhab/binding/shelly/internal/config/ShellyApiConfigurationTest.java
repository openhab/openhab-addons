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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;

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

    @Test
    void urlsAreConstructedCorrectly() {
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig(), "realm", DEVICE_IP);
        assertThat(config.getDeviceApiUrl(), is("http://" + DEVICE_IP));
        assertThat(config.getWebSocketCallback(), is("ws://" + LOCAL_IP + ":8080/shelly/wsevent"));
        assertThat(config.getEventCallbackUrl(), startsWith("http://" + LOCAL_IP + ":8080/shelly/event"));
    }

    @Test
    void discoveryConstructorInitialState() {
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig("myUser", "myPass"), "my-shelly",
                DEVICE_IP);
        assertThat(config.getRealm(), is("my-shelly"));
        assertThat(config.getDeviceHostAddress(), is(DEVICE_IP));
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
        setField(thing, "eventsSwitch", true);
        setField(thing, "eventsPush", false);
        setField(thing, "eventsRoller", false);
        setField(thing, "eventsSensorReport", false);
        ShellyApiConfiguration config = new ShellyApiConfiguration(thing, bindingConfig(), "", false);
        assertThat(config.getEventsButton(), is(true));
        assertThat(config.getEventsSwitch(), is(true));
        assertThat(config.getEventsPush(), is(false));
        assertThat(config.getEventsRoller(), is(false));
        assertThat(config.getEventsSensorReport(), is(false));
    }

    @Test
    void thingConstructorPropagatesRangeExtender() throws Exception {
        ShellyThingConfiguration thing = new ShellyThingConfiguration();
        setField(thing, "deviceIp", DEVICE_IP + ":10000");
        setField(thing, "enableRangeExtender", true);
        ShellyApiConfiguration config = new ShellyApiConfiguration(thing, bindingConfig(), "shelly-range", false);
        assertThat(config.getRealm(), is("shelly-range"));
        assertThat(config.getEnableRangeExtender(), is(true));
        InetSocketAddress address = config.getDeviceSocketAddress();
        String deviceIp = address != null && address.getAddress() != null ? address.getAddress().getHostAddress() : "";
        assertThat(deviceIp, is(DEVICE_IP));
        int port = address != null ? address.getPort() : -1;
        assertThat(port, is(10000));
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
        assertThat(config.getBdAddr(), is("bc026ec3a6c7"));
        assertThat(config.getDeviceHostAddress(), is(""));
    }

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

    @Test
    void refreshLocalIpNoopWhenOverrideConfigured() {
        // bindingConfig() always sets the CONFIG_LOCAL_IP override
        ShellyBindingRuntimeConfig runtime = bindingConfig();
        ShellyApiConfiguration config = new ShellyApiConfiguration(runtime, "realm", DEVICE_IP);
        config.refreshLocalIp(runtime);
        assertThat(config.getLocalIp(), is(LOCAL_IP));
    }

    @Test
    void refreshLocalIpNoopWhenDeviceIpUnresolved() throws Exception {
        ShellyBindingRuntimeConfig runtime = runtimeConfigNoOverride("10.0.0.1");
        ShellyApiConfiguration config = new ShellyApiConfiguration(thingConfig("", true), runtime, "realm", false,
                false);
        config.refreshLocalIp(runtime);
        assertThat(config.getLocalIp(), is("10.0.0.1"));
    }

    @Test
    void refreshLocalIpFallsBackToGlobalWhenNoSameSubnetMatch() {
        // 203.0.113.0/24 is RFC 5737 TEST-NET-3, guaranteed not to match any real local interface
        ShellyBindingRuntimeConfig runtime = runtimeConfigNoOverride("10.0.0.1");
        ShellyApiConfiguration config = new ShellyApiConfiguration(runtime, "realm", "203.0.113.5");
        config.refreshLocalIp(runtime);
        assertThat(config.getLocalIp(), is("10.0.0.1"));
    }

    @Test
    void refreshLocalIpPrefersSameSubnetMatch() throws Exception {
        String maybeRealIp = findNonLoopbackIPv4Address();
        Assumptions.assumeTrue(maybeRealIp != null, "No non-loopback IPv4 interface found on this machine");
        String realIp = Objects.requireNonNull(maybeRealIp);
        // 192.0.2.0/24 is RFC 5737 TEST-NET-1, guaranteed not to match any real local interface
        ShellyBindingRuntimeConfig runtime = runtimeConfigNoOverride("192.0.2.1");
        ShellyApiConfiguration config = new ShellyApiConfiguration(runtime, "realm", realIp);
        config.refreshLocalIp(runtime);
        assertThat(config.getLocalIp(), is(realIp));
    }

    private static @Nullable String findNonLoopbackIPv4Address() throws SocketException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) {
                continue;
            }
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr.getHostAddress();
                }
            }
        }
        return null;
    }

    private ShellyBindingRuntimeConfig runtimeConfigNoOverride(String nasIp) {
        return new ShellyBindingRuntimeConfig(new ShellyBindingConfiguration(), 8080, networkAddressService(nasIp));
    }

    private static NetworkAddressService networkAddressService(@Nullable String ip) {
        return new NetworkAddressService() {
            @Override
            public @Nullable String getPrimaryIpv4HostAddress() {
                return ip;
            }

            @Override
            public @Nullable String getConfiguredBroadcastAddress() {
                return null;
            }

            @Override
            public boolean isUseOnlyOneAddress() {
                return false;
            }

            @Override
            public boolean isUseIPv6() {
                return false;
            }

            @Override
            public void addNetworkAddressChangeListener(NetworkAddressChangeListener listener) {
            }

            @Override
            public void removeNetworkAddressChangeListener(NetworkAddressChangeListener listener) {
            }
        };
    }

    private ShellyBindingRuntimeConfig bindingConfig() {
        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, LOCAL_IP));
        return new ShellyBindingRuntimeConfig(raw, 8080, nullNas());
    }

    private ShellyBindingRuntimeConfig bindingConfig(String userId, String password) {
        ShellyBindingConfiguration raw = ShellyBindingConfiguration.fromProperties(Map.of(
                ShellyBindingConfiguration.CONFIG_LOCAL_IP, LOCAL_IP, ShellyBindingConfiguration.CONFIG_DEF_HTTP_USER,
                userId, ShellyBindingConfiguration.CONFIG_DEF_HTTP_PWD, password));
        return new ShellyBindingRuntimeConfig(raw, 8080, nullNas());
    }

    private static NetworkAddressService nullNas() {
        return new NetworkAddressService() {
            @Override
            public @Nullable String getPrimaryIpv4HostAddress() {
                return null;
            }

            @Override
            public @Nullable String getConfiguredBroadcastAddress() {
                return null;
            }

            @Override
            public boolean isUseOnlyOneAddress() {
                return false;
            }

            @Override
            public boolean isUseIPv6() {
                return false;
            }

            @Override
            public void addNetworkAddressChangeListener(NetworkAddressChangeListener listener) {
            }

            @Override
            public void removeNetworkAddressChangeListener(NetworkAddressChangeListener listener) {
            }
        };
    }

    private ShellyThingConfiguration thingConfig(String deviceIp, boolean eventsCoIoT) {
        ShellyThingConfiguration thing = new ShellyThingConfiguration();
        try {
            setField(thing, "deviceIp", deviceIp);
            setField(thing, "eventsCoIoT", eventsCoIoT);
        } catch (Exception e) {
            throw new AssertionError(e);
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
