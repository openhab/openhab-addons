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
package org.openhab.binding.freeboxos.internal.api.ftp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;

/**
 * The {@link FtpManager} is the Java class used to handle api requests
 * related to ftp
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FtpManager extends RestManager {

    public FtpManager(ApiHandler apiHandler) {
        super(apiHandler, "ftp");
    }

    public boolean getFtpStatus() throws FreeboxException {
        return getFtpConfig().isEnabled();
    }

    public boolean changeFtpStatus(boolean enable) throws FreeboxException {
        FtpConfig config = getFtpConfig();
        config.setEnabled(enable);
        return put("config", config, FtpConfigResponse.class).isEnabled();
    }

    private FtpConfig getFtpConfig() throws FreeboxException {
        return get("config", FtpConfigResponse.class, true);
    }

    // Response classes and validity evaluations
    private static class FtpConfigResponse extends Response<FtpConfig> {
    }
}
