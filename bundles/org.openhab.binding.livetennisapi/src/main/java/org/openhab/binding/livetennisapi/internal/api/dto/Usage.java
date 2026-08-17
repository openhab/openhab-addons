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

/**
 * The calling key's own usage and quota as returned by {@code GET /usage}. Calls to that endpoint are quota-exempt.
 *
 * @author Ben - Initial contribution
 */
public class Usage {

    public String tier;
    public Limits limits;
    public Today today;
    public String asOf;

    public static class Limits {
        public Integer perMinute;
        public Integer perDay;
    }

    public static class Today {
        public Integer calls;
        public Integer errors;
        public Integer remainingDay;
    }
}
