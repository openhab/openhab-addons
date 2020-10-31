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
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;

/**
 * The {@link RollershutterChannel} channel provides mappings for the UP, DOWN and STOP commands
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class RollershutterChannel extends DeviceChannel {

    public RollershutterChannel(final BundleContext bundleContext, final ChannelConfig config) {
        super(bundleContext, config);
    }

    @Override
    public @Nullable String mapCommand(final Command command) {
        String data = null;

        if (command instanceof UpDownType) {
            if (config.up != null && UpDownType.UP.equals(command)) {
                data = config.up;
            } else if (config.down != null && UpDownType.DOWN.equals(command)) {
                data = config.down;
            }
        } else if (command instanceof StopMoveType) {
            if (config.stop != null && StopMoveType.STOP.equals(command)) {
                data = config.stop;
            }
        } else {
            data = super.mapCommand(command);
        }

        logger.debug("Mapped command is '{}'", data);

        return data;
    }
}
