/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EmbyClientSocket implements the low level communication to Emby through
 * websocket. Usually this communication is done through port 9090
 *
 * @author Zachary Christiansen - Initial contribution
 *
 */
public class EmbyDeviceEncoder {

    private final Logger logger = LoggerFactory.getLogger(EmbyDeviceEncoder.class);

    public String encodeDeviceID(String deviceID) {
        return deviceID.replaceAll("[^A-Za-z0-9]", "UYHJKU");
    }
}
