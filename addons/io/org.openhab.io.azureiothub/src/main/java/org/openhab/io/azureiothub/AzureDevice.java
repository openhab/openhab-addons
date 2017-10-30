/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.io.azureiothub;

import java.io.IOException;
import java.net.URISyntaxException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;

/**
 * Represents an device on the azure iot hub
 *
 * @author Niko Tanghe
 */

public class AzureDevice {

    private Logger logger = LoggerFactory.getLogger(AzureDevice.class);

    private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    private DeviceClient Client;

    public AzureDevice(Device azureDevice, RegistryManager registryManager) {
        Device = azureDevice;
        RetrievedAt = DateTime.now();

        try {
            String connectionstring = registryManager.getDeviceConnectionString(azureDevice);
            Client = new DeviceClient(connectionstring, protocol);
            Client.open();
            logger.info("Device {} - state {}", azureDevice.getDeviceId(), azureDevice.getConnectionState().toString());
        } catch (URISyntaxException e) {
            logger.error("Invalid connectionstring", e);
        } catch (Exception e) {
            logger.error("Failed to connect to azure Iot hub device", e);
        }
    }

    public void SendMessage(Message msg, AzureEventCallback callback, Object lockobj) {

        try {
            // keep connection open,
            // according to inline comments, calling open on an already open connection does nothing.
            Client.open();
        } catch (IOException e) {
            logger.error("Failed to connect to azure Iot hub device", e);
        }

        Client.sendEventAsync(msg, callback, lockobj);
    }

    public DateTime RetrievedAt;

    public Device Device;

    public void close() {
        if (Client != null) {
            try {
                Client.closeNow();
            } catch (IOException e) {
                logger.error("Failed to close connection to azure device", e);
            }
        }
    }
}
