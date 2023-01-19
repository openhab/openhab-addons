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

/**
 * This class handles the color temperature X/U command.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public class ColorTemperature2Command extends BasePercentCommand {

    protected ColorTemperature2Command(int setId) {
        super('x', 'u', setId);
    }
}
