/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.freeboxos.internal.rest.ActivableConfigIntf;

/**
 * The {@link FtpConfig} is the Java class used to map the "FtpConfig" structure used by the FTP configuration API
 *
 * https://dev.freebox.fr/sdk/os/ftp/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FtpConfig implements ActivableConfigIntf {
    private boolean enabled;
    private String password = "";
    private boolean allowAnonymous;
    private boolean allowAnonymousWrite;
    private boolean allowRemoteAccess;
    private boolean weakPassword;
    private int portCtrl;
    private int portData;
    private String remoteDomain = "";

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    public boolean isAllowAnonymousWrite() {
        return allowAnonymousWrite;
    }

    public boolean isAllowRemoteAccess() {
        return allowRemoteAccess;
    }

    public boolean isWeakPassword() {
        return weakPassword;
    }

    public int getPortCtrl() {
        return portCtrl;
    }

    public int getPortData() {
        return portData;
    }

    public String getRemoteDomain() {
        return remoteDomain;
    }

}
