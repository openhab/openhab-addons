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
package org.openhab.binding.rfxcom.internal.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * An Utility class to handle {@link ByteEnumWrapper} instances
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class ByteEnumUtil {
    private ByteEnumUtil() {
        // deliberately empty
    }

    public static <T extends ByteEnumWrapper> T fromByte(Class<T> typeClass, int input)
            throws RFXComUnsupportedValueException {
        for (T enumValue : typeClass.getEnumConstants()) {
            if (enumValue.toByte() == input) {
                return enumValue;
            }
        }

        throw new RFXComUnsupportedValueException(typeClass, input);
    }

    public static <T extends ByteEnumWrapper> T convertSubType(Class<T> typeClass, String subType)
            throws RFXComUnsupportedValueException {
        for (T enumValue : typeClass.getEnumConstants()) {
            if (enumValue.toString().equals(subType)) {
                return enumValue;
            }
        }

        try {
            int byteValue = Integer.parseInt(subType);
            return fromByte(typeClass, byteValue);
        } catch (NumberFormatException e) {
            throw new RFXComUnsupportedValueException(typeClass, subType);
        }
    }

    public static <T extends ByteEnumWrapperWithSupportedSubTypes<?>> T fromByte(Class<T> typeClass, int input,
            Object subType) throws RFXComUnsupportedValueException {
        for (T enumValue : typeClass.getEnumConstants()) {
            if (enumValue.toByte() == input && enumValue.supportedBySubTypes().contains(subType)) {
                return enumValue;
            }
        }

        throw new RFXComUnsupportedValueException(RFXComLighting5Message.Commands.class, input, subType);
    }
}
