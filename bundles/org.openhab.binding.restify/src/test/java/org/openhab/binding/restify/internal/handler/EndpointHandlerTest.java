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

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.restify.internal.JsonSchemaValidator;
import org.openhab.binding.restify.internal.endpoint.Endpoint;
import org.openhab.binding.restify.internal.endpoint.EndpointParseException;
import org.openhab.binding.restify.internal.endpoint.EndpointParser;
import org.openhab.binding.restify.internal.endpoint.RegistrationException;
import org.openhab.binding.restify.internal.servlet.DispatcherServlet;
import org.openhab.binding.restify.internal.servlet.Response;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.networknt.schema.Error;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class EndpointHandlerTest {
    @Mock
    private Thing thing;

    @Mock
    private EndpointParser endpointParser;

    @Mock
    private DispatcherServlet dispatcherServlet;

    @Mock
    private JsonSchemaValidator schemaValidator;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Error schemaError;

    @Test
    void initializeRegistersEndpointWhenConfigurationIsValid() throws Exception {
        // Given
        var config = new EndpointConfiguration("/status", DispatcherServlet.Method.GET, "{\"response\":{}}");
        var endpoint = new Endpoint(null, new Response.JsonResponse(Map.of()));
        var handler = new TestEndpointHandler(config, thing, endpointParser, dispatcherServlet, schemaValidator);
        handler.setCallback(callback);
        when(schemaValidator.validateEndpointConfig(config.endpoint)).thenReturn(emptyList());
        when(endpointParser.parseEndpoint(config.endpoint)).thenReturn(endpoint);

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet).register(config.path, config.method, endpoint);
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == ONLINE));
    }

    @Test
    void initializeSetsOfflineWhenPathDoesNotStartWithSlash() throws Exception {
        // Given
        var config = new EndpointConfiguration("status", DispatcherServlet.Method.GET, "{\"response\":{}}");
        var handler = new TestEndpointHandler(config, thing, endpointParser, dispatcherServlet, schemaValidator);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(schemaValidator, never()).validateEndpointConfig(any());
        verify(dispatcherServlet, never()).register(any(), any(), any());
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == OFFLINE
                && statusInfo.getStatusDetail() == CONFIGURATION_ERROR));
    }

    @Test
    void initializeSetsOfflineWhenPathIsRoot() throws Exception {
        // Given
        var config = new EndpointConfiguration("/", DispatcherServlet.Method.GET, "{\"response\":{}}");
        var handler = new TestEndpointHandler(config, thing, endpointParser, dispatcherServlet, schemaValidator);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(schemaValidator, never()).validateEndpointConfig(any());
        verify(dispatcherServlet, never()).register(any(), any(), any());
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == OFFLINE
                && statusInfo.getStatusDetail() == CONFIGURATION_ERROR));
    }

    @Test
    void initializeSetsOfflineWhenPathStartsWithReservedPrefix() throws Exception {
        // Given
        var config = new EndpointConfiguration("/_foo", DispatcherServlet.Method.GET, "{\"response\":{}}");
        var handler = new TestEndpointHandler(config, thing, endpointParser, dispatcherServlet, schemaValidator);
        handler.setCallback(callback);

        // When
        handler.initialize();

        // Then
        verify(schemaValidator, never()).validateEndpointConfig(any());
        verify(dispatcherServlet, never()).register(any(), any(), any());
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == OFFLINE
                && statusInfo.getStatusDetail() == CONFIGURATION_ERROR));
    }

    @Test
    void initializeSetsOfflineWhenSchemaValidationFails() throws Exception {
        // Given
        var config = new EndpointConfiguration("/status", DispatcherServlet.Method.GET, "{\"response\":{}}");
        var handler = new TestEndpointHandler(config, thing, endpointParser, dispatcherServlet, schemaValidator);
        handler.setCallback(callback);
        when(schemaError.getMessage()).thenReturn("response is invalid");
        when(schemaValidator.validateEndpointConfig(config.endpoint)).thenReturn(List.of(schemaError));

        // When
        handler.initialize();

        // Then
        verify(endpointParser, never()).parseEndpoint(any());
        verify(dispatcherServlet, never()).register(any(), any(), any());
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == OFFLINE
                && statusInfo.getStatusDetail() == CONFIGURATION_ERROR));
    }

    @Test
    void initializeSetsOfflineWhenEndpointParsingFails() throws Exception {
        // Given
        var config = new EndpointConfiguration("/status", DispatcherServlet.Method.GET, "{\"response\":{}}");
        var handler = new TestEndpointHandler(config, thing, endpointParser, dispatcherServlet, schemaValidator);
        handler.setCallback(callback);
        when(schemaValidator.validateEndpointConfig(config.endpoint)).thenReturn(emptyList());
        when(endpointParser.parseEndpoint(config.endpoint)).thenThrow(new EndpointParseException("invalid endpoint"));

        // When
        handler.initialize();

        // Then
        verify(dispatcherServlet, never()).register(any(), any(), any());
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == OFFLINE
                && statusInfo.getStatusDetail() == CONFIGURATION_ERROR));
    }

    @Test
    void initializeSetsOfflineWhenRegistrationFails() throws Exception {
        // Given
        var config = new EndpointConfiguration("/status", DispatcherServlet.Method.GET, "{\"response\":{}}");
        var endpoint = new Endpoint(null, new Response.JsonResponse(Map.of()));
        var handler = new TestEndpointHandler(config, thing, endpointParser, dispatcherServlet, schemaValidator);
        handler.setCallback(callback);
        when(schemaValidator.validateEndpointConfig(config.endpoint)).thenReturn(emptyList());
        when(endpointParser.parseEndpoint(config.endpoint)).thenReturn(endpoint);
        org.mockito.Mockito.doThrow(new RegistrationException("duplicate")).when(dispatcherServlet)
                .register(config.path, config.method, endpoint);

        // When
        handler.initialize();

        // Then
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == OFFLINE
                && statusInfo.getStatusDetail() == CONFIGURATION_ERROR));
    }

    @Test
    void disposeUnregistersEndpointWhenInitialized() throws Exception {
        // Given
        var config = new EndpointConfiguration("/status", DispatcherServlet.Method.POST, "{\"response\":{}}");
        var endpoint = new Endpoint(null, new Response.JsonResponse(Map.of()));
        var handler = new TestEndpointHandler(config, thing, endpointParser, dispatcherServlet, schemaValidator);
        handler.setCallback(callback);
        when(schemaValidator.validateEndpointConfig(config.endpoint)).thenReturn(emptyList());
        when(endpointParser.parseEndpoint(config.endpoint)).thenReturn(endpoint);
        handler.initialize();

        // When
        handler.dispose();

        // Then
        verify(dispatcherServlet).unregister(config.path, config.method);
    }

    @Test
    void disposeDoesNotUnregisterWhenNotInitialized() {
        // Given
        var config = new EndpointConfiguration("/status", DispatcherServlet.Method.POST, "{\"response\":{}}");
        var handler = new TestEndpointHandler(config, thing, endpointParser, dispatcherServlet, schemaValidator);

        // When
        handler.dispose();

        // Then
        verify(dispatcherServlet, never()).unregister(any(), any());
    }

    private static class TestEndpointHandler extends EndpointHandler {
        private final EndpointConfiguration endpointConfiguration;

        public TestEndpointHandler(EndpointConfiguration endpointConfiguration, Thing thing,
                EndpointParser endpointParser, DispatcherServlet dispatcherServlet,
                JsonSchemaValidator schemaValidator) {
            super(thing, endpointParser, dispatcherServlet, schemaValidator);
            this.endpointConfiguration = endpointConfiguration;
        }

        @Override
        public <T> T getConfigAs(Class<T> configurationClass) {
            return configurationClass.cast(endpointConfiguration);
        }
    }
}
