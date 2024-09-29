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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.State;

/**
 * The {@link DataItem} represents the base dataitem.
 *
 * @author Arjen Korevaar - Initial contribution
 */

@NonNullByDefault
public abstract class DataItem {
    private Msg msg;
    private ByteType byteType;
    private String subject;
    private @Nullable CodeType codeType;

    public Msg getMsg() {
        return msg;
    }

    public ByteType getByteType() {
        return byteType;
    }

    public String getSubject() {
        return subject;
    }

    public @Nullable CodeType getCodeType() {
        return codeType;
    }

    public DataItem(Msg msg, ByteType byteType, String subject, @Nullable CodeType codeType) {
        this.msg = msg;
        this.byteType = byteType;
        this.subject = subject;
        this.codeType = codeType;
    }

    public boolean hasValidCodeType(Message message) {
        // Used to bind a dataitem to a specific TBRA code
        @Nullable
        CodeType code = this.getCodeType();

        return (code == null || code == message.getCodeType());
    }

    /**
     * @param message unused in this default implementation
     * @return the channel id
     */
    public String getChannelId(Message message) {
        // Default implementation
        return subject;
    }

    public abstract State createState(Message message);
}
