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
package org.openhab.binding.httphook.internal.servlet;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.httphook.internal.servlet.DispatcherServlet.Method.GET;
import static org.openhab.binding.httphook.internal.servlet.DispatcherServlet.Method.POST;

import java.io.ByteArrayInputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.httphook.internal.HttpHookBinding;
import org.openhab.binding.httphook.internal.HttpHookBindingConfig;
import org.openhab.binding.httphook.internal.endpoint.Endpoint;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.binding.generic.ChannelTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Martin Grzeslowski - Initial contribution
 * @author Martin Grześlowski - Test browser response isolation and credential filtering
 */
@Timeout(value = 3, unit = SECONDS)
class DispatcherServletIntTest {
    private static final HttpHookBindingConfig ANONYMOUS_CONFIG = new HttpHookBindingConfig(false, null, null);
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServletIntTest.class);
    private static DispatcherServlet servlet;

    private static HttpHookBinding httpHookBinding;

    private static Server server;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void beforeAll() throws Exception {
        logger.info("Starting DispatcherServletIntTest Jetty server");
        TranslationProvider i18nProvider = mock();
        httpHookBinding = mock();

        lenient().when(httpHookBinding.getConfig()).thenReturn(ANONYMOUS_CONFIG);
        var authorizationService = new AuthorizationService(httpHookBinding);
        servlet = new DispatcherServlet(i18nProvider, authorizationService);

        server = new Server(0);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(servlet), "/httphook/*");

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
        servlet.register("/status/response", GET, endpoint("{\"foo\":\"boo\"}"));

        var response = sendGet("/httphook/status/response");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"foo\":\"boo\"}");
        assertThat(response.headers().firstValue("Content-Type"))
                .hasValueSatisfying(contentType -> assertThat(contentType).startsWith("application/json"));
    }

    @Test
    @DisplayName("returns configured content type")
    void returnsConfiguredContentType() throws Exception {
        servlet.register("/status/text", GET, endpoint(null, "plain response", "text/plain"));

        var response = sendGet("/httphook/status/text");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("plain response");
        assertThat(response.headers().firstValue("Content-Type"))
                .hasValueSatisfying(contentType -> assertThat(contentType).startsWith("text/plain"));
        assertThat(response.headers().firstValue("Content-Disposition")).isEmpty();
    }

    @Test
    void servesHtmlAsASandboxedDownload() throws Exception {
        var body = "<p>Frame status</p>";
        servlet.register("/status/html", GET, endpoint(null, body, "text/html"));

        var response = sendGet("/httphook/status/html");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo(body);
        assertThat(response.headers().firstValue("Content-Type"))
                .hasValueSatisfying(contentType -> assertThat(contentType).startsWith("text/html"));
        assertThat(response.headers().firstValue("Content-Disposition")).contains("attachment");
        assertThat(response.headers().firstValue("Content-Security-Policy")).contains("sandbox; default-src 'none'");
        assertThat(response.headers().firstValue("X-Content-Type-Options")).contains("nosniff");
    }

    @Test
    @DisplayName("passes request metadata JSON to transformation")
    void passesRequestMetadataJsonToTransformation() throws Exception {
        var transformation = transformation("ok");
        var authorization = new Authorization.Bearer("metadata-token");
        servlet.register("/status/metadata", POST, new Endpoint(authorization, transformation, "text/plain"));

        var request = HttpRequest.newBuilder().uri(server.getURI().resolve("/httphook/status/metadata?unit=test"))
                .header("X-Test", "abc").header("aUtHoRiZaTiOn", "Bearer metadata-token")
                .header("cOoKiE", "session=metadata-cookie").header("X-API-Key", "metadata-api-key")
                .POST(HttpRequest.BodyPublishers.ofString("payload")).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(transformation).apply(captor.capture());
        var context = captor.getValue();
        assertThat(context).contains("\"method\":\"POST\"", "\"path\":\"/status/metadata\"",
                "\"queryString\":\"unit=test\"", "\"body\":\"payload\"", "X-Test", "abc");
        assertThat(context).doesNotContain("metadata-token", "metadata-cookie", "metadata-api-key");
    }

    @Test
    void acceptsBodyAtByteLimitAndPreservesItsEncoding() throws Exception {
        var transformation = transformation("ok");
        servlet.register("/status/body-at-limit", POST, new Endpoint(null, transformation, "text/plain"));
        var body = "é".repeat(32 * 1024);
        var request = HttpRequest.newBuilder(server.getURI().resolve("/httphook/status/body-at-limit"))
                .header("Content-Type", "text/plain; charset=UTF-8").POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        var context = ArgumentCaptor.forClass(String.class);
        verify(transformation).apply(context.capture());
        assertThat(new ObjectMapper().readTree(context.getValue()).get("body").asText()).isEqualTo(body);
    }

    @Test
    void rejectsOversizedBodiesWithAndWithoutContentLength() throws Exception {
        var transformation = mock(ChannelTransformation.class);
        servlet.register("/status/oversized-body", POST, new Endpoint(null, transformation, "text/plain"));
        var body = ("é".repeat(32 * 1024) + "x").getBytes(StandardCharsets.UTF_8);
        var publishers = new HttpRequest.BodyPublisher[] { HttpRequest.BodyPublishers.ofByteArray(body),
                HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(body)) };

        for (var publisher : publishers) {
            // Jetty can reject a known length before reading the body. Wait for its response before uploading.
            var request = HttpRequest.newBuilder(server.getURI().resolve("/httphook/status/oversized-body"))
                    .expectContinue(publisher.contentLength() >= 0).header("Content-Type", "text/plain; charset=UTF-8")
                    .POST(publisher).build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(413);
            assertThat(response.body()).contains("\"code\":413", "servlet.error.body-too-large");
        }
        verifyNoInteractions(transformation);
    }

    @Test
    @DisplayName("returns 500 when transformation returns no output")
    void returnsServerErrorWhenTransformationReturnsNoOutput() throws Exception {
        var transformation = mock(ChannelTransformation.class);
        when(transformation.apply(anyString())).thenReturn(Optional.empty());
        servlet.register("/status/empty", GET, new Endpoint(null, transformation, "application/json"));

        var response = sendGet("/httphook/status/empty");

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.body()).contains("\"code\":500").contains("servlet.error.transformation.failed");
    }

    @Test
    void returnsGenericServerErrorWhenTransformationThrows() throws Exception {
        var transformation = mock(ChannelTransformation.class);
        when(transformation.apply(anyString())).thenThrow(new UnsupportedOperationException("Internal details"));
        servlet.register("/status/failed", GET, new Endpoint(null, transformation, "application/json"));

        var response = sendGet("/httphook/status/failed");

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.body()).isEqualTo("{\"code\":500,\"error\":\"servlet.error.internal\"}");
    }

    @Test
    @DisplayName("returns 404 for unknown endpoint")
    void returnsNotFoundForUnknownEndpoint() throws Exception {
        var response = sendGet("/httphook/status/unknown");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("\"code\":404").contains("servlet.error.not-found");
    }

    @Test
    @DisplayName("returns 401 when endpoint requires authorization and header is missing")
    void returnsUnauthorizedWhenAuthorizationHeaderIsMissing() throws Exception {
        servlet.register("/status/secure", GET,
                endpoint(new Authorization.Basic("john", "secret"), "{\"ok\":true}", "application/json"));

        var response = sendGet("/httphook/status/secure");

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("\"code\":401").contains("servlet.error.authorization.required");
    }

    @Test
    @DisplayName("returns payload when basic authorization header is valid")
    void returnsPayloadWhenBasicAuthorizationHeaderIsValid() throws Exception {
        servlet.register("/status/secure-valid", GET,
                endpoint(new Authorization.Basic("john", "secret"), "{\"ok\":true}", "application/json"));
        var credentials = Base64.getEncoder().encodeToString("john:secret".getBytes(StandardCharsets.UTF_8));

        var request = HttpRequest.newBuilder().uri(server.getURI().resolve("/httphook/status/secure-valid"))
                .header("Authorization", "Basic " + credentials).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"ok\":true}");
    }

    @Test
    @DisplayName("returns 401 when enforce authentication is true and endpoint has no auth")
    void returnsUnauthorizedWhenEnforceAuthenticationIsTrueAndEndpointHasNoAuthorization() throws Exception {
        when(httpHookBinding.getConfig()).thenReturn(new HttpHookBindingConfig(true, null, null));
        servlet.register("/status/open-noauth", GET, endpoint("{\"ok\":true}"));

        var response = sendGet("/httphook/status/open-noauth");

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("\"code\":401")
                .contains("servlet.error.authorization.missing-config-or-disable-enforce");
        when(httpHookBinding.getConfig()).thenReturn(ANONYMOUS_CONFIG);
    }

    @Test
    @DisplayName("returns payload when enforce authentication is true and default basic is valid")
    void returnsPayloadWhenEnforceAuthenticationIsTrueAndDefaultBasicAuthorizationIsValid() throws Exception {
        when(httpHookBinding.getConfig()).thenReturn(new HttpHookBindingConfig(true, "john:secret", null));
        servlet.register("/status/open-default-basic", GET, endpoint("{\"ok\":true}"));
        var credentials = Base64.getEncoder().encodeToString("john:secret".getBytes(StandardCharsets.UTF_8));

        var response = sendGet("/httphook/status/open-default-basic", "Basic " + credentials);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"ok\":true}");
        when(httpHookBinding.getConfig()).thenReturn(ANONYMOUS_CONFIG);
    }

    @Test
    @DisplayName("returns payload when default basic is set and provided basic credentials are valid")
    void returnsPayloadWhenDefaultBasicIsSetAndProvidedCredentialsAreValid() throws Exception {
        when(httpHookBinding.getConfig()).thenReturn(new HttpHookBindingConfig(false, "john:secret", null));
        servlet.register("/status/default-basic", GET, endpoint("{\"ok\":true}"));
        var credentials = Base64.getEncoder().encodeToString("john:secret".getBytes(StandardCharsets.UTF_8));

        var response = sendGet("/httphook/status/default-basic", "Basic " + credentials);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"ok\":true}");
        when(httpHookBinding.getConfig()).thenReturn(ANONYMOUS_CONFIG);
    }

    @Test
    @DisplayName("returns 401 when default basic is set and provided basic credentials are invalid")
    void returnsUnauthorizedWhenDefaultBasicIsSetAndProvidedCredentialsAreInvalid() throws Exception {
        when(httpHookBinding.getConfig()).thenReturn(new HttpHookBindingConfig(false, "john:secret", null));
        servlet.register("/status/default-basic-invalid", GET, endpoint("{\"ok\":true}"));
        var credentials = Base64.getEncoder().encodeToString("john:invalid".getBytes(StandardCharsets.UTF_8));

        var response = sendGet("/httphook/status/default-basic-invalid", "Basic " + credentials);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("\"code\":401")
                .contains("servlet.error.authorization.invalid-username-or-password");
        when(httpHookBinding.getConfig()).thenReturn(ANONYMOUS_CONFIG);
    }

    @Test
    @DisplayName("returns payload when default bearer is set and provided token is valid")
    void returnsPayloadWhenDefaultBearerIsSetAndProvidedTokenIsValid() throws Exception {
        when(httpHookBinding.getConfig()).thenReturn(new HttpHookBindingConfig(false, null, "my-token"));
        servlet.register("/status/default-bearer", GET, endpoint("{\"ok\":true}"));

        var response = sendGet("/httphook/status/default-bearer", "Bearer my-token");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"ok\":true}");
        when(httpHookBinding.getConfig()).thenReturn(ANONYMOUS_CONFIG);
    }

    @Test
    @DisplayName("returns 401 when default bearer is set and provided token is invalid")
    void returnsUnauthorizedWhenDefaultBearerIsSetAndProvidedTokenIsInvalid() throws Exception {
        when(httpHookBinding.getConfig()).thenReturn(new HttpHookBindingConfig(false, null, "my-token"));
        servlet.register("/status/default-bearer-invalid", GET, endpoint("{\"ok\":true}"));

        var response = sendGet("/httphook/status/default-bearer-invalid", "Bearer wrong-token");

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("\"code\":401").contains("servlet.error.authorization.invalid-token");
        when(httpHookBinding.getConfig()).thenReturn(ANONYMOUS_CONFIG);
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
