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
package org.openhab.binding.restify.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.restify.internal.endpoint.RegistrationException;
import org.openhab.binding.restify.internal.handler.EndpointConfiguration.AuthorizationType;
import org.openhab.binding.restify.internal.servlet.Authorization;
import org.openhab.binding.restify.internal.servlet.DispatcherServlet;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class EndpointHandlerTest {
    @Mock
    private Thing thing;

    @Mock
    private DispatcherServlet dispatcherServlet;

    @Mock
    private ThingHandlerCallback callback;

    @Test
    void initializeRegistersEndpointWhenConfigurationIsValid() throws Exception {
        // Given
        var config = new EndpointConfiguration("/status", DispatcherServlet.Method.GET, "JS:status.js");
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet).register(eq(config.path), eq(DispatcherServlet.Method.GET),
                argThat(endpoint -> endpoint.authorization() == null && endpoint.transformation().isPresent()
                        && endpoint.contentType().equals("application/json")));
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == ONLINE));
    }

    @Test
    void initializeSetsOfflineWhenPathDoesNotStartWithSlash() throws Exception {
        // Given
        var config = new EndpointConfiguration("status", DispatcherServlet.Method.GET, "JS:status.js");
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
    void initializeSetsOfflineWhenPathIsRoot() throws Exception {
        // Given
        var config = new EndpointConfiguration("/", DispatcherServlet.Method.GET, "JS:status.js");
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
    void initializeSetsOfflineWhenPathStartsWithReservedPrefix() throws Exception {
        // Given
        var config = new EndpointConfiguration("/_foo", DispatcherServlet.Method.GET, "JS:status.js");
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
        var config = new EndpointConfiguration("/status", DispatcherServlet.Method.GET, "invalid");
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
        var config = new EndpointConfiguration("/secure", DispatcherServlet.Method.GET, "JS:secure.js");
        config.authorizationType = AuthorizationType.BASIC;
        config.username = "john";
        config.password = "secret";
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet).register(eq(config.path), eq(DispatcherServlet.Method.GET),
                argThat(endpoint -> new Authorization.Basic("john", "secret").equals(endpoint.authorization())));
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == ONLINE));
    }

    @Test
    void initializeSetsOfflineWhenBasicAuthorizationIsIncomplete() throws Exception {
        // Given
        var config = new EndpointConfiguration("/secure", DispatcherServlet.Method.GET, "JS:secure.js");
        config.authorizationType = AuthorizationType.BASIC;
        config.username = "john";
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
        var config = new EndpointConfiguration("/secure", DispatcherServlet.Method.GET, "JS:secure.js");
        config.authorizationType = AuthorizationType.BEARER;
        config.token = "token-123";
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet).register(eq(config.path), eq(DispatcherServlet.Method.GET),
                argThat(endpoint -> new Authorization.Bearer("token-123").equals(endpoint.authorization())));
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == ONLINE));
    }

    @Test
    void initializeSetsOfflineWhenRegistrationFails() throws Exception {
        // Given
        var config = new EndpointConfiguration("/status", DispatcherServlet.Method.GET, "JS:status.js");
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);
        doThrow(new RegistrationException("duplicate")).when(dispatcherServlet).register(eq(config.path),
                eq(DispatcherServlet.Method.GET), any());

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet).register(eq(config.path), eq(DispatcherServlet.Method.GET), any());
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == OFFLINE
                && statusInfo.getStatusDetail() == CONFIGURATION_ERROR));
    }

    @Test
    void disposeUnregistersEndpointWhenInitialized() throws Exception {
        // Given
        var config = new EndpointConfiguration("/status", DispatcherServlet.Method.POST, "JS:status.js");
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);
        handler.setCallback(callback);
        handler.initialize();

        // When
        handler.dispose();

        // Then
        verify(dispatcherServlet).unregister(config.path, DispatcherServlet.Method.POST);
    }

    @Test
    void disposeDoesNotUnregisterWhenNotInitialized() {
        // Given
        var config = new EndpointConfiguration("/status", DispatcherServlet.Method.POST, "JS:status.js");
        var handler = new TestEndpointHandler(config, thing, dispatcherServlet);

        // When
        handler.dispose();

        // Then
        verify(dispatcherServlet, never()).unregister(any(), any());
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
