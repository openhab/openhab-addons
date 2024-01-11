/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.network.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.network.internal.utils.NetworkUtils;
import org.openhab.binding.network.internal.utils.NetworkUtils.ArpPingUtilEnum;
import org.openhab.binding.network.internal.utils.NetworkUtils.IpPingMethodEnum;
import org.openhab.binding.network.internal.utils.PingResult;

/**
 * Tests cases for {@see PresenceDetectionValue}
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class PresenceDetectionTest {

    private @NonNullByDefault({}) PresenceDetection subject;

    private @Mock @NonNullByDefault({}) Consumer<PresenceDetectionValue> callback;
    private @Mock @NonNullByDefault({}) ExecutorService detectionExecutorService;
    private @Mock @NonNullByDefault({}) ScheduledExecutorService scheduledExecutorService;
    private @Mock @NonNullByDefault({}) PresenceDetectionListener listener;
    private @Mock @NonNullByDefault({}) NetworkUtils networkUtils;

    @BeforeEach
    public void setUp() {
        // Mock an interface
        when(networkUtils.getInterfaceNames()).thenReturn(Set.of("TESTinterface"));
        doReturn(ArpPingUtilEnum.IPUTILS_ARPING).when(networkUtils).determineNativeArpPingMethod(anyString());
        doReturn(IpPingMethodEnum.WINDOWS_PING).when(networkUtils).determinePingMethod();

        subject = spy(new PresenceDetection(listener, scheduledExecutorService, Duration.ofSeconds(2)));
        subject.networkUtils = networkUtils;

        // Set a useful configuration. The default presenceDetection is a no-op.
        subject.setHostname("127.0.0.1");
        subject.setTimeout(Duration.ofMillis(300));
        subject.setUseDhcpSniffing(false);
        subject.setIOSDevice(true);
        subject.setServicePorts(Set.of(1010));
        subject.setUseArpPing(true, "arping", ArpPingUtilEnum.IPUTILS_ARPING);
        subject.setUseIcmpPing(true);

        assertThat(subject.pingMethod, is(IpPingMethodEnum.WINDOWS_PING));
    }

    // Depending on the amount of test methods an according amount of threads is spawned.
    // We will check if they spawn and return in time.
    @Test
    public void threadCountTest() {
        assertNull(subject.detectionExecutorService);

        doNothing().when(subject).performArpPing(any(), any());
        doNothing().when(subject).performJavaPing(any());
        doNothing().when(subject).performSystemPing(any());
        doNothing().when(subject).performServicePing(any(), anyInt());

        subject.getValue(callback -> {
        });

        // Thread count: ARP + ICMP + 1*TCP
        assertThat(subject.detectionChecks, is(3));
        assertNotNull(subject.detectionExecutorService);

        // "Wait" for the presence detection to finish
        ArgumentCaptor<Runnable> runnableCapture = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduledExecutorService, times(1)).execute(runnableCapture.capture());
        runnableCapture.getValue().run();

        assertThat(subject.detectionChecks, is(0));
        assertNull(subject.detectionExecutorService);
    }

    @Test
    public void partialAndFinalCallbackTests() throws InterruptedException, IOException {
        PingResult pingResult = new PingResult(true, Duration.ofMillis(10));
        doReturn(pingResult).when(networkUtils).nativePing(eq(IpPingMethodEnum.WINDOWS_PING), anyString(), any());
        doReturn(pingResult).when(networkUtils).nativeArpPing(eq(ArpPingUtilEnum.IPUTILS_ARPING), anyString(),
                anyString(), any(), any());
        doReturn(pingResult).when(networkUtils).servicePing(anyString(), anyInt(), any());

        doReturn(detectionExecutorService).when(subject).getThreadsFor(anyInt());

        subject.performPresenceDetection();

        assertThat(subject.detectionChecks, is(3));

        // Perform the different presence detection threads now
        ArgumentCaptor<Runnable> capture = ArgumentCaptor.forClass(Runnable.class);
        verify(detectionExecutorService, times(3)).execute(capture.capture());
        for (Runnable r : capture.getAllValues()) {
            r.run();
        }

        // "Wait" for the presence detection to finish
        ArgumentCaptor<Runnable> runnableCapture = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduledExecutorService, times(1)).execute(runnableCapture.capture());
        runnableCapture.getValue().run();

        assertThat(subject.detectionChecks, is(0));

        verify(subject, times(0)).performJavaPing(any());
        verify(subject).performSystemPing(any());
        verify(subject).performArpPing(any(), any());
        verify(subject).performServicePing(any(), anyInt());

        verify(listener, times(3)).partialDetectionResult(any());
        ArgumentCaptor<PresenceDetectionValue> pdvCapture = ArgumentCaptor.forClass(PresenceDetectionValue.class);
        verify(listener, times(1)).finalDetectionResult(pdvCapture.capture());

        assertThat(pdvCapture.getValue().getSuccessfulDetectionTypes(), is("ARP_PING, ICMP_PING, TCP_CONNECTION"));
    }

    @Test
    public void cacheTest() throws InterruptedException, IOException {
        PingResult pingResult = new PingResult(true, Duration.ofMillis(10));
        doReturn(pingResult).when(networkUtils).nativePing(eq(IpPingMethodEnum.WINDOWS_PING), anyString(), any());
        doReturn(pingResult).when(networkUtils).nativeArpPing(eq(ArpPingUtilEnum.IPUTILS_ARPING), anyString(),
                anyString(), any(), any());
        doReturn(pingResult).when(networkUtils).servicePing(anyString(), anyInt(), any());

        doReturn(detectionExecutorService).when(subject).getThreadsFor(anyInt());

        // We expect no valid value
        assertTrue(subject.cache.isExpired());
        // Get value will issue a PresenceDetection internally.
        subject.getValue(callback);
        verify(subject).performPresenceDetection();
        assertNotNull(subject.detectionExecutorService);
        // There should be no straight callback yet
        verify(callback, times(0)).accept(any());

        // Perform the different presence detection threads now
        ArgumentCaptor<Runnable> capture = ArgumentCaptor.forClass(Runnable.class);
        verify(detectionExecutorService, times(3)).execute(capture.capture());
        for (Runnable r : capture.getAllValues()) {
            r.run();
        }

        // "Wait" for the presence detection to finish
        capture = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduledExecutorService, times(1)).execute(capture.capture());
        capture.getValue().run();

        // Although there are multiple partial results and a final result,
        // the getValue() consumers get the fastest response possible, and only once.
        verify(callback, times(1)).accept(any());

        // As long as the cache is valid, we can get the result back again
        subject.getValue(callback);
        verify(callback, times(2)).accept(any());

        // Invalidate value, we should not get a new callback immediately again
        subject.cache.invalidateValue();
        subject.getValue(callback);
        verify(callback, times(2)).accept(any());
    }
}
