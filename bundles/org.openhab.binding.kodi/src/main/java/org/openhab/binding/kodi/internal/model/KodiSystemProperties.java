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
package org.openhab.binding.kodi.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class representing Kodi system properties (https://kodi.wiki/view/JSON-RPC_API/v9#System.Property.Value)
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class KodiSystemProperties {
    @SerializedName("canhibernate")
    private boolean canHibernate;
    @SerializedName("canreboot")
    private boolean canReboot;
    @SerializedName("cansuspend")
    private boolean canSuspend;
    @SerializedName("canshutdown")
    private boolean canShutdown;

    public boolean canHibernate() {
        return canHibernate;
    }

    public void setCanHibernate(boolean canHibernate) {
        this.canHibernate = canHibernate;
    }

    public boolean canReboot() {
        return canReboot;
    }

    public void setCanReboot(boolean canReboot) {
        this.canReboot = canReboot;
    }

    public boolean canSuspend() {
        return canSuspend;
    }

    public void setCansuspend(boolean canSuspend) {
        this.canSuspend = canSuspend;
    }

    public boolean canShutdown() {
        return canShutdown;
    }

    public void setCanShutdown(boolean canShutdown) {
        this.canShutdown = canShutdown;
    }

    public boolean canQuit() {
        return !canHibernate && !canReboot && !canShutdown && !canSuspend;
    }
}
