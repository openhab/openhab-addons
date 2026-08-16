/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.connectors.ConnectorBase;

import io.reactivex.FlowableEmitter;

/**
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class MockMeterReaderConnector extends ConnectorBase<Object> {

    private final boolean applyRetry;
    private final Supplier<Object> readNextSupplier;
    private final Runnable emissionFinished;

    protected MockMeterReaderConnector(String portName, boolean applyRetry, Supplier<Object> readNextSupplier,
            Runnable emissionFinished) {
        super(portName);
        this.applyRetry = applyRetry;
        this.readNextSupplier = readNextSupplier;
        this.emissionFinished = emissionFinished;
    }

    @Override
    public void openConnection() throws IOException {
    }

    @Override
    public void closeConnection() {
    }

    @Override
    protected Object readNext(byte @Nullable [] initMessage) throws IOException {
        try {
            return readNextSupplier.get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException cause) {
                throw cause;
            }
            throw e;
        }
    }

    @Override
    protected void emitValues(byte @Nullable [] initMessage, FlowableEmitter<@Nullable Object> emitter)
            throws IOException {
        try {
            super.emitValues(initMessage, emitter);
        } finally {
            emissionFinished.run();
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
