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
package org.openhab.binding.tapocontrol.internal.api.protocol.kasa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/**
 * Tests for {@link KasaXorProtocol}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class KasaXorProtocolTest {

    @Test
    void roundTripsUtf8Payload() {
        String payload = "{\"system\":{\"get_sysinfo\":{}}}";

        assertThat(KasaXorProtocol.decrypt(KasaXorProtocol.encrypt(payload)), is(payload));
    }

    @Test
    void buildsDimmerBrightnessAndOnCommands() {
        JsonObject values = new JsonObject();
        values.addProperty("brightness", 42);
        values.addProperty("device_on", true);

        List<String> commands = KasaXorProtocol.buildSetCommands(values, true);

        assertThat(commands, contains("{\"smartlife.iot.dimmer\":{\"set_brightness\":{\"brightness\":42}}}",
                "{\"smartlife.iot.dimmer\":{\"set_switch_state\":{\"state\":1}}}"));
    }

    @Test
    void buildsDimmerOffWithoutZeroBrightness() {
        JsonObject values = new JsonObject();
        values.addProperty("device_on", false);

        assertThat(KasaXorProtocol.buildSetCommands(values, true),
                contains("{\"smartlife.iot.dimmer\":{\"set_switch_state\":{\"state\":0}}}"));
    }

    @Test
    void buildsLegacySwitchRelayCommand() {
        JsonObject values = new JsonObject();
        values.addProperty("device_on", true);

        assertThat(KasaXorProtocol.buildSetCommands(values, false),
                contains("{\"system\":{\"set_relay_state\":{\"state\":1}}}"));
    }
}
