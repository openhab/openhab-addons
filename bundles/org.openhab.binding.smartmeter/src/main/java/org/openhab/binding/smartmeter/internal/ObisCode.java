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
package org.openhab.binding.smartmeter.internal;

import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;

/**
 * Represents an OBIS code.
 *
 * @see <a href="https://de.wikipedia.org/wiki/OBIS-Kennzahlen">https://de.wikipedia.org/wiki/OBIS-Kennzahlen</a> for
 *      more information
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class ObisCode {

    public static final String OBIS_PATTERN = "((?<A>[0-9]{1,3})-(?<B>[0-9]{1,3}):)?(?<C>[0-9]{1,3}).(?<D>[0-9]{1,3}).(?<E>[0-9]{1,3})(\\*(?<F>[0-9][0-9]{1,3}))?";

    private static Pattern obisPattern = Pattern.compile(OBIS_PATTERN);
    @Nullable
    private Byte a, b, f;
    private Byte c, d, e;

    private ObisCode(@Nullable Byte a, @Nullable Byte b, Byte c, Byte d, Byte e, @Nullable Byte f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    /**
     * Gets an {@link ObisCode} from a String. It must follow the pattern {@value #OBIS_PATTERN}
     *
     * @param obis The obis as String.
     * @return The new Obis code. Can not be null.
     * @throws IllegalArgumentException If the <code>obis</code> has not the right format.
     */
    public static ObisCode from(String obis) throws IllegalArgumentException {
        try {
            Matcher matcher = obisPattern.matcher(obis);
            if (matcher.find()) {
                String a = matcher.group("A");
                String b = matcher.group("B");
                String c = matcher.group("C");
                String d = matcher.group("D");
                String e = matcher.group("E");
                String f = matcher.group("F");
                return new ObisCode(a != null && !a.isEmpty() ? (byte) (0xFF & Integer.valueOf(a)) : null,
                        b != null && !b.isEmpty() ? (byte) (0xFF & Integer.valueOf(b)) : null,
                        (byte) (0xFF & Integer.valueOf(c)), (byte) (0xFF & Integer.valueOf(d)),
                        (byte) (0xFF & Integer.valueOf(e)),
                        f != null && !f.isEmpty() ? (byte) (0xFF & Integer.valueOf(f)) : null);
            }
            throw new IllegalArgumentException(obis + " is not correctly formated.");
        } catch (Exception e) {
            throw new IllegalArgumentException(obis + " is not correctly formated.", e);
        }
    }

    /**
     * Gets the OBIS as a String.
     * 
     * @return the obis as string.
     */
    public String asDecimalString() {
        try (Formatter format = new Formatter()) {
            format.format(SmartMeterBindingConstants.OBIS_FORMAT, a != null ? a & 0xFF : 0, b != null ? b & 0xFF : 0,
                    c & 0xFF, d & 0xFF, e & 0xFF, f != null ? f & 0xFF : 0);
            return format.toString();
        }
    }

    public @Nullable Byte getAGroup() {
        return a;
    }

    public @Nullable Byte getBGroup() {
        return b;
    }

    public @Nullable Byte getCGroup() {
        return c;
    }

    public @Nullable Byte getDGroup() {
        return d;
    }

    public @Nullable Byte getEGroup() {
        return e;
    }

    public @Nullable Byte getFGroup() {
        return f;
    }

    @Override
    public String toString() {
        return asDecimalString();
    }

    public boolean matches(@Nullable Byte a, @Nullable Byte b, Byte c, Byte d, Byte e, @Nullable Byte f) {
        return (this.a == null || a == null || this.a.equals(a)) && (this.b == null || b == null || this.b.equals(b))
                && this.c.equals(c) && this.d.equals(d) && this.e.equals(e)
                && (this.f == null || f == null || this.f.equals(f));
    }

    public boolean matches(Byte c, Byte d, Byte e) {
        return matches(null, null, c, d, e, null);
    }
}
