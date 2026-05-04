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
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.DEFAULT_LOCAL_PORT;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLY2_DEFAULT_PASSWORD;
import static org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration.*;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;

/**
 * Tests for {@link ShellyBindingConfiguration} and {@link ShellyBindingRuntimeConfig}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBindingConfigurationTest {

    // ── ShellyBindingConfiguration: default constructor ───────────────────────

    @Test
    void defaultConstructorValues() {
        ShellyBindingConfiguration config = new ShellyBindingConfiguration();
        assertThat(config.getDefaultUserId(), is("admin"));
        assertThat(config.getDefaultPassword(), is(SHELLY2_DEFAULT_PASSWORD));
        assertThat(config.getLocalIP(), is(""));
        assertThat(config.isAutoCoIoT(), is(true));
    }

    // ── ShellyBindingConfiguration: fromProperties(Map) ───────────────────────

    @Test
    void fromPropertiesOverridesCredentials() {
        ShellyBindingConfiguration config = ShellyBindingConfiguration
                .fromProperties(Map.of(CONFIG_DEF_HTTP_USER, "myuser", CONFIG_DEF_HTTP_PWD, "secret"));
        assertThat(config.getDefaultUserId(), is("myuser"));
        assertThat(config.getDefaultPassword(), is("secret"));
    }

    @Test
    void fromPropertiesLocalIpOverride() {
        ShellyBindingConfiguration config = ShellyBindingConfiguration
                .fromProperties(Map.of(CONFIG_LOCAL_IP, "192.168.0.50"));
        assertThat(config.getLocalIP(), is("192.168.0.50"));
    }

    @Test
    void fromPropertiesLocalIpBlankPreservesDefault() {
        ShellyBindingConfiguration config = ShellyBindingConfiguration.fromProperties(Map.of());
        assertThat(config.getLocalIP(), is(""));
    }

    @ParameterizedTest
    @MethodSource("autoCoIoTCases")
    void fromPropertiesAutoCoIoTParsing(Object value, boolean expected) {
        ShellyBindingConfiguration config = ShellyBindingConfiguration.fromProperties(Map.of(CONFIG_AUTOCOIOT, value));
        assertThat(config.isAutoCoIoT(), is(expected));
    }

    private static Stream<Arguments> autoCoIoTCases() {
        return Stream.of( //
                Arguments.of(false, false), //
                Arguments.of("false", false), //
                Arguments.of("true", true), //
                Arguments.of("TRUE", true) //
        );
    }

    @Test
    void fromPropertiesIgnoresUnknownKeys() {
        ShellyBindingConfiguration config = ShellyBindingConfiguration.fromProperties(Map.of("unknownKey", "value"));
        assertThat(config.getDefaultUserId(), is("admin"));
        assertThat(config.getDefaultPassword(), is(SHELLY2_DEFAULT_PASSWORD));
        assertThat(config.isAutoCoIoT(), is(true));
    }

    @Test
    void fromPropertiesNullMapPreservesDefaults() {
        ShellyBindingConfiguration config = ShellyBindingConfiguration.fromProperties((Map<String, Object>) null);
        assertThat(config.getDefaultUserId(), is("admin"));
        assertThat(config.getDefaultPassword(), is(SHELLY2_DEFAULT_PASSWORD));
        assertThat(config.isAutoCoIoT(), is(true));
    }

    // ── ShellyBindingConfiguration: fromProperties(Dictionary) ───────────────

    @Test
    void fromPropertiesNullDictionaryPreservesDefaults() {
        ShellyBindingConfiguration config = ShellyBindingConfiguration
                .fromProperties((Dictionary<String, Object>) null);
        assertThat(config.getDefaultUserId(), is("admin"));
        assertThat(config.getDefaultPassword(), is(SHELLY2_DEFAULT_PASSWORD));
        assertThat(config.isAutoCoIoT(), is(true));
    }

    @Test
    void fromPropertiesDictionaryAppliesOverrides() {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put(CONFIG_DEF_HTTP_USER, "dictuser");
        dict.put(CONFIG_DEF_HTTP_PWD, "dictpass");
        dict.put(CONFIG_AUTOCOIOT, "false");
        ShellyBindingConfiguration config = ShellyBindingConfiguration.fromProperties(dict);
        assertThat(config.getDefaultUserId(), is("dictuser"));
        assertThat(config.getDefaultPassword(), is("dictpass"));
        assertThat(config.isAutoCoIoT(), is(false));
    }

    // ── ShellyBindingRuntimeConfig: IP resolution ─────────────────────────────

    @Test
    void runtimeConfigUsesNasIpWhenConfigLocalIpIsBlank() {
        ShellyBindingConfiguration raw = new ShellyBindingConfiguration();
        ShellyBindingRuntimeConfig runtime = new ShellyBindingRuntimeConfig(raw, networkAddressService("10.0.0.1"));
        assertThat(runtime.getLocalIP(), is("10.0.0.1"));
    }

    @Test
    void runtimeConfigConfigLocalIpWinsOverNas() {
        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(CONFIG_LOCAL_IP, "192.168.1.5"));
        ShellyBindingRuntimeConfig runtime = new ShellyBindingRuntimeConfig(raw, networkAddressService("10.0.0.1"));
        assertThat(runtime.getLocalIP(), is("192.168.1.5"));
    }

    @Test
    void runtimeConfigLinkLocalIpFromNasBecomesEmpty() {
        ShellyBindingConfiguration raw = new ShellyBindingConfiguration();
        ShellyBindingRuntimeConfig runtime = new ShellyBindingRuntimeConfig(raw, networkAddressService("169.254.1.1"));
        assertThat(runtime.getLocalIP(), is(""));
    }

    @Test
    void runtimeConfigNullNasIpBecomesEmpty() {
        ShellyBindingConfiguration raw = new ShellyBindingConfiguration();
        ShellyBindingRuntimeConfig runtime = new ShellyBindingRuntimeConfig(raw, networkAddressService(null));
        assertThat(runtime.getLocalIP(), is(""));
    }

    // ── ShellyBindingRuntimeConfig: withHttpPort ──────────────────────────────

    @Test
    void withHttpPortCreatesNewInstanceWithPort() {
        ShellyBindingRuntimeConfig original = new ShellyBindingRuntimeConfig(new ShellyBindingConfiguration(),
                networkAddressService("10.0.0.1"));
        ShellyBindingRuntimeConfig updated = original.withHttpPort(9090);
        assertThat(updated, is(not(sameInstance(original))));
        assertThat(updated.getHttpPort(), is(9090));
    }

    @Test
    void withHttpPortPreservesOtherFields() {
        ShellyBindingConfiguration raw = ShellyBindingConfiguration.fromProperties(
                Map.of(CONFIG_DEF_HTTP_USER, "user1", CONFIG_DEF_HTTP_PWD, "pass1", CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig runtime = new ShellyBindingRuntimeConfig(raw, networkAddressService(null))
                .withHttpPort(9090);
        assertThat(runtime.getDefaultUserId(), is("user1"));
        assertThat(runtime.getDefaultPassword(), is("pass1"));
        assertThat(runtime.getLocalIP(), is("192.168.1.1"));
        assertThat(runtime.getHttpPort(), is(9090));
    }

    @Test
    void withHttpPortMinusOneReturnsDefaultPort() {
        ShellyBindingRuntimeConfig runtime = new ShellyBindingRuntimeConfig(new ShellyBindingConfiguration(),
                networkAddressService(null)).withHttpPort(9090).withHttpPort(-1);
        assertThat(runtime.getHttpPort(), is(DEFAULT_LOCAL_PORT));
    }

    @Test
    void defaultHttpPortIsDefaultLocalPort() {
        ShellyBindingRuntimeConfig runtime = new ShellyBindingRuntimeConfig(new ShellyBindingConfiguration(),
                networkAddressService(null));
        assertThat(runtime.getHttpPort(), is(DEFAULT_LOCAL_PORT));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
}
