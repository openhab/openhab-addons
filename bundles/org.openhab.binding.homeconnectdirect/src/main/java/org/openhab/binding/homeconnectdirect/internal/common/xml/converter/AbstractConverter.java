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
package org.openhab.binding.homeconnectdirect.internal.common.xml.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Abstract base class for XStream XML converters.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractConverter<T, C> implements Converter {

    private final Class<T> type;
    private final @Nullable Class<C> contextClass;

    @SuppressWarnings("unchecked")
    protected AbstractConverter() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            this.type = (Class<T>) typeArguments[0];
            this.contextClass = typeArguments.length > 1 && typeArguments[1] instanceof Class<?> clazz
                    && !Void.class.equals(clazz) ? (Class<C>) clazz : null;
        } else {
            throw new IllegalStateException("AbstractConverter must be extended with type arguments");
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean canConvert(@Nullable Class type) {
        return this.type.equals(type);
    }

    @Override
    public final void marshal(@Nullable Object o, @Nullable HierarchicalStreamWriter hierarchicalStreamWriter,
            @Nullable MarshallingContext marshallingContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final @Nullable Object unmarshal(@Nullable HierarchicalStreamReader reader,
            @Nullable UnmarshallingContext context) {
        if (reader != null) {
            var ctxClass = contextClass;
            if (context != null && ctxClass != null) {
                return process(reader, ctxClass.cast(context.get(ctxClass.getName())));
            } else {
                return process(reader, null);
            }
        }
        return null;
    }

    protected abstract T process(HierarchicalStreamReader reader, @Nullable C contextObject);

    protected Integer mapHexId(String hexIdString) {
        return mapHexId(hexIdString, 0);
    }

    protected Integer mapHexId(String hexIdString, Integer defaultValue) {
        try {
            return Integer.parseInt(hexIdString, 16);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    protected @Nullable Integer mapHexIdNullable(@Nullable String hexIdString) {
        if (hexIdString == null) {
            return null;
        }

        try {
            return Integer.parseInt(hexIdString, 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected @Nullable Integer mapIntegerNullable(@Nullable String integer) {
        if (integer == null) {
            return null;
        }

        try {
            return Integer.parseInt(integer);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected boolean mapBoolean(@Nullable String bool, boolean defaultValue) {
        if (bool == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(bool);
    }

    protected @Nullable Number mapNumberNullable(@Nullable String doubleOrInteger) {
        if (doubleOrInteger == null) {
            return null;
        }

        try {
            if (!doubleOrInteger.contains(".")) {
                return Integer.parseInt(doubleOrInteger);
            }
            return Double.parseDouble(doubleOrInteger);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
