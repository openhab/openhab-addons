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
 * The calling key's own usage and quota as returned by {@code GET /usage}. Calls to that endpoint are quota-exempt.
 *
 * @author Ben Synapse - Initial contribution
 */
public class Usage {

    public @Nullable String tier;
    public @Nullable Limits limits;
    public @Nullable Today today;
    public @Nullable String asOf;

    public static class Limits {
        public @Nullable Integer perMinute;
        public @Nullable Integer perDay;
    }

    public static class Today {
        public @Nullable Integer calls;
        public @Nullable Integer errors;
        public @Nullable Integer remainingDay;
    }
}
