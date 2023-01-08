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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Tim Buckley - Initial contribution
 */
@NonNullByDefault
public class FloatField extends Field<Float> {

    @Override
    public int defaultLength() {
        return 4;
    }

    @Override
    public Float value(ByteBuffer bytes) {
        return bytes.getFloat();
    }

    @Override
    protected ByteBuffer bytesInternal(Float value) {
        return ByteBuffer.allocate(4).putFloat(value);
    }
}
