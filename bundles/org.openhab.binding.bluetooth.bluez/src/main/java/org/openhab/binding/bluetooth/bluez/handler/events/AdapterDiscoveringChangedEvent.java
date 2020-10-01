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
package org.openhab.binding.bluetooth.bluez.handler.events;

import org.openhab.binding.bluetooth.bluez.handler.DBusBlueZEvent;

/**
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
public class AdapterDiscoveringChangedEvent extends DBusBlueZEvent {

    private boolean discovering;

    public AdapterDiscoveringChangedEvent(String adapter, boolean discovering) {
        super(EventType.ADAPTER_DISCOVERING_CHANGED, adapter);
        this.discovering = discovering;
    }

    public boolean isDiscovering() {
        return discovering;
    }

}
