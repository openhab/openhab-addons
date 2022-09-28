/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.echonetlite.internal;

import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.DEFAULT_RETRY_TIMEOUT_MS;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public abstract class EchonetObject {

    private final Logger logger = LoggerFactory.getLogger(EchonetObject.class);

    protected final InstanceKey instanceKey;
    protected final HashSet<Epc> pendingGets = new HashSet<>();

    protected InflightRequest inflightGetRequest = new InflightRequest(DEFAULT_RETRY_TIMEOUT_MS, "GET");
    protected InflightRequest inflightSetRequest = new InflightRequest(DEFAULT_RETRY_TIMEOUT_MS, "SET");

    protected long pollIntervalMs;

    public EchonetObject(final InstanceKey instanceKey, final Epc initialProperty) {
        this.instanceKey = instanceKey;
        pendingGets.add(initialProperty);
    }

    public InstanceKey instanceKey() {
        return instanceKey;
    }

    public void applyProperty(InstanceKey sourceInstanceKey, Esv esv, final int epcCode, final int pdc,
            final ByteBuffer edt) {
    }

    public boolean buildPollMessage(final EchonetMessageBuilder messageBuilder, final ShortSupplier tidSupplier,
            long nowMs, InstanceKey managementControllerKey) {
        if (pendingGets.isEmpty()) {
            return false;
        }

        if (hasInflight(nowMs, this.inflightGetRequest)) {
            return false;
        }

        final short tid = tidSupplier.getAsShort();
        messageBuilder.start(tid, managementControllerKey, instanceKey(), Esv.Get);

        for (Epc pendingProperty : pendingGets) {
            messageBuilder.appendEpcRequest(pendingProperty.code());
        }

        this.inflightGetRequest.requestSent(tid, nowMs);

        return true;
    }

    protected boolean hasInflight(long nowMs, InflightRequest inflightRequest) {
        if (inflightRequest.isInflight()) {
            return !inflightRequest.hasTimedOut(nowMs);
        }
        return false;
    }

    protected void setTimeouts(long pollIntervalMs, long retryTimeoutMs) {
        this.pollIntervalMs = pollIntervalMs;
        this.inflightGetRequest = new InflightRequest(retryTimeoutMs, inflightGetRequest);
        this.inflightSetRequest = new InflightRequest(retryTimeoutMs, inflightSetRequest);
    }

    public boolean buildUpdateMessage(final EchonetMessageBuilder messageBuilder, final ShortSupplier tid,
            final long nowMs, InstanceKey managementControllerKey) {
        return false;
    }

    public void refreshAll(long nowMs) {
    }

    public String toString() {
        return "ItemBase{" + "instanceKey=" + instanceKey + ", pendingProperties=" + pendingGets + '}';
    }

    public void update(String channelId, State state) {
    }

    public void removed() {
    }

    public void refresh(String channelId) {
    }

    public void applyHeader(Esv esv, short tid, long nowMs) {
        if ((esv == Esv.Get_Res || esv == Esv.Get_SNA)) {
            final long sentTimestampMs = this.inflightGetRequest.timestampMs;
            if (this.inflightGetRequest.responseReceived(tid)) {
                logger.debug("{} response time: {}ms", esv, nowMs - sentTimestampMs);
            } else {
                logger.warn("Unexpected {} response: {}", esv, tid);
                this.inflightGetRequest.checkOldResponse(tid, nowMs);
            }
        } else if ((esv == Esv.Set_Res || esv == Esv.SetC_SNA)) {
            final long sentTimestampMs = this.inflightSetRequest.timestampMs;
            if (this.inflightSetRequest.responseReceived(tid)) {
                logger.debug("{} response time: {}ms", esv, nowMs - sentTimestampMs);
            } else {
                logger.warn("Unexpected {} response: {}", esv, tid);
                this.inflightSetRequest.checkOldResponse(tid, nowMs);
            }
        }
    }

    public void checkTimeouts() {
    }

    protected static class InflightRequest {
        private static final long NULL_TIMESTAMP = -1;

        private final Logger logger = LoggerFactory.getLogger(InflightRequest.class);
        private final long timeoutMs;
        private final String name;
        private final Map<Short, Long> oldRequests = new HashMap<>();

        private short tid;
        private long timestampMs = NULL_TIMESTAMP;
        @SuppressWarnings("unused")
        private int timeoutCount = 0;

        InflightRequest(long timeoutMs, InflightRequest existing) {
            this(timeoutMs, existing.name);
            this.tid = existing.tid;
            this.timestampMs = existing.timestampMs;
        }

        InflightRequest(long timeoutMs, String name) {
            this.timeoutMs = timeoutMs;
            this.name = name;
        }

        void requestSent(short tid, long timestampMs) {
            this.tid = tid;
            this.timestampMs = timestampMs;
        }

        boolean responseReceived(short tid) {
            timestampMs = NULL_TIMESTAMP;
            timeoutCount = 0;

            return this.tid == tid;
        }

        boolean hasTimedOut(long nowMs) {
            final boolean timedOut = timestampMs + timeoutMs <= nowMs;
            if (timedOut) {
                logger.debug("Timed out {}, tid={}, timestampMs={} + timeoutMs={} <= nowMs={}", name, tid, timestampMs,
                        timeoutMs, nowMs);
                timeoutCount++;

                if (NULL_TIMESTAMP != tid) {
                    oldRequests.put(tid, timestampMs);
                }
            }
            return timedOut;
        }

        public boolean isInflight() {
            return NULL_TIMESTAMP != timestampMs;
        }

        public void checkOldResponse(short tid, long nowMs) {
            final Long oldResponseTimestampMs = oldRequests.remove(tid);
            if (null != oldResponseTimestampMs) {
                logger.debug("Timed out request, tid={}, actually took={}", tid, nowMs - oldResponseTimestampMs);
            }
        }

        public int timeoutCount() {
            return timeoutCount;
        }
    }
}
