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
package org.openhab.binding.network.internal.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.network.internal.NetworkBindingConfiguration;
import org.openhab.binding.network.internal.NetworkBindingConstants;
import org.openhab.binding.network.internal.NetworkDeviceType;
import org.openhab.binding.network.internal.PresenceDetection;
import org.openhab.binding.network.internal.PresenceDetectionValue;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.UnDefType;

/**
 * Tests cases for {@link NetworkHandler}.
 *
 * @author David Graeff - Initial contribution
 * @author Alexander Friese - Add HTTP presence detection
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class NetworkHandlerTest extends JavaTest {
    private ThingUID thingUID = new ThingUID("network", "ttype", "ping");

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) ScheduledExecutorService scheduledExecutorService;
    private @Mock @NonNullByDefault({}) ExecutorService resolver;
    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;

    @BeforeEach
    public void setUp() {
        when(thing.getUID()).thenReturn(thingUID);
    }

    @Test
    public void checkAllConfigurations() {
        NetworkBindingConfiguration config = new NetworkBindingConfiguration();
        NetworkHandler handler = spy(new NetworkHandler(thing, scheduledExecutorService, resolver,
                NetworkDeviceType.TCP_SERVICE, config, httpClient));
        handler.setCallback(callback);
        // Provide all possible configuration
        when(thing.getConfiguration()).thenAnswer(a -> {
            Configuration conf = new Configuration();
            conf.put(NetworkBindingConstants.PARAMETER_RETRY, 10);
            conf.put(NetworkBindingConstants.PARAMETER_HOSTNAME, "127.0.0.1");
            conf.put(NetworkBindingConstants.PARAMETER_PORT, 8080);
            conf.put(NetworkBindingConstants.PARAMETER_TIMEOUT, 1234);
            return conf;
        });
        PresenceDetection presenceDetection = spy(new PresenceDetection(handler, Duration.ofSeconds(2), resolver));

        handler.initialize(presenceDetection);
        assertThat(handler.retries, is(10));
        assertThat(presenceDetection.getHostname(), is("127.0.0.1"));
        assertThat(presenceDetection.getServicePorts().iterator().next(), is(8080));
        assertThat(presenceDetection.getTimeout(), is(Duration.ofMillis(1234)));
    }

    @Test
    public void tcpDeviceInitTests() {
        NetworkBindingConfiguration config = new NetworkBindingConfiguration();
        NetworkHandler handler = spy(new NetworkHandler(thing, scheduledExecutorService, resolver,
                NetworkDeviceType.TCP_SERVICE, config, httpClient));
        assertThat(handler.getDeviceType(), is(NetworkDeviceType.TCP_SERVICE));
        handler.setCallback(callback);
        // Port is missing, should make the device OFFLINE
        when(thing.getConfiguration()).thenAnswer(a -> {
            Configuration conf = new Configuration();
            conf.put(NetworkBindingConstants.PARAMETER_HOSTNAME, "127.0.0.1");
            return conf;
        });
        handler.initialize(new PresenceDetection(handler, Duration.ofSeconds(2), resolver));
        // Check that we are offline
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        assertThat(statusInfoCaptor.getValue().getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        assertThat(statusInfoCaptor.getValue().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
    }

    @Test
    public void pingDeviceInitTests() {
        NetworkBindingConfiguration config = new NetworkBindingConfiguration();
        NetworkHandler handler = spy(new NetworkHandler(thing, scheduledExecutorService, resolver,
                NetworkDeviceType.PING, config, httpClient));
        handler.setCallback(callback);
        // Provide minimal configuration
        when(thing.getConfiguration()).thenAnswer(a -> {
            Configuration conf = new Configuration();
            conf.put(NetworkBindingConstants.PARAMETER_HOSTNAME, "127.0.0.1");
            conf.put(NetworkBindingConstants.PARAMETER_REFRESH_INTERVAL, 0); // disable auto refresh
            return conf;
        });
        PresenceDetection presenceDetection = spy(new PresenceDetection(handler, Duration.ofSeconds(2), resolver));
        doReturn(Instant.now()).when(presenceDetection).getLastSeen();

        handler.initialize(presenceDetection);
        // Check that we are online
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        assertEquals(ThingStatus.ONLINE, statusInfoCaptor.getValue().getStatus());

        // Mock result value
        PresenceDetectionValue value = mock(PresenceDetectionValue.class);
        when(value.getLowestLatency()).thenReturn(Duration.ofMillis(10));
        when(value.isReachable()).thenReturn(true);
        when(value.getSuccessfulDetectionTypes()).thenReturn("TESTMETHOD");

        // Partial result from the PresenceDetection object should affect the
        // ONLINE and LATENCY channel
        handler.partialDetectionResult(value);
        verify(callback).stateUpdated(eq(new ChannelUID(thingUID, NetworkBindingConstants.CHANNEL_ONLINE)),
                eq(OnOffType.ON));
        verify(callback).stateUpdated(eq(new ChannelUID(thingUID, NetworkBindingConstants.CHANNEL_LATENCY)),
                eq(new QuantityType<>("10.0 ms")));

        // Final result affects the LASTSEEN channel
        handler.finalDetectionResult(value);
        verify(callback).stateUpdated(eq(new ChannelUID(thingUID, NetworkBindingConstants.CHANNEL_LASTSEEN)), any());
    }

    @Test
    public void httpDeviceInitTests() {
        NetworkHandler handler = createHttpHandler("https://example.com/status");
        PresenceDetection presenceDetection = spy(new PresenceDetection(handler, Duration.ofSeconds(2), resolver));

        handler.initialize(presenceDetection);
        // Check that we are online
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        assertEquals(ThingStatus.ONLINE, statusInfoCaptor.getValue().getStatus());

        assertThat(presenceDetection.getHttpUri(), is(URI.create("https://example.com/status")));
        // The host is used to identify the target of the presence detection
        assertThat(presenceDetection.getHostname(), is("example.com"));
    }

    @Test
    public void httpDeviceMalformedUrlInitTests() {
        NetworkHandler handler = createHttpHandler("https://example.com/not a valid path");

        handler.initialize(new PresenceDetection(handler, Duration.ofSeconds(2), resolver));
        assertConfigurationError();
    }

    @Test
    public void httpDeviceUnsupportedSchemeInitTests() {
        NetworkHandler handler = createHttpHandler("ftp://example.com/status");

        handler.initialize(new PresenceDetection(handler, Duration.ofSeconds(2), resolver));
        assertConfigurationError();
    }

    @Test
    public void httpDeviceRelativeUrlInitTests() {
        NetworkHandler handler = createHttpHandler("example.com/status");

        handler.initialize(new PresenceDetection(handler, Duration.ofSeconds(2), resolver));
        assertConfigurationError();
    }

    @Test
    public void httpDeviceStatusCodeChannelTests() {
        NetworkHandler handler = createHttpHandler("http://example.com/status");
        handler.initialize(new PresenceDetection(handler, Duration.ofSeconds(2), resolver));

        PresenceDetectionValue reachable = mock(PresenceDetectionValue.class);
        when(reachable.getLowestLatency()).thenReturn(Duration.ofMillis(10));
        when(reachable.isReachable()).thenReturn(true);
        when(reachable.getHttpStatusCode()).thenReturn(200);

        handler.partialDetectionResult(reachable);
        verify(callback).stateUpdated(eq(new ChannelUID(thingUID, NetworkBindingConstants.CHANNEL_HTTP_STATUS)),
                eq(new DecimalType(200)));

        // The status code is reported even if it is the reason for the device being considered offline
        PresenceDetectionValue unreachable = mock(PresenceDetectionValue.class);
        when(unreachable.getLowestLatency()).thenReturn(PresenceDetectionValue.UNREACHABLE);
        when(unreachable.isReachable()).thenReturn(false);
        when(unreachable.getHttpStatusCode()).thenReturn(503);

        handler.finalDetectionResult(unreachable);
        verify(callback).stateUpdated(eq(new ChannelUID(thingUID, NetworkBindingConstants.CHANNEL_ONLINE)),
                eq(OnOffType.OFF));
        verify(callback).stateUpdated(eq(new ChannelUID(thingUID, NetworkBindingConstants.CHANNEL_HTTP_STATUS)),
                eq(new DecimalType(503)));
    }

    @Test
    public void httpDeviceWithoutResponseTests() {
        NetworkHandler handler = createHttpHandler("http://example.com/status");
        handler.initialize(new PresenceDetection(handler, Duration.ofSeconds(2), resolver));

        PresenceDetectionValue value = mock(PresenceDetectionValue.class);
        when(value.getLowestLatency()).thenReturn(PresenceDetectionValue.UNREACHABLE);
        when(value.isReachable()).thenReturn(false);
        // Mockito would return 0 instead of null for the Integer status code by default
        doReturn(null).when(value).getHttpStatusCode();

        handler.finalDetectionResult(value);
        verify(callback).stateUpdated(eq(new ChannelUID(thingUID, NetworkBindingConstants.CHANNEL_HTTP_STATUS)),
                eq(UnDefType.UNDEF));
    }

    private NetworkHandler createHttpHandler(String url) {
        NetworkBindingConfiguration config = new NetworkBindingConfiguration();
        NetworkHandler handler = spy(new NetworkHandler(thing, scheduledExecutorService, resolver,
                NetworkDeviceType.HTTP, config, httpClient));
        assertThat(handler.getDeviceType(), is(NetworkDeviceType.HTTP));
        handler.setCallback(callback);
        when(thing.getConfiguration()).thenAnswer(a -> {
            Configuration conf = new Configuration();
            conf.put(NetworkBindingConstants.PARAMETER_URL, url);
            conf.put(NetworkBindingConstants.PARAMETER_REFRESH_INTERVAL, 0); // disable auto refresh
            return conf;
        });
        return handler;
    }

    private void assertConfigurationError() {
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        assertThat(statusInfoCaptor.getValue().getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        assertThat(statusInfoCaptor.getValue().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
    }
}
