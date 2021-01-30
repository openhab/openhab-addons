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
package org.openhab.binding.mikrotik.internal.model;

import static org.openhab.binding.mikrotik.internal.model.RouterosDevice.PROP_ID_KEY;

import java.math.BigInteger;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openhab.binding.mikrotik.internal.util.Converter;

/**
 * The {@link RouterosRegistrationBase} is a base model class for WiFi client models having casting accessors for
 * data that is same for all WiFi client types.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosRegistrationBase {
    protected Map<String, String> propMap;

    public RouterosRegistrationBase(Map<String, String> props) {
        this.propMap = props;
        this.postProcess();
    }

    protected void postProcess() {
        if (propMap.containsKey("bytes")) {
            String[] bytes = propMap.get("bytes").split(",");
            propMap.put("tx-byte", bytes[0]);
            propMap.put("rx-byte", bytes[1]);
        }
        if (propMap.containsKey("packets")) {
            String[] packets = propMap.get("packets").split(",");
            propMap.put("tx-packet", packets[0]);
            propMap.put("rx-packet", packets[1]);
        }
    }

    public String getId() {
        return propMap.get(PROP_ID_KEY);
    }

    public String getName() {
        return propMap.get("name");
    }

    public String getComment() {
        return propMap.get("comment");
    }

    public String getMacAddress() {
        return propMap.get("mac-address");
    }

    public String getSSID() {
        return propMap.get("ssid");
    }

    public String getInterfaceName() {
        return propMap.get("interface");
    }

    public BigInteger getTxBytes() {
        return new BigInteger(propMap.get("tx-byte"));
    }

    public BigInteger getRxBytes() {
        return new BigInteger(propMap.get("rx-byte"));
    }

    public BigInteger getTxPackets() {
        return new BigInteger(propMap.get("tx-packet"));
    }

    public BigInteger getRxPackets() {
        return new BigInteger(propMap.get("rx-packet"));
    }

    public String getUptime() {
        return propMap.get("uptime");
    }

    public @Nullable DateTime getUptimeStart() {
        if (propMap.containsKey("uptime")) {
            Period uptime = Converter.fromRouterosPeriod(getUptime());
            return DateTime.now().minus(uptime);
        }
        return null;
    }
}
