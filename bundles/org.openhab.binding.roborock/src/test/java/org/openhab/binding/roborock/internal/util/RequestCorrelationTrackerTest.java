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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
}
