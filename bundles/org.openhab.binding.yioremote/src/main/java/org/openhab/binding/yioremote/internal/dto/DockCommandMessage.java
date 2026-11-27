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

import static org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.MESSAGE_TYPE_DOCK;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DockCommandMessage} sends a simple command (ping, IR receiver on/off) to the YIO Dock.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class DockCommandMessage {
    public final String type = MESSAGE_TYPE_DOCK;
    public final String command;

    public DockCommandMessage(String command) {
        this.command = command;
    }
}
