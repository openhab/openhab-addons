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
package org.openhab.binding.bluetooth.bluez.internal.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public class ServicesResolvedEvent extends BlueZEvent {

    private boolean resolved;

    public ServicesResolvedEvent(String dbusPath, boolean resolved) {
        super(dbusPath, EventType.SERVICES_RESOLVED);
        this.resolved = resolved;
    }

    public boolean isResolved() {
        return resolved;
    }
}
