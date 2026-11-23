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
package org.openhab.binding.tibber.internal.pulse.stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Receives the events of a {@link StreamClient}.
 *
 * All callbacks run on the single reader thread of the client, one at a time and in order. A callback may block the
 * stream, so it must return quickly. No callback starts after {@link StreamClient#stop()} has been called, and when
 * {@code stop()} returns normally none is running any more (it joins the reader thread). If the join times out
 * (a callback is stuck in foreign code), the running callback can still finish but no new one starts.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public interface StreamListener {

    /**
     * The first websocket frame of an attempt arrived (any frame, including a ping). On this bridge nothing else
     * proves that the stream was accepted, because there is no HTTP response. Called at most once per attempt.
     */
    void onStreamActive();

    /**
     * A complete message arrived. The array is owned by the callee.
     *
     * @param message header and payload as sent by the bridge, to be parsed with {@link Envelope#parse(byte[])}
     * @return true if the message produced a valid reading (a valid SML frame with at least one applicable entry),
     *         regardless of whether a meter thing consumes it. The backoff is reset only after a stream stayed open
     *         for 30 seconds following the first message for which this returned true.
     */
    boolean onMessage(byte[] message);

    /**
     * The attempt ended. Called once per ended attempt, never after {@link StreamClient#stop()}.
     *
     * @param failure why the attempt ended; {@link StreamFailure#isFinal()} tells whether the client gives up
     */
    void onStreamLost(StreamFailure failure);
}
