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
package org.openhab.binding.amberelectric.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Container class for Sites, related to amberelectric
 *
 * @author Paul Smedley - Initial Contribution
 *
 */

@NonNullByDefault
public class Sites {
    public String id = "";
    public String nmi = "";
    public @NonNullByDefault({}) Channels[] channels;
    public String network = "";
    public String status = "";
    public String activeFrom = "";
    public int intervalLength;

    public class Channels {
        public String identifier = "";
        public String type = "";
        public String tariff = "";
    }

    public Sites() {
    }
}
