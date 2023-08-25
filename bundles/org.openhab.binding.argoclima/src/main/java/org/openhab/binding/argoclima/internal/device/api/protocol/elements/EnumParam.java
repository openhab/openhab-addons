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
package org.openhab.binding.argoclima.internal.device.api.protocol.elements;

import java.util.EnumSet;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.device.api.protocol.IArgoSettingProvider;
import org.openhab.binding.argoclima.internal.device.api.types.IArgoApiEnum;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enum-type of a param (supports mapping to/from enumerations implementing {@link IArgoApiEnum}
 *
 * @implNote Some enums (ex. timer type) may require unique handling of updates, hence this class'es implementation of
 *           {@link #handleCommandInternalEx(Command)} is not final
 *
 * @author Mateusz Bronk - Initial contribution
 *
 * @param <E> The type of underlying enum
 */
@NonNullByDefault
public class EnumParam<E extends Enum<E> & IArgoApiEnum> extends ArgoApiElementBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumParam.class);
    private Optional<E> currentValue;
    private final Class<E> cls;

    /**
     * C-tor
     *
     * @param settingsProvider the settings provider (getting device state as well as schedule configuration)
     * @param cls The type of underlying Enum (implementing {@link IArgoApiEnum} for mapping to/from integer values)
     */
    public EnumParam(IArgoSettingProvider settingsProvider, Class<E> cls) {
        super(settingsProvider);
        this.cls = cls;
        this.currentValue = Optional.empty();
    }

    /**
     * Gets the raw enum value from {@link Type} ({@link Command} or {@link State}) which are themselves strings
     *
     * @see #valueToState(Optional) for a reverse conversion
     *
     * @param <E> The type of underlying enum - implementing {@link IArgoApiEnum}
     * @param value Value to convert
     * @param cls The class of underlying Enum (implementing {@link IArgoApiEnum} for mapping to/from integer values)
     * @return Converted value (or empty, on conversion failure)
     */
    public static <E extends Enum<E> & IArgoApiEnum> Optional<E> fromType(Type value, Class<E> cls) {
        if (value instanceof StringType stringTypeCommand) {
            String newValue = stringTypeCommand.toFullString();
            try {
                return Optional.of(Enum.valueOf(cls, newValue));
            } catch (IllegalArgumentException ex) {
                LOGGER.debug("Failed to convert value: {} to enum. {}", value, ex.getMessage());
                return Optional.empty();
            }
        }
        return Optional.empty(); // Not a string Command/State -> ignoring the conversion
    }

    /**
     * Converts enum value to framework-compatible {@link State}
     *
     * @see {@link #fromType(Type, Class)} for a reverse conversion
     * @param <E> The type of underlying enum - implementing {@link IArgoApiEnum}
     * @param value The value to convert (wrapped into an optional)
     * @return Converted value. {@link UnDefType.UNDEF} if n/a
     */
    private static <E extends Enum<E> & IArgoApiEnum> State valueToState(Optional<E> value) {
        if (value.isEmpty()) {
            return UnDefType.UNDEF;
        }
        return new StringType(value.orElseThrow().toString());
    }

    @Override
    protected void updateFromApiResponseInternal(String responseValue) {
        strToInt(responseValue).ifPresent(raw -> {
            this.currentValue = this.fromInt(raw);
        });
    }

    @Override
    public State toState() {
        return valueToState(currentValue);
    }

    @Override
    public String toString() {
        if (currentValue.isEmpty()) {
            return "???";
        }
        return currentValue.get().toString();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default behavior - may be overridden for specialized enums
     */
    @Override
    protected HandleCommandResult handleCommandInternalEx(Command command) {
        if (!(command instanceof StringType)) {
            return HandleCommandResult.rejected(); // Unsupported command type
        }

        var requestedValue = fromType(command, cls);
        if (requestedValue.isEmpty()) {
            return HandleCommandResult.rejected(); // Value not valid for this enum
        }

        E val = requestedValue.orElseThrow(); // boilerplate, guaranteed to always succeed
        if (currentValue.map(cv -> (cv.compareTo(val) == 0)).orElse(false)) {
            return HandleCommandResult.rejected(); // Current value is the same as requested - nothing to do
        }

        this.currentValue = requestedValue; // We allow it!
        return HandleCommandResult.accepted(Integer.toString(val.getIntValue()), valueToState(requestedValue));
    }

    /**
     * Convert from int value to this enum
     *
     * @param value Int value (must match the underlying enum's {@link IArgoApiEnum#getIntValue()}
     * @return Converted value or empty if no match
     */
    private Optional<E> fromInt(int value) {
        return EnumSet.allOf(this.cls).stream().filter(p -> p.getIntValue() == value).findFirst();
    }
}
