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
package org.openhab.binding.thekeys.internal.api.model;

/**
 * Response DTO for /locker_status endpoint
 *
 * @author Jordan Martin - Initial contribution
 */
public class LockerStatusDTO {

    public static final int NEED_RESYNC = 0x26;
    public static final int CODE_LOCK_CLOSE = 0x31;
    public static final int CODE_LOCK_OPEN = 0x32;

    private String status;
    private int code;
    private String cause;
    private int id;
    private int version;
    private int position;
    private int rssi;
    private int battery;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public boolean isClosed() {
        return CODE_LOCK_CLOSE == code;
    }

    public boolean isOpened() {
        return CODE_LOCK_OPEN == code;
    }

    public boolean isNeedResync() {
        return NEED_RESYNC == code;
    }
}
