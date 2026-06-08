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
package org.openhab.binding.shelly.internal.api2;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Unit tests for {@link ShellyBluApi} — connection state guards.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBluApiTest {

    // ── getStatus() connection guard ─────────────────────────────────────────

    @Test
    void getStatusThrowsWhenNotConnected() throws Exception {
        ShellyBluApi api = buildBluApi();
        ShellyApiException ex = assertThrows(ShellyApiException.class, api::getStatus);
        assertThat(ex.getMessage(), is("offline.status-error-blu-not-connected"));
    }

    @Test
    void getStatusSucceedsWhenConnected() throws Exception {
        ShellyBluApi api = buildBluApi();
        setField(api, "connected", true);
        assertDoesNotThrow(api::getStatus);
    }

    // ── getSensorStatus() connection guard ───────────────────────────────────

    @Test
    void getSensorStatusThrowsWhenNotConnected() throws Exception {
        ShellyBluApi api = buildBluApi();
        ShellyApiException ex = assertThrows(ShellyApiException.class, api::getSensorStatus);
        assertThat(ex.getMessage(), is("offline.status-error-blu-sensor-unavailable"));
    }

    @Test
    void getSensorStatusSucceedsWhenConnected() throws Exception {
        ShellyBluApi api = buildBluApi();
        setField(api, "connected", true);
        assertDoesNotThrow(api::getSensorStatus);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ShellyBluApi buildBluApi() {
        ThingTypeUID thingTypeUID = THING_TYPE_SHELLYBLUBUTTON1;

        Thing ohThing = mock(Thing.class);
        when(ohThing.getThingTypeUID()).thenReturn(thingTypeUID);

        ShellyDeviceProfile deviceProfile = new ShellyDeviceProfile(thingTypeUID);

        HttpClient httpClient = mock(HttpClient.class);
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(httpClient);
        when(thing.getProfile()).thenReturn(deviceProfile);

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080, nullNas());
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-blu", "");

        return new ShellyBluApi("test-blu", mock(ShellyThingTable.class), thing, config, mock(WebSocketClient.class),
                mock(ScheduledExecutorService.class));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
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
}
