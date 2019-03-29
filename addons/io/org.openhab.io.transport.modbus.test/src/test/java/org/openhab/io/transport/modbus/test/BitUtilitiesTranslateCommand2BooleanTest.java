/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.junit.Test;
import org.openhab.io.transport.modbus.ModbusBitUtilities;

public class BitUtilitiesTranslateCommand2BooleanTest {

    @Test
    public void testZero() {
        Optional<Boolean> actual = ModbusBitUtilities.translateCommand2Boolean(DecimalType.ZERO);
        assertThat(actual, is(equalTo(Optional.of(false))));
    }

    @Test
    public void testNegative() {
        Optional<Boolean> actual = ModbusBitUtilities.translateCommand2Boolean(new DecimalType(-3.4));
        assertThat(actual, is(equalTo(Optional.of(true))));
    }

    @Test
    public void testPositive() {
        Optional<Boolean> actual = ModbusBitUtilities.translateCommand2Boolean(new DecimalType(3.4));
        assertThat(actual, is(equalTo(Optional.of(true))));
    }

    @Test
    public void testOn() {
        Optional<Boolean> actual = ModbusBitUtilities.translateCommand2Boolean(OnOffType.ON);
        assertThat(actual, is(equalTo(Optional.of(true))));
    }

    @Test
    public void testOpen() {
        Optional<Boolean> actual = ModbusBitUtilities.translateCommand2Boolean(OpenClosedType.OPEN);
        assertThat(actual, is(equalTo(Optional.of(true))));
    }

    @Test
    public void testOff() {
        Optional<Boolean> actual = ModbusBitUtilities.translateCommand2Boolean(OnOffType.OFF);
        assertThat(actual, is(equalTo(Optional.of(false))));
    }

    @Test
    public void testClosed() {
        Optional<Boolean> actual = ModbusBitUtilities.translateCommand2Boolean(OpenClosedType.CLOSED);
        assertThat(actual, is(equalTo(Optional.of(false))));
    }

    @Test
    public void testUnknown() {
        Optional<Boolean> actual = ModbusBitUtilities.translateCommand2Boolean(IncreaseDecreaseType.INCREASE);
        assertThat(actual, is(equalTo(Optional.empty())));
    }
}
