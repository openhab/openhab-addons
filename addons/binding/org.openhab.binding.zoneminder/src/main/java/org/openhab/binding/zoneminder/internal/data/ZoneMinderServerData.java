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
import org.openhab.binding.zoneminder.internal.api.ServerVersion;

public class ZoneMinderServerData extends ZoneMinderData {

    private ServerVersion _serverVersion = null;
    private ServerDiskUsage _serverDiskUsage = null;
    private ServerCpuLoad _serverCpuLoad = null;
    // private DaemonStatus _serverDaemonStatus = null;

    public ZoneMinderServerData(ServerVersion version, ServerDiskUsage diskUsage, ServerCpuLoad cpuLoad) {

        this._serverVersion = version;
        _serverDiskUsage = diskUsage;
        _serverCpuLoad = cpuLoad;
        // this._daemonStatus = daemonStatus;

    }

    public String getServerVersion() {
        return _serverVersion.version;
    }

    public String getServerVersionApi() {
        return _serverVersion.apiversion;
    }

    public String getServerDiskUsage() {
        return _serverDiskUsage.getSpace();

    }

    public Double getServerCpuLoad() {
        return _serverCpuLoad.getCpuLoad();

    }
}
