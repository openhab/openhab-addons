/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.upnpav;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;

/**
 * The {@link UPnPAVManager} is the Java class used to handle api requests
 * related to UPnP AV
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class UPnPAVManager extends RestManager {
    private static String UPNPAV_URL = "upnpav/config";

    public UPnPAVManager(ApiHandler apiHandler) {
        super(apiHandler, UPNPAV_URL);
    }

    public boolean getStatus() throws FreeboxException {
        return get(null, UPnPAVConfigResponse.class, true).isEnabled();
    }

    public boolean changeStatus(boolean enable) throws FreeboxException {
        UPnPAVConfig config = new UPnPAVConfig();
        config.setEnabled(enable);
        return put(null, config, UPnPAVConfigResponse.class).isEnabled();
    }

    // Response classes and validity evaluations
    private static class UPnPAVConfigResponse extends Response<UPnPAVConfig> {
    }
}
