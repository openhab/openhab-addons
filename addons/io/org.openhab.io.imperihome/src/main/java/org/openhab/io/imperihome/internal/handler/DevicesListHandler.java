/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.imperihome.internal.handler;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.openhab.io.imperihome.internal.model.device.AbstractDevice;
import org.openhab.io.imperihome.internal.model.device.DeviceList;
import org.openhab.io.imperihome.internal.processor.DeviceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device list request handler.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class DevicesListHandler {

    private final Logger logger = LoggerFactory.getLogger(DevicesListHandler.class);

    private final DeviceRegistry deviceRegistry;

    public DevicesListHandler(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    public DeviceList handle(HttpServletRequest req) {
        DeviceList response = new DeviceList();

        Collection<AbstractDevice> devices = deviceRegistry.getDevices().values();
        for (AbstractDevice device : devices) {
            device.updateParams();
        }

        response.setDevices(devices);

        logger.debug("Device list response: {}", response);
        return response;
    }

}
