/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
