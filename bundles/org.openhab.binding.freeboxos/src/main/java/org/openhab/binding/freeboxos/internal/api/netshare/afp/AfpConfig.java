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
package org.openhab.binding.freeboxos.internal.api.netshare.afp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ServerType;
import org.openhab.binding.freeboxos.internal.rest.ActivableConfigIntf;

/**
 * The {@link AfpConfig} is the Java class used to map answer returned by the AFP configuration API
 *
 * https://dev.freebox.fr/sdk/os/network_share/#AfpConfig
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AfpConfig implements ActivableConfigIntf {
    private boolean enabled;
    private boolean guestAllow;
    private ServerType serverType = ServerType.UNKNOWN;
    private @Nullable String loginName;
    private @Nullable String loginPassword;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (loginPassword == null) {
            loginPassword = "";
        }
    }

    public boolean isGuestAllow() {
        return guestAllow;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public @Nullable String getLoginName() {
        return loginName;
    }

    public @Nullable String getLoginPassword() {
        return loginPassword;
    }

}
