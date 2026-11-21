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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.calimero.IndividualAddress;

/**
 * What the header of the monitor shows for a bridge.
 *
 * @param connection the current connection, null while not connected
 * @param bufferSize the number of rows the bridge keeps
 * @param rows the number of rows it holds now
 * @param received telegrams received since the bridge handler was created
 * @param sent telegrams openHAB sent since then, every attempt counted
 * @param notDecrypted secured telegrams that could not be decrypted, per reason, since the last connect
 * @param notDecryptedOtherReason those the library reported with an error type outside the known ones
 * @param plainOnSecuredAddress plain telegrams received for an address that has a group key
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record MonitorStatus(@Nullable Connection connection, int bufferSize, int rows, long received, long sent,
        Map<NotDecryptedReason, Long> notDecrypted, long notDecryptedOtherReason, long plainOnSecuredAddress) {

    public MonitorStatus {
        notDecrypted = Map.copyOf(notDecrypted);
    }

    /** {@return all secured telegrams that could not be decrypted since the last connect} */
    public long notDecryptedTotal() {
        return notDecrypted.values().stream().mapToLong(Long::longValue).sum() + notDecryptedOtherReason;
    }

    /**
     * @param kind tunnel, router or serial
     * @param ownAddress the address openHAB sends from
     */
    public record Connection(ConnectionKind kind, IndividualAddress ownAddress) {
    }
}
