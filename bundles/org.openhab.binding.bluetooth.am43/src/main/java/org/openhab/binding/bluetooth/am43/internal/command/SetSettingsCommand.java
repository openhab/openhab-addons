/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.bluetooth.am43.internal.data.MotorSettings;

/**
 * The {@link SetSettingsCommand} sets the motor settings in bulk. There isn't a way to set them individually so before
 * you can use this command you need to retrieve the existing commands with a {@link GetAllCommand}
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class SetSettingsCommand extends AM43Command {

    private static final byte COMMAND = (byte) 0x11;

    public SetSettingsCommand(MotorSettings settings) {
        super(COMMAND, createContent(settings));
    }

    private static byte[] createContent(MotorSettings motorSettings) {
        @SuppressWarnings("null")
        int direction = motorSettings.getDirection().toByte();
        @SuppressWarnings("null")
        int operationMode = motorSettings.getOperationMode().toByte();
        int deviceType = motorSettings.getType();
        int deviceLength = motorSettings.getLength();
        int deviceSpeed = motorSettings.getSpeed();
        int deviceDiameter = motorSettings.getDiameter();

        int dataHead = ((direction & 1) << 1) | ((operationMode & 1) << 2) | (deviceType << 4);

        return new byte[] { (byte) dataHead, (byte) deviceSpeed, 0, (byte) ((deviceLength & 0xFF00) >> 8),
                (byte) (deviceLength & 0xFF), (byte) deviceDiameter };
    }
}
