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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * The {@link DataItem} holds the internal OpenTherm message and meta data.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class FlagDataItem extends DataItem {
    private int bitpos;

    public int getBitPos() {
        return bitpos;
    }

    public FlagDataItem(Msg msg, ByteType byteType, int bitpos, String subject) {
        this(msg, byteType, bitpos, subject, null);
    }

    public FlagDataItem(Msg msg, ByteType byteType, int bitpos, String subject, @Nullable CodeType codeType) {
        super(msg, byteType, subject, codeType);

        this.bitpos = bitpos;
    }

    @Override
    public State createState(Message message) {
        return OnOffType.from(message.getBit(super.getByteType(), this.getBitPos()));
    }
}
