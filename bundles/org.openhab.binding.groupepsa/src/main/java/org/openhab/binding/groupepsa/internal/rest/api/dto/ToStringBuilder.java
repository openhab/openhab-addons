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
package org.openhab.binding.groupepsa.internal.rest.api.dto;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
class ToStringBuilder implements Appendable, CharSequence {
    protected StringBuilder stringBuilder = new StringBuilder();

    ToStringBuilder(Object obj) {
    }

    @Override
    public int length() {
        return stringBuilder.length();
    }

    @Override
    public char charAt(int index) {
        return stringBuilder.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return stringBuilder.subSequence(start, end);
    }

    @Override
    public ToStringBuilder append(@Nullable CharSequence csq) throws IOException {
        if (stringBuilder.length() != 0) {
            stringBuilder.append(", ");
        }
        stringBuilder.append(csq);
        return this;
    }

    @Override
    public ToStringBuilder append(@Nullable CharSequence csq, int start, int end) throws IOException {
        if (stringBuilder.length() != 0) {
            stringBuilder.append(", ");
        }
        stringBuilder.append(csq, start, end);
        return this;
    }

    @Override
    public ToStringBuilder append(char c) throws IOException {
        if (stringBuilder.length() != 0) {
            stringBuilder.append(", ");
        }
        stringBuilder.append(c);
        return this;
    }

    public ToStringBuilder append(Object key, @Nullable Object value) {
        if (stringBuilder.length() != 0) {
            stringBuilder.append(", ");
        }
        stringBuilder.append(key.toString() + ": " + ((value == null) ? "(null)" : value.toString()));
        return this;
    }

    @Override
    public String toString() {
        return "{ " + stringBuilder.toString() + " }";
    }
}
