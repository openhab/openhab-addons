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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Envelope of the match list endpoints ({@code {"data": [...], "meta": {...}}}).
 *
 * @author Ben Abulafia - Initial contribution
 */
public class MatchListResponse {

    public @Nullable List<Match> data;
    public @Nullable Meta meta;

    /**
     * Pagination metadata. {@code has_more} is authoritative for whether further pages exist and must be read rather
     * than comparing {@code count} to the requested {@code limit}.
     */
    public static class Meta {

        public @Nullable Integer limit;
        public @Nullable Integer offset;
        public @Nullable Integer count;
        public @Nullable Integer total;
        public @Nullable Boolean hasMore;
    }
}
