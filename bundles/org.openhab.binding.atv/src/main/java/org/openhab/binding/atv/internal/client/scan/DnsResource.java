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
 * Represents a DNS resource record.
 *
 * <p>
 * The RDATA is typed according to the record type when parsed (see
 * {@link QueryType#parseRdata}): {@link String} for A/PTR, {@code CaseInsensitiveMap<byte[]>}
 * for TXT, {@link SrvRecord} for SRV and raw {@code byte[]} otherwise. When constructing
 * a message for {@link DnsMessage#pack()}, answers must carry a {@link String} domain
 * name and authority/additional records a raw {@code byte[]}.
 *
 * @param qname record name
 * @param qtype record type value
 * @param qclass record class
 * @param ttl time to live in seconds
 * @param rdLength length of the RDATA on the wire
 * @param rd parsed or raw RDATA
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record DnsResource(String qname, int qtype, int qclass, long ttl, int rdLength, Object rd) {

    /**
     * Creates a resource record from a typed query type.
     *
     * @param qname record name
     * @param qtype record type
     * @param qclass record class
     * @param ttl time to live in seconds
     * @param rdLength length of the RDATA on the wire
     * @param rd parsed or raw RDATA
     */
    public DnsResource(String qname, QueryType qtype, int qclass, long ttl, int rdLength, Object rd) {
        this(qname, qtype.value(), qclass, ttl, rdLength, rd);
    }

    /**
     * Creates a {@code DnsResource} from data in a data stream. All data from the
     * record is consumed, leaving the buffer ready for the next record.
     *
     * @param buffer buffer positioned at the record
     * @return the parsed record
     * @throws IllegalArgumentException if the parsed RDATA does not consume exactly
     *             {@code rdLength} bytes
     */
    public static DnsResource unpackRead(ByteBuffer buffer) {
        String qname = Dns.parseDomainName(buffer);
        int qtype = buffer.getShort() & 0xFFFF;
        int qclass = buffer.getShort() & 0xFFFF;
        long ttl = buffer.getInt() & 0xFFFFFFFFL;
        int rdLength = buffer.getShort() & 0xFFFF;
        int beforeRd = buffer.position();
        @Nullable
        QueryType type = QueryType.fromValue(qtype);
        Object rd;
        if (type != null) {
            rd = type.parseRdata(buffer, rdLength);
        } else {
            byte[] data = new byte[rdLength];
            buffer.get(data);
            rd = data;
        }
        if (buffer.position() != beforeRd + rdLength) {
            throw new IllegalArgumentException("RDATA length mismatch for " + qname);
        }
        return new DnsResource(qname, qtype, qclass, ttl, rdLength, rd);
    }
}
