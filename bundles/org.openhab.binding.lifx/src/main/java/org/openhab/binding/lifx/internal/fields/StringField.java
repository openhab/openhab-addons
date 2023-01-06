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
package org.openhab.binding.lifx.internal.fields;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Tim Buckley - Initial contribution
 */
@NonNullByDefault
public class StringField extends Field<String> {

    public static final Charset CHARSET = StandardCharsets.US_ASCII;

    private Charset charset;

    public StringField() {
        charset = StandardCharsets.US_ASCII;
    }

    public StringField(int length) {
        super(length);

        charset = StandardCharsets.US_ASCII;
    }

    public StringField(int length, Charset charset) {
        super(length);

        this.charset = charset;
    }

    @Override
    public int defaultLength() {
        return 3;
    }

    @Override
    public String value(ByteBuffer bytes) {
        byte[] buf = new byte[length];
        bytes.get(buf);

        ByteBuffer field = ByteBuffer.wrap(buf);

        String ret = charset.decode(field).toString();
        ret = ret.replace("\0", "");

        return ret;
    }

    @Override
    public ByteBuffer bytesInternal(String value) {
        return CHARSET.encode(value);
    }

    public StringField ascii() {
        charset = StandardCharsets.US_ASCII;
        return this;
    }

    public StringField utf8() {
        charset = StandardCharsets.UTF_8;
        return this;
    }
}
