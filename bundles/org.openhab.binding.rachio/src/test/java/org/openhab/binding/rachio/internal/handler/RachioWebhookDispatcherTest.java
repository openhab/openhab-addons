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
package org.openhab.binding.rachio.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_DEVICE_ZONE_RUN_STARTED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_PROGRAM_RAIN_SKIP_CREATED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_VALVE_RUN_START;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO.RachioWebhookPayload;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;

/**
 * Tests generic webhook dispatcher routing.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioWebhookDispatcherTest {
    @Test
    void irrigationEventRoutesThroughResourceAwareDispatcher() {
        RecordingHandler irrigationHandler = new RecordingHandler(RachioWebhookResourceType.IRRIGATION_CONTROLLER);
        RachioWebhookDispatcher dispatcher = new RachioWebhookDispatcher(List.of(irrigationHandler));
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.resourceType = "IRRIGATION_CONTROLLER";
        event.resourceId = "device-id";
        event.eventType = EVENT_DEVICE_ZONE_RUN_STARTED;

        assertThat(dispatcher.dispatch(event), is(true));
        assertThat(irrigationHandler.handled, is(true));
    }

    @Test
    void legacyIrrigationEventRoutesByDeviceId() {
        RecordingHandler irrigationHandler = new RecordingHandler(RachioWebhookResourceType.IRRIGATION_CONTROLLER);
        RachioWebhookDispatcher dispatcher = new RachioWebhookDispatcher(List.of(irrigationHandler));
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.deviceId = "device-id";
        event.type = "ZONE_STATUS";

        assertThat(dispatcher.dispatch(event), is(true));
        assertThat(irrigationHandler.handled, is(true));
    }

    @Test
    void valveEventRoutesByResourceType() {
        RecordingHandler valveHandler = new RecordingHandler(RachioWebhookResourceType.VALVE);
        RachioWebhookDispatcher dispatcher = new RachioWebhookDispatcher(List.of(valveHandler));
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.resourceType = "VALVE";
        event.resourceId = "valve-id";
        event.eventType = EVENT_VALVE_RUN_START;

        assertThat(dispatcher.dispatch(event), is(true));
        assertThat(valveHandler.handled, is(true));
    }

    @Test
    void programEventRoutesByResourceTypeAndPayloadProgramId() {
        RecordingHandler programHandler = new RecordingHandler(RachioWebhookResourceType.PROGRAM);
        RachioWebhookDispatcher dispatcher = new RachioWebhookDispatcher(List.of(programHandler));
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.resourceType = "PROGRAM";
        event.eventType = EVENT_PROGRAM_RAIN_SKIP_CREATED;
        RachioWebhookPayload payload = new RachioWebhookPayload();
        payload.programId = "program-id";
        event.payload = payload;
        event.normalize();

        assertThat(dispatcher.dispatch(event), is(true));
        assertThat(programHandler.handled, is(true));
        assertThat(event.resourceId, is("program-id"));
    }

    @Test
    void unknownResourceTypeDoesNotCrashDispatcher() {
        RecordingHandler irrigationHandler = new RecordingHandler(RachioWebhookResourceType.IRRIGATION_CONTROLLER);
        RachioWebhookDispatcher dispatcher = new RachioWebhookDispatcher(List.of(irrigationHandler));
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.resourceType = "FUTURE_RESOURCE";
        event.eventType = "FUTURE_EVENT";

        assertThat(dispatcher.dispatch(event), is(false));
        assertThat(irrigationHandler.handled, is(false));
    }

    private static class RecordingHandler implements RachioWebhookEventHandler {
        private final RachioWebhookResourceType resourceType;
        private boolean handled = false;

        RecordingHandler(RachioWebhookResourceType resourceType) {
            this.resourceType = resourceType;
        }

        @Override
        public boolean supports(RachioWebhookResourceType resourceType) {
            return this.resourceType == resourceType;
        }

        @Override
        public boolean handle(RachioEventGsonDTO event) {
            handled = true;
            return true;
        }
    }
}
