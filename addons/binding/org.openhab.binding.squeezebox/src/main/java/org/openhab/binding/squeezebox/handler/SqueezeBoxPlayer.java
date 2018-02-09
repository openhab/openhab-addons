/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.handler;

/**
 * Represents a Squeeze Player
 *
 * @author Dan Cunningham
 *
 */
public class SqueezeBoxPlayer {
    public String macAddress;
    public String name;
    public String ipAddr;
    public String model;
    public String uuid;

    public SqueezeBoxPlayer() {
        super();
    }

    /**
     * UID of player
     *
     * @return
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * UID of player
     *
     * @param uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Mac Address of player
     *
     * @param macAddress
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Mac Address of player
     *
     * @param macAddress
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * The name (label) of a player
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The name (label) of a player
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The ip address of a player
     *
     * @return
     */
    public String getIpAddr() {
        return ipAddr;
    }

    /**
     * The ip address of a player
     *
     * @param ipAddr
     */
    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    /**
     * The type of player
     *
     * @return
     */
    public String getModel() {
        return model;
    }

    /**
     * The type of player
     *
     * @param model
     */
    public void setModel(String model) {
        this.model = model;
    }
}
