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
package org.openhab.binding.tibberpulselocal.internal.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibberpulselocal.internal.TibberPulseLocalBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests the validation rules of the {@code thing-lifecycle} spec, "Configuration parameters" and "Meter
 * identification".
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class ConfigurationTest {

    private static final String PASSWORD = "s3cret-Pw";

    private static BridgeConfiguration bridge(String hostname, String password) {
        BridgeConfiguration config = new BridgeConfiguration();
        config.hostname = hostname;
        config.password = password;
        return config;
    }

    private static MeterConfiguration meter(String deviceId, int staleTimeout) {
        MeterConfiguration config = new MeterConfiguration();
        config.deviceId = deviceId;
        config.staleTimeout = staleTimeout;
        return config;
    }

    @Test
    void whenTheHostnameIsAnIpv4AddressThenItIsValid() {
        assertNull(bridge("192.0.2.20", PASSWORD).validate());
    }

    @Test
    void whenTheHostnameIsAnIpv6AddressThenItIsValid() {
        assertNull(bridge("2001:db8::1", PASSWORD).validate());
    }

    @Test
    void whenTheHostnameIsADnsNameThenItIsValid() {
        assertNull(bridge("bridge-a1b2c3d4.local", PASSWORD).validate());
        assertNull(bridge("xn--bcher-kva.example", PASSWORD).validate());
    }

    @Test
    void whenTheHostnameIsAtTheMaximumLengthThenItIsValid() {
        assertNull(bridge("a".repeat(253), PASSWORD).validate());
    }

    @Test
    void whenTheHostnameHasSurroundingWhitespaceThenItIsTrimmed() {
        BridgeConfiguration config = bridge("  bridge.example\t", PASSWORD);

        assertNull(config.validate());
        assertEquals("bridge.example", config.normalizedHostname());
    }

    @Test
    void whenTheHostnameIsEmptyThenItIsInvalid() {
        assertEquals(TEXT_INVALID_HOSTNAME, bridge("", PASSWORD).validate());
        assertEquals(TEXT_INVALID_HOSTNAME, bridge(" \t ", PASSWORD).validate());
    }

    @Test
    void whenTheHostnameHasInternalWhitespaceThenItIsInvalid() {
        assertEquals(TEXT_INVALID_HOSTNAME, bridge("bridge example", PASSWORD).validate());
        assertEquals(TEXT_INVALID_HOSTNAME, bridge("bridge\u00a0example", PASSWORD).validate());
    }

    @Test
    void whenTheHostnameContainsLineBreaksThenItIsInvalid() {
        assertEquals(TEXT_INVALID_HOSTNAME, bridge("bridge.example\r\nX-Injected: 1", PASSWORD).validate());
        assertEquals(TEXT_INVALID_HOSTNAME, bridge("bridge.example\nX", PASSWORD).validate());
    }

    @Test
    void whenTheHostnameContainsAControlCharacterThenItIsInvalid() {
        assertEquals(TEXT_INVALID_HOSTNAME, bridge("bridge\u0000.example", PASSWORD).validate());
        assertEquals(TEXT_INVALID_HOSTNAME, bridge("bridge\u007f.example", PASSWORD).validate());
    }

    @Test
    void whenTheHostnameIsAUrlOrHasAPathThenItIsInvalid() {
        assertEquals(TEXT_INVALID_HOSTNAME, bridge("http://192.0.2.20/", PASSWORD).validate());
        assertEquals(TEXT_INVALID_HOSTNAME, bridge("bridge.example/ws", PASSWORD).validate());
    }

    @Test
    void whenTheHostnameIsLongerThanTheMaximumThenItIsInvalid() {
        assertEquals(TEXT_INVALID_HOSTNAME, bridge("a".repeat(254), PASSWORD).validate());
    }

    @Test
    void whenTheHostnameHasAPortThenValidationAcceptsItAndTheResolverDecides() {
        assertNull(bridge("bridge.example:80", PASSWORD).validate());
    }

    @Test
    void whenThePasswordIsEmptyThenThePasswordIsNamedAsInvalid() {
        assertEquals(TEXT_EMPTY_PASSWORD, bridge("bridge.example", "").validate());
    }

    @Test
    void whenBothAreInvalidThenTheHostnameIsReportedFirst() {
        assertEquals(TEXT_INVALID_HOSTNAME, bridge("", "").validate());
    }

    @Test
    void whenTheBridgeConfigurationIsPrintedThenThePasswordIsNotIncluded() {
        assertFalse(bridge("bridge.example", PASSWORD).toString().contains(PASSWORD));
    }

    @Test
    void whenTheDeviceIdHasSixteenHexDigitsInAnyCaseThenItIsValidAndNormalizedToLowerCase() {
        MeterConfiguration config = meter("0123456789ABCDEF", 60);

        assertNull(config.validate());
        assertEquals("0123456789abcdef", config.normalizedDeviceId());
    }

    @Test
    void whenTheDeviceIdIsMalformedThenTheDeviceIdIsNamedAsInvalid() {
        for (String deviceId : new String[] { "", "not-a-device-id", "0123456789abcde", "0123456789abcdef0",
                "0123456789abcdeg", "0x23456789abcdef" }) {
            assertEquals(TEXT_INVALID_DEVICE_ID, meter(deviceId, 60).validate(), "device id '" + deviceId + "'");
        }
    }

    @Test
    void whenTheStaleTimeoutIsBelowTheMinimumThenItIsRejected() {
        assertEquals(TEXT_INVALID_STALE_TIMEOUT, meter("0123456789abcdef", 5).validate());
        assertEquals(TEXT_INVALID_STALE_TIMEOUT, meter("0123456789abcdef", 9).validate());
        assertNull(meter("0123456789abcdef", 10).validate());
    }

    @Test
    void whenTheStaleTimeoutIsNotSetThenTheDefaultIsSixtySeconds() {
        assertEquals(60, new MeterConfiguration().staleTimeout);
    }
}
