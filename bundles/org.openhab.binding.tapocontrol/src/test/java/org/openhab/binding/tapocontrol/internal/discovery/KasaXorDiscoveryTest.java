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
package org.openhab.binding.tapocontrol.internal.discovery;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link KasaXorDiscovery}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class KasaXorDiscoveryTest {

    @Test
    void decodesLegacyHs220DiscoveryResponse() {
        byte[] response = encrypt("""
                {"system":{"get_sysinfo":{
                  "sw_ver":"1.0.1 Build 230725 Rel.151842",
                  "hw_ver":"3.0",
                  "model":"HS220(US)",
                  "deviceId":"device-id",
                  "hwId":"hardware-id",
                  "oemId":"oem-id",
                  "alias":"living room chandelier",
                  "type":"IOT.SMARTPLUGSWITCH",
                  "mac":"14:EB:B6:64:45:AD",
                  "fwId":"firmware-id",
                  "dev_name":"Wi-Fi Smart Dimmer",
                  "err_code":0
                }}}
                """);

        var result = KasaXorDiscovery.decode(response, response.length, "192.168.0.61").orElseThrow();

        assertThat(result.deviceModel(), is("HS220(US)"));
        assertThat(result.deviceMac(), is("14EBB66445AD"));
        assertThat(result.alias(), is("living room chandelier"));
        assertThat(result.ip(), is("192.168.0.61"));
        assertThat(result.encryptionSchema().encryptType(), is("KASA"));
        assertThat(result.encryptionSchema().httpPort(), is(9999));
    }

    @Test
    void ignoresMalformedResponse() {
        byte[] response = encrypt("not JSON");

        assertThat(KasaXorDiscovery.decode(response, response.length, "192.168.0.61").isEmpty(), is(true));
    }

    private static byte[] encrypt(String content) {
        byte[] plainText = content.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = new byte[plainText.length];
        int key = 0xAB;
        for (int i = 0; i < plainText.length; i++) {
            encrypted[i] = (byte) (plainText[i] ^ key);
            key = Byte.toUnsignedInt(encrypted[i]);
        }
        return encrypted;
    }
}
