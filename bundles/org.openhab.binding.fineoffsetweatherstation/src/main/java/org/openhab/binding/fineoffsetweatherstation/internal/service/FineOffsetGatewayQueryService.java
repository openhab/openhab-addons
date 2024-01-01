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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Command;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.ConversionContext;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.DebugDetails;
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
    private static final Protocol PROTOCOL = Protocol.DEFAULT;
    private final Logger logger = LoggerFactory.getLogger(FineOffsetGatewayQueryService.class);

    private final FineOffsetDataParser fineOffsetDataParser;

    private final ConversionContext conversionContext;

    public FineOffsetGatewayQueryService(FineOffsetGatewayConfiguration config,
            @Nullable ThingStatusListener thingStatusListener, ConversionContext conversionContext) {
        super(config, thingStatusListener);
        this.fineOffsetDataParser = new FineOffsetDataParser(PROTOCOL);
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
    public Collection<MeasuredValue> getMeasuredValues() {
        Map<String, MeasuredValue> valuePerChannel = new LinkedHashMap<>();

        byte[] data = executeCommand(Command.CMD_GW1000_LIVEDATA);
        if (data != null) {
            DebugDetails debugDetails = new DebugDetails(data, Command.CMD_GW1000_LIVEDATA, PROTOCOL);
            List<MeasuredValue> measuredValues = fineOffsetDataParser.getMeasuredValues(data, conversionContext,
                    debugDetails);
            for (MeasuredValue measuredValue : measuredValues) {
                valuePerChannel.put(measuredValue.getChannelId(), measuredValue);
            }
            logger.trace("{}", debugDetails);
        }

        data = executeCommand(Command.CMD_READ_RAIN);
        if (data != null) {
            DebugDetails debugDetails = new DebugDetails(data, Command.CMD_READ_RAIN, PROTOCOL);
            List<MeasuredValue> measuredRainValues = fineOffsetDataParser.getRainData(data, conversionContext,
                    debugDetails);
            for (MeasuredValue measuredValue : measuredRainValues) {
                valuePerChannel.put(measuredValue.getChannelId(), measuredValue);
            }
            logger.trace("{}", debugDetails);
        }

        return valuePerChannel.values();
    }

    protected byte @Nullable [] executeCommand(Command command) {
        return executeCommand(command.name(), command.getPayload(), command::isResponseValid);
    }
}
