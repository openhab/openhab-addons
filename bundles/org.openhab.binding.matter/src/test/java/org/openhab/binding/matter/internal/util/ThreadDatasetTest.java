/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ThreadDataset}.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ThreadDatasetTest {

    @Test
    void testThreadDatasetCreationAndConversion() {
        ThreadDataset ds = new ThreadDataset();
        ds.setPendingTimestamp(0x1122334455667788L);
        ds.setDelayTimer(30_000);
        ds.setChannel(15);
        ds.setChannelMask(134152192L);
        ds.setPanId(6699);
        ds.setNetworkKey("00112233445566778899AABBCCDDEEFF");
        ds.setPskc("A1A2A3A4A5A6A7A8A9AAABACADAEAFB0");
        ds.setMeshLocalPrefix("FD000DB800000000");
        ds.setNetworkName("openHAB‑Thread");

        String tlvHex = ds.toHex();
        ThreadDataset back = ThreadDataset.fromHex(tlvHex);

        assertEquals(Optional.of("openHAB‑Thread"), back.getNetworkName());
        assertEquals(Optional.of(15), back.getChannel());
        assertEquals(Optional.of("A1A2A3A4A5A6A7A8A9AAABACADAEAFB0"), back.getPskcHex());
        assertEquals(Optional.of(6699), back.getPanId());
        assertEquals(Optional.of("FD000DB800000000"), back.getMeshLocalPrefixHex());
        assertEquals(Optional.of(134152192L), back.getChannelMask());
    }

    @Test
    void testThreadChannelSet() {
        ThreadDataset ds = new ThreadDataset();
        Set<Integer> channels = new LinkedHashSet<>(
                Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26));
        ds.setChannelSet(channels);
        assertEquals(Optional.of(channels), ds.getChannelSet());
        assertEquals(Optional.of(134215680L), ds.getChannelMask());
    }

    @Test
    void testThreadDatasetFromHex() {
        // This is the dataset from the OpenThread Examples, so well known
        String hexInput = "0e080000000000010000000300000f35060004001fffe002084a3bdda4723ed6a00708fd2fca4935b73ecc051000112233445566778899aabbccddeeff030f4f70656e5468726561642d36616462010212340410f2cd947327a3453d03c7bef5b51ea2070c0402a0f7f8";
        ThreadDataset ds = ThreadDataset.fromHex(hexInput);

        assertEquals(Optional.of("OpenThread-6adb"), ds.getNetworkName());
        assertEquals(Optional.of(15), ds.getChannel());
        assertEquals(Optional.of("F2CD947327A3453D03C7BEF5B51EA207"), ds.getPskcHex());
        assertEquals(Optional.of(4660), ds.getPanId());
        assertEquals(Optional.of("4A3BDDA4723ED6A0"), ds.getExtPanIdHex());
        assertEquals(Optional.of("FD2FCA4935B73ECC"), ds.getMeshLocalPrefixHex());
        assertEquals(Optional.of(4294901760L), ds.getChannelMask());

        Set<Integer> expectedChannels = Set.of(16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31);
        assertEquals(Optional.of(expectedChannels), ds.getChannelSet());
    }

    @Test
    void testJsonConversion() {
        ThreadDataset ds = new ThreadDataset();
        ds.setNetworkName("TestNetwork");
        ds.setChannel(15);
        ds.setPanId(0x1234);

        String json = ds.toJson();
        ThreadDataset back = ThreadDataset.fromJson(json);
        assertNotNull(back);
        assertEquals(Optional.of("TestNetwork"), back.getNetworkName());
        assertEquals(Optional.of(15), back.getChannel());
        assertEquals(Optional.of(0x1234), back.getPanId());
    }
}
