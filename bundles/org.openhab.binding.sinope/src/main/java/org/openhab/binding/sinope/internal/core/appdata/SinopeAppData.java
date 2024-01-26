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
package org.openhab.binding.sinope.internal.core.appdata;

import java.nio.ByteBuffer;

import org.openhab.binding.sinope.internal.util.ByteUtil;

/**
 * The Class SinopeAppData.
 *
 * @author Pascal Larin - Initial contribution
 */
public class SinopeAppData {

    /** The Constant DATA_ID_SIZE. */
    protected static final int DATA_ID_SIZE = 4;

    /** The Constant DATA_SIZE. */
    protected static final int DATA_SIZE = 1;

    /** The internal data. */
    protected byte[] internal_data; // Full App object

    /** The data id. */
    private byte[] dataId;

    /**
     * The data.
     * data == null ? Do not send data size and payload
     */
    private byte[] data; // Data field of the app object

    /**
     * Instantiates a new sinope app data.
     *
     * @param dataId the data id
     * @param data the data
     */
    public SinopeAppData(byte[] dataId, byte[] data) {
        this.dataId = dataId;
        this.data = data;
    }

    /**
     * Read.
     *
     * @param d the d
     */
    public void read(byte[] d) {
        this.internal_data = d;
        if (d.length >= DATA_ID_SIZE) {
            ByteBuffer bb = ByteBuffer.wrap(d);
            this.dataId = new byte[DATA_ID_SIZE];

            bb.get(dataId);
            this.dataId = ByteUtil.reverse(dataId);
            if (d.length > DATA_ID_SIZE) {
                int len = bb.get() & 0xff;

                this.data = new byte[len];
                bb.get(this.data);
            }
        }
    }

    /**
     * Gets the internal data.
     *
     * @return the internal data
     */
    public byte[] getInternalData() {
        if (internal_data == null) {
            int len = data != null ? 1 + data.length : 0;
            byte[] b = new byte[DATA_ID_SIZE + len];

            ByteBuffer bb = ByteBuffer.wrap(b);
            bb.put(ByteUtil.reverse(dataId));
            if (data != null) { // Special case, Data null, don't put app size
                bb.put((byte) data.length);
                bb.put(data);
            }
            this.internal_data = bb.array();
        }

        return this.internal_data;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * @see java.lang.Object#toString()
     */
    /*
     *
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("\n\tData ID: %s", ByteUtil.toString(this.dataId)));
        if (data != null) {
            sb.append(String.format("\n\t\tData Size: 0x%02X ", getData().length));
            sb.append(String.format("\n\t\tData: %s", ByteUtil.toString(getData())));
        }
        return sb.toString();
    }

    /**
     * Clean data.
     * It nullifies the data field. Tells to not send data size and payload.
     */
    public void cleanData() {
        this.data = null;
    }
}
