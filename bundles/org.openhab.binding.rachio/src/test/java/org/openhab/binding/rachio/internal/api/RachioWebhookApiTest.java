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
package org.openhab.binding.rachio.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.APIURL_BASE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.APIURL_DEV_DELETE_WEBHOOK;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.APIURL_DEV_POST_WEBHOOK;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.APIURL_DEV_QUERY_WEBHOOK;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.APIURL_DEV_WEBHOOK_EVENT_TYPES;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_DEVICE_ZONE_RUN_STARTED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_PROGRAM_RAIN_SKIP_CANCELED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_PROGRAM_RAIN_SKIP_CREATED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_SCHEDULE_STARTED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_VALVE_RUN_END;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_VALVE_RUN_START;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.WEBHOOK_CREATE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.WEBHOOK_LIST;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.WEBHOOK_LIST_EVENT_TYPES;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.WEBHOOK_QUERY_CONTROLLER_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.WEBHOOK_QUERY_VALVE_ID;

import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioApiLegacyWebHookEventType;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioApiWebHookEntry;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioApiWebHookResourceId;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookTarget;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests generic WebhookService API helpers.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioWebhookApiTest {
    private static final OfficialSignatureSample FREEZE_SKIP_SAMPLE = new OfficialSignatureSample(
            "FREEZE_SKIP_NOTIFICATION_EVENT", "8f68fb5e-02e8-4b2d-adb0-d2fd1e59db6c",
            "{\"eventId\":\"6776d89e-b4e7-3f5a-864f-ba39e6bafa05\",\"eventType\":\"FREEZE_SKIP_NOTIFICATION_EVENT\",\"externalId\":\"schedule freeze webhook\",\"payload\":{\"scheduleId\":\"0114b3b9-31fb-4fe8-aa0e-b4f60aac5f91\",\"startTime\":\"2024-12-19T20:33:47.487367Z\",\"tempC\":\"2.0\",\"thresholdC\":\"7.0\"},\"resourceId\":\"85c309c6-ba69-4f90-8f3c-60e5ea3640fb\",\"resourceType\":\"IRRIGATION_CONTROLLER\",\"timestamp\":\"2024-12-19T19:33:47.487355Z\"}",
            "1056e2029f904e981ab76a2425853d90e7663b1bb8329a7c764f63f015be3092");
    private static final OfficialSignatureSample PROGRAM_RAIN_SKIP_SAMPLE = new OfficialSignatureSample(
            "PROGRAM_RAIN_SKIP_CREATED_EVENT", "10061589-b3e1-418e-a27d-d1bb4763a9ea",
            "{\"eventId\":\"6ace3fde-7f3d-316c-9a02-67f7d36ba6a3\",\"eventType\":\"PROGRAM_RAIN_SKIP_CREATED_EVENT\",\"payload\":{\"plannedRunStartTime\":\"2024-12-11T23:08:09Z\"},\"resourceId\":\"34e81383-6116-4604-be1c-b6e2dedebaef\",\"resourceType\":\"PROGRAM\",\"timestamp\":\"2024-12-11T22:18:00.445250Z\"}",
            "5daa5f6a301fce1d6f251096dd89c5c20809a80312b8d95ddf9c47da2ec5552f");
    private static final OfficialSignatureSample VALVE_RUN_START_SAMPLE = new OfficialSignatureSample(
            "VALVE_RUN_START_EVENT", "0b6de780-6f47-410b-8c2a-c6e9c506f3f4",
            "{\"eventId\":\"e040e09b-3d09-3dca-a688-3b24401d53b0\",\"eventType\":\"VALVE_RUN_START_EVENT\",\"externalId\":\"123-123-123\",\"payload\":{\"durationSeconds\":\"181\",\"flowDetected\":false,\"programId\":\"48a7181c-e06e-4a4f-bd65-92567fce0b81\",\"runType\":\"PROGRAM\",\"startTime\":\"2024-12-11T20:20:48Z\"},\"resourceId\":\"205aa289-8fe0-4f8e-8fc2-032f5f1e1c24\",\"resourceType\":\"VALVE\",\"timestamp\":\"2024-12-11T20:20:52Z\"}",
            "b8b1a1ee62542e3910c41f405371d5e419f30055f6ce07d776e8cbee84d7b14d");

    @Test
    void officialRachioWebhookSignatureSamplesValidateAgainstRawMinifiedBody() {
        for (OfficialSignatureSample sample : officialSignatureSamples()) {
            assertThat(sample.eventType(),
                    RachioApi.isValidWebHookSignature(sample.signature(), bytes(sample.body()), sample.apiToken()),
                    is(true));
            assertThat(sample.eventType(), RachioApi.isValidWebHookSignature("sha256=" + sample.signature(),
                    bytes(sample.body()), sample.apiToken()), is(true));
        }
    }

    @Test
    void officialRachioWebhookSignatureSamplesRejectWrongTokenAndAlteredPayload() {
        for (OfficialSignatureSample sample : officialSignatureSamples()) {
            assertThat(sample.eventType(),
                    RachioApi.isValidWebHookSignature(sample.signature(), bytes(sample.body()), "wrong-token"),
                    is(false));
            assertThat(sample.eventType(), RachioApi.isValidWebHookSignature(sample.signature(),
                    bytes(alterEventId(sample.body())), sample.apiToken()), is(false));
            assertThat(sample.eventType(), RachioApi.isValidWebHookSignature(sample.signature(),
                    bytes(alterResourceId(sample.body())), sample.apiToken()), is(false));
        }
    }

    @Test
    void prettyPrintedOfficialRachioWebhookBodyDoesNotValidateAgainstMinifiedBodySignature() {
        String prettyBody = new GsonBuilder().setPrettyPrinting().create()
                .toJson(JsonParser.parseString(FREEZE_SKIP_SAMPLE.body()));

        assertThat(RachioApi.isValidWebHookSignature(FREEZE_SKIP_SAMPLE.signature(), bytes(prettyBody),
                FREEZE_SKIP_SAMPLE.apiToken()), is(false));
    }

    @Test
    void webhookListParsesStringEventTypes() {
        List<RachioApiWebHookEntry> webhooks = RachioApi.parseWebHookList("""
                [
                  {
                    "id": "hook-id",
                    "url": "https://example.org/rachio/webhook",
                    "externalId": "external-id",
                    "eventTypes": [ "ZONE_STATUS", "SCHEDULE_STATUS" ]
                  }
                ]
                """);

        assertThat(webhooks.size(), is(1));
        assertThat(webhooks.getFirst().eventTypes, contains("ZONE_STATUS", "SCHEDULE_STATUS"));
    }

    @Test
    void webhookListParsesObjectEventTypesByPreferredIdentifier() {
        List<RachioApiWebHookEntry> webhooks = RachioApi.parseWebHookList("""
                {
                  "webhooks": [
                    {
                      "id": "hook-id",
                      "url": "https://example.org/rachio/webhook",
                      "externalId": "external-id",
                      "eventTypes": [
                        { "id": "ZONE_STATUS", "type": "ignored-type", "description": "Zone status" },
                        { "type": "SCHEDULE_STATUS" },
                        { "name": "DEVICE_STATUS" },
                        { "eventType": "RAIN_DELAY" }
                      ]
                    }
                  ]
                }
                """);

        assertThat(webhooks.size(), is(1));
        assertThat(webhooks.getFirst().eventTypes,
                contains("ZONE_STATUS", "SCHEDULE_STATUS", "DEVICE_STATUS", "RAIN_DELAY"));
    }

    @Test
    void webhookListParsesMixedEventTypesAndSkipsUnknownObjects() {
        List<RachioApiWebHookEntry> webhooks = RachioApi.parseWebHookList("""
                {
                  "data": [
                    {
                      "id": "hook-id",
                      "resourceId": { "irrigationControllerId": "controller-id" },
                      "eventTypes": [
                        "DEVICE_ZONE_RUN_STARTED_EVENT",
                        { "id": "10" },
                        { "description": "unsupported shape" },
                        [ "unsupported nested array" ]
                      ]
                    }
                  ]
                }
                """);

        assertThat(webhooks.size(), is(1));
        assertThat(webhooks.getFirst().eventTypes, contains("DEVICE_ZONE_RUN_STARTED_EVENT", "10"));
        assertThat(webhooks.getFirst().resourceId.irrigationControllerId, is("controller-id"));
    }

    @Test
    void webhookListReturnsEntryWhenLegacyEventTypesAreObjects() {
        List<RachioApiWebHookEntry> webhooks = RachioApi.parseWebHookList("""
                {
                  "id": "hook-id",
                  "url": "https://example.org/rachio/webhook",
                  "externalId": "external-id",
                  "eventTypes": [
                    { "id": "ZONE_STATUS", "type": "ZONE_STATUS", "description": "Zone status" },
                    { "id": "SCHEDULE_STATUS", "type": "SCHEDULE_STATUS" }
                  ]
                }
                """);

        assertThat(webhooks.size(), is(1));
        assertThat(webhooks.getFirst().id, is("hook-id"));
        assertThat(webhooks.getFirst().url, is("https://example.org/rachio/webhook"));
        assertThat(webhooks.getFirst().externalId, is("external-id"));
        assertThat(webhooks.getFirst().eventTypes, contains("ZONE_STATUS", "SCHEDULE_STATUS"));
    }

    @Test
    void legacyEventTypeListParsesNotificationServiceResponse() {
        String json = """
                [
                  {"id":"5","type":"DEVICE_STATUS","description":"Device status"},
                  {"id":"10","type":"ZONE_STATUS","description":"Zone status"}
                ]
                """;

        List<RachioApiLegacyWebHookEventType> eventTypes = RachioApi.parseLegacyNotificationEventTypes(json);

        assertThat(eventTypes.size(), is(2));
        assertThat(eventTypes.getFirst().id, is("5"));
        assertThat(eventTypes.getFirst().type, is("DEVICE_STATUS"));
    }

    @Test
    void eventTypeListParsesStringAndObjectEntries() {
        String json = """
                {
                  "eventTypes": [
                    "DEVICE_ZONE_RUN_STARTED_EVENT",
                    {"eventType":"SCHEDULE_STARTED_EVENT"}
                  ]
                }
                """;

        List<String> eventTypes = RachioApi.parseWebhookEventTypeList(json);

        assertThat(eventTypes, contains(EVENT_DEVICE_ZONE_RUN_STARTED, EVENT_SCHEDULE_STARTED));
    }

    @Test
    void eventTypeMapParsesGroupedResourceResponse() {
        String json = """
                {
                  "eventTypes": [
                    {
                      "resourceType": "VALVE",
                      "eventTypes": [
                        "VALVE_RUN_START_EVENT",
                        "VALVE_RUN_END_EVENT"
                      ]
                    },
                    {
                      "resourceType": "PROGRAM",
                      "eventTypes": [
                        "PROGRAM_RAIN_SKIP_CREATED_EVENT",
                        "PROGRAM_RAIN_SKIP_CANCELED_EVENT"
                      ]
                    },
                    {
                      "resourceType": "LIGHTING_CONTROLLER",
                      "eventTypes": [
                        "LIGHTING_ZONE_STATE_CHANGE_EVENT"
                      ]
                    },
                    {
                      "resourceType": "IRRIGATION_CONTROLLER",
                      "eventTypes": [
                        "SCHEDULE_STARTED_EVENT",
                        "SCHEDULE_STOPPED_EVENT"
                      ]
                    }
                  ]
                }
                """;

        Map<RachioWebhookResourceType, Set<String>> eventTypesByResourceType = RachioApi.parseWebhookEventTypeMap(json);

        assertThat(Objects.requireNonNull(eventTypesByResourceType.get(RachioWebhookResourceType.VALVE)).size(), is(2));
        assertThat(Objects.requireNonNull(eventTypesByResourceType.get(RachioWebhookResourceType.PROGRAM)).size(),
                is(2));
        assertThat(Objects.requireNonNull(eventTypesByResourceType.get(RachioWebhookResourceType.LIGHTING_CONTROLLER))
                .size(), is(1));
        assertThat(Objects.requireNonNull(eventTypesByResourceType.get(RachioWebhookResourceType.IRRIGATION_CONTROLLER))
                .size(), is(2));
        assertThat(
                Objects.requireNonNull(eventTypesByResourceType.get(RachioWebhookResourceType.IRRIGATION_CONTROLLER)),
                containsInAnyOrder("SCHEDULE_STARTED_EVENT", "SCHEDULE_STOPPED_EVENT"));
    }

    @Test
    void irrigationTargetBuildsResourceAwareListQueryAndPayload() {
        RachioWebhookTarget target = RachioWebhookTarget.irrigationController("device id",
                List.of(EVENT_DEVICE_ZONE_RUN_STARTED));

        assertThat(target.buildListQuery(), is(WEBHOOK_QUERY_CONTROLLER_ID + "=device+id"));
        Map<String, Object> payload = target.buildCreatePayload("https://host/rachio/webhook", "external-id");

        assertThat(payload, hasEntry("externalId", "external-id"));
        assertThat(payload, hasEntry("url", "https://host/rachio/webhook"));
        @SuppressWarnings("unchecked")
        Map<String, Object> resourceId = (Map<String, Object>) payload.get("resourceId");
        assertThat(resourceId, hasEntry("irrigationControllerId", "device id"));
    }

    @Test
    void valveTargetBuildsResourceAwareListQueryAndPayload() {
        RachioWebhookTarget target = new RachioWebhookTarget("valve id", RachioWebhookResourceType.VALVE,
                List.of(EVENT_VALVE_RUN_START, EVENT_VALVE_RUN_END));

        assertThat(target.buildListQuery(), is(WEBHOOK_QUERY_VALVE_ID + "=valve+id"));
        Map<String, Object> payload = target.buildCreatePayload("https://host/rachio/webhook", "external-id");

        @SuppressWarnings("unchecked")
        Map<String, Object> resourceId = (Map<String, Object>) payload.get("resourceId");
        assertThat(resourceId, hasEntry("valveId", "valve id"));
        assertThat(payload.get("url"), is("https://host/rachio/webhook"));
        assertThat(payload.get("eventTypes"), is(List.of(EVENT_VALVE_RUN_START, EVENT_VALVE_RUN_END)));
    }

    @Test
    void programTargetBuildsResourceAwarePayload() {
        RachioWebhookTarget target = new RachioWebhookTarget("program id", RachioWebhookResourceType.PROGRAM,
                List.of(EVENT_PROGRAM_RAIN_SKIP_CREATED, EVENT_PROGRAM_RAIN_SKIP_CANCELED));
        Map<String, Object> payload = target.buildCreatePayload("https://host/rachio/webhook", "external-id");

        @SuppressWarnings("unchecked")
        Map<String, Object> resourceId = (Map<String, Object>) payload.get("resourceId");
        assertThat(resourceId, hasEntry("programId", "program id"));
        assertThat(payload.get("eventTypes"),
                is(List.of(EVENT_PROGRAM_RAIN_SKIP_CREATED, EVENT_PROGRAM_RAIN_SKIP_CANCELED)));
    }

    @Test
    void exactWebhookMatchIsRetained() {
        RachioWebhookTarget target = RachioWebhookTarget.irrigationController("device-id",
                List.of(EVENT_DEVICE_ZONE_RUN_STARTED, EVENT_SCHEDULE_STARTED));
        RachioApiWebHookEntry webhook = webhook("https://host/rachio/webhook", "external-id", "device-id",
                List.of(EVENT_SCHEDULE_STARTED, EVENT_DEVICE_ZONE_RUN_STARTED));

        assertThat(target.matches(webhook, "https://host/rachio/webhook", "external-id"), is(true));
    }

    @Test
    void mismatchedWebhookIsNotAnExactMatch() {
        RachioWebhookTarget target = RachioWebhookTarget.irrigationController("device-id",
                List.of(EVENT_DEVICE_ZONE_RUN_STARTED, EVENT_SCHEDULE_STARTED));
        RachioApiWebHookEntry webhook = webhook("https://host/rachio/webhook", "external-id", "other-device",
                List.of(EVENT_SCHEDULE_STARTED, EVENT_DEVICE_ZONE_RUN_STARTED));

        assertThat(target.matches(webhook, "https://host/rachio/webhook", "external-id"), is(false));
    }

    @Test
    void unrelatedResourceTypeDoesNotMatchTarget() {
        RachioWebhookTarget target = new RachioWebhookTarget("valve-id", RachioWebhookResourceType.VALVE,
                List.of("VALVE_RUN_STARTED_EVENT"));
        RachioApiWebHookEntry webhook = webhook("https://host/rachio/webhook", "external-id", "device-id",
                List.of("VALVE_RUN_STARTED_EVENT"));

        assertThat(target.resourceMatches(webhook), is(false));
    }

    @Test
    void irrigationTargetAcceptsIrrigationEvents() {
        RachioWebhookTarget target = RachioWebhookTarget.irrigationController("device-id",
                List.of(EVENT_DEVICE_ZONE_RUN_STARTED, EVENT_SCHEDULE_STARTED));

        assertThat(target.getUnsupportedEventTypes(Set.of(EVENT_DEVICE_ZONE_RUN_STARTED, EVENT_SCHEDULE_STARTED))
                .isEmpty(), is(true));
    }

    @Test
    void valveTargetAcceptsValveEvents() {
        RachioWebhookTarget target = new RachioWebhookTarget("valve-id", RachioWebhookResourceType.VALVE,
                List.of(EVENT_VALVE_RUN_START, EVENT_VALVE_RUN_END));

        assertThat(target.getUnsupportedEventTypes(Set.of(EVENT_VALVE_RUN_START, EVENT_VALVE_RUN_END)).isEmpty(),
                is(true));
    }

    @Test
    void invalidResourceEventCombinationIsDetectedAndFiltered() {
        RachioWebhookTarget target = new RachioWebhookTarget("valve-id", RachioWebhookResourceType.VALVE,
                List.of("VALVE_RUN_START_EVENT", EVENT_SCHEDULE_STARTED));
        Set<String> supportedValveEvents = Set.of("VALVE_RUN_START_EVENT", "VALVE_RUN_END_EVENT");

        assertThat(target.getUnsupportedEventTypes(supportedValveEvents), contains(EVENT_SCHEDULE_STARTED));
        assertThat(target.filterEventTypes(supportedValveEvents).getEventTypeList(), contains("VALVE_RUN_START_EVENT"));
    }

    @Test
    void registerWebhookUsesInitializationPurposeAndFallsBackWhenEventCatalogLookupIsLocallyThrottled()
            throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        RecordingRateLimitManager rateLimitManager = new RecordingRateLimitManager(1);
        setField(api, "httpApi", http);
        setField(api, "rateLimitManager", rateLimitManager);

        api.registerWebHook("device-id", "https://host.example/rachio/webhook", "", "", "external-id", false,
                RequestPurpose.INITIALIZATION);

        assertThat(rateLimitManager.requestPurposes,
                contains(RequestPurpose.INITIALIZATION, RequestPurpose.INITIALIZATION, RequestPurpose.INITIALIZATION));
        assertThat(http.getUrls.size(), is(1));
        assertThat(http.getUrls.getFirst(), containsString(WEBHOOK_LIST));
        assertThat(http.postUrls.size(), is(1));
        assertThat(http.postUrls.getFirst(), containsString(WEBHOOK_CREATE));
    }

    @Test
    void createWebhookPayloadContainsExplicitBasicAuthUserInfoWithoutDiagnosticSecretLeak() throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        setField(api, "httpApi", http);
        setField(api, "rateLimitManager", new RecordingRateLimitManager(0));

        api.registerWebHook("device-id", "https://webhook.site/57b-example", "rachio-test", "test-secret-123",
                "external-id", false, RequestPurpose.INITIALIZATION);

        JsonObject payload = JsonParser.parseString(http.postBodies.getFirst()).getAsJsonObject();
        String payloadUrl = payload.get("url").getAsString();
        String sanitizedUrl = RachioApi.sanitizeWebhookUrlForDiagnostic(payloadUrl);

        assertThat(payloadUrl, is("https://rachio-test:test-secret-123@webhook.site/57b-example"));
        assertThat(new URI(payloadUrl).getRawUserInfo(), is("rachio-test:test-secret-123"));
        assertThat(RachioApi.webhookUrlContainsUserInfo(payloadUrl), is(true));
        assertThat(sanitizedUrl.startsWith("callbackUrlHash="), is(true));
        assertThat(sanitizedUrl.contains("rachio-test"), is(false));
        assertThat(sanitizedUrl.contains("test-secret-123"), is(false));
        assertThat(sanitizedUrl.contains("webhook.site"), is(false));
    }

    @Test
    void createWebhookFailureSanitizesCredentialUrlFromExceptionDiagnostics() throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        String registrationUrl = "https://rachio-test:test-secret-123@webhook.site/57b-example";
        RachioApiResult result = new RachioApiResult();
        result.resultString = "Failed to register " + registrationUrl;
        http.postException = new RachioApiException("Failed to register " + registrationUrl, result);
        setField(api, "httpApi", http);
        setField(api, "rateLimitManager", new RecordingRateLimitManager(0));

        RachioApiException exception = assertThrows(RachioApiException.class,
                () -> api.registerWebHook("device-id", "https://webhook.site/57b-example", "rachio-test",
                        "test-secret-123", "external-id", false, RequestPurpose.INITIALIZATION));

        String message = String.valueOf(exception.getMessage());
        assertThat(message, containsString("cause=RachioApiException"));
        assertThat(message, containsString("callbackUrlHash="));
        assertThat(message.contains("rachio-test"), is(false));
        assertThat(message.contains("test-secret-123"), is(false));
        assertThat(message.contains("webhook.site"), is(false));
        assertThat(exception.getApiResult().resultString.contains("rachio-test"), is(false));
        assertThat(exception.getApiResult().resultString.contains("test-secret-123"), is(false));
        assertThat(exception.getApiResult().resultString.contains("webhook.site"), is(false));
    }

    @Test
    void createWebhookPayloadPercentEncodesSpecialCredentialCharacters() throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        setField(api, "httpApi", http);
        setField(api, "rateLimitManager", new RecordingRateLimitManager(0));

        api.registerWebHook("device-id", "https://webhook.site/57b-example", "rachio test", "test-secret-123:/?@",
                "external-id", false, RequestPurpose.INITIALIZATION);

        JsonObject payload = JsonParser.parseString(http.postBodies.getFirst()).getAsJsonObject();
        String payloadUrl = payload.get("url").getAsString();

        assertThat(payloadUrl, is("https://rachio%20test:test-secret-123%3A%2F%3F%40@webhook.site/57b-example"));
        assertThat(RachioApi.sanitizeWebhookUrlForDiagnostic(payloadUrl).startsWith("callbackUrlHash="), is(true));
    }

    @Test
    void legacyNotificationWebhookBuildsEncodedCredentialUrlWithoutSanitizerLeak() throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        setField(api, "httpApi", http);
        setField(api, "rateLimitManager", new RecordingRateLimitManager(0));

        api.registerLegacyNotificationWebHook("device-id", "https://webhook.site/57b-example", "rachio test",
                "test-secret-123:/?@", "external-id", false, RequestPurpose.INITIALIZATION);

        JsonObject payload = JsonParser.parseString(http.postBodies.getFirst()).getAsJsonObject();
        String payloadUrl = payload.get("url").getAsString();
        String sanitizedUrl = RachioApi.sanitizeWebhookUrlForDiagnostic(payloadUrl);

        assertThat(http.getUrls.getFirst(), containsString(APIURL_DEV_QUERY_WEBHOOK + "/device-id/webhook"));
        assertThat(http.postUrls.getFirst(), containsString(APIURL_DEV_POST_WEBHOOK));
        assertThat(payloadUrl, is("https://rachio%20test:test-secret-123%3A%2F%3F%40@webhook.site/57b-example"));
        assertThat(sanitizedUrl.startsWith("callbackUrlHash="), is(true));
        assertThat(sanitizedUrl.contains("test-secret-123"), is(false));
        assertThat(sanitizedUrl.contains("webhook.site"), is(false));
        assertThat(payload.getAsJsonObject("device").get("id").getAsString(), is("device-id"));
        assertThat(payload.getAsJsonArray("eventTypes").size(), is(9));
    }

    @Test
    void clearAllLegacyNotificationWebhooksDeletesDeviceCallbacksThenRegistersAgain() throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        http.legacyWebhookListJson = """
                [
                  {"id":"hook-1","url":"https://other.example/hook","externalId":"other"},
                  {"id":"hook-2","url":"https://host.example/rachio/webhook","externalId":"external-id"}
                ]
                """;
        setField(api, "httpApi", http);
        setField(api, "rateLimitManager", new RecordingRateLimitManager(0));

        api.registerLegacyNotificationWebHook("device-id", "https://host.example/rachio/webhook", "", "", "external-id",
                true, RequestPurpose.INITIALIZATION);

        assertThat(http.deleteUrls, contains(APIURL_BASE + APIURL_DEV_DELETE_WEBHOOK + "/hook-1",
                APIURL_BASE + APIURL_DEV_DELETE_WEBHOOK + "/hook-2"));
        assertThat(http.postUrls.size(), is(1));
    }

    @Test
    void legacyNotificationEventTypeEndpointUsesRequestThrottling() throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        RecordingRateLimitManager rateLimitManager = new RecordingRateLimitManager(0);
        setField(api, "httpApi", http);
        setField(api, "rateLimitManager", rateLimitManager);

        api.listLegacyNotificationEventTypes(RequestPurpose.INITIALIZATION);

        assertThat(http.getUrls.getFirst(), containsString(APIURL_DEV_WEBHOOK_EVENT_TYPES));
        assertThat(rateLimitManager.requestPurposes, contains(RequestPurpose.INITIALIZATION));
    }

    @Test
    void registerWebhookListLookupLocalThrottlePropagatesForDeferredHandlerRetry() throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        RecordingRateLimitManager rateLimitManager = new RecordingRateLimitManager(2);
        setField(api, "httpApi", http);
        setField(api, "rateLimitManager", rateLimitManager);

        RachioApiThrottledException exception = assertThrows(RachioApiThrottledException.class,
                () -> api.registerWebHook("device-id", "https://host.example/rachio/webhook", "", "", "external-id",
                        false, RequestPurpose.INITIALIZATION));

        assertThat(exception.getRequestPurpose(), is(RequestPurpose.INITIALIZATION));
        assertThat(http.getUrls.size(), is(1));
        assertThat(http.getUrls.getFirst(), containsString(WEBHOOK_LIST_EVENT_TYPES));
        assertThat(http.postUrls.isEmpty(), is(true));
    }

    @Test
    void serverSideWebhookRateLimitIsNotTreatedAsLocalThrottleFallback() throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        http.eventTypeLookupResponseCode = HttpStatus.TOO_MANY_REQUESTS_429;
        setField(api, "httpApi", http);
        setField(api, "rateLimitManager", new RecordingRateLimitManager(0));

        RachioApiException exception = assertThrows(RachioApiException.class, () -> api.registerWebHook("device-id",
                "https://host.example/rachio/webhook", "", "", "external-id", false, RequestPurpose.INITIALIZATION));

        assertThat(exception instanceof RachioApiThrottledException, is(false));
        assertThat(exception.getApiResult().isResponseRateLimit(), is(true));
        assertThat(http.postUrls.isEmpty(), is(true));
    }

    private RachioApiWebHookEntry webhook(String url, String externalId, String irrigationControllerId,
            List<String> eventTypes) {
        RachioApiWebHookEntry webhook = new RachioApiWebHookEntry();
        webhook.url = url;
        webhook.externalId = externalId;
        RachioApiWebHookResourceId resourceId = new RachioApiWebHookResourceId();
        resourceId.irrigationControllerId = irrigationControllerId;
        webhook.resourceId = resourceId;
        webhook.eventTypes.addAll(eventTypes);
        return webhook;
    }

    private static void setField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private List<OfficialSignatureSample> officialSignatureSamples() {
        return List.of(FREEZE_SKIP_SAMPLE, PROGRAM_RAIN_SKIP_SAMPLE, VALVE_RUN_START_SAMPLE);
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private String alterEventId(String body) {
        JsonObject object = JsonParser.parseString(body).getAsJsonObject();
        object.addProperty("eventId", "00000000-0000-0000-0000-000000000000");
        return object.toString();
    }

    private String alterResourceId(String body) {
        JsonObject object = JsonParser.parseString(body).getAsJsonObject();
        object.addProperty("resourceId", "00000000-0000-0000-0000-000000000000");
        return object.toString();
    }

    private record OfficialSignatureSample(String eventType, String apiToken, String body, String signature) {
    }

    private static class RecordingRateLimitManager extends ClientRateLimitManager {
        private final int throwOnCall;
        private int callCount;
        private final List<RequestPurpose> requestPurposes = new ArrayList<>();

        RecordingRateLimitManager(int throwOnCall) {
            super(10, Duration.ofSeconds(30));
            this.throwOnCall = throwOnCall;
        }

        @Override
        public void tryThrottle(ClientRateLimitManager.PRIORITY priority, RequestPurpose requestPurpose)
                throws ClientRateLimitManager.RateLimitThrottleException {
            callCount++;
            requestPurposes.add(requestPurpose);
            if (callCount == throwOnCall) {
                throw new ClientRateLimitManager.RateLimitThrottleException(priority, requestPurpose, 0.1, 0.2);
            }
        }
    }

    private static class RecordingRachioHttp extends RachioHttp {
        private final List<String> getUrls = new ArrayList<>();
        private final List<String> postUrls = new ArrayList<>();
        private final List<String> postBodies = new ArrayList<>();
        private final List<String> deleteUrls = new ArrayList<>();
        private @Nullable RachioApiException postException;
        private int eventTypeLookupResponseCode = HttpStatus.OK_200;
        private String legacyWebhookListJson = "[]";

        @Override
        public RachioApiResult httpGet(String url, @Nullable String urlParameters) throws RachioApiException {
            getUrls.add(url);
            RachioApiResult result = new RachioApiResult();
            result.url = url;
            result.requestMethod = "GET";
            result.responseCode = eventTypeLookupResponseCode;
            if (url.contains(WEBHOOK_LIST_EVENT_TYPES)) {
                if (eventTypeLookupResponseCode == HttpStatus.TOO_MANY_REQUESTS_429) {
                    throw new RachioApiException("Webhook event type lookup rate limited", result);
                }
                result.resultString = """
                        {
                          "eventTypes": [
                            {
                              "resourceType": "IRRIGATION_CONTROLLER",
                              "eventTypes": [ "SCHEDULE_STARTED_EVENT" ]
                            }
                          ]
                        }
                        """;
            } else if (url.contains(APIURL_DEV_WEBHOOK_EVENT_TYPES)) {
                result.resultString = """
                        [
                          {"id":"5","type":"DEVICE_STATUS","description":"Device status"}
                        ]
                        """;
            } else if (url.contains(APIURL_DEV_QUERY_WEBHOOK)) {
                result.resultString = legacyWebhookListJson;
            } else {
                result.resultString = "{ \"webhooks\": [] }";
            }
            return result;
        }

        @Override
        public RachioApiResult httpPost(String url, String postData) throws RachioApiException {
            RachioApiException exception = postException;
            if (exception != null) {
                throw exception;
            }
            postUrls.add(url);
            postBodies.add(postData);
            RachioApiResult result = new RachioApiResult();
            result.url = url;
            result.requestMethod = "POST";
            result.responseCode = HttpStatus.CREATED_201;
            result.resultString = "{}";
            return result;
        }

        @Override
        public RachioApiResult httpDelete(String url, @Nullable String urlParameters) {
            deleteUrls.add(url);
            RachioApiResult result = new RachioApiResult();
            result.url = url;
            result.requestMethod = "DELETE";
            result.responseCode = HttpStatus.NO_CONTENT_204;
            result.resultString = "";
            return result;
        }
    }
}
