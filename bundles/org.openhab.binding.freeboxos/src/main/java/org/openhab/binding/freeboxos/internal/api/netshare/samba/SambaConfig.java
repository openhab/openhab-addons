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
package org.openhab.binding.freeboxos.internal.api.netshare.samba;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SambaConfig} is the Java class used to map answer returned by the Samba configuration API
 *
 * https://dev.freebox.fr/sdk/os/network_share/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SambaConfig {
    private boolean fileShareEnabled;
    private boolean printShareEnabled;
    private boolean logonEnabled;
    private @Nullable String logonUser;
    private @Nullable String logonPassword;
    private @Nullable String workgroup;
    private boolean smbv2Enabled;

    public boolean isFileShareEnabled() {
        return fileShareEnabled;
    }

    public void setFileShareEnabled(boolean fileShareEnabled) {
        this.fileShareEnabled = fileShareEnabled;
    }

    public boolean isPrintShareEnabled() {
        return printShareEnabled;
    }

    public void setPrintShareEnabled(boolean printShareEnabled) {
        this.printShareEnabled = printShareEnabled;
    }

    public boolean isLogonEnabled() {
        return logonEnabled;
    }

    public @Nullable String getLogonUser() {
        return logonUser;
    }

    public @Nullable String getLogonPassword() {
        return logonPassword;
    }

    public @Nullable String getWorkgroup() {
        return workgroup;
    }

    public boolean isSmbv2Enabled() {
        return smbv2Enabled;
    }
}
