/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.fenecon.internal.api;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Address}.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class AddressTest {

    @Test
    void testSplitAddress() {
        String adress = "component/channel";

        Address restApiAddress = new Address(adress);

        assertEquals("component", restApiAddress.getComponent().component());
        assertEquals("channel", restApiAddress.getChannel().channel());
    }

    @Test
    void testInvalidAddress1() {
        String invalidAddress = "invalidAddress";

        assertThrowsExactly(IllegalArgumentException.class, () -> {
            new Address(invalidAddress);
        });
    }

    @Test
    void testInvalidAddress2() {
        String invalidAddress = "in/valid/address";

        assertThrowsExactly(IllegalArgumentException.class, () -> {
            new Address(invalidAddress);
        });
    }

    @Test
    void testCompareSameAddress() {
        Address adress1 = new Address("component/channel");
        Address adress2 = new Address("component/channel");

        assertEquals(adress1, adress2);
    }

    @Test
    void testCompareNotSameAddress() {
        Address adress1 = new Address("component/channel1");
        Address adress2 = new Address("component/channel2");

        assertNotEquals(adress1, adress2);
    }
}
