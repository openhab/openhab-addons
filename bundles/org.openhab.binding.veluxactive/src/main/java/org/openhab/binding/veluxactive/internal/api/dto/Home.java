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
package org.openhab.binding.veluxactive.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Home topology and status, including modules and rooms.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class Home {
    public @Nullable String id;
    public @Nullable String name;
    public @Nullable String timezone;
    public @Nullable List<@Nullable Module> modules;
    public @Nullable List<@Nullable Room> rooms;
}
