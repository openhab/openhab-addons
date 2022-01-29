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
package org.openhab.binding.pilight.internal.handler;

import static org.openhab.binding.pilight.internal.PilightBindingConstants.CHANNEL_STATE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.dto.Action;
import org.openhab.binding.pilight.internal.dto.Device;
import org.openhab.binding.pilight.internal.dto.Status;
import org.openhab.binding.pilight.internal.types.PilightContactType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PilightContactHandler} is responsible for handling a pilight contact.
 *
 * @author Stefan Röllin - Initial contribution
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
@NonNullByDefault
public class PilightContactHandler extends PilightBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(PilightContactHandler.class);

    public PilightContactHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateFromStatus(Status status) {
        String state = status.getValues().get("state");
        if (state != null) {
            updateState(CHANNEL_STATE, PilightContactType.valueOf(state.toUpperCase()).toOpenClosedType());
        }
    }

    @Override
    void updateFromConfigDevice(Device device) {
    }

    @Override
    protected @Nullable Action createUpdateCommand(ChannelUID channelUID, Command command) {
        logger.warn("A contact is a read only device");
        return null;
    }
}
