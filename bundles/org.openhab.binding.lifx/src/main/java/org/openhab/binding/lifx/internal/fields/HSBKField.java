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
package org.openhab.binding.lifx.internal.fields;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class HSBKField extends Field<HSBK> {

    public static final Field<Integer> FIELD_HUE = new UInt16Field().little();
    public static final Field<Integer> FIELD_SATURATION = new UInt16Field().little();
    public static final Field<Integer> FIELD_BRIGHTNESS = new UInt16Field().little();
    public static final Field<Integer> FIELD_KELVIN = new UInt16Field().little();

    @Override
    public int defaultLength() {
        return 8;
    }

    @Override
    public HSBK value(ByteBuffer bytes) {
        int hue = FIELD_HUE.value(bytes);
        int saturation = FIELD_SATURATION.value(bytes);
        int brightness = FIELD_BRIGHTNESS.value(bytes);
        int kelvin = FIELD_KELVIN.value(bytes);

        return new HSBK(hue, saturation, brightness, kelvin);
    }

    @Override
    protected ByteBuffer bytesInternal(HSBK value) {
        return ByteBuffer.allocate(defaultLength()).put(FIELD_HUE.bytes(value.getHue()))
                .put(FIELD_SATURATION.bytes(value.getSaturation())).put(FIELD_BRIGHTNESS.bytes(value.getBrightness()))
                .put(FIELD_KELVIN.bytes(value.getKelvin()));
    }
}
