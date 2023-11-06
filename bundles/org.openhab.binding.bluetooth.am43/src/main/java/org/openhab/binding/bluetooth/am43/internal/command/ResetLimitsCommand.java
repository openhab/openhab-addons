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
package org.openhab.binding.bluetooth.am43.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SetPositionCommand} sets the position of the motor, ranging from 0 to 100.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class ResetLimitsCommand extends AM43Command {

    private static final byte COMMAND = (byte) 0x22;

    public ResetLimitsCommand() {
        super(COMMAND, (byte) 0, (byte) 0, (byte) 1);
    }
}
