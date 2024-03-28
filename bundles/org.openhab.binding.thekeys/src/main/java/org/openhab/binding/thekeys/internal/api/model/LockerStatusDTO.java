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
package org.openhab.binding.thekeys.internal.api.model;

/**
 * Response DTO for /locker_status endpoint
 *
 * @author Jordan Martin - Initial contribution
 */
public class LockerStatusDTO {

    private static final int NEED_RESYNC = 0x26;
    private static final int CODE_LOCK_CLOSE = 0x31;
    private static final int CODE_LOCK_OPEN = 0x32;

    private String status;
    private int code;
    private String cause;
    private int version;
    private int position;
    private int rssi;
    private int battery;

    public String getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    public int getVersion() {
        return version;
    }

    public int getPosition() {
        return position;
    }

    public int getRssi() {
        return rssi;
    }

    public int getBattery() {
        return battery;
    }

    public String getCause() {
        return cause;
    }

    public boolean isClosed() {
        return CODE_LOCK_CLOSE == code;
    }

    public boolean isOpened() {
        return CODE_LOCK_OPEN == code;
    }

    public boolean needsResync() {
        return NEED_RESYNC == code;
    }
}
