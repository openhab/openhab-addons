/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.arcam.internal.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the SA30
 *
 * @author Joep Admiraal - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class ArcamSa30Test {

    @InjectMocks
    @NonNullByDefault({})
    private ArcamSA30 sut;

    @Test
    public void getBalanceShouldParseNegativeValue() {
        int result = sut.getBalance((byte) 0x83);
        assertEquals(-3, result);
    }

    @Test
    public void getBalanceShouldParsePositiveValue() {
        int result = sut.getBalance((byte) 0x03);
        assertEquals(3, result);
    }

    @Test
    public void getTimeoutCounter() {
        int result = sut.getTimeoutCounter(List.of((byte) 0x00, (byte) 0x03));
        assertEquals(3, result);
    }
}
