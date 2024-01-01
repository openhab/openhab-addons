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

import java.util.Collections;
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

/**
 * Service to query an ELV gateway device.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class ELVGatewayQueryService extends GatewayQueryService {

    private final FineOffsetDataParser fineOffsetDataParser;

    private final ConversionContext conversionContext;

    public ELVGatewayQueryService(FineOffsetGatewayConfiguration config,
            @Nullable ThingStatusListener thingStatusListener, ConversionContext conversionContext) {
        super(config, thingStatusListener);
        this.fineOffsetDataParser = new FineOffsetDataParser(Protocol.ELV);
        this.conversionContext = conversionContext;
    }

    @Override
    public @Nullable String getFirmwareVersion() {
        Command command = Command.CMD_READ_FIRMWARE_VERSION;
        var data = executeCommand(command.name(), command.getPayloadAlternative(), bytes -> true);
        if (null != data) {
            return fineOffsetDataParser.getFirmwareVersion(data);
        }
        return null;
    }

    @Override
    public Map<SensorGatewayBinding, SensorDevice> getRegisteredSensors() {
        // not supported by ELV device
        return Collections.emptyMap();
    }

    @Override
    public @Nullable SystemInfo fetchSystemInfo() {
        // not supported by ELV device
        return null;
    }

    @Override
    public List<MeasuredValue> getMeasuredValues() {
        Command command = Command.CMD_WS980_LIVEDATA;
        // since this request has 2 checksums we shortcut it here and provide the concrete payload directly
        byte[] payload = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0x0b, (byte) 0x00, (byte) 0x06, (byte) 0x04,
                (byte) 0x04, (byte) 0x19 };
        byte[] data = executeCommand(command.name(), payload, command::isResponseValid);
        if (data == null) {
            return Collections.emptyList();
        }
        DebugDetails debugDetails = new DebugDetails(data, Command.CMD_WS980_LIVEDATA, Protocol.ELV);
        List<MeasuredValue> measuredValues = fineOffsetDataParser.getMeasuredValues(data, conversionContext,
                debugDetails);
        logger.trace("{}", debugDetails);
        return measuredValues;
    }
}
