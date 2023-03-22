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
package org.openhab.binding.herzborg.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;

/**
 * The {@link BusHandler} is a handy base class, implementing data communication with Herzborg devices.
 *
 * @author Pavel Fedin - Initial contribution
 */
@NonNullByDefault
public abstract class BusHandler extends BaseBridgeHandler {
    protected Bus bus;

    public BusHandler(Bridge bridge, Bus bus) {
        super(bridge);
        this.bus = bus;
    }

    public Bus getBus() {
        return bus;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do here, but we have to implement it
    }
}
