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
package org.openhab.binding.amazonechocontrol.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.util.NonNullListTypeAdapterFactory;
import org.openhab.binding.amazonechocontrol.internal.util.SerializeNull;
import org.openhab.binding.amazonechocontrol.internal.util.SerializeNullTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link GsonTypeAdapterFactoriesTest} contains tests for the various {@link TypeAdapterFactory} implementations
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
public class GsonTypeAdapterFactoriesTest {
    @Test
    public void testSerializeNullFactoryReturnsNullForNonAnnotatedClass() {
        TypeAdapterFactory factory = new SerializeNullTypeAdapterFactory();
        TypeToken<NonAnnotatedTestTO> typeToken = TypeToken.get(NonAnnotatedTestTO.class);
        TypeAdapter<NonAnnotatedTestTO> typeAdapter = factory.create(new Gson(), typeToken);

        assertThat(typeAdapter, is(nullValue()));
    }

    @Test
    public void testSerializeNullFactoryReturnsTypeAdapterForAnnotatedClass() {
        TypeAdapterFactory factory = new SerializeNullTypeAdapterFactory();
        TypeToken<?> typeToken = TypeToken.get(AnnotatedTestTO.class);
        TypeAdapter<?> typeAdapter = factory.create(new Gson(), typeToken);

        assertThat(typeAdapter, is(notNullValue()));
    }

    @Test
    public void testNonNullListFactoryReturnsNullForNonListClass() {
        TypeAdapterFactory factory = new NonNullListTypeAdapterFactory();
        TypeToken<?> typeToken = TypeToken.get(String.class);
        TypeAdapter<?> typeAdapter = factory.create(new Gson(), typeToken);

        assertThat(typeAdapter, is(nullValue()));
    }

    @Test
    public void testNonNullListFactoryReturnsTypeAdapterForAnnotatedClass() {
        TypeAdapterFactory factory = new NonNullListTypeAdapterFactory();
        TypeToken<?> typeToken = TypeToken.getParameterized(List.class, String.class);
        TypeAdapter<?> typeAdapter = factory.create(new Gson(), typeToken);

        assertThat(typeAdapter, is(notNullValue()));
    }

    @Test
    public void testSerializeAnnotatedNull() {
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new SerializeNullTypeAdapterFactory()).create();

        String serialized = gson.toJson(new AnnotatedTestTO());
        String expected = "{\"annotatedNullValue\":null,\"annotatedNonNullValue\":\"bar\","
                + "\"nonNullValue\":\"foo\",\"serializedNameNonNullValue\":\"foo\"}";

        assertThat(serialized, is(expected));
    }

    @Test
    public void testNullListsAreNotDeserialized() {
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new NonNullListTypeAdapterFactory()).create();

        String in = "{\"list\" : [\"foo\"],\"nullList\" : null}";

        ListTestTO listTest = Objects.requireNonNull(gson.fromJson(in, ListTestTO.class));
        assertThat(listTest.list, is(List.of("foo")));
        assertThat(listTest.nullList, is(List.of()));
        assertThat(listTest.missingList, is(List.of()));
    }

    @Test
    public void combinedTest() {
        String in = "{\"list\" : null}";

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new SerializeNullTypeAdapterFactory())
                .registerTypeAdapterFactory(new NonNullListTypeAdapterFactory()).create();

        CombinedTestTO combined = Objects.requireNonNull(gson.fromJson(in, CombinedTestTO.class));
        assertThat(combined.list, is(List.of()));

        String expected = "{\"serializeNullString\":null,\"list\":[]}";
        String out = gson.toJson(combined);
        assertThat(out, is(expected));
    }

    private static class ListTestTO {
        public List<String> list = List.of();
        public List<String> nullList = List.of();
        public List<String> missingList = List.of();
    }

    private static class CombinedTestTO {
        @SerializeNull
        public @Nullable String serializeNullString = null;
        public @Nullable String noSerializeNullString = null;
        public List<String> list = List.of();
    }

    private static class AnnotatedTestTO extends NonAnnotatedTestTO {
        @SerializeNull
        public @Nullable String annotatedNullValue = null;

        @SerializeNull
        public @Nullable String annotatedNonNullValue = "bar";
    }

    private static class NonAnnotatedTestTO {
        public @Nullable String nullValue = null;

        public String nonNullValue = "foo";

        @SerializedName("serializedNameNullValue")
        public @Nullable String nullValue2 = null;

        @SerializedName("serializedNameNonNullValue")
        public String nonNullNullValue2 = "foo";
    }
}
