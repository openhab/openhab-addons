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
public class BoolIntField extends Field<Boolean> {

    public BoolIntField() {
        super(1);
    }

    @Override
    public int defaultLength() {
        return 1;
    }

    @Override
    public Boolean value(ByteBuffer bytes) {
        return bytes.get() == 1;
    }

    @Override
    public ByteBuffer bytesInternal(Boolean value) {
        return ByteBuffer.allocate(1).put((byte) (value ? 1 : 0));
    }
}
