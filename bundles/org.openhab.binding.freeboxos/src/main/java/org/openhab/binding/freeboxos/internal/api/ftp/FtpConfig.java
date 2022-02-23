/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.ActivableConfig;

/**
 * The {@link FtpConfig} is the Java class used to map the "FtpConfig"
 * structure used by the FTP configuration API
 * https://dev.freebox.fr/sdk/os/ftp/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FtpConfig implements ActivableConfig {
    public static class FtpConfigResponse extends Response<FtpConfig> {
    }

    protected boolean enabled;
    protected boolean allowAnonymous;
    protected boolean allowAnonymousWrite;
    protected String password = "";
    protected boolean allowRemoteAccess;
    protected boolean weakPassword;
    protected int portCtrl;
    protected int portData;
    protected String remoteDomain = "";

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
