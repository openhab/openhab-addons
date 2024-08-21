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
package org.openhab.binding.openthermgateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * The {@link IntDataItem} represents an 8 or 16 bit signed integer.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class IntDataItem extends DataItem {

    public IntDataItem(Msg msg, ByteType byteType, String subject) {
        super(msg, byteType, subject, null);
    }

    @Override
    public State createState(Message message) {
        return new DecimalType(message.getInt(super.getByteType()));
    }
}
