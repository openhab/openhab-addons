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
package org.openhab.binding.satel.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Command class for command that clear troubles memory.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class ClearTroublesCommand extends ControlCommand {

    public static final byte COMMAND_CODE = (byte) 0x8b;

    /**
     * Creates new command class instance.
     *
     * @param userCode code of the user on behalf the control is made
     */
    public ClearTroublesCommand(String userCode) {
        super(COMMAND_CODE, userCodeToBytes(userCode));
    }
}
