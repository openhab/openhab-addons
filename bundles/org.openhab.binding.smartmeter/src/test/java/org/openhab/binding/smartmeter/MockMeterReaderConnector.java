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
package org.openhab.binding.smartmeter;

import java.io.IOException;
import java.util.function.Supplier;

import org.openhab.binding.smartmeter.connectors.ConnectorBase;

/**
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
public class MockMeterReaderConnector extends ConnectorBase<Object> {

    private boolean applyRetry;
    private Supplier<Object> readNextSupplier;

    protected MockMeterReaderConnector(String portName, boolean applyRetry, Supplier<Object> readNextSupplier) {
        super(portName);
        this.applyRetry = applyRetry;
        this.readNextSupplier = readNextSupplier;
    }

    @Override
    public void openConnection() throws IOException {
    }

    @Override
    public void closeConnection() {
    }

    @Override
    protected Object readNext(byte[] initMessage) throws IOException {
        try {
            return readNextSupplier.get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
    }

    @Override
    protected boolean applyRetryHandling() {
        return this.applyRetry;
    }

    @Override
    protected boolean applyPeriod() {
        return true;
    }

    @Override
    protected void retryHook(int retryCount) {
        super.retryHook(retryCount);
    }
}
