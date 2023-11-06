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
 * {@link CubeCommand} is the base class for commands to be send to the MAX! Cube.
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public abstract class CubeCommand {

    /**
     * @return the String to be send to the MAX! Cube
     */
    public abstract String getCommandString();

    /**
     * @return the String expected to be received from the Cube to signify the
     *         end of the message
     */
    public abstract String getReturnStrings();
}
