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
package org.openhab.binding.bluetooth.am43.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ControlAction} list possible controls actions that can be sent through
 * {@link org.openhab.binding.bluetooth.am43.internal.command.ControlCommand}
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public enum ControlAction {
    CLOSE(0xee),
    OPEN(0xdd),
    STOP(0xcc);

    private byte code;

    private ControlAction(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return code;
    }
}
