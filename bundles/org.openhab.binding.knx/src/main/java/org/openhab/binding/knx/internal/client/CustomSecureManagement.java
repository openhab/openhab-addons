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
package org.openhab.binding.knx.internal.client;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.SerialNumber;
import io.calimero.link.KNXLinkClosedException;
import io.calimero.mgmt.SecureManagement;
import io.calimero.mgmt.TransportLayer;
import io.calimero.mgmt.TransportLayerImpl;
import io.calimero.secure.Security;

/**
 * This class is to provide access to protected constructors in the Calimero library.
 * Reason is to provide custom KNX keyring data.
 *
 * @author Holger Friedrich - initial contribution
 *
 */
@NonNullByDefault
public class CustomSecureManagement extends SecureManagement {
    // Receivers drop a secured telegram unless its sequence number is above the last one they accepted from this
    // sender, so it has to keep growing across restarts. KNX Falcon and xknx count milliseconds since this date.
    private static final Instant SEQUENCE_NUMBER_EPOCH = Instant.parse("2018-01-05T00:00:00Z");

    public CustomSecureManagement(final TransportLayer transportLayer, final Security security)
            throws KNXLinkClosedException {
        // super(link, secureManagement) is not yet available in Calimero 2.5.1
        super((TransportLayerImpl) transportLayer, SerialNumber.Zero, initialSequenceNumber(Instant.now()),
                security.deviceToolKeys());

        // instance of Security has been created in ctor of SecureManagement, but only using deviceToolKeys
        // no need to clear, just copy (otherwise SAL would lack the group keys)
        final Security sal = security();
        sal.groupKeys().putAll(security.groupKeys());
        sal.groupSenders().putAll(security.groupSenders());
    }

    public final Security security() {
        return super.security();
    }

    // Calimero reads and advances the sequence number without a lock, and commands, reads and read responses are
    // sent from different threads; two telegrams with the same number would make the receiver drop the second one.
    @Override
    @NonNullByDefault({})
    public synchronized Optional<byte[]> secureGroupObject(final IndividualAddress src, final GroupAddress dst,
            final byte[] apdu) throws InterruptedException {
        return super.secureGroupObject(src, dst, apdu);
    }

    /**
     * Calimero treats 0 as tool access, and a value of 1 makes it sync with the receiver first; a clock before the
     * epoch falls back to that.
     */
    static long initialSequenceNumber(final Instant now) {
        return Math.max(1, Duration.between(SEQUENCE_NUMBER_EPOCH, now).toMillis());
    }
}
