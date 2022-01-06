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
package org.openhab.binding.velux.internal.things;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;

/**
 * <B>Velux</B> product characteristics: Velocity.
 * <P>
 * See <a href=
 * "https://velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api.pdf#page=45">KLF200
 * Velocity parameter</a>
 * <P>
 * Methods in handle this type of information:
 * <UL>
 * <LI>{@link #getVelocity()} to retrieve the value of the characteristic.</LI>
 * <LI>{@link #get(int)} to convert a value into the characteristic.</LI>
 * <LI>{@link #getByName(String)} to convert a name into the characteristic.</LI>
 * <LI>{@link #dump} to retrieve a human-readable description of all values.</LI>
 * </UL>
 *
 * @see VeluxKLFAPI
 *
 * @author Guenther Schreiner - initial contribution.
 */
@NonNullByDefault
public enum VeluxProductVelocity {
    DEFAULT((short) 0, "default"),
    SILENT((short) 1, "silent"),
    FAST((short) 2, "fast"),
    VELOCITY_NOT_AVAILABLE((short) 255, ""),
    UNDEFTYPE((short) 0xffff, VeluxBindingConstants.UNKNOWN);

    // Class internal

    private short velocity;
    private String velocityName;

    // Reverse-lookup map for getting a VeluxProductVelocity from a value.
    private static final Map<Short, VeluxProductVelocity> LOOKUPTYPEID2ENUM = Stream.of(VeluxProductVelocity.values())
            .collect(Collectors.toMap(VeluxProductVelocity::getVelocity, Function.identity()));

    // Constructor

    private VeluxProductVelocity(short velocity, String velocityName) {
        this.velocity = velocity;
        this.velocityName = velocityName;
    }

    // Class access methods

    public short getVelocity() {
        return velocity;
    }

    public static VeluxProductVelocity get(short velocity) {
        return LOOKUPTYPEID2ENUM.getOrDefault(velocity, VeluxProductVelocity.UNDEFTYPE);
    }

    public static VeluxProductVelocity getByName(String velocityName) {
        for (VeluxProductVelocity enumItem : VeluxProductVelocity.values()) {
            if (enumItem.velocityName.equals(velocityName)) {
                return enumItem;
            }
        }
        return VeluxProductVelocity.UNDEFTYPE;
    }

    public static String dump() {
        StringBuilder sb = new StringBuilder();
        for (VeluxProductVelocity typeId : VeluxProductVelocity.values()) {
            sb.append(typeId).append(VeluxBindingConstants.OUTPUT_VALUE_SEPARATOR);
        }
        if (sb.lastIndexOf(VeluxBindingConstants.OUTPUT_VALUE_SEPARATOR) > 0) {
            sb.deleteCharAt(sb.lastIndexOf(VeluxBindingConstants.OUTPUT_VALUE_SEPARATOR));
        }
        return sb.toString();
    }
}
