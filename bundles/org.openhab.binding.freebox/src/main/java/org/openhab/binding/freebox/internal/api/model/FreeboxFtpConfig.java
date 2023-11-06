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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxFtpConfig} is the Java class used to map the "FtpConfig"
 * structure used by the FTP configuration API
 * https://dev.freebox.fr/sdk/os/ftp/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxFtpConfig {
    private Boolean enabled;
    private Boolean allowAnonymous;
    private Boolean allowAnonymousWrite;
    private String password;
    private Boolean allowRemoteAccess;
    private Boolean weakPassword;
    private Integer portCtrl;
    private Integer portData;
    private String remoteDomain;

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(Boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public Boolean isAllowAnonymousWrite() {
        return allowAnonymousWrite;
    }

    public void setAllowAnonymousWrite(Boolean allowAnonymousWrite) {
        this.allowAnonymousWrite = allowAnonymousWrite;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean isAllowRemoteAccess() {
        return allowRemoteAccess;
    }

    public void setAllowRemoteAccess(Boolean allowRemoteAccess) {
        this.allowRemoteAccess = allowRemoteAccess;
    }

    public Boolean isWeakPassword() {
        return weakPassword;
    }

    public void setWeakPassword(Boolean weakPassword) {
        this.weakPassword = weakPassword;
    }

    public Integer getPortCtrl() {
        return portCtrl;
    }

    public void setPortCtrl(Integer portCtrl) {
        this.portCtrl = portCtrl;
    }

    public Integer getPortData() {
        return portData;
    }

    public void setPortData(Integer portData) {
        this.portData = portData;
    }

    public String getRemoteDomain() {
        return remoteDomain;
    }

    public void setRemoteDomain(String remoteDomain) {
        this.remoteDomain = remoteDomain;
    }
}
