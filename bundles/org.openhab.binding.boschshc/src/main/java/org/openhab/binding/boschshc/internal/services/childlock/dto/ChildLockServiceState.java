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
package org.openhab.binding.boschshc.internal.services.childlock.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.core.library.types.OnOffType;

/**
 * State for {@link ChildLockService} to activate and deactivate the child lock
 * of a device.
 * 
 * @author Christian Oeing - Initial contribution
 */
public class ChildLockServiceState extends BoschSHCServiceState {
    public ChildLockServiceState() {
        super("childLockState");
    }

    public ChildLockState childLock;

    public OnOffType getActiveState() {
        return OnOffType.from(this.childLock.toString());
    }
}
