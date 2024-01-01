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
package org.openhab.binding.dbquery.internal;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault({})
class Value2StateConverterTest {
    public static final BigDecimal BIG_DECIMAL_NUMBER = new BigDecimal("212321213123123123123123");
    private Value2StateConverter instance;

    @BeforeEach
    void setUp() {
        instance = new Value2StateConverter();
    }

    @AfterEach
    void tearDown() {
        instance = null;
    }

    @ParameterizedTest
    @ValueSource(classes = { StringType.class, DecimalType.class, DateTimeType.class, OpenClosedType.class,
            OnOffType.class })
    void givenNullValueReturnUndef(Class<State> classe) {
        assertThat(instance.convertValue(null, classe), is(UnDefType.NULL));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "stringValue" })
    void givenStringValueAndStringTargetReturnStringtype(String value) {
        var converted = instance.convertValue(value, StringType.class);
        assertThat(converted.toFullString(), is(value));
    }

    @ParameterizedTest
    @MethodSource("provideValuesOfAllSupportedResultRowTypesExceptBytes")
    void givenValidObjectTypesAndStringTargetReturnStringtypeWithString(Object value) {
        var converted = instance.convertValue(value, StringType.class);
        assertThat(converted.toFullString(), is(value.toString()));
    }

    @Test
    void givenByteArrayAndStringTargetReturnEncodedBase64() {
        var someBytes = "Hello world".getBytes(Charset.defaultCharset());
        var someBytesB64 = Base64.getEncoder().encodeToString(someBytes);
        var converted = instance.convertValue(someBytes, StringType.class);
        assertThat(converted.toFullString(), is(someBytesB64));
    }

    @ParameterizedTest
    @MethodSource("provideNumericTypes")
    void givenNumericTypeAndDecimalTargetReturnDecimaltype(Number value) {
        var converted = instance.convertValue(value, DecimalType.class);
        assertThat(converted, instanceOf(DecimalType.class));
        assertThat(((DecimalType) converted).doubleValue(), closeTo(value.doubleValue(), 0.01d));
    }

    @ParameterizedTest
    @MethodSource("provideNumericTypes")
    void givenNumericStringAndDecimalTargetReturnDecimaltype(Number value) {
        var numberString = value.toString();
        var converted = instance.convertValue(numberString, DecimalType.class);
        assertThat(converted, instanceOf(DecimalType.class));
        assertThat(((DecimalType) converted).doubleValue(), closeTo(value.doubleValue(), 0.01d));
    }

    @Test
    void givenDurationAndDecimalTargetReturnDecimaltypeWithMilliseconds() {
        var duration = Duration.ofDays(1);
        var converted = instance.convertValue(duration, DecimalType.class);
        assertThat(converted, instanceOf(DecimalType.class));
        assertThat(((DecimalType) converted).longValue(), is(duration.toMillis()));
    }

    @Test
    void givenInstantAndDatetimeTargetReturnDatetype() {
        var instant = Instant.now();
        var converted = instance.convertValue(instant, DateTimeType.class);
        assertThat(converted, instanceOf(DateTimeType.class));
        assertThat(((DateTimeType) converted).getZonedDateTime(),
                is(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()).withFixedOffsetZone()));
    }

    @Test
    void givenDateAndDatetimeTargetReturnDatetype() {
        var date = new Date();
        var converted = instance.convertValue(date, DateTimeType.class);
        assertThat(converted, instanceOf(DateTimeType.class));
        assertThat(((DateTimeType) converted).getZonedDateTime(),
                is(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).withFixedOffsetZone()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "2019-10-12T07:20:50.52Z", "2019-10-12" })
    void givenValidStringDateAndDatetimeTargetReturnDatetype(String date) {
        var converted = instance.convertValue(date, DateTimeType.class);
        assertThat(converted, instanceOf(DateTimeType.class));
        var convertedDateTime = ((DateTimeType) converted).getZonedDateTime();
        assertThat(convertedDateTime.getYear(), is(2019));
        assertThat(convertedDateTime.getMonthValue(), is(10));
        assertThat(convertedDateTime.getDayOfMonth(), is(12));
        assertThat(convertedDateTime.getHour(), anyOf(is(7), is(0)));
    }

    @ParameterizedTest
    @MethodSource("trueValues")
    void givenValuesConsideratedTrueAndOnOffTargetReturnOn(Object value) {
        var converted = instance.convertValue(value, OnOffType.class);
        assertThat(converted, instanceOf(OnOffType.class));
        assertThat(converted, is(OnOffType.ON));
    }

    @ParameterizedTest
    @MethodSource("falseValues")
    void givenValuesConsideratedFalseAndOnOffTargetReturnOff(Object value) {
        var converted = instance.convertValue(value, OnOffType.class);
        assertThat(converted, instanceOf(OnOffType.class));
        assertThat(converted, is(OnOffType.OFF));
    }

    @ParameterizedTest
    @MethodSource("trueValues")
    void givenValuesConsideratedTrueAndOpenClosedTargetReturnOpen(Object value) {
        var converted = instance.convertValue(value, OpenClosedType.class);
        assertThat(converted, instanceOf(OpenClosedType.class));
        assertThat(converted, is(OpenClosedType.OPEN));
    }

    @ParameterizedTest
    @MethodSource("falseValues")
    void givenValuesConsideratedFalseAndOpenClosedTargetReturnClosed(Object value) {
        var converted = instance.convertValue(value, OpenClosedType.class);
        assertThat(converted, instanceOf(OpenClosedType.class));
        assertThat(converted, is(OpenClosedType.CLOSED));
    }

    private static Stream<Object> trueValues() {
        return Stream.of("true", "True", 1, 2, "On", "on", -1, 0.3);
    }

    private static Stream<Object> falseValues() {
        return Stream.of("false", "False", 0, 0.0d, "off", "Off", "", "a value");
    }

    private static Stream<Number> provideNumericTypes() {
        return Stream.of(1L, 1.2, 1.2f, -1, 0, BIG_DECIMAL_NUMBER);
    }

    private static Stream<Object> provideValuesOfAllSupportedResultRowTypes() {
        return Stream.of("", "String", Boolean.TRUE, 1L, 1.2, 1.2f, BIG_DECIMAL_NUMBER,
                "bytes".getBytes(Charset.defaultCharset()), Instant.now(), new Date(), Duration.ofDays(1));
    }

    private static Stream<Object> provideValuesOfAllSupportedResultRowTypesExceptBytes() {
        return provideValuesOfAllSupportedResultRowTypes().filter(o -> !(o instanceof byte[]));
    }
}
