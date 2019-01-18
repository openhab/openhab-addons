/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

/**
 * The {@link LCommand} request a status update for MAX! devices.
 *
 * @author Marcel Verpaalen - Initial Contribution
 */
public class LCommand extends CubeCommand {

    @Override
    public String getCommandString() {
        return "l:" + '\r' + '\n';
    }

    @Override
    public String getReturnStrings() {
        return "L:";
    }

}
