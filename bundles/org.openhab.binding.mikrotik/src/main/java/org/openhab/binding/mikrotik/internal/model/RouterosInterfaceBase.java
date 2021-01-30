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

import org.joda.time.DateTime;
import org.openhab.binding.mikrotik.internal.util.Converter;

/**
 * The {@link RouterosInterfaceBase} is a base model class for network interface models having casting accessors for
 * data that is same for all interface types.
 *
 * @author Oleg Vivtash - Initial contribution
 */
public abstract class RouterosInterfaceBase {
    protected Map<String, String> propMap;
    protected RouterosInterfaceType type;

    public RouterosInterfaceBase(Map<String, String> props) {
        this.propMap = props;
        this.type = RouterosInterfaceType.resolve(getType());
    }

    public String getProperty(String propName) {
        return propMap.get(propName);
    }

    public abstract RouterosInterfaceType getDesignedType();

    public abstract boolean hasDetailedReport();

    public abstract boolean hasMonitor();

    public String getApiType() {
        return getDesignedType().toString();
    };

    public void mergeProps(Map<String, String> otherProps) {
        this.propMap.putAll(otherProps);
    }

    public boolean validate() {
        return getDesignedType() == this.type;
    }

    public String getId() {
        return propMap.get(PROP_ID_KEY);
    }

    public String getType() {
        return propMap.get("type");
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

    public boolean isEnabled() {
        return propMap.get("disabled").equals("false");
    }

    public boolean isConnected() {
        return propMap.get("running").equals("true");
    }

    public int getLinkDowns() {
        return Integer.parseInt(propMap.get("link-downs"));
    }

    public DateTime getLastLinkDownTime() {
        return Converter.fromRouterosTime(propMap.get("last-link-down-time"));
    }

    public DateTime getLastLinkUpTime() {
        return Converter.fromRouterosTime(propMap.get("last-link-up-time"));
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

    public BigInteger getTxDrops() {
        return new BigInteger(propMap.get("tx-drop"));
    }

    public BigInteger getRxDrops() {
        return new BigInteger(propMap.get("rx-drop"));
    }

    public BigInteger getTxErrors() {
        return new BigInteger(propMap.get("tx-error"));
    }

    public BigInteger getRxErrors() {
        return new BigInteger(propMap.get("rx-error"));
    }
}
