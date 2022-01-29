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
package org.openhab.binding.boschshc.internal.devices.windowcontact;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_CONTACT;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.shuttercontact.ShutterContactService;
import org.openhab.binding.boschshc.internal.services.shuttercontact.ShutterContactState;
import org.openhab.binding.boschshc.internal.services.shuttercontact.dto.ShutterContactServiceState;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * Detects open windows and doors.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
public class WindowContactHandler extends BoschSHCHandler {

    public WindowContactHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        this.createService(ShutterContactService::new, this::updateChannels, List.of(CHANNEL_CONTACT));
    }

    private void updateChannels(ShutterContactServiceState state) {
        State contact = state.value == ShutterContactState.CLOSED ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        updateState(CHANNEL_CONTACT, contact);
    }
}
