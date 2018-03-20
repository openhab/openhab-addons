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
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

/**
 * Represents an a list of devices on the Azure IoT Hub
 *
 * @author Niko Tanghe - Initial contribution
 * @author Kai Kreuzer - removed joda-time dependency and cleaned up code
 */

public class AzureDevices {

    private final Logger logger = LoggerFactory.getLogger(AzureDevices.class);

    private Map<String, AzureDevice> map = new HashMap<>();
    private String connectionstring;
    private RegistryManager registryManager;

    public AzureDevices(String connectionstring) throws IOException {
        this.connectionstring = connectionstring;
        this.registryManager = RegistryManager.createFromConnectionString(this.connectionstring);
    }

    public AzureDevice getDevice(String deviceId) throws URISyntaxException, IOException, IotHubException,
            JsonSyntaxException, IllegalArgumentException, NoSuchAlgorithmException {
        AzureDevice device = map.get(deviceId);

        // not created yet or cache expired
        if (device == null || device.getRetrievedAt().plus(Duration.ofMinutes(30)).isBefore(LocalDateTime.now())) {

            // close connection
            if (device != null) {
                device.close();
                registryManager.removeDevice(deviceId);
            }

            // re-create
            device = new AzureDevice(getAzureDevice(deviceId), registryManager);
            map.put(deviceId, device);
            return device;
        }

        return device;
    }

    private Device getAzureDevice(String deviceId) throws IllegalArgumentException, NoSuchAlgorithmException,
            IotHubException, JsonSyntaxException, IOException {
        Device device = Device.createFromId(deviceId, null, null);

        try {
            device = registryManager.addDevice(device);
        } catch (IotHubException iote) {
            // this happens if the device already exists, so let's retrieve it
            device = registryManager.getDevice(deviceId);
        }

        return device;
    }
}
