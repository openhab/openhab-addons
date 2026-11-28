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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a DNS message, including its header.
 *
 * <p>
 * Only what the scanner needs is supported: header, questions and PTR/SRV/TXT/A
 * resource records with name compression on decode (the QNAME encoder never
 * compresses).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class DnsMessage {

    private int msgId;
    private int flags;
    private final List<DnsQuestion> questions = new ArrayList<>();
    private final List<DnsResource> answers = new ArrayList<>();
    private final List<DnsResource> authorities = new ArrayList<>();
    private final List<DnsResource> resources = new ArrayList<>();

    /**
     * Creates a new message with id 0 and the default flags ({@code 0x0120}).
     */
    public DnsMessage() {
        this(0, 0x0120);
    }

    /**
     * Creates a new message.
     *
     * @param msgId message id
     */
    public DnsMessage(int msgId) {
        this(msgId, 0x0120);
    }

    /**
     * Creates a new message.
     *
     * @param msgId message id
     * @param flags header flags
     */
    public DnsMessage(int msgId, int flags) {
        this.msgId = msgId;
        this.flags = flags;
    }

    /**
     * Returns the message id.
     *
     * @return message id
     */
    public int msgId() {
        return msgId;
    }

    /**
     * Returns the header flags.
     *
     * @return header flags
     */
    public int flags() {
        return flags;
    }

    /**
     * Sets the header flags.
     *
     * @param flags new flags
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Returns the (mutable) question section.
     *
     * @return questions
     */
    public List<DnsQuestion> questions() {
        return questions;
    }

    /**
     * Returns the (mutable) answer section.
     *
     * @return answers
     */
    public List<DnsResource> answers() {
        return answers;
    }

    /**
     * Returns the (mutable) authority section.
     *
     * @return authorities
     */
    public List<DnsResource> authorities() {
        return authorities;
    }

    /**
     * Returns the (mutable) additional resource section.
     *
     * @return additional resources
     */
    public List<DnsResource> resources() {
        return resources;
    }

    /**
     * Unpacks bytes into this message.
     *
     * @param msg raw message bytes
     * @return this message, for chaining
     */
    public DnsMessage unpack(byte[] msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg);

        msgId = buffer.getShort() & 0xFFFF;
        flags = buffer.getShort() & 0xFFFF;
        int qdCount = buffer.getShort() & 0xFFFF;
        int anCount = buffer.getShort() & 0xFFFF;
        int nsCount = buffer.getShort() & 0xFFFF;
        int arCount = buffer.getShort() & 0xFFFF;

        for (int i = 0; i < qdCount; i++) {
            questions.add(DnsQuestion.unpackRead(buffer));
        }
        for (int i = 0; i < anCount; i++) {
            answers.add(DnsResource.unpackRead(buffer));
        }
        for (int i = 0; i < nsCount; i++) {
            authorities.add(DnsResource.unpackRead(buffer));
        }
        for (int i = 0; i < arCount; i++) {
            resources.add(DnsResource.unpackRead(buffer));
        }
        return this;
    }

    /**
     * Packs this message into bytes. Answer records must carry a {@link String} RDATA
     * (encoded as a domain name); authority and additional records must carry raw
     * {@code byte[]} RDATA.
     *
     * @return the encoded message
     */
    public byte[] pack() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        writeShort(buf, msgId);
        writeShort(buf, flags);
        writeShort(buf, questions.size());
        writeShort(buf, answers.size());
        writeShort(buf, authorities.size());
        writeShort(buf, resources.size());

        for (DnsQuestion question : questions) {
            buf.writeBytes(question.pack());
        }

        for (DnsResource answer : answers) {
            byte[] data = Dns.qnameEncode((String) answer.rd());
            buf.writeBytes(Dns.qnameEncode(answer.qname()));
            writeShort(buf, answer.qtype());
            writeShort(buf, answer.qclass());
            writeInt(buf, answer.ttl());
            writeShort(buf, data.length);
            buf.writeBytes(data);
        }

        for (List<DnsResource> section : List.of(authorities, resources)) {
            for (DnsResource resource : section) {
                byte[] data = (byte[]) resource.rd();
                buf.writeBytes(Dns.qnameEncode(resource.qname()));
                writeShort(buf, resource.qtype());
                writeShort(buf, resource.qclass());
                writeInt(buf, resource.ttl());
                writeShort(buf, data.length);
                buf.writeBytes(data);
            }
        }
        return buf.toByteArray();
    }

    private static void writeShort(ByteArrayOutputStream buf, int value) {
        buf.write((value >> 8) & 0xFF);
        buf.write(value & 0xFF);
    }

    private static void writeInt(ByteArrayOutputStream buf, long value) {
        buf.write((int) ((value >> 24) & 0xFF));
        buf.write((int) ((value >> 16) & 0xFF));
        buf.write((int) ((value >> 8) & 0xFF));
        buf.write((int) (value & 0xFF));
    }

    @Override
    public String toString() {
        return String.format("MsgId=0x%04X%nFlags=0x%04X%nQuestions=%s%nAnswers=%s%nAuthorities=%s%nResources=%s",
                msgId, flags, questions, answers, authorities, resources);
    }
}
