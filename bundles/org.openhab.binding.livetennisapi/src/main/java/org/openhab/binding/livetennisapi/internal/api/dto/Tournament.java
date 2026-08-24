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
package org.openhab.binding.livetennisapi.internal.api.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A tournament of the catalogue as returned by the Live Tennis API.
 *
 * @author Ben Abulafia - Initial contribution
 */
public class Tournament {

    public @Nullable String id;
    public @Nullable String name;
    public @Nullable String tour;
    public @Nullable String surface;
    public @Nullable Boolean indoor;
    public @Nullable String city;
    public @Nullable String country;
    public @Nullable String category;
}
