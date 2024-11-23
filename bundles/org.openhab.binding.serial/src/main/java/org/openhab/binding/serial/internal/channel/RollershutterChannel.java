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
package org.openhab.binding.serial.internal.channel;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;

/**
 * The {@link RollershutterChannel} channel provides mappings for the UP, DOWN and STOP commands
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class RollershutterChannel extends DeviceChannel {

    public RollershutterChannel(final ChannelConfig config) {
        super(config);
    }

    @Override
    public Optional<String> mapCommand(final Command command) {
        String data;

        final String upValue = config.upValue;
        final String downValue = config.downValue;
        final String stopValue = config.stopValue;

        if (command instanceof UpDownType) {
            if (upValue != null && UpDownType.UP.equals(command)) {
                data = upValue;
            } else if (downValue != null && UpDownType.DOWN.equals(command)) {
                data = downValue;
            } else {
                data = command.toFullString();
            }
        } else if (command instanceof StopMoveType) {
            if (stopValue != null && StopMoveType.STOP.equals(command)) {
                data = stopValue;
            } else {
                data = command.toFullString();
            }
        } else {
            data = formatCommand(command);
        }

        final Optional<String> result = transformCommand(data);

        logger.debug("Mapped command is '{}'", result.orElse(null));

        return result;
    }
}
