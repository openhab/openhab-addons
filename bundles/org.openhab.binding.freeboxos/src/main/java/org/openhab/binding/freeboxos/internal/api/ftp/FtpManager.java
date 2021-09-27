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
import org.openhab.binding.freeboxos.internal.api.ConfigurableRest;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.ftp.FtpConfig.FtpConfigResponse;

/**
 * The {@link FtpManager} is the Java class used to handle api requests
 * related to ftp
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FtpManager extends ConfigurableRest<FtpConfig, FtpConfigResponse> {
    private static final String FTP_PATH = "ftp";

    public FtpManager(FreeboxOsSession session) {
        super(FTP_PATH, CONFIG_SUB_PATH, session, FtpConfigResponse.class);
    }

    public boolean getFtpStatus() throws FreeboxException {
        return getConfig().isEnabled();
    }

    public boolean setFtpStatus(boolean enable) throws FreeboxException {
        FtpConfig config = getConfig();
        config.setEnabled(enable);
        return setConfig(config).isEnabled();
    }
}
