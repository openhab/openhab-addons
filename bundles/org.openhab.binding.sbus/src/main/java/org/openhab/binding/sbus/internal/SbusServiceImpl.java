/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.SbusException;
import ro.ciprianpascu.sbus.facade.SbusAdapter;
import ro.ciprianpascu.sbus.msg.SbusRequest;
import ro.ciprianpascu.sbus.msg.SbusResponse;
import ro.ciprianpascu.sbus.net.SbusMessageListener;

/**
 * The {@link SbusServiceImpl} implements the SbusService interface by delegating to SbusAdapter.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusServiceImpl implements SbusService {

    private final Logger logger = LoggerFactory.getLogger(SbusServiceImpl.class);
    private @Nullable SbusAdapter adapter;

    @Override
    public void initialize(String host, int port, int timeout) throws Exception {
        try {
            adapter = new SbusAdapter(host, port, timeout);
            logger.debug("SBUS adapter initialized for {}:{} with timeout {}ms", host, port, timeout);
        } catch (SbusException e) {
            throw new IllegalStateException("Failed to initialize SBUS adapter: " + e.getMessage(), e);
        }
    }

    @Override
    public SbusResponse executeTransaction(SbusRequest request) throws Exception {
        SbusAdapter sbusAdapter = getAdapter();
        try {
            return sbusAdapter.executeTransaction(request);
        } catch (SbusException e) {
            throw new IllegalStateException("SBUS transaction failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void addMessageListener(SbusMessageListener listener) throws Exception {
        SbusAdapter sbusAdapter = getAdapter();
        sbusAdapter.addMessageListener(listener);
    }

    @Override
    public void removeMessageListener(SbusMessageListener listener) {
        if (this.adapter != null) {
            adapter.removeMessageListener(listener);
        }
    }

    @Override
    public void close() {
        final SbusAdapter adapter = this.adapter;
        if (adapter != null) {
            adapter.close();
            this.adapter = null;
        }
    }

    private SbusAdapter getAdapter() throws Exception {
        SbusAdapter sbusAdapter = adapter;
        if (sbusAdapter == null) {
            throw new IllegalStateException("SBUS adapter not initialized. Call initialize() first.");
        }
        return sbusAdapter;
    }
}
