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
 * Tests for {@link ShellyBindingConfiguration}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBindingConfigurationTest {

    // ── Default constructor ───────────────────────────────────────────────────

    @Test
    void defaultConstructorValues() {
        ShellyBindingConfiguration config = new ShellyBindingConfiguration();
        assertThat(config.getDefaultUserId(), is("admin"));
        assertThat(config.getDefaultPassword(), is(""));
        assertThat(config.getLocalIP(), is(""));
        assertThat(config.getHttpPort(), is(DEFAULT_LOCAL_PORT));
        assertThat(config.isAutoCoIoT(), is(true));
    }

    // ── NetworkAddressService constructor ─────────────────────────────────────

    @Test
    void networkAddressServiceValidIpIsUsed() {
        ShellyBindingConfiguration config = new ShellyBindingConfiguration(networkAddressService("10.0.0.1"));
        assertThat(config.getLocalIP(), is("10.0.0.1"));
    }

    @Test
    void networkAddressServiceLinkLocalIpIsFilteredToEmpty() {
        // 169.254.x.x (link-local) addresses are unreachable from devices; must be discarded
        ShellyBindingConfiguration config = new ShellyBindingConfiguration(networkAddressService("169.254.1.1"));
        assertThat(config.getLocalIP(), is(""));
    }

    @Test
    void networkAddressServiceNullIpBecomesEmpty() {
        ShellyBindingConfiguration config = new ShellyBindingConfiguration(networkAddressService(null));
        assertThat(config.getLocalIP(), is(""));
    }

    // ── withHttpPort ──────────────────────────────────────────────────────────

    @Test
    void withHttpPortCreatesNewInstanceWithPort() {
        ShellyBindingConfiguration original = new ShellyBindingConfiguration();
        ShellyBindingConfiguration updated = original.withHttpPort(9090);
        assertThat(updated, is(not(sameInstance(original))));
        assertThat(updated.getHttpPort(), is(9090));
    }

    @Test
    void withHttpPortPreservesOtherFields() {
        ShellyBindingConfiguration config = ShellyBindingConfiguration
                .fromProperties("192.168.1.1", Map.of(CONFIG_DEF_HTTP_USER, "user1", CONFIG_DEF_HTTP_PWD, "pass1"))
                .withHttpPort(9090);
        assertThat(config.getDefaultUserId(), is("user1"));
        assertThat(config.getDefaultPassword(), is("pass1"));
        assertThat(config.getLocalIP(), is("192.168.1.1"));
    }

    @Test
    void withHttpPortMinusOneReturnsDefaultPort() {
        ShellyBindingConfiguration config = new ShellyBindingConfiguration().withHttpPort(9090).withHttpPort(-1);
        assertThat(config.getHttpPort(), is(DEFAULT_LOCAL_PORT));
    }

    // ── fromProperties(Map) ───────────────────────────────────────────────────

    @Test
    void fromPropertiesOverridesCredentials() {
        ShellyBindingConfiguration config = ShellyBindingConfiguration.fromProperties("",
                Map.of(CONFIG_DEF_HTTP_USER, "myuser", CONFIG_DEF_HTTP_PWD, "secret"));
        assertThat(config.getDefaultUserId(), is("myuser"));
        assertThat(config.getDefaultPassword(), is("secret"));
    }

    @Test
    void fromPropertiesLocalIpOverridesThenParameter() {
        // Property overrides the localIP parameter
        ShellyBindingConfiguration override = ShellyBindingConfiguration.fromProperties("10.0.0.1",
                Map.of(CONFIG_LOCAL_IP, "192.168.0.50"));
        assertThat(override.getLocalIP(), is("192.168.0.50"));

        // Without override, the parameter value is used
        ShellyBindingConfiguration fromParam = ShellyBindingConfiguration.fromProperties("10.0.0.1", Map.of());
        assertThat(fromParam.getLocalIP(), is("10.0.0.1"));
    }

    @ParameterizedTest
    @MethodSource("autoCoIoTCases")
    void fromPropertiesAutoCoIoTParsing(Object value, boolean expected) {
        ShellyBindingConfiguration config = ShellyBindingConfiguration.fromProperties("",
                Map.of(CONFIG_AUTOCOIOT, value));
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
        ShellyBindingConfiguration config = ShellyBindingConfiguration.fromProperties("",
                Map.of("unknownKey", "value"));
        assertThat(config.getDefaultUserId(), is("admin"));
        assertThat(config.getDefaultPassword(), is(""));
        assertThat(config.isAutoCoIoT(), is(true));
    }

    // ── fromProperties(Dictionary) ────────────────────────────────────────────

    @Test
    void fromPropertiesNullDictionaryPreservesLocalIpAndDefaults() {
        ShellyBindingConfiguration config = ShellyBindingConfiguration.fromProperties("10.0.0.2",
                (Dictionary<String, Object>) null);
        assertThat(config.getLocalIP(), is("10.0.0.2"));
        assertThat(config.getDefaultUserId(), is("admin"));
        assertThat(config.getDefaultPassword(), is(""));
        assertThat(config.isAutoCoIoT(), is(true));
    }

    @Test
    void fromPropertiesNullDictionaryAndMapYieldSameDefaults() {
        // null Dictionary must produce identical defaults to an empty Map — no "admin" password leak
        ShellyBindingConfiguration fromNull = ShellyBindingConfiguration.fromProperties("10.0.0.3",
                (Dictionary<String, Object>) null);
        ShellyBindingConfiguration fromEmpty = ShellyBindingConfiguration.fromProperties("10.0.0.3", Map.of());
        assertThat(fromNull.getDefaultUserId(), is(fromEmpty.getDefaultUserId()));
        assertThat(fromNull.getDefaultPassword(), is(fromEmpty.getDefaultPassword()));
        assertThat(fromNull.isAutoCoIoT(), is(fromEmpty.isAutoCoIoT()));
    }

    @Test
    void fromPropertiesDictionaryAppliesOverrides() {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put(CONFIG_DEF_HTTP_USER, "dictuser");
        dict.put(CONFIG_DEF_HTTP_PWD, "dictpass");
        dict.put(CONFIG_AUTOCOIOT, "false");
        ShellyBindingConfiguration config = ShellyBindingConfiguration.fromProperties("192.168.1.1", dict);
        assertThat(config.getDefaultUserId(), is("dictuser"));
        assertThat(config.getDefaultPassword(), is("dictpass"));
        assertThat(config.getLocalIP(), is("192.168.1.1"));
        assertThat(config.isAutoCoIoT(), is(false));
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
