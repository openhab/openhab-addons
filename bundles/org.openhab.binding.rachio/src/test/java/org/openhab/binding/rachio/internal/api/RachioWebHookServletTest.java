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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_WEBHOOK_PATH;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.RachioHandlerFactory;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;

/**
 * Tests servlet-level Rachio webhook duplicate handling.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioWebHookServletTest {
    private static final String VALID_SIGNATURE = "valid-signature";
    private static final String INVALID_SIGNATURE = "invalid-signature";

    @Test
    void duplicateWebhookEventIsAcknowledgedButRoutedOnce() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = eventJson("abc");

        HttpServletResponse firstResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), firstResponse);
        HttpServletResponse duplicateResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), duplicateResponse);

        verify(handlerFactory, times(1)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
        verify(firstResponse).setStatus(HttpServletResponse.SC_OK);
        verify(duplicateResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void failedWebhookProcessingLeavesEventRetryable() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        when(handlerFactory.webHookEvent(anyString(), any(RachioEventGsonDTO.class)))
                .thenThrow(new RuntimeException("simulated failure")).thenReturn(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = eventJson("abc");

        HttpServletResponse failedResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), failedResponse);
        HttpServletResponse retryResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), retryResponse);

        verify(handlerFactory, times(2)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
        verify(failedResponse).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(retryResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void unroutedWebhookProcessingLeavesEventRetryable() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        when(handlerFactory.webHookEvent(anyString(), any(RachioEventGsonDTO.class))).thenReturn(false, true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = eventJson("abc");

        HttpServletResponse failedResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), failedResponse);
        HttpServletResponse retryResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), retryResponse);

        verify(handlerFactory, times(2)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
        verify(failedResponse).setStatus(HttpServletResponse.SC_OK);
        verify(retryResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void webhookEventWithoutEventIdIsAlwaysRouted() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = eventJsonWithoutEventId();

        servlet.service(request(body, VALID_SIGNATURE), response());
        servlet.service(request(body, VALID_SIGNATURE), response());

        verify(handlerFactory, times(2)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
    }

    @Test
    void differentWebhookEventIdsAreRoutedNormally() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);

        servlet.service(request(eventJson("event-1"), VALID_SIGNATURE), response());
        servlet.service(request(eventJson("event-2"), VALID_SIGNATURE), response());

        verify(handlerFactory, times(2)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
    }

    @Test
    void signedModernWebhookEventIsNormalizedBeforeDispatch() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        ArgumentCaptor<RachioEventGsonDTO> eventCaptor = ArgumentCaptor.forClass(RachioEventGsonDTO.class);

        servlet.service(request(eventJson("event-1"), VALID_SIGNATURE), response());

        verify(handlerFactory).webHookEvent(eq("127.0.0.1"), eventCaptor.capture());
        RachioEventGsonDTO event = eventCaptor.getValue();
        assertThat(event.type, is("ZONE_STATUS"));
        assertThat(event.subType, is("ZONE_STARTED"));
        assertThat(event.deviceId, is("controller-id"));
        assertThat(event.getZoneNumberForWebhookHandling(), is(1));
        assertThat(event.duration, is(60));
    }

    @Test
    void invalidSignatureDoesNotPoisonDuplicateCache() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        when(handlerFactory.isValidWebHookSignature(anyString(), any(byte[].class), any(RachioEventGsonDTO.class)))
                .thenReturn(false, true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = eventJson("event-1");

        HttpServletResponse invalidResponse = response();
        servlet.service(request(body, INVALID_SIGNATURE), invalidResponse);
        HttpServletResponse validResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), validResponse);

        verify(invalidResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(validResponse).setStatus(HttpServletResponse.SC_OK);
        verify(handlerFactory, times(1)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
    }

    @Test
    void invalidModernSignatureDoesNotSuppressLaterLegacyNotification() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);

        HttpServletResponse invalidResponse = response();
        servlet.service(request(eventJson("event-1"), INVALID_SIGNATURE), invalidResponse);
        HttpServletResponse legacyResponse = response();
        servlet.service(request(legacyEventJson("external-id"), null), legacyResponse);

        verify(invalidResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(legacyResponse).setStatus(HttpServletResponse.SC_OK);
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
        verify(handlerFactory, times(1)).legacyWebHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
    }

    @Test
    void duplicateDetectionRunsAfterSignatureValidation() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);

        servlet.service(request(eventJson("event-1"), INVALID_SIGNATURE), response());

        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void validLegacyNotificationEventWithoutSignatureIsAccepted() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();

        servlet.service(request(legacyEventJson("external-id"), null), response);

        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).isValidWebHookSignature(anyString(), any(byte[].class));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void legacyScheduleStatusWithAmbiguousHintsWithoutSignatureIsAccepted() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        ArgumentCaptor<RachioEventGsonDTO> eventCaptor = ArgumentCaptor.forClass(RachioEventGsonDTO.class);
        HttpServletResponse response = response();

        servlet.service(request(weakModernHintLegacyEventJson("SCHEDULE_STATUS", "SCHEDULE_COMPLETED",
                "SCHEDULE_COMPLETED_EVENT", "DEVICE"), null), response);

        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), eventCaptor.capture());
        RachioEventGsonDTO event = eventCaptor.getValue();
        assertThat(event.type, is("SCHEDULE_STATUS"));
        assertThat(event.subType, is("SCHEDULE_COMPLETED"));
        assertThat(event.eventType, is("SCHEDULE_COMPLETED_EVENT"));
        assertThat(event.resourceType, is("DEVICE"));
        assertThat(event.resourceId, is("controller-id"));
        assertThat(event.timestamp, is("2026-06-23T18:00:00Z"));
        assertThat(event.eventId, is(""));
        assertThat(event.payload == null, is(true));
        verify(handlerFactory, never()).isValidWebHookSignature(anyString(), any(byte[].class),
                any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void legacyZoneStatusWithAmbiguousHintsWithoutSignatureIsAccepted() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        ArgumentCaptor<RachioEventGsonDTO> eventCaptor = ArgumentCaptor.forClass(RachioEventGsonDTO.class);
        HttpServletResponse response = response();

        servlet.service(request(
                weakModernHintLegacyEventJson("ZONE_STATUS", "ZONE_STARTED", "DEVICE_ZONE_RUN_STARTED_EVENT", "DEVICE"),
                null), response);

        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), eventCaptor.capture());
        RachioEventGsonDTO event = eventCaptor.getValue();
        assertThat(event.type, is("ZONE_STATUS"));
        assertThat(event.subType, is("ZONE_STARTED"));
        assertThat(event.eventType, is("DEVICE_ZONE_RUN_STARTED_EVENT"));
        assertThat(event.resourceType, is("DEVICE"));
        assertThat(event.resourceId, is("controller-id"));
        assertThat(event.timestamp, is("2026-06-23T18:00:00Z"));
        assertThat(event.eventId, is(""));
        assertThat(event.payload == null, is(true));
        verify(handlerFactory, never()).isValidWebHookSignature(anyString(), any(byte[].class),
                any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void unsignedEventIdMixedPayloadWithLegacyIdentifiersIsRejected() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();

        servlet.service(request(
                eventIdStrongMixedLegacyEventJson("ZONE_STATUS", "ZONE_STARTED", "DEVICE_ZONE_RUN_STARTED_EVENT"),
                null), response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory, never()).legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void stringifiedLegacyZoneRunStatusDoesNotMaskTopLevelZoneData() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        ArgumentCaptor<RachioEventGsonDTO> eventCaptor = ArgumentCaptor.forClass(RachioEventGsonDTO.class);
        String body = """
                {
                  "type": "ZONE_STATUS",
                  "subType": "ZONE_STARTED",
                  "category": "ZONE",
                  "deviceId": "controller-id",
                  "externalId": "external-id",
                  "zoneNumber": 7,
                  "zoneRunStatus": "{\\"duration\\":120,\\"zoneNumber\\":0,\\"state\\":\\"\\"}"
                }
                """;

        servlet.service(request(body, null), response());

        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), eventCaptor.capture());
        RachioEventGsonDTO event = eventCaptor.getValue();
        assertThat(event.getZoneNumberForWebhookHandling(), is(7));
        assertThat(event.getZoneRunStateForWebhookHandling(), is("ZONE_STARTED"));
        verify(handlerFactory, never()).isValidWebHookSignature(anyString(), any(byte[].class));
    }

    @Test
    void legacyShortZoneRunStatesAreNormalizedForWebhookHandling() {
        RachioEventGsonDTO event = new RachioEventGsonDTO();

        event.zoneRunState = "STARTED";
        assertThat(event.getZoneRunStateForWebhookHandling(), is("ZONE_STARTED"));

        event.zoneRunState = "STOPPED";
        assertThat(event.getZoneRunStateForWebhookHandling(), is("ZONE_STOPPED"));

        event.zoneRunState = "COMPLETED";
        assertThat(event.getZoneRunStateForWebhookHandling(), is("ZONE_COMPLETED"));
    }

    @Test
    void invalidSignedEventIdMixedPayloadWithLegacyIdentifiersIsRejected() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();

        servlet.service(request(
                eventIdStrongMixedLegacyEventJson("SCHEDULE_STATUS", "SCHEDULE_COMPLETED", "SCHEDULE_COMPLETED_EVENT"),
                INVALID_SIGNATURE), response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory).isValidWebHookSignature(eq(INVALID_SIGNATURE), any(byte[].class),
                any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void validSignedEventIdMixedPayloadWithLegacyIdentifiersProcessesAsModern() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();

        servlet.service(request(
                eventIdStrongMixedLegacyEventJson("SCHEDULE_STATUS", "SCHEDULE_COMPLETED", "SCHEDULE_COMPLETED_EVENT"),
                VALID_SIGNATURE), response);

        verify(handlerFactory).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void unsignedPayloadObjectMixedPayloadWithLegacyIdentifiersIsRejected() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();

        servlet.service(request(
                payloadObjectStrongMixedLegacyEventJson("ZONE_STATUS", "ZONE_STARTED", "DEVICE_ZONE_RUN_STARTED_EVENT"),
                null), response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory, never()).legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void invalidSignedPayloadObjectMixedPayloadWithLegacyIdentifiersIsRejected() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();

        servlet.service(request(payloadObjectStrongMixedLegacyEventJson("SCHEDULE_STATUS", "SCHEDULE_COMPLETED",
                "SCHEDULE_COMPLETED_EVENT"), INVALID_SIGNATURE), response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory).isValidWebHookSignature(eq(INVALID_SIGNATURE), any(byte[].class),
                any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void numericLegacyZoneStatusTypeIsNormalizedAndAcceptedWithoutSignature() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        ArgumentCaptor<RachioEventGsonDTO> eventCaptor = ArgumentCaptor.forClass(RachioEventGsonDTO.class);

        servlet.service(request(legacyEventJson("\"10\"", "\"subType\": \"ZONE_STARTED\","), null), response());

        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().type, is("ZONE_STATUS"));
        assertThat(eventCaptor.getValue().subType, is("ZONE_STARTED"));
        verify(handlerFactory, never()).isValidWebHookSignature(anyString(), any(byte[].class));
    }

    @Test
    void numericLegacyScheduleStatusTypeIsNormalizedAndAcceptedWithoutSignature() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        ArgumentCaptor<RachioEventGsonDTO> eventCaptor = ArgumentCaptor.forClass(RachioEventGsonDTO.class);

        servlet.service(request(legacyEventJson("9", "\"subType\": \"SCHEDULE_COMPLETED\","), null), response());

        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().type, is("SCHEDULE_STATUS"));
    }

    @Test
    void numericLegacyRainSensorTypeIsNormalizedAndAcceptedWithoutSignature() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        ArgumentCaptor<RachioEventGsonDTO> eventCaptor = ArgumentCaptor.forClass(RachioEventGsonDTO.class);

        servlet.service(request(legacyEventJson("11", "\"subType\": \"RAIN_SENSOR_DETECTION_ON\","), null), response());

        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().type, is("RAIN_SENSOR_DETECTION"));
        assertThat(eventCaptor.getValue().subType, is("RAIN_SENSOR_DETECTION_ON"));
        verify(handlerFactory, never()).isValidWebHookSignature(anyString(), any(byte[].class));
    }

    @Test
    void lowercaseSubtypeFieldIsAcceptedForLegacyEvent() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        ArgumentCaptor<RachioEventGsonDTO> eventCaptor = ArgumentCaptor.forClass(RachioEventGsonDTO.class);

        servlet.service(request(legacyEventJson("\"ZONE_STATUS\"", "\"subtype\": \"ZONE_STARTED\","), null),
                response());

        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().subType, is("ZONE_STARTED"));
    }

    @Test
    void eventSubTypeFieldIsAcceptedForLegacyEvent() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        ArgumentCaptor<RachioEventGsonDTO> eventCaptor = ArgumentCaptor.forClass(RachioEventGsonDTO.class);

        servlet.service(request(legacyEventJson("\"ZONE_STATUS\"", "\"eventSubType\": \"ZONE_STARTED\","), null),
                response());

        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().subType, is("ZONE_STARTED"));
    }

    @Test
    void knownLegacyTypeWithoutSubtypeIsAcceptedWhenIdentifiersArePresent() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);

        servlet.service(request(legacyEventJson("\"DEVICE_STATUS\"", ""), null), response());

        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).isValidWebHookSignature(anyString(), any(byte[].class));
    }

    @Test
    void knownLegacyTypeWithoutExternalIdIsRejectedWhenUnsigned() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = legacyEventJson("\"DEVICE_STATUS\"", "").replace("\"externalId\": \"external-id\",", "");
        HttpServletResponse response = response();

        servlet.service(request(body, null), response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory, never()).legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void knownLegacyTypeWithoutDeviceIdIsRejectedWhenUnsigned() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = legacyEventJson("\"DEVICE_STATUS\"", "").replace("\"deviceId\": \"controller-id\",", "");
        HttpServletResponse response = response();

        servlet.service(request(body, null), response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory, never()).legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void legacyNotificationEventWithWrongExternalIdIsRejected() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        when(handlerFactory.legacyWebHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class))).thenReturn(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();
        String body = legacyEventJson("wrong-external-id");

        servlet.service(request(body, null), response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void newWebhookServiceEventWithoutSignatureIsRejected() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();

        servlet.service(request(eventJson("event-1"), null), response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory, never()).legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void recognizedLegacyTypeWithoutLegacyDeviceIdCannotBypassNewWebhookSignature() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();
        String body = eventJson("event-1").replace("\"eventId\": \"event-1\",",
                "\"eventId\": \"event-1\", \"type\": \"ZONE_STATUS\",");

        servlet.service(request(body, null), response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory, never()).legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void unknownUnsignedLegacyPayloadIsRejected() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();

        servlet.service(request(legacyEventJson("external-id").replace("ZONE_STATUS", "FUTURE_STATUS"), null),
                response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory, never()).legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void unsignedPayloadWithWebhookMarkersAndUnknownTypeIsRejected() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();
        String body = eventIdStrongMixedLegacyEventJson("FUTURE_STATUS", "FUTURE_STARTED", "FUTURE_EVENT");

        servlet.service(request(body, null), response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory, never()).legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void legacyNotificationEventWithUnknownDeviceIdIsRejected() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        when(handlerFactory.legacyWebHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class))).thenReturn(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        HttpServletResponse response = response();
        String body = legacyEventJson("external-id").replace("controller-id", "unknown-controller");

        servlet.service(request(body, null), response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(handlerFactory).legacyWebHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    @Test
    void legacyClassificationSummaryDoesNotExposePayloadOrCredentials() {
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.type = "ZONE_STATUS";
        event.title = "callback-password";
        event.subType = "payload-secret";
        event.deviceId = "controller-secret";
        event.externalId = "external-id-secret";
        event.eventType = "event-type-secret";
        event.resourceType = "resource-type-secret";
        event.eventId = "event-id-secret";
        event.resourceId = "resource-id-secret";
        event.timestamp = "timestamp-secret";
        event.payload = new RachioEventGsonDTO.RachioWebhookPayload();

        String summary = RachioWebHookServlet.describeLegacyClassification(event);

        assertThat(summary, containsString("legacyTypeRecognized=true"));
        assertThat(summary, containsString("type='ZONE_STATUS'"));
        assertThat(summary, containsString("eventIdPresent=true"));
        assertThat(summary, containsString("resourceIdPresent=true"));
        assertThat(summary, containsString("timestampPresent=true"));
        assertThat(summary, containsString("payloadPresent=true"));
        assertThat(summary, containsString("eventTypePresent=true"));
        assertThat(summary, containsString("resourceTypePresent=true"));
        assertThat(summary, containsString("subTypePresent=true"));
        assertThat(summary, containsString("deviceIdPresent=true"));
        assertThat(summary, containsString("externalIdPresent=true"));
        assertThat(summary, not(containsString("callback-password")));
        assertThat(summary, not(containsString("payload-secret")));
        assertThat(summary, not(containsString("controller-secret")));
        assertThat(summary, not(containsString("external-id-secret")));
        assertThat(summary, not(containsString("event-type-secret")));
        assertThat(summary, not(containsString("resource-type-secret")));
        assertThat(summary, not(containsString("event-id-secret")));
        assertThat(summary, not(containsString("resource-id-secret")));
        assertThat(summary, not(containsString("timestamp-secret")));
    }

    @Test
    void webhookEventDescriptionDoesNotExposeExternalId() {
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.eventId = "event-id";
        event.eventType = "DEVICE_ZONE_RUN_STARTED_EVENT";
        event.resourceType = "IRRIGATION_CONTROLLER";
        event.resourceId = "controller-id";
        event.deviceId = "controller-id";
        event.externalId = "external-id-secret";

        String summary = RachioWebHookServlet.describeEvent(event);

        assertThat(summary, containsString("externalIdPresent=true"));
        assertThat(summary, not(containsString("external-id-secret")));
    }

    private RachioWebHookServlet servlet(RachioHandlerFactory handlerFactory) throws Exception {
        return new RachioWebHookServlet(handlerFactory);
    }

    private RachioHandlerFactory mockHandlerFactory(boolean validSignature) {
        RachioHandlerFactory handlerFactory = Mockito.mock(RachioHandlerFactory.class);
        when(handlerFactory.isValidWebHookSignature(anyString(), any(byte[].class))).thenReturn(validSignature);
        when(handlerFactory.isValidWebHookSignature(anyString(), any(byte[].class), any(RachioEventGsonDTO.class)))
                .thenReturn(validSignature);
        when(handlerFactory.webHookEvent(anyString(), any(RachioEventGsonDTO.class))).thenReturn(true);
        when(handlerFactory.legacyWebHookEvent(anyString(), any(RachioEventGsonDTO.class))).thenReturn(true);
        return handlerFactory;
    }

    private HttpServletRequest request(String body, @Nullable String signature) throws IOException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(SERVLET_WEBHOOK_PATH);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(443);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8443);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getHeader("x-signature")).thenReturn(signature);
        when(request.getInputStream())
                .thenReturn(new ByteArrayServletInputStream(body.getBytes(StandardCharsets.UTF_8)));
        return request;
    }

    private HttpServletResponse response() throws IOException {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        return response;
    }

    private String eventJson(String eventId) {
        return """
                {
                  "eventId": "%s",
                  "eventType": "DEVICE_ZONE_RUN_STARTED_EVENT",
                  "resourceType": "IRRIGATION_CONTROLLER",
                  "resourceId": "controller-id",
                  "externalId": "external-id",
                  "payload": {
                    "zoneNumber": "1",
                    "zoneName": "Front Yard",
                    "durationSeconds": "60"
                  }
                }
                """.formatted(eventId);
    }

    private String eventJsonWithoutEventId() {
        return """
                {
                  "eventType": "DEVICE_ZONE_RUN_STARTED_EVENT",
                  "resourceType": "IRRIGATION_CONTROLLER",
                  "resourceId": "controller-id",
                  "externalId": "external-id",
                  "payload": {
                    "zoneNumber": "1",
                    "zoneName": "Front Yard",
                    "durationSeconds": "60"
                  }
                }
                """;
    }

    private String legacyEventJson(String externalId) {
        return """
                {
                  "type": "ZONE_STATUS",
                  "subType": "ZONE_STARTED",
                  "category": "ZONE",
                  "deviceId": "controller-id",
                  "externalId": "%s",
                  "zoneNumber": 7
                }
                """.formatted(externalId);
    }

    private String legacyEventJson(String type, String subtypeMember) {
        return """
                {
                  "type": %s,
                  %s
                  "category": "ZONE",
                  "deviceId": "controller-id",
                  "externalId": "external-id",
                  "zoneNumber": 7
                }
                """.formatted(type, subtypeMember);
    }

    private String weakModernHintLegacyEventJson(String type, String subType, String eventType, String resourceType) {
        return """
                {
                  "type": "%s",
                  "eventType": "%s",
                  "subType": "%s",
                  "deviceId": "controller-id",
                  "externalId": "external-id",
                  "resourceId": "controller-id",
                  "resourceType": "%s",
                  "timestamp": "2026-06-23T18:00:00Z",
                  "zoneNumber": 7
                }
                """.formatted(type, eventType, subType, resourceType);
    }

    private String eventIdStrongMixedLegacyEventJson(String type, String subType, String eventType) {
        return """
                {
                  "eventId": "event-id",
                  "integrationState": "WATERING",
                  "resourceId": "controller-id",
                  "routingId": "controller-id",
                  "type": "%s",
                  "title": "Legacy notification",
                  "deviceId": "controller-id",
                  "zoneNumber": 7,
                  "zoneId": "zone-id",
                  "externalId": "external-id",
                  "eventType": "%s",
                  "subType": "%s",
                  "category": "DEVICE",
                  "resourceType": "DEVICE"
                }
                """.formatted(type, eventType, subType);
    }

    private String payloadObjectStrongMixedLegacyEventJson(String type, String subType, String eventType) {
        return """
                {
                  "integrationState": "WATERING",
                  "resourceId": "controller-id",
                  "routingId": "controller-id",
                  "type": "%s",
                  "title": "Legacy notification",
                  "deviceId": "controller-id",
                  "zoneNumber": 7,
                  "zoneId": "zone-id",
                  "externalId": "external-id",
                  "eventType": "%s",
                  "subType": "%s",
                  "category": "DEVICE",
                  "resourceType": "DEVICE",
                  "payload": {
                    "zoneNumber": "7",
                    "zoneName": "Front lawn"
                  }
                }
                """.formatted(type, eventType, subType);
    }

    private static class ByteArrayServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        ByteArrayServletInputStream(byte[] body) {
            inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(@Nullable ReadListener readListener) {
        }
    }
}
