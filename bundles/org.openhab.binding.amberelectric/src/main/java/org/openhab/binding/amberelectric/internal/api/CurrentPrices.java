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
 * Container class for Current Pricing, related to amberelectric
 *
 * @author Paul Smedley <paul@smedley.id.au> - Initial contribution
 *
 */
@NonNullByDefault
public class CurrentPrices {
    public String type = "";
    public String date = "";
    public int duration;
    public String startTime = "";
    public String endTime = "";
    public String nemTime = "";
    public double perKwh;
    public double renewables;
    public double spotPerKwh;
    public String channelType = "";
    public String spikeStatus = "";
    public String descriptor = "";
    public boolean estimate;
    public @NonNullByDefault({}) AdvancedPrice advancedPrice;

    public class AdvancedPrice {
        public double low;
        public double predicted;
        public double high;
    }

    private CurrentPrices() {
    }
}
