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
package org.openhab.binding.restify.internal.servlet;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.restify.internal.servlet.DispatcherServlet.Method.GET;
import static org.openhab.binding.restify.internal.servlet.DispatcherServlet.Method.POST;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.restify.internal.RestifyBinding;
import org.openhab.binding.restify.internal.RestifyBindingConfig;
import org.openhab.binding.restify.internal.endpoint.Endpoint;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.binding.generic.ChannelTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@Timeout(value = 3, unit = SECONDS)
class DispatcherServletIntTest {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServletIntTest.class);
    private static DispatcherServlet servlet;

    private static RestifyBinding restifyBinding;

    private static Server server;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void beforeAll() throws Exception {
        logger.info("Starting DispatcherServletIntTest Jetty server");
        TranslationProvider i18nProvider = mock();
        restifyBinding = mock();

        lenient().when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
        var authorizationService = new AuthorizationService(restifyBinding);
        servlet = new DispatcherServlet(i18nProvider, authorizationService);

        server = new Server(0);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(servlet), "/restify/*");

        server.setHandler(context);
        server.start();
        logger.info("Started DispatcherServletIntTest Jetty server at {}", server.getURI());
    }

    @AfterAll
    static void afterAll() throws Exception {
        logger.info("Stopping DispatcherServletIntTest Jetty server");
        if (server == null) {
            return;
        }
        server.stop();
        server = null;
        logger.info("Stopped DispatcherServletIntTest Jetty server");
    }

    @Test
    @DisplayName("returns transformation output for registered endpoint")
    void returnsTransformationOutputForRegisteredEndpoint() throws Exception {
        servlet.register("/foo", GET, endpoint("{\"foo\":\"boo\"}"));

        var response = sendGet("/restify/foo");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"foo\":\"boo\"}");
        assertThat(response.headers().firstValue("Content-Type"))
                .hasValueSatisfying(contentType -> assertThat(contentType).startsWith("application/json"));
    }

    @Test
    @DisplayName("returns configured content type")
    void returnsConfiguredContentType() throws Exception {
        servlet.register("/text", GET, endpoint(null, "plain response", "text/plain"));

        var response = sendGet("/restify/text");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("plain response");
        assertThat(response.headers().firstValue("Content-Type"))
                .hasValueSatisfying(contentType -> assertThat(contentType).startsWith("text/plain"));
    }

    @Test
    @DisplayName("passes request metadata JSON to transformation")
    void passesRequestMetadataJsonToTransformation() throws Exception {
        var transformation = transformation("ok");
        servlet.register("/metadata", POST, new Endpoint(null, transformation, "text/plain"));

        var request = HttpRequest.newBuilder().uri(server.getURI().resolve("/restify/metadata?unit=test"))
                .header("X-Test", "abc").POST(HttpRequest.BodyPublishers.ofString("payload")).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(transformation).apply(captor.capture());
        assertThat(captor.getValue()).contains("\"method\":\"POST\"", "\"path\":\"/metadata\"",
                "\"queryString\":\"unit=test\"", "\"body\":\"payload\"", "X-Test", "abc");
    }

    @Test
    @DisplayName("returns 500 when transformation returns no output")
    void returnsServerErrorWhenTransformationReturnsNoOutput() throws Exception {
        var transformation = mock(ChannelTransformation.class);
        when(transformation.apply(anyString())).thenReturn(Optional.empty());
        servlet.register("/empty", GET, new Endpoint(null, transformation, "application/json"));

        var response = sendGet("/restify/empty");

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.body()).contains("\"code\":500").contains("servlet.error.transformation.failed");
    }

    @Test
    @DisplayName("returns 404 for unknown endpoint")
    void returnsNotFoundForUnknownEndpoint() throws Exception {
        var response = sendGet("/restify/unknown");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("\"code\":404").contains("servlet.error.not-found");
    }

    @Test
    @DisplayName("returns 401 when endpoint requires authorization and header is missing")
    void returnsUnauthorizedWhenAuthorizationHeaderIsMissing() throws Exception {
        servlet.register("/secure", GET,
                endpoint(new Authorization.Basic("john", "secret"), "{\"ok\":true}", "application/json"));

        var response = sendGet("/restify/secure");

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("\"code\":401").contains("servlet.error.authorization.required");
    }

    @Test
    @DisplayName("returns payload when basic authorization header is valid")
    void returnsPayloadWhenBasicAuthorizationHeaderIsValid() throws Exception {
        servlet.register("/secure-valid", GET,
                endpoint(new Authorization.Basic("john", "secret"), "{\"ok\":true}", "application/json"));
        var credentials = Base64.getEncoder().encodeToString("john:secret".getBytes(StandardCharsets.UTF_8));

        var request = HttpRequest.newBuilder().uri(server.getURI().resolve("/restify/secure-valid"))
                .header("Authorization", "Basic " + credentials).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"ok\":true}");
    }

    @Test
    @DisplayName("returns 401 when enforce authentication is true and endpoint has no auth")
    void returnsUnauthorizedWhenEnforceAuthenticationIsTrueAndEndpointHasNoAuthorization() throws Exception {
        when(restifyBinding.getConfig()).thenReturn(new RestifyBindingConfig(true, null, null));
        servlet.register("/open-noauth", GET, endpoint("{\"ok\":true}"));

        var response = sendGet("/restify/open-noauth");

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("\"code\":401")
                .contains("servlet.error.authorization.missing-config-or-disable-enforce");
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
    }

    @Test
    @DisplayName("returns payload when enforce authentication is true and default basic is valid")
    void returnsPayloadWhenEnforceAuthenticationIsTrueAndDefaultBasicAuthorizationIsValid() throws Exception {
        when(restifyBinding.getConfig()).thenReturn(new RestifyBindingConfig(true, "john:secret", null));
        servlet.register("/open-default-basic", GET, endpoint("{\"ok\":true}"));
        var credentials = Base64.getEncoder().encodeToString("john:secret".getBytes(StandardCharsets.UTF_8));

        var response = sendGet("/restify/open-default-basic", "Basic " + credentials);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"ok\":true}");
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
    }

    @Test
    @DisplayName("returns payload when default basic is set and provided basic credentials are valid")
    void returnsPayloadWhenDefaultBasicIsSetAndProvidedCredentialsAreValid() throws Exception {
        when(restifyBinding.getConfig()).thenReturn(new RestifyBindingConfig(false, "john:secret", null));
        servlet.register("/default-basic", GET, endpoint("{\"ok\":true}"));
        var credentials = Base64.getEncoder().encodeToString("john:secret".getBytes(StandardCharsets.UTF_8));

        var response = sendGet("/restify/default-basic", "Basic " + credentials);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"ok\":true}");
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
    }

    @Test
    @DisplayName("returns 401 when default basic is set and provided basic credentials are invalid")
    void returnsUnauthorizedWhenDefaultBasicIsSetAndProvidedCredentialsAreInvalid() throws Exception {
        when(restifyBinding.getConfig()).thenReturn(new RestifyBindingConfig(false, "john:secret", null));
        servlet.register("/default-basic-invalid", GET, endpoint("{\"ok\":true}"));
        var credentials = Base64.getEncoder().encodeToString("john:invalid".getBytes(StandardCharsets.UTF_8));

        var response = sendGet("/restify/default-basic-invalid", "Basic " + credentials);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("\"code\":401")
                .contains("servlet.error.authorization.invalid-username-or-password");
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
    }

    @Test
    @DisplayName("returns payload when default bearer is set and provided token is valid")
    void returnsPayloadWhenDefaultBearerIsSetAndProvidedTokenIsValid() throws Exception {
        when(restifyBinding.getConfig()).thenReturn(new RestifyBindingConfig(false, null, "my-token"));
        servlet.register("/default-bearer", GET, endpoint("{\"ok\":true}"));

        var response = sendGet("/restify/default-bearer", "Bearer my-token");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"ok\":true}");
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
    }

    @Test
    @DisplayName("returns 401 when default bearer is set and provided token is invalid")
    void returnsUnauthorizedWhenDefaultBearerIsSetAndProvidedTokenIsInvalid() throws Exception {
        when(restifyBinding.getConfig()).thenReturn(new RestifyBindingConfig(false, null, "my-token"));
        servlet.register("/default-bearer-invalid", GET, endpoint("{\"ok\":true}"));

        var response = sendGet("/restify/default-bearer-invalid", "Bearer wrong-token");

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("\"code\":401").contains("servlet.error.authorization.invalid-token");
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
    }

    private HttpResponse<String> sendGet(String path) throws Exception {
        var request = HttpRequest.newBuilder().uri(server.getURI().resolve(path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendGet(String path, String authorizationHeader) throws Exception {
        var request = HttpRequest.newBuilder().uri(server.getURI().resolve(path))
                .header("Authorization", authorizationHeader).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static Endpoint endpoint(String body) {
        return endpoint(null, body, "application/json");
    }

    private static Endpoint endpoint(@Nullable Authorization authorization, String body, String contentType) {
        return new Endpoint(authorization, transformation(body), contentType);
    }

    private static ChannelTransformation transformation(String body) {
        var transformation = mock(ChannelTransformation.class);
        when(transformation.apply(anyString())).thenReturn(Optional.of(body));
        return transformation;
    }
}
