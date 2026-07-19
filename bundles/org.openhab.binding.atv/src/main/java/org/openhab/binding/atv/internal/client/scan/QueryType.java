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
package org.openhab.binding.atv.internal.client.scan;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A DNS record type ID.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum QueryType {

    /** IPv4 host address record. */
    A(0x01),
    /** Domain name pointer record. */
    PTR(0x0C),
    /** Text record. */
    TXT(0x10),
    /** Service locator record. */
    SRV(0x21),
    /** Query for all record types. */
    ANY(0xFF);

    private final int value;

    QueryType(int value) {
        this.value = value;
    }

    /**
     * Returns the on-wire type value.
     *
     * @return the numeric record type
     */
    public int value() {
        return value;
    }

    /**
     * Looks up a query type from its on-wire value.
     *
     * @param value numeric record type
     * @return the matching type or {@code null} if unknown
     */
    public static @Nullable QueryType fromValue(int value) {
        for (QueryType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }

    /**
     * Parses the RDATA from a DNS resource record according to the type of the record.
     *
     * <p>
     * Returns a {@link String} for A (dotted IPv4) and PTR (domain name), a
     * {@code CaseInsensitiveMap<byte[]>} for TXT, a {@link SrvRecord} for SRV and a raw
     * {@code byte[]} for anything else.
     *
     * @param buffer buffer positioned at the RDATA
     * @param length RDATA length in bytes
     * @return the parsed data
     */
    public Object parseRdata(ByteBuffer buffer, int length) {
        switch (this) {
            case A: {
                if (length != 4) {
                    throw new IllegalArgumentException(
                            "An A record must have exactly 4 bytes of data (not " + length + ")");
                }
                return (buffer.get() & 0xFF) + "." + (buffer.get() & 0xFF) + "." + (buffer.get() & 0xFF) + "."
                        + (buffer.get() & 0xFF);
            }
            case PTR:
                return Dns.parseDomainName(buffer);
            case TXT:
                return Dns.parseTxtDict(buffer, length);
            case SRV:
                return Dns.parseSrvRecord(buffer);
            default: {
                byte[] data = new byte[length];
                buffer.get(data);
                return data;
            }
        }
    }
}
