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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link KeyfobHandler} is responsible for handling everything associated to
 * any Freebox Home keyfob thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class KeyfobHandler extends HomeNodeHandler {

    public KeyfobHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected State getChannelState(HomeManager homeManager, String channelId, EndpointState state) {
        String value = state.value();
        if (value != null) {
            switch (channelId) {
                case KEYFOB_ENABLE:
                    return OnOffType.from(state.asBoolean());
                case NODE_BATTERY:
                    return DecimalType.valueOf(value);
            }
        }
        return UnDefType.NULL;
    }

    @Override
    protected boolean executeSlotCommand(HomeManager homeManager, String channelId, Command command,
            Configuration config, int intValue) throws FreeboxException {
        if (KEYFOB_ENABLE.equals(channelId) && command instanceof OnOffType onOff) {
            return getManager(HomeManager.class).putCommand(getClientId(), intValue, onOff);
        }
        return false;
    }
}
