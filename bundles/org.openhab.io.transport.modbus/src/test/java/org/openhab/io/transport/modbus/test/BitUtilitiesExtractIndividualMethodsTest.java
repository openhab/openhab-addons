/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.io.transport.modbus.test;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openhab.io.transport.modbus.ModbusBitUtilities;
import org.openhab.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * @author Sami Salonen - Initial contribution
 */
@RunWith(Parameterized.class)
public class BitUtilitiesExtractIndividualMethodsTest {

    final byte[] bytes;
    final ValueType type;
    final int byteIndex;
    final Object expectedResult;

    @Rule
    public final ExpectedException shouldThrow = ExpectedException.none();

    public BitUtilitiesExtractIndividualMethodsTest(Object expectedResult, ValueType type, byte[] bytes,
            int byteIndex) {
        this.expectedResult = expectedResult; // Exception or DecimalType
        this.type = type;
        this.bytes = bytes;
        this.byteIndex = byteIndex;
    }

    @Parameters
    public static Collection<Object[]> data() {
        // We use test data from BitUtilitiesExtractStateFromRegistersTest
        // In BitUtilitiesExtractStateFromRegistersTest the data is aligned to registers
        //
        // Here (in registerVariations) we generate offsetted variations of the byte data
        // to test extractXX which can operate on data aligned on byte-level, not just data aligned on-register level
        Collection<Object[]> data = BitUtilitiesExtractStateFromRegistersTest.data();
        return data.stream().flatMap(values -> {
            Object expectedResult = values[0];
            ValueType type = (ValueType) values[1];
            ModbusRegisterArray registers = (ModbusRegisterArray) values[2];
            int index = (int) values[3];
            return registerVariations(expectedResult, type, registers, index);
        }).collect(Collectors.toList());
    }

    private boolean isUnsignedType() {
        return type.name().toLowerCase().charAt(0) == 'u';
    }

    /**
     * Generate register variations for extractXX functions
     *
     *
     * @return entries of (byte[], byteIndex)
     */
    private static Stream<Object[]> registerVariations(Object expectedResult, ValueType type,
            ModbusRegisterArray registers, int index) {
        byte[] origBytes = registers.getBytes();
        int origRegisterIndex = index;
        int origByteIndex = origRegisterIndex * 2;

        Builder<Object[]> streamBuilder = Stream.builder();
        for (int offset = 0; offset < 5; offset++) {
            int byteIndex = origByteIndex + offset;
            byte[] bytesOffsetted = new byte[origBytes.length + offset];
            for (int i = 0; i < bytesOffsetted.length; i++) {
                bytesOffsetted[i] = 99;
            }
            System.arraycopy(origBytes, 0, bytesOffsetted, offset, origBytes.length);
            // offsetted:
            streamBuilder.add(new Object[] { expectedResult, type, bytesOffsetted, byteIndex });

            // offsetted, with no extra bytes following
            // (this is only done for successfull cases to avoid copyOfRange padding with zeros
            if (!(expectedResult instanceof Class)) {
                byte[] bytesOffsettedCutExtra = Arrays.copyOfRange(bytesOffsetted, 0, byteIndex + type.getBits() / 8);
                streamBuilder.add(new Object[] { expectedResult, type, bytesOffsettedCutExtra, byteIndex });
            }
        }
        return streamBuilder.build();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExtractIndividual16BitIntegers() throws InstantiationException, IllegalAccessException {
        assumeTrue(type == ValueType.INT16 || type == ValueType.UINT16);
        final Object expectedNumber;
        if (expectedResult instanceof Class && Exception.class.isAssignableFrom((Class<?>) expectedResult)) {
            shouldThrow.expect((Class<? extends Throwable>) expectedResult);
            // dummy 'number' for error message. Should raise anyways
            expectedNumber = ((Class<? extends Throwable>) expectedResult).newInstance();
        } else {
            DecimalType expectedDecimal = (DecimalType) expectedResult;
            if (isUnsignedType()) {
                expectedNumber = expectedDecimal.intValue();
            } else {
                expectedNumber = expectedDecimal.shortValue();
            }
        }

        String testExplanation = String.format("bytes=%s, byteIndex=%d, type=%s", Arrays.toString(bytes), byteIndex,
                type);
        switch (type) {
            case INT16:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractSInt16(bytes, byteIndex));
                break;
            case UINT16:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractUInt16(bytes, byteIndex));
                break;
            default:
                // does not happen, test is skipped

        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExtractIndividual32BitIntegers() throws InstantiationException, IllegalAccessException {
        assumeTrue(type == ValueType.INT32 || type == ValueType.UINT32 || type == ValueType.INT32_SWAP
                || type == ValueType.UINT32_SWAP);
        final Object expectedNumber;
        if (expectedResult instanceof Class && Exception.class.isAssignableFrom((Class<?>) expectedResult)) {
            shouldThrow.expect((Class<? extends Throwable>) expectedResult);
            // dummy 'number' for error message. Should raise anyways
            expectedNumber = ((Class<? extends Throwable>) expectedResult).newInstance();
        } else {
            DecimalType expectedDecimal = (DecimalType) expectedResult;
            if (isUnsignedType()) {
                expectedNumber = expectedDecimal.longValue();
            } else {
                expectedNumber = expectedDecimal.intValue();
            }
        }
        String testExplanation = String.format("bytes=%s, byteIndex=%d, type=%s", Arrays.toString(bytes), byteIndex,
                type);
        switch (type) {
            case INT32:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractSInt32(bytes, byteIndex));
                break;
            case UINT32:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractUInt32(bytes, byteIndex));
                break;
            case INT32_SWAP:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractSInt32Swap(bytes, byteIndex));
                break;
            case UINT32_SWAP:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractUInt32Swap(bytes, byteIndex));
                break;
            default:
                // does not happen, test is skipped
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExtractIndividual64BitIntegers() throws InstantiationException, IllegalAccessException {
        assumeTrue(type == ValueType.INT64 || type == ValueType.UINT64 || type == ValueType.INT64_SWAP
                || type == ValueType.UINT64_SWAP);
        final Object expectedNumber;
        if (expectedResult instanceof Class && Exception.class.isAssignableFrom((Class<?>) expectedResult)) {
            shouldThrow.expect((Class<? extends Throwable>) expectedResult);
            // dummy 'number' for error message. Should raise anyways
            expectedNumber = ((Class<? extends Throwable>) expectedResult).newInstance();
        } else {
            DecimalType expectedDecimal = (DecimalType) expectedResult;
            if (isUnsignedType()) {
                expectedNumber = expectedDecimal.toBigDecimal().toBigIntegerExact();
            } else {
                expectedNumber = expectedDecimal.longValue();
            }
        }

        String testExplanation = String.format("bytes=%s, byteIndex=%d, type=%s", Arrays.toString(bytes), byteIndex,
                type);
        switch (type) {
            case INT64:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractInt64(bytes, byteIndex));
                break;
            case UINT64:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractUInt64(bytes, byteIndex));
                break;
            case INT64_SWAP:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractInt64Swap(bytes, byteIndex));
                break;
            case UINT64_SWAP:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractUInt64Swap(bytes, byteIndex));
                break;
            default:
                // does not happen, test is skipped
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExtractIndividual32Floats() throws InstantiationException, IllegalAccessException {
        assumeTrue(type == ValueType.FLOAT32 || type == ValueType.FLOAT32_SWAP);
        final Object expectedNumber;
        if (expectedResult instanceof Class && Exception.class.isAssignableFrom((Class<?>) expectedResult)) {
            shouldThrow.expect((Class<? extends Throwable>) expectedResult);
            // dummy 'number' for error message. Should raise anyways
            expectedNumber = ((Class<? extends Throwable>) expectedResult).newInstance();

        } else if (expectedResult instanceof Optional<?>) {
            assertTrue(!((Optional<?>) expectedResult).isPresent());
            expectedNumber = Float.NaN;
        } else {
            DecimalType expectedDecimal = (DecimalType) expectedResult; // Optional is used only with empty values
            expectedNumber = expectedDecimal.floatValue();
        }

        String testExplanation = String.format("bytes=%s, byteIndex=%d, type=%s", Arrays.toString(bytes), byteIndex,
                type);
        switch (type) {
            case FLOAT32:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractFloat32(bytes, byteIndex));
                break;
            case FLOAT32_SWAP:
                assertEquals(testExplanation, expectedNumber, ModbusBitUtilities.extractFloat32Swap(bytes, byteIndex));
                break;
            default:
                // does not happen, test is skipped
        }
    }
}
