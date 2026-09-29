/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal.local;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MessageWrapper} wraps command type and message content
 *
 * @author Jan N. Klug - Initial contribution
 * @author Maciej Jarzebowski - Add sub-device (cid) addressing
 */
@NonNullByDefault
public class MessageWrapper<T> {
    public CommandType commandType;
    public T content;

    /**
     * The node id of the sub-device this message is addressed to, or {@code null} if the message is addressed to the
     * device the connection is established with.
     */
    public final @Nullable String cid;

    public MessageWrapper(CommandType commandType, T content) {
        this(commandType, content, null);
    }

    public MessageWrapper(CommandType commandType, T content, @Nullable String cid) {
        this.commandType = commandType;
        this.content = content;
        this.cid = cid;
    }

    @Override
    public String toString() {
        return "MessageWrapper{commandType=" + commandType + ", content='" + content + "', cid='" + cid + "'}";
    }
}
