/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.energidataservice.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.time.Duration;
import java.time.LocalDate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link DateQueryParameter}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class DateQueryParameterTest {

    @Test
    void dateQueryParameterTypeWithNegativeOffset() {
        DateQueryParameter parameter = DateQueryParameter.of(DateQueryParameterType.UTC_NOW, Duration.ofHours(-12));
        assertThat(parameter.toString(), is(equalTo("utcnow-PT12H")));
    }

    @Test
    void dateQueryParameterTypeWithPositiveOffset() {
        DateQueryParameter parameter = DateQueryParameter.of(DateQueryParameterType.UTC_NOW, Duration.ofHours(12));
        assertThat(parameter.toString(), is(equalTo("utcnow+PT12H")));
    }

    @Test
    void dateQueryParameterTypeWithZeroOffset() {
        DateQueryParameter parameter = DateQueryParameter.of(DateQueryParameterType.UTC_NOW, Duration.ZERO);
        assertThat(parameter.toString(), is(equalTo("utcnow")));
    }

    @Test
    void dateQueryParameterTypeWithoutOffset() {
        DateQueryParameter parameter = DateQueryParameter.of(DateQueryParameterType.NOW);
        assertThat(parameter.toString(), is(equalTo("now")));
    }

    @Test
    void localDate() {
        DateQueryParameter parameter = DateQueryParameter.of(LocalDate.of(2023, 2, 28));
        assertThat(parameter.toString(), is(equalTo("2023-02-28")));
    }
}
