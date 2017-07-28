/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.supla.entities;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public final class SuplaChannelStatus {
    private final boolean connected;
    private final boolean enabled;

    public SuplaChannelStatus(boolean connected, boolean enabled) {
        this.connected = connected;
        this.enabled = enabled;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "SuplaChannelStatus{" +
                "connected=" + connected +
                ", enabled=" + enabled +
                '}';
    }
}
