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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tuya.internal.local.dto.RequestRefusal;
import org.openhab.binding.tuya.internal.local.dto.TcpStatusPayload;

import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;

/**
 * The {@link DpQueryRefusalHandlerTest} is a test class for {@link TuyaDevice.DpQueryRefusalHandler}
 *
 * @author Maciej Jarzebowski - Initial contribution
 */
@NonNullByDefault
public class DpQueryRefusalHandlerTest {
    private static final MessageWrapper<?> DP_QUERY = new MessageWrapper<>(CommandType.DP_QUERY,
            Map.of("dps", List.of(1)));
    private static final MessageWrapper<?> STATUS_QUERY = new MessageWrapper<>(CommandType.CONTROL,
            Map.of("dps", Map.of()));
    private static final MessageWrapper<?> PROBE = new MessageWrapper<>(CommandType.HEART_BEAT, Map.of());

    private EmbeddedChannel channel(ChannelHandler... handlers) {
        EmbeddedChannel channel = new EmbeddedChannel(handlers);
        channel.freezeTime();
        return channel;
    }

    private TuyaDevice.DpQueryRefusalHandler refusalHandler() {
        return new TuyaDevice.DpQueryRefusalHandler(List.of(DP_QUERY, STATUS_QUERY), PROBE);
    }

    private static MessageWrapper<?> refusal(CommandType commandType) {
        return new MessageWrapper<>(commandType, new RequestRefusal("json obj data unvalid"));
    }

    private static void pass(EmbeddedChannel channel, long milliseconds) {
        channel.advanceTimeBy(milliseconds, TimeUnit.MILLISECONDS);
        channel.runScheduledPendingTasks();
    }

    private static void assertHeartbeatSent(EmbeddedChannel channel) {
        assertSame(PROBE, channel.readOutbound());
        assertNull(channel.readOutbound());
    }

    private static void assertStatusQuerySent(EmbeddedChannel channel) {
        assertSame(DP_QUERY, channel.readOutbound());
        assertSame(STATUS_QUERY, channel.readOutbound());
        assertNull(channel.readOutbound());
    }

    @Test
    public void refusalIsAnsweredWithHeartbeat() {
        EmbeddedChannel channel = channel(refusalHandler());
        MessageWrapper<?> refusal = refusal(CommandType.DP_QUERY);

        channel.writeInbound(refusal);

        assertHeartbeatSent(channel);
        assertSame(refusal, channel.readInbound());
        channel.finishAndReleaseAll();
    }

    @Test
    public void deviceAnsweringHeartbeatKeepsTheConnection() {
        EmbeddedChannel channel = channel(
                new TuyaDevice.ResponseTimeoutHandler("device", "address", STATUS_QUERY, PROBE), refusalHandler());

        channel.writeOutbound(DP_QUERY, STATUS_QUERY);
        channel.writeInbound(refusal(CommandType.DP_QUERY));
        channel.writeInbound(new MessageWrapper<>(CommandType.HEART_BEAT, ""));
        pass(channel, TCP_CONNECTION_MESSAGE_RESPONSE + 1);

        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
    }

    @Test
    public void deviceNotAnsweringHeartbeatIsReconnected() {
        EmbeddedChannel channel = channel(
                new TuyaDevice.ResponseTimeoutHandler("device", "address", STATUS_QUERY, PROBE), refusalHandler());

        channel.writeOutbound(DP_QUERY, STATUS_QUERY);
        channel.writeInbound(refusal(CommandType.DP_QUERY));
        pass(channel, TCP_CONNECTION_PROBE_RESPONSE + 1);

        assertFalse(channel.isOpen());
    }

    @Test
    public void refusedStatusQueryIsRetriedWithDoublingInterval() {
        EmbeddedChannel channel = channel(refusalHandler());

        for (long interval : new long[] { 1000, 2000, 4000, 8000, 16000, 32000, 60000, 60000 }) {
            channel.writeInbound(refusal(CommandType.DP_QUERY));
            assertHeartbeatSent(channel);

            pass(channel, interval - 1);
            assertNull(channel.readOutbound());

            pass(channel, 1);
            assertStatusQuerySent(channel);
        }
        channel.finishAndReleaseAll();
    }

    @Test
    public void reportedStatusStartsRetriesOver() {
        EmbeddedChannel channel = channel(refusalHandler());
        channel.writeInbound(refusal(CommandType.DP_QUERY));
        pass(channel, 1000);
        channel.writeInbound(refusal(CommandType.DP_QUERY));
        channel.releaseOutbound();

        channel.writeInbound(new MessageWrapper<>(CommandType.DP_QUERY, new TcpStatusPayload()));
        pass(channel, 2000);
        assertNull(channel.readOutbound());

        channel.writeInbound(refusal(CommandType.DP_QUERY));
        assertHeartbeatSent(channel);
        pass(channel, 1000);
        assertStatusQuerySent(channel);
        channel.finishAndReleaseAll();
    }

    @Test
    public void subDeviceStatusDoesNotStopRetries() {
        EmbeddedChannel channel = channel(refusalHandler());
        TcpStatusPayload subDeviceStatus = new TcpStatusPayload();
        subDeviceStatus.cid = "e0f6bf69791fc50c";

        channel.writeInbound(refusal(CommandType.DP_QUERY));
        assertHeartbeatSent(channel);
        channel.writeInbound(new MessageWrapper<>(CommandType.DP_QUERY, subDeviceStatus));
        pass(channel, 1000);

        assertStatusQuerySent(channel);
        channel.finishAndReleaseAll();
    }

    @Test
    public void refusedCommandIsIgnored() {
        EmbeddedChannel channel = channel(refusalHandler());

        channel.writeInbound(refusal(CommandType.CONTROL));
        pass(channel, 1000);

        assertNull(channel.readOutbound());
        channel.finishAndReleaseAll();
    }
}
