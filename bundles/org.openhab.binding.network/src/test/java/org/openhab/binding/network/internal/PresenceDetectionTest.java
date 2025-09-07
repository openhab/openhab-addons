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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    private @NonNullByDefault({}) PresenceDetection asyncSubject;

    private @Mock @NonNullByDefault({}) Consumer<PresenceDetectionValue> callback;
    private @Mock @NonNullByDefault({}) ExecutorService detectionExecutorService;
    private @Mock @NonNullByDefault({}) ExecutorService waitForResultExecutorService;
    private @Mock @NonNullByDefault({}) ScheduledExecutorService scheduledExecutorService;
    private @Mock @NonNullByDefault({}) PresenceDetectionListener listener;
    private @Mock @NonNullByDefault({}) NetworkUtils networkUtils;

    @BeforeEach
    public void setUp() {
        // Mock an interface
        when(networkUtils.getInterfaceNames()).thenReturn(Set.of("TESTinterface"));
        doReturn(ArpPingUtilEnum.IPUTILS_ARPING).when(networkUtils).determineNativeArpPingMethod(anyString());
        doReturn(IpPingMethodEnum.WINDOWS_PING).when(networkUtils).determinePingMethod();

        // Inject a direct executor so async tasks run synchronously in tests
        subject = spy(new PresenceDetection(listener, Duration.ofSeconds(2), Runnable::run));
        subject.networkUtils = networkUtils;

        // Set a useful configuration. The default presenceDetection is a no-op.
        subject.setHostname("127.0.0.1");
        subject.setTimeout(Duration.ofMillis(300));
        subject.setUseDhcpSniffing(false);
        subject.setIOSDevice(true);
        subject.setServicePorts(Set.of(1010));
        subject.setUseArpPing(true, "arping", ArpPingUtilEnum.IPUTILS_ARPING);
        subject.setUseIcmpPing(true);

        asyncSubject = spy(new PresenceDetection(listener, Duration.ofSeconds(2), Executors.newSingleThreadExecutor()));

        asyncSubject.networkUtils = networkUtils;
        asyncSubject.setHostname("127.0.0.1");
        asyncSubject.setTimeout(Duration.ofMillis(300));
        asyncSubject.setUseDhcpSniffing(false);
        asyncSubject.setIOSDevice(true);
        asyncSubject.setServicePorts(Set.of(1010));
        asyncSubject.setUseArpPing(true, "arping", ArpPingUtilEnum.IPUTILS_ARPING);
        asyncSubject.setUseIcmpPing(true);

        assertThat(subject.pingMethod, is(IpPingMethodEnum.WINDOWS_PING));
    }

    // Depending on the amount of test methods an according amount of threads is used.
    @Test
    public void usedThreadCountTest() {
        // Custom executor to count submitted tasks
        class CountingExecutor implements java.util.concurrent.Executor {
            int count = 0;

            @Override
            public void execute(@Nullable Runnable command) {
                count++;
                if (command != null) {
                    command.run();
                }
            }
        }
        CountingExecutor countingExecutor = new CountingExecutor();

        // Create a new subject with the counting executor
        subject = spy(new PresenceDetection(listener, Duration.ofSeconds(2), countingExecutor));
        subject.networkUtils = networkUtils;
        subject.setHostname("127.0.0.1");
        subject.setTimeout(Duration.ofMillis(300));
        subject.setUseDhcpSniffing(false);
        subject.setIOSDevice(true);
        subject.setServicePorts(Set.of(1010));
        subject.setUseArpPing(true, "arping", ArpPingUtilEnum.IPUTILS_ARPING);
        subject.setUseIcmpPing(true);

        doNothing().when(subject).performArpPing(any(), any());
        doNothing().when(subject).performJavaPing(any());
        doNothing().when(subject).performSystemPing(any());
        doNothing().when(subject).performServicePing(any(), anyInt());

        subject.getValue(callback -> {
            // No-op callback
        });

        // Thread count: ARP + ICMP + 1*TCP + task completion watcher = 4
        assertThat(countingExecutor.count, is(4));
    }

    @Test
    public void partialAndFinalCallbackTests() throws InterruptedException, IOException {
        PingResult pingResult = new PingResult(true, Duration.ofMillis(10));
        doReturn(pingResult).when(networkUtils).nativePing(eq(IpPingMethodEnum.WINDOWS_PING), anyString(), any());
        doReturn(pingResult).when(networkUtils).nativeArpPing(eq(ArpPingUtilEnum.IPUTILS_ARPING), anyString(),
                anyString(), any(), any());
        doReturn(pingResult).when(networkUtils).servicePing(anyString(), anyInt(), any());

        subject.performPresenceDetection();

        assertThat(subject.detectionChecks, is(3));

        // All detection methods should be called (direct executor runs synchronously)
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

        // We expect no valid value
        assertTrue(asyncSubject.cache.isExpired());
        // Get value will issue a PresenceDetection internally.
        asyncSubject.getValue(callback);
        verify(asyncSubject).performPresenceDetection();
        Thread.sleep(200); // give it some time to execute
        // Callback should be called once with the result (since we use direct executor)
        verify(callback, times(1)).accept(any());

        // As long as the cache is valid, we can get the result back again
        asyncSubject.getValue(callback);
        verify(callback, times(2)).accept(any());

        // Invalidate value, we should not get a new callback immediately again
        asyncSubject.cache.invalidateValue();
        asyncSubject.getValue(callback);
        verify(callback, times(2)).accept(any());
    }
}
