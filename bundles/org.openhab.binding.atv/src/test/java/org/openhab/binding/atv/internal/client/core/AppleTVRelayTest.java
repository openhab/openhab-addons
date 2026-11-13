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
package org.openhab.binding.atv.internal.client.core;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.atv.internal.client.conf.AtvConfig;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionFailedError;
import org.openhab.binding.atv.internal.client.exceptions.NoServiceError;
import org.openhab.binding.atv.internal.client.settings.Settings;

/**
 * Tests for {@link AppleTVRelay}.
 *
 * <p>
 * A protocol's teardown can wait on the network. Running it while holding the relay's lock once froze every
 * Apple TV: on Java 21 each blocked thread pinned a shared carrier until none were left for the teardown
 * itself. These tests check that teardown never runs under that lock.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class AppleTVRelayTest {

    private final DeviceLoop loop = new DeviceLoop();
    private final AppleTVRelay relay = new AppleTVRelay(new AtvConfig(InetAddress.getLoopbackAddress(), "Living Room"),
            Settings.ofDefaults(), new CoreStateDispatcher(loop), loop);
    private final List<Boolean> lockHeldDuringClose = new CopyOnWriteArrayList<>();

    @AfterEach
    void tearDown() {
        relay.close();
    }

    @Test
    void failedProtocolsAreClosedWithoutTheRelayLock() throws Exception {
        relay.addProtocol(
                protocol(Protocol.Companion, CompletableFuture.failedFuture(new ConnectionFailedError("no"))));
        relay.addProtocol(protocol(Protocol.AirPlay, CompletableFuture.failedFuture(new ConnectionFailedError("no"))));

        ExecutionException e = assertThrows(ExecutionException.class, () -> relay.connect().get(10, TimeUnit.SECONDS));
        assertInstanceOf(NoServiceError.class, e.getCause());
        assertEquals(List.of(false, false), lockHeldDuringClose);
    }

    @Test
    void connectedProtocolsAreClosedWithoutTheRelayLock() throws Exception {
        relay.addProtocol(protocol(Protocol.Companion, CompletableFuture.completedFuture(true)));
        relay.connect().get(10, TimeUnit.SECONDS);

        relay.close().get(10, TimeUnit.SECONDS);
        assertEquals(List.of(false), lockHeldDuringClose);
    }

    private SetupData protocol(Protocol protocol, CompletableFuture<Boolean> connected) {
        return new SetupData(protocol, () -> connected, () -> lockHeldDuringClose.add(Thread.holdsLock(relay)), Map::of,
                Map.of(), Set.of());
    }
}
