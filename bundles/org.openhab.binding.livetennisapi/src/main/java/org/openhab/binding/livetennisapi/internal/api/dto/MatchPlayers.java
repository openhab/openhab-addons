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
 * The two participants of a match. Each side ({@link #p1}, {@link #p2}) is a {@link Player}, which for a doubles
 * match is a doubles team rather than an individual — so singles and doubles share this same two-sided shape.
 *
 * @author Ben Synapse - Initial contribution
 */
public class MatchPlayers {

    public @Nullable Player p1;
    public @Nullable Player p2;
}
