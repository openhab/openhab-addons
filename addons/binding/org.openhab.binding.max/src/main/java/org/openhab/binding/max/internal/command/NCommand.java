/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.command;

/**
 * The {@link N_CubeCommand} starts the inclusion mode for new MAX! devices.
 * 
 * @author Marcel Verpaalen - Initial Contribution
 */
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
