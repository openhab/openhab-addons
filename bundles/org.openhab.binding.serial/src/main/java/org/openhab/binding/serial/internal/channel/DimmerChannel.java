/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.serial.internal.transform.ValueTransformationProvider;
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

    public DimmerChannel(final ValueTransformationProvider valueTransformationProvider, final ChannelConfig config) {
        super(valueTransformationProvider, config);
    }

    @Override
    public @Nullable String mapCommand(final Command command) {
        String data;

        if (command instanceof OnOffType) {
            data = super.mapCommand(command);
        } else {
            if (command instanceof IncreaseDecreaseType) {
                if (config.increaseValue != null && IncreaseDecreaseType.INCREASE.equals(command)) {
                    data = config.increaseValue;
                } else if (config.decreaseValue != null && IncreaseDecreaseType.DECREASE.equals(command)) {
                    data = config.decreaseValue;
                } else {
                    data = command.toFullString();
                }
            } else {
                data = formatCommand(command);
            }

            data = transformCommand(data);

            logger.debug("Mapped command is '{}'", data);
        }

        return data;
    }
}
