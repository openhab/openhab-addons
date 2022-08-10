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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Command;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.ConversionContext;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Protocol;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.SensorGatewayBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SystemInfo;
import org.openhab.binding.fineoffsetweatherstation.internal.handler.ThingStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to query the gateway device.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class FineOffsetGatewayQueryService extends GatewayQueryService {
    private final Logger logger = LoggerFactory.getLogger(FineOffsetGatewayQueryService.class);

    private final FineOffsetDataParser fineOffsetDataParser;

    private final ConversionContext conversionContext;

    public FineOffsetGatewayQueryService(FineOffsetGatewayConfiguration config,
            @Nullable ThingStatusListener thingStatusListener, ConversionContext conversionContext) {
        super(config, thingStatusListener);
        this.fineOffsetDataParser = new FineOffsetDataParser(Protocol.DEFAULT);
        this.conversionContext = conversionContext;
    }

    @Override
    public @Nullable String getFirmwareVersion() {
        var data = executeCommand(Command.CMD_READ_FIRMWARE_VERSION);
        if (null != data) {
            return fineOffsetDataParser.getFirmwareVersion(data);
        }
        return null;
    }

    @Override
    public Map<SensorGatewayBinding, SensorDevice> getRegisteredSensors() {
        var data = executeCommand(Command.CMD_READ_SENSOR_ID_NEW);
        if (null == data) {
            return Map.of();
        }
        return fineOffsetDataParser.getRegisteredSensors(data, () -> {
            @Nullable
            SystemInfo systemInfo = fetchSystemInfo();
            if (systemInfo != null) {
                return systemInfo.isUseWh24();
            }
            return null;
        });
    }

    @Override
    public @Nullable SystemInfo fetchSystemInfo() {
        var data = executeCommand(Command.CMD_READ_SSSS);
        if (data == null) {
            logger.debug("Unexpected response to System Info!");
            return null;
        }
        return fineOffsetDataParser.fetchSystemInfo(data);
    }

    @Override
    public List<MeasuredValue> getMeasuredValues() {
        byte[] data = executeCommand(Command.CMD_GW1000_LIVEDATA);
        if (data == null) {
            return Collections.emptyList();
        }
        return fineOffsetDataParser.getMeasuredValues(data, conversionContext);
    }

    protected byte @Nullable [] executeCommand(Command command) {
        return executeCommand(command.name(), command.getPayload(), command::isResponseValid);
    }
}
