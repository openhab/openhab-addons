/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * An Utility class to handle {@link ByteEnumWrapper} instances
 *
 * @author Martin van Wingerden - Simplify some code in the RFXCOM binding
 */
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
}
