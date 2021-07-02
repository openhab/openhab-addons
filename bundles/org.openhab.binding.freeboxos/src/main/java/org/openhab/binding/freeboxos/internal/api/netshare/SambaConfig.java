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
package org.openhab.binding.freeboxos.internal.api.netshare;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SambaConfig} is the Java class used to map the "SambaConfig"
 * structure used by the Samba configuration API
 * https://dev.freebox.fr/sdk/os/network_share/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SambaConfig {
    private boolean fileShareEnabled;
    private boolean printShareEnabled;
    protected boolean logonEnabled;
    protected @Nullable String logonUser;
    protected @Nullable String logonPassword;
    protected @Nullable String workgroup;

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
}
