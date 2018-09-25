/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implementation for writing coils
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class BasicModbusWriteCoilRequestBlueprint implements ModbusWriteCoilRequestBlueprint {

    private static StandardToStringStyle toStringStyle = new StandardToStringStyle();

    static {
        toStringStyle.setUseShortClassName(true);
    }

    /**
     * Implementation of {@link BitArray} with single bit as data
     *
     * @author Sami Salonen - Initial contribution
     *
     */
    private static class SingleBitArray extends BasicBitArray {

        public SingleBitArray(boolean bit) {
            super(bit);
        }

        @Override
        public String toString() {
            return "SingleBitArray(bit=" + toBinaryString() + ")";
        }

    }

    private int slaveId;
    private int reference;
    private BitArray bits;
    private boolean writeMultiple;
    private int maxTries;

    /**
     * Construct coil write request with single bit of data
     *
     * @param slaveId slave id to write to
     * @param reference reference address
     * @param data bit to write
     * @param writeMultiple whether to use {@link ModbusWriteFunctionCode.WRITE_MULTIPLE_COILS} over
     *            {@link ModbusWriteFunctionCode.WRITE_COIL}
     * @param maxTries maximum number of tries in case of errors, should be at least 1
     */
    public BasicModbusWriteCoilRequestBlueprint(int slaveId, int reference, boolean data, boolean writeMultiple,
            int maxTries) {
        this(slaveId, reference, new SingleBitArray(data), writeMultiple, maxTries);
    }

    /**
     * Construct coil write request with many bits of data
     *
     * @param slaveId slave id to write to
     * @param reference reference address
     * @param data bit(s) to write
     * @param writeMultiple whether to use {@link ModbusWriteFunctionCode.WRITE_MULTIPLE_COILS} over
     *            {@link ModbusWriteFunctionCode.WRITE_COIL}. Useful with single bit of data.
     * @param maxTries maximum number of tries in case of errors, should be at least 1
     * @throws IllegalArgumentException in case <code>data</code> is empty, <code>writeMultiple</code> is
     *             <code>false</code> but there are many bits to write.
     */
    public BasicModbusWriteCoilRequestBlueprint(int slaveId, int reference, BitArray data, boolean writeMultiple,
            int maxTries) {
        super();
        this.slaveId = slaveId;
        this.reference = reference;
        this.bits = data;
        this.writeMultiple = writeMultiple;
        this.maxTries = maxTries;

        if (!writeMultiple && bits.size() > 1) {
            throw new IllegalArgumentException("With multiple coils, writeMultiple must be true");
        }
        if (bits.size() == 0) {
            throw new IllegalArgumentException("Must have at least one bit");
        }
        if (maxTries <= 0) {
            throw new IllegalArgumentException("maxTries should be positive, was " + maxTries);
        }
    }

    @Override
    public int getUnitID() {
        return slaveId;
    }

    @Override
    public int getReference() {
        return reference;
    }

    @Override
    public ModbusWriteFunctionCode getFunctionCode() {
        return writeMultiple ? ModbusWriteFunctionCode.WRITE_MULTIPLE_COILS : ModbusWriteFunctionCode.WRITE_COIL;
    }

    @Override
    public BitArray getCoils() {
        return bits;
    }

    @Override
    public int getMaxTries() {
        return maxTries;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, toStringStyle).append("slaveId", slaveId).append("reference", reference)
                .append("functionCode", getFunctionCode()).append("bits", bits).append("maxTries", maxTries).toString();
    }
}
