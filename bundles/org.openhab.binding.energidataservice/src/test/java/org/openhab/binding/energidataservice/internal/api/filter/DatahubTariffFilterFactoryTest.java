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
package org.openhab.binding.energidataservice.internal.api.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.energidataservice.internal.api.DatahubTariffFilter;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameter;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameterType;

/**
 * Tests for {@link DatahubTariffFilterFactory}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class DatahubTariffFilterFactoryTest {

    private DatahubTariffFilterFactory datahubTariffFilterFactory = new DatahubTariffFilterFactory();

    @Test
    void getSystemTariff() {
        DatahubTariffFilter actual = DatahubTariffFilterFactory.getSystemTariff();
        Collection<String> chargeTypeCodes = actual.getChargeTypeCodesAsStrings();
        Collection<String> notes = actual.getNotes();
        assertThat(chargeTypeCodes, is(empty()));
        assertThat(notes.size(), is(equalTo(1)));
        assertThat(notes.stream().findFirst().get(), is(equalTo("Systemtarif")));
        assertThat(actual.getStart(), is(equalTo(DateQueryParameter.of(LocalDate.of(2023, 1, 1)))));
    }

    @Test
    void getTransmissionGridTariff() {
        DatahubTariffFilter actual = DatahubTariffFilterFactory.getTransmissionGridTariff();
        Collection<String> chargeTypeCodes = actual.getChargeTypeCodesAsStrings();
        Collection<String> notes = actual.getNotes();
        assertThat(chargeTypeCodes, is(empty()));
        assertThat(notes.size(), is(equalTo(1)));
        assertThat(notes.stream().findFirst().get(), is(equalTo("Transmissions nettarif")));
        assertThat(actual.getStart(), is(equalTo(DateQueryParameter.of(LocalDate.of(2023, 1, 1)))));
    }

    @Test
    void getElectricityTax() {
        DatahubTariffFilter actual = DatahubTariffFilterFactory.getElectricityTax();
        Collection<String> chargeTypeCodes = actual.getChargeTypeCodesAsStrings();
        Collection<String> notes = actual.getNotes();
        assertThat(chargeTypeCodes, is(empty()));
        assertThat(notes.size(), is(equalTo(1)));
        assertThat(notes.stream().findFirst().get(), is(equalTo("Elafgift")));
        assertThat(actual.getStart(), is(equalTo(DateQueryParameter.of(LocalDate.of(2023, 1, 1)))));
    }

    @Test
    void getReducedElectricityTax() {
        DatahubTariffFilter actual = DatahubTariffFilterFactory.getReducedElectricityTax();
        Collection<String> chargeTypeCodes = actual.getChargeTypeCodesAsStrings();
        Collection<String> notes = actual.getNotes();
        assertThat(chargeTypeCodes, is(empty()));
        assertThat(notes.size(), is(equalTo(1)));
        assertThat(notes.stream().findFirst().get(), is(equalTo("Reduceret elafgift")));
        assertThat(actual.getStart(), is(equalTo(DateQueryParameter.of(LocalDate.of(2021, 2, 1)))));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForGetGridTariffByGLN")
    void getGridTariffByGLN(String gln, DateQueryParameter expectedStart, List<String> expectedChargeTypeCodes,
            List<String> expectedNotes) {
        DatahubTariffFilter actual = datahubTariffFilterFactory.getGridTariffByGLN(gln);
        Collection<String> chargeTypeCodes = actual.getChargeTypeCodesAsStrings();
        Collection<String> notes = actual.getNotes();
        assertThat(chargeTypeCodes, containsInAnyOrder(expectedChargeTypeCodes.toArray()));
        assertThat(notes, containsInAnyOrder(expectedNotes.toArray()));
        assertThat(actual.getStart(), is(equalTo(expectedStart)));
    }

    private static Stream<Arguments> provideTestCasesForGetGridTariffByGLN() {
        return Stream.of( //
                Arguments.of("5790001095451", DateQueryParameter.of(DateQueryParameterType.START_OF_DAY),
                        List.of("AAL-NT-05", "AAL-NTR05"), List.of("Nettarif C time")), //
                Arguments.of("5790000705184", DateQueryParameter.EMPTY, List.of("30TR_C_ET"),
                        List.of("Nettarif C time")), //
                Arguments.of("5790000682102", DateQueryParameter.of(LocalDate.of(2022, 10, 1)), List.of("IEV-NT-01"),
                        List.of("Nettarif C time")), //
                Arguments.of("5790001089030", DateQueryParameter.of(LocalDate.of(2023, 1, 1)), List.of("CD", "CD R"),
                        List.of())); //
    }
}
