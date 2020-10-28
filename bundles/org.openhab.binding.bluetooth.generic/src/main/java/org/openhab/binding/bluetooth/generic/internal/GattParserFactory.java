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
package org.openhab.binding.bluetooth.generic.internal;

import org.osgi.service.component.annotations.Component;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParserFactory;

/**
 * The GattChannelHandler handles the mapping of channels to bluetooth gatt characteristics.
 *
 * @author Connor Petty - Initial contribution
 */
@Component(service = GattParserFactory.class)
public class GattParserFactory {

    public BluetoothGattParser getParser() {
        return BluetoothGattParserFactory.getDefault();
    }
}
