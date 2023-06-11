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
package org.openhab.binding.max.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link N_CubeCommand} starts the inclusion mode for new MAX! devices.
 *
 * @author Marcel Verpaalen - Initial Contribution
 */
@NonNullByDefault
public class NCommand extends CubeCommand {
    // Example n:003c = start inclusion, timeout 003c = 60 sec

    // Timeout for inclusion mode in seconds
    private static final int DEFAULT_TIMEOUT = 60;

    @Override
    public String getCommandString() {
        return "n:" + String.format("%02x", DEFAULT_TIMEOUT) + '\r' + '\n';
    }

    @Override
    public String getReturnStrings() {
        return "N:";
    }
}
