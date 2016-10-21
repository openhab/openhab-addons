package org.openhab.binding.zoneminder.internal.data;

import org.openhab.binding.zoneminder.internal.api.ServerVersion;

public class ZoneMinderServerData extends ZoneMinderData {

    private ServerVersion _serverVersion = null;
    // private DaemonStatus _serverDaemonStatus = null;

    public ZoneMinderServerData(ServerVersion version) {

        this._serverVersion = version;
        // this._daemonStatus = daemonStatus;

    }
}
