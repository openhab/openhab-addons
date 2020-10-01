/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FtpConfig} is the Java class used to map the "FtpConfig"
 * structure used by the FTP configuration API
 * https://dev.freebox.fr/sdk/os/ftp/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FtpConfig {
    protected boolean enabled;
    protected boolean allowAnonymous;
    protected boolean allowAnonymousWrite;
    protected String password;
    protected boolean allowRemoteAccess;
    protected boolean weakPassword;
    protected int portCtrl;
    protected int portData;
    protected String remoteDomain;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public boolean isAllowAnonymousWrite() {
        return allowAnonymousWrite;
    }

    public void setAllowAnonymousWrite(boolean allowAnonymousWrite) {
        this.allowAnonymousWrite = allowAnonymousWrite;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAllowRemoteAccess() {
        return allowRemoteAccess;
    }

    public void setAllowRemoteAccess(boolean allowRemoteAccess) {
        this.allowRemoteAccess = allowRemoteAccess;
    }

    public boolean isWeakPassword() {
        return weakPassword;
    }

    public void setWeakPassword(boolean weakPassword) {
        this.weakPassword = weakPassword;
    }

    public int getPortCtrl() {
        return portCtrl;
    }

    public void setPortCtrl(int portCtrl) {
        this.portCtrl = portCtrl;
    }

    public int getPortData() {
        return portData;
    }

    public void setPortData(int portData) {
        this.portData = portData;
    }

    public String getRemoteDomain() {
        return remoteDomain;
    }

    public void setRemoteDomain(String remoteDomain) {
        this.remoteDomain = remoteDomain;
    }
}
