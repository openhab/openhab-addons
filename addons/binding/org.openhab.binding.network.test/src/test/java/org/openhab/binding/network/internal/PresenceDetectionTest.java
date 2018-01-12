/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.network.internal.toberemoved.cache.ExpiringCacheAsync;
import org.openhab.binding.network.internal.toberemoved.cache.ExpiringCacheHelper;
import org.openhab.binding.network.internal.utils.NetworkUtils;
import org.openhab.binding.network.internal.utils.NetworkUtils.ArpPingUtilEnum;
import org.openhab.binding.network.internal.utils.NetworkUtils.IpPingMethodEnum;

/**
 * Tests cases for {@see PresenceDetectionValue}
 *
 * @author David Graeff - Initial contribution
 */
@RunWith(MockitoJUnitRunner.class)
public class PresenceDetectionTest {
    static long CACHETIME = 2000L;
    @Mock
    NetworkUtils networkUtils;

    @Mock
    PresenceDetectionListener listener;

    @Mock
    ExecutorService executorService;

    @Mock
    Consumer<PresenceDetectionValue> callback;

    PresenceDetection subject;

    @Before
    public void setUp() throws UnknownHostException {
        MockitoAnnotations.initMocks(this);

        // Mock an interface
        when(networkUtils.getInterfaceNames()).thenReturn(Collections.singleton("TESTinterface"));
        doReturn(ArpPingUtilEnum.IPUTILS_ARPING).when(networkUtils).determineNativeARPpingMethod(anyString());
        doReturn(IpPingMethodEnum.WINDOWS_PING).when(networkUtils).determinePingMethod();

        subject = spy(new PresenceDetection(listener, (int) CACHETIME));
        subject.networkUtils = networkUtils;
        subject.cache = spy(new ExpiringCacheAsync<PresenceDetectionValue>(CACHETIME, () -> {
            subject.performPresenceDetection(false);
        }));

        // Set a useful configuration. The default presenceDetection is a no-op.
        subject.setHostname("127.0.0.1");
        subject.setTimeout(300);
        subject.setUseDhcpSniffing(false);
        subject.setIOSDevice(true);
        subject.setServicePorts(Collections.singleton(1010));
        subject.setUseArpPing(true, "arping");
        subject.setUseIcmpPing(true);

        assertThat(subject.getPingMethod(), is(IpPingMethodEnum.WINDOWS_PING));
    }

    @After
    public void shutDown() {
        subject.waitForPresenceDetection();
    }

    // Depending on the amount of test methods an according amount of threads is spawned.
    // We will check if they spawn and return in time.
    @Test
    public void threadCountTest() {
        assertNull(subject.executorService);

        doNothing().when(subject).performARPping(anyObject());
        doNothing().when(subject).performJavaPing();
        doNothing().when(subject).performSystemPing();
        doNothing().when(subject).performServicePing(anyInt());

        subject.performPresenceDetection(false);

        // Thread count: ARP + ICMP + 1*TCP
        assertThat(subject.detectionChecks, is(3));
        assertNotNull(subject.executorService);

        subject.waitForPresenceDetection();
        assertThat(subject.detectionChecks, is(0));
        assertNull(subject.executorService);
    }

    @Test
    public void partialAndFinalCallbackTests() throws InterruptedException, IOException {
        doReturn(true).when(networkUtils).nativePing(eq(IpPingMethodEnum.WINDOWS_PING), anyString(), anyInt());
        doReturn(true).when(networkUtils).nativeARPPing(eq(ArpPingUtilEnum.IPUTILS_ARPING), anyString(), anyString(),
                anyObject(), anyInt());
        doReturn(true).when(networkUtils).servicePing(anyString(), anyInt(), anyInt());

        assertTrue(subject.performPresenceDetection(false));
        subject.waitForPresenceDetection();

        verify(subject, times(0)).performJavaPing();
        verify(subject).performSystemPing();
        verify(subject).performARPping(anyObject());
        verify(subject).performServicePing(anyInt());

        verify(listener, times(3)).partialDetectionResult(anyObject());
        ArgumentCaptor<PresenceDetectionValue> capture = ArgumentCaptor.forClass(PresenceDetectionValue.class);
        verify(listener, times(1)).finalDetectionResult(capture.capture());

        assertThat(capture.getValue().getSuccessfulDetectionTypes(), is("ARP_PING, ICMP_PING, TCP_CONNECTION"));
    }

    @Test
    public void cacheTest() throws InterruptedException, IOException {
        doReturn(true).when(networkUtils).nativePing(eq(IpPingMethodEnum.WINDOWS_PING), anyString(), anyInt());
        doReturn(true).when(networkUtils).nativeARPPing(eq(ArpPingUtilEnum.IPUTILS_ARPING), anyString(), anyString(),
                anyObject(), anyInt());
        doReturn(true).when(networkUtils).servicePing(anyString(), anyInt(), anyInt());

        doReturn(executorService).when(subject).getThreadsFor(anyInt());

        // We expect no valid value
        assertTrue(subject.cache.isExpired());
        // Get value will issue a PresenceDetection internally.
        subject.getValue(callback);
        verify(subject).performPresenceDetection(eq(false));
        assertNotNull(subject.executorService);
        // There should be no straight callback yet
        verify(callback, times(0)).accept(anyObject());

        // Perform the different presence detection threads now
        ArgumentCaptor<Runnable> capture = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService, times(3)).execute(capture.capture());
        for (Runnable r : capture.getAllValues()) {
            r.run();
        }
        // "Wait" for the presence detection to finish
        subject.waitForPresenceDetection();

        // Although there are multiple partial results and a final result,
        // the getValue() consumers get the fastest response possible, and only once.
        verify(callback, times(1)).accept(anyObject());

        // As long as the cache is valid, we can get the result back again
        subject.getValue(callback);
        verify(callback, times(2)).accept(anyObject());

        // Invalidate value, we should not get a new callback immediately again
        subject.cache.invalidateValue();
        subject.getValue(callback);
        verify(callback, times(2)).accept(anyObject());
    }

    @Test
    public void reuseValueTests() throws InterruptedException, IOException {
        final long START_TIME = 1000L;
        when(subject.cache.getCurrentNanoTime()).thenReturn(TimeUnit.MILLISECONDS.toNanos(START_TIME));

        // The PresenceDetectionValue.getLowestLatency() should return the smallest latency
        PresenceDetectionValue v = subject.updateReachableValue(PresenceDetectionType.ICMP_PING, 20);
        PresenceDetectionValue v2 = subject.updateReachableValue(PresenceDetectionType.ICMP_PING, 19);
        assertEquals(v, v2);
        assertThat(v.getLowestLatency(), is(19.0));

        // Advance in time but not expire the cache (1ms left)
        final long ALMOST_EXPIRE = START_TIME + CACHETIME - 1;
        when(subject.cache.getCurrentNanoTime()).thenReturn(TimeUnit.MILLISECONDS.toNanos(ALMOST_EXPIRE));

        // Updating should reset the expire timer of the cache
        v2 = subject.updateReachableValue(PresenceDetectionType.ICMP_PING, 28);
        assertEquals(v, v2);
        assertThat(v2.getLowestLatency(), is(19.0));
        assertThat(ExpiringCacheHelper.expireTime(subject.cache),
                is(TimeUnit.MILLISECONDS.toNanos(ALMOST_EXPIRE + CACHETIME)));

        // Cache expire. A new PresenceDetectionValue instance will be returned
        when(subject.cache.getCurrentNanoTime())
                .thenReturn(TimeUnit.MILLISECONDS.toNanos(ALMOST_EXPIRE + CACHETIME + CACHETIME + 1));
        v2 = subject.updateReachableValue(PresenceDetectionType.ICMP_PING, 25);
        assertNotEquals(v, v2);
        assertThat(v2.getLowestLatency(), is(25.0));
    }
}
