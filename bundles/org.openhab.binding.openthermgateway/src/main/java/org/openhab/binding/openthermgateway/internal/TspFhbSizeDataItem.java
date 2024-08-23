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
 * The {@link DataItem} represents a transparent slave parameter or fault history buffer size.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class TspFhbSizeDataItem extends DataItem {
    private int valueId;

    public int getValueId() {
        return valueId;
    }

    public TspFhbSizeDataItem(Msg msg, ByteType byteType, int valueId, String subject) {
        super(msg, byteType, subject, null);

        this.valueId = valueId;
    }

    @Override
    public State createState(Message message) {
        return new DecimalType(message.getUInt(super.getByteType()));
    }
}
