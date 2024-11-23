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
package org.openhab.binding.freebox.internal.api.model;

import java.util.List;

/**
 * The {@link FreeboxSystemConfig} is the Java class used to map the "SystemConfig"
 * structure used by the system API
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxSystemConfig {
    private String firmwareVersion;
    private String mac;
    private String serial;
    private String uptime;
    private long uptimeVal;
    private String boardName;
    private int tempCpum;
    private int tempSw;
    private int tempCpub;
    private int fanRpm;
    private boolean boxAuthenticated;
    private String diskStatus;
    private String boxFlavor;
    private String userMainStorage;
    private List<FreeboxSensor> fans;
    private List<FreeboxSensor> sensors;

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getMac() {
        return mac;
    }

    public String getSerial() {
        return serial;
    }

    public String getUptime() {
        return uptime;
    }

    public long getUptimeVal() {
        return uptimeVal;
    }

    public String getBoardName() {
        return boardName;
    }

    public int getTempCpum() {
        if (sensors != null) {
            for (FreeboxSensor sensor : sensors) {
                if ("temp_cpum".equals(sensor.getId())) {
                    return sensor.getValue();
                }
            }
        }
        return tempCpum;
    }

    public int getTempSw() {
        if (sensors != null) {
            for (FreeboxSensor sensor : sensors) {
                if ("temp_sw".equals(sensor.getId())) {
                    return sensor.getValue();
                }
            }
        }
        return tempSw;
    }

    public int getTempCpub() {
        if (sensors != null) {
            for (FreeboxSensor sensor : sensors) {
                if ("temp_cpub".equals(sensor.getId())) {
                    return sensor.getValue();
                }
            }
        }
        return tempCpub;
    }

    public int getFanRpm() {
        if (fans != null) {
            for (FreeboxSensor fan : fans) {
                if ("fan0_speed".equals(fan.getId())) {
                    return fan.getValue();
                }
            }
        }
        return fanRpm;
    }

    public boolean isBoxAuthenticated() {
        return boxAuthenticated;
    }

    public String getDiskStatus() {
        return diskStatus;
    }

    public String getBoxFlavor() {
        return boxFlavor;
    }

    public String getUserMainStorage() {
        return userMainStorage;
    }
}
