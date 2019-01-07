/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api.model;

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
        return tempCpum;
    }

    public int getTempSw() {
        return tempSw;
    }

    public int getTempCpub() {
        return tempCpub;
    }

    public int getFanRpm() {
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
