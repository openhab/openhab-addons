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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a DNS question.
 *
 * @param qname queried name
 * @param qtype record type (see {@link QueryType})
 * @param qclass query class (mDNS scans use {@code 0x8001}: class IN with the
 *            unicast-response bit set)
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record DnsQuestion(String qname, int qtype, int qclass) {

    /**
     * Creates a question from a typed query type.
     *
     * @param qname queried name
     * @param qtype record type
     * @param qclass query class
     */
    public DnsQuestion(String qname, QueryType qtype, int qclass) {
        this(qname, qtype.value(), qclass);
    }

    /**
     * Creates a {@code DnsQuestion} from a data stream, leaving the buffer positioned
     * after the question.
     *
     * @param buffer buffer positioned at the question
     * @return the parsed question
     */
    public static DnsQuestion unpackRead(ByteBuffer buffer) {
        String qname = Dns.parseDomainName(buffer);
        int qtype = buffer.getShort() & 0xFFFF;
        int qclass = buffer.getShort() & 0xFFFF;
        return new DnsQuestion(qname, qtype, qclass);
    }

    /**
     * Encodes the question data as needed for a DNS query or response.
     *
     * @return the encoded question
     */
    public byte[] pack() {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.writeBytes(Dns.qnameEncode(qname));
        data.write((qtype >> 8) & 0xFF);
        data.write(qtype & 0xFF);
        data.write((qclass >> 8) & 0xFF);
        data.write(qclass & 0xFF);
        return data.toByteArray();
    }
}
