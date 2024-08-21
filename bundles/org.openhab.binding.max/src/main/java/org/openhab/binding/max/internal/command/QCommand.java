/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link QCommand} Quits the connection to the MAX! Cube.
 *
 * @author Marcel Verpaalen - Initial Contribution
 */
@NonNullByDefault
public class QCommand extends CubeCommand {

    @Override
    public String getCommandString() {
        return "q:" + '\r' + '\n';
    }

    @Override
    public String getReturnStrings() {
        return "";
    }
}
