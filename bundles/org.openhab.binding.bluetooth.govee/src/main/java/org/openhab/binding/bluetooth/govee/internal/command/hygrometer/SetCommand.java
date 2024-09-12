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
package org.openhab.binding.bluetooth.govee.internal.command.hygrometer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
public abstract class SetCommand extends GoveeCommand {

    @Override
    public byte getCommandType() {
        return WRITE_TYPE;
    }
}
