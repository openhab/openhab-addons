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
package org.openhab.binding.ddwrt.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.session.ClientSession;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.slf4j.Logger;

/**
 * Tests for {@link DDWRTGenericDevice}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class DDWRTGenericDeviceTest {

    private static final String DEFAULT_ROUTE_COMMAND = "ip -o route show default 2>/dev/null | "
            + "awk '{for (i=1;i<=NF;i++) if ($i == \"dev\") {print $(i+1); exit}}'";
    private static final String PHYSICAL_INTERFACE_COMMAND = "for path in /sys/class/net/*; do iface=${path##*/}; "
            + "[ \"$iface\" = lo ] && continue; [ -e \"$path/device\" ] || continue; "
            + "[ \"$(cat \"$path/operstate\" 2>/dev/null)\" = up ] || continue; echo \"$iface\"; break; done";

    @Test
    void readsTrafficFromDefaultRouteInterfaceWithoutBr0() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout(anyString())).thenReturn("");
        when(runner.execStdout(DEFAULT_ROUTE_COMMAND)).thenReturn("eth0");
        when(runner.execStdout("awk -v iface='eth0' '$1 == iface \":\" {print $2, $10; exit}' /proc/net/dev"))
                .thenReturn("123456 654321");
        DDWRTGenericDevice device = new DDWRTGenericDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));

        device.refreshCommon(runner);

        assertThat(device.getIfIn(), is(123456L));
        assertThat(device.getIfOut(), is(654321L));
        verify(runner, times(0))
                .execStdout("awk -v iface='br0' '$1 == iface \":\" {print $2, $10; exit}' /proc/net/dev");
    }

    @Test
    void cachesInterfaceUntilSessionCloses() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout(DEFAULT_ROUTE_COMMAND)).thenReturn("eth0", "enp1s0");
        DDWRTGenericDevice device = new DDWRTGenericDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));

        assertThat(device.getLanInterface(runner), is("eth0"));
        assertThat(device.getLanInterface(runner), is("eth0"));
        verify(runner, times(1)).execStdout(DEFAULT_ROUTE_COMMAND);

        device.closeSessionQuietly();

        assertThat(device.getLanInterface(runner), is("enp1s0"));
        verify(runner, times(2)).execStdout(DEFAULT_ROUTE_COMMAND);
    }

    @Test
    void sessionReplacementWaitsForInterfaceDiscovery() throws Exception {
        CountDownLatch discoveryStarted = new CountDownLatch(1);
        CountDownLatch continueDiscovery = new CountDownLatch(1);
        CountDownLatch replacementStarted = new CountDownLatch(1);
        CountDownLatch replacementCompleted = new CountDownLatch(1);

        SshRunner oldRunner = mock(SshRunner.class);
        when(oldRunner.execStdout(anyString())).thenReturn("");
        when(oldRunner.execStdout(DEFAULT_ROUTE_COMMAND)).thenAnswer(invocation -> {
            discoveryStarted.countDown();
            if (!continueDiscovery.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting to continue interface discovery");
            }
            return "eth0";
        });

        ClientSession oldClientSession = mock(ClientSession.class);
        when(oldClientSession.isOpen()).thenReturn(true);
        SshAuthSession oldSession = mock(SshAuthSession.class);
        when(oldSession.getClientSession()).thenReturn(oldClientSession);
        when(oldSession.createRunner()).thenReturn(oldRunner);

        DDWRTDeviceConfiguration oldConfig = new DDWRTDeviceConfiguration();
        DDWRTGenericDevice device = new DDWRTGenericDevice(oldConfig, mock(Logger.class));
        device.authSession = oldSession;

        DDWRTDeviceConfiguration newConfig = new DDWRTDeviceConfiguration();
        newConfig.password = "replacement-password";
        SshAuthSession newSession = mock(SshAuthSession.class);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> refresh = executor.submit(device::refresh);
            assertThat(discoveryStarted.await(5, TimeUnit.SECONDS), is(true));

            Future<?> replacement = executor.submit(() -> {
                replacementStarted.countDown();
                try {
                    device.replaceSession(newConfig, newSession);
                } finally {
                    replacementCompleted.countDown();
                }
            });
            assertThat(replacementStarted.await(5, TimeUnit.SECONDS), is(true));
            assertThat(replacementCompleted.await(250, TimeUnit.MILLISECONDS), is(false));

            continueDiscovery.countDown();
            refresh.get(5, TimeUnit.SECONDS);
            replacement.get(5, TimeUnit.SECONDS);

            SshRunner newRunner = mock(SshRunner.class);
            when(newRunner.execStdout(DEFAULT_ROUTE_COMMAND)).thenReturn("enp1s0");
            assertThat(device.getLanInterface(newRunner), is("enp1s0"));
            verify(oldSession).close();
        } finally {
            continueDiscovery.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void fallsBackToActivePhysicalInterface() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout(DEFAULT_ROUTE_COMMAND)).thenReturn("");
        when(runner.execStdout(PHYSICAL_INTERFACE_COMMAND)).thenReturn("wlan0");
        DDWRTGenericDevice device = new DDWRTGenericDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));

        assertThat(device.getLanInterface(runner), is("wlan0"));
    }

    @Test
    void rejectsLoopbackAndFallsBackToPhysicalInterface() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout(DEFAULT_ROUTE_COMMAND)).thenReturn("lo");
        when(runner.execStdout(PHYSICAL_INTERFACE_COMMAND)).thenReturn("eth0");
        DDWRTGenericDevice device = new DDWRTGenericDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));

        assertThat(device.getLanInterface(runner), is("eth0"));
    }

    @Test
    void keepsRouterLanInterfacesUnchanged() {
        SshRunner runner = mock(SshRunner.class);
        DDWRTDeviceConfiguration config = new DDWRTDeviceConfiguration();
        Logger logger = mock(Logger.class);

        assertThat(new DDWRTBroadcomDevice(config, logger).getLanInterface(runner), is("br0"));
        assertThat(new DDWRTOpenWrtDevice(config, logger).getLanInterface(runner), is("br-lan"));
    }

    @Test
    void unresolvedInterfaceReturnsEmptyAndLogsOnlyOnce() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout(anyString())).thenReturn("");
        Logger logger = mock(Logger.class);
        DDWRTDeviceConfiguration config = new DDWRTDeviceConfiguration();
        config.hostname = "linux-host";
        DDWRTGenericDevice device = new DDWRTGenericDevice(config, logger);

        assertThat(device.getLanInterface(runner), is(""));
        assertThat(device.getLanInterface(runner), is(""));

        verify(logger, times(1)).warn("Could not determine a LAN traffic interface for {}; counters will remain zero",
                "linux-host");
    }
}
