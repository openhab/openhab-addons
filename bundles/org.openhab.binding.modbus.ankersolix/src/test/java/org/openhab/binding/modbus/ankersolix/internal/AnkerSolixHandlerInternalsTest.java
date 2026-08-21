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
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.measure.MetricPrefix;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
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
    void setUp() throws Exception {
        handler = newHandler();
        Set<Object> activeHandlers = getField(handler, "ACTIVE_HANDLERS");
        activeHandlers.clear();
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
        assertEquals("Anker SOLIX Smart Plug Gen 2", smartPlugMapped);

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

    @Test
    void batteryPowerSetpointShouldAcceptZeroAndRejectOutOfRange() throws Exception {
        invokeVoid(handler, "handleDeviceCommand", "battery-power-setpoint", new DecimalType("0"));

        State zeroShadow = invoke(handler, "getShadowState", "battery-power-setpoint");
        assertInstanceOf(QuantityType.class, zeroShadow);
        QuantityType<?> zeroQuantity = (QuantityType<?>) zeroShadow;
        assertEquals(0, zeroQuantity.toBigDecimal().intValue());

        handler = newHandler();
        invokeVoid(handler, "handleDeviceCommand", "battery-power-setpoint", new DecimalType("10001"));
        State outOfRangeShadow = invoke(handler, "getShadowState", "battery-power-setpoint");
        assertNull(outOfRangeShadow);
    }

    @Test
    void backupSocEnableShouldSetSwitchShadowForOnAndOff() throws Exception {
        invokeVoid(handler, "handleDeviceCommand", "backup-soc-enable", OnOffType.ON);
        State onShadow = invoke(handler, "getShadowState", "backup-soc-enable");
        assertEquals(OnOffType.ON, onShadow);

        invokeVoid(handler, "handleDeviceCommand", "backup-soc-enable", OnOffType.OFF);
        State offShadow = invoke(handler, "getShadowState", "backup-soc-enable");
        assertEquals(OnOffType.OFF, offShadow);
    }

    @Test
    void chargingLimitSocShouldOnlySetShadowWithinRange() throws Exception {
        invokeVoid(handler, "handleDeviceCommand", "charging-limit-soc", new DecimalType("80"));
        State minShadow = invoke(handler, "getShadowState", "charging-limit-soc");
        assertInstanceOf(QuantityType.class, minShadow);
        assertEquals(80, ((QuantityType<?>) minShadow).toBigDecimal().intValue());

        handler = newHandler();
        invokeVoid(handler, "handleDeviceCommand", "charging-limit-soc", new DecimalType("79"));
        State belowMinShadow = invoke(handler, "getShadowState", "charging-limit-soc");
        assertNull(belowMinShadow);

        invokeVoid(handler, "handleDeviceCommand", "charging-limit-soc", new DecimalType("100"));
        State maxShadow = invoke(handler, "getShadowState", "charging-limit-soc");
        assertInstanceOf(QuantityType.class, maxShadow);
        assertEquals(100, ((QuantityType<?>) maxShadow).toBigDecimal().intValue());
    }

    @Test
    void dischargeAndReserveSocShouldRespectConfiguredRanges() throws Exception {
        invokeVoid(handler, "handleDeviceCommand", "discharge-limit-soc", new DecimalType("20"));
        State dischargeMaxShadow = invoke(handler, "getShadowState", "discharge-limit-soc");
        assertInstanceOf(QuantityType.class, dischargeMaxShadow);
        assertEquals(20, ((QuantityType<?>) dischargeMaxShadow).toBigDecimal().intValue());

        handler = newHandler();
        invokeVoid(handler, "handleDeviceCommand", "discharge-limit-soc", new DecimalType("21"));
        State dischargeOutOfRangeShadow = invoke(handler, "getShadowState", "discharge-limit-soc");
        assertNull(dischargeOutOfRangeShadow);

        invokeVoid(handler, "handleDeviceCommand", "backup-reserve-soc", new DecimalType("100"));
        State reserveMaxShadow = invoke(handler, "getShadowState", "backup-reserve-soc");
        assertInstanceOf(QuantityType.class, reserveMaxShadow);
        assertEquals(100, ((QuantityType<?>) reserveMaxShadow).toBigDecimal().intValue());

        handler = newHandler();
        invokeVoid(handler, "handleDeviceCommand", "backup-reserve-soc", new DecimalType("101"));
        State reserveOutOfRangeShadow = invoke(handler, "getShadowState", "backup-reserve-soc");
        assertNull(reserveOutOfRangeShadow);
    }

    @Test
    void capabilityMaskShouldGateBackupSocCommands() throws Exception {
        Map<Integer, Integer> registerCache = getField(handler, "registerCache");
        registerCache.put(32775, 0);

        Boolean unsupported = invoke(handler, "isCapabilitySupported", 0);
        assertFalse(unsupported);

        invokeVoid(handler, "handleDeviceCommand", "charging-limit-soc", new DecimalType("80"));
        assertNull(invoke(handler, "getShadowState", "charging-limit-soc"));

        registerCache.put(32775, 1 << 0);
        Boolean supported = invoke(handler, "isCapabilitySupported", 0);
        assertTrue(supported);

        invokeVoid(handler, "handleDeviceCommand", "charging-limit-soc", new DecimalType("80"));
        assertNotNull(invoke(handler, "getShadowState", "charging-limit-soc"));
    }

    @Test
    void capabilityMaskPollShouldBeOptionalAndReadSeparately() throws Exception {
        List<?> pollRanges = invoke(handler, "getPollRanges");
        Object capabilityRange = nonNull(pollRanges.get(pollRanges.size() - 1));
        Object startAddress = nonNull(invoke(capabilityRange, "startAddress"));
        Object length = nonNull(invoke(capabilityRange, "length"));
        Object optional = nonNull(invoke(capabilityRange, "optional"));

        assertEquals(Integer.valueOf(32775), startAddress);
        assertEquals(Integer.valueOf(1), length);
        assertEquals(true, optional);
    }

    @Test
    void capabilityMaskReadFailureShouldBackOffRegularPolling() throws Exception {
        List<?> pollRanges = invoke(handler, "getPollRanges");
        Object capabilityRange = nonNull(pollRanges.get(pollRanges.size() - 1));
        AsyncModbusFailure<ModbusReadRequestBlueprint> failure = new AsyncModbusFailure<>(
                mock(ModbusReadRequestBlueprint.class), new RuntimeException("Illegal Data Address"));

        invokeVoid(handler, "handleReadFailure", capabilityRange, failure);

        Set<Object> backedOffRanges = getField(handler, "backedOffRanges");
        assertTrue(backedOffRanges.contains(capabilityRange));

        // a repeated failure must stay idempotent (no duplicate bookkeeping)
        invokeVoid(handler, "handleReadFailure", capabilityRange, failure);
        assertEquals(1, backedOffRanges.size());
    }

    @Test
    void capabilityMaskReadFailureShouldNotBackOffWhileThingIsNotOnline() throws Exception {
        Thing offlineThing = mock(Thing.class);
        when(offlineThing.getStatus()).thenReturn(ThingStatus.OFFLINE);
        AbstractAnkerSolixHandler offlineHandler = new AnkerSolixSolarbankHandler(offlineThing);

        List<?> pollRanges = invoke(offlineHandler, "getPollRanges");
        Object capabilityRange = nonNull(pollRanges.get(pollRanges.size() - 1));
        AsyncModbusFailure<ModbusReadRequestBlueprint> failure = new AsyncModbusFailure<>(
                mock(ModbusReadRequestBlueprint.class), new RuntimeException("connection timed out"));

        // a general communication failure (bridge/network down) must not be mistaken for register rejection
        invokeVoid(offlineHandler, "handleReadFailure", capabilityRange, failure);

        Set<Object> backedOffRanges = getField(offlineHandler, "backedOffRanges");
        assertTrue(backedOffRanges.isEmpty());
    }

    @Test
    void firmwareVersionChangeShouldNotResumeAnUnbackedOffRange() throws Exception {
        invokeVoid(handler, "checkFirmwareVersionChange", "1.0.0.0");

        // changing the version afterwards must not throw when the range was never backed off
        invokeVoid(handler, "checkFirmwareVersionChange", "1.1.0.0");

        Set<Object> backedOffRanges = getField(handler, "backedOffRanges");
        assertTrue(backedOffRanges.isEmpty());
    }

    @Test
    void firmwareVersionChangeShouldKeepRangeBackedOffWithoutModbusConfig() throws Exception {
        List<?> pollRanges = invoke(handler, "getPollRanges");
        Object capabilityRange = nonNull(pollRanges.get(pollRanges.size() - 1));
        Set<Object> backedOffRanges = getField(handler, "backedOffRanges");
        backedOffRanges.add(capabilityRange);

        invokeVoid(handler, "checkFirmwareVersionChange", "1.0.0.0");
        invokeVoid(handler, "checkFirmwareVersionChange", "1.1.0.0");

        // without a Modbus configuration, resumePolling() must be a no-op and keep the range backed off
        assertTrue(backedOffRanges.contains(capabilityRange));
    }

    @Test
    void resumeAllBackedOffRangesShouldBeNoOpWithoutModbusConfig() throws Exception {
        List<?> pollRanges = invoke(handler, "getPollRanges");
        Object capabilityRange = nonNull(pollRanges.get(pollRanges.size() - 1));
        Set<Object> backedOffRanges = getField(handler, "backedOffRanges");
        backedOffRanges.add(capabilityRange);

        invokeVoid(handler, "resumeAllBackedOffRanges");

        assertTrue(backedOffRanges.contains(capabilityRange));
    }

    @Test
    void notifyOtherHandlersOfNewDeviceShouldRegisterAndPromptSiblingsToRetry() throws Exception {
        AbstractAnkerSolixHandler existingHandler = newHandler();
        List<?> pollRanges = invoke(existingHandler, "getPollRanges");
        Object capabilityRange = nonNull(pollRanges.get(pollRanges.size() - 1));
        Set<Object> existingBackedOffRanges = getField(existingHandler, "backedOffRanges");
        existingBackedOffRanges.add(capabilityRange);

        Set<Object> activeHandlers = getField(handler, "ACTIVE_HANDLERS");
        activeHandlers.add(existingHandler);

        // adding a new device (this handler initializing) must prompt the already-active sibling to retry,
        // since a parallel-machine capability register may only start answering once another unit is paired
        invokeVoid(handler, "notifyOtherHandlersOfNewDevice");

        assertTrue(activeHandlers.contains(handler));
        // without a Modbus configuration on the sibling, the retry is a safe no-op and keeps it backed off
        assertTrue(existingBackedOffRanges.contains(capabilityRange));
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String fieldName) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    @SuppressWarnings("null")
    private static Object nonNull(@Nullable Object value) {
        return Objects.requireNonNull(value);
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
        Thing thing = mock(Thing.class);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        return new AnkerSolixSolarbankHandler(thing);
    }
}
