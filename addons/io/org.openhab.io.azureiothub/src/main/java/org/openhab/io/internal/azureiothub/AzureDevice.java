/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.internal.azureiothub;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;

/**
 * Represents an device on the Azure IoT Hub
 *
 * @author Niko Tanghe - Initial contribution
 * @author Kai Kreuzer - Cleaned up code
 */
public class AzureDevice {

    private static final IotHubClientProtocol PROTOCOL = IotHubClientProtocol.MQTT;

    private final Logger logger = LoggerFactory.getLogger(AzureDevice.class);

    private final DeviceClient client;
    private LocalDateTime retrievedAt;

    private Device device;

    public AzureDevice(Device azureDevice, RegistryManager registryManager) throws URISyntaxException, IOException {
        device = azureDevice;
        retrievedAt = LocalDateTime.now();

        String connectionstring = registryManager.getDeviceConnectionString(azureDevice);
        client = new DeviceClient(connectionstring, PROTOCOL);
        client.open();
        logger.debug("device {} - state {}", azureDevice.getDeviceId(), azureDevice.getConnectionState());
    }

    public LocalDateTime getRetrievedAt() {
        return retrievedAt;
    }

    public Device getDevice() {
        return device;
    }

    public void sendMessage(Message msg, AzureEventCallback callback, Object lockobj) {
        try {
            // keep connection open,
            // according to inline comments, calling open on an already open connection does nothing.
            client.open();
        } catch (IOException e) {
            logger.warn("Failed to connect to Azure IoT Hub device", e);
        }
        client.sendEventAsync(msg, callback, lockobj);
    }

    public void close() {
        if (client != null) {
            try {
                client.closeNow();
            } catch (IOException e) {
                logger.warn("Failed to close connection to Azure device", e);
            }
        }
    }
}
