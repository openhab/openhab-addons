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
package org.openhab.binding.insteon.internal.transport.message;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.utils.BinaryUtils;

/**
 * Definition (layout) of an Insteon message. Says which bytes go where.
 * For more info, see the public Insteon Developer's Guide, 2nd edition,
 * and the Insteon Modem Developer's Guide.
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class MsgDefinition {
    private final byte[] data;
    private final int headerLength;
    private final Direction direction;
    private final Map<String, Field> fields;

    public MsgDefinition(byte[] data, int headerLength, Direction direction, Map<String, Field> fields) {
        this.data = data;
        this.headerLength = headerLength;
        this.direction = direction;
        this.fields = fields;
    }

    public byte[] getData() {
        return data;
    }

    public int getLength() {
        return data.length;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public Direction getDirection() {
        return direction;
    }

    public Field getField(String name) throws FieldException {
        Field field = fields.get(name);
        if (field == null) {
            throw new FieldException("field " + name + " not found");
        }
        return field;
    }

    public List<Field> getFields() {
        return fields.values().stream().toList();
    }

    public boolean containsField(String name) {
        return fields.containsKey(name);
    }

    public byte getByte(String name) throws FieldException {
        return getField(name).getByte(data);
    }

    public byte getCommand() {
        try {
            return getByte("Cmd");
        } catch (FieldException e) {
            return (byte) 0xFF;
        }
    }

    public boolean isExtended() {
        try {
            return BinaryUtils.isBitSet(getByte("messageFlags"), 4);
        } catch (FieldException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        String s = direction + ":";
        for (Field field : getFields()) {
            s += field.toString(data) + "|";
        }
        return s;
    }
}
