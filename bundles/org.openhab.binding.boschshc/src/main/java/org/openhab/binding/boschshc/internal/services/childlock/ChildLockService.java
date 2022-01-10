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
package org.openhab.binding.boschshc.internal.services.childlock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.childlock.dto.ChildLockServiceState;
import org.openhab.binding.boschshc.internal.services.childlock.dto.ChildLockState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

/**
 * Indicates if child lock of device is active.
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class ChildLockService extends BoschSHCService<ChildLockServiceState> {
    public ChildLockService() {
        super("Thermostat", ChildLockServiceState.class);
    }

    @Override
    public ChildLockServiceState handleCommand(Command command) throws BoschSHCException {
        if (command instanceof OnOffType) {
            ChildLockServiceState state = new ChildLockServiceState();
            state.childLock = ChildLockState.valueOf(command.toFullString());
            return state;
        } else {
            return super.handleCommand(command);
        }
    }
}
