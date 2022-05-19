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

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.Utils;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Command;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.ConversionContext;
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
 * Service to query the gateway device.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class FineOffsetGatewayQueryService implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(FineOffsetGatewayQueryService.class);

    private @Nullable Socket socket;
    private final FineOffsetGatewayConfiguration config;
    private final ThingStatusListener thingStatusListener;
    private final FineOffsetDataParser fineOffsetDataParser;

    private final ConversionContext conversionContext;

    public FineOffsetGatewayQueryService(FineOffsetGatewayConfiguration config, ThingStatusListener thingStatusListener,
            ConversionContext conversionContext) {
        this.config = config;
        this.thingStatusListener = thingStatusListener;
        this.fineOffsetDataParser = new FineOffsetDataParser();
        this.conversionContext = conversionContext;
    }

    public @Nullable String getFirmwareVersion() {
        var data = executeCommand(Command.CMD_READ_FIRMWARE_VERSION);
        if (null != data) {
            return fineOffsetDataParser.getFirmwareVersion(data);
        }
        return null;
    }

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

    public @Nullable SystemInfo fetchSystemInfo() {
        var data = executeCommand(Command.CMD_READ_SSSS);
        if (data == null) {
            logger.debug("Unexpected response to System Info!");
            return null;
        }
        return fineOffsetDataParser.fetchSystemInfo(data);
    }

    public List<MeasuredValue> getLiveData() {
        byte[] data = executeCommand(Command.CMD_GW1000_LIVEDATA);
        if (data == null) {
            return Collections.emptyList();
        }
        return fineOffsetDataParser.getLiveData(data, conversionContext);
    }

    private synchronized byte @Nullable [] executeCommand(Command command) {
        byte[] buffer = new byte[2028];
        int bytesRead;
        byte[] request = command.getPayload();

        try {
            Socket socket = getConnection();
            if (socket == null) {
                return null;
            }
            InputStream in = socket.getInputStream();
            socket.getOutputStream().write(request);
            if ((bytesRead = in.read(buffer)) == -1) {
                return null;
            }
            if (!command.isResponseValid(buffer)) {
                if (bytesRead > 0) {
                    logger.debug("executeCommand({}), invalid response: {}", command,
                            Utils.toHexString(buffer, bytesRead, ""));
                } else {
                    logger.debug("executeCommand({}): no response", command);
                }
                return null;
            }

        } catch (IOException ex) {
            thingStatusListener.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    ex.getMessage());
            try {
                close();
            } catch (IOException e) {
                // ignored
            }
            return null;
        } catch (Exception ex) {
            logger.warn("executeCommand({})", command, ex);
            return null;
        }

        var data = Arrays.copyOfRange(buffer, 0, bytesRead);
        logger.trace("executeCommand({}): received: {}", command, Utils.toHexString(data, data.length, ""));
        return data;
    }

    private synchronized @Nullable Socket getConnection() {
        Socket socket = this.socket;
        if (socket == null) {
            try {
                socket = new Socket(config.ip, config.port);
                socket.setSoTimeout(5000);
                this.socket = socket;
                thingStatusListener.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
            } catch (IOException e) {
                thingStatusListener.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        e.getMessage());
            }
        }
        return socket;
    }

    @Override
    public void close() throws IOException {
        Socket socket = this.socket;
        this.socket = null;
        if (socket != null) {
            socket.close();
        }
    }
}
