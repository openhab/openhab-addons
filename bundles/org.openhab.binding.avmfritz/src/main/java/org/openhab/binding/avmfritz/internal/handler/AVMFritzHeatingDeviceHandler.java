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
package org.openhab.binding.avmfritz.internal.handler;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;

/**
 * Handler for a FRITZ! heating device. Handles commands, which are sent to one of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class AVMFritzHeatingDeviceHandler extends DeviceHandler implements AVMFritzHeatingActionsHandler {

    public AVMFritzHeatingDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void setBoostMode(long duration) {
        handleAction(MODE_BOOST, duration);
    }

    @Override
    public void setWindowOpenMode(long duration) {
        handleAction(MODE_WINDOW_OPEN, duration);
    }
}
