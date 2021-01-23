/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.rscp;

import static org.openhab.binding.e3dc.internal.rscp.util.ByteUtils.reverseByteArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openhab.binding.e3dc.internal.rscp.util.ByteUtils;

/**
 * The {@link RSCPFrame} is responsible for the RSCP Frame that is exchanged between client and server.
 *
 * @author Brendon Votteler - Initial Contribution
 */
public class RSCPFrame {
    // byte sizes
    public static final int sizeMagic = 2;
    public static final int sizeCtrl = 2;

    public static final int sizeTsSeconds = 8;

    public static final int sizeTsNanoSeconds = 4;
    public static final int sizeLength = 2;
    // note: data size goes here, but is unknown as it is variable
    public static final int sizeCRC = 4;
    // byte offset structure (number of bytes counting from zero)
    // first 2 bytes: magic bytes (E3DC)
    // next 2 bytes: control bytes (version, crc included flag, etc)
    // next 8 bytes: time stamp in seconds (since 1970-01-01 00:00:00)
    // next 4 bytes: time stamp nanoseconds (elapsed since last second)
    // next 2 bytes: frame length
    // next ? bytes: data portion of variable length
    // last 4 bytes: CRC checksum if included (if bit 23 is set)
    public static final int offsetMagic = 0;
    public static final int offsetCtrl = offsetMagic + sizeMagic;
    public static final int offsetTsSeconds = offsetCtrl + sizeCtrl;
    public static final int offsetTsNanoSeconds = offsetTsSeconds + sizeTsSeconds;
    public static final int offsetLength = offsetTsNanoSeconds + sizeTsNanoSeconds;
    public static final int offsetData = offsetLength + sizeLength;
    private static final byte[] magicBytes = ByteUtils.hexStringToByteArray("E3DC");
    private final byte[] controlBytes;
    private final Instant timestamp;
    private final List<RSCPData> data;

    RSCPFrame(byte[] controlBytes, Instant timestamp, List<RSCPData> dataList, boolean enableChecksum) {
        this.controlBytes = controlBytes;
        this.timestamp = timestamp;
        this.data = dataList;
        setChecksumBitTo(enableChecksum);
    }

    /**
     * Get a builder to construct an {@link RSCPFrame}.
     *
     * @return A builder ({@link RSCPFrame.Builder});
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get the data contained in this frame as a list of {@link RSCPData} elements.
     *
     * @return The data in this frame as a list.
     */
    public List<RSCPData> getData() {
        if (data == null) {
            return Collections.emptyList();
        }
        return data;
    }

    /**
     * Get the size of the data in bytes.
     *
     * @return Total byte count of all data elements in this frame.
     */
    public int getDataByteCount() {
        if (data == null) {
            return 0;
        }

        return data.stream().mapToInt(RSCPData::getByteCount).sum();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * <p>
     * Get frame content as byte array. Will calculate and append checksum CRC if needed.
     * </p>
     * <p>
     * See also {@link Builder#withChecksum()} or {@link Builder#withoutChecksum()}.
     * </p>
     *
     * @return Byte array ready to be encrypted and sent.
     */
    public byte[] getAsByteArray() {

        // calculate size needed
        int sizeNeeded = getFrameByteCount();

        ByteBuffer byteBuffer = ByteBuffer.allocate(sizeNeeded).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(magicBytes);
        byteBuffer.put(controlBytes);
        byteBuffer.putLong(timestamp.getEpochSecond());
        byteBuffer.putInt(timestamp.getNano());
        // skip length, coming back to it, move cursor to where the data block begins
        byteBuffer.position(offsetData);
        int offsetEndOfData = offsetData;
        for (RSCPData value : data) {
            int byteSize = value.getByteCount();
            byteBuffer.put(value.getAsByteArray());
            offsetEndOfData += byteSize; // move offset along
        }
        // set length now that we know it
        short dataByteCount = (short) (offsetEndOfData - offsetData);
        byteBuffer.putShort(offsetLength, dataByteCount);

        if (isChecksumBitSet()) {
            byte[] data = byteBuffer.array();
            int checksum = ByteUtils.calculateCRC32Checksum(data, 0, offsetEndOfData);
            byteBuffer.putInt(offsetEndOfData, checksum);
        }

        return byteBuffer.array();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RSCPFrame rscpFrame = (RSCPFrame) o;
        return Arrays.equals(controlBytes, rscpFrame.controlBytes) && timestamp.equals(rscpFrame.timestamp)
                && data.equals(rscpFrame.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(timestamp, data);
        result = 31 * result + Arrays.hashCode(controlBytes);
        return result;
    }

    private static void validateBytesCanBeFrameElseThrow(byte[] bytes) {
        if (bytes == null || bytes.length < offsetData) {
            throw new IllegalArgumentException("Byte array is null, or too small to be a frame.");
        }

        byte[] frameMagicBytes = ByteUtils.copyBytesIntoNewArray(bytes, offsetMagic, sizeMagic);
        if (!Arrays.equals(frameMagicBytes, magicBytes)) {
            throw new IllegalArgumentException("Byte array does not contain magic bytes.");
        }

        byte[] frameLengthBytes = ByteUtils.copyBytesIntoNewArray(bytes, offsetLength, sizeLength);
        short frameDataLength = ByteUtils.bytesToShort(reverseByteArray(frameLengthBytes));
        if (frameDataLength < 0) {
            throw new IllegalArgumentException("Frame data length value is less than zero.");
        }
    }

    private int getFrameByteCount() {
        return offsetData + getDataByteCount() + (isChecksumBitSet() ? sizeCRC : 0);
    }

    private boolean isChecksumBitSet() {
        // grab first ctrl byte
        byte ctrlPart1 = this.controlBytes[1];
        // the 4th least significant bit is the CRC flag
        int bitPosition = 4; // Position of this bit in a byte

        return (ctrlPart1 >> bitPosition & 1) == 1;
    }

    private void setChecksumBitTo(boolean flag) {
        int bitPosition = 4;
        controlBytes[1] = (flag) ? ((byte) (controlBytes[1] | (1 << bitPosition)))
                : ((byte) (controlBytes[1] & ~(1 << bitPosition)));
    }

    public static class Builder {

        private byte[] controlBytes = reverseByteArray(ByteUtils.hexStringToByteArray("1100"));
        private Instant timestamp;
        private List<RSCPData> dataList = new ArrayList<>();
        private boolean enableChecksum = true;

        Builder() {
        }

        /**
         * <p>
         * Read in a frame from raw bytes.
         * </p>
         * <p>
         * This will attempt to re-construct the entire {@link RSCPFrame}, assuming the raw data can be validated.
         * </p>
         *
         * @param bytes Raw bytes, typically received from IO when communicating with an E3DC server.
         * @return A constructed {@link RSCPFrame}. Throws {@link IllegalArgumentException} if the provided bytes are
         *         misformed. Throws {@link IllegalStateException} when validation during construction fails.
         */
        public RSCPFrame buildFromRawBytes(byte[] bytes) {
            validateBytesCanBeFrameElseThrow(bytes);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.rewind();

            this.controlBytes = new byte[sizeCtrl];
            byteBuffer.position(offsetCtrl);
            byteBuffer.get(this.controlBytes, 0, this.controlBytes.length);

            long epochSecs = byteBuffer.getLong(offsetTsSeconds);
            int nanos = byteBuffer.getInt(offsetTsNanoSeconds);
            this.timestamp = Instant.ofEpochSecond(epochSecs, nanos);

            short dataLength = byteBuffer.getShort(offsetLength);

            byte[] data = new byte[dataLength];
            byteBuffer.position(offsetData);
            byteBuffer.get(data, 0, dataLength);

            this.dataList = RSCPData.builder().buildFromRawBytes(data);

            return build();
        }

        public Builder controlBytes(byte[] controlBytes) {
            this.controlBytes = controlBytes;
            return this;
        }

        /**
         * Set the timestamp - required when constructing a frame.
         *
         * @param timestamp An {@link Instant} to be set as the timestamp for the constructed {@link RSCPFrame}.
         * @return The builder.
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Appends provided {@link RSCPData} to the data block under construction.
         *
         * @param dataList List of {@link RSCPData}.
         * @return The builder.
         */
        public Builder addData(List<RSCPData> dataList) {
            this.dataList.addAll(dataList);
            return this;
        }

        /**
         * Appends provided {@link RSCPData} to the data block under construction.
         *
         * @param data The {@link RSCPData} to append.
         * @return The builder.
         */
        public Builder addData(RSCPData data) {
            this.dataList.add(data);
            return this;
        }

        /**
         * <p>
         * Explicitly indicate that a checksum should be included in the resulting {@link RSCPFrame}'s raw data.
         * </p>
         * <p>
         * The default assumption is that a checksum should be included.
         * Making a call to this optional (e.g. if we want to make it obvious).
         * </p>
         * <p>
         * See also {@link Builder#withoutChecksum()}.
         * </p>
         * .
         *
         * @return The builder.
         */
        public Builder withChecksum() {
            this.enableChecksum = true;
            return this;
        }

        /**
         * Indicate that we do not want a checksum to be included in the resulting {@link RSCPFrame}'s raw data.
         *
         * @return The builder.
         */
        public Builder withoutChecksum() {
            this.enableChecksum = false;
            return this;
        }

        /**
         * Validates and creates an instance of {@link RSCPFrame}.
         *
         * @return The built {@link RSCPFrame}. Throws an {@link IllegalStateException} if validation fails.
         */
        public RSCPFrame build() {
            validate();
            return new RSCPFrame(controlBytes, timestamp, dataList, enableChecksum);
        }

        public void validate() {
            if (timestamp == null) {
                throw new IllegalStateException("Timestamp value is required.");
            }
            if (controlBytes == null || controlBytes.length != RSCPFrame.sizeCtrl) {
                throw new IllegalStateException(
                        "Control bytes are null or have incorrect length (expected length: " + sizeCtrl + ").");
            }
            if (dataList == null || dataList.isEmpty()) {
                throw new IllegalStateException("Value list cannot be null or empty.");
            }
        }
    }
}
