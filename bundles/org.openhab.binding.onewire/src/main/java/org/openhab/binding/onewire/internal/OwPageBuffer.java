/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.onewire.internal;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwPageBuffer} provides encapsulates a buffer for OwPacket payloads
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class OwPageBuffer {
    private final Logger logger = LoggerFactory.getLogger(OwPageBuffer.class);
    public static final int PAGE_SIZE = 8;

    private ByteBuffer byteBuffer;

    /**
     * constructor for empty buffer
     *
     */
    public OwPageBuffer() {
        byteBuffer = ByteBuffer.allocate(0);
    }

    /**
     * constructor for new buffer of given length
     *
     * @param pageNum number of pages
     */
    public OwPageBuffer(int pageNum) {
        byteBuffer = ByteBuffer.allocate(pageNum * PAGE_SIZE);
    }

    /**
     * constructor for given byte array
     *
     * @param bytes byte array containing the data
     */
    public OwPageBuffer(byte[] bytes) {
        if (bytes.length % PAGE_SIZE != 0) {
            byteBuffer = ByteBuffer.allocate((bytes.length / PAGE_SIZE + 1) * PAGE_SIZE);
            logger.warn("initializing buffer which is not aligned to pages (requested size is {})!", bytes.length);
        } else {
            byteBuffer = ByteBuffer.allocate(bytes.length);
        }

        byteBuffer.put(bytes);
    }

    /**
     * get number of pages in this buffer
     *
     * @return number of pages
     */
    public int length() {
        return byteBuffer.limit() / PAGE_SIZE;
    }

    /**
     * get a single page as byte array
     *
     * @param pageNum page number, starting with 0
     * @return byte array containing the page's data
     */
    public byte[] getPage(int pageNum) {
        byte[] page = new byte[PAGE_SIZE];
        byteBuffer.position(pageNum * PAGE_SIZE);
        byteBuffer.get(page);
        return page;
    }

    /**
     * get a single page
     *
     * @param pageNum page number, starting with 0
     * @return string representation of the page's data
     */
    public String getPageString(int pageNum) {
        return HexUtils.bytesToHex(getPage(pageNum));
    }

    /**
     * get a single byte in a page
     *
     * @param pageNum page number, starting with 0
     * @param byteNum byte number, starting from 0 (beginning of page)
     * @return integer of the requested byte
     */
    public int getByte(int pageNum, int byteNum) {
        int index = pageNum * PAGE_SIZE + byteNum;
        if (index < byteBuffer.limit()) {
            return byteBuffer.get(index) & 0xFF;
        } else {
            return 0;
        }
    }

    public void setByte(int pageNum, int byteNum, byte value) {
        int index = pageNum * PAGE_SIZE + byteNum;
        if (index < byteBuffer.limit()) {
            byteBuffer.put(index, value);
        } else {
            throw new IllegalArgumentException("index out of range");
        }
    }

    /**
     * get this page buffer as byte array
     *
     * @return this page buffer as byte array
     */
    public byte[] getBytes() {
        return byteBuffer.array();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(new String("["));
        for (int i = 0; i < length(); i++) {
            if (i > 0) {
                s.append(new String(", "));
            }
            s.append(getPageString(i));
        }
        s.append(new String("]"));
        return s.toString();
    }
}
