/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.system;

import java.util.List;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.base.UniFiProtectModel;

/**
 * Liveview model for UniFi Protect
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Liveview extends UniFiProtectModel {

    public String name;
    public Boolean isDefault;
    public Boolean isGlobal;
    public String layout;
    public List<LiveviewSlot> slots;
    public String owner;

    public static class LiveviewSlot {
        public List<String> cameras;
        public Integer cycleInterval;
        public String cycleMode;
    }
}
