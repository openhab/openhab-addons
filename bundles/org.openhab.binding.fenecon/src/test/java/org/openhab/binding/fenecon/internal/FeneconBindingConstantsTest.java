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
package org.openhab.binding.fenecon.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fenecon.internal.api.Address;

/**
 * Test for {@link FeneconBindingConstants}.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class FeneconBindingConstantsTest {

    @Test
    void checkAllAddressesAreListed() throws IllegalArgumentException, IllegalAccessException {
        List<Address> findAddresses = new ArrayList<>();

        for (Field eachDeclaredField : FeneconBindingConstants.class.getDeclaredFields()) {
            if (eachDeclaredField.getName().endsWith("_ADDRESS")) {
                String address = (String) eachDeclaredField.get(FeneconBindingConstants.class);
                if (address != null) {
                    findAddresses.add(new Address(address));
                }
            }
        }

        assertEquals(FeneconBindingConstants.ADDRESSES.size(), findAddresses.size());
        assertTrue(findAddresses.containsAll(FeneconBindingConstants.ADDRESSES));
    }

    @Test
    void checkAllAddressesAreUnique() throws IllegalArgumentException, IllegalAccessException {
        List<Address> findAddresses = new ArrayList<>();

        for (Field eachDeclaredField : FeneconBindingConstants.class.getDeclaredFields()) {
            if (eachDeclaredField.getName().endsWith("_ADDRESS")) {
                String address = (String) eachDeclaredField.get(FeneconBindingConstants.class);
                if (address != null) {
                    Address findAddress = new Address(address);
                    assertFalse(findAddresses.contains(findAddress),
                            "Duplicate address found: " + findAddress + " for field " + eachDeclaredField.getName());
                    findAddresses.add(findAddress);
                }
            }
        }
    }

    @Test
    void checkAllAddressesConsistOfComponentAndChannel() throws IllegalArgumentException, IllegalAccessException {
        for (Field eachDeclaredField : FeneconBindingConstants.class.getDeclaredFields()) {
            if (eachDeclaredField.getName().endsWith("_ADDRESS")) {
                String address = (String) eachDeclaredField.get(FeneconBindingConstants.class);
                if (address != null) {
                    assertDoesNotThrow(() -> new Address(address));
                }
            }
        }
    }
}
