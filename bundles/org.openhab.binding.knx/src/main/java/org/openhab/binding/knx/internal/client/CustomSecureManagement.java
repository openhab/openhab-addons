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
import java.util.function.IntUnaryOperator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.KNXAddress;
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

    /** Told about every received telegram the library rejected, with the library's error type. */
    @FunctionalInterface
    interface FailureListener {
        void failed(int errorType, IndividualAddress source, KNXAddress destination);
    }

    private final Logger logger = LoggerFactory.getLogger(CustomSecureManagement.class);
    private final ThreadLocal<@Nullable Boolean> securingGroupObject = new ThreadLocal<>();
    private volatile @Nullable FailureListener failureListener;

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

    void onFailure(@Nullable FailureListener listener) {
        failureListener = listener;
    }

    // Calimero reads and advances the sequence number without a lock, and commands, reads and read responses are
    // sent from different threads; two telegrams with the same number would make the receiver drop the second one.
    @Override
    @NonNullByDefault({})
    public synchronized Optional<byte[]> secureGroupObject(final IndividualAddress src, final GroupAddress dst,
            final byte[] apdu) throws InterruptedException {
        securingGroupObject.set(Boolean.TRUE);
        try {
            return super.secureGroupObject(src, dst, apdu);
        } finally {
            securingGroupObject.remove();
        }
    }

    /**
     * The library calls this for a received telegram it rejects, and also when it fails to secure one of ours; only
     * the first kind goes to the listener. A telegram of ours is secured inside {@link #secureGroupObject} on the
     * sending thread, a received one is checked on the notifier thread, so the call path tells them apart where the
     * source address would not.
     */
    @Override
    @NonNullByDefault({})
    protected void securityFailure(int errorType, IntUnaryOperator updateFunction, IndividualAddress src,
            KNXAddress dst, int ctrlExtended, long seqNo) {
        if (errorType >= 1 && errorType <= 4) {
            super.securityFailure(errorType, updateFunction, src, dst, ctrlExtended, seqNo);
        }
        FailureListener listener = failureListener;
        if (listener != null && securingGroupObject.get() == null) {
            try {
                listener.failed(errorType, src, dst);
            } catch (RuntimeException e) {
                logger.debug("The group monitor could not record a security failure: {}", e.toString());
            }
        }
    }

    /**
     * Calimero treats 0 as tool access, and a value of 1 makes it sync with the receiver first; a clock before the
     * epoch falls back to that.
     */
    static long initialSequenceNumber(final Instant now) {
        return Math.max(1, Duration.between(SEQUENCE_NUMBER_EPOCH, now).toMillis());
    }
}
