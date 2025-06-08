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
import org.openhab.binding.sbus.handler.SbusService;
import org.openhab.binding.sbus.handler.TemperatureUnit;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import ro.ciprianpascu.sbus.facade.SbusAdapter;

/**
 * The {@link SbusServiceImpl} implements the SbusService interface by delegating to SbusAdapter.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@Component(service = SbusService.class)
@NonNullByDefault
public class SbusServiceImpl implements SbusService {
    private @Nullable SbusAdapter adapter;

    @Activate
    public SbusServiceImpl() {
        // Service is activated but adapter is initialized later with connection parameters
    }

    /**
     * Initializes the underlying SbusAdapter with connection parameters.
     *
     * @param host the host address of the Sbus device
     * @param port the port number to use
     * @throws Exception if initialization fails
     */
    @Override
    public void initialize(String host, int port) throws Exception {
        this.adapter = new SbusAdapter(host, port);
    }

    @Deactivate
    public void deactivate() {
        close();
    }

    @Override
    public float[] readTemperatures(int subnetId, int id, TemperatureUnit temperatureUnit) throws Exception {
        final SbusAdapter adapter = this.adapter;
        if (adapter == null) {
            throw new IllegalStateException("SbusAdapter not initialized");
        }
        return adapter.readTemperatures(subnetId, id, temperatureUnit.getValue());
    }

    @Override
    public int[] readRgbw(int subnetId, int id, int channelNumber) throws Exception {
        final SbusAdapter adapter = this.adapter;
        if (adapter == null) {
            throw new IllegalStateException("SbusAdapter not initialized");
        }
        return adapter.readRgbw(subnetId, id, channelNumber);
    }

    @Override
    public int[] readStatusChannels(int subnetId, int id) throws Exception {
        final SbusAdapter adapter = this.adapter;
        if (adapter == null) {
            throw new IllegalStateException("SbusAdapter not initialized");
        }
        return adapter.readExecutionStatusChannels(subnetId, id);
    }

    @Override
    public boolean[] readContactStatusChannels(int subnetId, int id) throws Exception {
        final SbusAdapter adapter = this.adapter;
        if (adapter == null) {
            throw new IllegalStateException("SbusAdapter not initialized");
        }
        return adapter.readContactStatusChannels(subnetId, id);
    }

    @Override
    public void writeRgbw(int subnetId, int id, int channelNumber, int[] color) throws Exception {
        final SbusAdapter adapter = this.adapter;
        if (adapter == null) {
            throw new IllegalStateException("SbusAdapter not initialized");
        }
        adapter.writeRgbw(subnetId, id, channelNumber, color);
    }

    @Override
    public void writeSingleChannel(int subnetId, int id, int channelNumber, int value, int timer) throws Exception {
        final SbusAdapter adapter = this.adapter;
        if (adapter == null) {
            throw new IllegalStateException("SbusAdapter not initialized");
        }
        adapter.writeSingleExecutionChannel(subnetId, id, channelNumber, value, timer);
    }

    @Override
    public void close() {
        final SbusAdapter adapter = this.adapter;
        if (adapter != null) {
            adapter.close();
            this.adapter = null;
        }
    }
}
