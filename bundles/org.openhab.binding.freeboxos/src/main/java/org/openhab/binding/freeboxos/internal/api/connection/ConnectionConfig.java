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
package org.openhab.binding.freeboxos.internal.api.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.SigAlpStatus;

/**
 * The {@link ConnectionConfig} is the Java class used to map the "ConnectionConfiguration" structure used by the
 * connection
 * API
 *
 * https://dev.freebox.fr/sdk/os/connection/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConnectionConfig {
    private boolean ping;
    private boolean isSecurePass;
    private boolean remoteAccess;
    private int remoteAccessPort;
    private int remoteAccessMinPort;
    private int remoteAccessMaxPort;
    private @Nullable String remoteAccessIp;
    private boolean apiRemoteAccess;
    private boolean wol;
    private boolean adblock;
    private boolean adblockNotSet;
    private boolean allowTokenRequest;
    private SigAlpStatus sigAlp = SigAlpStatus.UNKNOWN;

    public boolean isPing() {
        return ping;
    }

    public void setPing(boolean ping) {
        this.ping = ping;
    }

    public boolean isSecurePass() {
        return isSecurePass;
    }

    public boolean isRemoteAccess() {
        return remoteAccess;
    }

    public void setRemoteAccess(boolean remoteAccess) {
        this.remoteAccess = remoteAccess;
    }

    public int getRemoteAccessPort() {
        return remoteAccessPort;
    }

    public void setRemoteAccessPort(int remoteAccessPort) {
        this.remoteAccessPort = remoteAccessPort;
    }

    public int getRemoteAccessMinPort() {
        return remoteAccessMinPort;
    }

    public int getRemoteAccessMaxPort() {
        return remoteAccessMaxPort;
    }

    public @Nullable String getRemoteAccessIp() {
        return remoteAccessIp;
    }

    public boolean isApiRemoteAccess() {
        return apiRemoteAccess;
    }

    public boolean isWol() {
        return wol;
    }

    public void setWol(boolean wol) {
        this.wol = wol;
    }

    public boolean isAdblock() {
        return adblock;
    }

    public void setAdblock(boolean adblock) {
        this.adblock = adblock;
    }

    public boolean isAdblockNotSet() {
        return adblockNotSet;
    }

    public boolean isAllowTokenRequest() {
        return allowTokenRequest;
    }

    public void setAllowTokenRequest(boolean allowTokenRequest) {
        this.allowTokenRequest = allowTokenRequest;
    }

    public SigAlpStatus getSigAlp() {
        return sigAlp;
    }

    public void setSigAlp(SigAlpStatus sigAlp) {
        this.sigAlp = sigAlp;
    }

}
