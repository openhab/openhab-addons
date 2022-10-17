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
package org.openhab.binding.saicismart.internal.asn1.v3_0;

import org.bn.coders.IASN1PreparedElement;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.saicismart.internal.asn1.AbstractMessage;

/**
 *
 * @author Markus Heberling - Initial contribution
 */
public class Message<E extends IASN1PreparedElement>
        extends AbstractMessage<MP_DispatcherHeader, MP_DispatcherBody, E> {

    private final byte[] reserved;

    public Message(MP_DispatcherHeader header, byte[] reserved, MP_DispatcherBody body, @Nullable E applicationData) {
        super(header, body, applicationData);
        this.reserved = reserved;
    }

    public byte[] getReserved() {
        return reserved;
    }
}
