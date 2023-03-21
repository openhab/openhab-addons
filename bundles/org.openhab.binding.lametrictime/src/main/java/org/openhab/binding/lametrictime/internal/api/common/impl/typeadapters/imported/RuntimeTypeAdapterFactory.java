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
/*
 * Copied from
 * https://raw.githubusercontent.com/google/gson/master/extras/src/main/java/com/google/gson/typeadapters/RuntimeTypeAdapterFactory.java
 * and repackaged here with additional content from
 * com.google.gson.internal.{Streams,TypeAdapters,LazilyParsedNumber}
 * to avoid using the internal package.
 */
package org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.imported;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

/**
 * Adapts values whose runtime type may differ from their declaration type. This
 * is necessary when a field's type is not the same type that GSON should create
 * when deserializing that field. For example, consider these types:
 *
 * <pre>
 * {
 *     &#64;code
 *     abstract class Shape {
 *         int x;
 *         int y;
 *     }
 *     class Circle extends Shape {
 *         int radius;
 *     }
 *     class Rectangle extends Shape {
 *         int width;
 *         int height;
 *     }
 *     class Diamond extends Shape {
 *         int width;
 *         int height;
 *     }
 *     class Drawing {
 *         Shape bottomShape;
 *         Shape topShape;
 *     }
 * }
 * </pre>
 * <p>
 * Without additional type information, the serialized JSON is ambiguous. Is
 * the bottom shape in this drawing a rectangle or a diamond?
 *
 * <pre>
 *    {@code
 *   {
 *     "bottomShape": {
 *       "width": 10,
 *       "height": 5,
 *       "x": 0,
 *       "y": 0
 *     },
 *     "topShape": {
 *       "radius": 2,
 *       "x": 4,
 *       "y": 1
 *     }
 *   }}
 * </pre>
 *
 * This class addresses this problem by adding type information to the
 * serialized JSON and honoring that type information when the JSON is
 * deserialized:
 *
 * <pre>
 *    {@code
 *   {
 *     "bottomShape": {
 *       "type": "Diamond",
 *       "width": 10,
 *       "height": 5,
 *       "x": 0,
 *       "y": 0
 *     },
 *     "topShape": {
 *       "type": "Circle",
 *       "radius": 2,
 *       "x": 4,
 *       "y": 1
 *     }
 *   }}
 * </pre>
 *
 * Both the type field name ({@code "type"}) and the type labels ({@code
 * "Rectangle"}) are configurable.
 *
 * <h3>Registering Types</h3>
 * Create a {@code RuntimeTypeAdapterFactory} by passing the base type and type field
 * name to the {@link #of} factory method. If you don't supply an explicit type
 * field name, {@code "type"} will be used.
 *
 * <pre>
 * {
 *     &#64;code
 *     RuntimeTypeAdapterFactory<Shape> shapeAdapterFactory = RuntimeTypeAdapterFactory.of(Shape.class, "type");
 * }
 * </pre>
 *
 * Next register all of your subtypes. Every subtype must be explicitly
 * registered. This protects your application from injection attacks. If you
 * don't supply an explicit type label, the type's simple name will be used.
 *
 * <pre>
 *    {@code
 * shapeAdapter.registerSubtype(Rectangle.class, "Rectangle");
 * shapeAdapter.registerSubtype(Circle.class, "Circle");
 * shapeAdapter.registerSubtype(Diamond.class, "Diamond");
 * }
 * </pre>
 *
 * Finally, register the type adapter factory in your application's GSON builder:
 *
 * <pre>
 * {
 *     &#64;code
 *     Gson gson = new GsonBuilder().registerTypeAdapterFactory(shapeAdapterFactory).create();
 * }
 * </pre>
 *
 * Like {@code GsonBuilder}, this API supports chaining:
 *
 * <pre>
 * {
 *     &#64;code
 *     RuntimeTypeAdapterFactory<Shape> shapeAdapterFactory = RuntimeTypeAdapterFactory.of(Shape.class)
 *             .registerSubtype(Rectangle.class).registerSubtype(Circle.class).registerSubtype(Diamond.class);
 * }
 * </pre>
 *
 * @author Christophe Bornet - Initial contribution
 */
@NonNullByDefault
public final class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
    private final Class<?> baseType;
    private final String typeFieldName;
    private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<String, Class<?>>();
    private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<Class<?>, String>();

    private RuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName) {
        if (typeFieldName == null || baseType == null) {
            throw new IllegalArgumentException();
        }
        this.baseType = baseType;
        this.typeFieldName = typeFieldName;
    }

    /**
     * Creates a new runtime type adapter using for {@code baseType} using {@code
     * typeFieldName} as the type field name. Type field names are case sensitive.
     */
    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName) {
        return new RuntimeTypeAdapterFactory<T>(baseType, typeFieldName);
    }

    /**
     * Creates a new runtime type adapter for {@code baseType} using {@code "type"} as
     * the type field name.
     */
    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType) {
        return new RuntimeTypeAdapterFactory<T>(baseType, "type");
    }

    /**
     * Registers {@code type} identified by {@code label}. Labels are case
     * sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or {@code label}
     *             have already been registered on this type adapter.
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
        if (type == null || label == null) {
            throw new IllegalArgumentException();
        }
        if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
            throw new IllegalArgumentException("types and labels must be unique");
        }
        labelToSubtype.put(label, type);
        subtypeToLabel.put(type, label);
        return this;
    }

    /**
     * Registers {@code type} identified by its {@link Class#getSimpleName simple
     * name}. Labels are case sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or its simple name
     *             have already been registered on this type adapter.
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type) {
        return registerSubtype(type, type.getSimpleName());
    }

    @Override
    public @Nullable <R> TypeAdapter<R> create(@Nullable Gson gson, @Nullable TypeToken<R> type) {
        if (type.getRawType() != baseType) {
            return null;
        }

        final Map<String, TypeAdapter<?>> labelToDelegate = new LinkedHashMap<String, TypeAdapter<?>>();
        final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<Class<?>, TypeAdapter<?>>();
        for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
            TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
            labelToDelegate.put(entry.getKey(), delegate);
            subtypeToDelegate.put(entry.getValue(), delegate);
        }

        return new TypeAdapter<R>() {
            @Override
            public @Nullable R read(JsonReader in) throws IOException {
                JsonElement jsonElement = RuntimeTypeAdapterFactory.parse(in);
                JsonElement labelJsonElement = jsonElement.getAsJsonObject().remove(typeFieldName);
                if (labelJsonElement == null) {
                    throw new JsonParseException("cannot deserialize " + baseType
                            + " because it does not define a field named " + typeFieldName);
                }
                String label = labelJsonElement.getAsString();
                @SuppressWarnings("unchecked") // registration requires that subtype extends T
                TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
                if (delegate == null) {
                    throw new JsonParseException("cannot deserialize " + baseType + " subtype named " + label
                            + "; did you forget to register a subtype?");
                }
                return delegate.fromJsonTree(jsonElement);
            }

            @Override
            public void write(JsonWriter out, @Nullable R value) throws IOException {
                Class<?> srcType = value.getClass();
                String label = subtypeToLabel.get(srcType);
                @SuppressWarnings("unchecked") // registration requires that subtype extends T
                TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
                if (delegate == null) {
                    throw new JsonParseException(
                            "cannot serialize " + srcType.getName() + "; did you forget to register a subtype?");
                }
                JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
                if (jsonObject.has(typeFieldName)) {
                    throw new JsonParseException("cannot serialize " + srcType.getName()
                            + " because it already defines a field named " + typeFieldName);
                }
                JsonObject clone = new JsonObject();
                clone.add(typeFieldName, new JsonPrimitive(label));
                for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                    clone.add(e.getKey(), e.getValue());
                }
                RuntimeTypeAdapterFactory.write(clone, out);
            }
        }.nullSafe();
    }

    /**
     * Takes a reader in any state and returns the next value as a JsonElement.
     */
    private static @Nullable JsonElement parse(JsonReader reader) throws JsonParseException {
        boolean isEmpty = true;
        try {
            reader.peek();
            isEmpty = false;
            return RuntimeTypeAdapterFactory.JSON_ELEMENT.read(reader);
        } catch (EOFException e) {
            /*
             * For compatibility with JSON 1.5 and earlier, we return a JsonNull for
             * empty documents instead of throwing.
             */
            if (isEmpty) {
                return JsonNull.INSTANCE;
            }
            // The stream ended prematurely so it is likely a syntax error.
            throw new JsonSyntaxException(e);
        } catch (MalformedJsonException e) {
            throw new JsonSyntaxException(e);
        } catch (IOException e) {
            throw new JsonIOException(e);
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
        }
    }

    /**
     * Writes the JSON element to the writer, recursively.
     */
    private static void write(JsonElement element, JsonWriter writer) throws IOException {
        RuntimeTypeAdapterFactory.JSON_ELEMENT.write(writer, element);
    }

    private static final TypeAdapter<JsonElement> JSON_ELEMENT = new TypeAdapter<JsonElement>() {
        @Override
        public @Nullable JsonElement read(@Nullable JsonReader in) throws IOException {
            switch (in.peek()) {
                case STRING:
                    return new JsonPrimitive(in.nextString());
                case NUMBER:
                    String number = in.nextString();
                    return new JsonPrimitive(new LazilyParsedNumber(number));
                case BOOLEAN:
                    return new JsonPrimitive(in.nextBoolean());
                case NULL:
                    in.nextNull();
                    return JsonNull.INSTANCE;
                case BEGIN_ARRAY:
                    JsonArray array = new JsonArray();
                    in.beginArray();
                    while (in.hasNext()) {
                        array.add(read(in));
                    }
                    in.endArray();
                    return array;
                case BEGIN_OBJECT:
                    JsonObject object = new JsonObject();
                    in.beginObject();
                    while (in.hasNext()) {
                        object.add(in.nextName(), read(in));
                    }
                    in.endObject();
                    return object;
                case END_DOCUMENT:
                case NAME:
                case END_OBJECT:
                case END_ARRAY:
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public void write(JsonWriter out, @Nullable JsonElement value) throws IOException {
            if (value == null || value.isJsonNull()) {
                out.nullValue();
            } else if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    out.value(primitive.getAsNumber());
                } else if (primitive.isBoolean()) {
                    out.value(primitive.getAsBoolean());
                } else {
                    out.value(primitive.getAsString());
                }

            } else if (value.isJsonArray()) {
                out.beginArray();
                for (JsonElement e : value.getAsJsonArray()) {
                    write(out, e);
                }
                out.endArray();

            } else if (value.isJsonObject()) {
                out.beginObject();
                for (Map.Entry<String, JsonElement> e : value.getAsJsonObject().entrySet()) {
                    out.name(e.getKey());
                    write(out, e.getValue());
                }
                out.endObject();

            } else {
                throw new IllegalArgumentException("Couldn't write " + value.getClass());
            }
        }
    };

    /**
     * This class holds a number value that is lazily converted to a specific number type
     *
     * @author Inderjeet Singh
     */
    public static final class LazilyParsedNumber extends Number {
        private final String value;

        public LazilyParsedNumber(String value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                try {
                    return (int) Long.parseLong(value);
                } catch (NumberFormatException nfe) {
                    return new BigDecimal(value).intValue();
                }
            }
        }

        @Override
        public long longValue() {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return new BigDecimal(value).longValue();
            }
        }

        @Override
        public float floatValue() {
            return Float.parseFloat(value);
        }

        @Override
        public double doubleValue() {
            return Double.parseDouble(value);
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         * If somebody is unlucky enough to have to serialize one of these, serialize
         * it as a BigDecimal so that they won't need Gson on the other side to
         * deserialize it.
         */
        private Object writeReplace() throws ObjectStreamException {
            return new BigDecimal(value);
        }
    }
}
