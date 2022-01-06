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
package org.openhab.binding.lifx.internal.dto;

import static org.openhab.binding.lifx.internal.LifxBindingConstants.*;

import java.nio.ByteBuffer;

import org.openhab.binding.lifx.internal.fields.Field;
import org.openhab.binding.lifx.internal.fields.HSBK;
import org.openhab.binding.lifx.internal.fields.HSBKField;
import org.openhab.binding.lifx.internal.fields.UInt32Field;
import org.openhab.binding.lifx.internal.fields.UInt8Field;

/**
 * @author Wouter Born - Initial contribution
 */
public class SetColorZonesRequest extends Packet {

    public static final int TYPE = 0x1F5;

    public static final Field<Integer> FIELD_START_INDEX = new UInt8Field();
    public static final Field<Integer> FIELD_END_INDEX = new UInt8Field();
    public static final HSBKField FIELD_COLOR = new HSBKField();
    public static final Field<Long> FIELD_FADE_TIME = new UInt32Field().little();
    public static final Field<Integer> FIELD_APPLY = new UInt8Field();

    private int startIndex = MIN_ZONE_INDEX;
    private int endIndex = MAX_ZONE_INDEX;
    private HSBK color;
    private long fadeTime;
    private ApplicationRequest apply;

    public HSBK getColor() {
        return color;
    }

    public int getHue() {
        return color.getHue();
    }

    public int getSaturation() {
        return color.getSaturation();
    }

    public int getBrightness() {
        return color.getBrightness();
    }

    public int getKelvin() {
        return color.getKelvin();
    }

    public long getFadeTime() {
        return fadeTime;
    }

    public ApplicationRequest getApply() {
        return apply;
    }

    public SetColorZonesRequest() {
        setTagged(false);
        setAddressable(true);
        setResponseRequired(true);
    }

    public SetColorZonesRequest(HSBK color, long fadeTime, ApplicationRequest apply) {
        this(MIN_ZONE_INDEX, MAX_ZONE_INDEX, color, fadeTime, apply);
    }

    public SetColorZonesRequest(int index, HSBK color, long fadeTime, ApplicationRequest apply) {
        this(index, index, color, fadeTime, apply);
    }

    public SetColorZonesRequest(int startIndex, int endIndex, HSBK color, long fadeTime, ApplicationRequest apply) {
        this();
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.color = color;
        this.fadeTime = fadeTime;
        this.apply = apply;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 15;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        startIndex = FIELD_START_INDEX.value(bytes);
        endIndex = FIELD_END_INDEX.value(bytes);
        color = FIELD_COLOR.value(bytes);
        fadeTime = FIELD_FADE_TIME.value(bytes);
        apply = ApplicationRequest.fromValue(FIELD_APPLY.value(bytes));
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_START_INDEX.bytes(startIndex))
                .put(FIELD_END_INDEX.bytes(endIndex)).put(FIELD_COLOR.bytes(color)).put(FIELD_FADE_TIME.bytes(fadeTime))
                .put(FIELD_APPLY.bytes(apply.getValue()));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
