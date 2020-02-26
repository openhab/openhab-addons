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

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
final class EnumMapper<E extends @NonNull Enum<E>> {
    private final Logger logger = LoggerFactory.getLogger(EnumMapper.class);

    private final Class<E> enumClass;
    private final Map<E, String> enumToStringMap;
    private final Map<String, E> stringToEnumMap;
    private final E unexpectedValue;

    private EnumMapper(
            final Class<E> enumClass,
            final Map<E, String> enumToStringMap,
            final Map<String, E> stringToEnumMap,
            final E unexpectedValue
    ) {
        // Trust the builder has created everything correctly.
        this.enumClass = enumClass;
        this.enumToStringMap = enumToStringMap;
        this.stringToEnumMap = stringToEnumMap;
        this.unexpectedValue = unexpectedValue;
    }

    public @Nullable String getStringForEnum(final E enumValue) {
        Objects.requireNonNull(enumValue);

        return this.enumToStringMap.get(enumValue);
    }

    public E getEnumForString(final String stringValue) {
        Objects.requireNonNull(stringValue);

        final @Nullable E enumValue = this.stringToEnumMap.get(stringValue);

        if (enumValue != null) {
            return enumValue;
        } else {
            this.logger.trace(
                    "Given unexpected string for enum {}: \"{}\"",
                    this.enumClass.toString(),
                    stringValue
            );

            return this.unexpectedValue;
        }
    }

    public static <E extends @NonNull Enum<E>> Builder<E> builder(final Class<E> enumClass) {
        return new Builder<>(enumClass);
    }

    public static class Builder<E extends @NonNull Enum<E>> {
        private final Class<E> enumClass;

        private final Map<E, String> enumToStringMap;
        private final Map<String, E> stringToEnumMap;
        private final Set<E> ignoredValues;

        private @Nullable E unexpectedValue;

        public Builder(final Class<E> enumClass) {
            this.enumClass = Objects.requireNonNull(enumClass);

            this.enumToStringMap = new EnumMap<>(enumClass);
            this.stringToEnumMap = new HashMap<>();
            this.ignoredValues = EnumSet.noneOf(enumClass);
        }

        public Builder<E> setUnexpectedValue(final E enumValue) {
            Objects.requireNonNull(enumValue);

            checkNotUsed(enumValue);

            this.unexpectedValue = enumValue;

            return this;
        }

        public Builder<E> ignore(final E enumValue) {
            Objects.requireNonNull(enumValue);

            checkNotUsed(enumValue);

            this.ignoredValues.add(enumValue);

            return this;
        }

        public Builder<E> add(final E enumValue, final String stringValue) {
            Objects.requireNonNull(enumValue);
            Objects.requireNonNull(stringValue);

            checkNotUsed(enumValue);

            if (this.stringToEnumMap.containsKey(stringValue)) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The string value \"{0}\" has already been added to the map.",
                        stringValue
                ));
            }

            this.enumToStringMap.put(enumValue, stringValue);
            this.stringToEnumMap.put(stringValue, enumValue);

            return this;
        }

        public EnumMapper<E> build() {
            final @Nullable E unknownValue = this.unexpectedValue;
            if (unknownValue == null) {
                throw new IllegalStateException("The unknown value has not been set.");
            }

            final EnumSet<E> missingValues = EnumSet.allOf(this.enumClass);
            missingValues.removeAll(this.enumToStringMap.keySet());
            missingValues.removeAll(this.ignoredValues);
            missingValues.remove(unknownValue);

            if (missingValues.size() > 0) {
                throw new IllegalStateException("Expected values have not been added: " + missingValues.toString());
            }

            return new EnumMapper<>(
                    this.enumClass,
                    Collections.unmodifiableMap(this.enumToStringMap),
                    Collections.unmodifiableMap(this.stringToEnumMap),
                    unknownValue
            );
        }

        private void checkNotUsed(final E enumValue) {
            if (this.unexpectedValue == enumValue) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The enum value \"{0}\" has already been used as the unexpected value.",
                        enumValue.toString()
                ));
            }

            if (this.ignoredValues.contains(enumValue)) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The enum value \"{0}\" has already been set as ignored.",
                        enumValue.toString()
                ));
            }

            if (this.enumToStringMap.containsKey(enumValue)) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The enum value \"{0}\" has already been added to the map.",
                        enumValue.toString()
                ));
            }
        }
    }
}
