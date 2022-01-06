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
package org.openhab.io.imperihome.internal.handler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import org.openhab.io.imperihome.internal.model.device.AbstractDevice;
import org.openhab.io.imperihome.internal.processor.DeviceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device action request handler.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class DeviceActionHandler {

    private final Logger logger = LoggerFactory.getLogger(DeviceActionHandler.class);

    private final DeviceRegistry deviceRegistry;

    public DeviceActionHandler(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    public void handle(HttpServletRequest req, Matcher urlMatcher) {
        String deviceId, action, value;
        deviceId = URLDecoder.decode(urlMatcher.group(1), StandardCharsets.UTF_8);
        action = URLDecoder.decode(urlMatcher.group(2), StandardCharsets.UTF_8);

        if (urlMatcher.group(3) == null) {
            value = null;
        } else {
            value = URLDecoder.decode(urlMatcher.group(3), StandardCharsets.UTF_8);
        }

        logger.debug("Action request for device {}: [{}] [{}]", deviceId, action, value);

        AbstractDevice device = deviceRegistry.getDevice(deviceId);
        if (device == null) {
            logger.warn("Received action request for unknown device: {}", urlMatcher.group(0));
        } else {
            device.performAction(action, value);
        }
    }
}
