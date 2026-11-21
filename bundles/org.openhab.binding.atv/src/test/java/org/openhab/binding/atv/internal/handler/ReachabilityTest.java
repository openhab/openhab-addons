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
package org.openhab.binding.atv.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionFailedError;
import org.openhab.binding.atv.internal.client.exceptions.NoServiceError;
import org.openhab.binding.atv.internal.client.exceptions.OperationTimeoutError;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;

/**
 * Tests for {@link Reachability}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ReachabilityTest {

    @Test
    public void testConnectTimeoutIsNoAnswer() {
        assertThat(Reachability.of(wrapped(new SocketTimeoutException("Connect timed out"))),
                is(Reachability.NO_ANSWER));
    }

    @Test
    public void testNoRouteIsNoAnswer() {
        assertThat(Reachability.of(wrapped(new NoRouteToHostException())), is(Reachability.NO_ANSWER));
    }

    @Test
    public void testOverallTimeoutIsNoAnswer() {
        assertThat(Reachability.of(new TimeoutException()), is(Reachability.NO_ANSWER));
    }

    @Test
    public void testRequestTimeoutIsNoAnswer() {
        assertThat(Reachability.of(new ExecutionException(new OperationTimeoutError("no reply"))),
                is(Reachability.NO_ANSWER));
    }

    @Test
    public void testRefusedIsHostUp() {
        assertThat(Reachability.of(wrapped(new ConnectException("Connection refused"))), is(Reachability.HOST_UP));
    }

    @Test
    public void testDroppedMidExchangeIsHostUp() {
        assertThat(Reachability.of(wrapped(new EOFException())), is(Reachability.HOST_UP));
    }

    @Test
    public void testProtocolErrorIsAnswered() {
        assertThat(Reachability.of(new ExecutionException(new ProtocolError("bad reply"))), is(Reachability.ANSWERED));
    }

    @Test
    public void testStrongestProtocolFailureWins() {
        NoServiceError all = new NoServiceError("no protocol could be connected");
        all.addSuppressed(new ConnectionFailedError("mrp", new SocketTimeoutException()));
        all.addSuppressed(new ConnectionFailedError("companion", new ConnectException()));
        assertThat(Reachability.of(new ExecutionException(all)), is(Reachability.HOST_UP));

        all.addSuppressed(new ProtocolError("airplay"));
        assertThat(Reachability.of(new CompletionException(all)), is(Reachability.ANSWERED));
    }

    @Test
    public void testAllProtocolsTimingOutIsNoAnswer() {
        NoServiceError all = new NoServiceError("no protocol could be connected");
        all.addSuppressed(new ConnectionFailedError("mrp", new SocketTimeoutException()));
        all.addSuppressed(new ConnectionFailedError("companion", new NoRouteToHostException()));
        assertThat(Reachability.of(new ExecutionException(all)), is(Reachability.NO_ANSWER));
    }

    @Test
    public void unresolvableHostIsNoAnswer() {
        assertThat(Reachability.of(wrapped(new UnknownHostException("atv.lan"))), is(Reachability.NO_ANSWER));
    }

    @Test
    public void timeoutWithFailedCloseIsNoAnswer() {
        SocketTimeoutException timeout = new SocketTimeoutException("Connect timed out");
        timeout.addSuppressed(new IOException("close failed"));
        assertThat(Reachability.of(wrapped(timeout)), is(Reachability.NO_ANSWER));
    }

    private static ExecutionException wrapped(Exception socketFailure) {
        return new ExecutionException(new ConnectionFailedError("failed to connect", socketFailure));
    }
}
