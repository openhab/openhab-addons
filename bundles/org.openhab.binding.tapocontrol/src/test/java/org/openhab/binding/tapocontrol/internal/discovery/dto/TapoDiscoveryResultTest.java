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
package org.openhab.binding.tapocontrol.internal.discovery.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TapoDiscoveryResult}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
class TapoDiscoveryResultTest {

    @Test
    void selectsKasaKlapForKasaIotDeviceWithKlapEncryption() {
        // UDP 20002 discovery reply of an EP10 with firmware 1.1.1 (identifiers removed)
        TapoDiscoveryResult result = parse("""
                {"device_type":"IOT.SMARTPLUGSWITCH","device_model":"EP10(US)","hw_ver":"1.0","obd_src":"tplink",
                "protocol_version":1,"factory_default":false,"mgt_encrypt_schm":{"is_support_https":false,
                "encrypt_type":"KLAP","new_klap":1,"ANS":true,"http_port":80,"lv":2}}
                """);

        assertThat(result.protocol(), is("KASA_KLAP"));
    }

    @Test
    void keepsKlapForTapoDevice() {
        TapoDiscoveryResult result = parse("""
                {"device_type":"SMART.TAPOPLUG","device_model":"P110(EU)","mgt_encrypt_schm":{"is_support_https":false,
                "encrypt_type":"KLAP","http_port":80,"lv":2}}
                """);

        assertThat(result.protocol(), is("KLAP"));
    }

    @Test
    void keepsAesForTapoDevice() {
        TapoDiscoveryResult result = parse("""
                {"device_type":"SMART.TAPOPLUG","device_model":"P100(EU)","mgt_encrypt_schm":{"is_support_https":false,
                "encrypt_type":"AES","http_port":80}}
                """);

        assertThat(result.protocol(), is("AES"));
    }

    private static TapoDiscoveryResult parse(String json) {
        return Objects.requireNonNull(GSON.fromJson(json, TapoDiscoveryResult.class));
    }
}
