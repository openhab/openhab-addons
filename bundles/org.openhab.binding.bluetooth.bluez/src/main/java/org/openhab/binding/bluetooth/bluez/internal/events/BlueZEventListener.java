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
package org.openhab.binding.bluetooth.bluez.internal.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is the listener interface for BlueZEvents.
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public interface BlueZEventListener {

    public void onDBusBlueZEvent(BlueZEvent event);

    public default void onDiscoveringChanged(AdapterDiscoveringChangedEvent event) {
        onDBusBlueZEvent(event);
    }

    public default void onPoweredChange(AdapterPoweredChangedEvent event) {
        onDBusBlueZEvent(event);
    }

    public default void onRssiUpdate(RssiEvent event) {
        onDBusBlueZEvent(event);
    }

    public default void onTxPowerUpdate(TXPowerEvent event) {
        onDBusBlueZEvent(event);
    }

    public default void onCharacteristicNotify(CharacteristicUpdateEvent event) {
        onDBusBlueZEvent(event);
    }

    public default void onManufacturerDataUpdate(ManufacturerDataEvent event) {
        onDBusBlueZEvent(event);
    }

    public default void onServiceDataUpdate(ServiceDataEvent event) {
        onDBusBlueZEvent(event);
    }

    public default void onConnectedStatusUpdate(ConnectedEvent event) {
        onDBusBlueZEvent(event);
    }

    public default void onNameUpdate(NameEvent event) {
        onDBusBlueZEvent(event);
    }

    public default void onServicesResolved(ServicesResolvedEvent event) {
        onDBusBlueZEvent(event);
    }
}
