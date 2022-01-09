/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class VersionField extends Field<Version> {

    @Override
    public int defaultLength() {
        return 4;
    }

    @Override
    public Version value(ByteBuffer bytes) {
        long value = bytes.getInt() & 0xFFFFFFFFL;
        long major = (value >> 16) & 0xFFL;
        long minor = value & 0xFFL;
        return new Version(major, minor);
    }

    @Override
    public ByteBuffer bytesInternal(Version value) {
        return ByteBuffer.allocate(4).putInt((int) (((value.getMajor() << 16) | value.getMinor()) & 0xFFFFFFFFL));
    }
}
