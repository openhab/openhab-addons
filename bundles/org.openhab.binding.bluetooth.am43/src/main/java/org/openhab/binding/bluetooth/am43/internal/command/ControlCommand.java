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
package org.openhab.binding.bluetooth.am43.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.am43.internal.data.ControlAction;

/**
 * The {@link ControlCommand} is used for basic UP, DOWN, and STOP commands.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class ControlCommand extends AM43Command {

    private static final byte COMMAND = (byte) 0x0a;

    public ControlCommand(ControlAction action) {
        super(COMMAND, action.getCode());
    }
}
