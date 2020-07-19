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
package org.openhab.binding.e3dc.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.modbus.Data;
import org.openhab.binding.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.e3dc.internal.modbus.DataListener;
import org.openhab.binding.e3dc.internal.modbus.ModbusDataProvider;
import org.openhab.binding.e3dc.util.DataConverterTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataConverterTest} Test data conversions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DataListenerMock implements DataListener {
    private final Logger logger = LoggerFactory.getLogger(DataListenerMock.class);
    private @Nullable Data currentData;
    private int callCounter = 0;
    private DataType listenerType;

    public DataListenerMock(DataType t) {
        listenerType = t;
    }

    @Override
    public void dataAvailable(ModbusDataProvider provider) {
        callCounter++;
        currentData = provider.getData(listenerType);
        logger.info("Callback {} Data {}", callCounter, currentData);
    }

    public @Nullable Data getCurrentData() {
        return currentData;
    }

    public int getCallCounter() {
        return callCounter;
    }
}
