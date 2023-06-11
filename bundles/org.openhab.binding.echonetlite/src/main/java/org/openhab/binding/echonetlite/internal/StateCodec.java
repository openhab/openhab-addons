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
package org.openhab.binding.echonetlite.internal;

import static org.openhab.binding.echonetlite.internal.HexUtil.hex;
import static org.openhab.binding.echonetlite.internal.LangUtil.b;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public interface StateCodec extends StateEncode, StateDecode {

    class OnOffCodec implements StateCodec {
        private final int on;
        private final int off;

        public OnOffCodec(int on, int off) {
            this.on = on;
            this.off = off;
        }

        public State decodeState(final ByteBuffer edt) {
            return b(on) == edt.get() ? OnOffType.ON : OnOffType.OFF;
        }

        public void encodeState(final State state, final ByteBuffer edt) {
            final OnOffType onOff = (OnOffType) state;
            edt.put(onOff == OnOffType.ON ? b(on) : b(off));
        }

        public String itemType() {
            return "Switch";
        }
    }

    enum StandardVersionInformationCodec implements StateDecode {

        INSTANCE;

        public State decodeState(final ByteBuffer edt) {
            final int pdc = edt.remaining();
            if (pdc != 4) {
                return StringType.EMPTY;
            }

            return new StringType("" + (char) edt.get(edt.position() + 2));
        }

        public String itemType() {
            return "String";
        }
    }

    enum HexStringCodec implements StateDecode {

        INSTANCE;

        public State decodeState(final ByteBuffer edt) {
            return new StringType(hex(edt, "", "", "", ""));
        }

        public String itemType() {
            return "String";
        }
    }

    enum OperatingTimeDecode implements StateDecode {
        INSTANCE;

        public State decodeState(final ByteBuffer edt) {
            // Specification isn't explicit about byte order, but seems to be work with testing.
            edt.order(ByteOrder.BIG_ENDIAN);

            final int b0 = edt.get() & 0xFF;
            final long time = edt.getInt() & 0xFFFFFFFFL;

            final TimeUnit timeUnit;
            switch (b0) {
                case 0x42:
                    timeUnit = TimeUnit.MINUTES;
                    break;

                case 0x43:
                    timeUnit = TimeUnit.HOURS;
                    break;

                case 0x44:
                    timeUnit = TimeUnit.DAYS;
                    break;

                case 0x41:
                default:
                    timeUnit = TimeUnit.SECONDS;
                    break;
            }

            return new QuantityType<>(timeUnit.toSeconds(time), Units.SECOND);
        }

        public String itemType() {
            return "Number:Time";
        }
    }

    class Option {
        final String name;
        final int value;
        final StringType state;

        public Option(final String name, final int value) {
            this.name = name;
            this.value = value;
            this.state = new StringType(name);
        }
    }

    class OptionCodec implements StateCodec {

        private final Logger logger = LoggerFactory.getLogger(OptionCodec.class);
        private final Map<String, Option> optionByName = new HashMap<>();
        private final Option[] optionByValue = new Option[256]; // All options values are single bytes on the wire
        private final StringType unknown = new StringType("Unknown");

        public OptionCodec(Option... options) {
            for (Option option : options) {
                optionByName.put(option.name, option);
                optionByValue[option.value] = option;
            }
        }

        public String itemType() {
            return "String";
        }

        public State decodeState(final ByteBuffer edt) {
            final int value = edt.get() & 0xFF;
            final Option option = optionByValue[value];
            return null != option ? option.state : unknown;
        }

        public void encodeState(final State state, final ByteBuffer edt) {
            final Option option = optionByName.get(state.toFullString());
            if (null != option) {
                edt.put(b(option.value));
            } else {
                logger.warn("No option specified for: {}", state);
            }
        }
    }

    enum Decimal8bitCodec implements StateCodec {

        INSTANCE;

        public String itemType() {
            return "Number";
        }

        public State decodeState(final ByteBuffer edt) {
            final int value = edt.get(); // Should expand to typed value (mask excluded)
            return new DecimalType(value);
        }

        public void encodeState(final State state, final ByteBuffer edt) {
            edt.put((byte) (((DecimalType) state).intValue()));
        }
    }

    enum Temperature8bitCodec implements StateCodec {
        INSTANCE;

        public State decodeState(final ByteBuffer edt) {
            final int value = edt.get();
            return new QuantityType<>(value, SIUnits.CELSIUS);
        }

        public String itemType() {
            return "Number:Temperature";
        }

        public void encodeState(final State state, final ByteBuffer edt) {
            final @Nullable QuantityType<?> tempCelsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            edt.put((byte) (Objects.requireNonNull(tempCelsius).intValue()));
        }
    }
}
