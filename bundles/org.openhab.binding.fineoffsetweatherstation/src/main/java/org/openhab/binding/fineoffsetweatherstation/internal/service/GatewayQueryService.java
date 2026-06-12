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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.SensorGatewayBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SystemInfo;
import org.openhab.binding.fineoffsetweatherstation.internal.handler.ThingStatusListener;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface defining the API for querying a gateway device.
 * <p>
 * The actual transport (TCP binary protocol or Ecowitt HTTP API) is provided by the concrete
 * implementations; all of them yield the same domain objects so the handler stays transport-agnostic.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public abstract class GatewayQueryService implements AutoCloseable {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final FineOffsetGatewayConfiguration config;

    @Nullable
    private final ThingStatusListener thingStatusListener;

    public GatewayQueryService(FineOffsetGatewayConfiguration config,
            @Nullable ThingStatusListener thingStatusListener) {
        this.config = config;
        this.thingStatusListener = thingStatusListener;
    }

    @Nullable
    public abstract String getFirmwareVersion();

    public abstract Map<SensorGatewayBinding, SensorDevice> getRegisteredSensors();

    @Nullable
    public abstract SystemInfo fetchSystemInfo();

    public abstract Collection<MeasuredValue> getMeasuredValues();

    protected void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        @Nullable
        ThingStatusListener listener = thingStatusListener;
        if (listener != null) {
            listener.updateStatus(status, statusDetail, description);
        }
    }

    @Override
    public void close() throws IOException {
    }
}
