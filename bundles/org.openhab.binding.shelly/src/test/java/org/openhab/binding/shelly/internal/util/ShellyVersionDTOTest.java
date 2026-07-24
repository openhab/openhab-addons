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
package org.openhab.binding.shelly.internal.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link ShellyVersionDTO}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyVersionDTOTest {
    private final ShellyVersionDTO dto = new ShellyVersionDTO();

    @ParameterizedTest
    @MethodSource("provideTestCasesForStripBuildHash")
    void stripBuildHash(String input, String expected) {
        assertThat(dto.stripBuildHash(input), is(equalTo(expected)));
    }

    private static Stream<Arguments> provideTestCasesForStripBuildHash() {
        return Stream.of( //
                Arguments.of("2.6.2-06f6da23", "2.6.2"), //
                Arguments.of("2.6.2-beta1-06f6da23", "2.6.2-beta1"), //
                Arguments.of("2.6.2-rc1-abc12345", "2.6.2-rc1"), //
                Arguments.of("v1.7.5-g9979d16", "v1.7.5"), //
                Arguments.of("v1.7.5-g9979d1", "v1.7.5"), //
                Arguments.of("2.6.2", "2.6.2"), //
                Arguments.of("1.10.0-rc2", "1.10.0-rc2"), //
                Arguments.of("1.5.1-cabf215a", "1.5.1"), //
                Arguments.of("2.6.2-DEADBEEF", "2.6.2-DEADBEEF"), //
                Arguments.of("", "")); //
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForCheckBeta")
    void checkBeta(String version, boolean expectedBeta) {
        assertThat(dto.checkBeta(version), is(equalTo(expectedBeta)));
    }

    private static Stream<Arguments> provideTestCasesForCheckBeta() {
        return Stream.of( //
                Arguments.of("2.6.2", false), //
                Arguments.of("2.6.2-06f6da23", false), //
                Arguments.of("1.7.5", false), //
                Arguments.of("1.4.4", false), //
                Arguments.of("1.10.0-rc2", true), //
                Arguments.of("1.10.0-beta1", true), //
                Arguments.of("1.10.0-BETA1", true), //
                Arguments.of("1.10.0-rc2-g623b41ec0-master", true), //
                Arguments.of("???", true), //
                Arguments.of("", true), //
                Arguments.of((Object) null, false)); //
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForUpdateAvailableComparison")
    void updateAvailableComparison(String installed, String available, boolean expectedUpdateNeeded) {
        String strippedInstalled = dto.stripBuildHash(installed);
        String strippedAvailable = dto.stripBuildHash(available);
        boolean updateNeeded = dto.compare(strippedInstalled, strippedAvailable) < 0;
        assertThat("installed=" + installed + " available=" + available, updateNeeded,
                is(equalTo(expectedUpdateNeeded)));
    }

    private static Stream<Arguments> provideTestCasesForUpdateAvailableComparison() {
        return Stream.of( //
                Arguments.of("2.6.2-06f6da23", "2.6.0", false), //
                Arguments.of("2.6.2-06f6da23", "2.6.2-abc12345", false), //
                Arguments.of("2.6.2-06f6da23", "2.6.2", false), //
                Arguments.of("2.6.2-06f6da23", "2.6.3-abc12345", true), //
                Arguments.of("2.6.2-06f6da23", "2.6.3", true), //
                Arguments.of("1.7.5", "1.7.6", true), //
                Arguments.of("1.7.5", "1.7.5", false), //
                Arguments.of("1.7.5-g9979d16", "1.7.5-g623b41e", false), //
                Arguments.of("1.4.4", "1.4.4-cabf215a", false), //
                Arguments.of("1.4.3", "1.4.4-cabf215a", true)); //
    }
}
