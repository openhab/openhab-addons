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
package org.openhab.binding.mikrotik.internal.model;

import static org.openhab.binding.mikrotik.internal.model.RouterosDevice.PROP_ID_KEY;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.util.Converter;

/**
 * The {@link RouterosInterfaceBase} is a base model class for network interface models having casting accessors for
 * data that is same for all interface types.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public abstract class RouterosInterfaceBase extends RouterosBaseData {
    protected @Nullable RouterosInterfaceType type;

    public RouterosInterfaceBase(Map<String, String> props) {
        super(props);
        this.type = RouterosInterfaceType.resolve(getType());
    }

    public @Nullable String getProperty(String propName) {
        return getProp(propName);
    }

    public abstract RouterosInterfaceType getDesignedType();

    public abstract boolean hasDetailedReport();

    public abstract boolean hasMonitor();

    public String getApiType() {
        return getDesignedType().toString();
    };

    public boolean validate() {
        return getDesignedType() == this.type;
    }

    public @Nullable String getId() {
        return getProp(PROP_ID_KEY);
    }

    public @Nullable String getType() {
        return getProp("type");
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

    public boolean isEnabled() {
        return "false".equals(getProp("disabled", ""));
    }

    public boolean isConnected() {
        return "true".equals(getProp("running", ""));
    }

    public @Nullable Integer getLinkDowns() {
        return getIntProp("link-downs");
    }

    public @Nullable LocalDateTime getLastLinkDownTime() {
        return Converter.fromRouterosTime(getProp("last-link-down-time"));
    }

    public @Nullable LocalDateTime getLastLinkUpTime() {
        return Converter.fromRouterosTime(getProp("last-link-up-time"));
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

    public @Nullable BigInteger getTxDrops() {
        return getBigIntProp("tx-drop");
    }

    public @Nullable BigInteger getRxDrops() {
        return getBigIntProp("rx-drop");
    }

    public @Nullable BigInteger getTxErrors() {
        return getBigIntProp("tx-error");
    }

    public @Nullable BigInteger getRxErrors() {
        return getBigIntProp("rx-error");
    }
}
