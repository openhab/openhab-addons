/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
