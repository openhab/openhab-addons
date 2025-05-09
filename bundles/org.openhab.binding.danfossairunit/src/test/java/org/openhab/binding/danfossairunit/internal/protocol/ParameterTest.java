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
package org.openhab.binding.danfossairunit.internal.protocol;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * This class provides test cases for {@link Parameter}
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ParameterTest {

    @Test
    void getRequestWhenReadHumidityReturnsValidRequest() {
        byte[] actual = Parameter.HUMIDITY.getRequest();
        assertThat(actual, is(equalTo(new byte[] { 0x01, 0x04, 0x14, 0x70 })));
    }

    @Test
    void getRequestWhenWriteManualModeReturnsValidRequest() {
        byte[] actual = Parameter.MODE.getRequest(new byte[] { 0x02 });
        assertThat(actual, is(equalTo(new byte[] { 0x01, 0x06, 0x14, 0x12, 0x02 })));
    }

    @Test
    void getRequestWhenAttemptingWriteOperationForReadOnlyThrows() {
        assertThrows(IllegalArgumentException.class, () -> Parameter.HUMIDITY.getRequest(new byte[] { (byte) 0x64 }));
    }
}
