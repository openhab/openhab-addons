/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sinope.internal.core.base;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.openhab.binding.sinope.internal.core.appdata.SinopeAppData;
import org.openhab.binding.sinope.internal.util.ByteUtil;

/**
 * The Class SinopeDataRequest.
 *
 * @author Pascal Larin - Initial contribution
 */
public abstract class SinopeDataRequest extends SinopeRequest {

    /** The seq. */
    private byte[] seq;

    /** The request type. */
    private byte requestType;

    /** The res 1. */
    private byte res1;

    /** The res 2. */
    private byte res2;

    /** The res 3. */
    private byte[] res3;

    /** The res 4. */
    private byte[] res4;

    /** The dst device id. */
    private byte[] dstDeviceId;

    /** The app data. */
    private SinopeAppData appData;

    /**
     * Instantiates a new sinope data request.
     *
     * @param seq the seq
     * @param dstDeviceId the dst device id
     * @param appData the app data
     */
    public SinopeDataRequest(byte[] seq, byte[] dstDeviceId, SinopeAppData appData) {
        super();
        this.seq = seq;
        this.requestType = 0;
        this.res1 = 0;
        this.res2 = 0;
        this.res3 = new byte[] { 0, 0 };
        this.res4 = new byte[] { 0, 0 };
        this.dstDeviceId = dstDeviceId;

        this.appData = appData;
    }

    /**
     * Gets the seq.
     *
     * @return the seq
     */
    public byte[] getSeq() {
        return seq;
    }

    /**
     * Gets the request type.
     *
     * @return the request type
     */
    public byte getRequestType() {
        return requestType;
    }

    /**
     * Gets the res 1.
     *
     * @return the res 1
     */
    public byte getRes1() {
        return res1;
    }

    /**
     * Gets the res 2.
     *
     * @return the res 2
     */
    public byte getRes2() {
        return res2;
    }

    /**
     * Gets the res 3.
     *
     * @return the res 3
     */
    public byte[] getRes3() {
        return res3;
    }

    /**
     * Gets the res 4.
     *
     * @return the res 4
     */
    public byte[] getRes4() {
        return res4;
    }

    /**
     * Gets the dst device id.
     *
     * @return the dst device id
     */
    public byte[] getDstDeviceId() {
        return dstDeviceId;
    }

    /**
     * Gets the app data size.
     *
     * @return the app data size
     */
    public int getAppDataSize() {
        return getAppData().getInternalData().length;
    }

    /**
     * Gets the app data.
     *
     * @return the app data
     */
    public SinopeAppData getAppData() {
        return appData;
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getFrameData()
     */
    /*
     *
     *
     * @see ca.tulip.sinope.core.internal.SinopeFrame#getFrameData()
     */
    @Override
    protected byte[] getFrameData() {
        int appDataLen = getAppDataSize();
        byte b[] = new byte[seq.length + 1 + 1 + 1 + res3.length + res4.length + dstDeviceId.length + 1 + appDataLen];

        ByteBuffer bb = ByteBuffer.wrap(b);

        bb.put(ByteUtil.reverse(seq));
        bb.put(requestType);
        bb.put(res1);
        bb.put(res2);
        bb.put(ByteUtil.reverse(res3));
        bb.put(ByteUtil.reverse(res4));
        bb.put(ByteUtil.reverse(dstDeviceId));
        bb.put((byte) appDataLen);
        bb.put(getAppData().getInternalData());
        //
        // System.out.println(toString(bb.array()));
        return bb.array();
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#toString()
     */
    /*
     *
     *
     * @see ca.tulip.sinope.core.internal.SinopeFrame#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("\nData: %s", ByteUtil.toString(getFrameData())));
        sb.append(String.format("\n\tSeq: %s", ByteUtil.toString(getSeq())));
        sb.append(String.format("\n\tRequest Type: 0x%02X ", getRequestType()));
        sb.append(String.format("\n\tRes1: 0x%02X ", getRes1()));
        sb.append(String.format("\n\tRes2: 0x%02X ", getRes2()));
        sb.append(String.format("\n\tRes3: %s", ByteUtil.toString(getRes3())));
        sb.append(String.format("\n\tRes4: %s", ByteUtil.toString(getRes4())));
        sb.append(String.format("\n\tDstDeviceId: %s", ByteUtil.toString(getDstDeviceId())));
        sb.append(String.format("\n\tAppDataSize: 0x%02X ", getAppDataSize()));
        sb.append(String.format("\n\tAppData: %s", getAppData()));

        return sb.toString();
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeRequest#getReplyAnswer(java.io.InputStream)
     */
    /*
     *
     *
     * @see ca.tulip.sinope.core.internal.SinopeRequest#getReplyAnswer(java.io.InputStream)
     */
    @Override
    public abstract SinopeDataAnswer getReplyAnswer(InputStream r) throws IOException;
}
