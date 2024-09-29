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
package org.openhab.binding.lifx.internal.dto;

import java.nio.ByteBuffer;

import org.openhab.binding.lifx.internal.fields.Field;
import org.openhab.binding.lifx.internal.fields.HSBK;
import org.openhab.binding.lifx.internal.fields.HSBKField;
import org.openhab.binding.lifx.internal.fields.UInt32Field;
import org.openhab.binding.lifx.internal.fields.UInt64Field;
import org.openhab.binding.lifx.internal.fields.UInt8Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of StateTileEffect packet
 *
 * @author Pawel Pieczul - Initial contribution
 */
public class StateTileEffectResponse extends Packet {

    public static final int TYPE = 0x2D0;

    // size.from.to....what
    // ------------------------------
    // 1....0....0.....reserved
    // 4....1....4.....instance ID
    // 1....5....5.....effect type
    // 4....6....9.....speed
    // 8....10...17....duration
    // 4....18...21....reserved
    // 4....22...25....reserved
    // 32...26...57....parameters (8*32 bits)
    // 1....58...58....palette count
    // 128..59...186...palette (16*8 bits)

    private static final Field<Integer> FIELD_RESERVED_0 = new UInt8Field();
    private static final Field<Long> FIELD_INSTANCE_ID = new UInt32Field().little();
    private static final Field<Integer> FIELD_TYPE = new UInt8Field();
    private static final Field<Long> FIELD_SPEED = new UInt32Field().little();
    private static final Field<Long> FIELD_DURATION = new UInt64Field().little();
    private static final Field<Long> FIELD_RESERVED_18_TO_25 = new UInt32Field().little();
    private static final Field<Long> FIELD_PARAMETER_26_TO_57 = new UInt32Field().little();
    private static final Field<Integer> FIELD_PALETTE_COUNT = new UInt8Field();
    private static final Field<HSBK> FIELD_PALETTE_59_TO_186 = new HSBKField();

    private Integer reserved0 = 0;
    private Long reserved18to21 = 0L;
    private Long reserved22to25 = 0L;
    private Effect effect;

    private final Logger logger = LoggerFactory.getLogger(StateTileEffectResponse.class);

    public Effect getEffect() {
        return effect;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 187;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        reserved0 = FIELD_RESERVED_0.value(bytes);
        Long instanceId = FIELD_INSTANCE_ID.value(bytes);
        Integer effectType = FIELD_TYPE.value(bytes);
        Long speed = FIELD_SPEED.value(bytes);
        Long duration = FIELD_DURATION.value(bytes);
        reserved18to21 = FIELD_RESERVED_18_TO_25.value(bytes);
        reserved22to25 = FIELD_RESERVED_18_TO_25.value(bytes);
        Long[] parameters = new Long[8];
        for (int i = 0; i < 8; i++) {
            parameters[i] = FIELD_PARAMETER_26_TO_57.value(bytes);
        }
        Integer paletteCount = FIELD_PALETTE_COUNT.value(bytes);
        HSBK[] palette = new HSBK[paletteCount];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = FIELD_PALETTE_59_TO_186.value(bytes);
        }
        try {
            effect = new Effect(effectType, speed, duration, palette);
        } catch (IllegalArgumentException e) {
            logger.debug("Wrong effect type received: {}", effectType);
            effect = null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("StateTileEffectResponse: instanceId={}, type={}, speed={}, duration={}, palette_count={}",
                    instanceId, effectType, speed, duration, paletteCount);
            logger.debug("StateTileEffectResponse parameters=[{}, {}, {}, {}, {}, {}, {}, {}]", parameters[0],
                    parameters[1], parameters[2], parameters[3], parameters[4], parameters[5], parameters[6],
                    parameters[7]);
            for (int i = 0; i < palette.length; i++) {
                logger.debug("StateTileEffectResponse palette[{}] = {}", i, palette[i]);
            }
        }
    }

    @Override
    protected ByteBuffer packetBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(packetLength());
        buffer.put(FIELD_RESERVED_0.bytes(reserved0));
        buffer.put(FIELD_INSTANCE_ID.bytes(0L));
        buffer.put(FIELD_TYPE.bytes(effect.getType().intValue()));
        buffer.put(FIELD_SPEED.bytes(effect.getSpeed()));
        buffer.put(FIELD_DURATION.bytes(effect.getDuration()));
        buffer.put(FIELD_RESERVED_18_TO_25.bytes(reserved18to21));
        buffer.put(FIELD_RESERVED_18_TO_25.bytes(reserved22to25));
        for (int i = 0; i < 8; i++) {
            buffer.put(FIELD_PARAMETER_26_TO_57.bytes(0L));
        }
        HSBK[] palette = effect.getPalette();
        buffer.put(FIELD_PALETTE_COUNT.bytes(palette.length));
        for (int i = 0; i < palette.length; i++) {
            buffer.put(FIELD_PALETTE_59_TO_186.bytes(palette[i]));
        }
        HSBK hsbkZero = new HSBK(0, 0, 0, 0);
        for (int i = 0; i < 16 - palette.length; i++) {
            buffer.put(FIELD_PALETTE_59_TO_186.bytes(hsbkZero));
        }
        return buffer;
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
