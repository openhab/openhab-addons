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
package org.openhab.binding.freeboxos.internal.api.netshare;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.ActivableConfig;

/**
 * The {@link SambaConfig} is the Java class used to map answer
 * returned by the Samba configuration API
 * https://dev.freebox.fr/sdk/os/network_share/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SambaConfig implements ActivableConfig {
    // Response classes
    public static class SambaConfigResponse extends Response<SambaConfig> {
    }

    private boolean fileShareEnabled;
    private boolean printShareEnabled;
    protected boolean logonEnabled;
    protected @Nullable String logonUser;
    protected @Nullable String logonPassword;
    protected @Nullable String workgroup;

    @Override
    public boolean isEnabled() {
        return fileShareEnabled;
    }

    @Override
    public void setEnabled(boolean fileShareEnabled) {
        this.fileShareEnabled = fileShareEnabled;
    }

    public boolean isPrintShareEnabled() {
        return printShareEnabled;
    }

    public void setPrintShareEnabled(boolean printShareEnabled) {
        this.printShareEnabled = printShareEnabled;
    }
}
