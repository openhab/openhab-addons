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
package org.openhab.binding.atv.internal.client.protocols.raop;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for how {@link AirPlayV2} picks the audio format.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class AirPlayV2Test {

    @Test
    void receiverListingOnlyAlacAndAacGetsAlac() {
        assertTrue(
                AirPlayV2.needsAlac(info(Map.of("bufferStream", List.of(18L, 22L), "audioStream", List.of(18L, 22L)))));
    }

    @Test
    void receiverListingPcmGetsPcm() {
        assertFalse(AirPlayV2.needsAlac(info(Map.of("audioStream", List.of(11L, 18L)))));
    }

    @Test
    void receiverListingNeitherFormatGetsPcm() {
        assertFalse(AirPlayV2.needsAlac(info(Map.of("audioStream", List.of(22L)))));
    }

    @Test
    void appleDeviceWithoutRealTimeListGetsPcm() {
        assertFalse(AirPlayV2.needsAlac(info(Map.of("bufferStream", List.of(19L, 21L, 22L)))));
    }

    @Test
    void receiverWithoutFormatInfoGetsPcm() {
        assertFalse(AirPlayV2.needsAlac(Map.of()));
    }

    private static Map<String, Object> info(Map<String, Object> formats) {
        return Map.of("supportedAudioFormatsExtended", formats);
    }
}
