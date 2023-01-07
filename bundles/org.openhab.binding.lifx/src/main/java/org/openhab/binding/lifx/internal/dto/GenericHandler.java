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
package org.openhab.binding.lifx.internal.dto;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A generic handler that dynamically creates "standard" packet instances.
 *
 * <p>
 * Packet types must have an empty constructor and cannot require any
 * additional logic (other than parsing).
 *
 * @param <T> the packet subtype this handler constructs
 *
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
@NonNullByDefault
public class GenericHandler<T extends Packet> implements PacketHandler<T> {

    private Constructor<T> constructor;

    private boolean typeFound;
    private int type;

    public boolean isTypeFound() {
        return typeFound;
    }

    public int getType() {
        return type;
    }

    public GenericHandler(Class<T> clazz) {
        try {
            constructor = clazz.getConstructor();
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("Packet class cannot be handled by GenericHandler", ex);
        }

        try {
            Field typeField = clazz.getField("TYPE");
            type = (int) typeField.get(null);
            typeFound = true;
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            // silently ignore
            typeFound = false;
        }
    }

    @Override
    public T handle(ByteBuffer buf) {
        try {
            T ret = constructor.newInstance();
            ret.parse(buf);
            return ret;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("Unable to instantiate empty packet", ex);
        }
    }
}
