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

import static org.openhab.binding.e3dc.internal.rscp.RSCPDataType.*;
import static org.openhab.binding.e3dc.internal.rscp.RSCPFrame.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.openhab.binding.e3dc.internal.rscp.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RSCPData} is responsible for the RSCP date inside a {@link RSCPFrame}.
 *
 * @author Brendon Votteler - Initial Contribution
 */
public class RSCPData {
    private static final Logger logger = LoggerFactory.getLogger(RSCPData.class);
    // byte sizes
    private static final int sizeDataTag = 4;
    private static final int sizeDataType = 1;
    private static final int sizeDataLength = 2;

    // byte offset structure (number of bytes counting from zero)
    // first 4 bytes: namespace identifier (1 byte) and data tag of what the data request/response is related to
    // next 1 bytes: data type (string, int, container, etc)
    // next 2 bytes: data length in bytes
    // next ? bytes: data portion of variable length
    // last 4 bytes: CRC checksum (if applicable)
    private static final int offsetDataTag = 0;
    private static final int offsetDataType = offsetDataTag + sizeDataTag;
    private static final int offsetDataLength = offsetDataType + sizeDataType;
    private static final int offsetData = offsetDataLength + sizeDataLength;

    private final RSCPTag dataTag;
    private final RSCPDataType dataType;
    private final short dataLength;
    private final byte[] value; // unknown size

    RSCPData(RSCPTag dataTag, RSCPDataType dataType, byte[] value) {
        this.dataTag = dataTag;
        this.dataType = dataType;
        this.value = value;
        this.dataLength = (short) value.length;
    }

    public static Builder builder() {
        return new Builder();
    }

    public RSCPTag getDataTag() {
        return dataTag;
    }

    public RSCPDataType getDataType() {
        return dataType;
    }

    public byte[] getValueAsByteArray() {
        return value;
    }

    public List<RSCPData> getContainerData() {
        if (RSCPDataType.CONTAINER != getDataType()) {
            return Collections.emptyList();
        } else {
            return RSCPData.builder().buildFromRawBytes(getValueAsByteArray());
        }
    }

    /**
     * Get the payload of this RSCPData instance as byte array
     *
     * @return A byte array containing the data.
     */
    public byte[] getAsByteArray() {
        byte[] bytes = new byte[offsetData + dataLength];
        // copy over to final position and reverse
        System.arraycopy(ByteUtils.reverseByteArray(getDataTagAsBytes()), 0, bytes, 0, sizeDataTag);
        System.arraycopy(ByteUtils.reverseByteArray(getDataTypeAsBytes()), 0, bytes, offsetDataType, sizeDataType);
        System.arraycopy(ByteUtils.reverseByteArray(ByteUtils.shortToBytes(dataLength)), 0, bytes, offsetDataLength,
                sizeDataLength);
        System.arraycopy(value, 0, bytes, offsetData, dataLength);
        return bytes;
    }

    /**
     * Get the byte count of the entire instance.
     *
     * @return Number of bytes this instance holds.
     */
    public int getByteCount() {
        return offsetData + dataLength;
    }

    /**
     * Try to get the value contained in this RSCPData instance.
     *
     * @return An {@link Optional} containing a value if the raw data can be interpreted as short. Otherwise, returns
     *         {@link Optional#empty()}.
     */
    public Optional<Short> getValueAsShort() {
        if (!this.dataType.isValidShortType()) {
            return Optional.empty();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).put(this.value);
        byteBuffer.rewind();
        return Optional.of(byteBuffer.getShort());
    }

    /**
     * Try to get the value contained in this RSCPData instance.
     *
     * @return An {@link Optional} containing a value if the raw data can be interpreted as integer. Otherwise, returns
     *         {@link Optional#empty()}.
     */
    public Optional<Integer> getValueAsInt() {
        if (!this.dataType.isValidIntType()) {
            return Optional.empty();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).put(this.value);
        byteBuffer.rewind();
        return Optional.of(byteBuffer.getInt());
    }

    /**
     * Try to get the value contained in this RSCPData instance.
     *
     * @return An {@link Optional} containing a value if the raw data can be interpreted as long. Otherwise, returns
     *         {@link Optional#empty()}.
     */
    public Optional<Long> getValueAsLong() {
        if (!this.dataType.isValidLongType()) {
            return Optional.empty();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).put(this.value);
        byteBuffer.rewind();
        return Optional.of(byteBuffer.getLong());
    }

    /**
     * Try to get the value contained in this RSCPData instance.
     *
     * @return An {@link Optional} containing a value if the raw data can be interpreted as float. Otherwise, returns
     *         {@link Optional#empty()}.
     */
    public Optional<Float> getValueAsFloat() {
        if (this.dataType != FLOAT32) {
            return Optional.empty();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(Float.BYTES).order(ByteOrder.LITTLE_ENDIAN).put(this.value);
        byteBuffer.rewind();
        return Optional.of(byteBuffer.getFloat());
    }

    /**
     * Try to get the value contained in this RSCPData instance.
     *
     * @return An {@link Optional} containing a value if the raw data can be interpreted as double. Otherwise, returns
     *         {@link Optional#empty()}.
     */
    public Optional<Double> getValueAsDouble() {
        if (this.dataType != DOUBLE64) {
            return Optional.empty();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES).order(ByteOrder.LITTLE_ENDIAN).put(this.value);
        byteBuffer.rewind();
        return Optional.of(byteBuffer.getDouble());
    }

    /**
     * Try to get the value contained in this RSCPData instance.
     *
     * @return An {@link Optional} containing a value if the raw data can be interpreted as Instant. Otherwise, returns
     *         {@link Optional#empty()}.
     */
    public Optional<Instant> getValueAsInstant() {
        if (this.dataType != TIMESTAMP) {
            return Optional.empty();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(sizeTsSeconds + sizeTsNanoSeconds).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(this.value);
        byteBuffer.rewind();

        long epochSeconds = byteBuffer.getLong();
        int nanos = byteBuffer.getInt();

        return Optional.of(Instant.ofEpochSecond(epochSeconds, nanos));
    }

    /**
     * Try to get the value contained in this RSCPData instance.
     *
     * @return An {@link Optional} containing a value if the raw data can be interpreted as Instant. Otherwise, returns
     *         {@link Optional#empty()}.
     */
    public Optional<Duration> getValueAsDuration() {
        if (this.dataType != TIMESTAMP) {
            return Optional.empty();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(sizeTsSeconds + sizeTsNanoSeconds).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(this.value);
        byteBuffer.rewind();

        long seconds = byteBuffer.getLong();
        int nanos = byteBuffer.getInt();

        return Optional.of(Duration.ofSeconds(seconds).plusNanos(nanos));
    }

    /**
     * <p>
     * Try to get the value contained in this RSCPData instance.
     * </p>
     * <p>
     * Works for most {@link RSCPDataType}s. If not, {@link RSCPData#getValueAsByteArray()} is your best bet to obtain a
     * value that can be interpreted manually.
     * </p>
     *
     * @return An {@link Optional} containing a value if the raw data can be interpreted as string. Otherwise, returns
     *         {@link Optional#empty()}.
     */
    public Optional<String> getValueAsString() {
        switch (this.dataType) {
            case STRING:
                return Optional.of(new String(this.value, StandardCharsets.UTF_8));
            case BOOL:
                return Optional.of(String.format("%b", this.value[0] == 1));
            case DOUBLE64:
                return Optional.of(String.format("%.2f", getValueAsDouble().orElse(0.0)));
            case FLOAT32:
                return Optional.of(String.format("%.2f", getValueAsFloat().orElse(0.0F)));
            case CHAR8:
            case UCHAR8:
            case INT16:
            case UINT16:
            case INT32:
            case UINT32:
            case INT64:
                return Optional.of(String.format("%d", getValueAsLong().orElse(0L)));
            case TIMESTAMP:
                return getValueAsInstant().map(instant -> DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        .withZone(ZoneId.from(ZoneOffset.UTC)).format(instant));
            // TODO: Should be able to get a few more done here eventually
            default:
                return Optional.empty();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RSCPData rscpData = (RSCPData) o;
        return dataLength == rscpData.dataLength && Arrays.equals(value, rscpData.value) && dataTag == rscpData.dataTag
                && dataType == rscpData.dataType;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(dataLength, dataTag, dataType);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }

    private byte[] getDataTagAsBytes() {
        return dataTag.getValueAsBytes();
    }

    private byte[] getDataTypeAsBytes() {
        byte[] bytes = new byte[1];
        bytes[0] = dataType.getValue();
        return bytes;
    }

    public static class Builder {
        private RSCPTag dataTag;
        private RSCPDataType dataType;
        private byte[] value;

        Builder() {
        }

        /**
         * <p>
         * Build a list of {@link RSCPData} instances given provided raw data.
         * </p>
         * <p>
         * This method will try to do some basic validations. Will throw {@link IllegalStateException} if the data
         * cannot be validated before construction.
         * </p>
         *
         * @param bytes Raw bytes, typically received from IO when communicating with an E3DC server, or extracted from
         *            the value of an {@link RSCPData} with data type {@link RSCPDataType#CONTAINER}.
         * @return A list of constructed {@link RSCPData} instances.
         */
        public List<RSCPData> buildFromRawBytes(byte[] bytes) {
            if (bytes == null || bytes.length < offsetData) {
                logger.warn("Not enough bytes to form RSCPData instance(s), returning empty list (data truncated?).");
                return Collections.emptyList();
            }

            List<RSCPData> rscpDataList = new ArrayList<>();

            byte[] tagNameBytes = ByteUtils.copyBytesIntoNewArray(bytes, offsetDataTag, sizeDataTag);
            RSCPTag tag = RSCPTag.getTagForBytes(ByteUtils.reverseByteArray(tagNameBytes));

            // single byte, no need to reverse
            RSCPDataType dataType = RSCPDataType.getDataTypeForBytes(bytes[offsetDataType]);

            ByteBuffer byteBuffer = getLittleEndianByteBuffer(Short.BYTES).put(bytes, offsetDataLength, sizeDataLength);
            byteBuffer.flip();
            short dataLength = byteBuffer.getShort();

            if (bytes.length < offsetData + dataLength) {
                logger.warn("Not enough bytes in data section to form complete RSCPValue instance (data truncated?)");
                return Collections.emptyList();
            }

            byte[] data = ByteUtils.copyBytesIntoNewArray(bytes, offsetData, dataLength);

            RSCPData rscpData = RSCPData.builder().tag(tag).valueOfType(dataType, data).build();

            rscpDataList.add(rscpData);

            // more left to process?
            if (bytes.length > offsetData + dataLength) {
                // truncate bytes and start recursion
                byte[] remainingBytes = ByteUtils.truncateFirstNBytes(bytes, offsetData + dataLength);
                rscpDataList.addAll(buildFromRawBytes(remainingBytes));
            }

            return rscpDataList;
        }

        /**
         * Define the tag for this instance. See {@link RSCPTag}.
         *
         * @param tag The tag to set.
         * @return The builder.
         */
        public Builder tag(RSCPTag tag) {
            this.dataTag = tag;
            return this;
        }

        /**
         * <p>
         * Define the data type and value for this instance.
         * </p>
         * <p>
         * Typically, it will be easier to construct the data type and value pairs with other helper methods.
         * </p>
         * <p>
         * For examples, see {@link Builder#boolValue(boolean)}, {@link Builder#char8Value(char)},
         * {@link Builder#int16Value(short)} , {@link Builder#int32Value(int)}, {@link Builder#stringValue(String)},
         * {@link Builder#float32Value(float)}, {@link Builder#double64Value(double)},
         * {@link Builder#timestampValue(Instant)} etc.
         * </p>
         *
         * @param dataType The {@link RSCPDataType} to associate the value with.
         * @param value The raw data as byte array.
         * @return The builder.
         */
        public Builder valueOfType(RSCPDataType dataType, byte[] value) {
            this.dataType = dataType;
            this.value = value;
            return this;
        }

        /**
         * Set the data type ({@link RSCPDataType#TIMESTAMP} and value from an {@link Instant}.
         *
         * @param timestamp The value.
         * @return The builder.
         */
        public Builder timestampValue(Instant timestamp) {
            ByteBuffer byteBuffer = getLittleEndianByteBuffer(sizeTsSeconds + sizeTsNanoSeconds)
                    .putLong(timestamp.getEpochSecond()).putInt(timestamp.getNano());
            byte[] timestampBytes = byteBuffer.array();

            return valueOfType(RSCPDataType.TIMESTAMP, timestampBytes);
        }

        /**
         * Set the data type ({@link RSCPDataType#TIMESTAMP} and value from a {@link Duration}.
         *
         * @param duration The value.
         * @return The builder.
         */
        public Builder timestampValue(Duration duration) {
            return timestampValue(Instant.ofEpochSecond(duration.getSeconds(), duration.getNano()));
        }

        /**
         * Set the data type ({@link RSCPDataType#BOOL}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder boolValue(boolean value) {
            byte[] flag = new byte[1];
            flag[0] = (byte) ((value) ? 0xFF : 0x00);
            return valueOfType(RSCPDataType.BOOL, flag);
        }

        /**
         * Set the data type ({@link RSCPDataType#CHAR8}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder char8Value(char value) {
            ByteBuffer byteBuffer = getLittleEndianByteBuffer(Character.BYTES).putChar(value);
            return valueOfType(RSCPDataType.CHAR8, byteBuffer.array());
        }

        /**
         * Set the data type ({@link RSCPDataType#UCHAR8}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder uchar8Value(char value) {
            ByteBuffer byteBuffer = getLittleEndianByteBuffer(Character.BYTES).putChar(value);
            return valueOfType(RSCPDataType.UCHAR8, byteBuffer.array());
        }

        /**
         * Set the data type ({@link RSCPDataType#INT16}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder int16Value(short value) {
            ByteBuffer byteBuffer = getLittleEndianByteBuffer(Short.BYTES).putShort(value);
            return valueOfType(RSCPDataType.INT16, byteBuffer.array());
        }

        /**
         * Set the data type ({@link RSCPDataType#UINT16}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder uint16Value(short value) {
            ByteBuffer byteBuffer = getLittleEndianByteBuffer(Short.BYTES).putShort(value);
            return valueOfType(UINT16, byteBuffer.array());
        }

        /**
         * Set the data type ({@link RSCPDataType#INT32}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder int32Value(int value) {
            ByteBuffer byteBuffer = getLittleEndianByteBuffer(Integer.BYTES).putInt(value);
            return valueOfType(INT32, byteBuffer.array());
        }

        /**
         * Set the data type ({@link RSCPDataType#UINT32}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder uint32Value(int value) {
            ByteBuffer byteBuffer = getLittleEndianByteBuffer(Integer.BYTES).putInt(value);
            return valueOfType(UINT32, byteBuffer.array());
        }

        /**
         * Set the data type ({@link RSCPDataType#INT64}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder int64Value(long value) {
            ByteBuffer byteBuffer = getLittleEndianByteBuffer(Long.BYTES).putLong(value);
            return valueOfType(RSCPDataType.INT64, byteBuffer.array());
        }

        /**
         * Set the data type ({@link RSCPDataType#UINT64}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder uint64Value(long value) {
            ByteBuffer byteBuffer = getLittleEndianByteBuffer(Long.BYTES).putLong(value);
            return valueOfType(RSCPDataType.UINT64, byteBuffer.array());
        }

        /**
         * Set the data type ({@link RSCPDataType#FLOAT32}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder float32Value(float value) {
            ByteBuffer byteBuffer = getLittleEndianByteBuffer(Float.BYTES).putFloat(value);
            return valueOfType(RSCPDataType.FLOAT32, byteBuffer.array());
        }

        /**
         * Set the data type ({@link RSCPDataType#DOUBLE64}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder double64Value(double value) {
            ByteBuffer byteBuffer = getLittleEndianByteBuffer(Double.BYTES).putDouble(value);
            return valueOfType(RSCPDataType.DOUBLE64, byteBuffer.array());
        }

        /**
         * Set the data type ({@link RSCPDataType#STRING}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder stringValue(String value) {
            return valueOfType(RSCPDataType.STRING, value.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * Set the data type ({@link RSCPDataType#BYTEARRAY}) and value.
         *
         * @param value The value.
         * @return The builder.
         */
        public Builder byteArrayValue(byte[] value) {
            return valueOfType(RSCPDataType.BYTEARRAY, value);
        }

        /**
         * Set the data type ({@link RSCPDataType#CONTAINER}) and a list of RSCPData as the value.
         *
         * @param dataList A list of {@link RSCPData} instances.
         * @return The builder.
         */
        public Builder containerValues(List<RSCPData> dataList) {
            this.dataType = RSCPDataType.CONTAINER;
            dataList.forEach(data -> appendBytes(data.getAsByteArray()));

            return this;
        }

        /**
         * Set the data type ({@link RSCPDataType#CONTAINER}) and an instance of RSCPData as the value.
         *
         * @param value A {@link RSCPData} instance.
         * @return The builder.
         */
        public Builder containerValue(RSCPData value) {
            return containerValues(Collections.singletonList(value));
        }

        private void appendBytes(byte[] bytes) {
            if (bytes == null || bytes.length < 1) {
                return;
            }
            int oldValueSize = (value != null) ? value.length : 0;
            int newSize = bytes.length + oldValueSize;
            byte[] newValue = new byte[newSize];

            if (value != null) {
                System.arraycopy(value, 0, newValue, 0, oldValueSize);
            }
            System.arraycopy(bytes, 0, newValue, oldValueSize, bytes.length);
            this.value = newValue;
        }

        public RSCPData build() {
            validate();
            return new RSCPData(dataTag, dataType, value);
        }

        private ByteBuffer getLittleEndianByteBuffer(int capacity) {
            return ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN);
        }

        private void validate() {
            if (dataTag == null) {
                throw new IllegalStateException("Tag value is required.");
            }
            if (dataType == null) {
                throw new IllegalStateException("DataType value is required.");
            }
            if (value == null || value.length == 0) {
                throw new IllegalStateException("Data must not be empty.");
            }
        }
    }
}
