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
package org.openhab.binding.httphook.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.httphook.internal.HttpHookBindingConstants.CHANNEL_TYPE_RESPONSE;
import static org.openhab.binding.httphook.internal.HttpHookBindingConstants.THING_TYPE_ENDPOINT;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.httphook.internal.endpoint.RegistrationException;
import org.openhab.binding.httphook.internal.handler.EndpointConfiguration.AuthorizationType;
import org.openhab.binding.httphook.internal.servlet.Authorization;
import org.openhab.binding.httphook.internal.servlet.DispatcherServlet;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class EndpointHandlerTest {
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_ENDPOINT, "status");

    @Mock
    private DispatcherServlet dispatcherServlet;

    @Mock
    private ThingHandlerCallback callback;

    @Test
    void initializeRegistersEndpointWhenConfigurationIsValid() throws Exception {
        // Given
        var config = new EndpointConfiguration();
        var thing = thingWithChannels(responseChannel("response", DispatcherServlet.Method.GET, "JS:status.js"));
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet).register(eq("/status/response"), eq(DispatcherServlet.Method.GET),
                argThat(endpoint -> endpoint.authorization() == null && endpoint.transformation().isPresent()
                        && endpoint.contentType().equals("application/json")));
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == ONLINE));
    }

    @Test
    void initializeRegistersAllResponseChannels() throws Exception {
        // Given
        var config = new EndpointConfiguration();
        var thing = thingWithChannels(responseChannel("summary", DispatcherServlet.Method.GET, "JS:summary.js"),
                responseChannel("forecast", DispatcherServlet.Method.POST, "JS:forecast.js"));
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet).register(eq("/status/summary"), eq(DispatcherServlet.Method.GET), any());
        verify(dispatcherServlet).register(eq("/status/forecast"), eq(DispatcherServlet.Method.POST), any());
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == ONLINE));
    }

    @Test
    void initializeSetsOfflineWhenNoResponseChannelExists() throws Exception {
        // Given
        var config = new EndpointConfiguration();
        var thing = thingWithChannels();
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet, never()).register(any(), any(), any());
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == OFFLINE
                && statusInfo.getStatusDetail() == CONFIGURATION_ERROR));
    }

    @Test
    void initializeSetsOfflineWhenTransformationPatternIsInvalid() throws Exception {
        // Given
        var config = new EndpointConfiguration();
        var thing = thingWithChannels(responseChannel("response", DispatcherServlet.Method.GET, "invalid"));
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet, never()).register(any(), any(), any());
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == OFFLINE
                && statusInfo.getStatusDetail() == CONFIGURATION_ERROR));
    }

    @Test
    void initializeRegistersBasicAuthorizationWhenConfigured() throws Exception {
        // Given
        var config = new EndpointConfiguration();
        config.authorizationType = AuthorizationType.BASIC;
        config.username = "john";
        config.password = "secret";
        var thing = thingWithChannels(responseChannel("secure", DispatcherServlet.Method.GET, "JS:secure.js"));
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet).register(eq("/status/secure"), eq(DispatcherServlet.Method.GET),
                argThat(endpoint -> new Authorization.Basic("john", "secret").equals(endpoint.authorization())));
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == ONLINE));
    }

    @Test
    void initializeSetsOfflineWhenBasicAuthorizationIsIncomplete() throws Exception {
        // Given
        var config = new EndpointConfiguration();
        config.authorizationType = AuthorizationType.BASIC;
        config.username = "john";
        var thing = thingWithChannels(responseChannel("secure", DispatcherServlet.Method.GET, "JS:secure.js"));
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet, never()).register(any(), any(), any());
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == OFFLINE
                && statusInfo.getStatusDetail() == CONFIGURATION_ERROR));
    }

    @Test
    void initializeRegistersBearerAuthorizationWhenConfigured() throws Exception {
        // Given
        var config = new EndpointConfiguration();
        config.authorizationType = AuthorizationType.BEARER;
        config.token = "token-123";
        var thing = thingWithChannels(responseChannel("secure", DispatcherServlet.Method.GET, "JS:secure.js"));
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet).register(eq("/status/secure"), eq(DispatcherServlet.Method.GET),
                argThat(endpoint -> new Authorization.Bearer("token-123").equals(endpoint.authorization())));
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == ONLINE));
    }

    @Test
    void initializeSetsOfflineWhenRegistrationFails() throws Exception {
        // Given
        var config = new EndpointConfiguration();
        var thing = thingWithChannels(responseChannel("response", DispatcherServlet.Method.GET, "JS:status.js"));
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);
        doThrow(new RegistrationException("duplicate")).when(dispatcherServlet).register(eq("/status/response"),
                eq(DispatcherServlet.Method.GET), any());

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet).register(eq("/status/response"), eq(DispatcherServlet.Method.GET), any());
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == OFFLINE
                && statusInfo.getStatusDetail() == CONFIGURATION_ERROR));
    }

    @Test
    void disposeUnregistersEndpointsWhenInitialized() throws Exception {
        // Given
        var config = new EndpointConfiguration();
        var thing = thingWithChannels(responseChannel("status", DispatcherServlet.Method.POST, "JS:status.js"),
                responseChannel("weather", DispatcherServlet.Method.GET, "JS:weather.js"));
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);
        handler.initialize();

        // When
        handler.dispose();

        // Then
        verify(dispatcherServlet).unregister("/status/status", DispatcherServlet.Method.POST);
        verify(dispatcherServlet).unregister("/status/weather", DispatcherServlet.Method.GET);
    }

    @Test
    void disposeDoesNotUnregisterWhenNotInitialized() {
        // Given
        var config = new EndpointConfiguration();
        var thing = thingWithChannels(responseChannel("status", DispatcherServlet.Method.POST, "JS:status.js"));
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);

        // When
        handler.dispose();

        // Then
        verify(dispatcherServlet, never()).unregister(any(), any());
    }

    private static Thing thingWithChannels(Channel... channels) {
        return ThingBuilder.create(THING_TYPE_ENDPOINT, THING_UID).withChannels(List.of(channels)).build();
    }

    private static Channel responseChannel(String id, DispatcherServlet.Method method, String transformationPattern) {
        return responseChannel(id, method, transformationPattern, "application/json");
    }

    private static Channel responseChannel(String id, DispatcherServlet.Method method, String transformationPattern,
            String contentType) {
        var configuration = new Configuration(Map.of("method", method.name(), "transformationPattern",
                List.of(transformationPattern), "contentType", contentType));
        return ChannelBuilder.create(new ChannelUID(THING_UID, id), "String").withType(CHANNEL_TYPE_RESPONSE)
                .withConfiguration(configuration).build();
    }

    private static class TestEndpointHandler extends EndpointHandler {
        private final EndpointConfiguration endpointConfiguration;

        public TestEndpointHandler(EndpointConfiguration endpointConfiguration, Thing thing,
                DispatcherServlet dispatcherServlet) {
            super(thing, dispatcherServlet);
            this.endpointConfiguration = endpointConfiguration;
        }

        @Override
        public <T> T getConfigAs(Class<T> configurationClass) {
            return configurationClass.cast(endpointConfiguration);
        }
    }
}
