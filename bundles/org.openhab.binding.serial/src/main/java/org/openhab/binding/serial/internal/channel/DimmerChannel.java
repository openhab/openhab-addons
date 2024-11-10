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
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

/**
 * The {@link DimmerChannel} channel applies a format followed by a transform.
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class DimmerChannel extends SwitchChannel {

    public DimmerChannel(final ChannelConfig config) {
        super(config);
    }

    @Override
    public Optional<String> mapCommand(final Command command) {
        Optional<String> result;

        if (command instanceof OnOffType) {
            result = super.mapCommand(command);
        } else {
            String data;

            final String increaseValue = config.increaseValue;
            final String decreaseValue = config.decreaseValue;

            if (command instanceof IncreaseDecreaseType) {
                if (increaseValue != null && IncreaseDecreaseType.INCREASE.equals(command)) {
                    data = increaseValue;
                } else if (decreaseValue != null && IncreaseDecreaseType.DECREASE.equals(command)) {
                    data = decreaseValue;
                } else {
                    data = command.toFullString();
                }
            } else {
                data = formatCommand(command);
            }

            result = transformCommand(data);

            logger.debug("Mapped command is '{}'", result.orElse(null));
        }

        return result;
    }
}
