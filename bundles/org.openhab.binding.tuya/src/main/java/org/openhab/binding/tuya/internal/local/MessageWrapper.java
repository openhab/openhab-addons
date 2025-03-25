/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

/**
 * The {@link MessageWrapper} wraps command type and message content
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class MessageWrapper<T> {
    public CommandType commandType;
    public T content;

    public MessageWrapper(CommandType commandType, T content) {
        this.commandType = commandType;
        this.content = content;
    }

    @Override
    public String toString() {
        return "MessageWrapper{commandType=" + commandType + ", content='" + content + "'}";
    }
}
