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

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.NODE_BATTERY;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link PirHandler} is responsible for handling everything associated to
 * any Freebox Home PIR motion detection thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PirHandler extends HomeNodeHandler {

    public PirHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected State getChannelState(HomeManager homeManager, String channelId, EndpointState state) {
        String value = state.value();
        if (value != null) {
            switch (channelId) {
                case NODE_BATTERY:
                    return DecimalType.valueOf(value);
            }
        }
        return UnDefType.NULL;
    }
}
