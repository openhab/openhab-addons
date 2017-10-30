/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.io.azureiothub;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

/**
 * Represents an a list of devices on the azure iot hub
 *
 * @author Niko Tanghe
 */

public class AzureDevices {

    private Logger logger = LoggerFactory.getLogger(AzureDevices.class);

    private Map<String, AzureDevice> map = new HashMap<String, AzureDevice>();
    private String connectionstring;
    private RegistryManager registryManager;

    public AzureDevices(String connectionstring) throws Exception {
        this.connectionstring = connectionstring;
        this.registryManager = RegistryManager.createFromConnectionString(this.connectionstring);
    }

    public AzureDevice getDevice(String deviceId) throws Exception {
        AzureDevice device = map.get(deviceId);

        // not created yet or cache expired
        if (device == null
                || Minutes.minutesBetween(device.RetrievedAt, DateTime.now()).isGreaterThan(Minutes.minutes(30))) {

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

    private Device getAzureDevice(String deviceId) throws Exception {
        Device device = Device.createFromId(deviceId, null, null);

        try {
            device = registryManager.addDevice(device);
        } catch (IotHubException iote) {
            device = registryManager.getDevice(deviceId);
        } catch (Exception ex) {
            logger.error("failed to add device", ex);
        }

        return device;
    }
}
