/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.api.dto;

import java.util.List;

/**
 * Liveview layout definition available in Protect.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Liveview {
    public String id;
    public ModelKey modelKey; // "liveview"
    public String name;
    public boolean isDefault;
    public boolean isGlobal;
    public String owner; // userId
    public Integer layout; // 1-26
    public List<LiveviewSlot> slots;
}
