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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECTION_MESSAGE_RESPONSE;

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

    private EmbeddedChannel channel() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new TuyaDevice.ResponseTimeoutHandler("device", "address", STATUS_QUERY));
        channel.freezeTime();
        return channel;
    }

    private void letResponseTimeoutPass(EmbeddedChannel channel) {
        channel.advanceTimeBy(TCP_CONNECTION_MESSAGE_RESPONSE + 1, TimeUnit.MILLISECONDS);
        channel.runScheduledPendingTasks();
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

        assertFalse(channel.isOpen());
    }
}
