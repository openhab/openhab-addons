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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.library.types.DecimalType;
import org.openhab.io.transport.modbus.ModbusBitUtilities;
import org.openhab.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * @author Sami Salonen - Initial contribution
 */
public class BitUtilitiesExtractIndividualMethodsTest {

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

    public static Stream<Object[]> filteredTestData(ValueType type) {
        return data().stream().filter(values -> (ValueType) values[1] == type);
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
                if (bytesOffsettedCutExtra.length != bytesOffsetted.length) {
                    streamBuilder.add(new Object[] { expectedResult, type, bytesOffsettedCutExtra, byteIndex });
                }
            }
        }
        return streamBuilder.build();
    }

    private void testIndividual(Object expectedResult, ValueType type, byte[] bytes, int byteIndex,
            Supplier<Number> methodUnderTest, Function<DecimalType, Number> expectedPrimitive) {
        testIndividual(expectedResult, type, bytes, byteIndex, methodUnderTest, expectedPrimitive, null);
    }

    @SuppressWarnings("unchecked")
    private void testIndividual(Object expectedResult, ValueType type, byte[] bytes, int byteIndex,
            Supplier<Number> methodUnderTest, Function<DecimalType, Number> expectedPrimitive,
            @Nullable Number defaultWhenEmptyOptional) {
        String testExplanation = String.format("bytes=%s, byteIndex=%d, type=%s", Arrays.toString(bytes), byteIndex,
                type);
        final Object expectedNumber;
        if (expectedResult instanceof Class && Exception.class.isAssignableFrom((Class<?>) expectedResult)) {
            assertThrows((Class<? extends Throwable>) expectedResult, () -> methodUnderTest.get());
        } else if (expectedResult instanceof Optional<?>) {
            assertTrue(!((Optional<?>) expectedResult).isPresent());
            if (defaultWhenEmptyOptional == null) {
                fail("Should provide defaultWhenEmptyOptional");
            }
            return;
        } else {
            DecimalType expectedDecimal = (DecimalType) expectedResult;
            expectedNumber = expectedPrimitive.apply(expectedDecimal);
            assertEquals(expectedNumber, methodUnderTest.get(), testExplanation);
        }
    }

    public static Stream<Object[]> filteredTestDataSInt16() {
        return filteredTestData(ValueType.INT16);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataSInt16")
    public void testExtractIndividualSInt16(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex, () -> ModbusBitUtilities.extractSInt16(bytes, byteIndex),
                decimal -> decimal.shortValue());
    }

    public static Stream<Object[]> filteredTestDataUInt16() {
        return filteredTestData(ValueType.UINT16);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataUInt16")
    public void testExtractIndividualUInt16(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex, () -> ModbusBitUtilities.extractUInt16(bytes, byteIndex),
                decimal -> decimal.intValue());
    }

    public static Stream<Object[]> filteredTestDataSInt32() {
        return filteredTestData(ValueType.INT32);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataSInt32")
    public void testExtractIndividualSInt32(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex, () -> ModbusBitUtilities.extractSInt32(bytes, byteIndex),
                decimal -> decimal.intValue());
    }

    public static Stream<Object[]> filteredTestDataUInt32() {
        return filteredTestData(ValueType.UINT32);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataUInt32")
    public void testExtractIndividualUInt32(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex, () -> ModbusBitUtilities.extractUInt32(bytes, byteIndex),
                decimal -> decimal.longValue());
    }

    public static Stream<Object[]> filteredTestDataSInt32Swap() {
        return filteredTestData(ValueType.INT32_SWAP);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataSInt32Swap")
    public void testExtractIndividualSInt32Swap(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex,
                () -> ModbusBitUtilities.extractSInt32Swap(bytes, byteIndex), decimal -> decimal.intValue());
    }

    public static Stream<Object[]> filteredTestDataUInt32Swap() {
        return filteredTestData(ValueType.UINT32_SWAP);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataUInt32Swap")
    public void testExtractIndividualUInt32Swap(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex,
                () -> ModbusBitUtilities.extractUInt32Swap(bytes, byteIndex), decimal -> decimal.longValue());
    }

    public static Stream<Object[]> filteredTestDataSInt64() {
        return filteredTestData(ValueType.INT64);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataSInt64")
    public void testExtractIndividualSInt64(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex, () -> ModbusBitUtilities.extractSInt64(bytes, byteIndex),
                decimal -> decimal.longValue());
    }

    public static Stream<Object[]> filteredTestDataUInt64() {
        return filteredTestData(ValueType.UINT64);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataUInt64")
    public void testExtractIndividualUInt64(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex, () -> ModbusBitUtilities.extractUInt64(bytes, byteIndex),
                decimal -> decimal.toBigDecimal().toBigIntegerExact());
    }

    public static Stream<Object[]> filteredTestDataSInt64Swap() {
        return filteredTestData(ValueType.INT64_SWAP);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataSInt64Swap")
    public void testExtractIndividualSInt64Swap(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex,
                () -> ModbusBitUtilities.extractSInt64Swap(bytes, byteIndex), decimal -> decimal.longValue());
    }

    public static Stream<Object[]> filteredTestDataUInt64Swap() {
        return filteredTestData(ValueType.UINT64_SWAP);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataUInt64Swap")
    public void testExtractIndividualUInt64Swap(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex,
                () -> ModbusBitUtilities.extractUInt64Swap(bytes, byteIndex),
                decimal -> decimal.toBigDecimal().toBigIntegerExact());
    }

    public static Stream<Object[]> filteredTestDataFloat32() {
        return filteredTestData(ValueType.FLOAT32);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataFloat32")
    public void testExtractIndividualFloat32(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex,
                () -> ModbusBitUtilities.extractFloat32(bytes, byteIndex), decimal -> decimal.floatValue(), Float.NaN);
    }

    public static Stream<Object[]> filteredTestDataFloat32Swap() {
        return filteredTestData(ValueType.FLOAT32_SWAP);
    }

    @ParameterizedTest
    @MethodSource("filteredTestDataFloat32Swap")
    public void testExtractIndividualFloat32Swap(Object expectedResult, ValueType type, byte[] bytes, int byteIndex)
            throws InstantiationException, IllegalAccessException {
        testIndividual(expectedResult, type, bytes, byteIndex,
                () -> ModbusBitUtilities.extractFloat32Swap(bytes, byteIndex), decimal -> decimal.floatValue(),
                Float.NaN);
    }
}
