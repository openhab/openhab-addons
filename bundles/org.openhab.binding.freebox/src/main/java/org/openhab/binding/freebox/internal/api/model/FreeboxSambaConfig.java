/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link FreeboxSambaConfig} is the Java class used to map the "SambaConfig"
 * structure used by the Samba configuration API
 * https://dev.freebox.fr/sdk/os/network_share/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxSambaConfig {
    private Boolean fileShareEnabled;
    private Boolean printShareEnabled;
    private Boolean logonEnabled;
    private String logonUser;
    private String logonPassword;
    private String workgroup;

    public Boolean isFileShareEnabled() {
        return fileShareEnabled;
    }

    public void setFileShareEnabled(Boolean fileShareEnabled) {
        this.fileShareEnabled = fileShareEnabled;
    }

    public Boolean isPrintShareEnabled() {
        return printShareEnabled;
    }

    public void setPrintShareEnabled(Boolean printShareEnabled) {
        this.printShareEnabled = printShareEnabled;
    }

    public Boolean isLogonEnabled() {
        return logonEnabled;
    }

    public void setLogonEnabled(Boolean logonEnabled) {
        this.logonEnabled = logonEnabled;
    }

    public String getLogonUser() {
        return logonUser;
    }

    public void setLogonUser(String logonUser) {
        this.logonUser = logonUser;
    }

    public String getWorkgroup() {
        return workgroup;
    }

    public void setWorkgroup(String workgroup) {
        this.workgroup = workgroup;
    }

    public void setLogonPassword(String logonPassword) {
        this.logonPassword = logonPassword;
    }
}
