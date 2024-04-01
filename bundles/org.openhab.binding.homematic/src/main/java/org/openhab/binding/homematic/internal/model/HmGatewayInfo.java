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
package org.openhab.binding.homematic.internal.model;

/**
 * Info object which holds gateway specific informations.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HmGatewayInfo {
    public static final String ID_HOMEGEAR = "HOMEGEAR";
    public static final String ID_CCU = "CCU";
    public static final String ID_DEFAULT = "DEFAULT";

    private String id;
    private String type;
    private String firmware;
    private String address;
    private boolean rfInterface;
    private boolean wiredInterface;
    private boolean cuxdInterface;
    private boolean hmipInterface;
    private boolean groupInterface;

    /**
     * Returns the id of the gateway type.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the gateway type.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the type of the gateway.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the server.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the firmware version of the gateway.
     */
    public String getFirmware() {
        return firmware;
    }

    /**
     * Sets the firmware version of the gateway.
     */
    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    /**
     * Returns the address of the Homematic gateway.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address of the Homematic gateway.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns true, if the gateway is a Homegear gateway.
     */
    public boolean isHomegear() {
        return ID_HOMEGEAR.equals(id);
    }

    /**
     * Returns true, if the gateway is a CCU.
     */
    public boolean isCCU() {
        return ID_CCU.equals(id);
    }

    /**
     * Return true, if the gateway is a CCU1.
     */
    public boolean isCCU1() {
        return "CCU".equals(type);
    }

    /**
     * Returns true, if the gateway supports the CUxD interface.
     */
    public boolean isCuxdInterface() {
        return cuxdInterface;
    }

    /**
     * Sets the CUxD support of the gateway.
     */
    public void setCuxdInterface(boolean cuxdInterface) {
        this.cuxdInterface = cuxdInterface;
    }

    /**
     * Returns true, if the gateway supports the wired interface.
     */
    public boolean isWiredInterface() {
        return wiredInterface;
    }

    /**
     * Sets the wired support of the gateway.
     */
    public void setWiredInterface(boolean wiredInterface) {
        this.wiredInterface = wiredInterface;
    }

    /**
     * Returns true, if the gateway supports the HMIP interface.
     */
    public boolean isHmipInterface() {
        return hmipInterface;
    }

    /**
     * Sets the HMIP support of the gateway.
     */
    public void setHmipInterface(boolean hmipInterface) {
        this.hmipInterface = hmipInterface;
    }

    /**
     * Returns true, if the gateway supports the Group interface.
     */
    public boolean isGroupInterface() {
        return groupInterface;
    }

    /**
     * Sets the Group support of the gateway.
     */
    public void setGroupInterface(boolean groupInterface) {
        this.groupInterface = groupInterface;
    }

    /**
     * Returns true, if the gateway supports the RF interface.
     */
    public boolean isRfInterface() {
        return rfInterface;
    }

    /**
     * Sets the RF support of the gateway.
     */
    public void setRfInterface(boolean rfInterface) {
        this.rfInterface = rfInterface;
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s,type=%s,firmware=%s,address=%s,rf=%b,wired=%b,hmip=%b,cuxd=%b,group=%b]",
                getClass().getSimpleName(), id, type, firmware, address, rfInterface, wiredInterface, hmipInterface,
                cuxdInterface, groupInterface);
    }
}
