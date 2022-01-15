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
package org.openhab.binding.mikrotik.internal.model;

import static org.openhab.binding.mikrotik.internal.model.RouterosDevice.PROP_ID_KEY;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.util.Converter;

/**
 * The {@link RouterosRegistrationBase} is a base model class for WiFi client models having casting accessors for
 * data that is same for all WiFi client types.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosRegistrationBase extends RouterosBaseData {

    public RouterosRegistrationBase(Map<String, String> props) {
        super(props);
        this.postProcess();
    }

    protected void postProcess() {
        if (hasProp("bytes")) {
            String bytesStr = getProp("bytes");
            if (bytesStr != null) {
                String[] bytes = bytesStr.split(",");
                setProp("tx-byte", bytes[0]);
                setProp("rx-byte", bytes[1]);
            }
        }
        if (hasProp("packets")) {
            String packetsStr = getProp("packets");
            if (packetsStr != null) {
                String[] packets = packetsStr.split(",");
                setProp("tx-packet", packets[0]);
                setProp("rx-packet", packets[1]);
            }
        }
    }

    public @Nullable String getId() {
        return getProp(PROP_ID_KEY);
    }

    public @Nullable String getName() {
        return getProp("name");
    }

    public @Nullable String getComment() {
        return getProp("comment");
    }

    public @Nullable String getMacAddress() {
        return getProp("mac-address");
    }

    public @Nullable String getSSID() {
        return getProp("ssid");
    }

    public @Nullable String getInterfaceName() {
        return getProp("interface");
    }

    public @Nullable BigInteger getTxBytes() {
        return getBigIntProp("tx-byte");
    }

    public @Nullable BigInteger getRxBytes() {
        return getBigIntProp("rx-byte");
    }

    public @Nullable BigInteger getTxPackets() {
        return getBigIntProp("tx-packet");
    }

    public @Nullable BigInteger getRxPackets() {
        return getBigIntProp("rx-packet");
    }

    public @Nullable String getUptime() {
        return getProp("uptime");
    }

    public @Nullable LocalDateTime getUptimeStart() {
        return Converter.routerosPeriodBack(getUptime());
    }
}
