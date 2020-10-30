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

import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;

/**
 * The {@link RollershutterChannel} channel provides mappings for the UP, DOWN and STOP commands
 *
 * @author Mike Major - Initial contribution
 */
public class RollershutterChannel extends DeviceChannel {

    public RollershutterChannel(final BundleContext bundleContext, final ChannelConfig config) {
        super(bundleContext, config);
    }

    @Override
    public String mapCommand(final Command command) {
        String data = null;

        if (config.up != null && UpDownType.UP.equals(command)) {
            data = config.up;
        } else if (config.down != null && UpDownType.DOWN.equals(command)) {
            data = config.down;
        } else if (config.stop != null && StopMoveType.STOP.equals(command)) {
            data = config.stop;
        }

        logger.debug("Mapped command is '{}'", data);

        return data;
    }
}
