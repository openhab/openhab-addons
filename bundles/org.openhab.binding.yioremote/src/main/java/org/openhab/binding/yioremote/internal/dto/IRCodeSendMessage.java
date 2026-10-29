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
package org.openhab.binding.yioremote.internal.dto;

import static org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IRCodeSendMessage} asks the YIO Dock to send an IR code.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class IRCodeSendMessage {
    public final String type = MESSAGE_TYPE_DOCK;
    public final String command = COMMAND_IR_SEND;
    public final String code;
    public final String format = IR_CODE_FORMAT_HEX;

    public IRCodeSendMessage(String code) {
        this.code = code;
    }
}
