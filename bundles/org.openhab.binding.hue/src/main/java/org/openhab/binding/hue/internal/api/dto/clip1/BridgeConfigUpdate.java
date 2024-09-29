/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.api.dto.clip1;

/**
 * Collection of updates to the bridge configuration.
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding, minor code cleanup
 * @author Samuel Leisering - added Sensor support
 */
public class BridgeConfigUpdate extends ConfigUpdate {
    /**
     * Set the port of the proxy or null if there is no proxy.
     *
     * @param port port for proxy
     * @return this object for chaining calls
     */
    public BridgeConfigUpdate setProxyPort(Integer port) {
        if (port != null && port < 0) {
            throw new IllegalArgumentException("Invalid value for port");
        }

        commands.add(new Command("proxyport", port == null ? 0 : port));
        return this;
    }

    /**
     * Set the name of the bridge, which also functions as the UPnP name.
     *
     * @param name new name [4..16]
     * @return this object for chaining calls
     */
    public BridgeConfigUpdate setName(String name) {
        if (Util.stringSize(name) < 4 || Util.stringSize(name) > 16) {
            throw new IllegalArgumentException("Bridge name must be between 4 and 16 characters long");
        }

        commands.add(new Command("name", name));
        return this;
    }

    /**
     * Set the address of the proxy or null if there is no proxy.
     *
     * @param ip ip of proxy
     * @return this object for chaining calls
     */
    public BridgeConfigUpdate setProxyAddress(String ip) {
        if (ip != null && Util.stringSize(ip) > 40) {
            throw new IllegalArgumentException("Bridge proxy address can be at most 40 characters long");
        }

        commands.add(new Command("proxyaddress", ip == null ? "none" : ip));
        return this;
    }

    /**
     * Set whether the link button has been pressed within the last 30 seconds or not.
     *
     * @param pressed true for pressed, false for not pressed
     * @return this object for chaining calls
     */
    public BridgeConfigUpdate setLinkButton(boolean pressed) {
        commands.add(new Command("linkbutton", pressed));
        return this;
    }

    /**
     * Set the IP address of the bridge.
     *
     * @param ip ip address of bridge
     * @return this object for chaining calls
     */
    public BridgeConfigUpdate setIPAddress(String ip) {
        commands.add(new Command("ipaddress", ip));
        return this;
    }

    /**
     * Set the network mask of the bridge.
     *
     * @param netmask network mask
     * @return this object for chaining calls
     */
    public BridgeConfigUpdate setNetworkMask(String netmask) {
        commands.add(new Command("netmask", netmask));
        return this;
    }

    /**
     * Set the gateway address of the bridge.
     *
     * @param ip gateway address
     * @return this object for chaining calls
     */
    public BridgeConfigUpdate setGateway(String ip) {
        commands.add(new Command("gateway", ip));
        return this;
    }

    /**
     * Set whether the bridge uses DHCP to get an ip address or not.
     *
     * @param enabled dhcp enabled
     * @return this object for chaining calls
     */
    public BridgeConfigUpdate setDHCP(boolean enabled) {
        commands.add(new Command("dhcp", enabled));
        return this;
    }
}
