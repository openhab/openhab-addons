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
package org.openhab.binding.openthermgateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * The {@link TspFhbValueDataItem} represents a transparent slave parameter or fault history buffer value.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class TspFhbValueDataItem extends DataItem {

    public TspFhbValueDataItem(Msg msg, String subject) {
        super(msg, ByteType.BOTH, subject, null);
    }

    @Override
    public String getChannelId(Message message) {
        // With TSP or FHB values, the index is HIGHBYTE, the value is LOWBYTE
        int index = message.getUInt(ByteType.HIGHBYTE);
        return getChannelId(index);
    }

    public String getChannelId(int index) {
        return super.getSubject() + "_" + index;
    }

    public String getLabel(int index) {
        return super.getSubject() + " " + index;
    }

    @Override
    public State createState(Message message) {
        // With TSP or FHB values, the index is HIGHBYTE, the value is LOWBYTE
        // TSP values are treated as Number:Dimensionless
        return new DecimalType(message.getUInt(ByteType.LOWBYTE));
    }
}
