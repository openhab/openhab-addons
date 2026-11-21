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
 * One group telegram as the monitor hands it out.
 *
 * @param number running number per bridge, strictly increasing for the life of the bridge handler
 * @param time when the binding saw the telegram, or when the library call for an own send ended (a read request:
 *            when it was handed to the library)
 * @param source the sender; for an own send the address openHAB sends from
 * @param destination the group address
 * @param type write, read or response; unknown for a telegram that could not be decrypted
 * @param direction in or out
 * @param payload the plain value bytes; for an own write or response the bytes the library put into the frame;
 *            empty for a read request and for a telegram that could not be decrypted
 * @param security plain, secured, or secured and not decrypted
 * @param reason why a secured telegram could not be decrypted, {@code null} otherwise
 * @param outcome what became of an own send, {@code null} for a received telegram
 * @param dataType the data type the value was decoded with, {@code null} if none is known
 * @param value the value as text, {@code null} if there is none or it could not be decoded
 * @param unit the unit of {@code value}, if any
 * @param sourceName the name of the sender, if known
 * @param destinationName the name of the group address, if known
 * @param plainOnSecuredAddress a received plain telegram to an address that has a group key; the library passes it
 *            on as a valid value
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record TelegramRecord(long number, Instant time, IndividualAddress source, GroupAddress destination,
        TelegramType type, Direction direction, byte[] payload, SecurityState security,
        @Nullable NotDecryptedReason reason, @Nullable SendOutcome outcome, @Nullable String dataType,
        @Nullable String value, @Nullable String unit, @Nullable String sourceName, @Nullable String destinationName,
        boolean plainOnSecuredAddress) {

    public TelegramRecord {
        payload = payload.clone();
    }

    @Override
    public byte[] payload() {
        return payload.clone();
    }
}
