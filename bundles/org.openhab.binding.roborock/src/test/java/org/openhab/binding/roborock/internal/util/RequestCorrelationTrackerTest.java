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
package org.openhab.binding.roborock.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault({})
class RequestCorrelationTrackerTest {

    @Test
    void trackerCorrelatesRequestIdAndMethodAndExpires() {
        RequestCorrelationTracker tracker = new RequestCorrelationTracker();

        tracker.register("getMap", 12345);
        assertEquals("getMap", tracker.findMethodByRequestId(12345));
        assertTrue(tracker.isRequestIdInUse(12345));

        tracker.cleanupExpired(0);
        assertNull(tracker.findMethodByRequestId(12345));
    }

    @Test
    void trackerKeepsMultipleRequestIdsForSameMethod() {
        RequestCorrelationTracker tracker = new RequestCorrelationTracker();

        tracker.register("getMap", 11111);
        tracker.register("getMap", 22222);

        assertEquals("getMap", tracker.findMethodByRequestId(11111));
        assertEquals("getMap", tracker.findMethodByRequestId(22222));
    }

    @Test
    void trackerRemoveByMethodRemovesAllRequestIdsForMethod() {
        RequestCorrelationTracker tracker = new RequestCorrelationTracker();

        tracker.register("getMap", 11111);
        tracker.register("getMap", 22222);

        tracker.removeByMethod("getMap");

        assertNull(tracker.findMethodByRequestId(11111));
        assertNull(tracker.findMethodByRequestId(22222));
    }

    @Test
    void trackerCanRemoveByRequestId() {
        RequestCorrelationTracker tracker = new RequestCorrelationTracker();

        tracker.register("getMap", 33333);
        tracker.removeByRequestId(33333);

        assertNull(tracker.findMethodByRequestId(33333));
    }

    @Test
    void cleanupExpiredRemovesOnlyStaleRequests() throws InterruptedException {
        RequestCorrelationTracker tracker = new RequestCorrelationTracker();

        tracker.register("stale", 44444);
        Thread.sleep(20);
        tracker.register("fresh", 55555);

        tracker.cleanupExpired(10);

        assertNull(tracker.findMethodByRequestId(44444));
        assertEquals("fresh", tracker.findMethodByRequestId(55555));
        assertFalse(tracker.isRequestIdInUse(44444));
        assertTrue(tracker.isRequestIdInUse(55555));
    }

    @Test
    void isMapRelatedMethodIdentifiesMapMethods() {
        assertTrue(RequestCorrelationTracker.isMapRelatedMethod("getMap"));
        assertTrue(RequestCorrelationTracker.isMapRelatedMethod("get_map_v1"));
        assertTrue(RequestCorrelationTracker.isMapRelatedMethod("mapDownload"));
        assertFalse(RequestCorrelationTracker.isMapRelatedMethod("getStatus"));
        assertFalse(RequestCorrelationTracker.isMapRelatedMethod("get_consumable"));
        assertFalse(RequestCorrelationTracker.isMapRelatedMethod(null));
    }

    @Test
    void preRegistrationAllowsImmediateResponseCorrelation() {
        RequestCorrelationTracker tracker = new RequestCorrelationTracker();

        // Simulate pre-registration before send (to avoid race condition)
        int requestId = 12345;
        tracker.register("getMap", requestId);

        // Verify correlation is immediately available for response handling
        assertTrue(tracker.isRequestIdInUse(requestId));
        assertEquals("getMap", tracker.findMethodByRequestId(requestId));
    }

    @Test
    void cleanupOnSendFailureRemovesPreRegisteredCorrelation() {
        RequestCorrelationTracker tracker = new RequestCorrelationTracker();

        // Simulate pre-registration before send
        int requestId = 12345;
        tracker.register("getMap", requestId);
        assertTrue(tracker.isRequestIdInUse(requestId));

        // Simulate send failure - cleanup should remove correlation
        tracker.removeByRequestId(requestId);

        // Verify correlation is cleaned up
        assertFalse(tracker.isRequestIdInUse(requestId));
        assertNull(tracker.findMethodByRequestId(requestId));
    }

    @Test
    void doubleRegistrationDoesNotCorruptState() {
        RequestCorrelationTracker tracker = new RequestCorrelationTracker();

        // First registration
        tracker.register("getMap", 12345);

        // Attempt double registration (should be idempotent or handled gracefully)
        tracker.register("getMap", 12345);

        // State should remain consistent
        assertEquals("getMap", tracker.findMethodByRequestId(12345));
        assertTrue(tracker.isRequestIdInUse(12345));

        // Cleanup should work correctly
        tracker.removeByRequestId(12345);
        assertFalse(tracker.isRequestIdInUse(12345));
    }
}
