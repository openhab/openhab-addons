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
package org.openhab.binding.tuya.internal.local;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECTION_MESSAGE_RESPONSE;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECTION_PROBE_RESPONSE;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tuya.internal.local.dto.RequestRefusal;

import io.netty.channel.embedded.EmbeddedChannel;

/**
 * The {@link ResponseTimeoutHandlerTest} is a test class for {@link TuyaDevice.ResponseTimeoutHandler}
 *
 * @author Maciej Jarzebowski - Initial contribution
 */
@NonNullByDefault
public class ResponseTimeoutHandlerTest {
    private static final MessageWrapper<?> STATUS_QUERY = new MessageWrapper<>(CommandType.CONTROL,
            Map.of("dps", Map.of()));
    private static final MessageWrapper<?> PROBE = new MessageWrapper<>(CommandType.HEART_BEAT, Map.of());

    private EmbeddedChannel channel() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new TuyaDevice.ResponseTimeoutHandler("device", "address", STATUS_QUERY, PROBE));
        channel.freezeTime();
        return channel;
    }

    private void letResponseTimeoutPass(EmbeddedChannel channel) {
        channel.advanceTimeBy(TCP_CONNECTION_MESSAGE_RESPONSE + 1, TimeUnit.MILLISECONDS);
        channel.runScheduledPendingTasks();
    }

    private void letProbeTimeoutPass(EmbeddedChannel channel) {
        channel.advanceTimeBy(TCP_CONNECTION_PROBE_RESPONSE + 1, TimeUnit.MILLISECONDS);
        channel.runScheduledPendingTasks();
    }

    private void assertProbeSent(EmbeddedChannel channel) {
        // the messages the test wrote itself are still queued, the probe is the one sent last
        Object last = null;
        for (Object sent = channel.readOutbound(); sent != null; sent = channel.readOutbound()) {
            last = sent;
        }
        assertSame(PROBE, last);
    }

    @Test
    public void acknowledgedCommandKeepsTheConnection() {
        EmbeddedChannel channel = channel();

        channel.writeOutbound(new MessageWrapper<>(CommandType.CONTROL_NEW, Map.of("dps", Map.of(201, "send_ir"))));
        // An infrared remote only acknowledges a command, it has no status to report in return
        channel.writeInbound(new MessageWrapper<>(CommandType.CONTROL_NEW, ""));
        letResponseTimeoutPass(channel);

        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
    }

    @Test
    public void unansweredCommandClosesTheConnection() {
        EmbeddedChannel channel = channel();

        channel.writeOutbound(new MessageWrapper<>(CommandType.CONTROL_NEW, Map.of("dps", Map.of(1, true))));
        letResponseTimeoutPass(channel);
        // the device is asked whether it is still there before the connection is given up on
        assertTrue(channel.isOpen());
        assertProbeSent(channel);
        letProbeTimeoutPass(channel);

        assertFalse(channel.isOpen());
    }

    @Test
    public void acknowledgedStatusQueryStillClosesTheConnection() {
        EmbeddedChannel channel = channel();

        channel.writeOutbound(STATUS_QUERY);
        // A device that accepts the connection before its API is ready acknowledges the query without reporting a
        // status, and only a new connection recovers it
        channel.writeInbound(new MessageWrapper<>(CommandType.CONTROL, ""));
        letResponseTimeoutPass(channel);
        // the device is asked whether it is still there before the connection is given up on
        assertTrue(channel.isOpen());
        assertProbeSent(channel);
        letProbeTimeoutPass(channel);

        assertFalse(channel.isOpen());
    }

    @Test
    public void statusReplyToStatusQueryKeepsTheConnection() {
        EmbeddedChannel channel = channel();

        channel.writeOutbound(STATUS_QUERY);
        channel.writeInbound(new MessageWrapper<>(CommandType.STATUS, ""));
        letResponseTimeoutPass(channel);

        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
    }

    @Test
    public void refusedStatusQueryStillClosesTheConnection() {
        EmbeddedChannel channel = channel();

        channel.writeOutbound(STATUS_QUERY);
        // A battery device woken too early refuses DP_QUERY and ignores the CONTROL; only a new connection recovers it
        channel.writeInbound(new MessageWrapper<>(CommandType.DP_QUERY, new RequestRefusal("json obj data unvalid")));
        letResponseTimeoutPass(channel);
        // the device is asked whether it is still there before the connection is given up on
        assertTrue(channel.isOpen());
        assertProbeSent(channel);
        letProbeTimeoutPass(channel);

        assertFalse(channel.isOpen());
    }

    @Test
    public void probeIsGivenMoreTimeToBeAnswered() {
        EmbeddedChannel channel = channel();

        channel.writeOutbound(PROBE);
        letResponseTimeoutPass(channel);

        // A gateway relaying the traffic of its sub-devices may take longer than the usual response timeout
        assertTrue(channel.isOpen());

        letProbeTimeoutPass(channel);

        assertFalse(channel.isOpen());
    }

    @Test
    public void answeredProbeKeepsTheConnection() {
        EmbeddedChannel channel = channel();

        channel.writeOutbound(PROBE);
        channel.writeInbound(new MessageWrapper<>(CommandType.HEART_BEAT, ""));
        channel.advanceTimeBy(TCP_CONNECTION_PROBE_RESPONSE + 1, TimeUnit.MILLISECONDS);
        channel.runScheduledPendingTasks();

        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
    }

    @Test
    public void connectionCheckedAfterTheTimeoutKeepsTheConnection() {
        EmbeddedChannel channel = channel();

        channel.writeOutbound(new MessageWrapper<>(CommandType.CONTROL_NEW, Map.of("dps", Map.of(1, true))));
        letResponseTimeoutPass(channel);
        assertProbeSent(channel);

        // The device was only slow to answer, so the connection is kept
        channel.writeInbound(new MessageWrapper<>(CommandType.HEART_BEAT, ""));
        letProbeTimeoutPass(channel);

        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
    }

    @Test
    public void connectionIsCheckedOnlyOnce() {
        EmbeddedChannel channel = channel();

        channel.writeOutbound(STATUS_QUERY);
        letResponseTimeoutPass(channel);
        assertProbeSent(channel);
        letProbeTimeoutPass(channel);

        assertFalse(channel.isOpen());
        assertNull(channel.readOutbound());
    }

    @Test
    public void writeDuringTheConnectionCheckDoesNotExtendIt() {
        EmbeddedChannel channel = channel();

        channel.writeOutbound(STATUS_QUERY);
        letResponseTimeoutPass(channel);
        assertProbeSent(channel);

        // Sending to a device that is being checked must not buy it time
        channel.writeOutbound(new MessageWrapper<>(CommandType.CONTROL_NEW, Map.of("dps", Map.of(1, true))));
        letProbeTimeoutPass(channel);

        assertFalse(channel.isOpen());
    }

    @Test
    public void commandAcknowledgedDuringTheConnectionCheckKeepsTheConnection() {
        EmbeddedChannel channel = channel();

        channel.writeOutbound(STATUS_QUERY);
        letResponseTimeoutPass(channel);
        assertProbeSent(channel);

        channel.writeOutbound(new MessageWrapper<>(CommandType.CONTROL_NEW, Map.of("dps", Map.of(1, true))));
        // The acknowledgement proves the device is there, which is what the check asked
        channel.writeInbound(new MessageWrapper<>(CommandType.CONTROL_NEW, ""));
        letProbeTimeoutPass(channel);

        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
    }
}
