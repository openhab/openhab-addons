/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.fields.MACAddress;

/**
 * The {@link LifxSelectorContext} stores the context that is used for broadcast and unicast communications with a
 * light using a {@link Selector}.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxSelectorContext {

    private final Selector selector;
    private final long sourceId;
    private final Supplier<Integer> sequenceNumberSupplier;
    private final String logId;
    private @Nullable InetSocketAddress host;
    private @Nullable MACAddress macAddress;
    private @Nullable SelectionKey broadcastKey;
    private @Nullable SelectionKey unicastKey;

    public LifxSelectorContext(Selector selector, long sourceId, Supplier<Integer> sequenceNumberSupplier, String logId,
            @Nullable SelectionKey broadcastKey) {
        this(selector, sourceId, sequenceNumberSupplier, logId, null, null, broadcastKey, null);
    }

    public LifxSelectorContext(Selector selector, long sourceId, Supplier<Integer> sequenceNumberSupplier, String logId,
            @Nullable InetSocketAddress host, @Nullable MACAddress macAddress, @Nullable SelectionKey broadcastKey,
            @Nullable SelectionKey unicastKey) {
        this.selector = selector;
        this.sourceId = sourceId;
        this.sequenceNumberSupplier = sequenceNumberSupplier;
        this.logId = logId;
        this.host = host;
        this.macAddress = macAddress;
        this.broadcastKey = broadcastKey;
        this.unicastKey = unicastKey;
    }

    public Selector getSelector() {
        return selector;
    }

    public long getSourceId() {
        return sourceId;
    }

    public Supplier<Integer> getSequenceNumberSupplier() {
        return sequenceNumberSupplier;
    }

    public String getLogId() {
        return logId;
    }

    public @Nullable InetSocketAddress getHost() {
        return host;
    }

    public @Nullable MACAddress getMACAddress() {
        return macAddress;
    }

    public @Nullable SelectionKey getBroadcastKey() {
        return broadcastKey;
    }

    public @Nullable SelectionKey getUnicastKey() {
        return unicastKey;
    }

    public void setHost(@Nullable InetSocketAddress host) {
        this.host = host;
    }

    public void setMACAddress(@Nullable MACAddress macAddress) {
        this.macAddress = macAddress;
    }

    public void setBroadcastKey(@Nullable SelectionKey broadcastKey) {
        this.broadcastKey = broadcastKey;
    }

    public void setUnicastKey(@Nullable SelectionKey unicastKey) {
        this.unicastKey = unicastKey;
    }
}
