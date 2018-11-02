/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class representing Kodi system properties (https://kodi.wiki/view/JSON-RPC_API/v9#System.Property.Value)
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class KodiSystemProperties {
    private boolean canhibernate;
    private boolean canreboot;
    private boolean cansuspend;
    private boolean canshutdown;

    public boolean canHibernate() {
        return canhibernate;
    }

    public void setCanhibernate(boolean canhibernate) {
        this.canhibernate = canhibernate;
    }

    public boolean canReboot() {
        return canreboot;
    }

    public void setCanreboot(boolean canreboot) {
        this.canreboot = canreboot;
    }

    public boolean canSuspend() {
        return cansuspend;
    }

    public void setCansuspend(boolean cansuspend) {
        this.cansuspend = cansuspend;
    }

    public boolean canShutDown() {
        return canshutdown;
    }

    public void setCanshutdown(boolean canshutdown) {
        this.canshutdown = canshutdown;
    }

    public boolean canQuit() {
        return !canhibernate && !canreboot && !canshutdown && !cansuspend;
    }
}
