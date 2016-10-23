/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.data;

import org.openhab.binding.zoneminder.internal.api.ServerCpuLoad;
import org.openhab.binding.zoneminder.internal.api.ServerDiskUsage;
import org.openhab.binding.zoneminder.internal.api.ServerData;

public class ZoneMinderServerData extends ZoneMinderData {

    private ServerData _serverVersion = null;
    private ServerDiskUsage _serverDiskUsage = null;
    private ServerCpuLoad _serverCpuLoad = null;
    // private DaemonStatus _serverDaemonStatus = null;

    public ZoneMinderServerData(ServerData version, ServerDiskUsage diskUsage, ServerCpuLoad cpuLoad) {

        this._serverVersion = version;
        _serverDiskUsage = diskUsage;
        _serverCpuLoad = cpuLoad;
        // this._daemonStatus = daemonStatus;

    }

    public void setServerVersionData(ServerData data) {
        _serverVersion = data;
    }

    public void setServerDiskUsageData(ServerDiskUsage data) {
        _serverDiskUsage = data;
    }

    public void setServerCpuLoadData(ServerCpuLoad data) {
        _serverCpuLoad = data;
    }

    public ServerData getServerVersionData() {
        return _serverVersion;
    }

    public ServerDiskUsage getServerDiskUsageData() {
        return _serverDiskUsage;
    }

    public ServerCpuLoad getServerCpuLoadData() {
        return _serverCpuLoad;
    }

    public String getServerVersion() {
        if (_serverVersion != null) {
            return _serverVersion.version;
        }
        return "";
    }

    public String getServerVersionApi() {
        if (_serverVersion != null) {
            return _serverVersion.apiversion;
        }
        return "";
    }

    /*
     * public String getServerDiskUsage() {
     * if (_serverDiskUsage != null) {
     * return _serverDiskUsage.getSpace();
     * }
     * return "";
     * }
     */
    public Double getServerCpuLoad() {
        if (_serverDiskUsage != null) {
            return _serverCpuLoad.getCpuLoad();
        }
        return null;

    }
}
