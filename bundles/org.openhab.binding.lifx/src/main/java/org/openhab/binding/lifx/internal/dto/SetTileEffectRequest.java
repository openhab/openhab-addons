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
 * Implementation of SetTileEffect packet
 *
 * @author Pawel Pieczul - Initial contribution
 */
public class SetTileEffectRequest extends Packet {

    public static final int TYPE = 0x2CF;

    // size.from.to....what
    // ------------------------------
    // 1....0....0.....reserved
    // 1....1....1.....reserved
    // 4....2....5.....instance ID
    // 1....6....6.....effect type
    // 4....7....10....speed
    // 8....11...18....duration
    // 4....19...22....reserved
    // 4....23...26....reserved
    // 32...27...58....parameters (8*32 bits)
    // 1....59...59....palette count
    // 128..60...187...palette (16*8 bits)

    private static final Field<Integer> FIELD_RESERVED_0 = new UInt8Field();
    private static final Field<Integer> FIELD_RESERVED_1 = new UInt8Field();
    private static final Field<Long> FIELD_INSTANCE_ID = new UInt32Field().little();
    private static final Field<Integer> FIELD_TYPE = new UInt8Field();
    private static final Field<Long> FIELD_SPEED = new UInt32Field().little();
    private static final Field<Long> FIELD_DURATION = new UInt64Field().little();
    private static final Field<Long> FIELD_RESERVED_19_TO_26 = new UInt32Field().little();
    private static final Field<Long> FIELD_PARAMETER_27_TO_58 = new UInt32Field().little();
    private static final Field<Integer> FIELD_PALETTE_COUNT = new UInt8Field();
    private static final Field<HSBK> FIELD_PALETTE_60_TO_187 = new HSBKField();

    private final Logger logger = LoggerFactory.getLogger(SetTileEffectRequest.class);

    private Integer reserved0 = 0;
    private Integer reserved1 = 0;
    private Long reserved19to22 = 0L;
    private Long reserved23to26 = 0L;
    private Effect effect;

    public SetTileEffectRequest() {
        setTagged(false);
        setAddressable(true);
        setAckRequired(true);
    }

    public SetTileEffectRequest(Effect effect) {
        this();
        this.effect = effect;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 188;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        reserved0 = FIELD_RESERVED_0.value(bytes);
        reserved1 = FIELD_RESERVED_1.value(bytes);
        FIELD_INSTANCE_ID.value(bytes);
        Integer effectType = FIELD_TYPE.value(bytes);
        Long speed = FIELD_SPEED.value(bytes);
        Long duration = FIELD_DURATION.value(bytes);
        reserved19to22 = FIELD_RESERVED_19_TO_26.value(bytes);
        reserved23to26 = FIELD_RESERVED_19_TO_26.value(bytes);
        Long[] parameters = new Long[8];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = FIELD_PARAMETER_27_TO_58.value(bytes);
        }
        Integer paletteCount = FIELD_PALETTE_COUNT.value(bytes);
        HSBK[] palette = new HSBK[paletteCount];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = FIELD_PALETTE_60_TO_187.value(bytes);
        }
        try {
            effect = new Effect(effectType, speed, duration, palette);
        } catch (IllegalArgumentException e) {
            logger.debug("Wrong effect type received: {}", effectType);
            effect = null;
        }
    }

    @Override
    protected ByteBuffer packetBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(packetLength());
        buffer.put(FIELD_RESERVED_0.bytes(reserved0));
        buffer.put(FIELD_RESERVED_1.bytes(reserved1));
        buffer.put(FIELD_INSTANCE_ID.bytes(0L));
        buffer.put(FIELD_TYPE.bytes(effect.getType().intValue()));
        buffer.put(FIELD_SPEED.bytes(effect.getSpeed()));
        buffer.put(FIELD_DURATION.bytes(effect.getDuration()));
        buffer.put(FIELD_RESERVED_19_TO_26.bytes(reserved19to22));
        buffer.put(FIELD_RESERVED_19_TO_26.bytes(reserved23to26));
        for (int i = 0; i < 8; i++) {
            buffer.put(FIELD_PARAMETER_27_TO_58.bytes(0L));
        }
        buffer.put(FIELD_PALETTE_COUNT.bytes(effect.getPalette().length));
        HSBK[] palette = effect.getPalette();
        for (int i = 0; i < palette.length; i++) {
            buffer.put(FIELD_PALETTE_60_TO_187.bytes(palette[i]));
        }
        HSBK hsbkZero = new HSBK(0, 0, 0, 0);
        for (int i = 0; i < 16 - palette.length; i++) {
            buffer.put(FIELD_PALETTE_60_TO_187.bytes(hsbkZero));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("SetTileEffectRequest: type={}, speed={}, duration={}, palette_count={}", effect.getType(),
                    effect.getSpeed(), effect.getDuration(), palette.length);
            for (int i = 0; i < palette.length; i++) {
                logger.debug("SetTileEffectRequest palette[{}] = {}", i, palette[i]);
            }
        }
        return buffer;
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
