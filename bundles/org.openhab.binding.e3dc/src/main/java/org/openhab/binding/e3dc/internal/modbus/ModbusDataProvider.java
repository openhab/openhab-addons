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
package org.openhab.binding.e3dc.internal.modbus;

import java.util.ArrayList;

import org.openhab.binding.e3dc.internal.modbus.Data.DataType;

/**
 * The {@link ModbusDataProvider} Base class caring for listeners
 *
 * @author Bernd Weymann - Initial contribution
 */
public abstract class ModbusDataProvider {
    private final ArrayList<DataListener> listeners = new ArrayList<DataListener>();

    public void addDataListener(DataListener l) {
        listeners.add(l);
    }

    public void removeDataListener(DataListener l) {
        listeners.remove(l);
    }

    public abstract Data getData(DataType dataType);

    protected void informAllListeners() {
        listeners.forEach(l -> {
            l.dataAvailable(this);
        });
    }
}
