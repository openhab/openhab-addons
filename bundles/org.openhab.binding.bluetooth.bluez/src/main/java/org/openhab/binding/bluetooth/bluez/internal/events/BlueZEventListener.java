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

    void onDBusBlueZEvent(BlueZEvent event);

    default void onDiscoveringChanged(AdapterDiscoveringChangedEvent event) {
        onDBusBlueZEvent(event);
    }

    default void onPoweredChange(AdapterPoweredChangedEvent event) {
        onDBusBlueZEvent(event);
    }

    default void onRssiUpdate(RssiEvent event) {
        onDBusBlueZEvent(event);
    }

    default void onTxPowerUpdate(TXPowerEvent event) {
        onDBusBlueZEvent(event);
    }

    default void onCharacteristicNotify(CharacteristicUpdateEvent event) {
        onDBusBlueZEvent(event);
    }

    default void onManufacturerDataUpdate(ManufacturerDataEvent event) {
        onDBusBlueZEvent(event);
    }

    default void onServiceDataUpdate(ServiceDataEvent event) {
        onDBusBlueZEvent(event);
    }

    default void onConnectedStatusUpdate(ConnectedEvent event) {
        onDBusBlueZEvent(event);
    }

    default void onNameUpdate(NameEvent event) {
        onDBusBlueZEvent(event);
    }

    default void onServicesResolved(ServicesResolvedEvent event) {
        onDBusBlueZEvent(event);
    }
}
