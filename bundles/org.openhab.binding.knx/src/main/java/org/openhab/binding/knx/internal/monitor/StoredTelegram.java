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
package org.openhab.binding.knx.internal.monitor;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * A row as the store keeps it: only what was seen on the bus or sent, plus the outcome of an own send, which
 * arrives later for a read request.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class StoredTelegram implements OwnSend {

    /** Where the outcome of an own send goes once it has ended. */
    @FunctionalInterface
    interface EndListener {
        void ended(long number, SendOutcome outcome);
    }

    final long number;
    final Instant time;
    final IndividualAddress source;
    final GroupAddress destination;
    final TelegramType type;
    final Direction direction;
    final byte[] payload;
    final SecurityState security;
    final @Nullable NotDecryptedReason reason;
    final @Nullable String sentDataType;
    final @Nullable ValueText sentValue;
    final boolean plainOnSecuredAddress;
    private volatile @Nullable SendOutcome outcome;
    private final @Nullable EndListener endListener;

    StoredTelegram(long number, Instant time, IndividualAddress source, GroupAddress destination, TelegramType type,
            Direction direction, byte[] payload, SecurityState security, @Nullable NotDecryptedReason reason,
            @Nullable String sentDataType, @Nullable ValueText sentValue, @Nullable SendOutcome outcome,
            boolean plainOnSecuredAddress, @Nullable EndListener endListener) {
        this.number = number;
        this.time = time;
        this.source = source;
        this.destination = destination;
        this.type = type;
        this.direction = direction;
        this.payload = payload.clone();
        this.security = security;
        this.reason = reason;
        this.sentDataType = sentDataType;
        this.sentValue = sentValue;
        this.outcome = outcome;
        this.plainOnSecuredAddress = plainOnSecuredAddress;
        this.endListener = endListener;
    }

    @Override
    public void ended(SendOutcome outcome) {
        this.outcome = outcome;
        EndListener listener = endListener;
        if (listener != null) {
            listener.ended(number, outcome);
        }
    }

    @Nullable
    SendOutcome outcome() {
        return outcome;
    }
}
