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
package org.openhab.binding.salus.internal.handler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.salus.internal.SalusApi;
import org.openhab.binding.salus.internal.cloud.handler.CloudBridgeConfig;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.binding.salus.internal.rest.RestClient;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class AbstractBridgeHandlerTest {
    private static final String SETTER = "ep9:sIT600TH:SetLockKey";
    private final SalusApi api = mock(SalusApi.class);
    private final HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
    private final Bridge bridge = BridgeBuilder.create(new ThingTypeUID("salus", "salus-cloud-bridge"), "test").build();
    private final CloudBridgeConfig config = new CloudBridgeConfig();
    private final AbstractBridgeHandler<CloudBridgeConfig> handler = new AbstractBridgeHandler<>(bridge,
            httpClientFactory, CloudBridgeConfig.class) {
        @Override
        protected SalusApi newSalusApi(CloudBridgeConfig config, RestClient client, GsonMapper mapper) {
            return api;
        }

        @Override
        public Set<String> it600RequiredChannels() {
            return Set.of();
        }

        @Override
        public String channelPrefix() {
            return "ep9";
        }

        @Override
        protected <T> T getConfigAs(Class<T> configurationClass) {
            return configurationClass.cast(config);
        }
    };

    @BeforeEach
    void initialize() throws Exception {
        config.setUsername("test@example.com");
        config.setPassword("test-password");
        config.setRefreshInterval(3600);
        config.setPropertiesRefreshInterval(3600);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(mock(org.eclipse.jetty.client.HttpClient.class));
        when(api.findDevices()).thenReturn(new TreeSet<>());
        when(api.findDeviceProperties("thermostat"))
                .thenReturn(new TreeSet<>(GsonMapper.INSTANCE.parseAwsDeviceProperties("""
                        {"state":{"reported":{"11":{"properties":{"ep9:sIT600TH:LockKey":0}}}}}
                        """)));
        handler.setCallback(mock(ThingHandlerCallback.class));
        handler.initialize();
    }

    @AfterEach
    void dispose() {
        handler.dispose();
    }

    @Test
    void sharesOnePropertyRequestAcrossChannelRefreshes() throws Exception {
        for (int i = 0; i < 19; i++) {
            assertThat(handler.findPropertiesForDevice("thermostat")).hasSize(2);
        }
        verify(api).findDeviceProperties("thermostat");
    }

    @Test
    void invalidatesPropertiesAfterSuccessfulCommand() throws Exception {
        handler.findPropertiesForDevice("thermostat");
        when(api.setValueForProperty("thermostat", SETTER, 1L)).thenReturn(1L);
        assertThat(handler.setValueForProperty("thermostat", SETTER, 1L)).isTrue();
        handler.findPropertiesForDevice("thermostat");
        verify(api, times(2)).findDeviceProperties("thermostat");
    }

    @Test
    void preservesCommunicationFailureAndRetriesNextRead() throws Exception {
        var failure = new SalusApiException("Unavailable");
        when(api.findDeviceProperties("other")).thenThrow(failure).thenReturn(new TreeSet<>());
        assertThatThrownBy(() -> handler.findPropertiesForDevice("other")).isSameAs(failure);
        assertThat(handler.findPropertiesForDevice("other")).isEmpty();
    }

    @Test
    void preservesAuthenticationFailureAndRetriesNextRead() throws Exception {
        var failure = new AuthSalusApiException("Expired");
        when(api.findDeviceProperties("other")).thenThrow(failure).thenReturn(new TreeSet<>());
        assertThatThrownBy(() -> handler.findPropertiesForDevice("other")).isSameAs(failure);
        assertThat(handler.findPropertiesForDevice("other")).isEmpty();
    }

    @Test
    void invalidatesPropertiesAfterFailedCommand() throws Exception {
        handler.findPropertiesForDevice("thermostat");
        when(api.setValueForProperty("thermostat", SETTER, 1L)).thenThrow(new SalusApiException("Unavailable"));
        assertThatThrownBy(() -> handler.setValueForProperty("thermostat", SETTER, 1L))
                .isInstanceOf(SalusApiException.class);
        handler.findPropertiesForDevice("thermostat");
        verify(api, times(2)).findDeviceProperties("thermostat");
    }

    @Test
    void preservesAuthenticationFailureWhenLoadingPropertiesAfterWrite() throws Exception {
        var failure = new AuthSalusApiException("Expired");
        when(api.setValueForProperty("thermostat", SETTER, 1L)).thenReturn(1L);
        when(api.findDeviceProperties("thermostat")).thenThrow(failure);
        assertThatThrownBy(() -> handler.setValueForProperty("thermostat", SETTER, 1L)).isSameAs(failure);
    }
}
