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
package org.openhab.binding.bluetooth.dbusbluez.handler.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.dbusbluez.handler.DBusBlueZEvent;

/**
 *
 * @author Benjamin Lafois
 *
 */
@NonNullByDefault
public class AdapterPoweredChangedEvent extends DBusBlueZEvent {

    private boolean powered;

    public AdapterPoweredChangedEvent(String adapter, boolean powered) {
        super(EventType.ADAPTER_POWERED_CHANGED, adapter);
        this.powered = powered;
    }

    public boolean isPowered() {
        return powered;
    }

}
