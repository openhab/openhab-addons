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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.ALARM_TYPE_LORA_RECEIVED;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CHANNEL_GROUP_LORA;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CHANNEL_LORA_RSSI;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CHANNEL_LORA_RXDATA;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CHANNEL_LORA_RXDATARAW;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CHANNEL_LORA_SNR;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPRO1;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Tests for {@link Shelly2ApiRpc} handling of the documented "lora" notify-event (RX datagram received), whose
 * payload is nested under the "info" object rather than the generic "data" member.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2LoraNotifyEventTest {

    // API-docs example payload: base64("0123456789")
    private static final String API_DOCS_EVENT_JSON = """
            {"src":"shellypro1-test","params":{"ts":1.0,"events":[{"id":0,"component":"lora:0","event":"lora",
            "info":{"data":"MDEyMzQ1Njc4OQ==","rssi":-97,"snr":8,"tsu":123456}}]}}
            """;

    @Test
    void loraEventUpdatesRxChannelsFromDocumentedJson() throws ShellyApiException {
        Fixture f = build();
        f.rpc.onNotifyEvent(API_DOCS_EVENT_JSON);

        verify(f.thing).updateChannel(CHANNEL_GROUP_LORA, CHANNEL_LORA_RXDATARAW, new StringType("MDEyMzQ1Njc4OQ=="));
        verify(f.thing).updateChannel(CHANNEL_GROUP_LORA, CHANNEL_LORA_RXDATA, new StringType("0123456789"));
        verify(f.thing).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_RSSI),
                eq(new QuantityType<>(-97, Units.DECIBEL_MILLIWATTS)));
        verify(f.thing).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_SNR),
                eq(new QuantityType<>(8, Units.DECIBEL)));
    }

    @Test
    void loraEventWithNonUtf8PayloadUpdatesRawChannelOnlyNotText() throws ShellyApiException {
        Fixture f = build();
        // base64("ÿþ") — not a valid UTF-8 sequence
        f.rpc.onNotifyEvent("""
                {"src":"shellypro1-test","params":{"ts":1.0,"events":[{"id":0,"component":"lora:0","event":"lora",
                "info":{"data":"//4=","rssi":-97,"snr":8,"tsu":123456}}]}}
                """);

        verify(f.thing).updateChannel(CHANNEL_GROUP_LORA, CHANNEL_LORA_RXDATARAW, new StringType("//4="));
        verify(f.thing, times(0)).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_RXDATA), any());
    }

    @Test
    void loraEventPostsForcedTriggerOnEveryConsecutivePacket() throws ShellyApiException {
        Fixture f = build();
        // the alarm value never changes across consecutive packets, so postEvent's de-dup would swallow all
        // but the first unless the RPC layer forces it
        f.rpc.onNotifyEvent(API_DOCS_EVENT_JSON);
        f.rpc.onNotifyEvent(API_DOCS_EVENT_JSON);

        verify(f.thing, times(2)).postEvent(ALARM_TYPE_LORA_RECEIVED, true);
    }

    @Test
    void unrelatedNotifyEventDoesNotPostLoraTrigger() throws ShellyApiException {
        Fixture f = build();
        f.rpc.onNotifyEvent("""
                {"src":"shellypro1-test","params":{"ts":1.0,"events":[{"id":0,"event":"some.unknown.event"}]}}
                """);

        verify(f.thing, times(0)).postEvent(eq(ALARM_TYPE_LORA_RECEIVED), anyBoolean());
    }

    private static final class Fixture {
        final Shelly2ApiRpc rpc;
        final ShellyThingInterface thing;

        Fixture(Shelly2ApiRpc rpc, ShellyThingInterface thing) {
            this.rpc = rpc;
            this.thing = thing;
        }
    }

    private Fixture build() {
        ThingTypeUID thingTypeUID = THING_TYPE_SHELLYPRO1;

        Thing ohThing = mock(Thing.class);
        when(ohThing.getThingTypeUID()).thenReturn(thingTypeUID);

        ShellyDeviceProfile profile = new ShellyDeviceProfile(thingTypeUID);
        profile.settings.loraDetected = true;
        profile.settings.loraRxEnabled = true;

        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(thing.getProfile()).thenReturn(profile);

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080,
                mock(NetworkAddressService.class));
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-lora", "");

        Shelly2ApiRpc rpc = new Shelly2ApiRpc("test-lora", mock(ShellyThingTable.class), thing, config,
                mock(WebSocketClient.class), mock(ScheduledExecutorService.class));

        return new Fixture(rpc, thing);
    }
}
