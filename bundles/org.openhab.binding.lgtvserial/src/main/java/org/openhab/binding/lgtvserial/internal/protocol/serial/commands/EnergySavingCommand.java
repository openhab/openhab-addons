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
package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommand;

/**
 * This class handles the energy saving J/Q command.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public class EnergySavingCommand extends BaseStringCommand implements LGSerialCommand {

    protected EnergySavingCommand(int setId) {
        super('j', 'q', setId);
    }
}
