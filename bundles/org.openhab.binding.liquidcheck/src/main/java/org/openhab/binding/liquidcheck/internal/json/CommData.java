/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.liquidcheck.internal.json;

import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.PROPERTY_HOSTNAME;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.PROPERTY_IP;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.PROPERTY_NAME;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.PROPERTY_SECURITY_CODE;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.PROPERTY_SSID;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;

/**
 * The {@link CommData} is used for serializing and deserializing of JSONs.
 * It contains the complete communication data with header and payload or context.
 *
 * @author Marcel Goerentz - Initial contribution
 */

@NonNullByDefault
public class CommData {

    public Header header = new Header();
    public Payload payload = new Payload();
    public Context context = new Context();

    public Map<String, String> createPropertyMap() {
        Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, payload.device.firmware);
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, payload.device.hardware);
        properties.put(PROPERTY_NAME, payload.device.name);
        properties.put(Thing.PROPERTY_VENDOR, payload.device.manufacturer);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, payload.device.uuid);
        properties.put(PROPERTY_SECURITY_CODE, payload.device.security.code);
        properties.put(PROPERTY_IP, payload.wifi.station.ip);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, payload.wifi.station.mac);
        properties.put(PROPERTY_SSID, payload.wifi.accessPoint.ssid);
        return properties;
    }

    public Map<String, Object> createPropertyMap(boolean isHostname) {
        Map<String, Object> properties = new HashMap<>(createPropertyMap());
        if (isHostname) {
            properties.put(PROPERTY_HOSTNAME, payload.wifi.station.hostname);
        } else {
            properties.put(PROPERTY_HOSTNAME, payload.wifi.station.ip);
        }
        return properties;
    }
}
