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
package org.openhab.binding.shelly.internal.api2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2NotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2NotifyEventData;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyEvent;
import org.openhab.binding.shelly.internal.api2.ShellyBluJsonDTO.Shelly2NotifyBluEventData;
import org.openhab.binding.shelly.internal.util.ShellyUtils;

import com.google.gson.Gson;

/**
 * Tests for {@link Shelly2NotifyEvent#getBluData(Gson)} handling of the polymorphic {@code data} field.
 *
 * <p>
 * The binding's own {@code oh-blu.*} scanner script always sends {@code data} as a JSON object, but Gen2/Gen3
 * devices running Home Assistant's BLE-proxy script emit {@code NotifyEvent} frames with event
 * {@code ble.scan_result} where {@code data} is a JSON array. Before {@code data} became a raw
 * {@link com.google.gson.JsonElement}, Gson threw {@code Expected BEGIN_OBJECT but was BEGIN_ARRAY} while parsing
 * the whole frame, discarding it and flooding the log.
 * </p>
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class Shelly2NotifyEventDataShapeTest {

    private final Gson gson = new Gson();

    @Test
    void objectShapedDataParsesAndYieldsBluData() throws ShellyApiException {
        String json = """
                {"src":"shellyplusht-test","dst":"ohshelly-test-1","method":"NotifyEvent",
                "params":{"ts":1700000000.0,"events":[{"component":"script:1","id":1,"event":"oh-blu.data",
                "data":{"encryption":false,"BTHome_version":2,"pid":42,"Battery":85,
                "addr":"aa:bb:cc:dd:ee:01","rssi":-70},"ts":1700000000.0}]}}
                """;

        Shelly2NotifyEvent event = firstEvent(json);
        assertThat(event.event, is(equalTo("oh-blu.data")));

        Shelly2NotifyBluEventData blu = event.getBluData(gson);
        assertNotNull(blu);
        assertThat(blu.addr, is(equalTo("aa:bb:cc:dd:ee:01")));
        assertThat(blu.battery, is(equalTo(85)));
        assertThat(blu.pid, is(equalTo(42)));
    }

    @Test
    void arrayShapedDataParsesWithoutExceptionAndYieldsNoBluData() throws ShellyApiException {
        // Home Assistant BLE-proxy script style frame: "data" is [scanType, [[addr, rssi, advData, scanRsp], ...]]
        String json = """
                {"src":"shellyplugsg3-aabbccddeeff","dst":"ohshelly-Test-1","method":"NotifyEvent",
                "params":{"ts":1786653121.63,"events":[{"component":"script:1","id":1,"event":"ble.scan_result",
                "data":[2,[["aa:bb:cc:dd:ee:01",-97,"AgEEAwMH/hT=",""],["aa:bb:cc:dd:ee:02",-86,"AgEGEQYbxdU=",""]]],
                "ts":1786653121.63}]}}
                """;

        Shelly2NotifyEvent event = firstEvent(json);
        assertThat(event.event, is(equalTo("ble.scan_result")));
        assertThat(event.getBluData(gson), is(nullValue()));
    }

    @Test
    void absentDataYieldsNoBluDataWithoutException() throws ShellyApiException {
        String json = """
                {"src":"shellyplusht-test","params":{"ts":1700000000.0,
                "events":[{"component":"sys","id":0,"event":"config_changed","ts":1700000000.0}]}}
                """;

        Shelly2NotifyEvent event = firstEvent(json);
        assertThat(event.event, is(equalTo("config_changed")));
        assertThat(event.data, is(nullValue()));
        assertThat(event.getBluData(gson), is(nullValue()));
    }

    @Test
    void malformedObjectDataRaisesTheCheckedApiException() throws ShellyApiException {
        // "pid" is numeric in the DTO; a string there makes Gson fail on an otherwise object-shaped
        // payload. This has to surface as the checked ShellyApiException, the same error boundary the
        // frame used to fail with, because that is what both call sites catch - a raw
        // JsonSyntaxException would escape Shelly2RpcSocket.onMessage and ShellyBluApi.onNotifyEvent.
        String json = """
                {"src":"shellyplusht-test","dst":"ohshelly-test-1","method":"NotifyEvent",
                "params":{"ts":1700000000.0,"events":[{"component":"script:1","id":1,"event":"oh-blu.data",
                "data":{"addr":"aa:bb:cc:dd:ee:01","pid":"not-a-number"},"ts":1700000000.0}]}}
                """;

        Shelly2NotifyEvent event = firstEvent(json);
        assertThat(event.event, is(equalTo("oh-blu.data")));
        assertThrows(ShellyApiException.class, () -> event.getBluData(gson));
    }

    private Shelly2NotifyEvent firstEvent(String json) throws ShellyApiException {
        Shelly2RpcNotifyEvent message = ShellyUtils.fromJson(gson, json, Shelly2RpcNotifyEvent.class);
        Shelly2NotifyEventData params = message.params;
        assertNotNull(params);
        ArrayList<Shelly2NotifyEvent> events = params.events;
        assertNotNull(events);
        assertThat(events.size(), is(equalTo(1)));
        return events.get(0);
    }
}
