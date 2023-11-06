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
package org.openhab.binding.powermax.internal.state;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Base class for extensible state objects
 *
 * @author Ron Isaacson - Initial contribution
 */
@NonNullByDefault
public abstract class PowermaxStateContainer {

    protected final TimeZoneProvider timeZoneProvider;
    protected List<Value<?>> values;

    public abstract class Value<T> {
        protected @Nullable T value;
        protected final String channel;

        public Value(PowermaxStateContainer parent, String channel) {
            this.channel = channel;
            this.value = null;

            parent.getValues().add(this);
        }

        public @Nullable T getValue() {
            return value;
        }

        public void setValue(@Nullable T value) {
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        public void setValueUnsafe(@Nullable Object value) {
            this.value = (T) value;
        }

        public String getChannel() {
            return channel;
        }

        public abstract State getState();
    }

    public class DynamicValue<T> extends Value<T> {
        Supplier<@Nullable T> valueFunction;
        Supplier<State> stateFunction;

        public DynamicValue(PowermaxStateContainer parent, String channel, Supplier<@Nullable T> valueFunction,
                Supplier<State> stateFunction) {
            super(parent, channel);
            this.valueFunction = valueFunction;
            this.stateFunction = stateFunction;
        }

        // Note: setValue() is still valid, but the saved value will be ignored

        @Override
        public @Nullable T getValue() {
            return valueFunction.get();
        }

        @Override
        public State getState() {
            return stateFunction.get();
        }
    }

    public class BooleanValue extends Value<Boolean> {
        State trueState;
        State falseState;

        public BooleanValue(PowermaxStateContainer parent, String channel, State trueState, State falseState) {
            super(parent, channel);
            this.trueState = trueState;
            this.falseState = falseState;
        }

        public BooleanValue(PowermaxStateContainer parent, String channel) {
            this(parent, channel, OnOffType.ON, OnOffType.OFF);
        }

        @Override
        public State getState() {
            Boolean val = value;
            return val == null ? UnDefType.NULL : (val ? trueState : falseState);
        }
    }

    public class StringValue extends Value<String> {
        public StringValue(PowermaxStateContainer parent, String channel) {
            super(parent, channel);
        }

        @Override
        public State getState() {
            String val = value;
            return val == null ? UnDefType.NULL : new StringType(val);
        }
    }

    public class DateTimeValue extends Value<Long> {
        public DateTimeValue(PowermaxStateContainer parent, String channel) {
            super(parent, channel);
        }

        @Override
        public State getState() {
            Long val = value;
            if (val == null) {
                return UnDefType.NULL;
            }
            ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(val), timeZoneProvider.getTimeZone());
            return new DateTimeType(zoned);
        }
    }

    protected PowermaxStateContainer(TimeZoneProvider timeZoneProvider) {
        this.timeZoneProvider = timeZoneProvider;
        this.values = new ArrayList<>();
    }

    public List<Value<?>> getValues() {
        return values;
    }
}
