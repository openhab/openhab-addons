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
package org.openhab.binding.modbus.ankersolix.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import javax.measure.MetricPrefix;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * Tests internal conversion and shadow-state helper logic.
 *
 * @author Thorben Grove - Initial contribution
 */
@NonNullByDefault
class AnkerSolixHandlerInternalsTest {

    private AbstractAnkerSolixHandler handler = newHandler();

    @BeforeEach
    void setUp() {
        handler = newHandler();
    }

    @Test
    void readInt32ShouldDecodePositiveAndNegativeValues() throws Exception {
        Map<Integer, Integer> registerCache = getField(handler, "registerCache");

        // 0x0001_0002 -> 65538
        registerCache.put(100, 0x0001);
        registerCache.put(101, 0x0002);
        Integer positive = invoke(handler, "readInt32", 100);
        assertEquals(65538, positive);

        // 0xFFFF_FFF0 -> -16
        registerCache.put(200, 0xFFFF);
        registerCache.put(201, 0xFFF0);
        Integer negative = invoke(handler, "readInt32", 200);
        assertEquals(-16, negative);
    }

    @Test
    void readUInt32ShouldDecodeUnsignedValue() throws Exception {
        Map<Integer, Integer> registerCache = getField(handler, "registerCache");
        registerCache.put(300, 0xFFFF);
        registerCache.put(301, 0xFFFE);

        Long value = invoke(handler, "readUInt32", 300);
        assertEquals(4294967294L, value);
    }

    @Test
    void readStringShouldDecodeUtf8AndTrimNulBytes() throws Exception {
        Map<Integer, Integer> registerCache = getField(handler, "registerCache");
        // "ABCD" + NUL padding
        registerCache.put(400, 0x4142);
        registerCache.put(401, 0x4344);
        registerCache.put(402, 0x0000);

        String value = invoke(handler, "readString", 400, 3);
        assertEquals("ABCD", value);
    }

    @Test
    void parseSetpointCommandShouldSupportDecimalAndQuantityTypes() throws Exception {
        Integer decimal = invoke(handler, "parseSetpointCommand", new DecimalType("123"));
        assertEquals(123, decimal);

        QuantityType<?> kilowatt = new QuantityType<>(BigDecimal.valueOf(1.5), MetricPrefix.KILO(Units.WATT));
        Integer quantity = invoke(handler, "parseSetpointCommand", kilowatt);
        assertEquals(1500, quantity);

        Integer unsupported = invoke(handler, "parseSetpointCommand", new StringType("abc"));
        assertNull(unsupported);
    }

    @Test
    void toSignedSetpointShouldFollowSelectedDirection() throws Exception {
        setField(handler, "directionSelection", "discharge");
        Integer dischargePositive = invoke(handler, "toSignedSetpoint", 750);
        assertEquals(750, dischargePositive);

        setField(handler, "directionSelection", "charge");
        Integer chargeNegative = invoke(handler, "toSignedSetpoint", 750);
        assertEquals(-750, chargeNegative);

        Integer chargeNegativeInput = invoke(handler, "toSignedSetpoint", -750);
        assertEquals(-750, chargeNegativeInput);
    }

    @Test
    void resolveModelNameShouldPreferProductCodeMappingFromSerialNumber() throws Exception {
        String mappedFromUppercase = invoke(handler, "resolveModelName", "Generic Model", "DNMS1234567890");
        assertEquals("Anker SOLIX XE AC", mappedFromUppercase);

        String mappedFromLowercase = invoke(handler, "resolveModelName", "Generic Model", "dn7m1234567890");
        assertEquals("Anker SOLIX Solarbank 4 E5000 Pro", mappedFromLowercase);

        String fallbackToRaw = invoke(handler, "resolveModelName", "Generic Model", "ABCD1234567890");
        assertEquals("Generic Model", fallbackToRaw);

        String smartMeterMapped = invoke(handler, "resolveModelName", "Generic Model", "DNSL1234567890");
        assertEquals("Anker SOLIX Smart Meter Gen 2", smartMeterMapped);

        String smartPlugMapped = invoke(handler, "resolveModelName", "Generic Model", "QNA1234567890");
        assertEquals("Anker SOLIX Smart Plug", smartPlugMapped);

        String wallboxMapped = invoke(handler, "resolveModelName", "Generic Model", "A5191GZ212345678");
        assertEquals("Anker SOLIX V1 Smart EV Charger", wallboxMapped);

        String noModel = invoke(handler, "resolveModelName", null, "ABCD1234567890");
        assertNull(noModel);
    }

    @Test
    void readVersionShouldDecodeTwoRegistersToFourSegments() throws Exception {
        Map<Integer, Integer> registerCache = getField(handler, "registerCache");
        registerCache.put(10696, 0x0102);
        registerCache.put(10697, 0x0304);

        String value = invoke(handler, "readVersion", 10696);
        assertEquals("1.2.3.4", value);
    }

    @Test
    void readScaledInt16ShouldApplySignAndGain() throws Exception {
        Map<Integer, Integer> registerCache = getField(handler, "registerCache");
        // 0xFF9C = -100 (INT16)
        registerCache.put(10635, 0xFF9C);

        BigDecimal value = invoke(handler, "readScaledInt16", 10635, 100);
        assertEquals(new BigDecimal("-1"), value);
    }

    @Test
    void readScaledUInt16ShouldApplyGain() throws Exception {
        Map<Integer, Integer> registerCache = getField(handler, "registerCache");
        registerCache.put(30030, 1234);

        BigDecimal value = invoke(handler, "readScaledUInt16", 30030, 10);
        assertEquals(new BigDecimal("123.4"), value);
    }

    @Test
    void shadowStateShouldExpireAndBeRemoved() throws Exception {
        AnkerSolixConfiguration configuration = new AnkerSolixConfiguration();
        configuration.writeProtectionDurationSeconds = 5;
        setField(handler, "config", configuration);

        invokeVoid(handler, "setShadowState", "test-channel", new DecimalType("9"));

        State current = invoke(handler, "getShadowState", "test-channel");
        assertNotNull(current);

        Map<String, Instant> expiryMap = getField(handler, "shadowStateExpiry");
        expiryMap.put("test-channel", Instant.now().minusSeconds(1));

        State expired = invoke(handler, "getShadowState", "test-channel");
        assertNull(expired);

        Map<String, State> shadowMap = getField(handler, "shadowStates");
        assertFalse(shadowMap.containsKey("test-channel"));
        assertFalse(expiryMap.containsKey("test-channel"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String fieldName) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private static void setField(Object target, String fieldName, @Nullable Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                // continue searching the superclass
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(Object target, String methodName, Object... args) throws Exception {
        Method method = findMethod(target.getClass(), methodName, args.length);
        method.setAccessible(true);
        return (T) method.invoke(target, args);
    }

    private static void invokeVoid(Object target, String methodName, Object... args) throws Exception {
        Method method = findMethod(target.getClass(), methodName, args.length);
        method.setAccessible(true);
        method.invoke(target, args);
    }

    private static Method findMethod(Class<?> type, String methodName, int argCount) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == argCount) {
                    return method;
                }
            }
        }
        throw new IllegalArgumentException("Method not found: " + methodName + " with arg count " + argCount);
    }

    private static AbstractAnkerSolixHandler newHandler() {
        return new AnkerSolixSolarbankHandler(mock(Thing.class));
    }
}
