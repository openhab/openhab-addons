/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@RunWith(JUnitParamsRunner.class)
@NonNullByDefault
public abstract class ComplexEnumGsonAdapterTest<E extends Enum<E>, T extends TypeAdapter<E>> extends GsonAdapterTestBase<T> {
    protected abstract List<List<Object>> getGoodParams();
    protected abstract E getUnexpectedEnum();
    protected abstract String getUnexpectedString();

    @Test
    @Parameters(method = "getGoodParams")
    @TestCaseName("read(\"{1}\") = {0}")
    public void testReadGoodValue(
            final E expectedEnumValue,
            String providedStringValue
    ) throws IOException {
        testReadValue(expectedEnumValue, toJsonString(providedStringValue));
    }

    @Test
    @Parameters(method = "getGoodParams")
    @TestCaseName("write({0}) = \"{1}\"")
    public void testWriteGoodValue(
            final E providedEnumValue,
            String expectedStringValue
    ) throws IOException {
        testWriteValue(providedEnumValue, toJsonString(expectedStringValue));
    }

    @Test
    public void testReadUnexpectedValue() throws IOException {
        testReadValue(this.getUnexpectedEnum(), toJsonString(this.getUnexpectedString()));
    }

    @Test
    public void testWriteUnexpectedValue() throws IOException {
        testWriteValue(this.getUnexpectedEnum(), JSON_NULL);
    }

    private void testReadValue(
            final E expectedEnumValue,
            String providedStringValue
    ) throws IOException {
        /* Given */
        final JsonReader jsonReader = new JsonReader(
                new StringReader(providedStringValue)
        );

        final T adapter = this.getAdapter();


        /* When */
        final E enumValue = adapter.read(jsonReader);


        /* Then */
        assertThat(enumValue).isEqualTo(expectedEnumValue);
    }

    private void testWriteValue(
            final E providedEnumValue,
            String expectedStringValue
    ) throws IOException {
        /* Given */
        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(stringWriter);

        final T adapter = this.getAdapter();


        /* When */
        adapter.write(jsonWriter, providedEnumValue);


        /* Then */
        assertThat(stringWriter.toString()).isEqualTo(expectedStringValue);
    }
}
