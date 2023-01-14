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
package org.openhab.binding.serial.internal.channel;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.serial.internal.transform.ValueTransformationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

/**
 * The {@link SwitchChannel} channel provides mappings for the ON and OFF commands
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class SwitchChannel extends DeviceChannel {

    public SwitchChannel(final ValueTransformationProvider valueTransformationProvider, final ChannelConfig config) {
        super(valueTransformationProvider, config);
    }

    @Override
    public Optional<String> mapCommand(final Command command) {
        String data;

        final String onValue = config.onValue;
        final String offValue = config.offValue;

        if (onValue != null && OnOffType.ON.equals(command)) {
            data = onValue;
        } else if (offValue != null && OnOffType.OFF.equals(command)) {
            data = offValue;
        } else {
            data = command.toFullString();
        }

        final Optional<String> result = transformCommand(data);

        logger.debug("Mapped command is '{}'", result.orElse(null));

        return result;
    }
}
