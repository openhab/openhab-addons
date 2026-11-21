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
package org.openhab.binding.veluxactive.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.veluxactive.internal.api.ModuleInfo;
import org.openhab.binding.veluxactive.internal.api.ModuleType;
import org.openhab.binding.veluxactive.internal.exception.ApiException;
import org.openhab.binding.veluxactive.internal.exception.ApiUnauthorizedException;

import com.google.gson.JsonParser;

/**
 * Verifies VELUX requests, response parsing and structured API failures.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class VeluxActiveApiTest {
    private final HttpClient http = mock(HttpClient.class);
    private final Request request = mock(Request.class, RETURNS_SELF);
    private final ContentResponse response = mock(ContentResponse.class);
    private final VeluxActiveApi api = new VeluxActiveApi(http);

    @Test
    void signedCommandsPreserveRoutingWithoutRainOverride() throws Exception {
        when(response.getContentAsString()).thenReturn("{\"status\":\"ok\"}");
        var signature = Map.<String, Object> of("sign_key_id", "key-id", "timestamp", 1700000000L, "nonce", 1,
                "hash_target_position", "signature");
        api.setSignedPosition("token", "home", "window", "gateway", 50, "Europe/Copenhagen", signature);
        var module = commandBody().getAsJsonObject("home").getAsJsonArray("modules").get(0).getAsJsonObject();
        assertThat(module.get("bridge").getAsString(), is("gateway"));
        assertThat(module.get("hash_target_position").getAsString(), is("signature"));
        assertThat(module.get("target_position").getAsInt(), is(50));
        assertThat(module.has("force"), is(false));
        clearInvocations(request);
        api.unlock("token", "home", "gateway", "Europe/Copenhagen", Map.of("hash_scenario", "signature"));
        module = commandBody().getAsJsonObject("home").getAsJsonArray("modules").get(0).getAsJsonObject();
        assertThat(module.get("scenario").getAsString(), is("home"));
        assertThat(module.get("hash_scenario").getAsString(), is("signature"));
    }

    @Test
    void retrieveKeyAcceptsEmptyAcknowledgementAndRejectsModuleErrors() throws Exception {
        when(response.getContentAsString()).thenReturn("{\"status\":\"ok\"}");
        api.retrieveKey("token", "home", "gateway", "Europe/Copenhagen");
        assertThat(commandBody().getAsJsonObject("home").getAsJsonArray("modules").get(0).getAsJsonObject()
                .get("retrieve_key").getAsBoolean(), is(true));
        when(response.getContentAsString())
                .thenReturn("{\"status\":\"ok\",\"body\":{\"errors\":[{\"id\":\"gateway\",\"code\":6}]}}");
        assertThrows(ApiException.class, () -> api.retrieveKey("token", "home", "gateway", "Europe/Copenhagen"));
    }

    @Test
    void signingRequiresServerClockSample() throws Exception {
        assertThrows(ApiException.class, api::signingTime);
        when(response.getContentAsString()).thenReturn("{\"status\":\"ok\",\"time_server\":1700000000}");
        api.lock("token", "home", "gateway", "Europe/Copenhagen");
        assertThat(api.signingTime() >= 1700000000L && api.signingTime() < 1700000010L, is(true));
    }

    @BeforeEach
    void setup() throws Exception {
        when(http.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
    }

    @Test
    void discoversSupportedModulesWithoutRequiringStatus() throws Exception {
        when(response.getContentAsString()).thenReturn(
                """
                        {"status":"ok","body":{"homes":[
                          {"id":"home-a","modules":[{"id":"gateway-a","type":"NXG","name":"Attic"},
                                                  {"id":"unsupported","type":"NXZ"},{"id":"missing-type"},{"id":"null-type","type":null}]},
                          {"id":"home-b","modules":[{"id":"gateway-b","type":"NXG"}]}]}}
                        """);
        var gateways = api.modules("token");
        assertThat(gateways.size(), is(2));
        assertThat(gateways.getFirst(),
                is(new ModuleInfo("home-a", "gateway-a", "Attic", ModuleType.GATEWAY, null, null, null)));
        assertThat(gateways.get(1).homeId(), is("home-b"));
        verify(http).newRequest("https://app.velux-active.com/api/homesdata");
        verify(request).header(HttpHeader.AUTHORIZATION, "Bearer token");
        verify(request).followRedirects(false);
    }

    @Test
    void discoversOpeningsWithoutRequiringSubtypeOrStatus() throws Exception {
        when(response.getContentAsString()).thenReturn("""
                {"status":"ok","body":{"homes":[{"id":"home","modules":[
                 {"id":"window","type":"NXO","name":"Roof window","velux_type":"window"},
                 {"id":"cover","type":"NXO","velux_type":"shutter"},{"id":"unknown","type":"NXO"}]}]}}
                """);
        var modules = api.modules("token");
        assertThat(modules.size(), is(3));
        assertThat(modules.getFirst(),
                is(new ModuleInfo("home", "window", "Roof window", ModuleType.OPENING, null, null, null)));
        assertThat(modules.get(1).name(), is("VELUX Window/Cover"));
        assertThat(modules.get(2).type(), is(ModuleType.OPENING));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 42, 100 })
    void sendsUnsignedPositionWithGatewayRouting(int position) throws Exception {
        when(response.getContentAsString()).thenReturn("{\"status\":\"ok\"}");
        api.setPosition("token", "home", "opening", "gateway", position, "Europe/Copenhagen");
        verify(http).newRequest("https://app.velux-active.com/syncapi/v1/setstate");
        var home = commandBody().getAsJsonObject("home");
        assertThat(home.get("id").getAsString(), is("home"));
        assertThat(home.get("timezone").getAsString(), is("Europe/Copenhagen"));
        var module = home.getAsJsonArray("modules").get(0).getAsJsonObject();
        assertThat(module.get("id").getAsString(), is("opening"));
        assertThat(module.get("bridge").getAsString(), is("gateway"));
        assertThat(module.get("target_position").getAsInt(), is(position));
        assertThat(module.size(), is(3));
    }

    @Test
    void sendsStopToGatewayWithoutSigning() throws Exception {
        when(response.getContentAsString()).thenReturn("{\"status\":\"ok\",\"body\":null}");
        api.stopMovements("token", "home", "gateway", "Europe/Copenhagen");
        var module = commandBody().getAsJsonObject("home").getAsJsonArray("modules").get(0).getAsJsonObject();
        assertThat(module.get("id").getAsString(), is("gateway"));
        assertThat(module.get("stop_movements").getAsString(), is("all"));
        assertThat(module.size(), is(2));
    }

    @Test
    void rejectsOpeningCommandErrorsAndInvalidPositions() throws Exception {
        when(response.getContentAsString()).thenReturn("""
                {"status":"ok","body":{"errors":[{"id":"opening","code":6}]}}
                """);
        assertThrows(ApiException.class,
                () -> api.setPosition("token", "home", "opening", "gateway", 40, "Europe/Copenhagen"));
        assertThrows(ApiException.class, () -> api.stopMovements("token", "home", "gateway", "Europe/Copenhagen"));
        clearInvocations(request);
        assertThrows(IllegalArgumentException.class,
                () -> api.setPosition("token", "home", "opening", "gateway", -1, "Europe/Copenhagen"));
        assertThrows(IllegalArgumentException.class,
                () -> api.setPosition("token", "home", "opening", "gateway", 101, "Europe/Copenhagen"));
        verify(request, never()).send();
    }

    private com.google.gson.JsonObject commandBody() {
        ArgumentCaptor<StringContentProvider> content = ArgumentCaptor.forClass(StringContentProvider.class);
        verify(request).content(content.capture());
        StringBuilder json = new StringBuilder();
        content.getValue().iterator()
                .forEachRemaining(buffer -> json.append(StandardCharsets.UTF_8.decode(buffer.duplicate())));
        return JsonParser.parseString(json.toString()).getAsJsonObject();
    }

    @Test
    void discoversDepartureSwitchWithoutRoomOrStatus() throws Exception {
        when(response.getContentAsString()).thenReturn("""
                {"status":"ok","body":{"homes":[{"id":"home","modules":[
                  {"id":"switch","type":"NXD","name":"Departure switch","bridge":"gateway"},
                  {"id":"unnamed","type":"NXD"}]}]}}
                """);
        var modules = api.modules("token");
        assertThat(modules.size(), is(2));
        assertThat(modules.getFirst(), is(new ModuleInfo("home", "switch", "Departure switch",
                ModuleType.DEPARTURE_SWITCH, null, null, "gateway")));
        assertThat(modules.get(1).name(), is("VELUX Departure Switch"));
    }

    @Test
    void resolvesGatewayMembershipWithoutGuessingAmbiguousAssignments() throws Exception {
        when(response.getContentAsString()).thenReturn("""
                {"status":"ok","body":{"homes":[{"id":"home","modules":[
                  {"id":"first","type":"NXG","modules_bridged":["fallback","ambiguous","explicit"]},
                  {"id":"second","type":"NXG","modules_bridged":["ambiguous"]},
                  {"id":"fallback","type":"NXS"},
                  {"id":"explicit","type":"NXO","bridge":"second"},
                  {"id":"ambiguous","type":"NXD"},
                  {"id":"unassigned","type":"NXS"}]}]}}
                """);
        var modules = api.modules("token");
        assertThat(modules.get(2).bridgeId(), is("first"));
        assertThat(modules.get(3).bridgeId(), is("second"));
        assertThat(modules.get(4).bridgeId(), nullValue());
        assertThat(modules.get(5).bridgeId(), nullValue());
    }

    @Test
    void mapsSensorsToRoomsUsingTopology() throws Exception {
        when(response.getContentAsString()).thenReturn("""
                {"status":"ok","body":{"homes":[{"id":"home","rooms":[
                  {"id":"hall","name":"Hall","module_ids":["sensor","window"]},
                  {"id":"bedroom","name":"Bedroom","module_ids":["fallback"]}],
                  "modules":[{"id":"sensor","type":"NXS","name":"Indoor sensor","room_id":"hall"},
                    {"id":"fallback","type":"NXS"},{"id":"unassigned","type":"NXS"}]}]}}
                """);
        var modules = api.modules("token");
        assertThat(modules.get(0).roomId(), is("hall"));
        assertThat(modules.get(0).roomName(), is("Hall"));
        assertThat(modules.get(1).roomId(), is("bedroom"));
        assertThat(modules.get(2).roomId(), nullValue());
    }

    @Test
    void parsesGatewayStatusWithoutReachableField() throws Exception {
        when(response.getContentAsString()).thenReturn("""
                {"status":"ok","body":{"home":{"id":"home","modules":[
                {"id":"gateway","type":"NXG","locked":true,"locking":false,"last_seen":1791635715}
                ]}}}
                """);
        var status = api.homeStatus("token", "home");
        var module = Objects.requireNonNull(status.module("gateway"));
        assertThat(module.reachable, nullValue());
        assertThat(module.locked, is(Boolean.TRUE));
        assertThat(module.locking, is(Boolean.FALSE));
        assertThat(module.lastSeen, is(1791635715L));
        verify(http).newRequest("https://app.velux-active.com/api/homestatus");
    }

    @Test
    void preservesPerGatewayUnreachableErrorsEvenWithoutHome() throws Exception {
        when(response.getContentAsString()).thenReturn("""
                {"status":"ok","body":{"errors":[{"id":"gateway","code":6}]}}
                """);
        var status = api.homeStatus("token", "home");
        assertThat(Objects.requireNonNull(status.error("gateway")).code, is(6));
        assertThat(status.error("another-gateway"), nullValue());
        assertThat(status.module("gateway"), nullValue());
    }

    @Test
    void rejectsStatusForAnotherHome() {
        when(response.getContentAsString()).thenReturn("""
                {"status":"ok","body":{"home":{"id":"another-home","modules":[]}}}
                """);
        assertThrows(ApiException.class, () -> api.homeStatus("token", "home"));
    }

    @Test
    void sendsUnsignedAwayCommand() throws Exception {
        when(response.getContentAsString()).thenReturn("{\"status\":\"ok\",\"body\":{}}");
        api.lock("token", "home", "gateway", "Europe/Copenhagen");
        verify(http).newRequest("https://app.velux-active.com/syncapi/v1/setstate");
        ArgumentCaptor<StringContentProvider> content = ArgumentCaptor.forClass(StringContentProvider.class);
        verify(request).content(content.capture());
        var buffers = content.getValue().iterator();
        StringBuilder json = new StringBuilder();
        while (buffers.hasNext()) {
            json.append(StandardCharsets.UTF_8.decode(buffers.next().duplicate()));
        }
        var body = JsonParser.parseString(json.toString()).getAsJsonObject();
        assertThat(body.get("app_type").getAsString(), is("app_velux"));
        var home = body.getAsJsonObject("home");
        assertThat(home.get("id").getAsString(), is("home"));
        assertThat(home.get("timezone").getAsString(), is("Europe/Copenhagen"));
        var module = home.getAsJsonArray("modules").get(0).getAsJsonObject();
        assertThat(module.get("id").getAsString(), is("gateway"));
        assertThat(module.get("scenario").getAsString(), is("away"));
        assertThat(module.size(), is(2));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"status\":\"ok\",\"time_server\":1791654350}", "{\"status\":\"ok\",\"body\":null}",
            "{\"status\":\"ok\",\"body\":{}}", "{\"status\":\"ok\",\"body\":{\"errors\":[]}}" })
    void acceptsLockAcknowledgementWithOptionalBody(String json) throws Exception {
        when(response.getContentAsString()).thenReturn(json);
        api.lock("token", "home", "gateway", "Europe/Copenhagen");
    }

    @Test
    void stillRequiresBodyForStatusAndTopology() {
        when(response.getContentAsString()).thenReturn("{\"status\":\"ok\"}");
        assertThrows(ApiException.class, () -> api.homeStatus("token", "home"));
        assertThrows(ApiException.class, () -> api.modules("token"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"status\":\"failed\"}", "{\"status\":\"ok\",\"error\":{\"code\":6}}",
            "{\"status\":\"ok\",\"body\":{\"errors\":[{\"code\":6}]}}" })
    void rejectsUnconfirmedOrFailedLockCommand(String json) {
        when(response.getContentAsString()).thenReturn(json);
        assertThrows(ApiException.class, () -> api.lock("token", "home", "gateway", "Europe/Copenhagen"));
    }

    @Test
    void rejectsCommandErrorsInsideSuccessfulHttpResponse() {
        when(response.getContentAsString()).thenReturn("""
                {"status":"ok","body":{"errors":[{"id":"gateway","code":6}]}}
                """);
        assertThrows(ApiException.class, () -> api.lock("token", "home", "gateway", "Europe/Copenhagen"));
    }

    @Test
    void identifiesRejectedAuthenticationWithoutLeakingResponse() {
        var exception = assertThrows(ApiUnauthorizedException.class, () -> VeluxActiveApi.parseResponse(200, """
                {"error":{"code":2,"message":"secret-token"}}
                """));
        assertThat(Objects.requireNonNull(exception.getMessage()).contains("secret-token"), is(false));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "not JSON", "null", "[]", "{}", "{\"status\":\"failed\"}",
            "{\"status\":\"ok\",\"body\":[]}" })
    void rejectsMalformedResponses(String response) {
        assertThrows(ApiException.class, () -> VeluxActiveApi.parseResponse(200, response));
    }

    @Test
    void hidesTransportExceptionDetails() throws Exception {
        when(request.send()).thenThrow(new ExecutionException(new IllegalStateException("secret-token")));
        var error = assertThrows(ApiException.class, () -> api.modules("token"));
        assertThat(Objects.requireNonNull(error.getMessage()).contains("secret-token"), is(false));
        assertThat(error.getCause(), nullValue());
    }
}
