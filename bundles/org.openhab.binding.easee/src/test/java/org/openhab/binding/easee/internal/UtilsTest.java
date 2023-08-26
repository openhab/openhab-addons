/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.easee.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class UtilsTest {

    @Test
    public void formatDateTest1() {
        assertNotNull(Utils.formatDate(Instant.EPOCH));
    }

    @Test
    public void formatDateTest2() {
        assertThat(Utils.formatDate(Instant.EPOCH), startsWith("1970-01-01 "));
        assertThat(Utils.formatDate(Instant.EPOCH), endsWith(":00"));
    }

    @Test
    public void formatDateTest3() {
        // formatter cannot handle this date (at least in timezone CEST but should return default toString()
        assertThat(Utils.formatDate(Instant.MIN), startsWith("-1000000000-01-01"));
    }
}
